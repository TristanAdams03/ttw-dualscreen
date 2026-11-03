cmake_minimum_required(VERSION 3.25)
project(ttwds LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

include(FetchContent)
FetchContent_Declare(doctest
  URL https://github.com/doctest/doctest/archive/refs/tags/v2.4.11.tar.gz
  URL_HASH SHA256=632ed2c05a7f53fa961381497bf8069093f0d6628c5f26286161fbd32a560186
  DOWNLOAD_EXTRACT_TIMESTAMP TRUE)
FetchContent_Declare(nlohmann_json
  URL https://github.com/nlohmann/json/releases/download/v3.11.3/json.tar.xz
  URL_HASH SHA256=d6c65aca6b1ed68e7a182f4757257b107ae403032760ed6ef121c9d55e81757d
  DOWNLOAD_EXTRACT_TIMESTAMP TRUE)
FetchContent_MakeAvailable(doctest nlohmann_json)

file(GLOB TTWDS_NET_SRC CONFIGURE_DEPENDS src/net/*.cpp)
add_library(ttwds_core STATIC ${TTWDS_NET_SRC})
target_include_directories(ttwds_core PUBLIC src)
target_link_libraries(ttwds_core PUBLIC nlohmann_json::nlohmann_json)
if(MSVC)
  target_compile_options(ttwds_core PRIVATE /W4)
else()
  target_compile_options(ttwds_core PRIVATE -Wall -Wextra -Wpedantic)
endif()

enable_testing()
add_executable(ttwds_tests tests/main.cpp tests/test_net.cpp)
target_link_libraries(ttwds_tests PRIVATE ttwds_core doctest::doctest)
add_test(NAME ttwds_tests COMMAND ttwds_tests)
