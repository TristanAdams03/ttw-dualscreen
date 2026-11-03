#include "game/log.h"
#include <chrono>
#include <cstdarg>
#include <ctime>
#if defined(_WIN32)
#include <share.h>
#endif

namespace ttwds::game {

namespace {
// Windows: deny writers but let the user read the log while the game runs (fopen_s would not).
std::FILE* open_log(const std::string& path, const char* mode) {
#if defined(_WIN32)
    return _fsopen(path.c_str(), mode, _SH_DENYWR);
#else
    return std::fopen(path.c_str(), mode);
#endif
}
}  // namespace

const char* log_level_name(LogLevel l) {
    switch (l) {
        case LogLevel::Off: return "off";
        case LogLevel::Info: return "info";
        case LogLevel::Debug: return "debug";
    }
    return "?";
}

Logger::~Logger() {
    std::lock_guard<std::mutex> lk(mx_);
    if (f_) std::fclose(f_);
    f_ = nullptr;
}

bool Logger::open(const std::string& path, LogLevel level) {
    std::lock_guard<std::mutex> lk(mx_);
    if (f_) std::fclose(f_);
    f_ = nullptr;
    path_ = path;
    level_ = level;
    if (level == LogLevel::Off) return true;
    f_ = open_log(path, "w");
    return f_ != nullptr;
}

void Logger::setLevel(LogLevel level) {
    std::lock_guard<std::mutex> lk(mx_);
    level_ = level;
    if (level != LogLevel::Off && !f_ && !path_.empty()) f_ = open_log(path_, "a");
}

LogLevel Logger::level() const {
    std::lock_guard<std::mutex> lk(mx_);
    return level_;
}

void Logger::write(LogLevel l, const char* fmt, std::va_list ap) {
    std::lock_guard<std::mutex> lk(mx_);
    if (!f_ || static_cast<int>(l) > static_cast<int>(level_)) return;
    const auto now = std::chrono::system_clock::now();
    const std::time_t t = std::chrono::system_clock::to_time_t(now);
    const auto ms = std::chrono::duration_cast<std::chrono::milliseconds>(now.time_since_epoch()).count() % 1000;
    std::tm tm{};
#if defined(_WIN32)
    localtime_s(&tm, &t);
#else
    localtime_r(&t, &tm);
#endif
    char ts[32];
    std::strftime(ts, sizeof ts, "%H:%M:%S", &tm);
    std::fprintf(f_, "%s.%03d [%s] ", ts, static_cast<int>(ms), log_level_name(l));
    std::vfprintf(f_, fmt, ap);
    std::fputc('\n', f_);
    std::fflush(f_);  // a crash must not eat the last lines
}

void Logger::info(const char* fmt, ...) {
    std::va_list ap;
    va_start(ap, fmt);
    write(LogLevel::Info, fmt, ap);
    va_end(ap);
}

void Logger::debug(const char* fmt, ...) {
    std::va_list ap;
    va_start(ap, fmt);
    write(LogLevel::Debug, fmt, ap);
    va_end(ap);
}

}  // namespace ttwds::game
