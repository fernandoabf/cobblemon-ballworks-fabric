# Auditoria estática dos recursos — Cobblemon Ballworks (Forge 1.20.1) → port Fabric 1.21.1

Fonte auditada: `ballworks-orig/src/main/resources` (commit `e523ae3`, "Initial commit: Cobblemon Ballworks 0.1.0"), mais os registros em `ModItems.kt`, `ModBlocks.kt`, `BallRegistry.kt`, `ModCreativeTabs.kt` e `BallAnvilBlockEntity.kt`. Referências: o jar `Cobblemon-fabric-1.8.2+1.21.1.jar` (copiado para o scratchpad, sha256 `cae8ac32…`, idêntico ao da instância) e os 119 jars da pasta `mods` da instância (copiados; o scan abre também os jars aninhados em `META-INF/jars`).

Método: tudo checado por script (`python3 -I`), de forma exaustiva — nada foi amostrado e nada do clone foi executado. Os scripts estão em `scratchpad/audit-scripts/` e os dados brutos em `scratchpad/audit-out/` (ver Apêndice B).

## 0. Resumo

| Tema | Resultado |
|---|---|
| Inventário | 558 arquivos: 416 JSON, 139 PNG, 1 `.mcmeta`, 1 `.toml`, 1 `.txt`. Itens registrados: 83 (17 martelos, 31 `*_lid_blank`, 31 `*_lid`, `base_blank`, `pokeball_base`, `ball_mold`, `ball_anvil`). |
| Itens: modelo + textura | 83/83 OK (0 sem modelo, 0 sem textura). |
| Referências quebradas | **23**: todos os modelos `models/item/anvil_states/<bola>balllid.json` apontam para `item/lids/<bola>balllid`, que não existe (os arquivos são `lids/<bola>_lid.png`). Os 90 modelos de `models/block/anvil_states` resolvem todas as texturas. |
| Lang (en_us) | 86 chaves, **0 faltando**, 0 sobrando (itens, bloco, aba criativa, `ball_anvil.progress`/`finished`). Só existe `en_us`. |
| Receitas | 210 = **113 de crafting vanilla** (50 shaped + 63 shapeless) + **97 de mods ausentes** (32 Create, 32 Mekanism, 32 MekMM, 1 Tinkers). Todas as 113 usam `result.item` (o 1.21.1 exige `result.id`); 8 referências `forge:` → `c:` em 8 arquivos; 46 ids `cobblemon:` conferidos, **todos existem** no 1.8.2. 0 conflitos de grade contra 5265 receitas de crafting (pack + vanilla 1.21.1) e 0 conflitos internos. |
| 124 receitas `*_lid` | 31 bolas × 4 rotas (grade, Create, Mekanism, MekMM). Só **31** são o martelo na grade. |
| Geometria Pixelmon | Todos os 113 modelos dentro de [-16, 32] (carregam). Mas todos têm a mesma origem deslocada: centro XZ em (6.5, 0), a **8.14 px** do centro (8, 8) do bloco; a base fica em y = 11.7/11.8, **3.2–3.3 px abaixo do topo da bigorna (y = 15)**, e o Z vai de −1.9 a 1.9 (sai pela face norte). Translação: **(+1.5, +3.3, +8)** nos discos flat, **(+1.5, +3.2, +8)** nos stage1/stage2 e tampas. O UV automático amostra 33% da área fora do sprite e, nas folhas box-UV (26 das 28 texturas), 45–59% em pixel transparente. |
| Bola → modelos Pixelmon | 26/31 com flat + stage1 + stage2. **Sem disco:** azure, citrine, roseate, slate, verdant. Sem modelo de tampa (`balllid`): esses 5 + beast, dream, park. |
| Texturas | 139 PNG válidas (assinatura, CRC de todos os chunks, IHDR, IDAT descompactado com o tamanho certo, IEND, `PIL.load()`): **0 inválidas**. 110 são 16×16; 29 não são (28 em `anvil_progress`: 8× 32², 18× 128², 2× 512²; mais `anvil_states/aluminium_ingot` 32²). Nenhum `.png.mcmeta`. As 23 de `item/lids/` são cópias byte a byte de `item/<bola>_lid.png`. |

### Problemas bloqueantes para o port (1.21.1 / nosso pack)

1. **As pastas de dados mudaram de nome no 1.21**: `recipes/`→`recipe/`, `loot_tables/`→`loot_table/`, `tags/items/`→`tags/item/`, `tags/blocks/`→`tags/block/`. Do jeito que estão, as 210 receitas, a loot table da bigorna e as 6 tags são ignoradas em silêncio: não existe nenhuma receita, a bigorna quebra sem dropar, `#cobbleballworks:hammers` fica vazia e a picareta deixa de ser a ferramenta certa (mineração lenta e sem drop).
2. **`"result": {"item": …}` → `"result": {"id": …}` nas 113 receitas de crafting.** O codec de ItemStack do 1.21.1 exige `id`; sem a troca, todas falham no parse. Os ingredientes `{"item": …}` e `{"tag": …}` continuam válidos no 1.21.1, como mostram as receitas do próprio Cobblemon 1.8.2.
3. **Tags `forge:` → `c:`** (8 referências): sem isso, `base_blank`, `ball_anvil` e os martelos de ferro, ouro e diamante não têm receita que funcione. Como não dá para fazer `base_blank` nem a bigorna, a cadeia inteira trava. Mesmo depois de converter, `c:ingots/aluminum`, `c:gems/ruby` e `c:gems/sapphire` **não existem no pack**, então os martelos de alumínio, rubi e safira continuam impossíveis de fabricar.
4. **97 receitas de mods que não temos** (Create/Mekanism/MekMM/Tinkers) precisam sair. 65 estão embrulhadas em `forge:conditional`, que o Fabric não entende; as **32 `mekmm:stamper` nem têm condição** (o README diz o contrário). Não derrubam o jogo, mas são 97 erros de parse no log. Sem a receita do Tinkers, o `ball_mold` fica sem receita.
5. **Os 23 modelos `item/anvil_states/*balllid.json` apontam para texturas que não existem.** Se o BER usar esses modelos, aparece a textura roxa e preta.
6. **Os modelos Pixelmon (`anvil_states`) não servem como estão para o BER:** origem deslocada (8.14 px do centro e 3.2–3.3 px **dentro** da bigorna) e UV automático amostrando fora do sprite e em áreas transparentes das folhas box-UV do Pixelmon. Gsball e parkball (512²) nem seguem o layout das outras. Não trava o port se o BER continuar desenhando o sprite do item, como faz hoje. Para usar os modelos, é preciso reescrever as coordenadas e UVs (seção 5).
7. Dependência de código, não de asset: as 32 receitas de martelar na grade dependem de o martelo voltar com +1 de dano. No Forge isso vem de `getCraftingRemainingItem(ItemStack)`; no Fabric 1.21.1 o equivalente é `FabricItem#getRecipeRemainder(ItemStack)`, que existe no fabric-item-api-v1 0.116.17 (conferido no jar, com o `RecipeMixin` que o aplica às receitas). Sem isso, o martelo é consumido a cada craft.

## Pergunta do usuário: "e tem a forja automática também?"

- **No original, não existe uma máquina ou forja automática própria.** A "automação" do Ballworks são só receitas para máquinas de outros mods (README, seção *Automation lanes*):
  - `create:pressing`: Prensa Mecânica do Create sobre Depot ou esteira, `processingTime` 300. São 32 receitas: 31 tampas + 1 base.
  - `mekanism:crushing`: Crusher / Crushing Factory do Mekanism (32 receitas).
  - `mekmm:stamper`: Stamper do Mekanism: More Machine com o item `ball_mold` no slot de molde (32 receitas). O molde sai da Casting Table do Tinkers' Construct: 144 mB de `tconstruct:molten_steel` sobre qualquer peça da tag `#cobbleballworks:formed_ball_parts` (1 receita).
- **Nenhum desses mods está no nosso pack.** Conferi os 119 jars (117 mod ids de topo): não há `create`, `mekanism`, `mekmm` nem `tconstruct`. Mekanism, MekMM e Tinkers são mods (Neo)Forge. Por isso as 97 receitas saem do port e o `ball_mold` perde a função: só serve na Stamper e só é fabricado no Tinkers.
- **O que dá para automatizar no nosso pack:** o *Crafter* vanilla do 1.21 executa receitas de grade. As receitas de martelar (`<bola>_lid_blank` + `#cobbleballworks:hammers` → `<bola>_lid`, e `base_blank` + martelo → `pokeball_base`) são shapeless vanilla, então funcionam nele **desde que o port implemente o remainder do martelo** (item 7 acima). O Crafter **ejeta** o martelo danificado em vez de devolvê-lo à grade, então é preciso um funil para recolocá-lo. Isso precisa ser confirmado no jogo depois do port. A Ball Anvil continua manual (sem funil, um item por vez). Uma forja automática de verdade seria feature nova, não existe no original.

## 1. Inventário por tipo

