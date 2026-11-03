# TTW DualScreen: design

Date: 2026-11-03 · Status: awaiting user approval
Research notes (full detail, sources): `research/` beside this file (copied from the brainstorm scratchpad).

---

## 1. What this is and why

A second-screen companion for **Tale of Two Wastelands (TTW 3.4)** on the **AYN Thor** and similar dual-screen
Android handhelds. TTW plays full-screen on the top display. The bottom display becomes a live, touchable
Pip-Boy that looks and feels like TTW's own.

The user wants TTW to have what Skyrim (SkyrimWebMonitor), Fallout 4 (Bethesda's Pip-Boy app plus Strip-Boy) and
Morrowind (OpenMW-DS) already have on the Thor. The inspirations they chose: the **Fallout 4 Pip-Boy app**
(STAT/INV/DATA/MAP/RADIO, tap to act) and the **Skyrim companion's architecture** (a script-extender plugin
serves game state to a bottom-screen app).

### The user's requirements
- **"Capture Tale of Two Wastelands vibes perfectly"**: match TTW's look and feel.
- **Color follows the game.** TTW does not switch the Pip-Boy color between worlds (plain TTW keeps New Vegas
  amber everywhere), so we don't either. If the player changes the color or runs a tint-switcher mod, the bottom
  screen follows.
- Layout direction **C, "HUD first, Pip-Boy one tap away"**, chosen from three mockups. Approved mockup:
  `mockups/two-worlds-v2.html` (amber in both worlds; content differs per world).
- Works on the AYN Thor **and similar Android devices**.
- **Works with "dualscreen launcher"**: any launcher (§8, §12.2).
- Ends with a **walkthrough guide**: TTW on the AYN Thor, then this companion.

