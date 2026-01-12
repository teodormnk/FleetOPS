#include <iostream>
#include <vector>
#include <string>
#include <cmath>
#include <thread>
#include <chrono>

#include <sys/time.h>

#include <rabbitmq-c/amqp.h>
#include <rabbitmq-c/tcp_socket.h>

#include "nlohmann/json.hpp"

using json = nlohmann::json;

struct Point {
    double lat;
    double lon;
};

double calculateDistanceKm(Point p1, Point p2) {
    const double R = 6371.0;
    double dLat = (p2.lat - p1.lat) * M_PI / 180.0;
    double dLon = (p2.lon - p1.lon) * M_PI / 180.0;
    double lat1 = p1.lat * M_PI / 180.0;
    double lat2 = p2.lat * M_PI / 180.0;

    double a = std::pow(std::sin(dLat / 2), 2) +
               std::pow(std::sin(dLon / 2), 2) * std::cos(lat1) * std::cos(lat2);
    double c = 2 * std::asin(std::sqrt(a));
    return R * c;
}

void check_amqp_error(amqp_rpc_reply_t x, char const *context) {
    if (x.reply_type != AMQP_RESPONSE_NORMAL) {
        std::cerr << "Error in " << context << std::endl;
    }
}

int main() {
    std::cout << "Starting C++ Routing Service (Event Driven / RabbitMQ)..." << std::endl;

    std::this_thread::sleep_for(std::chrono::seconds(40));

    const char *hostname = "rabbitmq";
    int port = 5672;
    const char *queue_in = "order.queue";
    const char *queue_out = "order.route";

    amqp_connection_state_t conn;
    amqp_socket_t *socket = NULL;
    int status;

    while (true) {
            conn = amqp_new_connection();
            socket = amqp_tcp_socket_new(conn);

            if (!socket) {
                std::cerr << "Could not create TCP socket. Retrying in 5s..." << std::endl;
                std::this_thread::sleep_for(std::chrono::seconds(5));
                continue;
            }

            status = amqp_socket_open(socket, hostname, port);

            if (status) {
                std::cout << "RabbitMQ not ready yet (Port closed). Retrying in 5s..." << std::endl;
                amqp_destroy_connection(conn);
                std::this_thread::sleep_for(std::chrono::seconds(5));
                continue;
            } else {
                std::cout << "SUCCESS: Connected to RabbitMQ!" << std::endl;
                break;
            }
        }

    check_amqp_error(amqp_login(conn, "/", 0, 131072, 0, AMQP_SASL_METHOD_PLAIN, "guest", "guest"), "Logging in");
    amqp_channel_open(conn, 1);
    check_amqp_error(amqp_get_rpc_reply(conn), "Opening channel");

    amqp_basic_consume(conn, 1, amqp_cstring_bytes(queue_in), amqp_empty_bytes, 0, 1, 0, amqp_empty_table);
    check_amqp_error(amqp_get_rpc_reply(conn), "Consuming");

    std::cout << "Waiting for messages on " << queue_in << "..." << std::endl;

    while (1) {
        amqp_rpc_reply_t res;
        amqp_envelope_t envelope;

        amqp_maybe_release_buffers(conn);

        struct timeval timeout;
        timeout.tv_sec = 10;
        timeout.tv_usec = 0;

        res = amqp_consume_message(conn, &envelope, &timeout, 0);

        if (AMQP_RESPONSE_NORMAL == res.reply_type) {
            std::string msg_body((char *)envelope.message.body.bytes, envelope.message.body.len);
            std::cout << "Received Order: " << msg_body << std::endl;

            try {
                auto orderJson = json::parse(msg_body);

                double startLat = 44.4268, startLon = 26.1025; // Default: București
                double endLat = 44.5548, endLon = 26.0846;     // Default: Otopeni

                if (orderJson.contains("startLat")) startLat = orderJson["startLat"];
                if (orderJson.contains("startLon")) startLon = orderJson["startLon"];
                if (orderJson.contains("endLat")) endLat = orderJson["endLat"];
                if (orderJson.contains("endLon")) endLon = orderJson["endLon"];

                Point start = {startLat, startLon};
                Point end = {endLat, endLon};

                double distance = calculateDistanceKm(start, end);

                std::vector<json> routePoints;
                int steps = 10;
                for (int i = 0; i <= steps; ++i) {
                    double ratio = (double)i / steps;
                    double lat = start.lat + (end.lat - start.lat) * ratio;
                    double lon = start.lon + (end.lon - start.lon) * ratio;
                    if (i > 0 && i < steps) lat += 0.0005 * ((i % 2 == 0) ? 1 : -1);
                    routePoints.push_back({{"lat", lat}, {"lon", lon}});
                }

                json response;
                if(orderJson.contains("id")) response["orderId"] = orderJson["id"];

                response["distanceKm"] = distance;
                response["points"] = routePoints;
                response["status"] = "CALCULATED";

                std::string responseStr = response.dump();

                amqp_bytes_t message_bytes;
                message_bytes.len = responseStr.length();
                message_bytes.bytes = (void *)responseStr.c_str();

                amqp_basic_publish(conn, 1, amqp_cstring_bytes(""), amqp_cstring_bytes(queue_out),
                                   0, 0, NULL, message_bytes);

                std::cout << "Route calculated and sent to " << queue_out << std::endl;

            } catch (const std::exception& e) {
                std::cerr << "Error processing JSON: " << e.what() << std::endl;
            }

            amqp_destroy_envelope(&envelope);
        }
    }

    amqp_channel_close(conn, 1, AMQP_REPLY_SUCCESS);
    amqp_connection_close(conn, AMQP_REPLY_SUCCESS);
    amqp_destroy_connection(conn);
    return 0;
}