| Tipo | Pasta | Qtd | Observação |
|---|---|---:|---|
| Blockstate | `assets/…/blockstates/` | 1 | `ball_anvil.json`: 4 variantes `facing=north|east|south|west`, `y` = 0/90/180/270 |
| Modelo de bloco | `models/block/` | 1 | `ball_anvil.json`: 5 elementos, parent `minecraft:block/block` |
| Modelo de bloco Pixelmon | `models/block/anvil_states/` | 90 | 28 bolas × 3 estágios (84) + iron/aluminum disc_flat, aluminium_ingot, aluminium_plate_stage1-3 (6). Sem parent, nenhum em uso |
| Modelo de item | `models/item/` | 83 | 17 martelos (`item/handheld`), 62 tampas/blanks + base_blank, pokeball_base, ball_mold (`item/generated`), ball_anvil (parent = modelo do bloco) |
| Modelo de item Pixelmon | `models/item/anvil_states/` | 23 | `<bola>balllid.json`, sem parent, nenhum em uso, **textura quebrada** |
| Textura de bloco | `textures/block/` | 1 | `ball_anvil.png` 16×16 |
| Textura de bloco Pixelmon | `textures/block/anvil_progress/` | 28 | folhas box-UV 32²/128²/512² (28 bolas, incluindo cherish e gs) |
| Textura de bloco Pixelmon | `textures/block/anvil_states/` | 4 | iron_disc, aluminum_disc, aluminium_plate (16²), aluminium_ingot (32²) |
| Textura de item | `textures/item/` | 83 | 82 sprites de item 16×16 + `ball_anvil.png` (órfão) |
| Textura de item | `textures/item/lids/` | 23 | 23 cópias idênticas de `item/<bola>_lid.png`, não referenciadas |
| Lang | `lang/` | 1 | `en_us.json`, 86 chaves |
| Receitas | `data/cobbleballworks/recipes/` | 210 | ver seção 4 |
| Tags de item | `data/cobbleballworks/tags/items/` | 4 | `hammers` (17), `formed_lids` (31), `formed_ball_bases` (1), `formed_ball_parts` (2 tags) |
| Tags de bloco | `data/minecraft/tags/blocks/` | 2 | `mineable/pickaxe` e `needs_iron_tool` → `cobbleballworks:ball_anvil` |
| Loot table | `data/cobbleballworks/loot_tables/blocks/` | 1 | `ball_anvil.json` (dropa a si mesma, `survives_explosion`) |
| Outros | `raiz / META-INF / textures/item` | 3 | `pack.mcmeta` (pack_format 15 = 1.20.1), `META-INF/mods.toml` (Forge), `textures/item/README.txt` |
| **Total** | | **558** | |

Os registros no código batem 1:1 com os assets: 31 bolas no `BallRegistry`, 17 martelos em `HAMMER_DURABILITY`, 3 itens literais e 1 bloco com BlockItem. O README fala em "19 tiers", "18 hammers" e num item `ball_hammer`, mas está desatualizado: no código são 17 martelos e não existe `ball_hammer`.

## 2. Referências quebradas

### 2.1 Itens registrados → modelo de item → textura

Conferi os 83 itens (shared: 3, hammer: 17, lid_blank: 31, lid: 31, block_item: 1). **Nenhuma quebra.** Todos têm `models/item/<id>.json`. O `layer0` de cada um aponta para um PNG que existe; o `ball_anvil` herda o modelo do bloco, que usa `block/ball_anvil.png`. Também não há modelo de item sem item registrado. Os 17 martelos usam `minecraft:item/handheld`.

### 2.2 Modelos → texturas (todos os 197 modelos, inclusive `anvil_states`)

Resolvi cada `#variável` de cada face, seguindo a cadeia de parents locais. Não sobrou variável sem resolver e não há referência a texturas de outros namespaces.

| Modelo | Textura referenciada | Existe? | Arquivo que existe de fato |
|---|---|---|---|
| `item/anvil_states/diveballlid.json` | `cobbleballworks:item/lids/diveballlid` | não | `textures/item/lids/dive_lid.png` (= `item/dive_lid.png`) |
| `item/anvil_states/duskballlid.json` | `cobbleballworks:item/lids/duskballlid` | não | `textures/item/lids/dusk_lid.png` (= `item/dusk_lid.png`) |
| `item/anvil_states/fastballlid.json` | `cobbleballworks:item/lids/fastballlid` | não | `textures/item/lids/fast_lid.png` (= `item/fast_lid.png`) |
| `item/anvil_states/friendballlid.json` | `cobbleballworks:item/lids/friendballlid` | não | `textures/item/lids/friend_lid.png` (= `item/friend_lid.png`) |
| `item/anvil_states/greatballlid.json` | `cobbleballworks:item/lids/greatballlid` | não | `textures/item/lids/great_lid.png` (= `item/great_lid.png`) |
| `item/anvil_states/healballlid.json` | `cobbleballworks:item/lids/healballlid` | não | `textures/item/lids/heal_lid.png` (= `item/heal_lid.png`) |
| `item/anvil_states/heavyballlid.json` | `cobbleballworks:item/lids/heavyballlid` | não | `textures/item/lids/heavy_lid.png` (= `item/heavy_lid.png`) |
| `item/anvil_states/levelballlid.json` | `cobbleballworks:item/lids/levelballlid` | não | `textures/item/lids/level_lid.png` (= `item/level_lid.png`) |
| `item/anvil_states/loveballlid.json` | `cobbleballworks:item/lids/loveballlid` | não | `textures/item/lids/love_lid.png` (= `item/love_lid.png`) |
| `item/anvil_states/lureballlid.json` | `cobbleballworks:item/lids/lureballlid` | não | `textures/item/lids/lure_lid.png` (= `item/lure_lid.png`) |
| `item/anvil_states/luxuryballlid.json` | `cobbleballworks:item/lids/luxuryballlid` | não | `textures/item/lids/luxury_lid.png` (= `item/luxury_lid.png`) |
| `item/anvil_states/masterballlid.json` | `cobbleballworks:item/lids/masterballlid` | não | `textures/item/lids/master_lid.png` (= `item/master_lid.png`) |
| `item/anvil_states/moonballlid.json` | `cobbleballworks:item/lids/moonballlid` | não | `textures/item/lids/moon_lid.png` (= `item/moon_lid.png`) |
| `item/anvil_states/nestballlid.json` | `cobbleballworks:item/lids/nestballlid` | não | `textures/item/lids/nest_lid.png` (= `item/nest_lid.png`) |
| `item/anvil_states/netballlid.json` | `cobbleballworks:item/lids/netballlid` | não | `textures/item/lids/net_lid.png` (= `item/net_lid.png`) |
| `item/anvil_states/pokeballlid.json` | `cobbleballworks:item/lids/pokeballlid` | não | `textures/item/lids/poke_lid.png` (= `item/poke_lid.png`) |
| `item/anvil_states/premierballlid.json` | `cobbleballworks:item/lids/premierballlid` | não | `textures/item/lids/premier_lid.png` (= `item/premier_lid.png`) |
| `item/anvil_states/quickballlid.json` | `cobbleballworks:item/lids/quickballlid` | não | `textures/item/lids/quick_lid.png` (= `item/quick_lid.png`) |
| `item/anvil_states/repeatballlid.json` | `cobbleballworks:item/lids/repeatballlid` | não | `textures/item/lids/repeat_lid.png` (= `item/repeat_lid.png`) |
| `item/anvil_states/safariballlid.json` | `cobbleballworks:item/lids/safariballlid` | não | `textures/item/lids/safari_lid.png` (= `item/safari_lid.png`) |
| `item/anvil_states/sportballlid.json` | `cobbleballworks:item/lids/sportballlid` | não | `textures/item/lids/sport_lid.png` (= `item/sport_lid.png`) |
| `item/anvil_states/timerballlid.json` | `cobbleballworks:item/lids/timerballlid` | não | `textures/item/lids/timer_lid.png` (= `item/timer_lid.png`) |
| `item/anvil_states/ultraballlid.json` | `cobbleballworks:item/lids/ultraballlid` | não | `textures/item/lids/ultra_lid.png` (= `item/ultra_lid.png`) |

São 23 quebras, todas do mesmo padrão. O README.txt de texturas diz `lids/<ball>balllid.png`, mas os arquivos foram gravados como `<ball>_lid.png`. Correção: apagar `textures/item/lids/` (cópia exata) e apontar `"all"` para `cobbleballworks:item/<bola>_lid`, ou renomear os 23 PNGs para `<bola>balllid.png`. Mesmo assim o UV automático desses modelos não funciona (seção 5.4), então o mais simples é **não portar** `models/item/anvil_states/`.

Os 90 modelos de `models/block/anvil_states/` estão OK: os 84 discos apontam para `block/anvil_progress/<bola>ball.png` (28 PNGs, todos existem) e os 6 de ferro/alumínio para `block/anvil_states/*.png` (4 PNGs, todos existem).

### 2.3 Texturas órfãs ou duplicadas

- 24 PNG não são referenciados por nenhum modelo:
  - `textures/item/ball_anvil.png`: o modelo de item da bigorna herda o modelo 3D do bloco, então esse sprite 2D nunca é usado. Dá para apagar ou trocar o modelo de item por `item/generated`, se preferir ícone 2D.
  - as 23 de `textures/item/lids/*.png`, todas byte a byte iguais a `textures/item/<bola>_lid.png` (conferido por sha256).
