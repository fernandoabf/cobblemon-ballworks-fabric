# Cobblemon Ballworks — Fabric 1.21.1 port

Pixelmon-style Poké Ball forging for **Cobblemon 1.8.2** on **Fabric 1.21.1**: cook apricorns, craft
discs, put a disc on the anvil and **hold left-click with a hammer** until it becomes a lid, then
assemble lid + stone button + iron base into a Poké Ball. A furnace-like **Mechanical Anvil** does
the hammering automatically.

This is a port of [Cobblemon Ballworks](https://github.com/cxaiverb/CobblemonBallworks) by
**xaiver (cxaiverb)**, originally written for Forge 1.20.1. The goal of the port is to be as close
as possible to the classic Pixelmon (1.7.10–1.12.2) mechanics, which were researched from the wiki,
the community and the original game files (see [`docs/pesquisa/`](docs/pesquisa)).

**How to play** (Portuguese): [`docs/COMO-JOGAR.md`](docs/COMO-JOGAR.md). Specification and design
decisions: [`docs/PLANO.md`](docs/PLANO.md).

## Features

- **Every Cobblemon ball that has a recipe (46)** is forged: Poké, Great and Ultra Balls, the apricorn
  balls, Park, Dream, Beast, Master and all the Ancient balls. Cobblemon's own ball recipes are
  disabled and come back as **disc** recipes with the same shape and materials (copper, iron, gold,
  diamond, netherite...), using **cooked** apricorns. Cherish and Ancient Origin have no recipe in
  Cobblemon and still have none.
- **Poké Ball Anvil**: right-click a disc onto it and hold left-click with a hammer. The piece goes
  through 3 work stages (force 5 per stage; Dream Ball 10; Master and Beast Ball 15) and becomes a
  lid; iron discs become iron bases. Lid + stone button + iron base = one ball.
- **18 hammers**: wood, stone, iron, gold, diamond, aluminium, ruby, sapphire, amethyst and the eight
  evolution stones with Pixelmon's strength and durability, plus a netherite hammer (strength 8,
  upgraded from the diamond one at a smithing table like vanilla netherite tools).
- **Mechanical Anvil**: input, fuel and output like a furnace, works with hoppers, 40 ticks per force
  (10 s for a regular disc).
- **EMI** (optional): a category for each anvil, with the strikes every hammer needs.
- Server options in `config/cobbleballworks.json`; English and Brazilian Portuguese.

## Installing

Put the jar in `mods/` on **both the server and the clients**, next to Fabric API and Cobblemon 1.8.x
(Cobblemon already bundles fabric-language-kotlin). **Remove Cobblemon Parts Refabricated**: it
overrides the same Poké Ball recipes.

## Versions

| Component | Version |
| --- | --- |
| Minecraft | 1.21.1 (official Mojang mappings) |
| Fabric Loader | 0.19.5 (mod requires >= 0.16.10) |
| Fabric API | 0.116.17+1.21.1 |
| Cobblemon | 1.8.2+1.21.1 |
| EMI (optional) | 1.1.24+1.21.1 |
| fabric-language-kotlin | 1.13.6+kotlin.2.2.20 (bundled inside Cobblemon) |
| Gradle / Loom / Kotlin | 8.14.3 / 1.13.6 / 2.2.20 |
| Java | 21 |

## Building

You need a **JDK 21**.

```bash
./gradlew build         # -> build/libs/cobbleballworks-<version>.jar (drop it in mods/)
./gradlew runGametest   # headless dedicated gametest server with the real Cobblemon; exits by itself
./gradlew genSources    # decompiled Minecraft 1.21.1 sources (Mojang mappings) for reference
xvfb-run -a -s "-screen 0 1280x720x24" ./gradlew runShowcase   # client screenshots, see below
```

The first configuration downloads the official Cobblemon 1.8.2 jar from Modrinth into `run-libs/`
(git-ignored, SHA-1 checked). It is only used at dev runtime (gametests, showcase); the mod itself
does not compile against Cobblemon classes. EMI is a compile-only API dependency plus a dev runtime.

