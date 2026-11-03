# TTW DualScreen: xNVSE plugin technical feasibility notes

Research date: 2026-11-03. Research only; no project code written.

Repos read at these commits (all line numbers below refer to these):

| Repo | Commit | License |
|---|---|---|
| xNVSE/NVSE | `0ccd23ad885ddae533c1790a3fc56cd073e38de3` (latest release 6.4.9, 2026-09-16) | **no LICENSE file** (GitHub API `/license` → Not Found) |
| jazzisparis/JIP-LN-NVSE | `5a30ac4356ea0e93b9ff357b5031b1e420240a4d` | GPL-3.0 |
| carxt/JohnnyGuitarNVSE | `c87905e67b35d7303ffd113a5596384d4dce75b8` | LGPL-2.1 |
| andreyvelsk/SkyrimWebSocket | `0e298720322bab75cbee30cc6f99893747397ad3` | MIT |
| NicLagr/NewVegasWebMonitor (prior art, see §0) | `dd86d9415e2340ee1fa9b2a2414efdb9b09c1236` | MIT |
| CyberShonk/fnv-gamenative-4gb-patcher (= Nexus mod 98222) | `124719b1be12429e226610b6b9e8f95e41955091` | MIT |
| TaleOfTwoWastelands/TaleOfTwoWastelands (old public XML export) | `105d6f3264bf1112c132c27322e7176fb656cd70` | n/a |

Abbreviations: **X** = xNVSE `nvse/nvse/…`, **J** = JIP LN, **JG** = JohnnyGuitar `JG/…`, **NVWM** = NewVegasWebMonitor `plugin/src/…`, **SWS** = SkyrimWebSocket.

---

## 0. Most important finding: prior art already exists for exactly this device

**NicLagr/NewVegasWebMonitor** is a Fallout: New Vegas Pip-Boy companion app (Vue + Capacitor APK) plus an xNVSE plugin (`FNVWebSocket.dll`). It is "Built for the AYN Thor (running FNV via GameHub Lite)" (README.md:9) and adapted from andreyvelsk's SkyrimWebMonitor (MIT). Its WebSocket protocol is SkyrimWebSocket's, unchanged (README.md:12-14).

- Plugin: `plugin/src/main.cpp` (781 lines) and a hand-rolled Winsock WebSocket server `ws_server.cpp` (378 lines, SHA-1 handshake in `sha1.h`). Built with **CMake + MSVC on GitHub Actions `windows-latest`** (`.github/workflows/plugin.yml`, `plugin/CMakeLists.txt`). The author builds from macOS and has no Windows box; the original mingw plan (docs/fnv-websocket-plugin.md §8) was abandoned for MSVC CI.
- Implemented already: HP/AP/rads, SPECIAL, limbs, level/XP, karma, carry weight, caps, inventory (5 categories with condition, equipped state, value, weight, icon path, engine-computed weapon DAM), active effects, discovered map markers, quest journal and set-active-quest, radio (current station, station list, tune, off), equip/unequip/use/drop, fast travel (teleport), custom map marker.
- Commit messages (2026-06-17…06-28) repeatedly say values were "verified against in-game ground truth/live data". The author has no Windows machine, so this **implies** the plugin and Winsock loopback work on the Thor under GameHub Lite. This is **inferred, not stated**. The README only says "transport proven; live game-data reads are the next phase" (README.md:32), which is stale relative to the code.
- Bugs and weaknesses worth not copying (details in the sections below):
  - binds `INADDR_ANY` (ws_server.cpp:357), which exposes the socket to the LAN;
  - item count uses `countDelta` only (main.cpp:442);
  - quest "running" uses TESForm `flags & 1` instead of `questFlags` (main.cpp:326);
  - XP-to-next formula `100*(L+1)*L` (ws_server.cpp:191-194) disagrees with FNV's curve;
  - caches raw `TESForm*`/`TESObjectREFR*` across frames in maps (main.cpp:45-46,65), which is dangerous across loads;
  - the JSON "parser" is substring search.
- **Licensing caveat:** the LICENSE file says "Copyright (c) 2026 Andrey Velsk" (copied from upstream). Several engine addresses in it are credited to JIP (GPL-3) and JohnnyGuitar (LGPL-2.1).

Recommendation: treat NVWM as a reference implementation and feasibility evidence. Contact the author: they may already have answers to the Thor-specific questions, and collaboration on a GameNative/TTW variant is plausible. Do not fork blindly.

Other relevant repos:
- `Zyrcxies/NewVegasCoop`: an xNVSE co-op plugin using CMake + MSVC `-A Win32` (CMakeLists.txt:5, CMakePresets.json:8), a static CRT (CMakeLists.txt:12) and ENet over Winsock (CMakeLists.txt:22-25).
- `clayne/nvse_plugin_cmake_template`: a CMake template (not examined in depth).

---

## 1. Plugin skeleton (xNVSE 6.4.9)

### 1.1 Entry points and version checks

The official example is `nvse_plugin_example/main.cpp`. Exports are via `exports.def` (`NVSEPlugin_Query`, `NVSEPlugin_Load`) and `dllmain.c` is a no-op DllMain.

```cpp
// nvse_plugin_example/main.cpp:115-157 (abridged)
bool NVSEPlugin_Query(const NVSEInterface* nvse, PluginInfo* info) {
    info->infoVersion = PluginInfo::kInfoVersion;   // PluginAPI.h:1306-1316
    info->name = "MyFirstPlugin";
    info->version = 2;
    if (nvse->nvseVersion < PACKED_NVSE_VERSION) return false;          // nvse_version.h:6-9 → 6.4.9
    if (!nvse->isEditor) {
        if (nvse->runtimeVersion < RUNTIME_VERSION_1_4_0_525) return false;
        if (nvse->isNogore) return false;
    } else if (nvse->editorVersion < CS_VERSION_1_4_0_518) return false;
    return true;
}
// main.cpp:159-205
bool NVSEPlugin_Load(NVSEInterface* nvse) {
    g_pluginHandle = nvse->GetPluginHandle();
    g_messagingInterface = (NVSEMessagingInterface*)nvse->QueryInterface(kInterface_Messaging);
    g_messagingInterface->RegisterListener(g_pluginHandle, "NVSE", MessageHandler);
    if (!nvse->isEditor) {
        g_script  = (NVSEScriptInterface*)nvse->QueryInterface(kInterface_Script);
        g_consoleInterface = (NVSEConsoleInterface*)nvse->QueryInterface(kInterface_Console);
        g_eventInterface   = (NVSEEventManagerInterface*)nvse->QueryInterface(kInterface_EventManager);
        g_dataInterface    = (NVSEDataInterface*)nvse->QueryInterface(kInterface_Data);
        ...
    }
    nvse->SetOpcodeBase(0x2000);  // only needed if we register script commands; request a real range
    ...
}
```