- O `ball_mold` tem modelo, textura e lang, mas sem as receitas do Tinkers fica sem receita (ver 4.2).

### 2.4 Blockstate, loot table e tags

- Blockstate: 4 variantes cobrem os 4 valores de `FACING` (HorizontalDirectionalBlock). O modelo existe e os `y` são válidos. **OK.**
- Loot table `blocks/ball_anvil` → `cobbleballworks:ball_anvil` (existe). **OK**, mas no 1.21.1 vai para `data/cobbleballworks/loot_table/blocks/ball_anvil.json`.
- Tags: `hammers` = os 17 martelos registrados, sem faltar nem sobrar; `formed_lids` = as 31 tampas; `formed_ball_parts` referencia as 2 tags locais, que existem; `mineable/pickaxe` e `needs_iron_tool` → `ball_anvil`. **OK.** No 1.21.1 mudam para `tags/item/` e `tags/block/`. As tags `formed_*` só são usadas pela receita do Tinkers; depois de removê-la, ficam sem uso (são inofensivas).

### 2.5 Outros pontos do port

- `assets/cobbleballworks/textures/item/README.txt` tem letras maiúsculas no caminho, o que não é válido como ResourceLocation no 1.21. O Fabric só loga "Invalid path … ignoring", mas é melhor tirar de `assets/`.
- `pack.mcmeta` (pack_format 15) e `META-INF/mods.toml` são do Forge 1.20.1. No Fabric o mod usa `fabric.mod.json` e o pack de recursos do mod é gerado sozinho. Dá para apagar o `pack.mcmeta`; se quiser mantê-lo, o pack_format do 1.21.1 é 34 para assets e 48 para dados (conferido no `version.json` do server 1.21.1).
- Os 113 modelos Pixelmon não têm `parent` nem `particle`, o que não importa para o BER. No 1.21.1 o jogo lê esses JSONs, mas eles só viram BakedModel utilizável se forem registrados como modelos extras (Fabric `ModelLoadingPlugin` → `addModels(...)`) e buscados com `BakedModelManager.getModel(id)`.
- Chaves desconhecidas (`__comment` no `ball_anvil.json`, `credit` e `name` nos Pixelmon) são ignoradas pelo parser. Nenhum JSON inválido, nenhuma chave duplicada, nenhum BOM.

## 3. Lang

| Grupo | Chaves esperadas | Presentes |
|---|---:|---:|
| `item.cobbleballworks.<id>` (82 itens, sem contar o BlockItem) | 82 | 82 |
| `block.cobbleballworks.ball_anvil` (também é o nome do BlockItem) | 1 | 1 |
| `itemGroup.cobbleballworks.ballworks` (`ModCreativeTabs.kt`) | 1 | 1 |
| `block.cobbleballworks.ball_anvil.progress` = "Hammering... %s / %s" (2 args no código: progress, hits) | 1 | 1 |
| `block.cobbleballworks.ball_anvil.finished` = "Shaped: %s" (1 arg: nome do item) | 1 | 1 |
| **Total** | **86** | **86** |

**Nada falta e nada sobra.** As 3 chaves `translatable(...)` usadas no código existem e o número de `%s` bate com os argumentos. Para melhorar (não bloqueia): criar um `pt_br.json`, já que o servidor é de dois brasileiros, e, se quiser nomes bonitos de tag no EMI/JEI, `tag.item.cobbleballworks.hammers` / `formed_lids` / `formed_ball_bases` / `formed_ball_parts`. Se o `ball_mold` sair do port, a chave `item.cobbleballworks.ball_mold` sai junto.

## 4. Receitas (210)

| Tipo | Qtd | Classe |
|---|---:|---|
| `minecraft:crafting_shaped` | 50 | (a) 31 `*_lid_blank` + 17 martelos + `base_blank` + `ball_anvil` |
| `minecraft:crafting_shapeless` | 63 | (a) 31 `<bola>_lid` + `pokeball_base` (martelar na grade) + 31 `<bola>_ball` (montagem) |
| `create:pressing` (em `forge:conditional` mod_loaded create) | 32 | (b) |
| `mekanism:crushing` (em `forge:conditional` mod_loaded mekanism) | 32 | (b) |
| `mekmm:stamper` (**sem** condição) | 32 | (b) |
| `tconstruct:casting_table` (em `forge:conditional` mod_loaded tconstruct) | 1 | (b) |

### 4.1 (a) Crafting puro: 113 receitas, todas utilizáveis no pack depois da conversão

- Estrutura conferida em todas: todo símbolo do pattern tem key, nenhuma key sobra, linhas com a mesma largura, máximo 3×3, shapeless com 1 a 9 ingredientes, categoria `equipment`.
- **As 113 precisam de `result.item` → `result.id`** (bloqueante, item 2 do resumo).
- As 32 de martelar (`<bola>_lid` e `pokeball_base`) são idênticas entre si: `{item: <blank>}` + `{tag: cobbleballworks:hammers}` → 1× peça. Nenhuma diverge. Dependem do remainder do martelo no código Fabric.
- **Conflitos:** comparei as 113, já com `forge:`→`c:`, contra as 5265 receitas `crafting_shaped`/`crafting_shapeless` disponíveis: 4378 dos 119 jars do pack, incluindo jars aninhados e os 48 overrides do Parts Refabricated, e 887 do vanilla 1.21.1 (`server-1.21.1.jar` da réplica do scratchpad, copiado para `jar-vanilla/`). As tags foram expandidas recursivamente, inclusive as vanilla, e o shaped foi testado também espelhado. Também comparei as 113 entre si. Resultado: **0 conflitos externos e 0 internos**. O verificador foi validado com controles positivos (shaped com tag × `minecraft:stick`, shaped espelhado × `minecraft:shears`, shaped e shapeless × Parts Refabricated): pegou os 4.
- Os 10 itens vanilla referenciados (`amethyst_shard`, `copper_ingot`, `diamond`, `echo_shard`, `gold_ingot`, `iron_ingot`, `netherite_ingot`, `nether_star`, `shulker_shell`, `stick`) e as 3 tags vanilla (`minecraft:buttons`, `minecraft:planks`, `minecraft:stone_crafting_materials`) existem nos dados do 1.21.1.
- Martelos e materiais:

| Martelo | Material (`M`) | No nosso pack |
|---|---|---|
| `aluminium_hammer` | tag `forge:ingots/aluminum` | **`c:ingots/aluminum` não existe no pack: nunca casa** |
| `amethyst_hammer` | item `minecraft:amethyst_shard` | item vanilla, OK |
| `dawn_stone_hammer` | item `cobblemon:dawn_stone` | item do Cobblemon 1.8.2, existe |
| `diamond_hammer` | tag `forge:gems/diamond` | vira `c:gems/diamond`, que tem `minecraft:diamond` |
| `dusk_stone_hammer` | item `cobblemon:dusk_stone` | item do Cobblemon 1.8.2, existe |
| `fire_stone_hammer` | item `cobblemon:fire_stone` | item do Cobblemon 1.8.2, existe |
| `gold_hammer` | tag `forge:ingots/gold` | vira `c:ingots/gold`, que tem `minecraft:gold_ingot` |
| `iron_hammer` | tag `forge:ingots/iron` | vira `c:ingots/iron`, que tem `minecraft:iron_ingot` (Fabric API) |
| `leaf_stone_hammer` | item `cobblemon:leaf_stone` | item do Cobblemon 1.8.2, existe |
| `moon_stone_hammer` | item `cobblemon:moon_stone` | item do Cobblemon 1.8.2, existe |
| `ruby_hammer` | tag `forge:gems/ruby` | **`c:gems/ruby` não existe no pack: nunca casa** |
| `sapphire_hammer` | tag `forge:gems/sapphire` | **`c:gems/sapphire` não existe no pack: nunca casa** |
| `stone_hammer` | tag `minecraft:stone_crafting_materials` | tag vanilla, OK |
| `sun_stone_hammer` | item `cobblemon:sun_stone` | item do Cobblemon 1.8.2, existe |
| `thunder_stone_hammer` | item `cobblemon:thunder_stone` | item do Cobblemon 1.8.2, existe |
| `water_stone_hammer` | item `cobblemon:water_stone` | item do Cobblemon 1.8.2, existe |
| `wood_hammer` | tag `minecraft:planks` | tag vanilla, OK |

Para os 3 martelos sem material há três saídas: tirar as 3 receitas e deixar os itens só no criativo, trocar o material por algo que exista, ou apagar os 3 itens. A decisão é de design, não de port.

### 4.2 (b) Dependem de mods que NÃO temos: 97 arquivos para remover

**Create (`create:pressing`): 32 arquivos**

```
create_pressing_azure_lid.json create_pressing_beast_lid.json create_pressing_citrine_lid.json create_pressing_dive_lid.json
create_pressing_dream_lid.json create_pressing_dusk_lid.json create_pressing_fast_lid.json create_pressing_friend_lid.json
create_pressing_great_lid.json create_pressing_heal_lid.json create_pressing_heavy_lid.json create_pressing_level_lid.json
create_pressing_love_lid.json create_pressing_lure_lid.json create_pressing_luxury_lid.json create_pressing_master_lid.json
create_pressing_moon_lid.json create_pressing_nest_lid.json create_pressing_net_lid.json create_pressing_park_lid.json create_pressing_poke_lid.json
create_pressing_pokeball_base.json create_pressing_premier_lid.json create_pressing_quick_lid.json create_pressing_repeat_lid.json
create_pressing_roseate_lid.json create_pressing_safari_lid.json create_pressing_slate_lid.json create_pressing_sport_lid.json
create_pressing_timer_lid.json create_pressing_ultra_lid.json create_pressing_verdant_lid.json
```

