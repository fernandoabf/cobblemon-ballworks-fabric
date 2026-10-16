# Plano do port — Cobblemon Ballworks para Fabric 1.21.1

Objetivo: a forja de Poké Balls **igual à do Pixelmon antigo** (1.7.10–1.12.2 Reforged), dentro do
Cobblemon 1.8.2, em Fabric 1.21.1. A referência de fidelidade é o Pixelmon 1.12.2 Reforged 8.x
(a última versão com a forja completa); diferenças das versões anteriores estão anotadas.

As fontes de cada número estão em [`pesquisa/`](pesquisa):
[`pixelmon-codigo-fonte.md`](pesquisa/pixelmon-codigo-fonte.md) (análise dos jars reais do Pixelmon
1.7.10/1.12.2/1.16.5 — a mais confiável), [`pixelmon-wiki.md`](pesquisa/pixelmon-wiki.md),
[`pixelmon-comunidade.md`](pesquisa/pixelmon-comunidade.md) e
[`auditoria-recursos-ballworks.md`](pesquisa/auditoria-recursos-ballworks.md) (o que está quebrado
nos recursos da Ballworks original e como converter).

O código Forge original fica em [`../legacy/forge-1.20.1`](../legacy/forge-1.20.1) só como
referência: o port reescreve tudo em `src/`.

---

## 1. A cadeia completa (como o jogador faz uma Poké Ball)

1. Colhe apricorns (já existem no Cobblemon) e **cozinha na fornalha** → *Apricorn Cozido*.
2. Faz o **disco** na bancada com a **receita da própria bola no Cobblemon** (mesmo formato,
   materiais e quantidade, com os apricorns **cozidos**) → N discos (seção 3.1).
3. Faz **discos de ferro**: 3 lingotes de ferro numa linha → 5 Discos de Ferro.
4. Coloca o disco na **Bigorna** (clique direito) e **segura o clique esquerdo com um martelo** até
   ele virar **tampa** (ou o disco de ferro virar **base de ferro**). Ou usa a **Bigorna Mecânica**,
   que faz isso sozinha queimando combustível.
5. Monta na bancada (sem forma): **tampa + botão de pedra + base de ferro** → 1 Poké Ball do Cobblemon.

## 2. Itens e blocos (ids)

| Id | Nome pt-BR | Observação |
| --- | --- | --- |
| `cooked_<cor>_apricorn` (7: red, yellow, green, blue, pink, black, white) | Bolota Vermelha Cozida... | fornalha: apricorn cru → cozido, 0.1 XP, 200 ticks; pilha 64 |
| `<bola>_lid_blank` | Disco de \<bola\> | id herdado da Ballworks ("lid blank" = disco do Pixelmon) |
| `<bola>_lid` | Tampa de \<bola\> | |
| `base_blank` | Disco de Ferro | |
| `pokeball_base` | Base de Ferro | |
| `<material>_hammer` | Martelo de \<material\> | seção 4 |
| `ball_anvil` | Bigorna de Poké Ball | seção 5 (nome evita confusão com a bigorna vanilla) |
| `mechanical_anvil` | Bigorna Mecânica | seção 6 |

- `<bola>` = as **46 bolas do Cobblemon que têm receita**: as 22 do Pixelmon, as 5 de apricorn do
  Cobblemon (citrine, verdant, azure, roseate, slate), park, dream, beast, master e as 15 `ancient_*`.
  Cherish e Ancient Origin não têm receita no Cobblemon e ficam de fora.
- Nomes pt-BR das bolas: use os nomes do `pt_br.json` do próprio Cobblemon para ficar consistente.
  Há `en_us.json` e `pt_br.json` completos.
- Sprites de disco e tampa: derivados das bolas do Cobblemon por `tools/derive_ball_parts.py` (seção 11).
- **Remover** da Ballworks: `ball_mold` e as receitas de Create/Mekanism/MekMM/Tinkers (97 arquivos;
  lista na auditoria) e as receitas de "martelar na grade de craft" (`<bola>_lid.json` shapeless com
  martelo e `pokeball_base.json`). No Pixelmon só se martela na bigorna.

