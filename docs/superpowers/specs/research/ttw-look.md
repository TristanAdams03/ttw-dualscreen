# TTW DualScreen: TTW look-and-feel and content research

Researched 2026-11-03. Research only: no game files were downloaded or extracted. Every path comes from public text sources.

**Confidence legend.** **H** means two or more independent sources, or a primary source (game XML/INI, a TTW record export, the TTW FAQ). **M** means one good secondary source, such as a reverse-engineering note marked "observed" or a decompile. **L** means inferred or a single weak source. **UNVERIFIED** means a gap or guess that still needs checking on a live install.

## 0. Key sources (cited by short name below)

| Short name | What it is | URL |
|---|---|---|
| NVF | NVForever spec notes. A GPL-3.0 Rust re-implementation of FNV whose notes are derived from the game's menu XML, textures, INIs, xNVSE/JIP reads of the running original and owner screenshots. Each claim is tagged [documented], [observed] or [hypothesis]. Pages used: `pipboy.md`, `hud.md`, `fonts.md`, `menu-xml.md`, `radio.md`, `audio.md`, `radiation.md`, `misc-stats.md`, `reputation.md`, `companion-wheel.md`, `bsa.md` | https://github.com/LootJunkieAzza/NVForever/tree/main/docs/spec-notes |
| DECOMP | FNV decompile: `statsmenu.cpp`, `hudmainmenu.cpp`, `mapmenu.cpp` (hard-coded texture paths) | https://github.com/momusdigital/fnv_decomp/tree/87eb9373a0bb7c9a38070be4f00c3bcb615d928d/src/fallout/interface/menus |
| INI-NV | Vanilla FNV `Fallout.ini`/`FalloutPrefs.ini` (has `sFontFile_9=NVFont_Test`, an FNV-only entry) | https://github.com/auraside/Hone/tree/HEAD/Files/Settings |
| INI-FO3 | FO3 `Fallout.ini` (8 font slots) | https://github.com/KettlerKen/FO3/blob/HEAD/Fallout.ini.base |
| TTW-XML | TTW's official GitHub repo. It is an xEdit/GECK XML export of TaleOfTwoWastelands.esm and optional plugins, last commit 2015-09-09, so it reflects the **TTW 2.x alpha, not 3.4** | https://github.com/TaleOfTwoWastelands/TaleOfTwoWastelands |
| TTW-FAQ | Official TTW FAQ (ModdingLinked "The Best of Times"; mod.pub links to it) | https://thebestoftimes.moddinglinked.com/faq.html (source: https://github.com/ModdingLinked/The-Best-of-Times/blob/main/faq.html) |
| TTW-MODPUB | TTW release page: **version 3.4.0, updated 15 June 2026**, ModPub Private Use License | https://mod.pub/ttw/133-tale-of-two-wastelands |
| TTW-FORUM | Read-only TTW forum: t=6730 (train stations), t=7436 ("4 years later"), t=6896 and t=1750 (reputation) | https://taleoftwowastelands.com/viewtopic.php@t=6730 |
| GECK-TTW | GECK wiki "TTW Mod Conversion Package and Guidelines" (Wayback 20251209020623) | https://web.archive.org/web/20251209020623/https://geckwiki.com/index.php?title=TTW_Mod_Conversion_Package_and_Guidelines |
| ARCH-3.4 | A third-party analysis dump that lists every archive in a real **TTW_v3.4** MO2 install with entry counts | https://github.com/acidpointer/psycho/blob/9f55c4b1ddf985e8a21da8e8e16ccde6d105277b/analysis/radare2/output/fnv_fairfax_mesh_array_population_20261012.txt |
| WIKI | fallout.wiki (independent wiki): Pip-Boy 3000, Karma (FNV/FO3), FNV Reputation, FNV challenges, radio-station and collectible categories (read via `?action=raw` and the API) | https://fallout.wiki |
| GLOBALS-XML | Vanilla-derived `menus/globals.xml` and `map_menu.xml` (from an Android Pip-Boy radio recreation) | https://github.com/sticks-stuff/pipboy_radio_new_vegas/tree/HEAD/source/menus |

---

## 1. Pip-Boy page tree (TTW uses FNV's Pip-Boy UI in both worlds)

TTW is "FO3 inside FNV". The Pip-Boy menus are FNV's `menus\main\stats_menu.xml`, `inventory_menu.xml` and `map_menu.xml` from `Fallout - Misc.bsa`. In TTW 3.4 the installer ships its own rebuilt `Fallout - Misc.bsa` with 141 entries against vanilla's 142 (ARCH-3.4). No TTW change to the menu layout was found (**L**: inferred, UNVERIFIED). The physical buttons on the device read STATS / ITEMS / DATA (NVF: glows `StatsGlow`/`ItemsGlow`/`DataGlow`).

| Page (device button, page title in font 4) | Heading cards (right of title, font 2) | Sub-tabs in bottom tab line, in order | Sub-sub / modes | Conf. |
|---|---|---|---|---|
| **STATS** | `LVL n` · `HP cur/max` · `AP cur/max` · `XP cur/next` | 1 **Status** · 2 **S.P.E.C.I.A.L.** · 3 **Skills** · 4 **Perks** · 5 **General** | Status: left column **CND**, **RAD**, **EFF**; Hardcore adds **H2O**, **FOD**, **SLP** (values 0–1000). General has two modes toggled with **R** (PC): reputation list, and general list (43 misc stats plus a karma block: "Karma: <alignment>", Vault Boy karma picture, karma title). The button reads "General R)" / "Reputation R)"; which mode is the default is UNVERIFIED | H (tabs: WIKI, NVF screenshots) / M (General modes: NVF) |
| **ITEMS** | `Wg cur/max` · `HP cur/max` · `DT n.n` · `Caps n` | 1 **Weapons** · 2 **Apparel** · 3 **Aid** · 4 **Misc** · 5 **Ammo** | Item panel: icon (380 sq.), cards DAM or DT, WG, VAL, STR, CND bar, ammo "20ga (2/30)", EFFECTS; hints "Repair R)"/"Maintain R)", "Mod X)"; weapon skill badge plus skill requirement | H (tabs) / M (panel) |
| **DATA** | Location name (cell or region map name) · date and time **"MM.DD.YY, H:MM"**, e.g. "10.25.81, 9:29" (code: `"%s, %d:%02d"`) | 1 **Local Map** · 2 **World Map** · 3 **Quests** · 4 **Misc** · 5 **Radio** | Quests: "Show Location", Make Active. Misc: notes (holotapes play), "Show Active Quest Notes"/"Show All Notes", and **Challenges** (Data > Misc > Challenges, toggled with R). Maps: "Place Marker", travel. Radio: station list plus waveform panel, "Tune" | H (tabs: WIKI, NVF, DECOMP format string) |