- `NVSEInterface` (X PluginAPI.h:62-94) has `nvseVersion`, `runtimeVersion`, `editorVersion`, `isEditor`, `RegisterCommand`, `SetOpcodeBase`, `QueryInterface`, `GetPluginHandle`, `GetRuntimeDirectory`, `isNogore`, and more.
- Interface IDs are at PluginAPI.h:38-57: Serialization, Console, Messaging, CommandTable, StringVar, ArrayVar, Script, Data, EventManager, Logging, PlayerControls.
- **We do not need opcodes** unless we expose script commands. Return `false` from Query when `isEditor`, as NVWM main.cpp:766 does.
- Minimum version: require something like xNVSE ≥ 6.4.x for `kMessage_MainGameLoop`. That message was added in messaging kVersion 4 (PluginAPI.h:268-271).

### 1.2 Messaging interface (X PluginAPI.h:200-307)

Messages, in order:
- `kMessage_PostLoad`, `ExitGame`, `ExitToMainMenu`, `LoadGame`, `SaveGame`, `ScriptPrecompile`, `PreLoadGame`, `ExitGame_Console`;
- `PostLoadGame` (data = bool success), `PostPostLoad`, `RuntimeScriptError`;
- v2: `DeleteGame`, `RenameGame`, `RenameNewGame`, `NewGame`;
- v3: the `*Name` variants;
- v4 (xNVSE): `DeferredInit`, `ClearScriptDataCache`, **`MainGameLoop`** (line 271, "called each game loop"), `ScriptCompile`, `EventListDestroyed`, `PostQueryPlugins`, `OnFramePresent` (data = int isLoadingScreen), `ReloadConfig`, `OnRefSet3D`/`OnRefUnset3D`/`OnRefAttach`, `OnCellStateChange`, `OnCellRefsLoaded`, `OnNonPersistentFormLoad`/`Unload`.

### 1.3 Where MainGameLoop runs: the main-thread pump

```cpp
// X Hooks_Gameplay.cpp:36-37
static const UInt32 kMainLoopHookPatchAddr = 0x0086B386; // 7th call BEFORE first call to Sleep in oldWinMain
// Hooks_Gameplay.cpp:1132-1133
// "this address was chosen because it is only run when new vegas is in the foreground"
WriteRelJump(kMainLoopHookPatchAddr, (UInt32)&MainLoopHook);
// Hooks_Gameplay.cpp:888-942  HandleMainLoopHook(): first call records g_mainThreadID (line 908),
// dispatches kMessage_DeferredInit once, then every frame:
PluginManager::Dispatch_Message(0, NVSEMessagingInterface::kMessage_MainGameLoop, nullptr, 0, nullptr); // :942
// …followed by EventManager::Tick(), array/string GC, CallAfter* handling.
```

