FROM alpine:3.18 AS build
RUN apk add --no-cache build-base cmake

WORKDIR /app
COPY . .

RUN mkdir build && cd build && cmake .. && make

FROM alpine:3.18
RUN apk add --no-cache libstdc++

WORKDIR /app
COPY --from=build /app/build/routing_app .

EXPOSE 8081
CMD ["./routing_app"]