## 3. Receitas

### 3.1 Discos: a receita da bola no Cobblemon, saindo disco

**Decisão do usuário (16/10/2026):** todas as bolas usam a receita "difícil" do Cobblemon. O disco
de cada bola é feito com a **receita da própria bola no Cobblemon** — mesmo formato, mesmos
materiais (cobre, ferro, ouro, diamante, netherite, estrela do Nether, tumblestones...) e mesma
quantidade —, mas com **apricorns cozidos** no lugar dos crus e com o **disco** como resultado. As
bolas melhores custam o que custam no Cobblemon e o caminho do Pixelmon (fornalha → disco → bigorna →
montagem) continua. As receitas de 3 apricorns numa linha do Pixelmon saíram (estão na pesquisa, só
como referência).

Formato da cruz: ` C ` / `ECD` / ` B ` (cima, esquerda, centro, direita, baixo), espelhável como
toda receita shaped. Cores, sempre **cozidas**: V=vermelho, A=amarelo, Vd=verde, Az=azul, R=rosa,
P=preto, B=branco. T1–T4 = tags `cobblemon:tier_N_poke_ball_materials` do Cobblemon (no pack: cobre,
ferro, ouro, diamante). J/JC/JP = `cobblemon:tumblestone`/`sky_tumblestone`/`black_tumblestone`.
**Força** = força por estágio na bigorna (seção 5.2).

| Bola | Cima | Esq. | Centro | Dir. | Baixo | Discos | Força |
| --- | --- | --- | --- | --- | --- | --- | --- |
| poke | V | V | T1 | V | V | 4 | 5 |
| great | Az | V | T2 | V | Az | 4 | 5 |
| ultra | P | A | T3 | A | P | 4 | 5 |
| level | P | R | T2 | R | V | 4 | 5 |
| moon | A | Az | T2 | P | A | 4 | 5 |
| friend | A | Vd | T2 | Vd | V | 4 | 5 |
| love | B | R | T3 | R | R | 4 | 5 |
| safari | A | Vd | T1 | Vd | B | 4 | 5 |
| heavy | P | Az | T2 | Az | P | 4 | 5 |
| fast | V | A | T2 | A | B | 4 | 5 |
| repeat | P | V | T3 | V | A | 4 | 5 |
| timer | V | B | T3 | B | P | 4 | 5 |
| nest | Vd | A | T2 | A | Vd | 4 | 5 |
| net | P | Az | T2 | Az | B | 4 | 5 |
| dive | B | Az | T2 | Az | Az | 4 | 5 |
| luxury | P | B | T3 | V | P | 4 | 5 |
| heal | R | B | T1 | B | R | 4 | 5 |
| dusk | Vd | P | T3 | P | Vd | 4 | 5 |
| premier | B | B | T1 | B | B | 4 | 5 |
| sport | B | V | T2 | V | V | 4 | 5 |
| quick | Az | A | T3 | A | Az | 4 | 5 |
| lure | V | Az | T2 | Az | Vd | 4 | 5 |
| citrine | A | A | T1 | A | A | 4 | 5 |
| verdant | Vd | Vd | T1 | Vd | Vd | 4 | 5 |
| azure | Az | Az | T1 | Az | Az | 4 | 5 |
| roseate | R | R | T1 | R | R | 4 | 5 |
| slate | P | P | T1 | P | P | 4 | 5 |
| park | V | Vd | T2 | Vd | Vd | 4 | 5 |
| dream | V | R | T4 | R | Az | 4 | 10 |
| beast | `GEG` / `EDE` / `GEG` (D = T4, E = `echo_shard`, G = `#c:ingots/gold`) | | | | | 8 | 15 |
| master | `shulker_shell` | `#c:ingots/netherite` | `nether_star` | `#c:ingots/netherite` | `shulker_shell` | 1 | 15 |
| ancient_poke | V | J | T1 | J | V | 4 | 5 |
| ancient_great | V | J | T2 | J | Az | 4 | 5 |
| ancient_ultra | A | J | T3 | J | P | 4 | 5 |
| ancient_heavy | P | JP | T1 | JP | P | 4 | 5 |
| ancient_leaden | P | JP | T2 | JP | P | 4 | 5 |
| ancient_gigaton | P | JP | T3 | JP | P | 4 | 5 |
| ancient_feather | Az | JC | T1 | JC | B | 4 | 5 |
| ancient_wing | Az | JC | T2 | JC | B | 4 | 5 |
| ancient_jet | Az | JC | T3 | JC | B | 4 | 5 |
| ancient_ivory | B | J | T1 | J | B | 4 | 5 |
| ancient_citrine | A | J | T1 | J | A | 4 | 5 |
| ancient_verdant | Vd | J | T1 | J | Vd | 4 | 5 |
| ancient_azure | Az | J | T1 | J | Az | 4 | 5 |
| ancient_roseate | R | J | T1 | J | R | 4 | 5 |
| ancient_slate | P | J | T1 | J | P | 4 | 5 |

