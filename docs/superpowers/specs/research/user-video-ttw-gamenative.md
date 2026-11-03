# User-supplied notes: "Fallout Tale of Two Wastelands - Ayn Thor - GameNative - Vortex (2.0)"

Source video: https://www.youtube.com/watch?v=JiqVAiGlCtU
Provided by the user on 2026-11-03 as a transcribed summary. Treat as a firsthand community walkthrough
for TTW on an AYN Thor (Max/Pro) under GameNative; timestamps link into the video.

## Overview
- Build TTW on a Windows PC with **Vortex** (not MO2; Vortex deploys into the real Data folder, so one
  self-contained game folder can be copied to the handheld) [00:57], package root + config files [01:03],
  copy to the Thor and run inside a **GameNative** container [01:09].

## Phase 1: PC prep
1. Clean Fallout 3 GOTY + Fallout: New Vegas Ultimate (all DLC), from GOG, Steam, or Epic [03:16].
2. Install both outside `Program Files` [03:42].
3. Run both games once on the PC to create the registry entries and .ini files [04:18].

## Phase 2: TTW via Vortex on PC
1. Fresh Vortex profile for FNV [05:06].
2. Vortex → Open → Open Mod Staging Folder; create a folder named `Tale of Two Wastelands` and copy its path [05:33][05:56].
3. Official TTW Installer (per *The Best of Times* guide) [00:39]: right-click → Run as Administrator [02:50].
4. Point the installer's destination at that staging folder; ~15–20 min, ~17 GB output [06:16].
5. Restart Vortex, accept the external-changes prompt, deploy, enable all TTW plugins [06:43][06:56].

## Phase 3: Root mods + INI
1. xNVSE extracted into the FNV root (beside FalloutNV.exe / nvse_loader.exe) [08:48].
2. 4GB Patcher in the root, run once [09:23].
3. Remaining essentials from *The Best of Times* guide, or Timeless Jupiter's "TTW Essentials" collection, via Vortex [09:49].
4. Paste the *Best of Times* custom INI into `Documents\My Games\FalloutNV\FalloutCustom.ini` [11:04][11:26].
5. Verify on PC: launch via nvse_loader.exe and reach the TTW title screen [12:02].

## Phase 4: Stage for transfer (inside the FNV root on the PC) [13:13]
1. Redistributables folder: Visual C++ Redistributable All-in-One [01:38] + DirectX June 2010 offline installer (TechPowerUp) [13:22][14:49].
2. DRM/registry files:
   - GOG: GOG `beth_registry_command` file [13:47] + "Fallout New Vegas Registry Fix" .bat [18:02], both in the root [18:10].
   - Steam: copy `Steam.exe` into the root [14:33].
3. `config` folder ← everything in `C:\Users\<User>\Documents\My Games\FalloutNV` (.ini files) [15:01][15:13].
4. `appdata` folder ← everything in `C:\Users\<User>\AppData\Local\FalloutNV` (plugins.txt / load order) [15:27][15:33].
5. Copy the whole packaged FNV root folder to the handheld's storage [16:20].

## Phase 5: GameNative container on the Thor
1. GameNative → `+` → pick the transferred FNV folder [18:46].
2. Edit container:
   - Executable: `nvse_loader.exe` [19:55]
   - Graphics wrapper: `Wrapper-leegao` (or legacy wrapper) [20:05]
   - Graphics driver: `Turnip 26.2 R4`, DXVK/VKD3D `1.11 CAK` [20:05]
   - Device memory and Wine video memory: `4096` MB [20:14]
   - Present mode: all `Mailbox` [20:22]
   - Emulation: FEXCore `2512`, 32-bit emulation = `FEXCore` [20:22]
   - Env vars: add `FD_DEV_FEATURES` with `enable_tp_ubwc` [21:08]
3. Save, press Play once. The game won't start yet; this initializes the Wine container, installs Mono and builds `C:\` [21:36][21:40].

## Phase 6: Inside the container
1. Open the container desktop/file explorer; the game folder is mapped to `A:\` [22:06][22:10].
2. Run the VC++ AIO .bat [22:21] and the DirectX June 2010 installer [23:31].
3. Copy `A:\appdata\*` → `C:\users\xuser\AppData\Local\FalloutNV` [24:44]; `A:\config\*` → `C:\users\xuser\Documents\My Games\FalloutNV` [25:02].
4. Run `registry.bat` from `A:\` [25:59], then `beth_registry_command` and type `yes` [26:06].
5. Verify: temporarily set the executable to `FalloutNVLauncher.exe`, Play, confirm it shows **Play** not **Install**, close it without playing [27:04][27:12].
6. Set the executable back to `nvse_loader.exe`, save, Play → TTW boots [27:33].
7. Later mod updates: install via Vortex on PC, copy the updated game folder + `AppData\Local\FalloutNV` plugin list over, overwrite in the existing container [30:30].

## Notes for the companion (my inferences, not from the video)
- The container's Wine user is `xuser`, so My Games is at `C:\users\xuser\Documents\My Games\FalloutNV`.
  Our plugin's INI/log should live in `Data\NVSE\Plugins\` so it travels with the copied game folder.
- 32-bit x86 emulation in this setup is FEXCore (not Box64). Our plugin must be tested under FEXCore WoW64.
- Mod installs happen on the PC with Vortex, so our plugin DLL can ship as a Vortex-installable archive
  (`Data\NVSE\Plugins\ttw_dualscreen.dll` + .ini) as well as a manual drop-in.
