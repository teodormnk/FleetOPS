#include <iostream>
#include <thread>
#include <chrono>

int main()
{
    std::cout << "Routing Service C++ started..." << std::endl;
    while (true)
    {
        std::this_thread::sleep_for(std::chrono::seconds(10));
        std::cout << "Service is alive (waiting for requests)..." << std::endl;
    }
    return 0;
}