**Mekanism (`mekanism:crushing`): 32 arquivos**

```
mekanism_crushing_azure_lid.json mekanism_crushing_beast_lid.json mekanism_crushing_citrine_lid.json mekanism_crushing_dive_lid.json
mekanism_crushing_dream_lid.json mekanism_crushing_dusk_lid.json mekanism_crushing_fast_lid.json mekanism_crushing_friend_lid.json
mekanism_crushing_great_lid.json mekanism_crushing_heal_lid.json mekanism_crushing_heavy_lid.json mekanism_crushing_level_lid.json
mekanism_crushing_love_lid.json mekanism_crushing_lure_lid.json mekanism_crushing_luxury_lid.json mekanism_crushing_master_lid.json
mekanism_crushing_moon_lid.json mekanism_crushing_nest_lid.json mekanism_crushing_net_lid.json mekanism_crushing_park_lid.json
mekanism_crushing_poke_lid.json mekanism_crushing_pokeball_base.json mekanism_crushing_premier_lid.json mekanism_crushing_quick_lid.json
mekanism_crushing_repeat_lid.json mekanism_crushing_roseate_lid.json mekanism_crushing_safari_lid.json mekanism_crushing_slate_lid.json
mekanism_crushing_sport_lid.json mekanism_crushing_timer_lid.json mekanism_crushing_ultra_lid.json mekanism_crushing_verdant_lid.json
```

**Mekanism: More Machine (`mekmm:stamper`, sem `forge:conditional`): 32 arquivos**

```
mekmm_stamper_azure_lid.json mekmm_stamper_beast_lid.json mekmm_stamper_citrine_lid.json mekmm_stamper_dive_lid.json mekmm_stamper_dream_lid.json
mekmm_stamper_dusk_lid.json mekmm_stamper_fast_lid.json mekmm_stamper_friend_lid.json mekmm_stamper_great_lid.json mekmm_stamper_heal_lid.json
mekmm_stamper_heavy_lid.json mekmm_stamper_level_lid.json mekmm_stamper_love_lid.json mekmm_stamper_lure_lid.json mekmm_stamper_luxury_lid.json
mekmm_stamper_master_lid.json mekmm_stamper_moon_lid.json mekmm_stamper_nest_lid.json mekmm_stamper_net_lid.json mekmm_stamper_park_lid.json
mekmm_stamper_poke_lid.json mekmm_stamper_pokeball_base.json mekmm_stamper_premier_lid.json mekmm_stamper_quick_lid.json
mekmm_stamper_repeat_lid.json mekmm_stamper_roseate_lid.json mekmm_stamper_safari_lid.json mekmm_stamper_slate_lid.json mekmm_stamper_sport_lid.json
mekmm_stamper_timer_lid.json mekmm_stamper_ultra_lid.json mekmm_stamper_verdant_lid.json
```

**Tinkers' Construct (`tconstruct:casting_table`): 1 arquivo**

```
tconstruct_casting_ball_mold.json
```

O glob `data/cobbleballworks/recipes/{create_pressing_,mekanism_crushing_,mekmm_stamper_,tconstruct_casting_}*.json` pega exatamente esses 97. Efeitos colaterais: o `ball_mold` fica sem receita, porque só saía da Casting Table do Tinkers e só servia na Stamper do MekMM; vale remover o item, o modelo, a textura, a lang e a entrada na aba criativa. As tags `formed_*` ficam sem uso. Se um dia quiser manter alguma rota opcional, o equivalente Fabric de `forge:conditional`/`forge:mod_loaded` é `"fabric:load_conditions"` com a condição `fabric:all_mods_loaded`.

### 4.3 (c) Tags `forge:` → `c:` (conversão exata)

| De | Para | Arquivos | Situação no pack (scan dos 119 jars) |
|---|---|---|---|
| `forge:gems/diamond` | `c:gems/diamond` | `diamond_hammer.json` | definida (minecraft:diamond, `#c:diamonds` opcional) via Fabric API 0.116.17 (convention tags v2) |
| `forge:gems/ruby` | `c:gems/ruby` | `ruby_hammer.json` | **não existe em nenhum jar do pack**: a tag fica vazia e a receita nunca casa |
| `forge:gems/sapphire` | `c:gems/sapphire` | `sapphire_hammer.json` | **não existe em nenhum jar do pack**: a tag fica vazia e a receita nunca casa |
| `forge:ingots/aluminum` | `c:ingots/aluminum` | `aluminium_hammer.json` | **não existe em nenhum jar do pack**: a tag fica vazia e a receita nunca casa |
| `forge:ingots/gold` | `c:ingots/gold` | `gold_hammer.json` | definida (minecraft:gold_ingot) via Fabric API 0.116.17 (convention tags v2) |
| `forge:ingots/iron` | `c:ingots/iron` | `ball_anvil.json`, `base_blank.json`, `iron_hammer.json` | definida (minecraft:iron_ingot) via Fabric API 0.116.17 (convention tags v2) |

São 8 ocorrências em 8 arquivos, uma por arquivo. Nenhuma das 97 receitas da classe (b) usa `forge:` tag. As tags vanilla usadas (`minecraft:buttons` nas 31 montagens, `minecraft:planks`, `minecraft:stone_crafting_materials`) continuam iguais no 1.21.1.

### 4.4 (d) Ids `cobblemon:` referenciados, conferidos contra o jar do Cobblemon 1.8.2

Para cada id busquei evidência no jar: chave `item.cobblemon.<id>` no `en_us.json`, `models/item/<id>.json`, resultado de receita em `data/cobblemon/recipe/` e presença em tag de item.

| Id | Evidência no jar 1.8.2 |
|---|---|
| `cobblemon:azure_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:beast_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:black_apricorn` | lang:item, lang:block, models/item, textures/item, item-tag |
| `cobblemon:blue_apricorn` | lang:item, lang:block, models/item, textures/item, item-tag |
| `cobblemon:citrine_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:dawn_stone` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:dive_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:dream_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:dusk_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:dusk_stone` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:fast_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:fire_stone` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:friend_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:great_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:green_apricorn` | lang:item, lang:block, models/item, textures/item, item-tag |
| `cobblemon:heal_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:heavy_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:leaf_stone` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:level_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:love_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:lure_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:luxury_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:master_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:moon_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:moon_stone` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:nest_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:net_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:park_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:pink_apricorn` | lang:item, lang:block, models/item, textures/item, item-tag |
| `cobblemon:poke_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:premier_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:quick_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:red_apricorn` | lang:item, lang:block, models/item, textures/item, item-tag |
| `cobblemon:repeat_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:roseate_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:safari_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:slate_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:sport_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:sun_stone` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:thunder_stone` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:timer_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:ultra_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:verdant_ball` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:water_stone` | lang:item, models/item, recipe-result, item-tag |
| `cobblemon:white_apricorn` | lang:item, lang:block, models/item, textures/item, item-tag |
| `cobblemon:yellow_apricorn` | lang:item, lang:block, models/item, textures/item, item-tag |

**46 ids distintos (31 bolas, 7 apricorns, 8 pedras de evolução), todos existem. Nenhum faltando.** O Ballworks não usa nenhuma tag `cobblemon:`.

Tags `cobblemon:tier_*` do 1.8.2: o Cobblemon usa essas tags como ingrediente central das receitas de bola.

| Tag | Conteúdo | Itens no pack | Bolas desse tier (`tier_N_poke_balls`) |
|---|---|---|---|
| `cobblemon:tier_1_poke_ball_materials` | `#c:ingots/copper` | minecraft:copper_ingot | poke_ball, citrine_ball, verdant_ball, azure_ball, roseate_ball, slate_ball, premier_ball, safari_ball, heal_ball, ancient_poke_ball, ancient_citrine_ball, ancient_verdant_ball, ancient_azure_ball, ancient_roseate_ball, ancient_slate_ball, ancient_ivory_ball, ancient_feather_ball, ancient_heavy_ball |
| `cobblemon:tier_2_poke_ball_materials` | `#c:ingots/iron` | minecraft:iron_ingot | great_ball, fast_ball, level_ball, lure_ball, heavy_ball, friend_ball, moon_ball, sport_ball, park_ball, net_ball, dive_ball, nest_ball, ancient_great_ball, ancient_wing_ball, ancient_leaden_ball |
| `cobblemon:tier_3_poke_ball_materials` | `#c:ingots/gold` | minecraft:gold_ingot | ultra_ball, love_ball, repeat_ball, timer_ball, luxury_ball, dusk_ball, quick_ball, ancient_ultra_ball, ancient_jet_ball, ancient_gigaton_ball |
| `cobblemon:tier_4_poke_ball_materials` | `#c:gems/diamond` | minecraft:diamond (+ `#c:diamonds` opcional) | dream_ball, beast_ball |
| `cobblemon:tier_5_poke_ball_materials` | **não existe** | (a master usa `minecraft:nether_star` direto) | master_ball, ancient_origin_ball |