`runShowcase` starts the real client (Cobblemon and EMI included) with a dev-only mod that builds
the anvils in a flat world, opens the screens and saves screenshots to
`build/showcase/screenshots/`, then quits; `-PshowcaseLang=pt_br` takes them in Portuguese. It needs
Xvfb and Mesa on a headless machine.

Before a release, `./gradlew runProductionGametest` runs the same gametests on a production Fabric
server (official installer and launcher, intermediary names, the remapped jar plus the published
Fabric API and Cobblemon jars), and `runProductionShowcase` (under `xvfb-run`) does the showcase on a
production client with the published EMI.

No JDK? `./dgradle.sh <tasks>` runs the same Gradle inside the `gradle:8.14.3-jdk21` Docker image.

## Layout

| Path | Contents |
| --- | --- |
| `src/main` | common/server code + resources (`assets/`, `data/`) |
| `src/client` | client-only code (renderers, screens); Loom forbids `main` from touching `net.minecraft.client` |
| `src/gametest` | headless Fabric gametests (dev-only mod, never shipped) |
| `src/showcase` | dev-only client mod that builds scenes and takes screenshots (`runShowcase`) |
| `tools` | asset scripts: `derive_ball_parts.py` (disc and lid sprites) and `derive_piece_models.py` (3D pieces on the anvils) read Cobblemon's jar; `make_icon.py` (mod icon), `make_netherite_hammer.py` |
| `legacy/forge-1.20.1` | the original Forge sources and build files, kept as porting reference |
| `docs/PLANO.md` | specification of the port (Portuguese) |
| `docs/COMO-JOGAR.md` | player guide with every recipe (Portuguese) |
| `docs/pesquisa` | research notes: classic Pixelmon mechanics, audit of the original assets (Portuguese) |

## Credits and licenses

- Original mod, code and most textures: **xaiver (cxaiverb)** — MIT, see [`LICENSE`](LICENSE).
- Fabric 1.21.1 port: **fernandoabf** — MIT.
- The disc and lid sprites (`textures/item/<ball>_lid.png` and `<ball>_lid_blank.png`) are derived
  from **Cobblemon**'s Poké Ball item textures, © The Cobblemon Team, licensed under the
  [MPL-2.0](https://mozilla.org/MPL/2.0/): each lid is the top half of the ball and each disc is that
  half flattened, generated by [`tools/derive_ball_parts.py`](tools/derive_ball_parts.py). They stay
  under the MPL-2.0 (the notice is also stored inside each PNG). The cooked apricorn models reference
  Cobblemon's apricorn sprites without copying them.
- The 3D pieces drawn on the anvils (`models/piece/`) are the top half of Cobblemon's 3D Poké Ball
  models (`poke_ball_model.json`, `ancient_poke_ball_model.json`), © The Cobblemon Team, MPL-2.0;
  each one references its ball's texture in Cobblemon without copying it. The iron piece uses
  `textures/item/piece/iron_base.png`, Cobblemon's Poké Ball texture recoloured to steel. All of them
  are generated by [`tools/derive_piece_models.py`](tools/derive_piece_models.py) and stay under the
  MPL-2.0.
- The hammer sprites in `assets/cobbleballworks/textures/item` were imported from **Pixelmon** by the
  original author; they belong to their respective authors and are **not** covered by the MIT license.
  `netherite_hammer.png` is the diamond hammer sprite recoloured by
  [`tools/make_netherite_hammer.py`](tools/make_netherite_hammer.py) and stays under the same terms.
  The anvil follows classic Pixelmon but is drawn by this mod (its model uses the vanilla anvil
  textures by reference). This is a non-commercial fan project.
- The mod icon (`assets/cobbleballworks/icon.png`, made by [`tools/make_icon.py`](tools/make_icon.py))
  combines the Poké Ball lid and the iron hammer sprites described above and stays under their terms.
- [Cobblemon](https://cobblemon.com) is developed by the Cobblemon team. Pokémon is © Nintendo / Game
  Freak / The Pokémon Company. This project is not affiliated with any of them.
