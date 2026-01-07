#include <iostream>
#include "httplib.h"
#include "nlohmann/json.hpp"
#include <cmath>
#include <vector>

using json = nlohmann::json;
using namespace httplib;

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

int main() {
    Server svr;

    std::cout << "Starting C++ Routing Service on port 8081..." << std::endl;

    svr.Post("/route", [](const Request& req, Response& res) {
        try {
            auto body = json::parse(req.body);
            
            double startLat = body["startLat"];
            double startLon = body["startLon"];
            double endLat = body["endLat"];
            double endLon = body["endLon"];

            Point start = {startLat, startLon};
            Point end = {endLat, endLon};

            double distance = calculateDistanceKm(start, end);
            
            std::vector<json> routePoints;
            int steps = 10;
            for (int i = 0; i <= steps; ++i) {
                double ratio = (double)i / steps;
                double lat = start.lat + (end.lat - start.lat) * ratio;
                double lon = start.lon + (end.lon - start.lon) * ratio;

                if (i > 0 && i < steps) {
                    lat += 0.0005 * ((i % 2 == 0) ? 1 : -1);
                }
                
                routePoints.push_back({{"lat", lat}, {"lon", lon}});
            }

            json response;
            response["distanceKm"] = distance;
            response["estimatedDurationSeconds"] = (int)(distance * 60);
            response["points"] = routePoints;

            res.set_content(response.dump(), "application/json");
            
        } catch (const std::exception& e) {
            res.status = 400;
            res.set_content("Invalid JSON format", "text/plain");
            std::cout << "Error processing request: " << e.what() << std::endl;
        }
    });

    svr.listen("0.0.0.0", 8081);
    return 0;
}