O item central de cada montagem `<bola>_ball` bate com o material do tier no 1.8.2 em **31/31 bolas** (divergências: nenhuma). Sugestão opcional: trocar o item fixo por `{"tag": "cobblemon:tier_N_poke_ball_materials"}`, como faz o Cobblemon. A master continua com `minecraft:nether_star`.

Anéis dos `*_lid_blank` comparados com as receitas de bola do Cobblemon 1.8.2 (o mesmo padrão, só que sem o centro):

| Bola | Ballworks (t / l / r / b) | Cobblemon 1.8.2 (t / l / r / b) |
|---|---|---|
| level | black / yellow / pink / red | black / pink / pink / red |
| lure | red / blue / green / red | red / blue / blue / green |
| moon | yellow / blue / black / white | yellow / blue / black / yellow |

- **level, lure e moon** têm o anel diferente do 1.8.2. A causa provável é que o Ballworks mira o Cobblemon 1.4 (`mods.toml`: `[1.4.0,1.6)`), cujas receitas eram outras; isso não foi conferido contra o jar 1.4. Para espelhar o 1.8.2: level `l = pink`, lure `r = blue, b = green`, moon `b = yellow`.
- **beast e master** (fora da tabela): o formato é o mesmo, só muda item × tag. O Ballworks usa `minecraft:gold_ingot`/`minecraft:netherite_ingot`, o Cobblemon usa `#c:ingots/gold`/`#c:ingots/netherite`. No pack dá no mesmo, porque cada tag só contém esse item. A beast é o anel 3×3 completo `GEG/E·E/GEG` (4 ouro nos cantos, 4 echo shard nas bordas).
- As outras 26 têm anel idêntico.
- Atenção ao pack: o **Cobblemon Parts Refabricated 1.0.3**, que já está instalado, sobrescreve as receitas `cobblemon:<bola>_ball` das 31 bolas (48 overrides em `data/cobblemon/recipe/`) com o sistema de peças dele. O Ballworks não conflita com ele (0 conflitos de grade, ids diferentes), mas os dois sistemas de peças vão coexistir. Desligar um deles é decisão de design.

### 4.5 Por que há 124 receitas `*_lid`

O glob `*_lid.json` pega 124 arquivos, exatamente **4 por bola × 31 bolas** (contagem por bola: todas as 31 têm exatamente 4):

| Rota | Padrão do arquivo | Por bola | Total |
|---|---|---:|---:|
| Martelo na grade (crafting shapeless) | `<bola>_lid.json` | 1 | 31 |
| Create, Prensa Mecânica | `create_pressing_<bola>_lid.json` | 1 | 31 |
| Mekanism, Crusher | `mekanism_crushing_<bola>_lid.json` | 1 | 31 |
| MekMM, Stamper + molde | `mekmm_stamper_<bola>_lid.json` | 1 | 31 |
| **Total** | | **4** | **124** |

Os `*_lid_blank.json` (31) não entram porque terminam em `_lid_blank`. A `pokeball_base` tem as mesmas 4 rotas em arquivos que não terminam em `_lid` (`pokeball_base`, `create_pressing_pokeball_base`, `mekanism_crushing_pokeball_base`, `mekmm_stamper_pokeball_base`). No port sobram **31** receitas de tampa (todas de grade, conferidas: 0 divergentes) + 1 de base.

## 5. Geometria dos modelos Pixelmon (`models/*/anvil_states`)

### 5.1 Referência: a bigorna da Ballworks (`models/block/ball_anvil.json`)

Bounding box (1, 0, 0)–(15, 15, 15). **O topo fica em y = 15** (elemento `top_rim`, de (1, 14, 1) a (15, 15, 15)): uma superfície de 14×14 centrada em (8, 8). Abaixo dela, `top_plate` vai de y 10 a 14 e o `horn` ocupa z 0–3, y 11–14, na face norte. O BER atual usa `TOP_Y_OFFSET = 15/16 + 0.02`, coerente com isso. Todos os elementos estão dentro de [0, 16], sem rotação.

### 5.2 Consistência entre os modelos

Agrupei os 113 modelos por geometria idêntica (from/to/nomes, ignorando a textura):

| Grupo | Modelos | Elementos | min (x, y, z) | max (x, y, z) | Tamanho | Centro XZ | Dist. a (8, 8) | Base − topo bigorna |
|---|---:|---:|---|---|---|---|---:|---:|
| aluminium_ingot | 1 | 4 | (-7.9, 12, -2) | (-1.9, 14, 2) | (6, 2, 4) | (-4.9, 0) | 15.18 | -3 |
| aluminium_plate_stage1 | 1 | 1 | (-7.4, 13, -2.5) | (-1.4, 14, 3.5) | (6, 1, 6) | (-4.4, 0.5) | 14.49 | -2 |
| aluminium_plate_stage2 | 1 | 4 | (-7.9, 12.7, -2.5) | (-1.9, 14, 2.5) | (6, 1.3, 5) | (-4.9, 0) | 15.18 | -2.3 |
| aluminium_plate_stage3 | 1 | 4 | (-7.9, 12.9, -2.5) | (-1.9, 14, 2.5) | (6, 1.1, 5) | (-4.9, 0) | 15.18 | -2.1 |
| disc_flat (28 bolas + iron + aluminum) | 30 | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (3.8, 1.1, 3.8) | (6.5, 0) | 8.14 | -3.3 |
| disc_stage1 (28 bolas) | 28 | 7 | (4.6, 11.8, -1.9) | (8.4, 13.2, 1.9) | (3.8, 1.4, 3.8) | (6.5, 0) | 8.14 | -3.2 |
| disc_stage2 (28 bolas) | 28 | 7 | (4.6, 11.8, -1.9) | (8.4, 13.4, 1.9) | (3.8, 1.6, 3.8) | (6.5, 0) | 8.14 | -3.2 |
| item balllid (23 bolas) | 23 | 7 | (4.6, 11.8, -1.9) | (8.4, 13.7, 1.9) | (3.8, 1.9, 3.8) | (6.5, 0) | 8.14 | -3.2 |

- **Os modelos são consistentes entre si.** Todos os discos e tampas de bola compartilham a mesma origem: centro XZ (6.5, 0), pegada 3.8 × 3.8. Dentro de cada estágio, as 28 bolas têm geometria **idêntica** (no flat, ferro e alumínio também); só muda a textura. Por isso dá para trocar os 86 discos (28 × 3 + ferro + alumínio) por 3 modelos-base com filhos que só trocam `textures.all`.
- Altura: a base fica em y = 11.7 (flat, por causa do `RedTip`) ou 11.8 (stage1, stage2, tampa). O topo sobe de 12.8 (flat) para 13.2 (stage1), 13.4 (stage2) e 13.7 (tampa).
- **Distância do centro:** 8.14 px (1.5 em X, 8 em Z). O disco fica em cima da face norte, onde ficava o pivô do TESR antigo do Pixelmon, e não no meio do tampo. Os modelos de alumínio ficam a 15.2 px (x de −7.9 a −1.4).
- **Altura relativa à bigorna:** a base fica 3.2–3.3 px **abaixo** do topo (y = 15). Do jeito que está, o disco renderiza enterrado no `top_plate`/`horn` e meio para fora da face norte (z < 0).
- **Limites:** nenhum elemento sai de [-16, 32] (0 modelos que falhariam ao carregar). Mas **todos os 113** têm coordenadas fora de [0, 16] (z negativo; x negativo no alumínio).
- Elementos redundantes: nos 86 discos, `RedBottom` é **idêntico** a `RedTop` (mesmo from/to), um quad duplicado. Nas 23 tampas eles se sobrepõem em parte, com faces laterais coplanares (z-fighting).

### 5.3 Translação proposta (centralizar em cima da bigorna)

A conta é T = (8 − cx, 15 − min_y, 8 − cz), em pixels de modelo (1/16 de bloco). Somando T ao from/to de cada elemento, o objeto fica centrado em (8, 8) e apoiado em y = 15.

| Grupo | T (px) | T (blocos, para `poseStack.translate`) | Caixa resultante |
|---|---|---|---|
| aluminium_ingot | (12.9, 3, 8) | (0.8063, 0.1875, 0.5000) | (5, 15, 6)–(11, 17, 10) |
| aluminium_plate_stage1 | (12.4, 2, 7.5) | (0.7750, 0.1250, 0.4688) | (5, 15, 5)–(11, 16, 11) |
| aluminium_plate_stage2 | (12.9, 2.3, 8) | (0.8063, 0.1437, 0.5000) | (5, 15, 5.5)–(11, 16.3, 10.5) |
| aluminium_plate_stage3 | (12.9, 2.1, 8) | (0.8063, 0.1313, 0.5000) | (5, 15, 5.5)–(11, 16.1, 10.5) |
| disc_flat (28 bolas + iron + aluminum) | (1.5, 3.3, 8) | (0.0938, 0.2062, 0.5000) | (6.1, 15, 6.1)–(9.9, 16.1, 9.9) |
| disc_stage1 (28 bolas) | (1.5, 3.2, 8) | (0.0938, 0.2000, 0.5000) | (6.1, 15, 6.1)–(9.9, 16.4, 9.9) |
| disc_stage2 (28 bolas) | (1.5, 3.2, 8) | (0.0938, 0.2000, 0.5000) | (6.1, 15, 6.1)–(9.9, 16.6, 9.9) |
| item balllid (23 bolas) | (1.5, 3.2, 8) | (0.0938, 0.2000, 0.5000) | (6.1, 15, 6.1)–(9.9, 16.9, 9.9) |

