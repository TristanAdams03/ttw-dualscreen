#pragma once
#include "proto/game_iface.h"

struct FakeGame : ttwds::proto::IGame {
    bool loaded = true;
    int equipCalls = 0, dumpCalls = 0;
    bool saveLoaded() override { return loaded; }
    ttwds::proto::SpikeSnapshot snapshot(int64_t frame, int64_t ms) override {
        ttwds::proto::SpikeSnapshot s;
        s.frame = frame; s.ms = ms; s.saveLoaded = loaded;
        return s;
    }
    ttwds::proto::CommandResult equipFirstWeapon() override {
        ++equipCalls;
        return {true, "", nlohmann::json{{"formId", "0001A6B9"}, {"name", "Lincoln's Repeater"}}};
    }
    ttwds::proto::CommandResult dumpWorld() override {
        ++dumpCalls;
        return {true, "", nlohmann::json{{"worldspace", "Wasteland"}}};
    }
};