**Changes from FO3 that TTW inherits by using FNV's UI** (**H** unless marked):
- FO3 DATA **Notes** became FNV **Misc** (notes plus challenges). The FO3 page title "Notes" does not exist under TTW.
- FO3 STATS > General showed only karma and misc stats. FNV adds the Reputation mode.
- FO3 had no H2O/FOD/SLP.
- Skills are FNV's 13: Barter, Energy Weapons, Explosives, Guns, Lockpick, Medicine, Melee Weapons, Repair, Science, Sneak, Speech, Survival, Unarmed. Evidence: TTW renamed FO3's Small Guns and Big Guns bobbleheads to **Guns** and **Survival** (TTW-FAQ). NVF notes the FNV AVIF "Big Guns - OBSOLETE" is hidden.
- FO3 achievements became TTW **challenges** (Data > Misc > Challenges); GECK-TTW says "In some cases, ttw has challenges as replacements". TTW-FAQ lists about 90 added challenges (karma-level ones, "Bauble Head: Find all 20 collectible Vault-Tec Bobbleheads", "Kitsch Master: Find all 11 collectible snow globes", "Vault 101 Citizenship Award: Got the Pip-Boy 3000", and others).
- Level cap is 60 in TTW (TTW-FAQ; FNV is 50 with DLC). Perks come every level, or every 2 levels in Hardcore, since 3.3 (TTW-FAQ).
- FO3-only misc stats are tracked in TTW globals instead of the engine's misc-stat table: `TTWPCMiscStatBobbleheadsFound`, `TTWPCMiscStatCaptivesRescued`, `TTWPCMiscStatContractsCompleted`, `TTWPCMiscStatJusticeServed`, `TTWPCMiscStatPeopleMezzed` (GECK-TTW). Whether TTW shows them in the Pip-Boy General list is **UNVERIFIED**. FNV's list is a hard-coded set of 43 names (NVF misc-stats.md), so they probably don't appear and the companion would have to add them itself.

### 1a. Screen geometry and effects (for a pixel-faithful recreation)

| Item | Value | Source / conf. |
|---|---|---|
| Menu canvas | **1024 × 768** (`_pipboy_width`/`_pipboy_height`) | GLOBALS-XML (H) |
| Menu rect | 50 in and down, 855 × 650. The tab line sits at y 650 | NVF (M) |
| Line / text brightness | lines 200, text 255, disabled 128. Background fill brightness 128 at alpha 32. Gap 15. Lines 2 px thick on PC | GLOBALS-XML (H), NVF |
| Selected tab | boxed (lines at 200) and filled (brightness 128, alpha 32) | NVF (M) |
| List rows | 9 rows visible, 51 units per row (STATS/ITEMS); DATA rows 54; scroll bar 32 wide on the left | NVF (M) |
| Background | `Interface\Shared\Background\pipboy.dds` over the whole canvas at brightness 90 | NVF from inventory_menu.xml; also in map_menu.xml (H) |
| Scanlines | `textures\pipboy3000\pipboyscanlines.dds` (64×64 DXT1, a soft band). On screen, about 15% dimming every ~9.5 canvas units | NVF (M) |
| World map art | drawn at **alpha 128**, tinted | NVF (M) |
| INI effects | `[Pipboy] fBlurRadiusPipboy=3.5, fBlurIntensityPipboy=0.25, fScanlineScalePipboy=50, bEnableFlickerPipboy=1`; `[InterfaceFX] fBrightenPipboy=1.3, bEnableScanlinesPipboy=1, fVertHoldChance=0.08, fShudderChance=0.20` (";pipboy menu transitions"); pulse `fPulseBrightenIntensity=0.25, fPulseRadiusIntensity=0.5, fPulseRate=0.0006`; bursts `fDefaultBurstDuration=200/Intensity 2`, `fMiniBurstDuration=200/1.5`; menus `fScanlineScaleMenus=50, fBrightenMenus=1.7, fBlurRadiusMenus=0.3` | INI-NV (H); what each one scales exactly is UNVERIFIED (NVF §8) |
| Colour | `FalloutPrefs.ini [Interface] uPipboyColor=4290134783`, `uHUDColor=4290134783` = 0xFFB642FF = **RGB 255,182,66** (amber), packed RGBA. The same file still holds FO3-style `iSystemColorPipboy*=26,255,128` and `iSystemColorHUDAlt*=255,67,42` (the alternate HUD red), `iSystemColorTerminal*=33,231,121` | INI-NV (H, decoded). Which setting the engine reads: inferred u* (M) |
| On-screen measured amber | a solid CND bar photographed at about (220,180,64) after tone mapping | NVF (M) |

### 1b. STATS > Status details (useful exact wording)
- **CND**: the Vault Boy figure (zoom 145) with six limb brackets and bars; "Crippled" replaces a bracket at 0. Under the figure: **"<name> - Level <n>"**. Hints are right-aligned at the top right: "(9) Stimpak S)", "(3) Doctor's Bag E)" (NVF, observed). The face on the Pip-Boy page is always `face_00` (NVF; hypothesis for other states).
- **RAD**: "RAD RESIST" plus a value (e.g. "8%"). A meter from 0 to 1000 is labelled "500" and "1000", with a pointer at current rads. Hints: "RadAway A)", "Rad-X X)" (NVF observed).
- **EFF**: one line per source, e.g. "Vault Jumpsuit: Speech +2, Melee Weap. +2"; "No Effects" when there are none (NVF; the wording is a hypothesis).

### 1c. FNV HUD layout and wording (TTW uses the same HUD)

`menus\main\hud_main_menu.xml` (Fallout - Misc.bsa) declares named pieces. Positions are mostly set in code. Units are interface units with 960 = view height (NVF hud.md, M/H).

| Element | Where | Look / wording | Conf. |
|---|---|---|---|
| **HP + compass** (`HitPoints`, 369×127) | bottom-left, about 40 from the side and 30 from the bottom (hypothesis) | "**HP**" above a bar of **37 ticks** (8-unit step, `hud_tick_mark.dds`, ticks at brightness 175) that empties from the right. Below it, the compass window (345×64) shows `glow_hud_comp_direction_strip.dds` (1024 texels = 360°, letters N NE E SE S SW W NW 128 apart). The right end fades out; the heading sits at the bracket's small down-triangle. Actor ticks are **red when hostile, HUD colour otherwise**, and sneaking actors get none. Map markers show as small triangles, hollow when undiscovered and filled when discovered. The quest target uses `glow_hud_compass_objective_marker`; an off-band target is pinned at the end. **No distance numbers** | M/H |
| **AP + weapon** (`ActionPoints`, 386×127) | bottom-right | "**AP**" above a 37-tick bar that empties from the left (`Interface\VATS\vats_bar.dds`). Under the long line: "**CND**" plus a condition bar (60×20 ground, round(59×cnd)) with jam-threshold arrows (`repair_meter_pips.dds`). After the divider: the ammo counter "**13/60**" (clip/reserve), with an ammo-type label left of it ("HP", "JHP", "AP", "Slg"…; nothing for standard rounds) | M (xNVSE reads) |
| **Hardcore / status words** (`HardcoreMode`, font 7) | a row near the HP bar (captured at view 190,803) | "**H20**" (written with a zero, observed via JIP), "**FOD**", "**SLP**", "**RAD**", "**LMB**". States: hidden, faded (alpha 124), normal, red (HUD-alt colour), flashing. H20/FOD/SLP only show in Hardcore | M |
| **Crosshair** (`ReticleCenter`, 63×75) | centre | `Interface\HUD\glow_crosshair.dds`; switches to `glow_crosshair_filled.dds` over something activatable | M |
| **Prompt** (`Info`, 320×80) | under the crosshair | e.g. "E) Talk" plus the name, "VAL"/"WG" values ("--" for actors); "[EMPTY]", "Locked" | M |
| **Sneak indicator** (`SneakMeter`, 356×34) | top centre, text baseline at about y 51 | Only a bracketed word in font 7: **[HIDDEN]** and **[DETECTED]** in the HUD colour, **[CAUTION]** and **[DANGER]** in the alternate red | M (observed screenshots) |
| **Enemy health** (`EnemyHealth`, 350×52) | bottom centre | `glow_enemy_health_seperator.dds` bracket with ticks (at most 30, even count, emptying toward the middle). The actor's **name centred under it** in font 7. Red if hostile, HUD colour otherwise. **Never shown at full health**; lingers about 5.5 s | M |
| **Messages** (460×90) | top-left | Vault Boy mood icon (`glow_message_vaultboy_*`) plus text, e.g. "Your health is low. You may want to use a Stimpak or find some water." | M |
| **Quest / discovery notices** | top-left area | "Quest added", "You have discovered" over the place name in large caps, "+10 XP" | M |
| **XP meter** | — | "XP", "+n XP", "LEVEL UP" | M |
| **Radiation meter** (367×60) | right side, HUD-alt red | "**+9 RAD/SEC**": value in font 8, "RAD/SEC" in font 7, with a bracket and pointer | M |
| **Crippled-limb Vault Boy** | top-right, about 100 units from the right and 170 from the top | stats limb pieces at about 0.74 scale; the crippled limb is dashed red | M |
| **Companion icons** | — | **None in vanilla FNV.** Companions appear only as friendly compass ticks; the companion wheel is a separate menu | M (no HUD element exists in the XML list); UNVERIFIED by screenshot |