- Se preferir uma translação única para todos os estágios de uma bola (sem o "pulo" de 0.1 px ao trocar de estágio), use **(+1.5, +3.3, +8)** para tudo: o flat fica exatamente em y = 15 e os outros flutuam 0.1 px (1/160 de bloco). Um ε de +0.01 px evita z-fighting com o tampo.
- **Grave a translação no JSON** em vez de usar só `poseStack.translate`. O UV automático é calculado a partir de from/to, então transladar só no render mantém o UV quebrado (5.4). A rotação por `FACING` do BER (em volta de 0.5, 0.5) continua correta com o modelo centralizado.
- Escala: o disco tem 3.8 px de largura, enquanto o fallback atual (sprite do item a 0.5–0.75×) ocupa 8–12 px. Se ficar pequeno demais, aplique ×2 em torno de (8, 15, 8) depois de transladar.
- Os modelos de alumínio e ferro (`aluminium_ingot`, `aluminium_plate_stage1-3`, `aluminum_disc_flat`, `iron_disc_flat`) não correspondem a nenhum item do Ballworks. O único reaproveitável é o `iron_disc_flat`, como visual do `base_blank`; os outros podem ficar fora do port.

Elementos dos 4 modelos-base já transladados (discos com T = (1.5, 3.3, 8); tampa com (1.5, 3.2, 8); `RedBottom` duplicado removido):

```
disc_flat  (T = (1.5, 3.3, 8))
   RedTip    from [7.0, 15.0, 7] to [9.0, 16.0, 9]
   RedTop    from [6.5, 15.1, 6.5] to [9.5, 16.1, 9.5]
   RedFront  from [7.0, 15.1, 6.1] to [9.0, 16.1, 7.1]
   - RedBottom: duplicado, removido
   RedRight  from [6.1, 15.1, 7] to [7.1, 16.1, 9]
   RedLeft   from [8.9, 15.1, 7] to [9.9, 16.1, 9]
   RedBack   from [7.0, 15.1, 8.9] to [9.0, 16.1, 9.9]
disc_stage1  (T = (1.5, 3.3, 8))
   RedTip    from [7.0, 15.1, 7] to [9.0, 16.1, 9]
   RedTop    from [6.5, 15.5, 6.5] to [9.5, 16.5, 9.5]
   RedFront  from [7.0, 15.5, 6.1] to [9.0, 16.5, 7.1]
   - RedBottom: duplicado, removido
   RedRight  from [6.1, 15.5, 7] to [7.1, 16.5, 9]
   RedLeft   from [8.9, 15.5, 7] to [9.9, 16.5, 9]
   RedBack   from [7.0, 15.5, 8.9] to [9.0, 16.5, 9.9]
disc_stage2  (T = (1.5, 3.3, 8))
   RedTip    from [7.0, 15.1, 7] to [9.0, 16.1, 9]
   RedTop    from [6.5, 15.5, 6.5] to [9.5, 16.5, 9.5]
   RedFront  from [7.0, 15.7, 6.1] to [9.0, 16.7, 7.1]
   - RedBottom: duplicado, removido
   RedRight  from [6.1, 15.7, 7] to [7.1, 16.7, 9]
   RedLeft   from [8.9, 15.7, 7] to [9.9, 16.7, 9]
   RedBack   from [7.0, 15.7, 8.9] to [9.0, 16.7, 9.9]
lid  (T = (1.5, 3.2, 8))
   RedTip    from [7.0, 15.0, 7] to [9.0, 16.0, 9]
   RedTop    from [6.5, 15.4, 6.5] to [9.5, 16.4, 9.5]
   RedFront  from [7.0, 15.9, 6.1] to [9.0, 16.9, 7.1]
   RedBottom from [6.5, 15.9, 6.5] to [9.5, 16.9, 9.5]
   RedRight  from [6.1, 15.9, 7] to [7.1, 16.9, 9]
   RedLeft   from [8.9, 15.9, 7] to [9.9, 16.9, 9]
   RedBack   from [7.0, 15.9, 8.9] to [9.0, 16.9, 9.9]
```

### 5.4 UV automático × folhas de textura do Pixelmon

Nenhuma face dos 113 modelos tem `uv` explícito. O Minecraft deriva o UV de from/to, mas as texturas `anvil_progress/*.png` são **folhas box-UV de modelo de entidade** (ModelRenderer do 1.12, com offsets de textura que se perderam na conversão), não texturas de bloco. Gsball e parkball são de outro modelo (512², layout diferente). Amostrei 8×8 pontos por face com a regra vanilla de UV automático:

| Modelo | % fora do sprite (vaza do atlas) | % transparente | % opaco |
|---|---:|---:|---:|
| disc_flat, folhas box-UV 32²/128² (26), coordenadas originais | 33.3 | 44.6–54.5 | 12.1–22.1 |
| disc_flat, folhas box-UV 32²/128² (26), depois da translação 5.3 | 7.1 | 37.8–47.3 | 45.5–55.1 |
| disc_stage1, folhas box-UV 32²/128² (26), coordenadas originais | 33.3 | 48.8–57.7 | 9–17.8 |
| disc_stage1, folhas box-UV 32²/128² (26), depois da translação 5.3 | 21.4 | 35.1–44.6 | 33.9–43.5 |
| disc_stage2, folhas box-UV 32²/128² (26), coordenadas originais | 33.3 | 50.1–58.6 | 8.1–16.5 |
| disc_stage2, folhas box-UV 32²/128² (26), depois da translação 5.3 | 31 | 31.8–40.2 | 28.9–37.2 |
| disc_flat, folhas gs/park (512²) (2), coordenadas originais | 33.3 | 0 | 66.7 |
| disc_flat, folhas gs/park (512²) (2), depois da translação 5.3 | 7.1 | 0 | 92.9 |
| disc_stage1, folhas gs/park (512²) (2), coordenadas originais | 33.3 | 0 | 66.7 |
| disc_stage1, folhas gs/park (512²) (2), depois da translação 5.3 | 21.4 | 0 | 78.6 |
| disc_stage2, folhas gs/park (512²) (2), coordenadas originais | 33.3 | 0 | 66.7 |
| disc_stage2, folhas gs/park (512²) (2), depois da translação 5.3 | 31 | 0 | 69 |
| ferro/alumínio (6), coordenadas originais | 33.3–83.3 | 0–66.7 | 0–16.7 |
| ferro/alumínio (6), depois da translação 5.3 | 0–50 | 0–66.7 | 33.3–50 |
| tampas `balllid` (23) | textura inexistente | n/a | n/a |

Mesmo centralizados, nos 26 jogos de discos com folha box-UV (as 28 texturas menos gs e park) 32–47% da área amostra pixels transparentes, que viram buracos em `cutout` ou preto em `solid`. Gsball e parkball não têm transparência, mas a folha de 512² é de outro modelo, então as cores saem de lugares arbitrários. Nos stage1/stage2, 21–31% cai fora do sprite, porque y passa de 16 e as faces laterais ficam com v < 0. Apontar as tampas para os sprites 16×16 de `item/<bola>_lid` também não resolve: 45% fora e 21% transparente. **Recomendação:** UV explícito em cada face. A forma mais barata é usar os próprios sprites dos itens, num retângulo que é opaco nas 31 bolas (calculado): `item/<bola>_lid_blank` e `item/base_blank` → `"uv": [4, 7, 13, 11]`; `item/<bola>_lid` e `item/pokeball_base` → `"uv": [4, 6, 12, 10]`. Assim as 31 bolas, incluindo as 5 sem disco Pixelmon, e a base ganham um disco com a cor certa.

### 5.5 Tabela completa por modelo

`T` = translação proposta em px; "fora [0,16]" indica coordenadas fora do cubo do bloco. Nenhum modelo sai de [-16, 32].

