# TTW DualScreen: "dualscreen launcher" integration research

Researched 2026-11-03. Sources are listed inline. "Verified" means I read the source code, the decoded APK manifest, or the dex strings myself. "Inferred" means my own reasoning from that evidence.

---

## 1. What "dualscreen launcher" most likely means

### Ranking

| # | Candidate | Likelihood | Why |
|---|---|---|---|
| 1 | **DualScreen Launcher** by ProfMags (GitHub `JoeCorrell`), package `com.dualscreen.launcher` | **~65%** | The name matches exactly. It has its own GAFT catalog entry titled "DualScreen Launcher". It is the only "launcher" that ships PC-game dual-screen mods and **lists Fallout 3 as "Available"**. Asked to add New Vegas, the dev replied "Yes I've already downloaded it". A Reddit search for "dual screen launcher" in r/AynThor returns its threads first. |
| 2 | **Cocoon** (`rip.moth.cocoonshell`, by "Moth") | ~15% | The most popular Thor frontend. The press calls it "a dual-screen launcher by Moth". It has a **per-game "companion app" setting** for the bottom screen, which is what WalkThor integrates with. |
| 3 | **Mjolnir** (+ ES-DE + ES-DE Companion) | ~7% | Reddit titles: "Mjolnir: Full Setup & Demo (Custom Dual-Screen Android Launcher)" and "AYN Thor + ES-DE + ES-DE Companion + Mjolnir = Nearly seamless dual screen launcher!". It is a Home-button router that starts a chosen top app and a chosen bottom app. |
| 4 | AYN stock: the launcher's long-press **Screen Launch (Auto/Top/Bottom)**, the stock DualScreenAssistant, or Better Dual Screen Assistant | ~5% | Generic wording. BDSA is an assistant overlay, not a launcher. |
| 5 | Other dual-screen frontends: Loki (also by ProfMags), Járngreipr, iiSU, Console Launcher, RetroHrai, NeoStation, ZNeko, Argosy, Wave, Konsoru, LaunchBox, plus tiny repos literally named "dual screen launcher" (`dakotadawolfe/thor-dual-screen-launcher` 0★, `visorcraft/GhostGalleon` 4★, `a114126541/Display-launcher` 0★) | ~8% combined | Any of these could be meant generically. |

### Evidence

**DualScreen Launcher (DSL)**
- GAFT entry `content/dualscreen-launcher/index.md`: "An Android game launcher that adds dual-screen mods and configs for supported PC and Switch games". Access needs Patreon (`patreon.com/c/ProfMags`) or a dev-issued access code. https://github.com/andreyvelsk/GAFT/blob/main/content/dualscreen-launcher/index.md
- Repo https://github.com/JoeCorrell/DualScreen-Launcher: "Official DualScreen Launcher APK downloads only. Source is maintained privately." The README lists 13 PC games: **Fallout 3 Available**, Skyrim SE Available, Deus Ex HR DC Available, Stardew Available, Fallout 4 Beta, and others. There are also 7 Switch games, run through embedded Eden-DS.
- Releases v1.6.10 to **v1.6.22, the last one on 2026-09-10**. The APK is about 313 MB, package `com.dualscreen.launcher`, versionCode 75.
- r/AynThor "Dualscreen Launcher Now Supports Both PC and Switch Games in One App" (u/ProfMags, about 2 months old). A user asked "Could you add Fallout New Vegas in the future?" and ProfMags replied "Yes I've already downloaded it". Another user asked how to add DSL games as Cocoon shortcuts and got no answer. https://www.reddit.com/r/AynThor/comments/1w448sf/
- r/AynThor "I'm Building a Launcher for My Dual Screen Mods" (ProfMags): "drop their game files in as a .zip … hit Play … no container setup … will not include Steam, Epic Games, or GOG integration". A user requested "some kind of am start launch syntax so that games inside your app can be launched from other launchers"; it was never added (see the manifest below). https://www.reddit.com/r/AynThor/comments/1w2097l/
- r/AynThor "Do NOT Pay for Dualscreen Launcher!" (15 days ago, top post with many upvoted comments) says the mods are "seemingly abandoned", there is a Patreon paywall, "he also never updated his app. most pc games only work with a very specific version of the games." https://www.reddit.com/r/AynThor/comments/1x9jc3j/
- The "Mostly Comprehensive List of Launchers for Dual-Screen Handhelds" thread (u/torspoor, 14 days ago) lists frontends only and **does not include DSL**: ES-DE, Cocoon, iiSU, Beacon (no dual screen), Daijishō (no), RetroHrai!, Console Launcher, Socket, NeoStation, Jump, ZNeko, Argosy, LaunchBox, Cannoli (no), Wave, Konsoru, DigiBitPlay. Add-ons: Járngreipr, Mjolnir, ES-DE Companion. https://www.reddit.com/r/AynThor/comments/1xad9oh/

