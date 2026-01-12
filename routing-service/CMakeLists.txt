cmake_minimum_required(VERSION 3.10)
project(RoutingService)
set(CMAKE_CXX_STANDARD 17)

include_directories(include)
find_package(Threads REQUIRED)

find_library(RABBITMQ_LIB rabbitmq)
add_executable(routing_app src/main.cpp)

target_link_libraries(routing_app PRIVATE Threads::Threads)