| Modelo | Elem. | min | max | Centro XZ | Dist. | Base − 15 | Fora [0,16] | T |
|---|---:|---|---|---|---:|---:|---|---|
| `block/anvil_states/aluminium_ingot` | 4 | (-7.9, 12, -2) | (-1.9, 14, 2) | (-4.9, 0) | 15.18 | -3 | sim | (12.9, 3, 8) |
| `block/anvil_states/aluminium_plate_stage1` | 1 | (-7.4, 13, -2.5) | (-1.4, 14, 3.5) | (-4.4, 0.5) | 14.49 | -2 | sim | (12.4, 2, 7.5) |
| `block/anvil_states/aluminium_plate_stage2` | 4 | (-7.9, 12.7, -2.5) | (-1.9, 14, 2.5) | (-4.9, 0) | 15.18 | -2.3 | sim | (12.9, 2.3, 8) |
| `block/anvil_states/aluminium_plate_stage3` | 4 | (-7.9, 12.9, -2.5) | (-1.9, 14, 2.5) | (-4.9, 0) | 15.18 | -2.1 | sim | (12.9, 2.1, 8) |
| `block/anvil_states/aluminum_disc_flat` | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (6.5, 0) | 8.14 | -3.3 | sim | (1.5, 3.3, 8) |
| `block/anvil_states/beastball_disc_flat` | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (6.5, 0) | 8.14 | -3.3 | sim | (1.5, 3.3, 8) |
| `block/anvil_states/beastball_disc_stage1` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.2, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/beastball_disc_stage2` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.4, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/cherishball_disc_flat` | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (6.5, 0) | 8.14 | -3.3 | sim | (1.5, 3.3, 8) |
| `block/anvil_states/cherishball_disc_stage1` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.2, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/cherishball_disc_stage2` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.4, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/diveball_disc_flat` | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (6.5, 0) | 8.14 | -3.3 | sim | (1.5, 3.3, 8) |
| `block/anvil_states/diveball_disc_stage1` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.2, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/diveball_disc_stage2` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.4, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/dreamball_disc_flat` | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (6.5, 0) | 8.14 | -3.3 | sim | (1.5, 3.3, 8) |
| `block/anvil_states/dreamball_disc_stage1` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.2, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/dreamball_disc_stage2` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.4, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/duskball_disc_flat` | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (6.5, 0) | 8.14 | -3.3 | sim | (1.5, 3.3, 8) |
| `block/anvil_states/duskball_disc_stage1` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.2, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/duskball_disc_stage2` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.4, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/fastball_disc_flat` | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (6.5, 0) | 8.14 | -3.3 | sim | (1.5, 3.3, 8) |
| `block/anvil_states/fastball_disc_stage1` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.2, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/fastball_disc_stage2` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.4, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/friendball_disc_flat` | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (6.5, 0) | 8.14 | -3.3 | sim | (1.5, 3.3, 8) |
| `block/anvil_states/friendball_disc_stage1` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.2, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/friendball_disc_stage2` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.4, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/greatball_disc_flat` | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (6.5, 0) | 8.14 | -3.3 | sim | (1.5, 3.3, 8) |
| `block/anvil_states/greatball_disc_stage1` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.2, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/greatball_disc_stage2` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.4, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/gsball_disc_flat` | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (6.5, 0) | 8.14 | -3.3 | sim | (1.5, 3.3, 8) |
| `block/anvil_states/gsball_disc_stage1` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.2, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/gsball_disc_stage2` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.4, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/healball_disc_flat` | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (6.5, 0) | 8.14 | -3.3 | sim | (1.5, 3.3, 8) |
| `block/anvil_states/healball_disc_stage1` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.2, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/healball_disc_stage2` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.4, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/heavyball_disc_flat` | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (6.5, 0) | 8.14 | -3.3 | sim | (1.5, 3.3, 8) |
| `block/anvil_states/heavyball_disc_stage1` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.2, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/heavyball_disc_stage2` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.4, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/iron_disc_flat` | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (6.5, 0) | 8.14 | -3.3 | sim | (1.5, 3.3, 8) |
| `block/anvil_states/levelball_disc_flat` | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (6.5, 0) | 8.14 | -3.3 | sim | (1.5, 3.3, 8) |
| `block/anvil_states/levelball_disc_stage1` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.2, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/levelball_disc_stage2` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.4, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/loveball_disc_flat` | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (6.5, 0) | 8.14 | -3.3 | sim | (1.5, 3.3, 8) |
| `block/anvil_states/loveball_disc_stage1` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.2, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/loveball_disc_stage2` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.4, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/lureball_disc_flat` | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (6.5, 0) | 8.14 | -3.3 | sim | (1.5, 3.3, 8) |
| `block/anvil_states/lureball_disc_stage1` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.2, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/lureball_disc_stage2` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.4, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/luxuryball_disc_flat` | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (6.5, 0) | 8.14 | -3.3 | sim | (1.5, 3.3, 8) |
| `block/anvil_states/luxuryball_disc_stage1` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.2, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/luxuryball_disc_stage2` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.4, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/masterball_disc_flat` | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (6.5, 0) | 8.14 | -3.3 | sim | (1.5, 3.3, 8) |
| `block/anvil_states/masterball_disc_stage1` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.2, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/masterball_disc_stage2` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.4, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/moonball_disc_flat` | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (6.5, 0) | 8.14 | -3.3 | sim | (1.5, 3.3, 8) |
| `block/anvil_states/moonball_disc_stage1` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.2, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/moonball_disc_stage2` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.4, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/nestball_disc_flat` | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (6.5, 0) | 8.14 | -3.3 | sim | (1.5, 3.3, 8) |
| `block/anvil_states/nestball_disc_stage1` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.2, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/nestball_disc_stage2` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.4, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/netball_disc_flat` | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (6.5, 0) | 8.14 | -3.3 | sim | (1.5, 3.3, 8) |
| `block/anvil_states/netball_disc_stage1` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.2, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/netball_disc_stage2` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.4, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/parkball_disc_flat` | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (6.5, 0) | 8.14 | -3.3 | sim | (1.5, 3.3, 8) |
| `block/anvil_states/parkball_disc_stage1` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.2, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/parkball_disc_stage2` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.4, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/pokeball_disc_flat` | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (6.5, 0) | 8.14 | -3.3 | sim | (1.5, 3.3, 8) |
| `block/anvil_states/pokeball_disc_stage1` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.2, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/pokeball_disc_stage2` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.4, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/premierball_disc_flat` | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (6.5, 0) | 8.14 | -3.3 | sim | (1.5, 3.3, 8) |
| `block/anvil_states/premierball_disc_stage1` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.2, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/premierball_disc_stage2` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.4, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/quickball_disc_flat` | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (6.5, 0) | 8.14 | -3.3 | sim | (1.5, 3.3, 8) |
| `block/anvil_states/quickball_disc_stage1` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.2, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/quickball_disc_stage2` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.4, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/repeatball_disc_flat` | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (6.5, 0) | 8.14 | -3.3 | sim | (1.5, 3.3, 8) |
| `block/anvil_states/repeatball_disc_stage1` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.2, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/repeatball_disc_stage2` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.4, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/safariball_disc_flat` | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (6.5, 0) | 8.14 | -3.3 | sim | (1.5, 3.3, 8) |
| `block/anvil_states/safariball_disc_stage1` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.2, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/safariball_disc_stage2` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.4, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/sportball_disc_flat` | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (6.5, 0) | 8.14 | -3.3 | sim | (1.5, 3.3, 8) |
| `block/anvil_states/sportball_disc_stage1` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.2, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/sportball_disc_stage2` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.4, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/timerball_disc_flat` | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (6.5, 0) | 8.14 | -3.3 | sim | (1.5, 3.3, 8) |
| `block/anvil_states/timerball_disc_stage1` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.2, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/timerball_disc_stage2` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.4, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/ultraball_disc_flat` | 7 | (4.6, 11.7, -1.9) | (8.4, 12.8, 1.9) | (6.5, 0) | 8.14 | -3.3 | sim | (1.5, 3.3, 8) |
| `block/anvil_states/ultraball_disc_stage1` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.2, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `block/anvil_states/ultraball_disc_stage2` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.4, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `item/anvil_states/diveballlid` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.7, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `item/anvil_states/duskballlid` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.7, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `item/anvil_states/fastballlid` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.7, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `item/anvil_states/friendballlid` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.7, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `item/anvil_states/greatballlid` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.7, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `item/anvil_states/healballlid` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.7, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `item/anvil_states/heavyballlid` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.7, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `item/anvil_states/levelballlid` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.7, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `item/anvil_states/loveballlid` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.7, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `item/anvil_states/lureballlid` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.7, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `item/anvil_states/luxuryballlid` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.7, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `item/anvil_states/masterballlid` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.7, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `item/anvil_states/moonballlid` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.7, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `item/anvil_states/nestballlid` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.7, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `item/anvil_states/netballlid` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.7, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `item/anvil_states/pokeballlid` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.7, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `item/anvil_states/premierballlid` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.7, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `item/anvil_states/quickballlid` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.7, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `item/anvil_states/repeatballlid` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.7, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `item/anvil_states/safariballlid` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.7, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `item/anvil_states/sportballlid` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.7, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `item/anvil_states/timerballlid` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.7, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |
| `item/anvil_states/ultraballlid` | 7 | (4.6, 11.8, -1.9) | (8.4, 13.7, 1.9) | (6.5, 0) | 8.14 | -3.2 | sim | (1.5, 3.2, 8) |

## 6. Bola → modelos Pixelmon disponíveis

Mapeamento de nome: `BallRegistry` id `<b>` → Pixelmon `<b>ball` (por exemplo `poke` → `pokeball_disc_flat`, `pokeballlid`).