Se um modpack mudar o conteúdo das tags de tier do Cobblemon, os discos acompanham.

### 3.2 Outras receitas

| Resultado | Receita |
| --- | --- |
| 5 Discos de Ferro | 1 linha: `#c:ingots/iron` ×3 |
| Bigorna de Poké Ball | shaped `###` / `## ` / `###` com `#c:ingots/iron` (8 lingotes, "C" aberto à direita) |
| Bigorna Mecânica | shaped `III` / `IPI` / `IFI`: I = `minecraft:iron_bars`, P = `minecraft:sticky_piston`, F = `minecraft:furnace` |
| Martelo | shaped `MMM` / `MSM` / ` S ` (5 do material + 2 gravetos) |
| Poké Ball | **shapeless**: `<bola>_lid` + `minecraft:stone_button` + `pokeball_base` → 1 (só botão de **pedra**) |

### 3.3 Desativar as receitas nativas do Cobblemon

As 46 receitas `cobblemon:<bola>_ball` e as 7 de fornalha `cobblemon:<cor>_apricorn_smelt_to_dye`
(que disputariam a fornalha com o cozimento do apricorn) são removidas por um mixin no
`RecipeManager.apply` (`RecipeManagerMixin` + `DisabledRecipes`), antes de virarem receitas. Sem isso
as bolas continuariam saindo direto da bancada, e a Master e a Beast (que não usam apricorn) teriam
duas receitas com a mesma grade.

**Por que não sobrescrever os arquivos:** foi a primeira tentativa (arquivos em
`data/cobblemon/recipe/` com `fabric:load_conditions` sempre falsa) e o gametest mostrou que não
funciona. O Fabric empilha os data packs dos mods na ordem de `getAllMods()`, que é alfabética
(`cobbleballworks` fica abaixo de `cobblemon`), e um pack embutido do mod também entra em ordem
alfabética como "novo data pack" (`cobbleballworks:forge_overrides` < `cobblemon`). O mixin não
depende da ordem dos packs. O Loom remapeia o alvo do mixin direto na classe (`class_1863`/
`method_20705` no jar), sem refmap.

Gametest: depois de carregar, essas 53 receitas **não existem**, as outras receitas do Cobblemon
continuam lá, nenhuma receita fora do mod produz uma bola forjada, e a grade original do Cobblemon
(apricorns crus) não produz mais nada.

> Atenção: o mod **Cobblemon Parts Refabricated** sobrescreve os mesmos arquivos. Os dois não devem
> ficar instalados juntos; o modpack que vai usar este mod vai removê-lo.

### 3.4 Tags e formato 1.21

- Pastas no singular: `recipe/`, `loot_table/`, `tags/item/`, `tags/block/`.
- Resultado de receita: `"result": {"id": ..., "count": ...}` (era `item`).
- `forge:` → `c:` (`c:ingots/iron`, `c:ingots/gold`, `c:gems/diamond`...). Materiais que não existem
  no pack (rubi, safira, alumínio): receita condicional com `fabric:tags_populated` (ou omitir).

## 4. Martelos

**Força** = quanto cada martelada conta; **durabilidade** = usos. Fonte: `ItemHammer`/`GenericHammer`
do Pixelmon 1.12.2.