---

## 2. Fonts

| Slot (`[Fonts] sFontFile_n`, Fallout.ini) | File | Used for (FNV) | Conf. |
|---|---|---|---|
| 1 | `Textures\Fonts\Glow_Monofonto_Large.fnt` | map highlight-box names, note text, companion wheel | H (file) / M (use) |
| 2 | `Textures\Fonts\Monofonto_Large.fnt` | **most Pip-Boy text**: tabs, cards, lists, buttons, message text | H / M |
| 3 | `Textures\Fonts\Glow_Monofonto_Medium.fnt` | companion wheel preview/context text | H / M |
| 4 | `Textures\Fonts\Monofonto_VeryLarge02_Dialogs2.fnt` | Pip-Boy page titles (STATS/ITEMS/DATA) | H / M |
| 5 | `Textures\Fonts\Fixedsys_Comp_uniform_width.fnt` | terminals (guessed from the name) | H / L |
| 6 | `Textures\Fonts\Glow_Monofonto_VL_dialogs.fnt` | message-box titles | H / M |
| 7 | `Textures\Fonts\Baked-in_Monofonto_Large.fnt` | **all HUD labels and numbers**, sneak word, ammo, hardcore words | H / M |
| 8 | `Textures\Fonts\Glow_Futura_Caps_Large.fnt` | large HUD numbers (radiation "+n") | H / M |
| 9 (FNV only) | `Textures\Fonts\NVFont_Test.fnt` | has a .fnt and .tex but no .dds; draws nothing | H (INI-NV) / M |

- **Files and format.** `textures\fonts\*.fnt` are 14,632 bytes each: a header, the texture name (e.g. `Monofonto_Large_0_Lod_A`), then 256 glyphs × 14 floats (UV corners, width, height, pre/post spacing, ascent). Textures are `textures\fonts\*_0_lod_a.dds`; `monofonto_large` is 256² with the glyph shape in alpha. In vanilla FNV they live in **`Fallout - Textures2.bsa`** (NVF fonts.md, observed: M). FO3 kept them in its `Fallout - Textures.bsa` with `.tex` variants (BSAManager comment; ByroRedux tests). Line pitch per font (1–8): 31, 35, 32, 36, 25, 38, 38, 52 (NVF observed). **So the game's own bitmap fonts can be rendered at runtime from the player's install**, with no redistribution needed.
- Under TTW 3.4 the font files come from whichever archive wins: TTW rebuilds `Fallout - Textures2.bsa` (10,699 entries against vanilla 10,605; ARCH-3.4). The user's `Fallout.ini [Fonts]` may be remapped by UI mods (DarNified UI swaps slot 1 to `DarN_FranKleinBold_14.fnt`). The companion should **read `sFontFile_1..9` from the user's INI**, not hardcode the list.
- **Monofonto licence: not usable as a bundled fallback.** It's a Typodermic font (Ray Larabie). The free download comes with the **Typodermic Desktop EULA**, which covers finished artwork only. Their licence page says a font that "powers a PC or console game, desktop software, server, service, or device", live website text, or an embedded app font needs a paid Webfont/App/custom licence, and "A free desktop download does not include a free MyFonts webfont or other embedding license" (https://typodermicfonts.com/license/). Futura is commercial too.
  - **Recommendation:** load the game's `.fnt` and `.dds` at runtime. If a fallback is required, ship an OFL monospace font such as Share Tech Mono or VT323. Both are candidates to evaluate visually; neither was compared here.

---

## 3. Art, sound and UI-file paths

