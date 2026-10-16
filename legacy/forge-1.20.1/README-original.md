# Cobblemon Ballworks

A small **Cobblemon addon** for Minecraft 1.20.1 / Forge that brings back the
old Pixelmon-style feeling of assembling Poké Balls from parts instead of
crafting one in a single step.

> **Status:** fourth milestone. The uniform **blank → hammered lid →
> assembly** flow covers all 31 Cobblemon balls, and hammering now has a
> first-class in-world home: the **Ball Anvil**, a stateful block with a
> real block entity that stores the work-in-progress and tracks hits. The
> crafting-grid hammering recipes still work — the anvil is an additional
> path, not a replacement.

---

## What this mod currently does

Adds **65 items** total:

| Category | Items |
| --- | --- |
| Tools & shared parts | `ball_hammer` (damageable, 128 uses), `base_blank`, `pokeball_base` |
| Per-ball lid blanks | `<ball>_lid_blank` (×31) |
| Per-ball formed lids | `<ball>_lid` (×31) |

`<ball>` is the Cobblemon ball name minus the `_ball` suffix:
`poke, great, ultra, master, beast, azure, citrine, dive, dream, dusk,
fast, friend, heal, heavy, level, love, lure, luxury, moon, nest, net,
park, premier, quick, repeat, roseate, safari, slate, sport, timer,
verdant`. One creative tab (**Cobblemon Ballworks**) holds all of them.

### The universal crafting flow

The same four-step loop produces every ball:

1. **Base blank.** 3 iron ingots (tag `#forge:ingots/iron`) in a horizontal
   row → **5 × `base_blank`**. Shared across all balls.
2. **Lid blank.** Put the ball's apricorns / outer ingredients into a
   crafting table in the **original shape** (center slot left empty) →
   **5 × `<ball>_lid_blank`**. For example, `poke_lid_blank` takes 4 red
   apricorns at t/l/r/b. `heal_lid_blank` takes 2 pink apricorns (t, b) and
   2 white apricorns (l, r). `beast_lid_blank` uses 4 gold ingots at the
   corners and 4 echo shards at the edges. `master_lid_blank` uses 2
   shulker shells (t, b) and 2 netherite ingots (l, r).
3. **Hammer.** Two shapeless recipes finish the parts:
   - `base_blank` + `ball_hammer` → 1 × `pokeball_base`
   - `<ball>_lid_blank` + `ball_hammer` → 1 × `<ball>_lid`

   The hammer is **not consumed** — `BallHammerItem.getCraftingRemainingItem`
   returns a copy with `damageValue + 1`, so the same hammer goes back into
   the grid until its 128 durability is spent.
4. **Assembly.** Shapeless: `<ball>_lid` + `pokeball_base` + any button
   (tag `#minecraft:buttons`) + **the center ingredient from the original
   recipe** → 1 × `cobblemon:<ball>_ball`. The center ingredient is what
   gives each ball its character:
   - copper ingot: poke, azure, citrine, heal, premier, roseate, safari,
     slate, verdant
   - iron ingot: dive, fast, friend, great, heavy, lure, moon, nest, net,
     park, sport
   - gold ingot: dusk, love, luxury, quick, repeat, timer, ultra
   - level uses iron ingot (from original recipe)
   - dream uses diamond
   - beast uses diamond
   - master uses nether_star

Because every recipe is a stock `minecraft:crafting_shaped` or
`crafting_shapeless`, **JEI shows them automatically**, and any external
crafter (Create mechanical crafter, RS autocrafter, Mekanism formulaic
assemblicator) can run the whole pipeline end-to-end; they all respect the
crafting-remainder hammer too.

This path is **additive** — Cobblemon's stock recipes for all 31 balls are
still intact. Both paths appear in JEI. See “Known limitations” below.

---

## Hammers (19 tiers)

Every hammer is a `BallHammerItem`, which means the crafting-remainder
mechanic (hammer returns to the grid with +1 damage) and the Ball Anvil's
`held.item is BallHammerItem` check work for all of them equally.
Gameplay-wise, **tiers only differ in durability** — a diamond hammer
outlasts a wooden one but does not hammer faster or unlock different
recipes. The item tag `#cobbleballworks:hammers` contains every hammer;
every ball/base hammering recipe accepts that tag, so any hammer is
interchangeable at the crafting table and at the anvil.

