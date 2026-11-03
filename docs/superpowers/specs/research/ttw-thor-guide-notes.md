# TTW-on-Thor research notes (sources, conflicts, gaps)

Companion to `ttw-thor-guide-draft.md`. Researched 2026-11-03.

## How sources were reached
- **Reddit**
  - `.json` endpoints: HTTP 403 with every user agent. WebFetch and the SearXNG reader are also blocked.
  - Redlib mirrors sit behind Anubis proof-of-work challenges. Not bypassed.
  - What worked: the public **RSS feed** (`https://www.reddit.com/r/<sub>/comments/<id>/.rss`), with 20–25 s gaps between requests to stay under the 429 rate limit.
  - Wayback had only the 2024 r/winlator thread.
- **GitHub**
  - The REST API was rate-limited (unauthenticated).
  - The GitHub MCP tool worked for releases, issues, and code search. Its *issue search* returned 0 results for every query, so GameNative issues were **not** reviewed. Releases and source code were reviewed instead.
- **EmuReady** renders in the browser, so listings were read with Playwright in an isolated tab.
- **Nexus Mods** pages were read via Wayback snapshots (57486, 62552, 81281). Nexus 98222 (FNV GameNative 4GB Patcher) has no snapshot; its GitHub README was used instead.
- **YouTube**
  - Descriptions were read from the page metadata.
  - The main video's content comes from the **user-supplied summary** (`research/user-video-ttw-gamenative.md`), not a transcript we pulled. Treat its exact strings as possibly misheard.

## Primary sources and what each established