### Prior art, and why we still build this
| Project | What it is | Why it isn't the answer |
|---|---|---|
| Abacus1829/ayn-dual-screen (`falloutnv/`, v0.1.0, Sep 2026) | xNVSE plugin + generic multi-game Thor app; live stats, inventory, quests, perks, equip/use/drop, icons from BSAs, Mojave map, fast travel; "works under TTW unchanged" | **Source-available licence: no publishing builds or derivatives.** Not TTW-specific: Mojave-only map, system fonts, CSS/synth sounds, radio "still not right", no world-aware content. A third party (issue #2, "Pillars of Wastelands") is asking permission to release a TTW derivative. |
| NicLagr/NewVegasWebMonitor (MIT, Jun 2026, 3★) | FNV fork of SkyrimWebMonitor: Vue/Capacitor app + `FNVWebSocket.dll` (hand-rolled Winsock WS, CMake + MSVC on GitHub Actions) | Mojave-only, FNV-only, stale since June, known bugs (binds all interfaces, wrong XP curve, cached raw pointers across loads, substring "JSON parser"). **Useful MIT reference code** for the plugin. |
| robojan/RealPipboyNV + App (2015) | NVSE plugin + Android app | Old; its known-issues list is a gap checklist (no local map, no reputation, fake fast travel). |
| DualScreen Launcher (ProfMags) | Closed, Patreon app with its own Fallout 3 second-screen DLL | No FNV/TTW, no third-party API. |

**Our angle:** TTW-first fidelity (the game's own bitmap fonts, art, both worlds' maps and every DLC world,
world-aware content, TTW's radio rules), Thor-grade device behavior (focus, burn-in, survival, launchers), and
open source with a public protocol.

**Clean-room rule:** no code, assets or text copied from source-available or closed projects (Abacus,
DualScreen Launcher). Facts are fine (file paths, formats, engine addresses, documented behavior). MIT/GPL code
may be reused under its licence, with attribution in `NOTICE`.

---

## 2. Goals, non-goals, anti-goals

**Goals**
1. A live HUD dashboard the player glances at instead of opening the Pip-Boy.
2. Full Pip-Boy pages one tap away, with touch actions (equip, use, set the tracked quest, tune the radio,
   map actions).
3. Visual fidelity to TTW: live in-game colors and the game's own fonts, icons, Vault Boy and maps, all from the
   player's install.
4. World-aware content for the Capital Wasteland and the Mojave, including the DLC worlds.
5. Zero-touch connection that survives TTW's long loads, and never hurts the game.
6. Works with the common Thor launchers. An in-app plugin installer and health check.
7. An accurate setup guide.

**Non-goals (v1)**
- Moving TTW's in-game menus (barter, dialogue, containers) to the bottom screen.
- Streaming rendered game video (a GPU-captured minimap). A navmesh-drawn local map is v1.x (§6.4).
- Standalone Fallout 3 (FOSE). Plain FNV without TTW should still work as a bonus, with Mojave-only content.
- Cloud, accounts, telemetry. No network beyond loopback unless the player turns on LAN mode (v1.x).

**Anti-goals (failure even if it "works")**
- **Shipping any Bethesda or TTW asset**, or the Monofonto font (its free licence forbids embedding).
- **Hurting the game**: stealing controller focus, pausing GameNative's Wine container, frame drops, crashes,
  save corruption. The game always wins.
- Looking like a generic gamer HUD rather than TTW's Pip-Boy.
- Burning in the bottom OLED.
- Fighting other Thor tools (LED apps, launchers, AYN's assistant).

---

## 3. Constraints and facts the design rests on

- **Engine:** FNV 1.4.0.525, 32-bit x86, DirectX 9, with xNVSE 6.4.x. TTW 3.4.0 (June 2026) rebuilds the
  vanilla-named BSAs with different contents and adds `TaleOfTwoWastelands - {Main,Textures}.bsa`. **Never
  hardcode which archive holds a file.**
- **Plugin dependencies:** must not require JIP LN or JohnnyGuitar (common in TTW setups but not guaranteed).
  Read memory directly using documented offsets.
- **Device runtime:** the user's reference setup is the video "Fallout Tale of Two Wastelands - Ayn Thor -
  GameNative - Vortex (2.0)":
  - **GameNative** with **FEXCore 2512** for 32-bit, DXVK 1.11, Turnip, and `nvse_loader.exe` as the executable;
  - TTW built on a PC with Vortex, and the whole game folder copied to the Thor (it shows up as `A:\`);
  - Wine user `xuser`.

  GameHub (Lite) and Winlator Cmod are secondary targets (`nvse_loader.exe` reportedly fails on Winlator with
  error 998, so the guide covers per-frontend loader workarounds).
- **Loopback works:** Wine sockets are host sockets, and Android apps share `127.0.0.1`. Wine lacks the Windows
  HTTP server stack, so use **raw Winsock**. Behavior is the same under FEX and Box64 (Winsock is Wine's own
  code).
- **Thor hardware:** top display id 0 (1920×1080). Bottom 3.92" AMOLED 1240×1080 60 Hz touch, usually display
  id 4 but **ids aren't stable**. SD 8 Gen 2, shared with an emulated game.
- **Focus physics (Android):** one focused display gets controller input. Touch routes per display. Full CPU
  priority goes to the focused app. Any activity placed on the game's display pauses GameNative's container.
- **xNVSE main loop** (`kMessage_MainGameLoop`) "is only run when new vegas is in the foreground". Losing Wine
  window focus may stall updates. Candidate mitigations, **to be verified in the milestone-0 spike** (it has not
  run yet): AYN Focus Lock (top), a non-focusable companion window, and `bAlwaysActive=1` in `Fallout.ini [General]`.
- **Android:** Thor runs Android 13. App **minSdk 30**, targetSdk **≤36** (Android 17's local-network permission
  may cover loopback; avoid until verified).
- **Build machine:** Arch on WSL2 with Docker. No JDK, Android SDK, MSVC or clang installed. MinGW is
  ABI-incompatible with xNVSE headers. clang-cl+xwin compiles 43/49 example files, but the resulting DLL is
  unproven in-game.

---

## 4. System overview

```
┌──────────────────────────────── AYN Thor ────────────────────────────────┐
│ TOP display (id 0)                       BOTTOM display (id 4*)           │
│ GameNative / GameHub / Winlator          TTW DualScreen app (Kotlin)      │
│  └ Wine + FEXCore/Box64                    • HUD dashboard + Pip-Boy pages│
│     └ FalloutNV.exe + xNVSE                • asset cache (disk)           │
│        └ ttw_dualscreen.dll  ◄── WS ──►    • connection service (FGS)     │
│           127.0.0.1:<port>                 • launcher / installer / LEDs  │
└───────────────────────────────────────────────────────────────────────────┘
```

Deliverables:
1. **`ttw_dualscreen.dll`**: xNVSE plugin (C++20, Win32, MSVC). A loopback WebSocket server, a game-state
   snapshotter and command executor on the main thread, and an asset service.
2. **TTW DualScreen APK**: Kotlin + Jetpack Compose.
3. **Mock game server** (dev tool, Python): speaks the protocol with canned TTW data for both worlds, so the app
   is built and screenshot-tested without the game or a device.
4. **Guide**: `docs/guide/ttw-on-ayn-thor.md`.

Repo layout (builder may adjust): `plugin/`, `app/`, `protocol/` (schema + docs + fixtures shared by all
three), `tools/mockserver/`, `docs/`.

---

## 5. Bottom-screen UI

### 5.1 Home: HUD dashboard (direction C)
Full-bleed 1240×1080, no decorative bezel. Top to bottom:
1. **Header:** `CAPITAL WASTELAND · MEGATON` / `MOJAVE WASTELAND · GOODSPRINGS`, plus the game date in TTW's own
   format **`MM.DD.YY, H:MM`**.
2. **Compass strip:** slim, styled like the HUD compass (letters 128 apart per 360°; quest target and marker
   ticks; hostile ticks red). The slot can later switch to the v1.x minimap.
3. **Vault Boy + six limb bars**, using the game's status-figure textures (`textures\interface\stats\` with
   `_broken` variants). Compacted or hidden when idle (burn-in, §5.5).
4. **Meters:** HP, AP, RAD (with RAD RESIST). **H20 / FOD / SLP** (sic, the game writes "H20") whenever
   Hardcore is on, with the stage and its penalty.
5. **Active effects strip:** chems with remaining time, addictions, Well Rested, magazine buffs. Values shown
   are **effective (buffed)**.
6. **Tracked quest + current objective.**
7. **Equipped weapon:** CND, clip/reserve (`13/60`) and ammo-type tag.
8. **World row** (follows the classified world, §6.3):
   - Capital: karma title (read live), bobbleheads found / 20, current radio station.
   - Mojave: the reputation most recently changed, snow globes / 11, current radio station.
9. **Quick-use row:** four slots with live counts that work mid-combat. **App-side only in v1:** on first run
   they are seeded from the game's hotkeys 1–4 if those are bound (otherwise Stimpak, RadAway, Rad-X, Med-X), and
   the player reassigns them in the app. Nothing is written to the game's hotkeys or the save. Tapping a slot
   sends the normal `use` action.
10. **Tab bar:** STATS · ITEMS · DATA · MAP · RADIO.

### 5.2 Pip-Boy pages: TTW's page tree and wording
TTW uses FNV's Pip-Boy in both worlds:
- **STATS** (cards `LVL` · `HP` · `AP` · `XP`)
  - **Status:** CND (figure, "Crippled"), RAD (meter 0–1000), EFF (per-source lines, "No Effects"), and
    H2O/FOD/SLP in Hardcore. Stimpak, Doctor's Bag, RadAway and Rad-X buttons.
  - **S.P.E.C.I.A.L.**
  - **Skills:** FNV's 13.
  - **Perks:** including TTW bobblehead mini-perks.
  - **General:** two modes, like the game's R toggle. Reputation (discovered factions, fame/infamy titles; in
    the Capital Wasteland "none" unless a mod adds REPU records) and General (misc stats, karma block with title
    and picture, TTW's FO3-only stats from `TTWPCMiscStat*` globals).
- **ITEMS** (cards `Wg` · `HP` · `DT` · `Caps`)
  - Weapons, Apparel, Aid, Misc, Ammo.
  - Rows show the icon, count, equipped marker, DAM/DT, WG, VAL and CND. Sort and filter. Compare with equipped.
  - **Tap** to equip/use. **Long-press** for details, quick-slot assignment and drop (with a confirm step).
  - Repair/weapon mods are read-only in v1.
- **DATA** (cards: location · date)
  - **Quests:** active and completed, objectives, tap to make active, filter by world (Capital / Mojave / DLC).
  - **Misc:** notes (text), **Challenges** with progress, collectibles (bobbleheads, snow globes, skill books,
    magazines).
  - Local Map and World Map are under the MAP tab.
- **MAP**
  - The world map of the **current worldspace**, for every TTW worldspace including DLC and sub-worlds (Freeside,
    the Strip). Interiors fall back to the parent worldspace.
  - Drawn on the player's own map texture at the game's alpha-128 tint.
  - Shows the player arrow, discovered and undiscovered markers (hollow vs filled, the game's 15 icons), the quest
    target and companions.
  - Actions: **set/clear the custom marker**, and **fast travel** using the engine's real routine. If that
    isn't safe in v1, fall back to a teleport that also advances time and needs, gated on discovered and
    travelable, not in combat, and not over-encumbered.
- **RADIO**
  - Stations available in the current world, following TTW's rules: DC stations disabled in the Mojave and the
    reverse.
  - Current station, tune, off.

### 5.3 Look-and-feel rules
- **Colors:** read live from the game (HUD main id 1 for the dashboard, Pip-Boy id 4 for pages, HUD alt id 2 for
  warnings, normally 255,67,42). Pushed on change, so tint-switcher mods just work. Default amber 255,182,66.
- **Fonts:** the game's own bitmap fonts, read from `Fallout.ini [Fonts] sFontFile_1..9` (UI mods remap them),
  then `.fnt` + `*_0_lod_a.dds` through the asset service. Slot 2 for most Pip-Boy text, 4 for page titles, 7 for
  HUD labels and numbers. Monofonto is never bundled. Before the first connection, show an OFL fallback (the
  builder picks between Share Tech Mono and VT323 visually).
- **Geometry:** the Pip-Boy menu canvas is 1024×768. Scale to the bottom panel with layout adjustments, not
  stretching. Lines at brightness 200, text 255, disabled 128. Selected tab boxed with a fill at alpha 32. Line
  thickness scales.
- **Screen treatment:** scanlines from the game's `pipboyscanlines.dds`, soft glow, optional subtle flicker
  (INI `[Pipboy]`/`[InterfaceFX]` values as the guide). Must stay readable on a 3.92" panel.
- **Sounds (optional, off by default):** Pip-Boy UI sounds from the player's archives (`sound\fx\ui\pipboy\…`)
  on bottom-screen taps, at low volume.
- **Wording:** the game's labels (S.P.E.C.I.A.L., CND, DT, WG, VAL, "H20").
- **Readability:** a text-size setting (85–125%). Keep touch targets away from the panel edges (Android back
  gesture).

### 5.4 Connection states (zero-touch)
Explicit full-screen states, each with one plain sentence and, where useful, a single action:
- Looking for TTW…
- Game at main menu
- Loading…
- Paused / in a menu
- Connected
- Plugin version mismatch (what to update)
- Game paused by the launcher (battery-optimisation hint)

Reconnect forever with backoff capped at a few seconds, plus a heartbeat. Offline "last known state" is v1.x.

### 5.5 OLED care
- The Thor's own pixel refresh doesn't run while anything animates, so we provide our own protection.
- **Idle blank:** go fully black, or show a dim drifting clock, when the game is paused, in a menu, loading or
  disconnected, or after N minutes without changes or touches.
- Pixel-shift the whole layout a few px every few minutes.
- Fade or swap static chrome (Vault Boy, bar outlines) to a compact form when idle.
- A brightness ceiling. Optionally follow in-game light (OpenMW-DS-style).

### 5.6 Optional extras (v1, off by default)
- **Stick LEDs** mirror the Pip-Boy color at low brightness:
  - Use Bifrost's API (`com.moonbench.bifrost.api.ACTION_DISPLAY`) if it's installed. Otherwise write
    `/sys/class/sn3112{l,r}/led/brightness` (`1-R:G:B:A`) after checking `canWrite()`.
  - Write from a background thread, dedupe, and re-apply after screen wake.
  - Event pulses (low HP, rads) are v1.x.

---

## 6. Game plugin (`ttw_dualscreen.dll`)

### 6.1 Skeleton and lifecycle
- xNVSE `NVSEPlugin_Query` / `NVSEPlugin_Load`. Refuse to load unless FNV 1.4.0.525 and a supported xNVSE.
  Log why to `Data\NVSE\Plugins\ttw_dualscreen.log`. The log lives beside the DLL so it travels with the copied
  game folder.
- Messaging: `DeferredInit`, `PostLoadGame`, `PreLoadGame`, `ExitToMainMenu`, `NewGame`, `MainGameLoop`.
- **Never keep raw form or ref pointers across frames or loads.** Re-resolve by FormID each time. Drop caches on
  PreLoadGame, ExitToMainMenu and NewGame.
- **No code patches or hooks into the game in v1.** Only reads, plus actions through engine functions or the
  console interface.

### 6.2 Threading
- A **network thread** does socket I/O, WS framing and JSON parsing only.
- The **game main thread** (our `MainGameLoop` handler) does everything else:
  - drains the command queue;
  - runs actions;
  - takes due snapshots and serializes them;
  - hands strings back.
- Use real mutexes or atomics only: no lock-free tricks, because of Box64's weaker memory ordering.
- **Budget:** main-thread work under ~0.5 ms per frame on the Thor. Fast fields at ≤10 Hz; slow fields
  (inventory, quests, perks, map) only when an xNVSE event marks them dirty (`onadd`, `ondrop`, `onactorequip`,
  quest changes, `oncellattach`, `postloadgame`…).
- Send deltas, not full resends, for big inventories.

### 6.3 Data contract
The table below lists fields with access difficulty from the research. Full citations are in
`research/nvse-plugin.md` §7.

| Group | Fields | Difficulty |
|---|---|---|
| Player | HP/AP/rads (+resist), limbs ×6, SPECIAL, 13 skills, level, XP and next-level XP (FNV curve), karma value and title (live string), carry weight, DT/DR, caps, position/heading | easy (XP-next medium) |
| Hardcore | flag, H2O/FOD/SLP values and stages | easy |
| Effects | active effects with source, magnitude and remaining duration; effective values | easy–medium |
| Inventory | per category: form, name, count (including base container), equipped, CND, WG, VAL, DAM/DT, icon path, ammo/clip, weapon mods | medium |
| Perks | perks + ranks | easy |
| Quests | all quests, flags, objectives, active quest; set active | easy / medium |
| World | worldspace EditorID + name, cell name, interior flag, parent worldspace, world-map texture + bounds, map markers (type, discovered, travelable, position), custom marker, quest-target position, companions | medium (compass location markers hard) |
| Time | game day/month/year/hour | easy |
| Radio | current station; station list per TTW's rules | medium–hard (list heuristic) |
| Reputation | fame/infamy per discovered REPU form | easy |
| TTW | globals by EditorID: `bInDCWasteland`, `bInNVWasteland`, `TTWPCMiscStat*` (names to verify on 3.4), `TTWWorldLocation` if present | easy (names unverified) |
| UI | system colors 1–5 (the memory JohnnyGuitar's GetSystemColor reads), `Fallout.ini` fonts section | easy |
| Collectibles | bobbleheads found; snow globes (from challenges or misc stats); skill books/magazines read | medium |

**World classification:**
- Use the current worldspace EditorID, mapped through a table of all TTW worldspaces → {Capital, Mojave} + DLC
  label. For example, The Pitt and Point Lookout → Capital; Sierra Madre and Big MT → Mojave.
- Interiors use the last exterior world.
- `bInDCWasteland`/`bInNVWasteland` serve as a cross-check and a fallback.
- Ship the table as data, verified on 3.4 during the spike.

### 6.4 Assets
- An **asset service**: `asset.get {path, as: "png"|"raw"}` → bytes.
- Index every archive the game loads, in the engine's order: `SArchiveList`, then each plugin's
  `<plugin> - *.bsa` in load order, with loose files on top (last wins).
- Use the engine's own file system if that's safe on the main thread; otherwise our own BSA v104 reader + zlib.
- DDS (DXT1/3/5, uncompressed) → PNG in-plugin.
- The app caches assets on disk by path + archive identity + size.
- **v1.x local map:** the cell's navmesh geometry (SkyrimWebSocket's `local_map_get` approach), drawn by the
  app. A GPU capture is out of scope.

### 6.5 Actions
All run on the main thread, through engine functions or the console interface, never by writing to the
player's structures directly. Every action is validated: the form exists, the player owns it, the game is in a
safe state (not in dialogue, a menu or a loading screen; combat-aware where the game cares). The result goes
back with a reason on failure.

v1 actions:
- equip, unequip, use;
- drop (with confirm);
- quick-slot use;
- set active quest;
- radio tune and off;
- Pip-Boy light toggle;
- custom marker set and clear;
- fast travel.

Unsafe or uncertain actions are disabled by a feature flag rather than shipped half-working.

### 6.6 Server and protocol
- Raw Winsock with blocking sockets + `select()`. The hand-rolled RFC 6455 server (SHA-1 handshake) is our own
  code, informed by NVWM (MIT).
- **Bind `127.0.0.1` only** by default. LAN mode (v1.x) needs an INI switch plus a pairing token.
- Default port: pick one unused by others. Avoid 7786–7788, 8765, 27000, 28000. Configurable in
  `ttw_dualscreen.ini`.
- **Protocol v1** (JSON text frames), documented in `protocol/`, with fixtures shared by the mock server and the
  tests:
  - `hello` → server `welcome {plugin, xnvse, game, ttw, protocol, features[]}`;
  - `subscribe {topics, rate}` / `unsubscribe`;
  - server `state {topic, full|delta}`;
  - `query`;
  - `command {id, name, args}` → `result {id, ok, error?}`;
  - server `event` (save loaded, world changed, level up, color changed);
  - `ping`/`pong`.
- **Feature flags** let the app hide what this plugin build can't do.
- **Crash safety:** defensive readers with null and range checks; any reader failure blanks that field rather
  than guessing.
- Optional minidump only when logging is turned up.

---

## 7. Android app

- **Stack:**
  - Kotlin, Jetpack Compose, coroutines.
  - OkHttp WebSocket client and kotlinx.serialization.
  - Custom Compose drawing for the bitmap fonts and CRT effects.
  - minSdk 30, targetSdk ≤36.
- **Display handling:**
  - Bottom display = the smallest display that is switched on and not private. Device profiles: Thor default;
    Retroid dual-screen add-on reversed; AYANEO Pocket DS's main display is the bottom panel.
  - An **"Identify screens"** override.
  - Self-relocate once, with a 10 s cooldown.
  - **Never put any activity on the game display while TTW runs**, including permission dialogs: open those on
    the bottom display.
- **Focus:**
  - Once the window has focus, add a 1×1 focus anchor, then set `FLAG_NOT_FOCUSABLE` (the OpenMW-DS technique,
    which avoids an ANR).
  - Clear the flag only while text input is needed.
  - Optional accessibility service: a one-time focus hand-back tap on the game display (WalkThor technique),
    auto-open when a Windows wrapper comes to the front, and revive after a kill. Opt-in, with a Play-style
    disclosure.
  - The guide recommends AYN **Focus Lock → top** as the zero-code option.
- **Resilience:**
  - A foreground service (`specialUse`, `stopWithTask=false`, excluded from Recents) owns the socket.
  - Revive after a low-memory kill (WalkThor approach).
  - Release caches on trim.
  - Light footprint: the UI recomposes only on changed fields.
- **Network config:** cleartext only to `127.0.0.1`/`localhost`.
- **In-app plugin installer and health check:**
  - Through a folder picker (SAF), the user points at the game folder (GameNative's mapped folder).
  - The app copies `ttw_dualscreen.dll` and the `.ini` into `Data\NVSE\Plugins\`, verifies by SHA-256, and checks
    for `nvse_loader.exe` / the xNVSE DLL.
  - A checklist then shows plugin seen / not seen, xNVSE version, TTW detected, Focus Lock hint,
    battery-optimisation exemption for the launcher, and the container suspend policy hint (`never`).
- **Report a problem:** zips the app log, the last plugin log (fetched over the socket), and device and display
  info.
- **Distribution:** GitHub Releases (Obtainium-compatible), plus an in-app update check (off by default).

---

## 8. Launcher integration ("works with dualscreen launcher")

- **Every launcher** (AYN Screen Launch, Mjolnir, Járngreipr, Loki, Cocoon): a normal launcher activity that is
  happy being started on either display at any time.
- **Cocoon:** works as Cocoon's per-game **companion app**. Cocoon passes no game info, and none is needed.
  Optionally watch Cocoon's `launch_debug.log` (user-granted folder) to show "TTW is starting…" early.
- **ES-DE:** an optional game-start script and log watch for the same early signal.
- **Built-in "Play TTW":** launches the game on the top display, **last**, so the game gets focus.
  - GameNative: `app.gamenative.LAUNCH_GAME` (`app_id`, `game_source`, optional `exe`).
  - GameHub Lite / BannerHub: `*.LAUNCH_GAME` (`localGameId`, `autoStartGame`).
  - Winlator Cmod: `XServerDisplayActivity` (`shortcut_path`).
  - Official GameHub has no launch intent; show instructions instead.
- **DualScreen Launcher (ProfMags):** out of scope (user answer §12.2). It has no launch or companion API and no
  FNV/TTW entry.

---

## 9. Build, CI and testing

- **Plugin build:** CMake + MSVC (v143+), `-A Win32`, static CRT, xNVSE headers fetched at a pinned tag. It builds on
  GitHub Actions `windows-latest` (§12.4). VS Build Tools on the Windows host is a local fallback, and
  clang-cl + xwin in Docker a last resort.
- **App build:** Gradle in a pinned Docker image (JDK 17 + Android SDK), producing a signed release APK. The
  signing key is generated locally and kept out of git.
- **Tests:**
  - **Protocol:** fixtures validated by plugin unit tests (host-side, framing + JSON), mock-server tests and app
    tests.
  - **Plugin:** unit tests for the WS framing, JSON, BSA reader and DDS decoder. In-game smoke tests on the
    device.
  - **App:** unit tests (reducers, display selection, focus state machine). **Screenshot tests** (Roborazzi or
    Paparazzi) of every screen, in both worlds, in default amber and one custom tint.
  - **Device acceptance:**
    - plugin loads under GameNative + FEXCore;
    - connects with no taps;
    - tapping the bottom screen does not freeze the main loop or steal the controller;
    - survives save/load, world travel (Union Station), quit-to-menu and a 1-hour session;
    - no frame-rate drop above ~2 fps.

---

## 10. Milestones

0. **Go/no-go spike (device required):**
   - a 100-line plugin (log on load, loopback WS echo, a main-loop counter) plus a minimal app;
   - check the counter freezes or not when tapping the bottom screen, with and without Focus Lock and
     `bAlwaysActive=1`;
   - read HP, Hardcore, `bInDCWasteland` and colors 1–5, and run one equip;
   - dump worldspace EditorIDs in a few DLC worlds.

   Nothing else is built until this passes.
1. **Live dashboard (read-only):** connection states, display and focus handling, OLED care, both worlds.
2. **Pip-Boy pages (read-only) and assets:** icons, Vault Boy, game fonts, scanlines.
3. **Actions:** equip/use/drop, quick slots, set quest, radio, light.
4. **Map:** all worldspaces, markers, quest target, companions, custom marker, fast travel.
5. **Integration:** Play TTW, Cocoon/ES-DE signals, accessibility opt-in, installer and health check, LEDs,
   report-a-problem, releases.
6. **Guide:** TTW on the AYN Thor, plus installing TTW DualScreen.

v1.x backlog:
- navmesh local map in the compass slot;
- companion panel and commands;
- LAN/streaming mode with pairing;
- offline last-known state;
- top-HUD declutter;
- notes reader and holotape audio;
- map notes;
- LED event pulses;
- controller navigation of the bottom screen.

---

## 11. Guide outline (`docs/guide/ttw-on-ayn-thor.md`)

**Part 1, TTW on the Thor** (backbone: the user's GameNative video, cross-checked by research):
1. PC prep.
2. Build TTW with Vortex, then root mods and INI.
3. Package the game folder (redistributables, registry files per store, `config` + `appdata`).
4. Copy to the Thor.
5. GameNative container settings.
6. In-container setup and registry fix.
7. `nvse_loader.exe`.
8. Updating mods later.

It also includes Thor tips (Focus Lock, Cluster Tune, battery exemption, suspend policy `never`,
`bAlwaysActive=1`) and troubleshooting. Each step is marked verified, community-reported or unverified.

**Part 2, TTW DualScreen:**
1. Download.
2. Drop in the plugin (manual or Vortex).
3. Install the APK.
4. Run the first-run checklist.
5. Set up the launcher (Cocoon / Play TTW / others).
6. Troubleshooting.

---

## 12. Answers from the user (2026-11-03)
1. **Direction:** build our own TTW-first project (not a fork of NVWM, not a contribution to Abacus).
2. **"Dualscreen launcher":** *any launcher*. §8 applies as written. ProfMags' DualScreen Launcher is not a
   target; coexisting with it is not promised.
3. **Testing:** TTW already runs on the user's Thor. There's no wireless ADB and no TTW on a PC. So:
   - device steps are written as short **checklists the user runs**;
   - builds reach the device as GitHub Release (pre-release) downloads;
   - every device result the plan depends on (the spike especially) is reported back by the user.

   Wireless ADB can be added later to speed things up.
4. **Hosting:** a **public GitHub repo**. The plugin builds on GitHub Actions `windows-latest`. APKs and the
   plugin zip ship as GitHub Releases (Obtainium-compatible).

## 13. Calls made by Claude (please check)
- Name "TTW DualScreen". Licence **GPL-3.0** for the whole repo, so engine knowledge or code from GPL projects
  (JIP LN, NVForever) can be reused legally. MIT pieces (NVWM, SkyrimWebMonitor) are included with notices.
- Kotlin + Compose for the app (native control of focus, displays, services and memory; device-free screenshot
  tests). The C++ plugin uses raw Winsock.
- Interactive actions are in scope, read as "like the Fallout 4 and Skyrim apps".
- Quick slots: four, app-side, seeded from hotkeys 1–4. No write-back to the game in v1.
- Kept the compass strip from the approved mockup (players asked more for a local map), but made it slim and
  swappable for the v1.x minimap.
- No bezel. Fully black idle state for OLED care.
- Snow globes count / 11 (base + DLC), bobbleheads / 20.
- Drop needs a confirm step. Sounds and LEDs are off by default.
