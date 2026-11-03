#pragma once
// Tiny thread-safe file logger (portable). The network thread and the game thread both log.
#include <cstdarg>
#include <cstdio>
#include <mutex>
#include <string>

namespace ttwds::game {

enum class LogLevel { Off = 0, Info = 1, Debug = 2 };
const char* log_level_name(LogLevel l);

class Logger {
public:
    Logger() = default;
    ~Logger();
    Logger(const Logger&) = delete;
    Logger& operator=(const Logger&) = delete;

    // Truncates the file. With LogLevel::Off no file is created.
    bool open(const std::string& path, LogLevel level);
    void setLevel(LogLevel level);
    LogLevel level() const;

#if defined(__GNUC__)
#define TTWDS_PRINTF(a, b) __attribute__((format(printf, a, b)))
#else
#define TTWDS_PRINTF(a, b)
#endif
    void info(const char* fmt, ...) TTWDS_PRINTF(2, 3);
    void debug(const char* fmt, ...) TTWDS_PRINTF(2, 3);
#undef TTWDS_PRINTF

private:
    void write(LogLevel l, const char* fmt, std::va_list ap);
    mutable std::mutex mx_;
    std::FILE* f_ = nullptr;
    std::string path_;
    LogLevel level_ = LogLevel::Off;
};

}  // namespace ttwds::game