- **Safe main-thread work = do it inside our `kMessage_MainGameLoop` handler.** That is the same thread that runs scripts and the xNVSE event manager. This is exactly what NVWM does (main.cpp:756-758). SKSE has a TaskInterface; xNVSE has no equivalent, so we drain our own queue there.
- `g_mainThreadID` is exported (X Hooks_Gameplay.h:17), but only inside xNVSE. Plugins should record `GetCurrentThreadId()` themselves on the first MainGameLoop and assert on it.
- **Caveat (risk #1):** the hook point "is only run when new vegas is in the foreground". If FNV loses focus (for example, touching the bottom-screen app), the pump may stop and queued commands stall. See §6.
- Optional push triggers: native xNVSE events via `NVSEEventManagerInterface::SetNativeEventHandler(name, fn)` (PluginAPI.h:1042, 1175). Built-in event names in X EventManager.cpp include `onadd`, `ondrop`, `ondropitem`, `onactorequip`, `onhit`, `ondeath`, `oncellattach`, `loadgame`, `postloadgame`, `exittomainmenu`, `onstartcombat`, `oncombatend`, and more. Use them as "dirty" flags so inventory/quest snapshots are rebuilt only on change, not polled every second.
- Lifecycle: on `kMessage_PreLoadGame`, `ExitToMainMenu` and `NewGame`, drop every cached form/ref pointer. On `PostLoadGame(success)` and `DeferredInit`, resolve globals and caches again.

### 1.4 Build settings of the official example

From nvse_plugin_example.vcxproj:
- Win32 only; `PlatformToolset v145` (lines 38-66; the xNVSE CI itself builds with `/p:PlatformToolset=v143`, .github/workflows/build-legacy.yml);
- `LanguageStandard stdcpplatest` (135);
- `ForcedIncludeFiles nvse/prefix.h` (134);
- defines `RUNTIME;RUNTIME_VERSION=0x040020D0` (128);
- `ModuleDefinitionFile exports.def` (139);
- compiles 20 xNVSE `.cpp` files plus the `common` project (ClCompile list starting at line 305): containers, GameAPI, GameBSExtraData, GameData, GameExtraData, GameForms, GameObjects, GameOSDepend, GameProcess, GameRTTI, GameScript, GameSettings, GameTasks, GameTiles, GameTypes, NiTypes, printf, SafeWrite, Utilities, utility.

NVWM's CMakeLists mirrors this (CMakeLists.txt:17-40) with `/FI"nvse/prefix.h" /bigobj /EHsc`, a static CRT (`MSVC_RUNTIME_LIBRARY MultiThreaded`) and links `ws2_32 winmm`. That file is a ready-made template.

---

## 2. Build toolchain

### 2.1 MinGW (i686-w64-mingw32): not ABI-compatible with xNVSE headers

Concrete breakers found in the headers:
1. **Virtual destructors.** `BSExtraData` declares `virtual ~BSExtraData();` as its first virtual (X GameBSExtraData.h:13); so do `ExtraContainerChanges`, `ExtraHealth`, `ExtraWorn` and others (X GameExtraData.h:292-581). MSVC emits one vtable slot (scalar deleting dtor). The Itanium ABI used by GCC/MinGW emits two (complete and deleting), so every later virtual slot shifts by one. Calling any later virtual through these declarations calls the wrong function. MSVC also orders overloaded virtuals differently from GCC.
2. **Member-function-pointer trick.** `DEFINE_MEMBER_FN_LONG` (X Utilities.h:29-36) stores a 4-byte `UInt32` address and reinterprets `&_address` as a pointer-to-member-function. That relies on MSVC's 4-byte single-inheritance PMF. GCC PMFs are 8 bytes `{ptr, adj}`, so the call reads garbage `adj`.
3. **MSVC inline asm.** About 25 `__declspec(naked)` functions with MSVC `__asm { … }` blocks sit in the plugin source set: utility.cpp 14, containers.cpp 5, GameObjects.cpp 3, GameAPI/GameTiles/NiTypes 1 each. JIP and JG headers are full of them. GCC cannot parse MSVC block asm.
4. Class layout differences (tail-padding reuse, bitfields) and SEH (`__try`) differences.

Conclusion: MinGW could only work against a home-made, offset-only header set using free-function `ThisCall(addr, …)` wrappers and no virtual calls. That means throwing away xNVSE's headers. Not recommended. (The GameNative patcher is built with MinGW, but it is a standalone PE patcher that never includes NVSE headers.)

### 2.2 clang-cl + lld-link + xwin: tested in a throwaway experiment

Setup:
- Arch container with clang 23.1.4 and lld;
- xwin 0.10.0 `--arch x86 splat` (Windows SDK 10.0.26100 + MSVC CRT: 793 MB splat, 1.1 GB cache). xwin requires `--accept-license` for the Microsoft SDK/CRT EULA, which the user must accept themselves for real use;
- target `--target=i686-pc-windows-msvc /MT /std:c++latest /FInvse/prefix.h`;
- source: the unmodified xNVSE example source set at commit 0ccd23ad.

Scripts and logs: `research/build-logs/build_example*.sh`, `clangcl_pass1.log`, `clangcl_pass2.log`.

Results:
- **Pass 1: 39 of 49 TUs compiled.** Failures were:
  - case-sensitive includes (`GameApi.h`, `common/IFilestream.h`: Windows is case-insensitive);
  - MSVC-only token pasting in `DYNAMIC_CAST` (`-Winvalid-token-paste` is an error);
  - the `naked`/asm issues below.
- **Pass 2:** added `-Wno-invalid-token-paste` and two case symlinks. **43 of 49 compiled, including `main.cpp` and all of `common/`.** Six still fail on genuine clang limitations:
  - `'naked' attribute only applies to non-member functions` (clang does not allow naked member functions; MSVC does): containers.cpp `PrimitiveCS::Enter`, GameObjects.cpp:360/403, GameTiles.cpp:16, utility.cpp:5;
  - MS-asm quirks: `ambiguous operand size for instruction 'mov'` (utility.cpp:209/257), labels and C++ arrays referenced from asm (`kPackedValues`, NiTypes.cpp:8-27);
  - ArrayVar.h:409, a default-argument lambda with `[&]` at namespace scope (MSVC leniency).
- Link failed only because of those six objects.
- Pitfall: clang-cl parses absolute Unix paths like `/w/src/...` as switches (`/w`), so pass sources after `--`.

Verdict: **feasible with a small patch set.** About 24 naked member functions need rewriting as free `__fastcall` naked functions or plain C++, plus a few `dword ptr` fixes, and the patches must be re-applied on each xNVSE update. Alternatively, compile only the TUs we need and replace the asm helpers. It is good enough for Linux-only CI, but it would make us the only clang-built NVSE plugin, and ABI subtleties (e.g. MSVC-specific `/vd` or `/vmg` behaviour) would be ours to debug. Unverified: whether a clang-cl-built DLL runs correctly in-game, since there was no game runtime here.

### 2.3 What existing plugins use

- xNVSE: `nvse.sln`, MSBuild v143, `windows-latest` CI (build-legacy.yml).
- Example plugin: `.sln`/`.vcxproj` with v145.
- JIP: `jip_nvse.sln`, v143, stdcpplatest.
- JohnnyGuitar: `johnnyguitar.sln`, v145, with three GitHub workflows (`.github/workflows/releaseBuild.yml`, `continuousRelease.yml`, `testBuilds.yml`).
- NVWM and NewVegasCoop: **CMake `-A Win32` + MSVC**. Nobody examined uses clang-cl or MinGW.

### 2.4 Recommendation

1. **Primary: CMake project, MSVC v143/v145, Win32, static CRT (`/MT`).** Build in **GitHub Actions `windows-latest`** (copy NVWM's `plugin.yml`: `git clone xNVSE` → `cmake -A Win32` → `cmake --build --config Release` → upload artifact).
2. **Local loop: the dev box is WSL2 on a Windows host.** Install "Visual Studio Build Tools" (Desktop C++, x86 tools) on the Windows host and call it from WSL through interop (`cmd.exe /c "vcvars32.bat && cmake --build …"`), working on a `/mnt/c/...` checkout. That gives the real MSVC ABI with no VM.
3. Fallbacks: clang-cl + xwin with a maintained patch set (§2.2), or msvc-wine (real `cl.exe` under Wine on Linux; not tested here).

---

## 3. Game data access, field by field

The player is `PlayerCharacter* p = *g_thePlayer` (X GameObjects.h:655), or `PlayerCharacter::GetSingleton()`. **All reads must happen on the main thread.** Guard on `p && p->parentCell` and on not-loading (`InterfaceManager::IsMenuVisible(kMenuType_Loading=0x3EF)`, X GameUI.h:25,96).

The xNVSE `PlayerCharacter` declaration is sparse (X GameObjects.h:562-660). **JIP's `nvse/GameObjects.h:863-1100` is far more complete** (field names and offsets for 1.4.0.525). Recommended: vendor xNVSE headers, add a small `PlayerCharacterExt` overlay struct or `*(T*)((char*)p + off)` accessors for the JIP-documented offsets, each with a `static_assert`ed offset and a citation comment. That keeps us out of GPL code (offsets are facts) while staying in sync with xNVSE.

### 3.1 Actor values

`ActorValueOwner` vtable (X GameForms.h:1316-1335):
- `Fn_03(code)` = GetActorValue (current);
- `Fn_08(code)` = GetPermanentActorValue (max/base incl. permanent mods);
- `Fn_00/Fn_01` = base;
- `Fn_0A()` = GetLevel.

Codes are `EActorVals` (X GameAPI.h:289-385): SPECIAL 5-11, AP 12, CarryWeight 13, Health 16, DR 18, Karma 23, XP 24, limbs Head 25 … RightLeg 30 (Brain 31), skills 32-45, InventoryWeight 46, RadLevel 54, Dehydration 73, Hunger 74, Sleepdeprevation 75, DamageThreshold 76.

```cpp
// NVWM main.cpp:670-691 (pattern)
ActorValueOwner& av = p->avOwner;
s.level  = av.Fn_0A();
s.health = av.Fn_03(eActorVal_Health); s.healthMax = av.Fn_08(eActorVal_Health);
for (i=0;i<6;i++){ cur=av.Fn_03(eActorVal_Head+i); max=av.Fn_08(eActorVal_Head+i); limb[i]=cur/max*100; }
```

**XP to next level:** JIP documents `PlayerCharacter::pcLevelData` at +0x878 → `PCLevelData{…, UInt32 xpToNextLevel @+4}` (J GameObjects.h:895-901, 1038). Unverified whether this is the absolute threshold or the remainder; compare with the Pip-Boy. Fallback formula from memory, unverified: XP to reach level N = 25·(3N+2)·(N−1) (200, 550, 1050, …), i.e. `iXPBase`-style GMSTs (J internal/settings_enum.h:1115 `kGMST_iXPBase`). NVWM's `100·L·(L+1)` is wrong past level 2.

### 3.2 Hardcore

- `PlayerCharacter::isHardcore` at +0x7BC (X GameObjects.h:621; J GameObjects.h:1024, typed `UInt32`).
- H2O/FOD/SLP are AVs 73/74/75 (`Fn_03`; 0–1000 scale).
- Stage thresholds are JIP's `GetHardcoreStageThreshold` (J functions_jip/jip_fn_miscellaneous.h:53).

### 3.3 Inventory

- `ExtraContainerChanges::GetForRef(p)->GetEntryDataList()` (X GameExtraData.h:319-404). `EntryData{ extendData (tList<ExtraDataList>), countDelta @04, type (TESForm*) }`.
- **True count = base container count + `countDelta`.** JIP does `GetFormCount(&baseForm->GetContainer()->formCountList, entryList, item)` (J jip_fn_inventory.h:261-280). NVWM uses `countDelta` alone (main.cpp:442).
- Per stack (`extendData` items):
  - equipped = has `kExtraData_Worn`/`WornLeft`;
  - condition = `ExtraHealth::health` / base `health.health` of WEAP/ARMO (NVWM main.cpp:454-479);
  - also ExtraCount, ExtraOwnership and weapon-mod flags.
- Category from `form->typeID`: WEAP, ARMO, ALCH (aid), BOOK/NOTE, AMMO, MISC, KEYM and others.
- Weight and value via `DYNAMIC_CAST` to `TESWeightForm`/`TESValueForm` (NVWM main.cpp:96-103).
- Displayed value scales with condition, about `base·ratio^1.5` (NVWM main.cpp:483-486, empirically verified by its author).
- Weapon DAM as the Pip-Boy shows it: engine routine 0x644CE0, the core of JIP's `CalculateWeaponDamage` (NVWM main.cpp:105-139).
- **Icon texture path:** `TESIcon` (`ddsPath` String) at per-type offsets: WEAP/MISC/AMMO/KEYM 0x5C, ALCH 0x6C, NOTE 0x54, ARMO `bipedModel.icon[0]` 0x70+0x8C (NVWM main.cpp:141-159). Cleaner: `DYNAMIC_CAST(form, TESForm, TESIcon)` where the RTTI exists; JIP's GameForms.h declares the icon members per class.
- Equipped weapon: `Actor::GetEquippedWeapon()` (X GameObjects.h:526; J GameObjects.cpp:1542-1546).
- Ammo type and clip: J `Actor::GetAmmoInfo()` returns the HighProcess `+0x118` `ContChangesEntry` (J GameObjects.cpp:1524-1535). `type` = current ammo form; `countDelta` = rounds in the clip (J jip_fn_actor.h:1050-1064, `GetCurrentAmmo`/`GetCurrentAmmoRounds`). Reserve = inventory count of that ammo form.

### 3.4 Perks and effects

- Perks: `PlayerCharacter::perkRanksPC` `tList<PerkRank{BGSPerk*, UInt8 rank}>` at +0x87C (J GameObjects.h:837-842, 1039). JIP `GetPlayerPerks` → `GetAllPerks` (J jip_fn_misc_ref.h:1345). Teammate perks are at +0xAD4. Traits are perks with the trait flag.
- Active effects: `p->magicTarget.GetEffectList()` → `ActiveEffect{bActive, bTerminated, magicItem, effectItem->setting, magnitude, duration, timeElapsed}`. JIP filters `bActive && !bTerminated && (magnitude>0 || setting->effectFlags&0x100)` (J jip_fn_effect.h:314-334). For "hidden" (traits, crippled limbs) NVWM uses `duration<=0 || >=86400` (main.cpp:698-728).

### 3.5 Quests

- `activeQuest` +0x6B8, `questObjectiveList` `tList<BGSQuestObjective>` +0x6BC, `questTargetList` `tList<ObjectiveTarget>` +0x6C4 (J GameObjects.h:997-999; X GameObjects.h:613-614).
- `BGSQuestObjective{objectiveId, displayText, quest, targets, status}` with `status&1` = displayed and `status&2` = completed (J GameForms.h:1221-1230).
- `TESQuest::questFlags`: `kFlag_IsRunning 1`, `kFlag_Completed 2`, `kFlag_IsShownInPipboy 0x20`, `kFlag_Failed 0x40` (J GameForms.h:4094-4101).
- JIP `GetQuests(completed)` walks `questObjectiveList` for displayed objectives and filters on `questFlags & kFlag_Completed` (J jip_fn_quest.h:145-164). `GetActiveObjectives` walks `activeQuest->lVarOrObjectives` with `(status&3)==1` (188-204).
- The full quest list is `DataHandler::questList` +0x118 (J GameData.h:209).

### 3.6 World, cell, position, map markers, compass

- `p->parentCell`, `cell->IsInterior()`, `cell->worldSpace`. Names via `GetTheName()`/`fullName`.
- `TESWorldSpace::GetNameForLocation(String*, x, y, z)` is virtual (J GameForms.h:3773). Candidate for the Pip-Boy-style location name; unverified.
- Region: `p->currentRegion` +0x760. Last exterior door: +0x604.
- Position: `p->posX/Y/Z`. Heading: `p->rotZ` in radians.
- **Map markers:** `PlayerCharacter::mapMarkers` `tList<MapMarkerInfo{ExtraMapMarker::MarkerData*, TESObjectREFR* markerRef}>` at +0x7C8, with `mapMarkersWorldspace` +0x7D0 (J GameObjects.h:881-885, 1030). `MarkerData{fullName, flags (kFlag_Visible 1, kFlag_CanTravel 2, kFlag_Hidden 4), type (0 None … 14 Vault)}` (X GameExtraData.h:816-843). NVWM main.cpp:244-280 walks this list.
- **World-map projection:** `TESWorldSpace::worldMap` (TESTexture, the DDS path of the in-game map) +0x24, `mapData{usableDimensions, cellNWCoordinates, cellSECoordinates}` +0x80, `worldMapScale/CellX/CellY` +0x90-0x98 (J GameForms.h:3796-3801, 3824, 3842). These suffice to build the exact world→map affine transform, so no manual calibration. TTW has several worldspaces (Wasteland, WastelandNV, DLC worlds); map them by `worldSpace->parent` with `kParentFlag_UseMapData`.
- **Compass:**
  - `compassTargets` `tList<CompassTarget{Actor*, isHostile, isDetected}>*` +0xD48 (J GameObjects.h:887-892, 1048) for actors;
  - quest markers from `questTargetList` (+0x6C4);
  - the player's custom marker: `placedMarkerRef` +0x6F4 / `placedMarkerData` +0x6F8 (J GameObjects.h:1006-1007);
  - compass location markers: unknown (likely nearby map markers filtered by distance; HUDMainMenu internals not examined).

### 3.7 Game date and time

`GameTimeGlobals::Get()` = 0x11DE7B8 → `TESGlobal *year, *month, *day, *hour, *daysPassed, *timeScale` (J GameData.h:896-913). Read `->data`. JIP caches the same pointers at init (J internal/patches_game.h:5546-5552). Time played: `p->timePlayedCurrGame` ms +0x790 (J GameObjects.h).

### 3.8 Radio

- **Current station:** `*(RadioEntry**)0x11DD42C` → `radioRef` (J internal/jip_core.h:68; struct J GameData.h:956-961). JIP `GetPipboyRadio` (J jip_fn_miscellaneous.h:291-296).
- **Known stations list:** no engine global list found.
  - `MapMenu::radioRefList` (`ListBox<TESObjectREFR*>` @0x190, J GameUI.h:1314; `MapMenu::Get()` = `*(MapMenu**)0x11DA368`) holds the Pip-Boy list, but only while the Pip-Boy map menu exists.
  - Otherwise do what NVWM does: scan `DataHandler::cellArray` refs for `kExtraData_RadioData` and compute receivability from `ExtraRadioData{radius @0C, rangeType @10 (0 radius, 1 everywhere, 2 worldspace, 3 linked interiors), staticPerc, positionRef}` (J GameExtraData.h:1017-1024; NVWM main.cpp:349-415). This is heuristic, and TTW's DC stations need checking.

### 3.9 Faction reputation (FNV fame/infamy)

`DataHandler::reputationList` `tList<TESReputation>` +0x90 (J GameData.h:192). `TESReputation{fullName, icon, msgIcon, maxRep @40, positiveRep (fame) @44, negativeRep (infamy) @48}` (J GameForms.h:1579-1591). Faction→rep link is `TESFaction::reputation` (J jip_fn_faction.h:6-12). The title text (e.g. "Liked") derives from the fame/infamy thresholds; the GMST names are unverified.

### 3.10 TTW globals by EditorID

GLOB records keep their EDID at runtime in `TESGlobal::name` (`String` @0x18, `data` float @0x24; X GameForms.h:1769-1788). Two ways to look them up:
- **Robust:** walk `DataHandler::Get()->globalList` (`tList<TESGlobal>` @0xE8, X GameData.h:239; J GameData.h:203) once at `DeferredInit`/`PostLoadGame`, `_stricmp(g->name.m_data, "bInDCWasteland")`, cache the pointer, and invalidate on load.
- Alternative: the engine lookup `TESForm* GetFormByID(const char* editorID)` at 0x483A00 (X GameAPI.cpp:181, GameAPI.h:143-144, commented "Johnny Guitar supports this"). It depends on the EDID map `*(NiTMap**)0x11C54C8` (X GameAPI.cpp:19; J jip_core.h `EditorIDsMap`), which vanilla only populates for some form types.

TTW names checked against the public TTW repo (an **old alpha-era export**):
- `bInDCWasteland` (FormID 0C001E0F, Short) and `bInNVWasteland` (0C00DFAA) exist (`TaleOfTwoWastelands/Global Variables/*.xml`), as do `TTWWorldLocation`, `TTWHasNVSE` and `TTWNVSEVersion`.
- **`TTWPCMiscStatBobbleheadsFound` is not in that export.** Unverified for current TTW 3.x; confirm in xEdit against the installed `TaleOfTwoWastelands.esm`.
- Never hard-code FormIDs: the load-order index varies.

### 3.11 Live UI system colors

- JIP `SetSystemColor` (J jip_fn_ui.h:1224-1234): `SystemColorManager::GetSingleton()->sysColors.GetNthItem(type-1)->SetColorRGB(r,g,b)`, then `ThisCall(0xA0B350, g_interfaceManager->menuRoot, type, 0)` to refresh.
- JIP singleton: `*(SystemColorManager**)0x11D8A88` (J GameUI.h:2094-2129).
- JG `GetSystemColor` (JG functions/fn_ui.cpp:189-197): `ThisCall<uint32_t>(0x7190A0, mgr, type) >> 8` gives 0xRRGGBB, with singleton `ThisCall(0x718B60)` (JG internal/decoding.h:201-247).
- `SystemColor` vtable: `GetColor()` slot 1, `SetColor()` slot 2, `IsHard()`, `IsSoft()`. `HardSystemColor` stores the color at +0x0C; `SoftSystemColor` stores a `Setting*` at +0x0C.
- Type IDs (GECK wiki, GetSystemColor/SetSystemColor via Wayback): **1 HUD-Main, 2 HUD-Alt, 3 Terminal, 4 Pip-Boy, 5 Main/Pause menu**.
- Backing INI prefs (J internal/settings_enum.h:4829-4830): `uPipboyColor:Interface` @0x11D8AE4 and `uHUDColor:Interface` @0x11D8AD4. The other three are presumably hard colors or derived; unverified.
- Native approach: on the main thread, call `GetColor(type)` for 1..5 every ~2 s and push on change.

### 3.12 Files from BSA/loose Data and DDS→PNG

- Engine path (handles loose-over-BSA priority and TTW's BSAs): JG `FileFinder::GetFile(path, NiFile::OpenMode::READ_ONLY, UINT32_MAX, ARCHIVE_TYPE::TEXTURES)` = `CdeclCall<BSFile*>(0xAFDF20, …)` (JG internal/Game/Bethesda/FileFinder.cpp:34-39; used in JG functions/fn_file.cpp:39-108). Singleton `*(FileFinder**)0x11F81DC`; `Exist` 0xAFE0D0. `BSFile` read API: J GameAPI.h:214-247 / JG BSFile.cpp. Thread safety is unverified, so call it on the main thread in small chunks, or …
- … read the BSA directly from disk on the network thread with our own BSA v104 reader (zlib). That needs load-order and archive-list resolution: `*(tList<Archive>**)0x11F8160` (J jip_core.h `ArchivesList`).
- DDS decode: copy SWS's approach, `third_party/bcdec/bcdec.h` (BC1-5/BC7, MIT/Unlicense) + `stb_image_write.h` → PNG (SWS src/game/TextureConverter.cpp:1-80). FNV icons are mostly DXT1/DXT5.
- Better alternative: **send raw DDS (base64 or binary frame) and decode on Android.** This keeps CPU off the emulated game process. NVWM instead pre-converted icons offline (scripts/convert-dds-icons.py) and sends only the path.
- Cache by path plus file size/mtime; icons rarely change.

### 3.13 Optional: local map

- What the engine does: the Pip-Boy LocalMap renders the loaded cells top-down into a texture at runtime (`MapMenu` tiles `MM_LocalMap_ParentImage`, J GameUI.h:1250).
- JIP's HUD minimap reproduces this. It renders each cell with its own `NiCamera` through `BSTextureManager::GenerateRenderedTexture(camera, TextureParams(0x180,0x180,D3DFMT_X8R8G8B8…), &tex)` (J functions_jip/jip_fn_minimap.h:1069-1195). It even has a (commented-out) `SaveLocalMapTexture` that calls `D3DXSaveTextureToFile(…D3DXIFF_DDS…)` to `local_maps\%s.(%d,%d).dds` (1197-1238; the call sites at 2162/2188/2284 are commented out).
- So capture is **possible but hard**: replicate GPL'd naked-asm render code, then GPU readback (GetRenderTargetData + LockRect) every refresh. Under DXVK/Turnip on the Thor that means a stall on a translated D3D9 path. **Not realistic for v1.**
- **Realistic alternative (what SkyrimWebSocket does):** send geometry, not pixels. SWS `src/game/LocalMap.cpp` serializes navmesh vertices and triangles, plus doors, as base64 and the app draws them. FNV equivalents: `TESObjectCELL::navMeshArray` `BSSimpleArray<NavMesh*>*` +0x64 (J GameForms.h:3602) → `NavMesh::vertexArr` +0x28 and `triangleArr` +0x38 (J GameForms.h:3951-3961). Exterior overview: send the worldspace map DDS (§3.6). Fog-of-war: `TESObjectCELL::fullySeen` +0x25 (J GameForms.h:3588).

---

## 4. Actions (all executed inside the MainGameLoop handler)

| Action | Implementation | Source | Confidence |
|---|---|---|---|
| Equip | `Actor::EquipItem(form, count, xData, 1, lockEquip, noMessage)` → `ThisStdCall(0x88C650,…)`. Prefer JIP `EquipItemAlt(entry, noUnequip, noMessage)` which picks the stack's `ExtraDataList` (keeps condition and mods). | X GameObjects.cpp:10,228-231; J jip_fn_inventory.h:230-238, J GameObjects.cpp:444ff | high |
| Unequip | `UnequipItem(item, 1, entry->GetEquippedExtra(), 1, noEquip, noMessage)` (0x88C790) | X GameObjects.cpp:11,233-236; J jip_fn_inventory.h:240-253 | high |
| Use/consume aid | `EquipItem` on an `AlchemyItem` consumes it (NVWM main.cpp:656). Debounce: the app may double-fire (NVWM 590-600). | NVWM | medium-high |
| Drop | `TESObjectREFR::RemoveItem(item, xData, count, keepOwner, 1 /*drop*/, nullptr, …)` per stack, as in JIP `DropAlt`. Or console `player.Drop <id> <n>` via `NVSEConsoleInterface::RunScriptLine2(line, nullptr, true)` (X PluginAPI.h:96-110). | X GameObjects.h:54; J jip_fn_inventory.h:255-318; NVWM 658-667 | high |
| Set tracked quest | `p->activeQuest = q; ThisCall(0x60F110, q, &p->questTargetList, &p->questObjectiveList);` Clear: `activeQuest=NULL; questTargetList.RemoveAll()`. xNVSE `SetCurrentQuest` only sets the pointer. | J jip_fn_quest.h:33-37, 101, 117; X Commands_Quest.cpp:52-62; NVWM 637-650 | medium-high |
| Tune radio | NVWM uses console `PipBoyRadio 1 <refid>` / `PipBoyRadioOff` through `RunScriptLine2`. Exact vanilla syntax unverified. Native: unknown engine function. | NVWM 622-634 | medium-low |
| Pip-Boy light | JIP `TogglePipBoyLight`: state = `ThisCall<bool>(0x822B90, &p->magicTarget, &PipBoyLight->magicItem, 1)`. On: MagicCaster vcall at p+0x88. Off: 0x824400 on p+0x94. Then UI refresh 0x7FA310 ×2. Spell = `*(SpellItem**)0x11C358C`. | J jip_fn_ui.h:1430-1474; J jip_core.h:52 | medium (naked asm; port carefully) |
| Fast travel | (a) the real engine fast-travel routine: JIP hooks the call at 0x93BF22 and jumps to **0x93CDF0** (`thiscall(PlayerCharacter*, TESObjectREFR* markerRef)`). Calling it directly should give time passing, hardcore needs and companions, but whether it is safe outside MapMenu is **unverified**. (b) NVWM's teleport `MoveToMarker` 0x5CCB20 `(ref, marker, 0,0,0)` (no time or needs). Check `MarkerData::kFlag_CanTravel`, worldspace `kWorldFlag_NoFastTravel`, combat and encumbrance first. | J internal/hooks.h:737-751, 5101; NVWM 48-51, 603-607 | medium/low |
| Custom map marker | `ThisCall(0x952E60, p, NiPoint3 pos /*by value*/, TESForm* space)`, where space = interior cell or worldspace. Remove: `ThisCall(0x952F90, p)`. | JG internal/Game/Bethesda/PlayerCharacter.cpp:93-100; JG functions/fn_gameplay.cpp:231-248 | high |

General rules:
- Re-resolve forms by refID each time (`LookupFormByID`). Never execute a command against a pointer cached before a load.
- Reject commands while in menus, dialogue or loading, and return `commandResult{success:false, error}`.

---

## 5. Networking

| Option | Windows I/O model | Fit for a 32-bit DLL under Wine | Notes |
|---|---|---|---|
| **Hand-rolled on Winsock** (as NVWM) | blocking `accept/recv/send` + `select()` | best: smallest Wine API surface; NVWM shows it in production on GameHub Lite (implied) | ~400 lines: RFC 6455 handshake (SHA-1+base64), framing, ping/pong, close. Add a real JSON lib (nlohmann/json or a small header-only one). |
| civetweb | embedded C server, `poll()` emulated by `select()` on Windows (civetweb.c:5346-5381) | good; MIT; built-in WebSocket server | Thread-per-connection by default; drops into CMake easily. |
| IXWebSocket | thread per connection; Windows `poll` emulated with `WSAEventSelect`/`WSAWaitForMultipleEvents` (IXNetSystem.cpp:100-200) | good; BSD-3 | C++11, optional zlib and TLS (both unneeded). |
| websocketpp + standalone asio / Boost.Beast | **IOCP** (AcceptEx, overlapped I/O) | works in Wine generally, but more surface and much heavier | SkyrimWebSocket uses **Boost.Beast + Boost.Asio** via vcpkg (SWS vcpkg.json; src/server/WsSession.cpp), all game access via `SKSE::GetTaskInterface()->AddTask` (MessageRouter.cpp:141-525). |
| mongoose | select/poll event loop | fine technically | GPLv2/commercial dual license. |
| uWebSockets | uSockets/libuv | poor fit (heavy, 64-bit focus) | skip |

**Recommendation:** hand-rolled server (or civetweb if we want less code), with one network thread.
- **Bind 127.0.0.1 only.** NVWM's INADDR_ANY exposes the game to the LAN. Make LAN binding an explicit opt-in for the PC+handheld mode.
- Port configurable through an INI in `Data/NVSE/Plugins/`.
- Single client, or a few.

**Threading model:**
1. Network thread: select() loop with a 20–50 ms timeout or a self-pipe/loopback wake socket. It does handshake and framing, parses JSON into command structs, and pushes them to `inQueue` (mutex + `std::vector`, swap-drain like NVWM cmd_queue.h). It pops from `outQueue` and sends. **It never touches game memory.**
2. Main thread (`kMessage_MainGameLoop`):
   - drain `inQueue` (bounded per frame) and execute commands;
   - evaluate subscriptions (id, fields, frequency, lastSent, lastHash);
   - read state and serialize to JSON strings on the main thread, so no game pointers cross threads;
   - push strings to `outQueue`.
   - Heavy snapshots (inventory, markers, quests, radio scan) are rebuilt only when dirty (xNVSE native events, §1.3) or at a slow cadence, and staggered across frames.
   - Keep the per-frame budget well under 1 ms. Every instruction is x86-on-ARM.
3. Synchronization: use only `std::mutex`/`std::atomic` (lock-prefixed or SRWLock). **No benign races or plain-flag handoffs** (see Box64 `STRONGMEM`, §6).
4. Send-on-change: hash each serialized field payload and only send on difference, plus a heartbeat (SWS PROTOCOL.md semantics: subscribe/unsubscribe/query/command/heartbeat → data/heartbeat/error/commandResult; NVWM docs/fnv-websocket-plugin.md §5).

---

## 6. Wine / Box64 / FEXCore risks on Android

Evidence found:
- **xNVSE itself loads on the Thor under GameNative 1.1.1** when launched through the patched `FalloutNV.exe`. The patcher adds a `.gnvse` PE section that calls `LoadLibraryA("nvse_steam_loader.dll")` before the entry point. CyberShonk CHANGELOG.md "Validated": "Confirmed on an AYN Thor using GameNative … xNVSE logs recognize the executable as patched and xNVSE loads through the normal FalloutNV.exe launch path". The patcher exists because GameNative's Steamless unpack cache (`FalloutNV.exe.unpacked.exe`) overwrites a normally patched exe (README "Why this project exists"). Native **plugin** loading is not stated anywhere in that repo.
- **`nvse_loader.exe` fails under Winlator** with "cannot load FalloutNV.exe" error 998 (ERROR_NOACCESS, i.e. remote injection failing). Workaround: the 4GB patcher / steam_loader path, "worked for me" for one user and still crashed for another (Nexus forum topic 13493813, July–Dec 2024, via Wayback 20250617).
- **Coordinator-supplied (not verified by me):** a community video shows TTW on the Thor under GameNative with **FEXCore 2512 for 32-bit**, DXVK 1.11, Turnip and `nvse_loader.exe` as the executable. So the injection path can work under FEX WoW64 there.
- NVWM targets GameHub Lite on the Thor and appears to work (§0, inferred).
- Box64 issue #1589 "Fallout New Vegas crashes very often" (2024-06): "STRONGMEM=1 solves the problem" (memory ordering). Box64 issue #2587: FNV under muvm + Wine WoW64 hangs at low resolutions (desktop, closed).

**FEXCore WoW64 assessment.**
In Wine's new WoW64 mode the 32-bit process runs x86 code through an emulator DLL: FEX's WoW64 module, or Box64's wow64 module. Everything below the 32-bit PE DLLs (ntdll syscalls → Unix side) is native ARM64 Wine. Consequences:
- **Winsock** is Wine's PE `ws2_32.dll` → AFD ioctls → Wine's Unix socket code. The emulator only translates our x86 instructions, so socket semantics are Wine's regardless of FEX vs Box64. Loopback is the Android app's own network namespace. Android apps share the device's loopback, so the companion app can reach `127.0.0.1:<port>` opened by GameNative's Wine process (NVWM relies on the same thing under GameHub Lite). There is no FEX-specific Winsock risk beyond Wine's.
- **Plugin loading:** our DLL is an ordinary PE32 loaded by xNVSE via LoadLibrary, so it behaves like `nvse_1_4.dll` itself. If xNVSE works under FEX (per the video), a plugin that does not patch code is lower-risk than xNVSE.
- **Memory model:** FEX emulates x86 TSO conservatively by default (configurable; disabling it causes crashes) and added FEAT_LRCPC2 support in 2501. Box64's default is weaker (hence STRONGMEM reports). Use proper atomics and mutexes so we are correct under either.
- **Self-modifying code:** xNVSE patches game code with `VirtualProtect` + write (X SafeWrite.cpp:7-75). FEX invalidates translated pages, and 2501 added finer-grained inline-SMC handling. **Our plugin should write no hooks of its own in v1**; rely on `kMessage_MainGameLoop` and xNVSE events.
- **32-bit address space:** FEX 2509 fixed FEX allocating its own memory inside the 32-bit address space under WoW64 (release notes). Older FEX builds compete with FNV for its ≤4 GB, which matters for TTW. Keep the plugin lean: no Boost, small buffers.
- FEX 2511 enabled AVX for 32-bit but not under Wine WoW64 (Phoronix). Irrelevant as long as we build without `/arch:AVX` (default for x86 MSVC is SSE2). Build with `/arch:SSE2` or `/arch:IA32` to be safe.

**Top risks (ranked):**
1. **Focus / foreground.** xNVSE's main-loop hook runs "only when new vegas is in the foreground" (X Hooks_Gameplay.cpp:1132). FNV minimizes and pauses on focus loss (the reason AltTabFix exists). On the Thor, tapping the bottom screen moves focus (AYN Focus Mode: auto / lock top / lock bottom; Notebookcheck). Result: the pump stops and commands stall until focus returns.
   Mitigations to test:
   - lock Focus Mode to the top screen;
   - give the companion activity `FLAG_NOT_FOCUSABLE` so it takes touch but not focus (unverified on Thor);
   - an AltTabFix-style plugin;
   - check whether Wine even sees an X focus-out.
   **This is the #1 spike item.**
2. **Loader path differs per frontend:** nvse_loader injection (error 998 on Winlator, works under GameNative+FEX per the video), the GameNative 4GB patcher's `.gnvse` steam_loader path, and GameHub. Document per-frontend setup and detect "xNVSE missing" in the app (no server = show setup help).
3. **Engine-address correctness and TTW.** Every non-xNVSE read uses raw 1.4.0.525 offsets and addresses from JIP/JG/NVWM. A wrong one crashes the game, which is painful to debug under Wine on a handheld. Also TTW-specific: multiple worldspaces, DC radio, TTW globals that changed since the public export.
4. **Main-thread cost under translation.** Inventory walks, cell scans for radio stations and JSON building on the game thread cost frame time on FEX/Box64. Mitigate with event-driven dirty flags, staggering and send-on-change.
5. **Threading/memory-order bugs and lifecycle.** Cross-thread races that are invisible on x86 can surface under Box64 weak ordering. Stale `TESForm*` caches across load and quit-to-menu cause use-after-free.

Secondary risks:
- Android 17 "local network" permission. Enforced for apps targeting SDK 37; the docs do not say whether loopback counts (unverified). Keep the companion app at targetSdk ≤36 initially, or test with `adb shell am compat enable RESTRICT_LOCAL_NETWORK`.
- Binding 0.0.0.0 exposes the game to the LAN.
- GameNative's 4GB patcher is alpha (0.1.3-alpha) and must be re-run after GameNative re-unpacks.
- The xNVSE repo has no license file; JIP is GPLv3, so copying its code makes our plugin GPLv3.

---

## 7. Field → access method → citation → confidence

| Field | Access method (main thread) | Source citation | Confidence |
|---|---|---|---|
| Health / AP / rads (cur, max) | `p->avOwner.Fn_03/Fn_08(16 / 12 / 54)` | X GameForms.h:1316-1335; X GameAPI.h:289-385; NVWM main.cpp:670-681 | high |
| Limb condition ×6 | AV 25-30, cur/perm ×100 | X GameAPI.h:321-330; NVWM main.cpp:686-691 | high |
| Level | `avOwner.Fn_0A()` | X GameForms.h:1332 | high |
| XP current | AV 24 | X GameAPI.h:319 | high |
| XP next level | `*(PCLevelData**)(p+0x878)->xpToNextLevel`, or formula/GMST | J GameObjects.h:895-901,1038; J settings_enum.h:1115 | medium (verify semantics) |
| SPECIAL | AV 5-11 (`Fn_08` base, `Fn_03` with buffs) | X GameAPI.h:297-305 | high |
| Skills | AV 32-45 | X GameAPI.h:331-346 | high |
| Karma | AV 23 | X GameAPI.h:318 | high |
| Carry / inventory weight | AV 13 / AV 46 | X GameAPI.h:308,348 | high |
| DT / DR | AV 76 / AV 18 | X GameAPI.h:313,381 | high (DT AV vs armor sum: verify) |
| Caps | inventory count of form 0xF (base + delta) | J internal/patches_game.h:5552; J jip_fn_inventory.h:279 | high |
| Hardcore flag | `p->isHardcore` @0x7BC | X GameObjects.h:621; J GameObjects.h:1024 | high |
| H2O / FOD / SLP | AV 73 / 74 / 75 | X GameAPI.h:378-380 | high |
| Inventory list, counts | `ExtraContainerChanges::GetForRef(p)` entries; count = base `TESContainer` + `countDelta` | X GameExtraData.h:319-404; J jip_fn_inventory.h:261-283 | high |
| Category / weight / value | `typeID`; `TESWeightForm` / `TESValueForm` | NVWM main.cpp:96-103,488-552 | high |
| Equipped state | stack `ExtraDataList` has `kExtraData_Worn/WornLeft` | NVWM main.cpp:463-464; X GameExtraData.h:557 | high |
| Condition | `ExtraHealth::health` / base health | X GameExtraData.h:546; NVWM main.cpp:454-479 | high |
| Icon path | `TESIcon::ddsPath` (per-type offset or DYNAMIC_CAST) | NVWM main.cpp:141-177 | medium-high |
| Equipped weapon | `Actor::GetEquippedWeapon()` | X GameObjects.h:526; J GameObjects.cpp:1542 | high |
| Ammo type / clip | `Actor::GetAmmoInfo()` (HighProcess+0x118): `type`, `countDelta` | J GameObjects.cpp:1524-1535; J jip_fn_actor.h:1050-1064 | medium-high |
| Ammo reserve | inventory count of the ammo form | (as inventory) | high |
| Perks | `p->perkRanksPC` @0x87C `tList<PerkRank>` | J GameObjects.h:837-842,1039; J jip_fn_misc_ref.h:1345 | high |
| Active effects | `magicTarget.GetEffectList()` filter active/!terminated | J jip_fn_effect.h:314-334; NVWM main.cpp:698-728 | high |
| Active / tracked quest | `p->activeQuest` @0x6B8 | J GameObjects.h:997; X GameObjects.h:613 | high |
| Current objectives | `p->questObjectiveList` @0x6BC; `status&1` displayed, `&2` done | J GameForms.h:1221-1230; J jip_fn_quest.h:188-204 | high |
| Completed quests | `quest->questFlags & kFlag_Completed (2)`; `Failed 0x40` | J GameForms.h:4094-4101; J jip_fn_quest.h:145-164 | high |
| Set active quest | write `activeQuest` + `ThisCall(0x60F110, …)` | J jip_fn_quest.h:101,117; NVWM main.cpp:637-650 | medium-high |
| Worldspace / cell / name | `p->parentCell`, `->worldSpace`, `GetTheName()`; `GetNameForLocation` | J GameForms.h:3773; NVWM main.cpp:730-739 | high (name text: medium) |
| Position / heading | `posX/Y/Z`, `rotZ` (rad) | NVWM main.cpp:731 | high |
| Map markers | `p->mapMarkers` @0x7C8 `tList<MapMarkerInfo>`; `MarkerData` flags/type | J GameObjects.h:881-885,1030; X GameExtraData.h:816-843; NVWM main.cpp:244-280 | medium-high |
| World map image + bounds | `TESWorldSpace::worldMap` @0x24, `mapData` @0x80, scale/cell @0x90-0x98 | J GameForms.h:3796-3801,3824,3842 | medium |
| Compass targets | `p->compassTargets` @0xD48 (actors); `questTargetList` @0x6C4; `placedMarkerRef` @0x6F4 | J GameObjects.h:887-892,999,1006,1048 | medium |
| Game date / time | `GameTimeGlobals::Get()` (0x11DE7B8) → TESGlobal year/month/day/hour/daysPassed/timeScale | J GameData.h:896-913 | high |
| Radio: current | `*(RadioEntry**)0x11DD42C` → `radioRef` | J jip_core.h:68; J GameData.h:956; J jip_fn_miscellaneous.h:291-296 | high |
| Radio: station list | `MapMenu::radioRefList` (Pip-Boy open only), or scan cell refs with `ExtraRadioData` | J GameUI.h:1314,1319; J GameExtraData.h:1017-1024; NVWM main.cpp:349-415 | medium-low |
| Faction reputation | `DataHandler::reputationList` @0x90 → `positiveRep` / `negativeRep` / `maxRep` | J GameData.h:192; J GameForms.h:1579-1591 | high (titles: unverified) |
| TTW globals by EDID | walk `DataHandler::globalList` @0xE8, match `TESGlobal::name`; or `GetFormByID(const char*)` 0x483A00 | X GameData.h:239; X GameForms.h:1769-1788; X GameAPI.cpp:181 | high (method); names medium |
| System colors (5) | `SystemColorManager` (0x11D8A88 / `ThisCall 0x718B60`) → `GetColor` (0x7190A0) `>>8` | J GameUI.h:2094-2129; JG decoding.h:201-247; JG fn_ui.cpp:189-197; J settings_enum.h:4829-4830 | high |
| BSA / loose file read | `FileFinder::GetFile` cdecl 0xAFDF20 → `BSFile` | JG FileFinder.cpp:34-39; JG fn_file.cpp:39 | medium (thread safety unverified) |
| DDS→PNG | bcdec + stb_image_write, or raw DDS to the app | SWS src/game/TextureConverter.cpp:1-80 | high |
| Local map | geometry: `cell->navMeshArray` @0x64 → `NavMesh` vertices/triangles; pixels: JIP minimap render (hard) | J GameForms.h:3602,3951-3961; SWS src/game/LocalMap.cpp; J jip_fn_minimap.h:1069-1238 | geometry medium; pixels low |
| Game status (menus, loading, combat) | `IsMenuVisible(kMenuType_*)`, `CdeclCall<bool>(0x702360)` = menu mode, `p->pcInCombat` @0xDF0 | X GameUI.h:25,96-103; X Hooks_Gameplay.cpp:962; J GameObjects.h:1066 (pcInCombat) | high |

---

## 8. Suggested go/no-go spike (in order)

1. On the Thor, under each frontend (GameNative+FEX, GameHub, Winlator): confirm xNVSE loads (console banner "xNVSE 6.4.9", printed at X Hooks_Gameplay.cpp:895-905).
2. Build a 100-line plugin with CI MSVC:
   - log on Load;
   - start a 127.0.0.1 WebSocket echo;
   - send a counter incremented in `kMessage_MainGameLoop`.
3. From the Android app on the bottom screen, connect, then **tap the bottom screen and watch whether the MainGameLoop counter freezes** (risk #1). Test Focus Mode lock and `FLAG_NOT_FOCUSABLE`.
4. Read HP, `isHardcore`, `bInDCWasteland` and `GetColor(1..5)`, then send one equip command. That proves reads, actions and TTW globals.