Primary progression is wood → stone → iron → gold → diamond. Every
off-track hammer (aluminium, gems, evolution stones) matches iron
durability — they're cosmetic variants of the iron tier, craftable from
whatever material the player has available.

| Tier | Durability | Craft material |
| --- | ---: | --- |
| `wood_hammer` | 60 | `#minecraft:planks` |
| `stone_hammer` | 132 | `#minecraft:stone_crafting_materials` |
| `iron_hammer` | 250 | `#forge:ingots/iron` |
| `gold_hammer` | 500 | `#forge:ingots/gold` |
| `diamond_hammer` | 1024 | `#forge:gems/diamond` |
| `aluminium_hammer` | 250 | `#forge:ingots/aluminum` |
| `amethyst_hammer` | 250 | `minecraft:amethyst_shard` |
| `ruby_hammer` | 250 | `#forge:gems/ruby` |
| `sapphire_hammer` | 250 | `#forge:gems/sapphire` |
| `<stone>_hammer` × 8 | 250 | `cobblemon:<stone>` (Cobblemon evolution stones) |

Every recipe is the standard anvil silhouette `MMM / MSM / " S "` — 5
heads of the material plus 2 sticks — matching the legacy Pixelmon shape.
The ruby/sapphire tiers depend on modpack mods providing
`#forge:gems/<name>` tags; if the modpack doesn't populate them the recipe
simply doesn't resolve, but the item is still registered and accessible
through `/give` or creative.

Evolution-stone hammers (`dawn_stone_hammer`, `dusk_stone_hammer`,
`fire_stone_hammer`, `leaf_stone_hammer`, `moon_stone_hammer`,
`sun_stone_hammer`, `thunder_stone_hammer`, `water_stone_hammer`) craft
directly against Cobblemon's own stone items, so they work out-of-the-box
as long as Cobblemon is loaded.

---

## Automation lanes

Four ways to turn a blank into a formed part. All produce the same items
(`<ball>_lid`, `pokeball_base`) and all are 1-to-1.

| Path | Tier | Requires | Speed per blank |
| --- | --- | --- | --- |
| **Manual** | early-game | Ball Anvil + any hammer | 3 right-clicks |
| **Mekanism Crusher / Crushing Factory** | mid-game | Mekanism 10.4.x | stock Crusher rate |
| **Create Mechanical Press** | mid-game | Create 0.5.1.f | `processingTime: 300` ticks (≈15 s / blank) |
| **Mek: More Machine Stamping Factory + Ball Mold** | industrial | Mekanism: More Machine + Tinkers' Construct | stock Stamper rate, mold reusable |

Both automation lanes are **optional** — every recipe is wrapped in a
`forge:conditional` + `forge:mod_loaded:<modid>` check, so recipes
are silently dropped when the upstream mod isn't installed (no "unknown
recipe type" log warnings, no crashes). With both mods present, JEI picks
up each lane's recipes in its native category.

### Mekanism (Crusher / Crushing Factory)

Feed a blank into the Crusher input slot, the formed part comes out the
output. Works identically with all Crushing Factory upgrade tiers. 32
recipes cover every `<ball>_lid_blank` → `<ball>_lid` pair plus the
shared `base_blank` → `pokeball_base`.

### Mekanism: More Machine + Tinkers' Construct (Stamping Factory + mold)

The industrial "preferred" path uses a **reusable Ball Mold** forged at a
Tinkers' Construct casting table, then fed into Mekanism: More Machine's
Stamping Factory alongside blanks.

**Making the mold (one-time setup):**

1. Shape one Poké Ball part the manual way (Ball Anvil + hammer, or any
   of the other automation lanes) so you have any `<ball>_lid` or a
   `pokeball_base` in hand.
