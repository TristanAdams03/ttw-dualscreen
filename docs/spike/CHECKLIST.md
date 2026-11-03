# TTW DualScreen spike: device checklist

Run this on the AYN Thor. One action per line.
Fill in the results template at the bottom as you go.

Setup used: TTW through GameNative. The game folder is mapped as `A:\`.

## A. Install

1. On the Thor, open https://github.com/TristanAdams03/ttw-dualscreen/releases
2. Open the newest pre-release, `v0.0.1-spike`.
3. Download `TTWDualScreen-Plugin-v0.0.1-spike.zip`.
4. Download `TTWDualScreen-v0.0.1-spike.apk`.
5. Unzip the plugin zip.
6. Copy the `Data` folder from the zip into your TTW game folder.
   - That is the Fallout New Vegas / TTW folder you copied onto the Thor's storage.
   - It is the folder you picked when you added the game in GameNative with "+". GameNative shows it as `A:\`.
   - Open it with the Thor's Files app.
   - Go into `Data`, then `NVSE`, then `Plugins`.
   - If `Plugins` does not exist, create it.
7. Say yes to merge. Do not replace anything else.
8. Check that this file now exists: `Data\NVSE\Plugins\ttw_dualscreen.dll`
9. Check that this file exists too: `Data\NVSE\Plugins\ttw_dualscreen.ini`
10. Install the APK. Allow "install unknown apps" if Android asks.
11. Open the app. It moves itself to the bottom screen.
12. If it did not move, drag it to the bottom screen by hand. Note this in the results.

## B. First run

13. In GameNative, launch TTW as usual.
14. Load a save. Wait until you can walk around.
15. Look at the bottom-screen app. The big banner says one of: `LOOP: ALIVE`, `LOOP: STALLED`, `LOOP: SOCKET DEAD`.
16. If it says SOCKET DEAD, go to the troubleshooting section below.
17. Write down: the banner, the fps, the HP shown, the world flags, and the worldspace/cell.
18. Do the colors look right (compared with the game's Pip-Boy)? Write yes or no.

## C. Controller test (the main question)

Question: after you touch the bottom screen, does the controller still move your character?

Do this routine each time ("the touch test"):
- Walk with the controller. Confirm it works.
- Tap the bottom screen 5 times.
- Watch the banner. Write down the state.
- Push the stick, and press a button, in the game.
- Write yes or no: does the controller still move the character?

19. Touch test #1: Focus mode ON (the default). Record it.
20. In the app, turn Focus mode OFF. Touch test #2. Record it.
21. Turn Focus mode back ON.
22. Open the AYN quick settings drawer. Set Focus Lock to **Top screen**.
    Focus mode in the app stays ON. Run touch test #3 and record it.
23. In GameNative, exit the game (or long-press GameNative, App info, Force stop).
24. Open the container's `Fallout.ini`:
    `C:\users\xuser\Documents\My Games\FalloutNV\Fallout.ini`
    (use the container's file explorer in GameNative).
25. Edit this one. A second copy may exist in the game folder's `config` folder. Ignore that one.
26. Under `[General]`, add the line `bAlwaysActive=1`
27. Save the file.
    Open the AYN quick settings drawer and turn Focus Lock back off.
    Check Focus mode in the app is ON.
    Launch the game and load your save.
28. Touch test #4 (Focus Lock OFF, Focus mode ON, `bAlwaysActive=1`). Record it.

## D. Equip and world dump

Equip is refused in menus, the Pip-Boy, the console and loading screens.
Test it in normal gameplay with the Pip-Boy closed.
If it is refused, the app shows why: "in menu", "loading", "menu state unreadable", "no save loaded".

29. Stand in a Capital Wasteland exterior, Pip-Boy closed.
30. Tap **Equip first weapon**. Record what happened.
31. Tap **Dump world**. Record the result.
32. Go inside an interior. Repeat steps 30 and 31.
33. Go to any DLC world you can reach. Repeat steps 30 and 31.
34. Record the worldspace/cell for each place.

## E. Send the results

35. Tap **Copy report** in the app.
36. Paste it into the chat with Claude.
37. Also paste both log files:
    - `Data\NVSE\Plugins\ttw_dualscreen.log`
    - `Data\NVSE\Plugins\ttw_dualscreen.log.1` (the previous run)
    - If a log is long, attach the file instead of pasting.
38. Paste your filled-in results template too.

## If the banner says SOCKET DEAD

1. Does `Data\NVSE\Plugins\ttw_dualscreen.log` exist?
   - No: the plugin did not load. Check the DLL is in `Data\NVSE\Plugins\` and that xNVSE itself is installed.
2. Open the log. Does it say "server listening"?
   - No: read the lines above it. Paste them to Claude.
3. In the game console (press the ~ key), type `GetNVSEVersion`. Does it answer?
   - No: xNVSE did not load. The plugin cannot run.
4. Is something else using the port? The port is `[Server] Port` in `ttw_dualscreen.ini` (default 7960).
   - Change it to another number, such as 7961. Restart the game.
5. The plugin only loads on FNV 1.4.0.525.
   - The German no-gore exe is refused. The log says why.

## Results template

Copy this and fill it in.

```
Plugin and app installed OK:        yes / no
App moved to bottom screen itself:  yes / no

First run (step 15-18)
  Banner:
  fps:
  HP shown:
  World flags:
  Worldspace / cell:
  Colors look right:                yes / no

Touch test (banner state / fps / controller still works)
  #1 Focus mode ON:          ___ / ___ / yes / no
  #2 Focus mode OFF:         ___ / ___ / yes / no
  #3 Focus Lock = Top screen:      ___ / ___ / yes / no
  #4 bAlwaysActive=1, Focus Lock off:      ___ / ___ / yes / no

Equip first weapon (result or error text)
  Capital Wasteland exterior:
  Interior:
  DLC world:

Dump world (worldspace / cell)
  Capital Wasteland exterior:
  Interior:
  DLC world:

Anything odd:
```
