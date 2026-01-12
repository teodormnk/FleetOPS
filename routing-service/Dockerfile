FROM alpine:3.18 AS build
RUN apk add --no-cache build-base cmake curl rabbitmq-c rabbitmq-c-dev

WORKDIR /app

RUN mkdir -p include/nlohmann && \
    curl -L https://github.com/nlohmann/json/releases/download/v3.11.2/json.hpp -o include/nlohmann/json.hpp && \
    mkdir -p include && \
    curl -L https://raw.githubusercontent.com/yhirose/cpp-httplib/master/httplib.h -o include/httplib.h

COPY CMakeLists.txt .
COPY src ./src

RUN mkdir build && cd build && \
    cmake .. && \
    make

FROM alpine:3.18
RUN apk add --no-cache libstdc++ libgcc

WORKDIR /app
COPY --from=build /app/build/routing_app .

EXPOSE 8081
CMD ["./routing_app"]