2. Place that formed part on a Tinkers' Construct **casting table**.
3. Pour **144 mB of molten steel** (`tconstruct:molten_steel` — one
   ingot's worth) over it.
4. After ~6 seconds of cooling, the casting table produces a reusable
   **Ball Mold** (`cobbleballworks:ball_mold`). The formed part you
   placed as the cast is consumed into the mold.

The casting recipe is **tag-gated** on `#cobbleballworks:formed_ball_parts`,
which contains every `<ball>_lid` and `pokeball_base`. One recipe, any
formed part works — no need for 32 separate casting entries.

**Using the mold (automation loop):**

- `<ball>_lid_blank` + `ball_mold` in Mekanism: More Machine's **Stamping
  Factory** → `<ball>_lid` (for every ball), and `base_blank` + `ball_mold`
  → `pokeball_base`.
- The mold goes in the Stamper's **mold slot**, the blank in the main
  input. The recipe JSON uses the `mekmm:stamper` type with separate
  `input` / `mold` / `output` fields — the Stamper's dedicated `mold`
  slot is a strong indication the machine treats it as a
  catalyst/non-consumed input, not a second consumable.
- `BallMoldItem` also overrides `Item.hasCraftingRemainingItem` to
  return a copy of itself as a safety net — any processor that also
  respects the vanilla crafting-remainder hook will hand the mold back
  undamaged too.

32 stamping recipes ship: 31 per-ball lids + 1 shared base.

### Create (Mechanical Press over Depot or belt)

Place a blank on a Depot under a Mechanical Press, or dump blanks on a
belt running under the press. Each blank gets pressed once and becomes
the formed part.

**About "3 BONKs":** Create's base Mechanical Press plays a single press
animation per recipe. `processingTime: 300` (3× Create's default) makes
the press cycle last ~15 seconds per blank so it *feels* like three
bonks' worth of work, but the press will only visibly bonk **once** per
blank before producing output. True multi-press visuals would require
Create's **Sequenced Assembly** recipe type, which needs extra
intermediate items — deliberately not added here because it would muddy
the item registry. That's a reasonable future milestone if the
three-bonk feel needs to be visually explicit.

---

## The Ball Anvil

A pickaxe-minable workstation block (`cobbleballworks:ball_anvil`) that
replaces the crafting-grid hammering step with a focused, in-world
interaction. The grid recipes still exist for automation, but the anvil is
the intended manual path and it feels much more like Pixelmon's old
forge-a-ball loop — including the model itself, which is the legacy
Pixelmon anvil reconstructed in 1.20.1-compatible block-model JSON from
the export bundle. See "Visual assets" below. The anvil faces the player
on placement via a `FACING` block state (north/east/south/west variants).

### Crafting the anvil

```
iii
 i
iii
```

Seven iron ingots (tag `#forge:ingots/iron`) in an anvil silhouette → 1 ×
Ball Anvil. It drops itself when broken with an iron-or-better pickaxe.

### Using the anvil

Place it, then stand next to it and interact with the item in hand:

| Hand contents | Anvil state | Right-click does |
| --- | --- | --- |
| A supported blank (any `_lid_blank` or `base_blank`) | Empty | Loads one blank onto the anvil, consumes one from your stack (creative: not consumed). |
| `ball_hammer` | Holds a blank | Advances progress by 1, damages the hammer by 1 durability, plays an anvil clang, shows `Hammering... N/3` on your actionbar. |
| `ball_hammer` | Holds a finished part or is empty | Passes through (no damage, no progress). |
| Empty hand | Holds a finished part | Takes the finished part into your inventory. |
| Empty hand + **sneak** | Holds a blank with any progress | Takes the blank back, resets progress (safety — in case you loaded the wrong thing). |

Default conversion cost is **3 hammer hits** for every supported blank
(`base_blank` → `pokeball_base`, `<ball>_lid_blank` → `<ball>_lid`). The
value lives in one constant in `AnvilRecipes.kt`.

### What the anvil supports today

Every blank the 31-ball crafting flow produces. The lookup is primed off
`BallRegistry` + the shared `base_blank`, so adding a future ball to
`BallRegistry` automatically makes its new lid blank hammer-able at the
anvil — no code change needed.

### Safety / persistence

- The stored blank and progress are saved to NBT and resync to clients via
  the standard block-entity update packet.
- Breaking the anvil with an in-progress blank inside drops that blank as
  an item entity — even in creative — so nothing is silently destroyed.
- Swapping blanks mid-work is explicitly blocked: a loaded anvil rejects a
  second blank. Sneak-right-click with an empty hand to recover the blank
  first.

---

## Visual assets

The Ball Anvil's block model, item model, blockstate, and anvil texture
are imported directly from the legacy-Pixelmon asset export and live under
the `cobbleballworks` namespace. Layout:

```
assets/cobbleballworks/
├── blockstates/ball_anvil.json           # 4 facing variants (N/E/S/W)
├── models/
│   ├── block/
│   │   ├── ball_anvil.json               # 24 cuboid elements, snapped
│   │   └── anvil_states/                 # 90 work-state models, STAGED
│   │       ├── <ball>_disc_flat.json
│   │       ├── <ball>_disc_stage1.json
│   │       ├── <ball>_disc_stage2.json   # (28 balls)
│   │       ├── iron_disc_flat.json
│   │       ├── aluminum_disc_flat.json
│   │       ├── aluminium_ingot.json
│   │       └── aluminium_plate_stage{1,2,3}.json
│   └── item/
│       ├── ball_anvil.json
│       └── anvil_states/                 # 23 lid item models, STAGED
│           └── <ball>balllid.json
└── textures/
    ├── block/
    │   ├── ball_anvil.png
    │   ├── anvil_progress/               # 28 disc progress PNGs
    │   └── anvil_states/                 # iron/aluminum disc + plate/ingot
    └── item/
        ├── ball_anvil.png
        └── lids/                         # 23 lid PNGs
```

"STAGED" means the files are on disk in the correct mod namespace with
every `pixelmon:` reference rewritten to `cobbleballworks:`, but they are
not yet plugged into any renderer — `BallAnvilBlockEntity` simply exposes
`storedItem` + `progress` for a future BER that picks the right disc
model + progress texture per ball. Adding that renderer is now purely a
client-side task with no further asset work needed for the 28 balls the
export covers.

### Block property: `noOcclusion`

The rebuilt 4-element anvil doesn't fill its 16³ block volume — there are
gaps around the waist, between the horn and the top plate, and under the
base's footprint. Without `noOcclusion()` in the block properties, MC
assumes the block is a full opaque cube and **culls neighbor blocks' faces
against it**, creating see-through gaps where our geometry doesn't exist.
`ModBlocks.BALL_ANVIL` sets `.noOcclusion()` so the game knows we're not
a full cube; neighbor faces render normally and the anvil looks solid.
Collision + light-occlusion stay at the full-cube defaults, which is the
intended behavior for a workstation.

### Model rebuild notes

The first integration tried to use the Pixelmon export's block-model JSON
directly. It rendered as broken — stretched, sliced, floating geometry —
because that JSON is a straight conversion from a 1.12.2 TESR
`ModelRenderer`: Y was inverted (the "base" sat at y=22-24, outside block
bounds), the horn extended ~8 units past the block on -X (lighting
artifacts and chunk-boundary seams), and many small bars relied on
rescaled ±45° rotations that distort heavily.

So the Ball Anvil block model was **rebuilt from scratch** as a clean
four-element 1.20.1 block model — base (y=0-3, 12×12 footprint), waist
(y=3-9, 6×6 pillar), top (y=9-14, 12×12 plate), and a small horn nub on
-Z (y=10-13, 6×3×2 protrusion). Every element stays inside `[0,16]³`, no
rotations are used, and the model inherits `minecraft:block/block` so
inventory / first-person / ground display transforms come for free. The
silhouette stays Pixelmon-inspired — wide base, narrow waist, wide top
plate with a forward horn — without trying to match the legacy model
1-to-1.

The imported 90 work-state models under `models/block/anvil_states/`
(disc stages, ingot, plate) were not part of this bug — they're still
staged for the future BER. Some of them likely share the same TESR
coordinate issues; the recommendation for the BER milestone is to rebuild
them the same way (small clean cuboids within `[0,16]³`) using the
imported textures but not the imported geometry.

### Balls covered by the work-state assets

The export covers 24 of the 31 balls in `BallRegistry`:

> beast, dive, dream, dusk, fast, friend, great, heal, heavy, level,
> love, lure, luxury, master, moon, nest, net, park, poke, premier,
> quick, repeat, safari, sport, timer, ultra

Plus **cherishball** and **gsball** models which the mod doesn't register
yet (they're staged for if we ever add them). Not covered: **azure,
citrine, roseate, slate, verdant** — these have no legacy-Pixelmon
equivalent; the future BER will need a fallback disc model (a simple
colored-disc `cube_all` is fine) for those five.

---

## Target versions

Exactly these:

- **Minecraft:** 1.20.1
- **Forge:** 47.2.8
- **Java:** 17
- **Cobblemon (Forge):** `com.cobblemon:forge:1.4.0+1.20.1`
- **Kotlin for Forge:** 4.10.0
- **Kotlin JVM:** 1.9.22

Designed to run alongside Kotlin for Forge, Architectury, JEI, CraftTweaker,
Create, Mekanism, Refined Storage, etc. No hard dependency on any of those
(only Cobblemon, Forge, and Kotlin for Forge are mandatory).

---

## Import, build, run

### Open in IntelliJ IDEA

1. **File → Open…** and select this folder.
2. IntelliJ will detect the Gradle project and import it. Accept.
3. When prompted, point it at a **Java 17 JDK** (Temurin, Adoptium, Liberica,
   or the JetBrains Runtime all work).
4. Once Gradle sync finishes, refresh Gradle once more so the
   `genIntellijRuns` task becomes available.

### Generate run configurations

From the Gradle tool window, run:

```
Tasks → forgegradle_runs → genIntellijRuns
```

This creates **runClient**, **runServer**, and **runData** IntelliJ run
configs. Pick **runClient** to launch a dev Minecraft with your mod loaded.

### Build a shippable jar

```bash
./gradlew build
```

Output lands in `build/libs/cobbleballworks-<version>.jar`. Drop that jar into
a 1.20.1 Forge 47.2.8 instance that already has Cobblemon 1.4.0 and Kotlin for
Forge 4.10.0 installed.

### Run a dev client from the CLI

```bash
./gradlew runClient
```

The first run takes a while — ForgeGradle has to download, decompile, and
reobfuscate Minecraft. Subsequent runs are fast.

---

## Testing the Poké Ball loop in-game

There are now two manual paths end-to-end: a crafting-grid path (good for
JEI lookup / automation) and the Ball Anvil path (intended player
experience). Both produce the same parts and the same Cobblemon balls.

**Ball-anvil path** for one Poké Ball:

1. Make a Ball Anvil (7 iron, anvil-shape), place it, stand next to it.
2. Make a base blank (3 iron ingots in a row → 5 `base_blank`).
3. Right-click the anvil with a `base_blank`; actionbar shows it loaded.
4. Right-click with `ball_hammer` three times; actionbar shows
   `Hammering... 1 / 3 → 2 / 3 → Shaped: Poke Ball Base`.
5. Right-click with empty hand to take the `pokeball_base`.
6. Repeat with 4 red apricorns → 5 `poke_lid_blank`, then 3 hammer strikes
   on the anvil → `poke_lid`.
7. Combine `poke_lid + pokeball_base + button + copper_ingot` in a crafting
   table → `cobblemon:poke_ball`.

**Crafting-grid path** (shorter, no anvil needed):

1. `/gamemode creative` in a dev world.
2. Grab (or `/give`) some **red apricorns**, **iron ingots**, **copper
   ingots**, and a **button**.
3. Open JEI and type e.g. `poke_lid_blank` to see the recipe tree.
4. Craft the **base**: 3 iron ingots in a row → 5 × `base_blank`. 1 ×
   `base_blank` + `ball_hammer` → 1 × `pokeball_base`.
5. Craft the **lid**: 4 red apricorns in t/l/r/b positions (center empty) →
   5 × `poke_lid_blank`. 1 × `poke_lid_blank` + `ball_hammer` → 1 ×
   `poke_lid`.
6. **Assemble**: `poke_lid` + `pokeball_base` + any button + 1 copper ingot
   (shapeless) → **1 × `cobblemon:poke_ball`**.
7. Try to catch something to confirm it's the real Cobblemon item.

Every other ball works identically — only the apricorn colors and the
center ingredient change. JEI is the fastest way to see each one.

Tip: the hammer is damageable and takes 1 durability per hit in both
paths. Crafting-grid hammering costs 1 use per blank (so ~2 uses per ball).
Anvil hammering costs 3 uses per blank by default (so ~6 uses per ball,
~21 balls per 128-use hammer) — tweak `DEFAULT_LID_HITS` / `DEFAULT_BASE_HITS`
in `AnvilRecipes.kt` if that feels off.

---

## Project layout

```
.
├── build.gradle
├── settings.gradle
├── gradle.properties
├── gradle/wrapper/…             # Gradle 8.8 wrapper
├── gradlew, gradlew.bat
├── src/main/
│   ├── kotlin/com/xaiver/cobbleballworks/
│   │   ├── CobbleBallworks.kt         # @Mod entrypoint
│   │   ├── registry/
│   │   │   ├── BallRegistry.kt        # single list of ball short ids — source of truth
│   │   │   ├── ModItems.kt            # shared items + per-ball blanks & lids
│   │   │   ├── ModBlocks.kt           # ball_anvil block + its BlockItem
│   │   │   ├── ModBlockEntities.kt    # ball_anvil BlockEntityType
│   │   │   └── ModCreativeTabs.kt     # populates the creative tab from BallRegistry
│   │   ├── item/
│   │   │   └── BallHammerItem.kt      # damageable hammer, returns with +1 damage per craft
│   │   ├── block/
│   │   │   ├── BallAnvilBlock.kt      # interaction logic + drop-on-break
│   │   │   ├── BallAnvilBlockEntity.kt# stored-item + progress + NBT + sync + BER accessors
│   │   │   └── AnvilRecipes.kt        # blank → finished lookup, primed from BallRegistry
│   │   ├── client/
│   │   │   ├── BallAnvilRenderer.kt   # BlockEntityRenderer — workpiece on top of anvil
│   │   │   └── ClientSetup.kt         # client-only wiring, dist-guarded from mod entry
│   │   ├── data/                       # (empty, for future data-gen)
│   │   └── compat/                     # (empty, for future JEI/Create/Mek)
│   └── resources/
│       ├── META-INF/mods.toml
│       ├── pack.mcmeta
│       ├── assets/cobbleballworks/
│       │   ├── lang/en_us.json
│       │   ├── models/item/*.json
│       │   └── textures/item/ (user-supplied PNGs go here)
│       └── data/cobbleballworks/recipes/*.json
```

---

## Known limitations / TODOs

- **Item textures** ship for every lid_blank, lid, base_blank,
  pokeball_base, ball_anvil, and all 18 tiered hammers. The 31 per-ball
  lid+blank sprites were procedurally derived from Cobblemon's ball
  sprite sheets; the 18 tiered hammer PNGs were imported from the user's
  Pixelmon hammer bundle. Also staged for the future BER: 23 legacy
  Pixelmon lid PNGs under `textures/item/lids/` and the full anvil-progress
  work-state set.
- **Block-entity renderer shipped** (`client/BallAnvilRenderer.kt`). The
  stored workpiece now renders flat on top of the anvil, aligned with the
  block's FACING and scaled linearly from 0.5× (fresh blank) to 0.75×
  (fully formed), with a small progress-driven y-lift. The BER consumes
  three read-only accessors on `BallAnvilBlockEntity` — `storedItem`,
  `progressRatio`, `requiredHits` — all synced via the standard BE
  update packet. **The 90 imported Pixelmon work-state disc models under
  `models/block/anvil_states/` remain staged but unused**; using them
  live requires loading them through `ModelBakery` and rendering via
  `BlockRenderDispatcher`, which is a follow-up milestone.
- **Aluminum support — TODO.** `base_blank` currently accepts only
  `#forge:ingots/iron`. To extend, switch the key to a multi-entry
  ingredient list: `"i": [{"tag": "forge:ingots/iron"}, {"tag":
  "forge:ingots/aluminum"}]`. One line of JSON once you pick which
  aluminum mod populates the tag.
- **Cobblemon's stock ball recipes are *not* removed** for any of the 31
  balls — the new paths are all **additive**, so both show up in JEI. To
  force players down the parts route globally, pick one:
  1. Ship a datapack that overrides each `cobblemon:recipes/<ball>.json`
     with an empty / impossible recipe. `data/cobblemon/recipes/` mirrors
     our 31 file names.
  2. Use CraftTweaker (already in the modpack) with one
     `craftingTable.removeRecipe(<item:cobblemon:<ball>>);` line per ball.
  3. Wrap our `<ball>.json` recipes and the remove-recipes in a Forge
     `forge:conditional_recipe` behind a config flag.
- **Economy shift.** Most Cobblemon ball recipes produced 4 balls per craft
  from 4 apricorns + 1 metal. The Ballworks chain produces 5 lid blanks
  from those same 4 apricorns, so per-ball **apricorn cost is 20% lower**,
  but each ball also needs its own button + base + center ingredient +
  hammer damage, so **metal/gem cost goes up** (master is a 5× buff on the
  rare parts, beast is a 0.625× nerf). Intentional.
- **Beast and Master balls are edge cases.** They had no apricorns in the
  originals, so the converter treated the outer ring as the "lid material"
  and the center as the "special": `beast_lid_blank` uses 4 gold + 4 echo
  shards; `master_lid_blank` uses 2 shulker shells + 2 netherite ingots.
  These are the only two balls where the lid is non-apricorn — the anvil
  still shapes them with 3 hits like every other blank.
- **Anvil stores a single stack.** By design for this milestone: no hopper
  side, no sided inventory, no queueing multiple blanks. Sneak-right-click
  with an empty hand to recover the current blank safely.
- **No machine integration yet** — but the crafting-grid hammering recipes
  still work through Create mechanical crafters, RS autocrafters, Mekanism
  formulaic assemblicators, and anything else that respects vanilla
  crafting-remainder items. No Ballworks-specific machines or automation
  hooks yet.

---

## Next suggested milestones

1. **Wire Pixelmon work-state discs into the BER.** The basic BER is
   live and draws the `storedItem` flat on top of the anvil with a
   progress-based scale. Next step: swap the item-model render for the
   imported `anvil_states/<ball>_disc_{flat,stage1,stage2}` block models
   (staged under `models/block/anvil_states/`). Build a mapping table
   from `BallRegistry` short id (e.g. `poke`) to Pixelmon filename
   (`pokeball`) with a fallback for the 5 non-Pixelmon balls
   (azure, citrine, roseate, slate, verdant); pick a model by mapping
   `progressRatio` to `flat`/`stage1`/`stage2`; render via
   `Minecraft.getInstance().modelManager.getModel(...)` through
   `BlockRenderDispatcher`. The BE already exposes every accessor the
   lookup needs.
2. **`ball_hammer.png`.** The one remaining missing item texture. Everything
   else — 31 lid blanks, 31 lids, base_blank, pokeball_base, and the
   ball_anvil — already ships. Drop a 16×16 handheld-tool sprite at
   `textures/item/ball_hammer.png`.
3. **Aluminum alt-path.** Two-line change to `base_blank.json` — see the
   TODO above. Leave iron as the primary tag, add aluminum as the second
   entry in the ingredient list.
4. **Recipe removal.** Pick one of the three options above so Cobblemon's
   stock ball recipes are genuinely *replaced*, not just supplemented.
5. **Tier the base.** Optional: gold base for apricorn balls using
   gold-ingot-center recipes, netherite base for master, etc. Keeps the
   same anvil + hammer mechanic.
6. **Create Sequenced Assembly for true multi-bonk pressing.** The stock
   `create:pressing` lane shipped in this milestone is single-BONK per
   blank. For a truly three-press visual, register a
   `create:sequenced_assembly` recipe that takes a blank through N
   pressing steps. Requires adding N-1 intermediate "partial-lid" items
   (e.g. `<ball>_lid_quarter`, `<ball>_lid_half`), which is why it wasn't
   done now. Data-only still, same `forge:conditional` wrapper.
7. **Mekanism — additional recipe lanes.** Crushing ships now; natural
   follow-ups are Metallurgic Infuser (for ingot-based base recipes) and
   Osmium Compressor. Same data-only + `forge:conditional` pattern.
8. **Anvil upgrades.** Directional facing, hit-sounds tied to material,
   optional hopper-in/out for automation, or a two-slot "load queue" —
   but not before the BER lands, since all of those want visual feedback.
9. **JEI plugin.** Tiny plugin so hammering appears as its own category
   instead of a generic shapeless recipe, and with the anvil as the
   workstation icon.
10. **Data-gen.** Replace the hand-authored recipe JSONs with a
    `RecipeProvider` run from the `data` config. Pays off as more balls
    or apricorn colors land.

---

## Dependency sources (assumptions I had to make)

- **Cobblemon Maven** is at
  `https://maven.impactdev.net/repository/development/`. I verified
  `com.cobblemon:forge:1.4.0+1.20.1` is present by fetching
  `…/com/cobblemon/forge/maven-metadata.xml`.
- **Kotlin for Forge** is at
  `https://thedarkcolour.github.io/KotlinForForge/`. 4.10.0 is the
  widely-used 1.20.1 release.
- **ForgeGradle** 6.0.16+ is used via `id 'net.minecraftforge.gradle'
  version '[6.0.16,6.2)'`, pulled from
  `https://maven.minecraftforge.net/`.
- **Gradle wrapper** points at distribution `gradle-8.8-bin.zip` (ForgeGradle
  6 supports 8.1.1+; 8.8 is a safe modern choice).
- **Kotlin JVM plugin** pinned to 1.9.22 to match KFF 4.x's Kotlin runtime.

If any of these maven hosts move, update the `repositories {}` block in
`build.gradle`.

---

## License

MIT.