| Martelo | Material da receita | Força | Durabilidade |
| --- | --- | --- | --- |
| wood | `#minecraft:planks` | 1 | 59 |
| stone | `#minecraft:stone_tool_materials` | 2 | 131 |
| iron | `#c:ingots/iron` | 3 | 250 |
| gold | `#c:ingots/gold` | 4 | 32 |
| diamond | `#c:gems/diamond` | 5 | 1561 |
| amethyst | `minecraft:amethyst_shard` | 4 | 300 |
| leaf_stone | `cobblemon:leaf_stone` | 4 | 250 |
| fire/water/thunder/sun/moon/dawn/dusk_stone | `cobblemon:<pedra>` | 6 | 1561 |
| ruby / sapphire | `#c:gems/ruby` / `#c:gems/sapphire` (condicional) | 4 | 300 |
| aluminium | `#c:ingots/aluminum` (condicional) | 3 | 200 |

- Desgaste: **1 por estágio avançado** (3 por tampa, como no 8.x; até o 7.2.0 era 3 por estágio).
  Marteladas que não avançam estágio, coletar o pronto e bigorna vazia **não** gastam. Sem desgaste
  no criativo. Deixe o valor numa constante/config.
- O martelo **nunca quebra a bigorna**.

## 5. Bigorna de Poké Ball (manual)

### 5.1 Interação

| Ação | Resultado |
| --- | --- |
| Clique **direito** com disco/disco de ferro numa bigorna **vazia** (mão principal ou secundária) | coloca 1 unidade; som `ANVIL_LAND` vol 0.15 pitch 1.0 |
| Clique **direito** (mão principal) numa bigorna **ocupada**, com qualquer coisa na mão | devolve o item atual ao inventário (cai no chão se cheio); progresso perdido; som `WOODEN_BUTTON_CLICK_OFF` 0.1/3.0 |
| **Segurar clique esquerdo** com martelo | martela (5.2); a bigorna mostra as rachaduras de mineração e toca os sons de batida de metal |
| Martelar com tampa/base pronta em cima | quando a força acumulada chega a 5, o item vai para o inventário sem gastar martelo |
| Martelar bigorna vazia | nada (nem gasta martelo) |
| Quebrar com mão/ferramenta (não martelo) | dropa a bigorna e o item em cima (disco volta cru se não terminou) |

Bloco: dureza 1.0, resistência alta (~2000 no Pixelmon), som de metal, **não exige ferramenta**,
rotaciona para o jogador (FACING), hitbox pequena como a do Pixelmon (0.2–0.8 × altura 0.7).

### 5.2 Regra das marteladas (idêntica do 1.6.2 ao 1.12.2)

```
a cada martelada: count += força_do_martelo
se count >= força:        # força da receita: 5; Dream 10; Master e Beast 15 (seção 3.1)
    count = 0             # o excesso é descartado
    estágio += 1          # som ANVIL_LAND 0.15/1.0; gasta 1 de durabilidade
    se estágio == 3:      # disco -> tampa / disco de ferro -> base de ferro
        transforma o item; estágio = 0; som WOODEN_BUTTON_CLICK_OFF 0.1/3.0
```

Marteladas por tampa = 3 × ceil(força da receita / força do martelo). Com força 5: madeira 15,
pedra 9, ferro/ouro/gemas 6, diamante e pedras de evolução 3. Master e Beast (15): madeira 45,
diamante 9, pedras de evolução 9. No Pixelmon o 15 era da barra de alumínio. Salvar no NBT o item e o estágio (o Pixelmon não salvava o `count` parcial).

### 5.3 Ritmo (por que "segurar o botão" funciona igual ao Pixelmon)

No Pixelmon a martelada acontece quando o **progresso de mineração** da bigorna chega a 100%: a
bigorna tem dureza 1.0 e não exige ferramenta, o martelo tem velocidade = força, então cada martelada
leva `ceil(30 / força)` ticks + os 5 ticks de pausa do client vanilla (madeira ~1,75 s, ferro ~0,75 s,
diamante ~0,55 s; uma tampa: ~26 s com madeira, ~1,7 s com diamante). O martelo intercepta a quebra
(`onBlockStartBreak` → conta a martelada e cancela).

