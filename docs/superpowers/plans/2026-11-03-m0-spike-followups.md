# M0 spike: rulings and deferred follow-ups

Kept from the build ledger so milestone 1 planning can pick them up.

## Rulings
- Ruling A: WsServer owns the Subscription and wraps classify in a lambda capturing it; classify keeps the extra `Subscription&` param — keeps T3 portable/testable — cost if wrong: small signature edit.
- Ruling B: Controller creates the GitHub repo and pushes before Task 4 (T8 steps 1–2 moved earlier) — user approved a public GitHub repo — cost if wrong: repo exists earlier than planned.
- Ruling C: Task 2 implementer builds `tools/docker/plugin-linux.Dockerfile` as `ttwds-plugin-linux` before running tests — cost: none.
- Ruling D: classify signature is (text, const ServerInfo&, Subscription&) with ServerInfo{pluginVersion,game,xnvse,ttw} — needed to carry game/xnvse — cost: none. Empty reply = send nothing (server must honor).
- Ruling E: run Tasks 6 and 7 before 4/5 while GitHub auth is pending — they don't depend on 4/5; Task 7 Step 7 commits app.yml without pushing — cost: none.
- Ruling F: Task 7 Docker mounts whole repo (fixtures outside app/), buildToolsVersion 35.0.0 — needed for build — cost: none.
- Ruling G: Query refuses the German no-gore exe 0x040020D1 for the spike — strict per constraint "FNV 1.4.0.525" — cost if wrong: German-exe users can't run the spike (revisit in M1).
- Ruling H: equip refused while Pip-Boy open (menu mode) and 'menu state unreadable' fail-closed — fine for spike; checklist tests equip in gameplay — cost: none for M0.

## Deferred minors
- Task 1: minor (deferred): PROTOCOL.md doesn't state hello-before-subscribe order or out-of-range rateHz handling (README link verified exists)
- Task 2: minor (deferred): duplicate Host headers use last value; MSVC /W4 not yet run (Task 4 CI will)
- Task 3: minor (deferred): no wrong-type field tests (rateHz:"x" etc.); catch(...) reports "invalid json" for non-parse errors; missing <utility>; FIXTURES_DIR uses CMAKE_SOURCE_DIR
- Task 6: minor (deferred): stall_after counts from server start, not per connection
- Task 7: minor (deferred): SpikeClient.stop() leaks in-flight handshake socket; OkHttpClient never shut down; FocusMode shows on when token null; anchor holds key focus (add to checklist); cooldown wall-clock backwards; button Row may clip at 1240px (use FlowRow); Dockerfile cmdline-tools no SHA256 + CI no gradle wrapper-validation
- Task 4: minor (deferred): single frame ≥~1.95MB closes healthy client (cap frame size in sendToAll); queue frame-count not byte-limited; SetHandleInformation unchecked
- Task 5: minor (deferred): one kMenu fault blocks equip for session (note in checklist); equip refused with console/Pip-Boy open (note in checklist); snapshot lacks blocked state