**Cocoon**
- The APK is `rip.moth.cocoonshell` (Cocoon 3.11, released 2026-10-28), which confirms "Moth" is Cocoon's developer.
- Its Room DB `games` table has the columns `companionAppPackage TEXT` and `launchOnExternalDisplay INTEGER`. Its dex strings include "Failed to launch companion app", "companion_close", "focusCompanion", "COMPANION_REGISTER: gameId=", and "Companion follow skipped; existing tasks preserved" (verified from the dex strings of https://github.com/inssekt/CocoonFE/releases/download/beta-3.11/cocon-311.apk).
- WalkThor README: "In Cocoon, choose WalkThor as the companion app for the bottom screen (for every game, or just the ones you want). Cocoon then opens WalkThor whenever it starts a game." https://github.com/nerf-herder-29/WalkThor

---

## 2. Integration details for the top candidates

### 2.1 DualScreen Launcher (ProfMags), closed source, inspected from the APK

**Manifest** (decoded from `AndroidManifest.xml` in DualScreenLauncher-1.6.22-release.apk with range requests and my own AXML decoder):
```
package="com.dualscreen.launcher" versionName="1.6.22" minSdk=26 targetSdk=28
<activity com.dualscreen.launcher.session.GameSessionActivity exported="false" launchMode=singleTask landscape>
<activity com.dualscreen.launcher.session.CompanionDisplayCoverActivity exported="false"
          taskAffinity="com.dualscreen.launcher.companion.cover" excludeFromRecents="true">
<activity com.dualscreen.launcher.LauncherActivity exported="true">
   MAIN/LAUNCHER;  VIEW dualscreen://auth/callback   (Patreon OAuth only)
<meta-data android.allow_multiple_resumed_activities = true>
<service com.wemu.engine.services.NotificationService  FGS specialUse
         "Keeps a running Windows container alive while its window is not in the foreground.">
<provider com.wemu.engine.core.WemuFilesProvider exported="true"
          permission="android.permission.MANAGE_DOCUMENTS"
          authorities="com.dualscreen.launcher.core.WemuFilesProvider"
          <intent-filter> android.content.action.DOCUMENTS_PROVIDER
<activity org.yuzu.yuzu_emu.activities.EmulationActivity exported="false">   (embedded Eden)
```
What this means for us:
- **No exported game-launch entry point.** GameSessionActivity is not exported and there are no intent extras or deep links for games. Other frontends cannot launch a DSL game, and we cannot either.
- **No third-party companion mechanism for PC games.** `ExternalCompanionPackages` exists, but it lives under `org.yuzu.yuzu_emu.companion` and loads downloaded Switch companion modules through a ClassLoader. It is not for Android apps.
- **DSL runs its own embedded Windows runtime.** `lib/arm64-v8a/libwemu.so` comes from ProfMags' WEMU, which is Winlator-derived: Box64 0.4.2, a Proton-9-derived container, Turnip. It does **not** use GameNative, GameHub or Winlator. Container files are reachable through the system Files app via `WemuFilesProvider`. That is the probable route for a user to drop our xNVSE plugin into a DSL container, but this is **untested**.
- **DSL owns the bottom screen during a session.** Dex strings include `CompanionDisplayCoverActivity`, "Opaque companion cover attached display=", `android.app.Presentation`, `setLaunchDisplayId`, "THOR-TOP", "THOR-BOTTOM", "Start with the activity console on the lower screen", "Second screen showing a guest window at", and "Container asked for the second screen, but this device reports none".

**How DSL's own Fallout 3 support works** (from the public payload feed https://github.com/JoeCorrell/DualScreen-Launcher-Payloads, release `launcher-payloads-v1`, `payload-bootstrap.json` → `game-mods-2026.09.09.1.zip`, read with HTTP range requests):
```
mods/fallout3/manifest.json   version "launcher-2026.09.01-fo3-map-radio-status-2"
  dinput8.dll  (5.3 MB proxy DLL)     -> game dir
  Fallout3SecondScreen.ini            -> game dir
  dxvk.conf                           -> game dir
```
`Fallout3SecondScreen.ini`:
```ini
[panel]
; Used only when Wine does not enumerate a non-primary monitor.
x=0
y=720
width=1240
height=1080
[input]
; WEMU's ordered bottom-display touch channel. The native listener binds only
; to loopback and never forwards a handled touch into Fallout gameplay.
touch_port=7788
```
`dxvk.conf`: `[Fallout3.exe] dxvk.enableGraphicsPipelineLibrary = False` (GPL faulted on Turnip).

Dex strings also show the expected game build `Fallout 3 1.7.0.4`, path `C:\Games\fallout3`, a registry value `Software\Wow6432Node\Bethesda Softworks\Fallout3`, and "Provisioned Fallout 3 executable-scoped dinput8 override". The strings mention `FalloutNV/Fallout.ini` and `FalloutPrefs.ini`, but there is **no FNV/TTW catalog entry**.

So in DSL the "companion" is **not an Android app**. A proxy DLL injected into the game draws a Win32 window on a second Wine monitor (`THOR-BOTTOM`), which WEMU maps onto the Thor's bottom panel through a `Presentation`. Touches come back over `127.0.0.1:7788`.

**Related open-source piece**, https://github.com/JoeCorrell/AYN-Thor-Dualscreen-Mods: an Android companion app (`com.wemu.dualscreen.DualScreenActivity`) plus a SMAPI mod. The game-side mod serves `ws://127.0.0.1:7786` with plain JSON, one object per frame, and broadcasts a UDP discovery beacon on port 7787 every 3 s, but only when `AllowRemote=true`. Note from its README: *"The handshake and framing are written out by hand rather than taken from .NET, because the framework's WebSocket server is a wrapper over Windows components that Wine does not implement."* **For our xNVSE plugin: use raw Winsock, not http.sys / HttpListener / WinHTTP server.** Its network config allows cleartext only to 127.0.0.1 and localhost:
```xml
<domain-config cleartextTrafficPermitted="true">
  <domain includeSubdomains="false">127.0.0.1</domain>
  <domain includeSubdomains="false">localhost</domain>
</domain-config>
```

**Integration requirements if the user means DSL:**
1. TTW cannot be launched from DSL today (catalog-only, no FNV entry), and DSL cannot be scripted by intent.
2. Our app can still **talk to an xNVSE plugin running inside DSL's or WEMU's container over loopback**. Wine sockets are host sockets. Strip-Boy's GAMENATIVE.md confirms that `0.0.0.0:27000` in Wine is reachable at `127.0.0.1:27000` from Android. This should hold for WEMU too because it is Winlator-derived (inferred).
3. Do not take ports **7786, 7787 or 7788** (ProfMags). Also avoid 8765 (SkyrimWebSocket) and 27000/28000 (FO4 Pip-Boy).
4. Expect DSL's cover/console/Presentation to occupy the bottom display. A Presentation window is a higher window layer than a normal activity, so our activity could end up underneath it (inferred, untested). Whether the two can coexist has to be tested on a device.
5. Real first-class support needs one of these: (a) ProfMags adds an FNV/TTW entry that ships our plugin and leaves the bottom screen to our app; (b) we add a second "render mode" in the plugin that draws natively to a Wine second monitor, as his mods do; (c) the user runs TTW through GameNative, GameHub or Winlator instead. DSL appears unmaintained (no release since 2026-09-10) and the community is hostile to it, so (a) is unlikely.

WEMU standalone (https://github.com/JoeCorrell/wemu-release, `com.wemu` v0.3.0, debug-signed, targetSdk 28) has the same shape: only `WemuActivity` (MAIN/LAUNCHER) is exported; `SessionActivity` is not. It has the same exported `WemuFilesProvider` (`com.wemu.core.WemuFilesProvider`), and "can expose the bottom screen to the Windows container as an actual second monitor."

### 2.2 Cocoon (`rip.moth.cocoonshell`), closed-source app with an open platform config at https://github.com/inssekt/CocoonFE

**Pairing a companion with a game:** each game row stores `companionAppPackage` (a package name) and `launchOnExternalDisplay`. The user picks the companion in Cocoon, either per game or for all games. When the game starts, Cocoon launches that package on the other display. **No game info is passed in extras**: I found no companion-extra strings, only a `getLaunchIntentForPackage` reference. So the companion has to work out the game itself. WalkThor tails Cocoon's `launch_debug.log` in the Cocoon data folder, which the user links via SAF `OpenDocumentTree`:
```
[2026-10-14 08:59:19.145] [----] ══════ Launch: Legend of Zelda, The - The Wind Waker ══════
[2026-10-14 08:59:19.146] [INFO] Game: Legend of Zelda, The - The Wind Waker | File: …(USA).rvz
[2026-10-14 08:59:19.148] [INFO] Player: Dolphin Emulator (org.dolphinemu.dolphinemu/…)
```
WalkThor's regexes (data/CocoonLaunchLog.kt):
```kotlin
private val HEADER = Regex("""^\[([0-9-]+ [0-9:.]+)\] \[[^\]]*\] ═+ (Launch|Swap): (.+?) ═+\s*$""")
private val GAME   = Regex("""\] Game: (.+?) \| File: (.+)$""")
private val PLAYER = Regex("""\] Player: (.+?) \(""")
```
WalkThor notes that "Cocoon may start WalkThor (as a companion app) a moment before it finishes writing the launch to its log", so it keeps polling for a few seconds. Our app doesn't need the log to identify the game, because the xNVSE plugin's WebSocket tells us. The log is only useful for showing a "TTW launching…" state before the plugin's socket comes up.

Cocoon's manifest also declares `ExternalDisplayActivity` with category `android.intent.category.SECONDARY_HOME`, so Cocoon can be the secondary-display home.

**How Cocoon launches Windows games** (`android/windows.yaml`; ES-DE uses the same intents, see §2.4):
```yaml
- name: GameNative
  app: app.gamenative/.MainActivity
  action: app.gamenative.LAUNCH_GAME
  files: [steam, steamappid, pcgame, localappid, localgameid, gog, epic, amazon]
  extras: { app_id: { int: "{tags.app_id}" }, game_source: "{tags.game_source}" }
- name: GameHub Lite (Steam)
  app: gamehub.lite/com.xj.landscape.launcher.ui.gamedetail.GameDetailActivity
  action: gamehub.lite.LAUNCH_GAME
  extras: { steamAppId: "{tags.app_id}", autoStartGame: { bool: true } }
- name: GameHub Lite (Local Games)   # same activity/action, extras: localGameId, autoStartGame=true
- name: Winlator CMod
  app: com.winlator.cmod/.XServerDisplayActivity
  game: { extra: shortcut_path }      # path to the Winlator .desktop shortcut
  files: [desktop]
  flags: [clear-task, clear-top]
- name: BannerHub
  app: banner.hub/com.xiaoji.egggame.DeepLinkActivity
  action: banner.hub.LAUNCH_GAME
  extras: { localGameId: "{tags.localgameid}", autoStartGame: { bool: true } }
```
Official GameHub (`com.xiaoji.egggame`) has **no documented launch intent** in Cocoon or ES-DE. Only the Lite forks and BannerHub do.

**GameNative's own parser** (https://github.com/utkarshdalal/GameNative app/src/main/java/app/gamenative/utils/IntentLaunchManager.kt):
- action `app.gamenative.LAUNCH_GAME` with extras `app_id` (Int), `game_source` (STEAM/GOG/EPIC/AMAZON/CUSTOM_GAME; default STEAM), `exe` (String; matched against `container.executablePath`), `exec_args`, and `container_config` (JSON, at most 50 KB, applied as a temporary override);
- or the URI form `gamenative://run?appid=…&gamesource=…&exe=…&exec_args=…`.

FNV's Steam AppID is 22380. Whether TTW should launch through `exe=nvse_loader.exe` or a CUSTOM_GAME container still needs checking.

**Cocoon integration requirements:**
1. Have a normal `MAIN`/`LAUNCHER` activity so Cocoon lists the app as a companion candidate.
2. When Cocoon opens us on the bottom display, **stay put**. Don't bounce to another display, and use a cooldown like WalkThor's (§3.1).
3. Start without stealing focus. Cocoon launches the companion right next to the game (§3.3).
4. Survive Cocoon closing us: it has a "companion_close" path that finishes the companion task when the game ends.
5. Optionally read `launch_debug.log` through a user-granted SAF tree, as an early "TTW is starting" signal.

### 2.3 Mjolnir (https://github.com/blacksheepmvp/mjolnir, `xyz.blacksheep.mjolnir`)
- The user picks a "top app" and a "bottom app". Candidate apps come from `queryIntentActivities` over `CATEGORY_LAUNCHER` plus `CATEGORY_HOME` (utils/AppQueryHelper.kt), so any app with a launcher activity qualifies.
- `utils/DualScreenLauncher.kt` uses `dm.displays[0]` as top and `dm.displays[1]` as bottom with `setLaunchDisplayId`. **Launch order decides focus: whichever app launches last gets focus.** With the top screen as main, it launches the bottom app first and the top app second. The same rule applies if our app launches TTW itself (§4).
- Requirement: be a normal launcher app that is happy being started on display 1/4 at any time, including from Home presses mid-game.

### 2.4 Others in brief
- **AYN stock launcher:** long-press an icon → **Screen Launch** → Auto / Top Screen / Bottom Screen (https://retrohandhelds.gg/ayn-thor-setup-guide/). Our app should self-correct to the bottom screen anyway (§3.1).
- **ES-DE (Android):** the windows system uses the same intents, e.g. `%EMULATOR_GAMENATIVE% %ACTION%=app.gamenative.LAUNCH_GAME %EXTRA_game_source%=STEAM %EXTRAINTEGER_app_id%=%INJECT%=%ROM%` and `%EMULATOR_WINLATOR-CMOD% … %EXTRA_shortcut_path%=%ROM%` (https://gitlab.com/es-de/emulationstation-de/-/raw/master/resources/systems/android/es_systems.xml). Companions learn about launches in one of two ways:
  - **Custom Event Scripts:** ES-DE runs `ES-DE/scripts/game-start/*.sh` with arguments `"<rom path>" "<game name>" "<system>" "<system full name>"`. Járngreipr's script writes these to files that it watches with `FileObserver`.
  - **The log:** with Debug Mode on, `ES-DE/logs/es_log.txt` gets lines like `Scripting::fireEvent(): game-start "<path>" "<name>" "<sys>" "<sys full>"` (WalkThor EsdeLog.kt).
- **Loki** (ProfMags' open-source launcher, https://github.com/JoeCorrell/Loki, `com.thor.launcher`): has a per-entry "Always open on…" screen and registers a home activity on both displays (`SecondaryHomeActivity`).
- **Járngreipr:** per-app launch display `Primary`/`External`/`Ask`, plus ES-DE mode.
- **Better Dual Screen Assistant** (https://github.com/stormpanda/better-dual-screen-assistant): replaces `com.odin.dualscreen.assistant`. Not a launcher, but its docs are the best reference on Thor system internals (§3.6).

---

## 3. Dual-screen best practices (Thor and similar devices)

### 3.1 Which display is which
- **Thor:** the top screen is `Display.DEFAULT_DISPLAY` (id 0, 1920×1080). The bottom screen is **display id 4** on stock firmware (BDSA docs/SYSTEM_INTEGRATION_REFERENCE.md: `sub_screen_brightness` "Bottom, displayId = 4"; `sys.temp.disabled.touch` "0,1" disables display 4). The bottom display carries `FLAG_PRESENTATION` and is listed under `DISPLAY_CATEGORY_PRESENTATION` (OpenMW-DS DisplayRoles.kt).
- **Retroid Dual Screen add-on:** the roles are **reversed** relative to the Thor. OpenMW-DS has a `PROFILE_RETROID` that swaps game and companion.
- **AYANEO Pocket DS:** "its MAIN display is the bottom panel", and the presets found no presentation display there (OpenMW-DS `PROFILE_CUSTOM` comment).
- **Anbernic RG DS:** two equal-size panels, so size heuristics fail there.
- **AYANEO Flip DS** is a Windows x86 device, not Android, so it is out of scope for the Android app.
- **Display ids are not stable across boots.** OpenMW-DS matches displays by `"<id>|<name>|<w>x<h>"`, name and size first, id only as a tiebreaker. Always filter out `FLAG_PRIVATE` displays (screen recorders' virtual displays) and `STATE_OFF` displays. The Thor can turn either panel off: `Settings.System dual_screen_display_mode` 0 = dual, 1 = top only, 2 = bottom only.
- **Recommended approach:** a heuristic default, then device profiles, then a manual "Identify screens" override.

WalkThor's heuristic, which works on the Thor (https://github.com/nerf-herder-29/WalkThor DisplayHelper.kt):
```kotlin
private fun usableDisplays(c: Context) = c.getSystemService(DisplayManager::class.java).displays
    .filter { it.state != Display.STATE_OFF && (it.flags and Display.FLAG_PRIVATE) == 0 }
fun bottomDisplay(c: Context): Display? = usableDisplays(c).takeIf { it.size >= 2 }
    ?.minByOrNull { it.mode.physicalWidth.toLong() * it.mode.physicalHeight }
```
OpenMW-DS presets use `dm.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION).firstOrNull()` as the companion display, plus a **Custom** profile with an **Identify screens** button (https://github.com/Josh-Daniels/OpenMW-DS app/src/main/java/org/openmw/utils/DisplayRoles.kt).

**Self-relocation:**
- Relaunch on the bottom display with `ActivityOptions.makeBasic().setLaunchDisplayId(id)`, flags `NEW_TASK | MULTIPLE_TASK`, then `finishAndRemoveTask()` and `overridePendingTransition(0,0)`.
- Set an extra so we never bounce twice.
- Apply a **10 s cooldown**. If something put us back on the top screen right after we moved (Wayfinder, a frontend), respect it: "ping-ponging between screens disrupts the game launching alongside" (WalkThor).
- Megingiard adds a `WrongScreenOverlay` with a "Retry detection" button when it ends up on display 0.

### 3.2 Don't use `Presentation` for a separate companion app
- A `Presentation` must be owned by an Activity or a window context in our process. It only works on displays with `FLAG_PRESENTATION`, and **never on the default display**: "Attempted to add presentation window to a non-suitable display. Aborting." (OpenMW-DS CompanionActivity.kt KDoc).
- It is the right tool when **one process owns both screens**, as in OpenMW-DS, DSL/WEMU and Eden-DS. Our app is a separate process from the Wine game.
- Use one of these instead:
  - **(a) a normal Activity on the bottom display**, as WalkThor, Megingiard and SkyrimWebMonitor do;
  - **(b) a `TYPE_APPLICATION_OVERLAY` window from a foreground service.** Get the display with `createDisplayContext(display)` and the WindowManager from that context, and set `FLAG_NOT_FOCUSABLE`. This is how BDSA's ScreenOverlayManager draws on display 4. It needs `SYSTEM_ALERT_WINDOW`, but it never competes for activity focus or top-app status, and holding that permission also exempts us from background-activity-start limits.

### 3.3 Keep the game at top-app priority and keep the controller on the game
The key facts are spelled out in OpenMW-DS CompanionActivity.kt (confirmed on device):
- "Per-display focus governs whether two displays can hold focus at the SAME TIME. It does not give you two key-input destinations: `InputDispatcher` keeps a single focused *display*, and a controller is not associated with any display, so its key events go to the focused window of whichever display is focused. Touch routes per-display."
- "Android gives its full performance (top-app CPU priority and boost) only to the focused app. If WalkThor takes focus … the game on the top screen loses it, which can halve its frame rate" (WalkThor GameFocus.kt).

Techniques seen in the wild:
1. **WalkThor: non-focusable window plus an accessibility "hand-back tap".**
   - While a game runs, call `window.addFlags(FLAG_NOT_FOCUSABLE)`. Clear it only while a text field needs the IME, then `moveTaskToFront`.
   - Right after opening (within a short window, never mid-play), the accessibility service taps the game display once at the top-centre edge. Android then moves focus back:
     ```kotlin
     val path = Path().apply { moveTo(size.x / 2f, 2f) }
     val g = GestureDescription.Builder()
         .addStroke(GestureDescription.StrokeDescription(path, 0, 40))
         .setDisplayId(gameDisplayId).build()     // API 30+
     dispatchGesture(g, null, null)
     ```
   - Before tapping, it checks that a game, not a frontend, is in front of the game display, so the tap never presses frontend UI.
   - It tracks `onTopResumedActivityChanged` to know whether we hold top-app status.
   - Service config `canPerformGestures="true"`, `canRequestFilterKeyEvents="true"`. A Play-Store-style disclosure is required.
2. **OpenMW-DS: focusable while coming up, non-focusable once focused.**
   - Setting `FLAG_NOT_FOCUSABLE` in `onCreate` **caused an ANR**: "Application does not have a focused window". The activity is the only window on its display, so the dispatcher waits 5 s.
   - The fix: once the window has gained focus, add a 1×1 `TYPE_APPLICATION_PANEL` focus anchor (`FLAG_NOT_TOUCHABLE | FLAG_NOT_TOUCH_MODAL`, with the activity's token), then add `FLAG_NOT_FOCUSABLE` to the main window. Keep `FLAG_NOT_TOUCH_MODAL` throughout.
3. **Megingiard: `FLAG_NOT_FOCUSABLE` plus `PrimaryFocusAnchorActivity`.** A translucent activity is launched on `DEFAULT_DISPLAY` and finishes immediately, debounced to 250 ms. It is re-fired on `onTopResumedActivityChanged(true)`, `onNewIntent`, and gamepad `ACTION_DOWN`. **⚠ Do not copy this for Wine games.** A translucent activity on top **pauses** the activity beneath it (Android docs, multi-resume section). GameNative's `MainActivity.onPause()` calls `xEnvironment.onPause()`, which **suspends the Wine processes**, unless the container's suspend policy is `never`. The default `SUSPEND_POLICY_MANUAL` also sets `isOverlayPaused = true` and needs a manual resume (GameNative MainActivity.kt, Container.java). This is my inference from the code; I have not tested it.
4. **The AYN system setting.** `Settings.System screen_focus_lock`: 0 = touch-switchable, **1 = lock focus to the upper screen**, 2 = lower. It is exposed as "Focus Lock" in the AYN drawer and BDSA. Third-party apps can't write it, but we can tell users to turn it on. It is the zero-code option.
5. **Launch order.** When starting both apps, start the companion **first** and the game **last**; the last launch gets focus (Mjolnir DualScreenLauncher.kt).

Multi-resume: on Android 10+ every visible top activity on each display stays RESUMED, so the game is **not** paused just because the bottom screen has focus. It is paused by a translucent or covering activity **on its own display** (https://developer.android.com/develop/ui/views/layout/support-multi-window-mode). So we must **never put any activity on the game's display while TTW runs**. That includes Megingiard-style trampolines and permission dialogs; Megingiard opens its config screens there, which we should avoid.

### 3.4 Surviving being killed when a heavy game loads
- **WalkThor Revive.kt:**
  - Android restarts a killed accessibility service, and the service's `onServiceStart` reads `ActivityManager.getHistoricalProcessExitReasons(pkg, 0, 5)`.
  - If the reason was `REASON_LOW_MEMORY` or `REASON_CRASH*` (not `REASON_ANR`, not user-initiated), and we were visible during a game within the last 30 s, it waits.
  - Conditions before reopening: at least 10 s, and 4 consecutive checks where `!lowMemory && availMem > threshold + 768 MB`. It gives up after 3 minutes and allows at most 3 reopens per 10 minutes.
  - It then reopens on the bottom display from the service, which is allowed in the background.
  - It records a heartbeat every few seconds while visible, and clears it when the user closes the app on purpose.
- **Keep the footprint small:** release caches in `onTrimMemory(>= TRIM_MEMORY_RUNNING_MODERATE)` and cap image caches (WalkThor uses 8% of heap for Coil).
- **Foreground service:** BDSA uses `foregroundServiceType="specialUse"` with `PROPERTY_SPECIAL_USE_FGS_SUBTYPE`, `stopWithTask="false"`, and `excludeFromRecents="true"` so "clear all" in Recents doesn't kill it. Cocoon and DSL also run `specialUse` foreground services.
- **Reconnect forever.** SkyrimWebMonitor gives up after `MAX_RECONNECT_ATTEMPTS: 10` × 3 s, which is far too short for a TTW load. Use backoff capped at a few seconds, plus a heartbeat (it pings every 2.5 s) to detect a dead socket.
- **GameNative-specific** (Strip-Boy docs/GAMENATIVE.md): "Drops every ~10s → GameNative paused the Wine prefix while in background → Disable battery optimisation for GameNative". Tell users to exempt GameNative from battery optimisation and to set the TTW container's suspend policy to **never**.

### 3.5 Networking
- Loopback between Wine and Android needs no Wi-Fi. Strip-Boy adds a loopback UDP send because broadcast needs a non-loopback `IFF_BROADCAST` interface. The stock FO4 Pip-Boy app needs Wi-Fi for exactly that reason.
- For cleartext `ws://127.0.0.1`, set a `network_security_config` like the one in §2.1. SkyrimWebMonitor, a Capacitor WebView app, uses `cleartext: true` and `androidScheme: 'http'`.
- **SkyrimWebMonitor's Android shell** (https://github.com/andreyvelsk/SkyrimWebMonitor):
  - It is a plain `BridgeActivity` with `FLAG_KEEP_SCREEN_ON`, edge-to-edge, cutout `ALWAYS`, and system bars hidden.
  - It has **no display targeting and no focus handling**; it relies on the user placing it on the bottom screen.
  - Its server, the SKSE plugin, listens on `ws://127.0.0.1:8765`.

### 3.6 Thor system hooks worth knowing (BDSA docs/SYSTEM_INTEGRATION_REFERENCE.md)
- The AYN button broadcasts **`action.tcc.button.key.event`** with extras `key_action_down: Boolean` and `key_isLongPress: Boolean`. Usable as a "toggle companion" hotkey, though the stock assistant also listens to it.
- Per-screen settings: `sub_screen_brightness` (bottom) and `secondary_screen_volume_level` (bottom-screen volume).
- Touch control: `sys.temp.disabled.touch` = "0,1" disables bottom touch.
- The vendor root helper is the Binder service `PServerBinder`: transaction 0 with `writeStringArray(arrayOf(cmd, "0"))`. It is only reachable while "Force SELinux" is off, which is the stock setting (WalkThor AynSystem.kt).

---

## 4. Auto-starting the companion when TTW launches (options, best first)

1. **Our app is the launcher.**
   - A "Play TTW" button on the bottom screen fires the GameNative, GameHub Lite or Winlator intent from §2.2 with `setLaunchDisplayId(<top>)`. Launch it **last** so the game gets focus.
   - Our activity stays on the bottom screen and goes non-focusable.
   - This needs no accessibility service and gives the most dependable one-tap flow.
2. **A frontend's companion setting.** In Cocoon, set our package as the TTW entry's `companionAppPackage`. For Mjolnir, make our app its bottom app.
3. **An accessibility service that watches the foreground app.**
   - Use `TYPE_WINDOW_STATE_CHANGED` and map the package to "Windows wrapper": `app.gamenative`, `com.winlator.cmod`, `com.winlator`, `gamehub.lite`, `com.xiaoji.egggame`, `banner.hub`, `com.dualscreen.launcher`, `com.wemu`.
   - When one comes to the front on the game display, open on the bottom display. The service context can start activities from the background.
   - Same pattern as WalkThor EmulatorWatch.kt, whose Windows list is `"winlator", "gamehub", "mobox", "gamenative", "pluvia", "box64", "exagear", "termux"`.
4. **A foreground service that probes the plugin.** Poll `ws://127.0.0.1:<port>` and open the UI when the TTW plugin answers. Opening from the background requires `SYSTEM_ALERT_WINDOW` or the accessibility service, because of background-activity-start limits.
5. **ES-DE game-start script** plus `FileObserver`. Same background-start caveat.
6. **Speculative and untested:** have the plugin ask the wrapper to run `am start --display 4 -n <pkg>/<activity>` from inside the container. I haven't verified that `/system/bin/am` is reachable from GameNative's imagefs, so treat this as an idea to test, not a plan.

---

## 5. Thor stick LEDs: Strip-Boy path confirmed, plus other users

**Strip-Boy** (https://github.com/KuriGohan-Kamehameha/Strip-Boy, `patcher/smali/io/pipboy/thor/LEDBridge.smali`). The header comment says:
```
# Wire format: "1-R:G:B:A\n" to both sn3112{l,r} (the kernel driver
# accepts only prefix '1-'; '2-/3-/4-' are silent no-ops). PATH selects side.
#   A = clamp(bottom_screen * fB_clamped * 0.8925, 0, 255)   (≈35% PWM max)
```
- The smali builds the path from the constants `"/sys/class/sn3112"` + `"l"`/`"r"` + `"/led/brightness"` with value `"1-" … ":" … "\n"`.
- It writes from a background `HandlerThread("strip-boy-led")`, keeps only the latest pending value, and skips duplicate writes. That matters because sysfs writes take 5–10 ms each and per-frame synchronous writes broke rendering.
- If **Bifrost** (`com.moonbench.bifrost`) is installed, it sends an explicit broadcast `com.moonbench.bifrost.api.ACTION_DISPLAY` to `com.moonbench.bifrost/.external.ExternalApiReceiver` instead, with extras `apiVersion`, `effect="PIPBOY"`, `color`, `colorRight`, `intensity`, `intensityScale`, `speed`, `priority`, `until="EXPLICIT_CLEAR"`, and `phaseSeconds`.

**Other projects using the same nodes:**

| Project | How it writes | Format / notes |
|---|---|---|
| WalkThor StickLights.kt | Direct file write, after checking `canWrite()` | `brightness` = `"Z-R:G:B:255"`, where **Z = half of the ring (1 or 2)**, written with a 25 ms gap. A separate `enable` node takes "1"/"0". It then mirrors AYN's settings `joystick_light_enabled`, `joystick_led_light_picker_color` and `led_light_brightness_percent` through `PServerBinder`, because "AYN … draws them whenever the screen wakes". |
| Bifrost LedController.kt (https://github.com/Pollux-MoonBench/Bifrost) | Root `echo` via `PServerBinder` | `echo 1-R:G:B:BR > /sys/class/sn3112l/led/brightness`, plus `2-` for the second zone of each stick. |
| Thor Wayfinder StickLights.kt (https://github.com/Thor-Wayfinder/thor-wayfinder) | Direct write ("world-writable … no root") | `"1-R:G:B"` (zone 1 = ring) and `enable` "1"/"0". It falls back to the standard LED class (`multi_intensity`/`brightness`) on LineageOS Thor builds. |
| rosbean/balatro-dualscreen ThorLeds.java | Via `PServerBinder`; says it follows Bifrost | `<zone>-<R>:<G>:<B>:<brightness>` |
| BDSA, ArmadaLive | Stick-LED brightness controls | Their stick-LED path is not shown in the BDSA docs. The documented bits are the `com.ro.external.request.light.adj.action` broadcast (`isShow`, `light_brightness`) and `sys.light.curr.is.show`. |

**Conclusion:** the path `/sys/class/sn3112{l,r}/led/brightness` and the format `<zone>-R:G:B:A` are confirmed by at least five independent projects. They **disagree on whether zone `2-` does anything**: Strip-Boy says it's a no-op, while WalkThor and Bifrost say it is the second half of the ring. Test this on the device. Some projects write directly; others use the `PServerBinder` root helper. Check `File.canWrite()` first and fall back to the helper or Bifrost. Prefer **Bifrost's opt-in API when it is installed**, so we don't fight other LED apps. AYN re-applies its own LED settings on every wake, so re-assert our colour after `ACTION_SCREEN_ON`.

---

## 6. Open questions only the user can answer
1. **Which "dualscreen launcher"?** ProfMags' Patreon "DualScreen Launcher" app, Cocoon, Mjolnir, AYN's stock Screen Launch, or another frontend? A screenshot or the app name in Settings → Apps would settle it.
2. If it is ProfMags' DSL: does the user expect TTW to run *inside* DSL? That needs ProfMags to add FNV/TTW, and the app looks abandoned. Or should our app merely coexist with DSL's Fallout 3 mod? Do they hold an active Patreon or access code, so we can test on a device?
3. Which Windows wrapper actually runs their TTW (GameNative, GameHub (Lite or official), Winlator Cmod, WEMU), and which store copy (Steam, GOG)? This decides the launch intent.
4. Are they OK with granting an **accessibility service** (focus hand-back, auto-open, revive) and/or **Display over other apps**? Or should we rely on AYN "Focus Lock" and manual opening?
5. Do they want our app to *be* the launcher, i.e. a "Play TTW" button on the bottom screen?