**Desenho recomendado para 1.21.1 (conferir nas fontes descompiladas antes de implementar):**

- O martelo tem o componente `DataComponents.TOOL` com uma regra para a bigorna (tag
  `cobbleballworks:hammerable`) com velocidade = força e `damagePerBlock = 0`. Com dureza 1.0 e sem
  exigir ferramenta, o vanilla já calcula `força / 30` por tick.
- O martelo sobrescreve `Item.canAttackBlock(state, level, pos, player)` e retorna `false` para a
  bigorna. O client vanilla chama isso em `MultiPlayerGameMode.destroyBlock` (não quebra localmente)
  e o servidor em `ServerPlayerGameMode.destroyBlock` (depois de validar o progresso no próprio
  servidor). No **servidor** é ali que a martelada é aplicada (como o `onBlockStartBreak` do
  Pixelmon); no client não faz nada. Assim o ritmo, as rachaduras, os sons e o anti-trapaça do
  vanilla vêm de graça, sem pacote próprio.
- Alternativa, se o caminho acima falhar: `PlayerBlockBreakEvents.BEFORE` no servidor + retorno
  `false` só no client.
- Em criativo cada clique é uma martelada (vanilla já quebra na hora).

### 5.4 Visual

- O BER (`BallAnvilRenderer` + `PieceRenderer`) desenha a peça no centro da face de trabalho da
  bigorna (11 px de altura): **disco liso** no estágio 0, subindo nos estágios 1 e 2 até a
  **cúpula** quando a peça está pronta — a mesma sequência dos modelos do Pixelmon
  (ModelDiscFlat/Stage1/Stage2/HemiSphere). Ao passar de estágio a peça cresce em alguns ticks em vez
  de pular; a frente da peça (onde vai o botão) olha para o lado da bigorna virado para o jogador.
- **A peça é a Pokébola 3D do próprio Cobblemon:** o Cobblemon desenha a bola na mão com um modelo
  JSON (`models/item/poke_ball_model.json`, ou `ancient_poke_ball_model.json` nas bolas de Hisui):
  duas metades 8×4×8 com uma camada externa inflada, o botão e, nas antigas, placa, pino, ressaltos e
  trava. `tools/derive_piece_models.py` copia a metade de cima (sem botão nem trava) para
  `models/piece/lid.json` e `ancient_lid.json` e escreve um `models/piece/<bola>_lid.json` por bola
  apontando para a textura 3D dela no Cobblemon (`cobblemon:item/poke_balls/models/<bola>_ball`, por
  referência). A tampa pronta é essa metade em 65% (5,5 × 2,9 px: cabe na face de 7 px e embaixo da
  prensa da Bigorna Mecânica); o disco é a mesma metade achatada (30% da altura, 12% mais larga) que
  vai subindo até virar a tampa, então todo estágio tem a forma e as cores reais da bola. O disco e a
  base de ferro usam a metade de baixo com `textures/item/piece/iron_base.png` (a textura da Pokébola
  recolorida para aço), virada com a cúpula para cima. Item sem modelo de peça (receita de datapack)
  aparece deitado como item.
- **Histórico:** a geometria exportada do Pixelmon (`anvil_states`) estava de cabeça para baixo, com
  3,8 px de largura e UV fora das folhas box-UV, e saiu do mod. A primeira versão do port desenhava
  camadas de "quadrado arredondado" pintadas com o sprite 2D do item, que ficava embolado de perto;
  foi trocada pelo modelo 3D do Cobblemon na 1.0.1.
- A bigorna tem modelo próprio nas proporções da do Pixelmon (seção 11), então os discos não ficam
  enterrados.
- **Conferência visual:** `xvfb-run -a -s "-screen 0 1280x720x24" ./gradlew runShowcase` sobe o client
  com o mod de dev `src/showcase`, cria um mundo plano, monta as cenas, salva screenshots em
  `build/showcase/screenshots/` e fecha sozinho.

