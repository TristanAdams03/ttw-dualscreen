cmake_minimum_required(VERSION 3.25)
project(ttwds LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)
# Static CRT (/MT) everywhere so the plugin DLL and its static libs agree.
set(CMAKE_MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>")
if(MSVC)
  add_compile_options(/utf-8 /permissive-)
endif()

include(FetchContent)
FetchContent_Declare(doctest
  URL https://github.com/doctest/doctest/archive/refs/tags/v2.4.11.tar.gz
  URL_HASH SHA256=632ed2c05a7f53fa961381497bf8069093f0d6628c5f26286161fbd32a560186
  DOWNLOAD_EXTRACT_TIMESTAMP TRUE
  SYSTEM)
FetchContent_Declare(nlohmann_json
  URL https://github.com/nlohmann/json/releases/download/v3.11.3/json.tar.xz
  URL_HASH SHA256=d6c65aca6b1ed68e7a182f4757257b107ae403032760ed6ef121c9d55e81757d
  DOWNLOAD_EXTRACT_TIMESTAMP TRUE
  SYSTEM)
# doctest 2.4.11 declares cmake_minimum_required(3.0); CMake 4 refuses that without this.
set(CMAKE_POLICY_VERSION_MINIMUM 3.5)
FetchContent_MakeAvailable(doctest nlohmann_json)
unset(CMAKE_POLICY_VERSION_MINIMUM)

# Warnings are errors on our own targets only (third-party headers are SYSTEM).
function(ttwds_warnings target)
  if(MSVC)
    target_compile_options(${target} PRIVATE /W4 /WX)
  else()
    target_compile_options(${target} PRIVATE -Wall -Wextra -Wpedantic -Werror)
  endif()
endfunction()

file(GLOB TTWDS_NET_SRC CONFIGURE_DEPENDS src/net/*.cpp)
file(GLOB TTWDS_PROTO_SRC CONFIGURE_DEPENDS src/proto/*.cpp)
set(TTWDS_SERVER_SRC)
if(WIN32)
  file(GLOB TTWDS_SERVER_SRC CONFIGURE_DEPENDS src/server/*.cpp)
endif()
add_library(ttwds_core STATIC ${TTWDS_NET_SRC} ${TTWDS_PROTO_SRC} ${TTWDS_SERVER_SRC})
target_include_directories(ttwds_core PUBLIC src)
target_link_libraries(ttwds_core PUBLIC nlohmann_json::nlohmann_json)
if(WIN32)
  target_compile_definitions(ttwds_core PUBLIC WIN32_LEAN_AND_MEAN NOMINMAX _WIN32_WINNT=0x0601)
  target_link_libraries(ttwds_core PUBLIC ws2_32)
endif()
ttwds_warnings(ttwds_core)

set(FIXTURES_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../protocol/fixtures" CACHE PATH "protocol fixtures dir")
if(NOT EXISTS "${FIXTURES_DIR}/hello.json")
  message(FATAL_ERROR "protocol fixtures not found at FIXTURES_DIR=${FIXTURES_DIR}")
endif()
enable_testing()
add_executable(ttwds_tests tests/main.cpp tests/test_net.cpp tests/test_proto.cpp)
target_compile_definitions(ttwds_tests PRIVATE FIXTURES_DIR="${FIXTURES_DIR}")
target_link_libraries(ttwds_tests PRIVATE ttwds_core doctest::doctest)
ttwds_warnings(ttwds_tests)
add_test(NAME ttwds_tests COMMAND ttwds_tests)

if(WIN32)
  add_executable(ttwds_server_tests tests/test_server_win.cpp)
  target_compile_definitions(ttwds_server_tests PRIVATE FIXTURES_DIR="${FIXTURES_DIR}")
  target_link_libraries(ttwds_server_tests PRIVATE ttwds_core doctest::doctest)
  ttwds_warnings(ttwds_server_tests)
  add_test(NAME ttwds_server_tests COMMAND ttwds_server_tests)
  set_tests_properties(ttwds_server_tests PROPERTIES TIMEOUT 120)
endif()