### Official TTW / ModdingLinked
- **mod.pub TTW** (<https://mod.pub/ttw/133/files>)
  - Current release: **TTW 3.4** (v3.4.0, 1.2 GiB, uploaded 2026-06-11).
  - **YUPTTW 13.9.1**: "Install this as an MO2 mod and load it after TTW."
  - Updated OGG Vorbis files: optional, go in the FNV root folder.
  - TTW Conversion Package 3.4.3: not needed unless you convert mods.
  - Downloads need a login.
  - The mod.pub "Installation guide" and "FAQ" docs only link to The Best of Times.
- **taleoftwowastelands.com**: the forum is read-only and points to the Discord (<https://discord.taleoftwowastelands.com>).
- **The Best of Times (TBoT)**, ModdingLinked's TTW guide. There is no separate "Capital Wasteland" guide on moddinglinked.com (subdomains: vivanewvegas, thebestoftimes, themidnightride, dragonbornsfate).
  - **intro.html**
    - English copies with all DLC from Steam, GOG, or Epic, and you may mix stores.
    - Microsoft Store copies are unsupported. German No-Gore New Vegas is incompatible with xNVSE. The Fallout 3 GFWL version is OK if it is the English GOTY.
    - Needs 40 GB free (TTW itself is 17 GB), Windows 10+ 64-bit, and MOD:PUB and Nexus accounts.
  - **setup.html**
    - No default Windows folders (Program Files, Desktop, Documents); example `C:\Games`.
    - Fallout 3 only needs to be installed while the TTW installer runs.
    - Install the VC++ redists with winget. Mandatory ASLR off.
    - Run each game's launcher once to create INIs.
    - Epic's INI folder is `My Games\FalloutNV_Epic`.
  - **ttw.html**
    - Extract the installer outside the default Windows folders and Run As Administrator.
    - Audio encoding takes about 40 minutes on a Ryzen 5 5600X, possibly hours on slower CPUs.
    - The output folder should be 17 GB.
  - **essentials.html**: the mod order used in the guide.
    - Root: xNVSE, then the 4GB Patcher (Steam/GOG) or the Epic Games Patcher.
    - Then: TTWNVSE, JIP LN (+INI), JohnnyGuitar (+All Tweaks preset), ShowOff (+INI), NVTF (+INI), Stewie Tweaks (+VNV INI), Engine Optimizations, Combat Lag Fix, VATS Lag Fix, ActorCause Save Bloat Fix, kNVSE, UIO, MLF, Crash Logger, Improved Console, Console Paste.
    - FalloutCustom.ini block, including `SCharGenQuest=001FFFF8`.
    - ML Utilities Checker as a test step.
    - "Avoid using tools like Bethini."
  - **faq.html**
    - YUP is incompatible with TTW (use YUPTTW), as are UNP Plus, ILO (use SILO + TTW patch), and Increased Wasteland Spawns ("massive save bloat and crashes").
    - Old saves won't work.
    - Updating: patch versions are save-safe; major or minor versions need a new game.
    - Linux is not officially supported (Fluorine/Amethyst, Discord channel only).
    - Installer errors and fixes (xdelta3, BASS/XD3, not absolute, NTDLL → `-threads:1`, Unblock).
    - Lock FPS below 120.
    - Verifying game files reverts the Vorbis update and the exe patch.
- **xNVSE** (<https://github.com/xNVSE/NVSE/releases>): **6.4.9**, 2026-09-16, as `nvse_6_4_9.7z`. Goes in the base game folder.
- **4GB Patcher** (Nexus 62552, Wayback 2026-09-15)
  - Steam or GOG. Auto-loads xNVSE.
  - "Launch the game normally through your mod manager, directly, the Default Launcher, or Steam."
  - Requires the VC++ 2013 runtime.
- **Epic Games Patcher** (Nexus 81281, Wayback 2026-09-19)
  - Downgrades to the Steam exe, makes it 4 GB-aware, auto-loads NVSE.
  - "Do not use nvse_loader.exe … Forget it exists."
  - Removes some Epic integration (achievements).
  - The Steam exe is unoptimised, so performance is lower.
- **Fallout New Vegas Registry Fix 32_64 Bit** (Nexus 57486): a batch file that writes the install-path registry entry. "Can be used with Steam."
- **GOG support article 115003633889**: `beth_registry.zip` → `beth_registry.cmd`. Put it in the game folder, run it, and choose to update the registry. Fixes greyed-out launcher options and a missing Play button.

### GameNative (code and releases)
- **Releases**
  - v1.3.0 stable (2026-10-14). v1.4.0-prerelease (2026-10-22) makes gamescope the default renderer, flagged as a "Potential breaking change".
  - Relevant changes:
    - v1.1.1 (2026-07-26): Nexus Mods support, local mod import.
    - v1.2.1: GOG installer v1 ("fixes registry keys").
    - v1.3.0: Nexus/FOMOD placement, automatic Wine DLL overrides for mods, Import/Export Files, "Reset clocks and fan after the app is force killed", PServer CPU-frequency detection fix.
    - v1.4.0-pre: fix for games seeing two controllers on the AYN Thor and Flip 2.
- **Built-in New Vegas registry fixes**
  - Files: `gamefixes/STEAM_22380.kt`, `STEAM_22490.kt` (PCR), `GOG_1454587428.kt`, `EPIC_dabb52e328834da7bbe99691e374cb84.kt`.
  - Each is a `KeyedRegistryKeyFix(gameSource, gameId, "Software\Wow6432Node\Bethesda Softworks\FalloutNV", "Installed Path" → install path)`.
  - **Inference:** the fixes are tied to a store and game ID, so a custom game copied from a PC does not get them. That matches every custom-game report needing the manual registry fix.
- **`mods/BethesdaPluginManager.kt`**
  - `updateManagedPluginsTxt` writes `plugins.txt` and `loadorder.txt` as: known *vanilla* base plugins first, then retained lines, then managed plugins.
  - The FNV base order lists CaravanPack…TribalPack right after GunRunnersArsenal. TTW expects the Fallout 3 ESMs between those (see the Pocket Wastelands starter order).
  - **Inference:** don't let GameNative's built-in mod manager touch TTW's `plugins.txt`.

### Thor-specific community guides and tools
- **User-supplied summary of Timeless Jupiter's video** (`research/user-video-ttw-gamenative.md`; video <https://www.youtube.com/watch?v=JiqVAiGlCtU>, published 2026-07-27, GameNative 1.1.1).
  - This is the guide's backbone. It covers: Vortex on PC → package root/config/appdata/redist → GameNative custom game → container settings → first Play to initialise → VC++/DX in the container → copy appdata/config → `registry.bat` + `beth_registry` → launcher check → `nvse_loader.exe`.
  - Video description links: TBoT intro, the [TTW Essentials Collection](https://www.nexusmods.com/games/newvegas/collections/c1infa/revisions/2), Nexus 57486, TechPowerUp DirectX June 2010.
- **r/AynThor 1v6sao6** (Jupiter's thread, RSS)
  - Needs a PC with GOG or Steam Fallout 3 GOTY and FNV GOTY.
  - The GameNative 1.1.1 update changed the setup steps, so he made the updated video.
  - ClusterTune "on Medium" keeps it around 70 °C.
  - A commenter (bverwijst) got a black screen after copying via ADB. Jupiter's suggestions:
    - use only nvse_loader
    - check the container settings
    - "Try switching to 1.11 sarek"
    - "double check you are using the Legacy Wrapper"
    - "ADB transfer can sometimes reorganize files"
    - he copies over a high-speed USB-C cable
  - That commenter also mentioned Pocket Wastelands as a fallback.
  - The NMC texture packs can be merged through the TTW installer.
- **Shonk's Pocket Wastelands** (<https://cybershonk.github.io/PocketWastelands/>), changelog through 2026-06-26.
  - Rules: build TTW on PC; GOG is recommended; Steam works with a folder-rename / container-refresh workaround; Epic should work but is untested; keep the game in shared storage `/storage/emulated/0/PcGames/Fallout New Vegas/`.
  - **8 Gen 2 profile: "Verified working", last tested 2026-06-11, on GameNative 1.0.0.**
    - Container and translation: bionic, proton-10.0-arm64ec-2, FEXCore 2604 (Intermediate), Box64 0.3.6 (Compatibility), wow64Mode.
    - Graphics: DXVK 2.6.1-gplasync, turnip26.0.0_R8 (Vulkan 1.3, maxDeviceMemory 4096), videoMemorySize 2048, 1280×720, Mailbox.
    - Audio: PulseAudio with `PULSE_LATENCY_MSEC=144`.
    - Frame rate: 60 FPS limiter.
    - CPU list 0–6; WoW64 CPU list 4–7.
    - Windows components: directsound, vcrun2010, wmdecoder.
    - The JSON's executablePath is `FalloutNVLauncher.exe`.
    - Logged session: average 46 FPS over 111 s.
  - **Memory warning:** "Use 2 GB memory and 2 GB video memory unless your Android device has 16 GB of RAM."
  - Proton 11 ARM64EC is marked testing-only.
  - Avoid list: ENB, Wabbajack, LOD generation on Android, old managers, random INI packs, **Sound Extender NVSE** (crashes under GameNative).
  - The load-order page explains that TTW/FNV order plugins by file timestamp, and gives the full starter `plugins.txt`.
- **FNV GameNative 4GB + xNVSE Patcher** (CyberShonk, GitHub; Nexus 98222)
  - v0.1.3-alpha (2026-08-13). Steam copies installed through GameNative only.
  - GameNative's Steamless unpacking leaves `FalloutNV.exe.unpacked.exe`, which gets copied back over the exe. Patching only `FalloutNV.exe` therefore doesn't stick; this patcher patches both copies.
  - Run it by temporarily setting Executable Path to `FNVGameNativePatcher.exe`. The "Open Container" launch doesn't bring its window to the front on GN 1.1.1.
  - Tested on a real AYN Thor.
- **Droid Mod Loader** v0.8.0-beta: profiles include "Tale of Two Wastelands through a Fallout: New Vegas target". Needs Android 11+ and all-files access. Writes plugins.txt with timestamp ordering for FO3/FNV/TTW ("apply the selected order through plugin modification timestamps", per the user guide).

### Reddit and EmuReady reports (RSS / Playwright)
- **impulse9studio** (r/AynThor 1r8t7z6, Feb 2026; Thor Pro; GOG; FNV + mods, not TTW)
  - Settings: GameNative, Proton-10.0-arm64ec-2, turnip26r8, DXVK async-1.10.3, Box64 0.4.0 Performance preset, 720p, Medium.
  - About 60 FPS (55 average on EmuReady), about 5 h battery with an 80% charge limit.
  - Needed registry `Installed Path` = `A:\` (GOG).
  - GameHub: same FPS but NVSE error 998. Winlator/star: about 30 FPS, or crashes with the Performance preset.
- **PrivateSeaCow** (same thread): TTW on the Thor via GameNative.
  - Workflow: built with MO2 on PC and zipped through MO2's virtual explorer. Copies My Games and AppData. Load order lives in `AppData/Local/FalloutNV/plugins.txt`.
  - A save-load crash was fixed by changing the Box64 and FEXCore presets from Performance to **Intermediate**. Gets 45–50 FPS, with peaks of 60.
  - On GameHub Lite, the `xact_64` component.
- **Kobeau2123** (same thread)
  - GameHub NVSE fix: temporarily switch to proton_11.0arm64 to run NVSE once, then use MO2.
  - Ran the Beth registry file after installing the GOG version.
- **_joshd** (r/AynThor 1vb2q28, Jul 2026, Thor Max)
  - Settings: bionic, proton-10.0-arm64ec-2, FalloutNV.exe, 1280×720, pulse, wrapper-v2, "turnip t28-toaster (@mr_purple_666)", Vulkan renderer, DXVK 2.6.1-gplasync, Adrenotools on, Mailbox / renderer fifo, FEXCore 2512 (32-bit), Box64 0.3.6 Compatibility, FEXCore Intermediate.
  - Run the launcher once first (720p, AA 4, AF 8, V-sync on).
- **AlmightyOslo** (same thread): turned "Use Legacy DRM" off and "Unpack Files" on, and it then worked (Steam copy).
- **Specialist-String286** (1u3sr3b): change the executable from FalloutNVLauncher.exe to FalloutNV.exe to fix a black screen. Switch back only to change graphics.
- **JR004-2021** (1um5p4b): the top-left-corner picture is fixed by setting 16:9 in the launcher. Use Medium graphics.
- **HeidenShadows** (1um5p4b): "Need to install DirectX manually inside the common redistributable folder."
- **Aggressive_crackhead** (1t5cey9, Thor Max): vanilla FNV crashes after about 10–30 minutes whatever he tried.
- **burcbuluklu** (1t5cey9): 60 FPS with no crash on GameNative, but NPC heads spin. Config at pastebin aqWwAtPw (not read).
- **paraguybrarian** (1ry3t7g, Thor Max)
  - "Steam versions work but require registry keys for NV."
  - NV settings: Bionic, Proton-9.0-x86_64, turnip26.0.0_R8, 2.4.1-gplasync.
  - The second launch fixes unresponsive controls.
  - Pro vs Max: RAM rarely matters; storage does.
- **soupnazi81** (1ry3t7g): the FO4 Pip-Boy companion app 1.2 works with GOTY FO4 in GameHub Lite on the Thor (a data point for container ↔ Android networking).
- **ConnoisseurDeFemboys** (r/GameNative 1uxy6en; Xperia 1 VII / 8 Elite, not a Thor)
  - Container: Proton 11 arm64ec, DXVK 2.7.1-gplasync-0, FEXCore 2601 Intermediate, launcher Medium.
  - Graphics: shadows off, fade 14, grass 3, land quality 85.
  - Driver: Turnip Gen8 V33 (from StevenMXZ's Adreno-Tools-Drivers).
- **EmuReady FNV on the Thor**
  - [9f418d9c](https://www.emuready.com/listings/9f418d9c-16ef-4c3e-955a-515e92b16c13): Pro, GameNative, GOG, "Great".
  - [739d40eb](https://www.emuready.com/listings/739d40eb-cdd2-46df-b2b1-00b6f18e885b): Max, GameNative 0.9.1, bionic, proton-10.0-arm64ec, KIMCHI Turnip v26.0.0 R8, DXVK 2.6.1-gplasync, FEXCore 2512 Intermediate, Box64 0.3.6 Performance, 55–60 FPS.
  - [9923f28c](https://www.emuready.com/listings/9923f28c-6770-487c-9bd3-a52cc041f8d3): Max, GameHub 5.3.5, proton10.0-arm64x-2, turnip_v26.1.0_R4, dxvk-2.3.1-async, Fex-20251128, components Quartz/gecko/d3dx9/oalinst/mono/vcredist2022, Steam, 60 FPS, "1 crash in 50 hours".
  - [8fe32580](https://www.emuready.com/listings/8fe32580-35bc-40d4-ab15-df81045a8265): Max, GameHub 5.3.5, turnip_v25.0.0_R1, 50 FPS, 40% verified, with a comment that it doesn't work.
  - [80f24173](https://www.emuready.com/listings/80f24173-dec8-4e44-acff-dfabc2b5d085) (the "no sound / DXSetup" listing) is **Skyrim SE on GameHub (Thor Pro), not FNV**.
    - Fix: add Mono, Gecko, and dotnet40 as components, then run `directx_Jun2010_redist.exe` from Downloads inside the container.
    - "Translation profile should be extreme."
    - A commenter says GOG copies need the Wine compatibility layer (10.6).
- **2024 r/winlator 1hgi5bq** (Ntrmnt; Poco F3 SD870; Winlator Frost 10.0 b2; GOG). TTW works, with these caveats:
  - MO2 2.4.4 inside Winlator needs Wine Mono.
  - The TTW install inside Winlator "was taking too long", so it was built on a laptop and the files copied back.
  - "Do not touch your phone while the loading screens are running", or it crashed every time.
  - Crash at the Vault 101 "6 years later" cutscene. Workaround: the Nexus 70729 save.
- **2024 r/winlator 1fv399e** (GOG FNV .NET hang)
  - Kill the .NET/DX sub-setups in Task Manager and the main install continues (JoNAsOS3).
  - Or install Mono/Gecko first and skip .NET (Professional_Bag8275).
  - Or set `mscoree` to native,builtin in winecfg (coffincolors).
  - Not needed when FNV is installed on a PC and copied over.
- **Box64 #1936** (2024, SD778G): FNV was stable only with `STRONGMEM=1`, which cost about 20 FPS on that device. ptitSeb suggested `BOX64_X87DOUBLE=1` without STRONGMEM. Current Thor setups use FEXCore for 32-bit code, so this matters mainly if someone falls back to Box64.
- **Thor hardware** ([Retro Handhelds guide](https://retrohandhelds.gg/ayn-thor-setup-guide/))
  - Base / Pro / Max = SD 8 Gen 2 with 8 / 12 / 16 GB RAM and 128 GB / 256 GB / 512 GB or 1 TB storage. Lite = SD865.
  - Quick menu: Performance Mode Standard/Medium/High; Fan Quiet/Smart/Sports, with a custom curve via Smart → Edit.
  - **Controller input follows the last-touched screen.**
  - PC emulation from microSD can stutter or crash.
- **r/AynThor 1pzal05** (performance-mode PSA): Standard mode is enough for most games; High mainly pins clocks. One commenter saw no FPS gain from High in any game tested. Nothing FNV-specific.
- **ClusterTune** v1.3.0 README
  - PServer on AYN devices, no root. Per-app profiles. Auto Tune is experimental.
  - Thor overlay shows 3.19 GHz when the CPU drops below 1 GHz.
  - Physically validated on an AYN Thor. Thanks to a contributor for the "QCS8550 Small Underclock" values; a "Medium" preset name is not documented in the README.

## Conflicting reports (and how the draft handles them)

1. **Vortex vs MO2.** TBoT (official) is MO2-only. The video uses Vortex so that one real folder can be copied. → The draft follows the video and mentions MO2 workarounds. Expect ModdingLinked support to assume MO2.
2. **`nvse_loader.exe` vs `FalloutNV.exe`.**
   - The video and Jupiter use nvse_loader.exe.
   - The 4GB Patcher page says launch the game normally. The Epic patcher says never use nvse_loader. Pocket Wastelands and _joshd use FalloutNV.exe. The GameNative 4GB patcher restores the exe path to FalloutNV.exe.
   - → The draft keeps nvse_loader.exe for Steam/GOG as tested, requires FalloutNV.exe for Epic, and offers FalloutNV.exe as the fallback for black screens.
   - **Unverified:** what happens when nvse_loader.exe launches an exe that already auto-loads xNVSE.
3. **Memory.** The video uses 4096 device memory and 4096 Wine video memory. Pocket Wastelands warns 2 GB / 2 GB unless the device has 16 GB RAM (its own JSON uses maxDeviceMemory 4096 with videoMemorySize 2048). EmuReady enasty uses 0. → The draft says start at 2048 on Base/Pro.
4. **DXVK version.**
   - Video: "1.11 CAK", which we read as DXVK-Sarek 1.11.
   - PW / _joshd / enasty: 2.6.1-gplasync. impulse9studio: async-1.10.3. paraguybrarian: 2.4.1-gplasync. GameHub: 2.3.1-async. ConnoisseurDeFemboys: 2.7.1-gplasync.
   - No consensus. The video value needs confirming by watching `[20:05]`.
5. **Turnip build.** Video: 26.2 R4. PW: 26.0.0_R8. _joshd: Mr Purple t28-toaster. enasty: KIMCHI 26.0.0 R8. GameHub: 26.1.0_R4 / 25.0.0_R1. NefariusMarius warns that drivers you think are installed may not be; install directly in GameNative.
6. **Env var.** Video: `FD_DEV_FEATURES=enable_tp_ubwc` as transcribed. The documented SD8G2 glitch fix (Azahar #2559, Dolphin #14171, r/EmulationOnAndroid) is `FD_DEV_FEATURES=enable_tp_ubwc_flag_hint=1`. Probably a transcription truncation.
7. **Box64/FEXCore preset.** impulse9studio uses Box64 Performance (plain FNV). PrivateSeaCow fixed TTW save-load crashes by moving Performance → Intermediate. PW uses Box64 Compatibility + FEXCore Intermediate. → The draft recommends Intermediate/Compatibility for TTW.
8. **Proton version.** Most use proton-10.0-arm64ec-2. ConnoisseurDeFemboys uses Proton 11 arm64ec, and Aggressive_crackhead tried Proton 11. PW says Proton 11 is testing-only. paraguybrarian uses Proton-9.0-x86_64.
9. **INI tools.** TBoT: no BethINI, use its FalloutCustom.ini. PW: BethINI Pie. → The draft uses TBoT's INI.
10. **`bMultiThreadAudio`.** TBoT's INI sets it to 1. impulse9studio says 1 helps FPS but may cause crackling.
11. **GameHub vs GameNative for plain FNV.** Several users could not get GameNative to launch FNV and found GameHub easy. Others had the reverse (GameHub Lite failed, GameNative worked). The disagreement seems to depend on version, and GameNative is better documented as of Jul–Oct 2026.
12. **Steam route on the Thor.** The video copies `Steam.exe` into the root (mechanism unexplained). PW: GameNative recognises Steam ownership, and renaming the folder to match Steam's name plus refreshing the container worked. The CyberShonk patcher is for Steam copies installed *through* GameNative. Siredeath31: the normal 4GB patcher said "invalid exe" inside GameNative with Steam. impulse9studio doubts the Steam DRM survives a copy.
13. **TTW installer time.** TBoT: about 40 minutes on a 5600X, possibly hours. Video: about 15–20 minutes.
14. **Copy method.** Jupiter says don't use ADB. Others zip and extract. Pocket Wastelands warns that file timestamps decide load order. None of the sources say whether MTP or ZArchiver preserve modification times.
15. **GameNative version drift.** PW was tested on 1.0.0, the video on 1.1.1, and current stable is 1.3.0. Setting names and locations (e.g. the "Use Legacy DRM" and "Unpack Files" toggles, wrapper names) may have changed.

## Values we could NOT source (left unstated in the draft)
- Recommended Thor Performance Mode / fan mode for FNV/TTW specifically.
- A ClusterTune profile name or clock values for TTW. "Medium" comes from one comment and isn't in the README.
- An FPS cap other than 60.
- A TTW-specific in-game graphics preset on the Thor.
- Box64 environment variables for current builds (only the 2024 issue).
- Any setting for the **Thor Lite** (SD865).
- How to fix the DC-ruins stutter on Android.
- Any detail of GameNative issue-tracker reports (search tool failed).

## Not yet read (would strengthen the guide)
- The full video (to confirm "1.11 CAK", "26.2 R4", "enable_tp_ubwc", and what the Steam.exe copy does).
- The GameNative Discord registry post linked by impulse9studio.
- pastebin aqWwAtPw (burcbuluklu's container config).
- The [TTW Essentials Collection](https://www.nexusmods.com/games/newvegas/collections/c1infa/revisions/2) contents (compare with the TBoT list).
- The Pocket Mojave guide (<https://cybershonk.github.io/PocketMojave/>).
- GameNative issues on GitHub.

## Open questions only the user can answer
1. Which store each game is from (Steam / GOG / Epic), separately for FO3 and FNV. This decides the patcher, the registry/DRM step, and the executable.
2. Which Thor model (Lite / Base / Pro / Max). This sets the memory values and whether the guide is valid at all (no Lite data).
3. Whether the user has a Windows PC for the TTW build. Memory says his desktop dual-boots; Linux-only builds are unsupported by TTW.
4. MO2 or Vortex? Does he already have a TBoT/MO2 TTW install he'd want to reuse?
5. Free internal storage on the Thor, and whether he'd keep the game on microSD (discouraged).
6. GameNative version installed (stable 1.3.0 vs the 1.4.0 prerelease).
7. Which video values he actually saw at `[20:05]`–`[21:08]` (DXVK name, Turnip build, env-var value).
8. How much the companion guide should assume: TBoT essentials only, or the video's Essentials Collection.