### 5.5 Opcional (config, padrão desligado como no Pixelmon)

`allowAnvilAutoreloading`: depois de coletar o pronto martelando, puxa sozinho o próximo disco igual
do inventário.

## 6. Bigorna Mecânica (a "forja automática")

Existia do 1.7.10 ao 1.12.2 e funcionava **como uma fornalha**:

- **Receita:** seção 3.2.
- **GUI de fornalha** (textura vanilla `textures/gui/container/furnace.png`, título "Bigorna
  Mecânica"): slot 0 entrada (56,17), slot 1 combustível (56,53), slot 2 saída (116,35), chama e seta
  de progresso nos lugares da fornalha.
- **Combustível:** qualquer combustível de fornalha, com o tempo de queima vanilla (1 carvão = 8
  itens). Só acende/consome quando há algo processável e espaço na saída; depois de acesa queima até o
  fim; balde de lava devolve o balde vazio.
- **Tempo:** 200 ticks (10 s) por item no Pixelmon. Aqui: 40 ticks por ponto de força da receita,
  ou seja 200 ticks para os discos comuns, 400 para a Dream e 600 para Master e Beast. Entrada → saída: disco → tampa, disco de ferro → base de
  ferro. Não usa martelo.
- **Funis:** todas as faces aceitam inserção validada por slot (disco → entrada, combustível →
  combustível); extração só da saída (e do balde vazio no slot de combustível). Sem redstone e sem
  comparador.
- **Bloco:** dureza 2.5, metal, exige picareta, rotaciona; acende (luz) e solta fumaça/chama na
  frente enquanto trabalha (como a fornalha); quebrar dropa o inventário. No Pixelmon 1.12.2 era um
  modelo animado de 2 blocos de altura; aqui pode ser 1 bloco (decisão de design: começar com 1 bloco
  e um modelo simples).
- **Som:** o Pixelmon 8.x tocava `IRON_GOLEM_DEATH` vol 0.7 a cada item. Fiel, mas pode ser chato:
  deixar em config.
- **Visual:** mostrar o disco em cima/dentro (BER), como no Pixelmon.

## 7. EMI

O pack usa EMI (com JEI junto). Fazer um plugin EMI (entrypoint `emi`, só carrega se o EMI existir;
API `dev.emi:emi-fabric:1.1.24+1.21.1:api` no maven da TerraformersMC) com as categorias "Bigorna"
(catalisadores: bigorna e martelos; mostrar as marteladas por martelo) e "Bigorna Mecânica". O ideal é
um tipo de receita próprio `cobbleballworks:anvil` (entrada, resultado), lido pelas duas bigornas e
pelo EMI, com os JSONs em `data/cobbleballworks/recipe/anvil/`.

**Feito** (`src/client/.../compat/emi/BallworksEmiPlugin.kt`): o mod compila só contra a API
(`modCompileOnly`); o EMI completo roda apenas em dev (`modLocalRuntime`), para o showcase. Cada
receita `cobbleballworks:anvil` aparece nas duas categorias:

- **Bigorna de Pokébola** (estações: a bigorna e a tag `#cobbleballworks:hammers`): peça → resultado;
  um slot de martelo que passa pelos 17 martelos, um por segundo, com as marteladas daquele martelo
  ao lado (×N); a seta lista todos de uma vez; a linha "Força 5 × 3 estágios" explica a regra no
  tooltip. Largura 134, a que cabe no EMI sem alargar a tela.
- **Bigorna Mecânica** (estação: a bigorna mecânica): no estilo da fornalha do EMI, com a chama, a
  seta que enche no tempo real e o tempo em segundos (40 ticks por força). Id sintético
  `cobbleballworks:/mechanical_anvil/<ns>/<path>`, a convenção do EMI para receitas que não são o
  arquivo de receita.

As receitas de bancada (discos, montagem da bola) e de fornalha (bolotas cozidas) já aparecem nas
categorias do próprio EMI.

## 8. Testes

Toda mecânica precisa de gametest (`./gradlew runGametest`, roda com o Cobblemon real):

- Registro: itens, blocos, block entities, menu e tipos de receita existem.
- Receitas: cada disco existe com a quantidade certa; cada `anvil` disco → tampa; cada montagem
  tampa + botão de pedra + base → bola; as receitas nativas desativadas sumiram e as mantidas
  existem; nenhuma receita do mod referencia item ausente; as 7 receitas de cozimento.
- Bigorna: colocar (mão principal e secundária); marteladas por martelo = 3 × ceil(5/força);
  durabilidade gasta 1 por estágio e nada na bigorna vazia; coleta martelando; retirada com clique
  direito; quebrar dropa bigorna + item; o martelo não quebra a bigorna e outra ferramenta quebra;
  salvar/carregar NBT.
- Bigorna Mecânica: funil em cima/lado alimenta, funil embaixo só tira a saída; 1 carvão = 8 itens;
  sem combustível não anda; saída cheia para e não queima combustível; balde de lava devolve balde;
  salvar/carregar NBT.
- Traduções: todo item/bloco tem chave em `en_us` e `pt_br`.
- Script estático: todo modelo aponta para texturas que existem; todo item tem modelo.

## 9. Checklist

- [x] Toolchain: Loom 1.13.6 + Kotlin 2.2.20 + mojmap, gametest headless com o Cobblemon real
- [x] Converter recursos para 1.21 (pastas, `result.id`, `c:`), apagar receitas de mods ausentes,
      `ball_mold` e o martelar na grade
- [x] Itens: apricorns cozidos, discos, tampas, disco/base de ferro, martelos (força/durabilidade)
- [x] Todas as 46 bolas do Cobblemon na forja, com sprites de disco/tampa derivados do Cobblemon
- [x] Receitas: cozimento, discos (tabela 3.1), disco de ferro, martelos, montagem (as das bigornas
      entram junto com cada bigorna)
- [x] Desativar as receitas nativas das bolas forjáveis (3.3) + gametest
- [x] Bigorna: bloco, block entity, interação (5.1), marteladas (5.2/5.3), sons, NBT/sync
- [x] Bigorna: BER com os estágios do Pixelmon (5.4) + showcase com screenshots no client
- [x] Bigorna Mecânica: bloco, block entity com funis, menu/tela, combustível, luz/partículas, som, BER
- [x] Tipo de receita `cobbleballworks:anvil` (47 receitas: 46 discos + disco de ferro)
- [x] Plugin EMI (7): categorias das duas bigornas, marteladas por martelo, conferido no showcase
- [x] Traduções en_us/pt_br completas (gametest de paridade + toda chave usada no código existe)
- [x] Gametests da seção 8 todos verdes (31, com o Cobblemon real, em dev e num servidor de produção)
- [ ] Testar num servidor com o modpack completo e no client (render, GUI, EMI). Feito aqui, com o
      Cobblemon real, em dev e em produção (instalador/launcher oficiais, jars publicados): servidor
      dedicado headless (`runGametest` e `runProductionGametest`) e client (`runShowcase` e
      `runProductionShowcase`: render, GUI, EMI, en_us e pt_br). Falta o modpack completo, que só
      existe na máquina do usuário.
- [x] Versão 1.0.0 e jar em `build/libs`

## 10. Fora do escopo por enquanto

Cadeia do alumínio (bauxita, placa), martelo de netherite (1.16+), Bigorna Mecânica de 2 blocos com
animação, auto-recarga ligada por padrão, cherish e ancient origin (sem receita no Cobblemon).

## 11. Decisões tomadas durante o port

Registro do que foi decidido na implementação, com o motivo. Mudar qualquer uma é simples.

- **Nomes pt-BR dos apricorns cozidos:** "Bolota Vermelha Cozida" etc., porque o `pt_br.json` do
  Cobblemon chama o apricorn cru de "Bolota Vermelha" (o Pixelmon usava "Apricorn ... Cozido").
- **Textura do apricorn cozido:** o modelo aponta para o sprite do próprio Cobblemon
  (`cobblemon:item/<cor>_apricorn`, sem copiar o arquivo), tingido no client com um tom quente, mais
  uma sobreposição nossa com marcas de grelha e vapor. A cor do apricorn continua reconhecível.
- **Todas as bolas do Cobblemon na forja** (decisão do usuário): as 46 bolas com receita no Cobblemon
  têm disco e tampa. O disco usa a receita da bola no Cobblemon com apricorns cozidos (seção 3.1), a
  Master e a Beast pedem 15 de força por estágio na bigorna e a Dream 10.
- **Sprites de disco e tampa derivados do Cobblemon** (decisão do usuário): `tools/derive_ball_parts.py`
  lê o sprite de cada bola dentro do jar do Cobblemon (`run-libs/`) e gera a tampa (a metade de cima da
  bola, com uma borda) e o disco (essa metade achatada). O visual fica uniforme nas 46 bolas e
  substitui os sprites que a Ballworks tinha importado do Pixelmon. Licença: o Cobblemon é MPL-2.0;
  os PNGs gerados levam o aviso num bloco de texto do PNG e o README dá o crédito.
- **Martelos:** além da força e da durabilidade do Pixelmon, têm o dano de ataque do Pixelmon
  (2 + bônus do material: madeira 2, pedra 3, ferro/alumínio 4, ouro 2, diamante 5, gemas e pedra da
  folha 5, fogo/água 6, demais pedras 3), velocidade de ataque de picareta, encantabilidade
  (Eficiência e Inquebrável, como no Pixelmon) e conserto na bigorna vanilla com o material da receita.
  Fora da bigorna mineram como a mão (velocidade 1) e gastam 1 por bloco, como as ferramentas do
  Pixelmon.
- **Testes de integridade dos dados:** o Minecraft só registra no log um arquivo de dados quebrado (e
  uma tag com uma entrada inexistente é descartada inteira). Os gametests de
  `DataIntegrityGameTest` falham se qualquer tag, receita ou loot table do mod não carregar, se um item
  não tiver modelo ou nome em en_us/pt_br, ou se um modelo apontar para textura inexistente.
- **Modelos de tampa herdados (`models/item/anvil_states/`) e as cópias em `textures/item/lids/`
  removidos:** apontavam para texturas inexistentes; o BER da seção 5.4 usa modelos novos.
- **Bigorna:** modelo novo nas proporções da bigorna do Pixelmon (menor que um bloco: face de trabalho
  de 12×7 px no topo, a 11 px de altura, com chifre e calcanhar), usando as texturas da bigorna vanilla
  por referência (`minecraft:block/anvil` e `anvil_top`). A hitbox acompanha o modelo. Dureza 1.0,
  resistência 1200 (o 2000 do 1.12 equivale a 1200 hoje), som de metal, sem exigir ferramenta;
  picareta quebra mais rápido. Nome: "Poké Ball Anvil" / "Bigorna de Pokébola".
- **Ritmo das marteladas conferido no servidor:** depois da primeira martelada o client vanilla não
  manda um novo START_DESTROY_BLOCK, e o servidor aceitaria qualquer STOP na hora. `AnvilStrikes`
  confere o tempo desde a martelada anterior com a mesma tolerância de 70% que o vanilla usa na
  primeira. Em criativo não há essa conferência (cada clique é uma martelada).
- **Força salva no NBT:** além do item e do estágio, a força acumulada também é salva (o Pixelmon
  perdia a força parcial ao descarregar o chunk).
- **Configuração** em `config/cobbleballworks.json`: `hammerDamagePerStage` (1),
  `allowAnvilAutoreloading` (false) e `mechanicalAnvilSound` (true).
- **Bigorna Mecânica:** 1 bloco (o Pixelmon 1.12.2 tinha 2), com corpo de fornalha (abertura acesa
  quando trabalha), plataforma onde a peça aparece (BER, estágio pelo progresso), pórtico e prensa de
  pistão grudento — as texturas são todas vanilla por referência, como os ingredientes da receita.
  Luz 15 acesa, fumaça e chama na frente, chiado de fornalha. Tempo: 40 ticks por ponto de força
  (200 para um disco). Som de "clank" (morte do golem de ferro) a cada peça, desligável na config.
  Dureza 2.5, picareta, dropa o conteúdo.

