#pragma once
#include <string>
#include "proto/game_iface.h"

namespace ttwds::proto {

std::string state_json(const SpikeSnapshot& s);
std::string result_json(const std::string& id, const CommandResult& r);
std::string error_json(const std::string& message);

}  // namespace ttwds::proto
