#pragma once
// Hardware-exception guard for engine reads (MSVC SEH). fn must only touch POD data:
// with /EHsc, destructors of C++ objects between the fault and the __except do not run.
#include <cstddef>
#include <cstdint>

namespace ttwds::game {

// Runs fn(ctx). Returns false (and the exception code) if it raised an access violation
// or another hardware fault; any other exception propagates unchanged.
bool seh_call(void (*fn)(void*), void* ctx, unsigned long* code);

// Copies a NUL-terminated engine string into dst (always terminated, truncated at n-1).
// Returns false if src is null or faults. Empty strings return true with dst = "".
bool seh_copy_cstr(const char* src, char* dst, size_t n);

}  // namespace ttwds::game