Paths below are as the engine resolves them. Texture paths in records omit the leading `textures\`, and sound paths omit `sound\`.

**Use paths, not archive names.** ARCH-3.4 shows a TTW 3.4 install rebuilds the vanilla-named FNV archives with different contents. Vanilla vs TTW entry counts: Textures 11,470 vs 5,366; Textures2 10,605 vs 10,699; Meshes 19,587 vs 17,643; Sound 6,465 vs 4,811; Misc 142 vs 141. TTW also adds `TaleOfTwoWastelands - Textures.bsa` (13,627) and `- Main.bsa` (6,910).
- **Do not hardcode which BSA holds a file.** Index every `.bsa` the game would load: the `SArchiveList` (Textures, Textures2, Meshes, Voices1, Sound, Misc), then each plugin's `<plugin> - *.bsa` in load order, with loose files on top. Last one wins.
- The full TTW 3.4 archive set (ARCH-3.4):
  - FNV: `Fallout - {Meshes,Misc,Sound,Textures,Textures2,Voices1}.bsa`, `Update.bsa`, `{DeadMoney,HonestHearts,OldWorldBlues,LonesomeRoad,GunRunnersArsenal} - {Main,Sounds}.bsa` (TTW ships its own `*- Sounds.bsa` for those four DLC), and `{Caravan,Classic,Mercenary,Tribal}Pack - Main.bsa`.
  - FO3: `Fallout3 - {MenuVoices,Meshes,Sound,Textures,Voices}.bsa` and `{Anchorage,ThePitt,BrokenSteel,PointLookout,Zeta} - {Main,Sounds}.bsa`.
  - TTW: `TaleOfTwoWastelands - {Main,Textures}.bsa` and `YUPTTW - {Main,Sounds}.bsa`.
- Plugins: FalloutNV, DeadMoney, HonestHearts, OldWorldBlues, LonesomeRoad, GunRunnersArsenal, Fallout3, Anchorage, ThePitt, BrokenSteel, PointLookout, Zeta, TaleOfTwoWastelands, YUPTTW (+ packs) (fnv-to-fo4 `templates.json`; GECK-TTW master list).
- Under **MO2** the Data folder is virtual. An out-of-process reader won't see it; one inside the game process (an xNVSE plugin) will. This is an architecture risk.

### 3a. Textures

| Asset | Path | Likely BSA (vanilla / TTW) | Format | Conf. |
|---|---|---|---|---|
| Pip-Boy menu background | `textures\interface\shared\background\pipboy.dds` | Fallout - Textures*.bsa (which one: UNVERIFIED) | DDS | H |
| Scanlines | `textures\pipboy3000\pipboyscanlines.dds` | FNV Textures* | 64×64 DXT1 | M (NVF + ayn-dual-screen) |
| Screen glare | `textures\pipboy3000\screenglare.dds` | FNV Textures* | DDS | M |
| Trap: `greenscreen.dds` | `textures\pipboy3000\greenscreen.dds` is a pre-rendered picture of a whole Pip-Boy screen, not a background | — | — | M (ayn-dual-screen) |
| Pip-Boy arm/casing | `textures\pipboy3000\{pc,xbox,ps3,ps3o}\english\PipBoyArm01.dds` (the model names the xbox variant); ayn-dual-screen uses `pipboy3000\pipboy.dds` | FNV Textures* | DDS | M/L |
| UI atlas | `textures\interface\interfaceshared.tai` (text index) → `InterfaceShared<n>.dds` (1024²). **The HUD/Pip-Boy `glow_*` pieces are drawn from the atlas**, and the atlas art differs from the loose same-named files | FNV Textures* | TAI + DDS | M (NVF observed) |
| Status Vault Boy | `textures\interface\stats\{head,torso,left_arm,right_arm,left_leg,right_leg}.dds` plus `*_broken.dds`, and `face_00..04.dds`, `face_10.dds` (`Interface\Stats\face_%02d.dds`). No FO3 `pc_body.dds` set | FNV Textures* | DDS | H (DECOMP + NVF + ayn) |
| CND/RAD brackets | `vats_*_divider.dds` (limb bracket), `rad_meter_bracket.dds`, `rad_effects_bracket.dds`, `pointer_tail.dds` | folder UNVERIFIED | DDS | M (names) / L (folder) |
| Karma pictures | `textures\interface\icons\pipboyimages\karma icons\karma_{saintly,good,neutral,bad,evil}.dds` | FNV Textures* | DDS | H (DECOMP + 3 other repos) |
| S.P.E.C.I.A.L. Vault Boys | `textures\interface\icons\pipboyimages\s.p.e.c.i.a.l\special_<strength…luck>.dds` (from AVIF ICON) | FNV Textures* | DDS | H (NVRE test + ayn) |
| Skill Vault Boys | read the skill AVIF `ICON` field (folder likely `…\pipboyimages\skills\`) | — | DDS | L (folder UNVERIFIED) |
| Perk Vault Boys | `textures\interface\icons\pipboyimages\perks\perk_<name>.dds` (PERK ICON), e.g. `Perk_Cyborg.dds`, `Perk_size_matters.dds` | FNV Textures* / Fallout3 - Textures / TTW | DDS | H (TTW-XML) |
| Item icons | record `ICON`: `textures\interface\icons\pipboyimages\{weapons,apparel,items,…}\*.dds` (e.g. `Weapons\weapons_mine.dds`, `Items\items_cram.dds`, `apparel\vault_suit_21.dds`). Armour keeps male/female icons on the biped model | FNV / Fallout3 / DLC / TTW archives | DDS | H (TTW-XML) |
| Small HUD icons | record `MICO`: `textures\interface\icons\pipboyimages_small\weapons_small\glow_weapons_mine.dds` | same | DDS | H |
| Weapon skill badges | `textures\interface\icons\typeicons\weap_skill_icon_{sm_arms,big_guns,energy,explosives,melee,unarmed}.dds` (64², `IM_ItemIconBadge`) | FNV Textures* | DDS | M |
| Reputation pictures | REPU `ICON`, e.g. `interface\icons\pipboyimages\reputations\reputations_brotherhood_of_steel.dds` | FNV Textures* | DDS | H (format) |
| Unused TTW DC reputation art | `interface\icons\pipboyimages\reputations\ttw\{arefu,bigtown,canterburycommons,enclave,lamplight,megaton,oasis,paradise,regulators,rivetcity,royphillips,talon,tenpenny,underworld}.dds` ("we left the icons there", TTW dev Risewild, 2019) | TTW archives | DDS | M (alpha XML) / UNVERIFIED in 3.4 |
| Message Vault Boys | `textures\interface\icons\message icons\glow_message_vaultboy_{very_happy,neutral,sad,in_pain}.dds` | FNV Textures* | DDS | H (DECOMP) |
| Cursor | `textures\interface\icons\misc\glow_cursor.dds` (also used as the map player arrow) | FNV | DDS | H (map_menu.xml) |
| Shared UI bits | `interface\shared\marker\{square,square_filled,glow_square_small,glow_square_filled_small}.dds`, `interface\shared\meter\tickmark_{horiz,vert}.dds`, `interface\shared\solid.dds`, `solid_black.dds`, `interface\shared\arrow\repair_meter_pips.dds` | FNV | DDS | H |
| HUD | `textures\interface\hud\`: `glow_hud_left_seperatorglow.dds`, `glow_hud_right_seperatorglow.dds` (512×256), `hud_tick_mark.dds` (8×32 DXT3), `glow_hud_tick_mark.dds`, `glow_hud_comp_direction_strip.dds` (1024×64), `hud_compass_alphamap.dds`, `glow_hud_compass_{objective_marker,pc_marker,landmark,landmark_discovered}.dds`, `glow_crosshair.dds`, `glow_crosshair_filled.dds`, `glow_enemy_health_seperator.dds`, `glow_stealth_metre_seperator.dds`, `glow_messages_radiation_seperator_right.dds` and `_arrow.dds`; plus `interface\vats\vats_bar.dds` | vanilla: **Fallout - Textures2.bsa** (NVF) | DDS | H (paths) / M (BSA) |
| Map markers | `textures\interface\icons\world map\icon_map_{city,settlement,encampment,natural_landmark,cave,factory,monument,military,office,ruins_town,ruins_urban,ruins_sewer,metro,vault}.dds` for REFR XMRK TNAM types 1–14, plus `icon_map_undiscovered.dds` | FNV Textures* | DDS | H (DECOMP + fnvmap + bethesda-multitool + nv-rs) |
| Map overlays | `textures\interface\worldmap\{waypoint_node,waypoint_path,waypoint_path_interior}.dds`; local-map door `textures\interface\icons\local map\icon_local_door.dds` | FNV | DDS | H |
| Companion wheel | `textures\interface\companion_wheel\` (35 files: `hud_comp_bkg.dds`, `hud_comp_pie.dds`, `hud_comp_slice1..8.dds`, icons) | FNV | DDS | M |
| FNV DLC icons | under `textures\nvdlc0X\interface\icons\…` | `<DLC> - Main.bsa` | DDS | M (VegasCraft) |

### 3b. World-map textures (WRLD `ICON`) and calibration (MNAM/ONAM)

Read these from the WRLD record at runtime: the companion needs the `ICON` plus `MNAM` (usable size, NW/SE cells) plus `ONAM` (scale, offset). Child worldspaces flagged "Use Map Data" show their parent's map. Values below are from TTW-XML (the 2015 export, mostly vanilla records).

| Worldspace | ICON path (under textures\) | MNAM usable / cells NW→SE | ONAM scale, offset | Conf. |
|---|---|---|---|---|
| Wasteland (Capital Wasteland) | `Interface\Worldmap\Wasteland_1024_no_map.dds`. TTW keeps FO3's path; no separate TTW map file found | 2048², (-30,30)→(20,-20) | 1, (0,0) | H for the alpha; UNVERIFIED in 3.4 |
| WastelandNV (Mojave) | `interface\worldmap\wasteland_nv_1024_no_map.dds` (drawn 2048²) | 2048², (-35,35)→(25,-25) | 1, (0,0) | H (TTW-XML + NVF + ayn). NVF measured the cell grid fills the middle 0.7876 from (0.108, 0.102) |
| DLC02AnchorageBattle "Anchorage" | `DLCAnch\Interface\Worldmap\DLCAnch_1024_no_Map.dds` | 1024², (-25,25)→(25,-25) | 1.125, (-5000,-35000) | H (alpha) |
| DLC01PittWorld "The Pitt" | `DLCPitt\Interface\DLC01PittWorldMap.dds` | 1024², (-10,10)→(10,-10) | 2, (1000,5000) | H (alpha) |
| DLC03AdamsAFB "Adams Air Force Base" | `DLC03\Interface\DLC03Adams_1024_no_Map.dds` | 1024², (-7,5)→(3,-5) | 0.95, (0,0) | H (alpha) |
| DLC4Pointlookout "Point Lookout" | `DLC04\Interface\Worldmap\DLC04WorldMap.dds` | 2048², (-7,8)→(7,-6) | 0.91, (-3800,2200) | H (alpha) |
| NVDLC02ZionCanyon (Zion) | `interface\worldmap\wasteland_zion_1024_no_map.dds` | 1024², (-11,11)→(10,-12) | 1.1, (500,2500) | H (alpha) |
| Sierra Madre / Big MT / Divide | **UNVERIFIED**; read WRLD ICON | — | — | — |
| Mothership Zeta | no exterior worldspace in TTW's lists (**UNVERIFIED**) | — | — | L |

"Likely BSA" for the DLC maps is the DLC's own `- Main.bsa`; Wasteland's is `Fallout3 - Textures.bsa` or a TTW archive (**L**). It is unconfirmed whether a stray copy of `Wasteland_1024_no_map.dds` also exists in FNV's own archives.

### 3c. UI sounds (SOUN record → file; paths under `sound\`; vanilla in `Fallout - Sound.bsa`, which TTW 3.4 rebuilds under the same name)

| Event | SOUN | File | Conf. |
|---|---|---|---|
| Pip-Boy up / down | UIPipBoyAccessUp / UIPipBoyAccessDown | `fx\ui\pipboy\ui_pipboy_access_up.wav` / `_down.wav` | H (NVF audio.md, from the ESM) |
| Sub-tab change | UIPipBoyTab | `fx\ui\pipboy\ui_pipboy_tab.wav` | H |
| Page change (STATS/ITEMS/DATA) | UIPipBoyMode (also UIMenuMode, UICheckBox) | `fx\ui\pipboy\ui_pipboy_mode.wav` | H |
| List scroll / focus | UIPipBoyScroll = UIMenuFocus | `fx\ui\menu\ui_menu_focus.wav` | H |
| OK / Cancel / Prev-Next | UIMenuOK / UIMenuCancel / UIMenuPrevNext | `fx\ui\menu\ui_menu_ok.wav`, `ui_menu_cancel.wav`, `ui_menu_prevnext.wav` | H |
| Idle static crackle | UIPipBoyStatic | random file from the folder `fx\ui\pipboy\static\` | M (timing is a hypothesis) |
| **Geiger counter** | UIPipBoyRadiationA / B / C | folders `fx\ui\pipboy\radiation\a\`, `\b\`, `\c\` (3 takes each, about 1 s, 16-bit 44.1 kHz mono); a test loop UIPipBoyRadiationTestLP | M |
| Radio static | OBJRadioStaticLP / UIRadioStaticLP | `fx\ui\radio\ui_radio_static_lp.wav`; also UIRadioSignalFound / UIRadioSignalLost | M |
| Pop-up message | UIPopUpMessageGeneral | `fx\ui\popup\ui_popup_messagewindow.wav` | M |
| Take all | UIItemTakeAll | `fx\ui\items\ui_items_takeall.wav` | M |
| Reputation / karma / light | UIRepGood / UIRepBad; karma up/down; UIPipBoyLightOn / Off | files UNVERIFIED | L |

Notes: some records name `x.wav` where the archive holds `x.ogg` instead (the base sound BSA has 4,233 .wav and 2,232 .ogg files; NVF). **123 SOUN records in FalloutNV.esm start with "UI"**, so the companion should resolve sounds through the SOUN records rather than a fixed list.

### 3d. Menu files (all in `Fallout - Misc.bsa`)
`menus\main\hud_main_menu.xml`, `menus\main\stats_menu.xml`, `menus\main\inventory_menu.xml`, `menus\main\map_menu.xml`, `menus\globals.xml`, `menus\prefabs\{hudtemplates,compassicon,meter,brightness,card_info,tabline,tabline_template,super_text_box,text_box,box,line,vertical_fade_line,list_box,list_box_template,scrollbar_vert,item_stats_display,button_companion_wheel,icon_companion_wheel}.xml`, `menus\companion_wheel_menu.xml`, `menus\levelup_menu.xml`, `menus\vats_menu.xml` (NVF, DECOMP: H). Labels come from game settings referenced as `&-sXxx;` (e.g. `&-sPlaceMarker;`, `&-sShowLocation;`, `&-sShowActiveNotes;`, `&-sShowAllNotes;`, `&-sNextChallenge;`, `&-sMakeActiveQuest;`, `&-sTune;`, `&-sTravel;`, `&-sPlayAudio;`, `&-sStopAudio;`, `&-sData;`). Most of these strings are **not in the ESMs** but are executable defaults (NVF). If the companion parses the XML it needs a table of those default strings, which can be filled from screenshots.

---

## 4. World structure

### 4a. Worldspaces → wasteland flag

Mechanism from TTW-XML (2015 alpha; the scripts still exist by name in GECK-TTW, which marks some calls as deprecated):
- Quest `TTWFunctions` runs `TTWFNWorldspaceCheck` in GameMode. Outdoors only (it returns when `Player.IsInInterior`), it looks up `GetParentWorldspace` in FormList `TTWWorldSpacesCheckerList`.
- That list holds 11 FormLists. The first form of each is the mapper, which ORs a bit into `TTWWorldLocation` (global 0C010FAA).
- Bits: 1 = FO3 Wasteland group, 2 = DC downtown, 4 = The Pitt, 8 = Anchorage, 16 = Broken Steel, 32 = Point Lookout, 512 = Mojave, 1024 = Dead Money, 2048 = Honest Hearts, 4096 = OWB, 8192 = Lonesome Road.
- **The flags flip only for the bits 1|2 group (→ `TTWFNEnableDCWasteland`, sets `bInDCWasteland`=1) and bit 512 (→ `TTWFNEnableNVWasteland`, sets `bInNVWasteland`=1), each paired with disabling the other world.**
- In DLC worlds, interiors and unlisted worldspaces the flags **keep their previous value**: they inherit the wasteland you came from.
- Globals: `bInDCWasteland` 0C001E0F (Short, default 1), `bInNVWasteland` 0C00DFAA (Short, default 0). The start-menu script sets 1/0 for a DC start and 0/1 for an NV start (`TTWCG00Script`, `iGameChoice` 1 = NV, 2 = CW).

| World | Worldspace EditorIDs (FormID in 2015 load order) | bInDCWasteland | bInNVWasteland | Conf. |
|---|---|---|---|---|
| Capital Wasteland | Wasteland (0607EA1F), MegatonWorld, CitadelWorld, DLC03CitadelWorld, ParadiseFalls, Oasis, OasisHarold, NtlGuardDepotWorld, DLC02BaileysCrossroads | **1** (set) | 0 | H (alpha) |
| DC downtown | DCworld01 "Chevy Chase", 03, 05, 06, 08, 09, 10, 11, 12, 15, 17, 18; MonumentWorld; GNRroofWorld; MamaDolcesWorld; WashMonTop | **1** (set) | 0 | H (alpha) |
| TTW Union Station (DC) | TTWUnionStationDCWorld ("Colombus Circle" [sic] in the alpha; FAQ: "Columbus Circle"), TTWUnionStationWorld "Union Station"; both parented to Wasteland with Use Map Data | 1 (inherited; not in the checker lists) | 0 | M |
| Operation Anchorage (simulation) | DLC02AnchorageBattle "Anchorage", DLC02Glacier, DLC02Overlook, DLC02ChineseHQ (Anchorage.esm) | 1 (inherited) | 0 | M (inferred from script) |
| The Pitt | DLC01PittWorld "The Pitt", DLC01SteelMillExterior, DLC01MarketSquare, DLC01Haven (ThePitt.esm; TTW's internal DLC01 = Pitt, DLC02 = Anchorage) | 1 (inherited) | 0 | M |
| Broken Steel | DLC03AdamsAFB "Adams Air Force Base", DLC03RelayStation | 1 (inherited) | 0 | M |
| Point Lookout | DLC4Pointlookout "Point Lookout", DLC4Bog | 1 (inherited) | 0 | M |
| Mothership Zeta | interiors only (none listed) | 1 (inherited) | 0 | L |
| Tranquility Lane (simulation) | TranquilityLane (not listed) | 1 (inherited) | 0 | L |
| Mojave | WastelandNV (000DA726), WastelandNVmini, TheStripWorldNew, Lucky38World, GamorrahWorld, TheFortWorld, FreesideWorld, FreesideNorthWorld, FreesideFortWorld, BoulderCityWorld, GreenhouseWorld01 | 0 | **1** (set) | H (alpha) |
| Dead Money (Sierra Madre) | NVDLC01Villa, NVDLC01EastTownN/S, NVDLC01WestTownN/S, NVDLC01VillaDean, NVDLC01VillaChristine | 0 | 1 (inherited) | M |
| Honest Hearts (Zion) | NVDLC02ZionCanyon | 0 | 1 (inherited) | M |
| Old World Blues (Big MT) | NVDLC03BigMT, NVDLC03Higgs | 0 | 1 (inherited) | M |
| Lonesome Road (Divide) | NVDLC04DivideWorld, NVDLC04DivideVistaWorld, NVDLC04Road01World, NVDLC04Road02World, NVDLC04NukeSilo2 | 0 | 1 (inherited) | M |

Recommendation: have the companion classify worlds from the **current worldspace EditorID** using this table, and use the two flags only as a cross-check. Whether `TTWWorldLocation` still exists in 3.4 is **UNVERIFIED**.

### 4b. Travel and date
- **Route (TTW 3.2+, TTW-FORUM t=6730 and TTW-FAQ).** From The Mall, go to the National Archives, then into the Penn. Ave/The Mall Metro. Follow the clear tunnel to the new **Judiciary Square Metro**, which opens into a new DC worldspace, **Columbus Circle**, where **Union Station** stands. The Mojave end is the train station in **Freeside** (Freeside North, past the Mormon Fort, under the overpass). The FAQ says "To get back to the Mojave, use the Union Station located in Freeside", which looks like a typo for "Capital Wasteland".
- Story framing: the Lone Wanderer takes the train West and becomes the Courier ("Getting shot in the head was a real wake up call").
- **Mechanics (alpha `TTWTransitScript01`).**
  - Requires a ticket item (`TTWTrainTicket01`/`02`) and shows a confirm message.
  - It fades out, then plays a Bink video, moves the player, toggles world content, and sets `TTWWorldLocation` to 1 or 512.
  - **The first DC→NV trip** (Mojave intro quest VCG00 not yet started) resets health, takes the stolen-gear container, and sets **GameDay 18, GameMonth 9 (0-based, so October), GameYear 2281**. It then starts VCG00, the FNV opening at Doc Mitchell's.
  - Every other trip calls `TTWFNHardcoreSafeAdvanceDays 14` and plays "2 weeks later.bik".
  - The alpha's first-trip video was "9 years later.bik". **Since 3.2 it is "4 years later"** (TTW-FORUM t=7436, RoyBatty: "No, it uses 4 years later.").
- **Date shown.** There is one global game calendar, shown on the DATA header as "MM.DD.YY, H:MM". A DC start runs from FO3's 2277 and jumps to Oct 2281 on the first trip west. An NV start begins in 2281 and the Capital Wasteland is then played in 2281 or later. Later trips add about 14 days each. The exact 3.4 day/month values and the trip length are **UNVERIFIED**.
- On every world switch TTW calls `PipBoyRadioOff` and enables or disables that world's radio transmitters (see 5a).

---

## 5. World-specific dashboard content

### 5a. Radio stations by world (Pip-Boy list = stations in range without the Non-Pipboy flag, sorted by name)

| World | Stations (Pip-Boy) | TTW handling | Conf. |
|---|---|---|---|
| Capital Wasteland | **Galaxy News Radio** (two transmitter refs, before and after the dish is fixed: `RadioGalaxyNewsRef01`/`02`), **Enclave Radio** (`RadioEnclaveRef`; until Raven Rock is destroyed), **Agatha's Station** (`MS17RadioRef`, after the quest), **Vault 101 Emergency Frequency** (`MS16Vault101ExternalRadioREF`), Ranger Emergency Frequency, Chinese Radio Beacon, People's Republic of America Radio, Signals Alfa Lima / Echo Foxtrot / Kilo Bravo / Oscar Tango / Oscar Zulu / Papa November / Sierra Romeo / Sierra Victor / Yankee Bravo (ham radios) | All of these are **disabled while in the Mojave** (`TTWFNDisableDCWasteland`) | H (alpha script + WIKI category) |
| FO3 DLC | The Pitt: **Wernher's Distress Signal** (`DLC01RadioStationREF`, until DLC01Quest01 stage 50), Pitt Broadcast (in The Pitt). Anchorage: **Outcast Distress Signal** (`DLC02OutcastRadioSignalRef`). Zeta: **Recon Craft Theta Beacon** (`DLC05AlienRadioBeaconREF`). Point Lookout and Broken Steel: no new station found | DC-only | H/M |
| Mojave | **Radio New Vegas** (`RNVTARef`, everywhere), **Mojave Music Radio** (TTW uses its own copy, `TTWvCountryRadioRef`), **Black Mountain Radio** (radius 50,000, 5% static), **NCR Emergency Radio**, **Camp Guardian Radio**, White Glove Society Radio Signal, Launch Music Station, Vault-Tec Channel 9. Non-Pip-Boy local radios: Tops/Ultra-Luxe/Gomorrah, store radios | **Disabled while in DC** (`TTWFNDisableNVWasteland`) | H |
| FNV DLC | **Sierra Madre Broadcast** (`NVDLC01RadioStationRef`), **Happy Trails Expedition Broadcast** (`NVDLC02JedRadioTAREF`, until NVDLC02MQ00 is complete), **Mysterious Broadcast** (`NVDLC03RadioREF`, OWB); in-DLC: 372.12 / 546.14 / 631.08 / 743.00 Hz ULF signals (Sierra Madre) | NV-only | H/M |

TTW-FAQ: the installer "Transcodes mono MP3 → WAV → stereo WAV → MP3 to fix some FO3 radio tracks". No new TTW radio station was found.

### 5b. Collectibles by world

| World | Collectible | Count / notes | Tracking | Conf. |
|---|---|---|---|---|
| Capital Wasteland | **Vault-Tec Bobbleheads** | 20. TTW changed them to **mini-perks**; SPECIAL and skill boosts are optional via TTW optionals/MCM. Agility: run 5% faster. Barter: −10% prices. Charisma: +10% companion damage/DR. Endurance: +5 DR. Energy Weapons: ignore 5 more DT. Explosives: −10% spread. Guns: +5% damage. Intelligence: +5 skill points or +5% crit from magazines. Lockpick: picks break less. Luck: +2.5% crit. Medicine: −10% addiction chance. Melee: 10% faster swings. Perception: +10% accuracy. Repair: 10% slower wear. Science: +10% damage vs robots. Sneak: +10% sneak-crit damage. Speech: hire any companion regardless of karma. Strength: +25 lb carry. Survival: positive chems last 10% longer. Unarmed: double damage vs blocking. The Charisma and Speech bobbleheads **swapped locations** | `TTWPCMiscStatBobbleheadsFound`; challenge "Bauble Head" | H (TTW-FAQ, GECK-TTW) |
| Capital Wasteland | FO3 skill books (13 kinds: Big Book of Science, Chinese Army Spec Ops, D.C. Journal of Internal Medicine, Dean's Electronics, Duck and Cover!, Grognak the Barbarian, Guns and Bullets, Lying Congressional Style, Nikola Tesla and You, Pugilism Illustrated, Tales of a Junktown Jerky Vendor, Tumblers Today, U.S. Army: 30 Handy Flamethrower Recipes) plus You're SPECIAL! | How TTW maps the Big Guns book and its point values is **UNVERIFIED** | "Books Read" misc stat | M |
| Capital Wasteland | Weapon schematics ("Another Man's Treasure"); Nuka-Cola Quantum | — | challenges | M |
| FO3 DLC | Steel ingots, 100 (The Pitt, "Mill Worker"); Intel suitcases, 10 (Anchorage, "Covert Ops"); Alien captive recorded logs (Zeta, "Alien Archivist") | — | TTW challenges | H (TTW-FAQ) |
| Mojave | **Snow globes**: Goodsprings, Hoover Dam, Mormon Fort, Mt. Charleston, Nellis AFB, Test Site, The Strip (7) | 11 including DLC; TTW challenge "Kitsch Master: Find all 11" | challenge | H (WIKI + TTW-FAQ) |
| FNV DLC | Snow globes: Sierra Madre, Zion National Park, Big MT, Lonesome Road | — | — | H |
| Mojave | FNV skill books (13: Big Book of Science, Chinese Army Spec Ops, D.C. Journal, Dean's Electronics, Duck and Cover!, Grognak, Guns and Bullets, Lying Congressional Style, Nikola Tesla and You, Pugilism Illustrated, Tales of a Junktown Jerky Vendor, Tumblers Today, Wasteland Survival Guide) | permanent skill points | "Books Read" | H |
| Both? | **Skill magazines** (temporary): Boxing Times, Fixin' Things, Future Weapons Today, ¡La Fantoma!, Lad's Life, Locksmith's Reader, Meeting People, Milsurp Review, Patriot's Cookbook, Programmer's Digest, Salesman Weekly, Today's Physician, True Police Stories, Tæles of Chivalrie | Whether they appear in DC is **UNVERIFIED** (the TTW Intelligence bobblehead affects magazines, which suggests yes) | — | H (list) / L (DC) |
| Mojave | Sunset Sarsaparilla star caps (50) | — | — | L (from memory) |

### 5c. Karma
- Karma is an engine stat, shown on STATS > General in **both worlds** under TTW (**M**).
- Bands: Very Good ≥750, Good 250–749, Neutral −249 to 249, Evil −749 to −250, Very Evil ≤−750 (WIKI). Pictures: saintly / good / neutral / bad / evil (DECOMP).
- **Titles** are GMSTs `sKarmicTitle{Good,Neutral,Evil}{1..30}`, with titles fixed at level 30+ (NVF; FalloutNV.esm overrides 50 of the 90, and the rest are executable defaults).
- FNV titles (WIKI), Good / Neutral / Evil:

| Lv | Good | Neutral | Evil |
|---|---|---|---|
| 1 | Samaritan | Drifter | Grifter |
| 2 | Martyr | Renegade | Outlaw |
| 3 | Sentinel | Seeker | Opportunist |
| 4 | Defender | Wanderer | Plunderer |
| 5 | Dignitary | Citizen | Fat Cat |
| 6 | Peacekeeper | Adventurer | Marauder |
| 7 | Ranger of the Wastes | Vagabond of the Wastes | Pirate of the Wastes |
| 8 | Protector | Mercenary | Betrayer |
| 9 | Desert Avenger | Desert Scavenger | Desert Terror |
| 10 | Exemplar | Observer | Ne'er-do-well |
| 11 | Vegas Crusader | Vegas Councilor | Vegas Crime lord |
| 12 | Paladin | Keeper | Defiler |
| 13 | Mojave Legend | Mojave Myth | Mojave Boogeyman |
| 14 | Shield of Hope | Pinnacle of Survival | Sword of Despair |
| 15 | Vegas Legend | Vegas Myth | Vegas Boogeyman |
| 16 | Hero of the Wastes | Strider of the Wastes | Villain of the Wastes |
| 17 | Paragon | Beholder | Fiend |
| 18 | Wasteland Savior | Wasteland Watcher | Wasteland Destroyer |
| 19 | Saint | Super-Human | Evil Incarnate |
| 20 | Guardian of the Wastes | Renegade of the Wastes | Scourge of the Wastes |
| 21 | Restorer of Faith | Soldier of Fortune | Architect of Doom |
| 22 | Model of Selflessness | Profiteer | Bringer of Sorrow |
| 23 | Shepherd | Egocentric | Deceiver |
| 24 | Friend of the People | Loner | Consort of Discord |
| 25 | Champion of Justice | Hero for Hire | Stuff of Nightmares |
| 26 | Symbol of Order | Model of Apathy | Agent of Chaos |
| 27 | Herald of Tranquility | Person of Refinement | Instrument of Ruin |
| 28 | Last, Best Hope of Humanity | Moneygrubber | Soultaker |
| 29 | Savior of the Damned | Gray Stranger | Demon's Spawn (NVF reads the ESM as "Demon Spawn") |
| 30+ | Messiah | True Mortal | Devil |

- FO3 titles differ: e.g. 1 Vault Guardian / Vault Dweller / Vault Delinquent, 9 Urban Avenger / Urban Ranger / Urban Invader, 11 Capital Crusader / Capital Councilor / Capital Crimelord, 14 Ambassador of Peace / Pinnacle of Survival / Harbinger of War, 20 Last, Best Hope / Paradigm of Humanity / Scourge of Humanity.
- **Which set TTW shows, and whether it changes per world, is UNVERIFIED.** It is probably one GMST set for the whole game. TTW's karma challenges reuse FO3 achievement names, e.g. "Ambassador of Peace: Reached Level 14 with Good Karma".
- Recommendation: **read the title string live from the game** rather than hardcoding it.

### 5d. Reputation factions
- **FNV (13):** factions Boomers, Brotherhood of Steel, Caesar's Legion, Followers of the Apocalypse, Great Khans, Powder Gangers, NCR, White Glove Society; towns Freeside, Goodsprings, Novac, Primm, The Strip (WIKI, H).
- Fame and Infamy each have 4 ranges, giving 16 titles: Neutral, Accepted, Liked, Idolized, Shunned, Mixed, Smiling Troublemaker, Good-Natured Rascal, Hated, Sneering Punk, Unpredictable, Dark Hero, Vilified, Merciful Thug, Soft-Hearted Devil, Wild Child. Each has a one-line description (WIKI).
- **Capital Wasteland: none in TTW 3.x.** "there is no Reputation data on TalesofTwoWastelands.esm, it was an mod for 2.9.4" (2018). "The plans for a Capital Wasteland reputation system in TTW were abandoned, due to the scarcity of NPCs" (Risewild, 2019; TTW-FORUM t=6896).
- In the 2.x alpha, the optional `TTW_Reputation` plugin had: Arefu, Big Town, Lyons' Brotherhood of Steel, Brotherhood Outcasts, Canterbury Commons, The Enclave, Little Lamplight, Megaton, Oasis, Paradise Falls, Regulators, Rivet City, Roy Phillips, Talon Company, Tenpenny Tower, Underworld (TTW-XML). In 2015 the only "official" one was Brotherhood Outcasts, from Outcast trading (t=1750); its status in 3.4 is **UNVERIFIED**.
- A community **"TTW Reputations"** mod (by Kazopert) re-adds these zones (Nexus; seen in search results only).
- **Recommendation:** the dashboard's DC reputation panel should show "none" unless REPU records from mods exist, and should enumerate REPU forms at runtime.

### 5e. Companions and the companion wheel
- **DC (FO3):** Butch, Charon, Clover, Dogmeat, Fawkes, Jericho, Sergeant RL-3, Star Paladin Cross. TTW gives them the **companion wheel and companion perks**: Tunnel Snakes Rule, The Contract, Get Lucky, Man's Best Friend, Over-Compensation, Scrappy Survivor, Drill Sergeant, Second Skin (TTW-FAQ, H).
- **Mojave:** Arcade, Boone, Cass, ED-E, Lily, Raul, Rex, Veronica. DLC: Dead Money's Dog/God, Dean Domino, Christine; Honest Hearts' Follows-Chalk, Waking Cloud; Lonesome Road's ED-E (M; from memory).
- Companions **can cross wastelands**. Cross-world homes include: Arcade → Rivet City lab; Boone → Sniper Shack; Butch → Freeside Kings; Cass → Canterbury Commons; Charon → The Thorn; Clover → Freeside train station; Dogmeat → Gibson scrapyard; ED-E, Rex and Veronica → Megaton; Fawkes → Jacobstown; Jericho → Atomic Wrangler; Lily and Raul → Museum of History; RL-3 → Goodsprings; Cross → New Vegas Medical Clinic (TTW-FAQ).
- **Wheel layout** (`menus\companion_wheel_menu.xml`, NVF):
  - 760×760, centred, slightly above centre. Title "Companion Commands" (`&-sCWheelTitle;`) at y 110.
  - 8 radial slices (4 left, 4 right, none at top or bottom): Aggressive/Passive, Use Stimpak, Wait/Follow, Talk, Back Up, Near/Far, Open Inventory, Ranged/Melee.
  - Hub reads "Exit". Preview text and context text in font 3, e.g. "HP 94/285", "Stimpak 84".

---

## 6. Prior art: projects that draw a Pip-Boy outside the game

| Project | What | Takeaways |
|---|---|---|
| **Abacus1829/ayn-dual-screen** (`falloutnv/`), 2026 | **A direct competitor and the closest prior art.** An xNVSE 32-bit DLL that serves a live Pip-Boy web UI over HTTP, **built for the AYN Thor's second screen**, and claims "Works under Tale of Two Wastelands unchanged". It has its own BSA v104 reader, DXT decoder and PNG encoder, and serves icons from the user's archives ("1153 Pip-Boy icons … zero failures") | Licence is **source-available, not open source**, so don't reuse its code. Its UI uses **FO4-style tabs (STAT/INV/DATA/MAP/RADIO)** and **system monospace fonts**, synthesises sounds in the browser, draws the world map **only on the Mojave texture** (hardcoded `wasteland_nv_1024_no_map`), and its radio is "still not right". Those are exactly the gaps a faithful TTW-specific UI would fill. Two pitfalls it documents: the `greenscreen.dds` trap and the status-figure location. https://github.com/Abacus1829/ayn-dual-screen |
| robojan/RealPipboyNV + RealPipboyApp (2015) | NVSE plugin plus an Android (libGDX) app showing the FNV Pip-Boy with game assets (squish/zlib/libpng) | An old reference for data model and karma/map icon mapping. https://github.com/robojan/RealPipboyNV |
| LootJunkieAzza/NVForever (GPL-3.0) | Re-implementation; its **spec notes are the best layout spec found** (pixel positions, fonts, colours, tick counts, sounds) | Use as a design spec; verify against your own screenshots |
| sticks-stuff/pipboy_radio_new_vegas | Android Pip-Boy Radio-tab recreation built from FNV's `map_menu.xml`/`globals.xml` | Shows that driving layout from the real menu XML works |
| qwindelzorf/raspipboy (fork of Neal Corbett's) | Raspberry Pi FO3 Pip-Boy in Python/pygame, using `icon_map_*` art | FO3 look; hobby-hardware reference |
| The Wand Company Pip-Boy 3000 replica (official, FO3+FNV) | IPS LCD "emulat[ing] a vintage cathode ray tube"; over 2,000 menu entries; **world maps of the Capital Wasteland and the Mojave**; FM radio. Pre-sale, shipping June 2026 | Official two-world Pip-Boy, useful as a visual reference. https://fallout.bethesda.net/en-US/news/fallout-pip-boy-3000-replica |
| Bethesda Fallout 4 Pip-Boy app | FO4 only | Origin of the FO4 tab style; **not** the FNV look |
| Rainmeter / web skins | None worth citing were found | — |

---

## 7. Biggest unknowns and what to verify on a live TTW 3.4 install
1. **Whether the 2015 world-switch logic still holds in 3.4**: worldspace lists, `TTWWorldLocation` bits, and DLC worlds inheriting the flags. Check `bInDCWasteland`/`bInNVWasteland` with the console in each DLC and in Union Station / Columbus Circle.
2. **Which archive wins for each path under TTW 3.4.** TTW rebuilds the vanilla-named BSAs with different entry counts. Build a real index and check the menu XMLs, fonts, HUD textures, the Capital Wasteland map and the karma icons against it.
3. **Karma title set under TTW** (FO3 or FNV titles; one set or per world) and whether TTW overrides `sKarmicTitle*`.
4. Default General-tab mode (Reputation or General) and the exact game-setting strings (`sStatsCrippled`, `sPCMenuHint*`, tab names) that are missing from the ESMs.
5. World-map ICON paths for Sierra Madre, Big MT and the Divide, and whether `Wasteland_1024_no_map.dds` is TTW's or FO3's art in 3.4.
6. The 3.4 first-trip date values, and the trip length (the alpha used 14 days).
7. How the Pip-Boy INI effects (`fBlur*`, `fScanlineScale*`, `fBrighten*`, vertical hold, shudder) map to pixels. Calibrate from captures.
8. Whether TTW's `TTWPCMiscStat*` globals appear anywhere in the in-game UI.
9. Under MO2, how an out-of-process companion would reach the virtual Data folder. It probably can't, which argues for an xNVSE plugin.