| Bola | disc_flat | disc_stage1 | disc_stage2 | `anvil_progress/<b>ball.png` | balllid (modelo) | Precisa de fallback? |
|---|:-:|:-:|:-:|:-:|:-:|---|
| azure | **não** | **não** | **não** | **não** | **não** | disco (3 estágios), tampa |
| beast | sim | sim | sim | sim | **não** | tampa |
| citrine | **não** | **não** | **não** | **não** | **não** | disco (3 estágios), tampa |
| dive | sim | sim | sim | sim | sim (textura quebrada) | não |
| dream | sim | sim | sim | sim | **não** | tampa |
| dusk | sim | sim | sim | sim | sim (textura quebrada) | não |
| fast | sim | sim | sim | sim | sim (textura quebrada) | não |
| friend | sim | sim | sim | sim | sim (textura quebrada) | não |
| great | sim | sim | sim | sim | sim (textura quebrada) | não |
| heal | sim | sim | sim | sim | sim (textura quebrada) | não |
| heavy | sim | sim | sim | sim | sim (textura quebrada) | não |
| level | sim | sim | sim | sim | sim (textura quebrada) | não |
| love | sim | sim | sim | sim | sim (textura quebrada) | não |
| lure | sim | sim | sim | sim | sim (textura quebrada) | não |
| luxury | sim | sim | sim | sim | sim (textura quebrada) | não |
| master | sim | sim | sim | sim | sim (textura quebrada) | não |
| moon | sim | sim | sim | sim | sim (textura quebrada) | não |
| nest | sim | sim | sim | sim | sim (textura quebrada) | não |
| net | sim | sim | sim | sim | sim (textura quebrada) | não |
| park | sim | sim | sim | sim | **não** | tampa |
| poke | sim | sim | sim | sim | sim (textura quebrada) | não |
| premier | sim | sim | sim | sim | sim (textura quebrada) | não |
| quick | sim | sim | sim | sim | sim (textura quebrada) | não |
| repeat | sim | sim | sim | sim | sim (textura quebrada) | não |
| roseate | **não** | **não** | **não** | **não** | **não** | disco (3 estágios), tampa |
| safari | sim | sim | sim | sim | sim (textura quebrada) | não |
| slate | **não** | **não** | **não** | **não** | **não** | disco (3 estágios), tampa |
| sport | sim | sim | sim | sim | sim (textura quebrada) | não |
| timer | sim | sim | sim | sim | sim (textura quebrada) | não |
| ultra | sim | sim | sim | sim | sim (textura quebrada) | não |
| verdant | **não** | **não** | **não** | **não** | **não** | disco (3 estágios), tampa |

- **26/31** têm os 3 estágios de disco. **Sem disco (5):** azure, citrine, roseate, slate, verdant. São as bolas de apricorn/Hisui do Cobblemon, que não existem no Pixelmon legado. O README diz "24 of 31" mas lista 26; o número certo é 26.
- **Sem modelo de tampa (8):** azure, beast, citrine, dream, park, roseate, slate, verdant. As 23 que têm modelo estão com textura quebrada (2.2).
- Modelos Pixelmon que não correspondem a nenhuma bola registrada: `cherishball_*` (3) e `gsball_*` (3). A Cherish existe no Cobblemon mas não está no `BallRegistry`; a GS Ball não existe no Cobblemon. Também sobram `iron_disc_flat`, `aluminum_disc_flat`, `aluminium_ingot` e `aluminium_plate_stage1-3`.
- O `base_blank` (→ `pokeball_base`) também passa pela bigorna e não tem disco por bola. O candidato natural é o `iron_disc_flat` (um estágio só).
- Fallback sugerido para todos: os 3 modelos-base de 5.3 com UV explícito sobre `item/<b>_lid_blank` (flat e stage1, `uv [4, 7, 13, 11]`) e `item/<b>_lid` (stage2 e tampa, `uv [4, 6, 12, 10]`). Funciona para as 31 bolas e para a base (`item/base_blank` e `item/pokeball_base`, com os mesmos retângulos opacos).

## 7. Texturas

| Pasta | Dimensões | Qtd |
|---|---|---:|
| `textures/block` | 16x16 | 1 |
| `textures/block/anvil_progress` | 128x128 | 18 |
| `textures/block/anvil_progress` | 32x32 | 8 |
| `textures/block/anvil_progress` | 512x512 | 2 |
| `textures/block/anvil_states` | 16x16 | 3 |
| `textures/block/anvil_states` | 32x32 | 1 |
| `textures/item` | 16x16 | 83 |
| `textures/item/lids` | 16x16 | 23 |

- **Validade:** 139/139 PNG válidos: assinatura, CRC de todos os chunks, IHDR, tamanho do IDAT descompactado = h × (stride + 1), IEND, sem bytes sobrando depois do IEND, e `PIL.Image.load()` sem erro. **0 inválidos.**
- **16×16:** 110. **Não 16×16:** 29, todas texturas Pixelmon:
  - 128x128: diveball, duskball, fastball, friendball, healball, heavyball, levelball, loveball, lureball, luxuryball, masterball, moonball, nestball, netball, premierball, repeatball, safariball, timerball
  - 32x32: beastball, cherishball, dreamball, greatball, pokeball, quickball, sportball, ultraball, aluminium_ingot
  - 512x512: gsball, parkball
- Todas são quadradas e com lado potência de 2. Nenhuma reduz o mipmap do atlas.
- Formatos: ct3/bd8/il0 ×18, ct3/bd4/il0 ×8, ct2/bd8/il0 ×2, ct6/bd8/il0 ×111 (ct6 = RGBA, ct3 = paleta, ct2 = RGB; bd = bits; il0 = sem interlace). O NativeImage (STB) lê todos. Os únicos sem canal alfa são `block/ball_anvil.png` e `anvil_progress/gsball.png`, ambos RGB.
- **mcmeta:** só existe o `pack.mcmeta` da raiz. Nenhuma textura animada e nenhum `.png.mcmeta` órfão.
- Nenhum PNG totalmente transparente. 23 duplicatas exatas (`item/lids/*` = `item/*_lid`). 1 órfã (`item/ball_anvil.png`).
- Os 83 sprites de item, 16×16 RGBA, foram inspecionados visualmente numa folha de contato (`audit-out/sheet_items.png`) e estão coerentes. As texturas Pixelmon estão em `audit-out/sheet_progress.png`.

## 8. Checklist de conversão dos recursos (ordem sugerida)

1. Renomear: `data/cobbleballworks/recipes`→`recipe`, `loot_tables`→`loot_table`, `tags/items`→`tags/item`; `data/minecraft/tags/blocks`→`tags/block`.
2. Apagar as 97 receitas da seção 4.2 (glob `{create_pressing_,mekanism_crushing_,mekmm_stamper_,tconstruct_casting_}*.json`).
3. Nas 113 restantes, trocar `"result": {"item": X, "count": N}` por `"result": {"id": X, "count": N}`.
4. Trocar `forge:` por `c:` nas 8 referências da seção 4.3 e decidir o destino dos martelos de alumínio, rubi e safira.
5. Opcional: centro das montagens → `#cobblemon:tier_N_poke_ball_materials`; anéis de level, lure e moon → receita do 1.8.2.
6. Decidir o `ball_mold`. Sem Tinkers e MekMM ele não tem função; se sair, remover o modelo, a textura, a lang e a entrada na aba.
7. Apagar `textures/item/lids/` (23 duplicatas), `textures/item/README.txt`, `pack.mcmeta` e `META-INF/mods.toml`. Opcional: `textures/item/ball_anvil.png`.
8. `models/item/anvil_states/` (23, textura quebrada): não portar, ou repontar e dar UV explícito.
9. `models/block/anvil_states/` (90): se o BER for usar, trocar por 3 modelos-base transladados (5.3) com UV explícito (5.4), 1 filho por bola (31) e registro via `ModelLoadingPlugin`. Cherish, gs e alumínio ficam de fora.
10. Lang: nada obrigatório. Opcional: `pt_br.json` e `tag.item.cobbleballworks.*`.

## Apêndice A: compatibilidade de formato 1.20.1 → 1.21.1 conferida contra o Cobblemon 1.8.2

O jar do Cobblemon 1.8.2 serviu de referência viva do formato 1.21.1: `data/cobblemon/recipe/`, `loot_table/`, `tags/item/`, `tags/block/`; receitas com `"result": {"id": …, "count": …}`; ingredientes `{"item": …}` e `{"tag": …}`.

## Apêndice B: scripts e dados

Scripts (rodados com `python3 -I`, só leitura) em `<pasta de análise local>`:

- `audit_assets.py`: inventário, modelos, texturas, blockstate, itens, lang, tags, loot, receitas, comparação com o Cobblemon, geometria, mapeamento, validação de PNG → `audit-out/audit.json`
- `scan_pack.py`: tags de item, receitas de crafting e mod ids dos 119 jars do pack, incluindo jars aninhados → `audit-out/pack_scan.json`; a mesma varredura no `jar-vanilla/server-1.21.1.jar` → `audit-out/vanilla_scan.json`
- `fabric_item_check.py`: confirma que `FabricItem#getRecipeRemainder` existe no fabric-item-api-v1 0.116.17, junto com `RecipeMixin` e `RecipeRemainderHandler`
- `conflict_check.py`: conflitos de grade (shaped, espelhado e shapeless, com tags expandidas) → `audit-out/conflicts.json`
- `uv_sample.py`: amostragem do UV automático nas texturas → `audit-out/uv_sample.txt`
- `opaque_rect.py`, `geo_groups.py`, `dup_elems.py`, `classpool.py`, `contact_sheet.py`, `gen_report.py` (este relatório)

