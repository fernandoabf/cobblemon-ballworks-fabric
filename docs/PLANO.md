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
2. Faz o **disco** na bancada: 3 apricorns cozidos numa linha → N discos (tabela na seção 3).
3. Faz **discos de ferro**: 3 lingotes de ferro numa linha → 5 Discos de Ferro.
4. Coloca o disco na **Bigorna** (clique direito) e **segura o clique esquerdo com um martelo** até
   ele virar **tampa** (ou o disco de ferro virar **base de ferro**). Ou usa a **Bigorna Mecânica**,
   que faz isso sozinha queimando combustível.
5. Monta na bancada (sem forma): **tampa + botão de pedra + base de ferro** → 1 Poké Ball do Cobblemon.

## 2. Itens e blocos (ids)

| Id | Nome pt-BR | Observação |
| --- | --- | --- |
| `cooked_<cor>_apricorn` (7: red, yellow, green, blue, pink, black, white) | Apricorn Vermelho Cozido... | fornalha: apricorn cru → cozido, 0.1 XP, 200 ticks; pilha 64 |
| `<bola>_lid_blank` | Disco de \<bola\> | id herdado da Ballworks ("lid blank" = disco do Pixelmon) |
| `<bola>_lid` | Tampa de \<bola\> | |
| `base_blank` | Disco de Ferro | |
| `pokeball_base` | Base de Ferro | |
| `<material>_hammer` | Martelo de \<material\> | seção 4 |
| `ball_anvil` | Bigorna de Poké Ball | seção 5 (nome evita confusão com a bigorna vanilla) |
| `mechanical_anvil` | Bigorna Mecânica | seção 6 |

- Nomes pt-BR das bolas: use os nomes do `pt_br.json` do próprio Cobblemon para ficar consistente.
  Há `en_us.json` e `pt_br.json` completos.
- **Remover** da Ballworks: `ball_mold` e as receitas de Create/Mekanism/MekMM/Tinkers (97 arquivos;
  lista na auditoria) e as receitas de "martelar na grade de craft" (`<bola>_lid.json` shapeless com
  martelo e `pokeball_base.json`). No Pixelmon só se martela na bigorna.

## 3. Receitas

### 3.1 Discos (shaped, 1 linha, vale em qualquer linha da grade e espelhado)

Cores: V=vermelho, A=amarelo, Vd=verde, Az=azul, R=rosa, P=preto, B=branco — sempre **cozidos**.

| Bola | Linha | Discos | | Bola | Linha | Discos |
| --- | --- | --- | --- | --- | --- | --- |
| poke | V V V | 5 | | heavy | Az Az Az | 3 |
| great | Az V Az | 2 | | fast | V A V | 3 |
| ultra | P A P | 1 | | repeat | V P V | 3 |
| level | A V P | 2 | | timer | V P B | 3 |
| moon | Az A P | 2 | | nest | Vd A Vd | 3 |
| friend | V A Vd | 3 | | net | P Az P | 3 |
| love | R R R | 3 | | dive | Az R Az | 3 |
| safari | Vd Vd A | 3 | | luxury | B V P | 3 |
| heal | B R B | 3 | | dusk | Vd P Vd | 3 |
| premier | B V B | 3 | | sport | V B V | 3 |
| quick | Az A Az | 3 | | lure | Vd V Vd | 3 |

São as 22 bolas forjáveis do Pixelmon 1.12.2 (mesmas cores e quantidades no 1.7.10).

**Bolas só do Cobblemon (proposta, não existe no Pixelmon):** são variações coloridas da Poké Ball,
então rendem 5 como ela. `citrine` A A A, `verdant` Vd Vd Vd, `slate` P P P; `azure` Az B Az e
`roseate` R B R (porque Az Az Az e R R R já são Heavy e Love). Confirmar com o usuário.

**Sem disco (como no Pixelmon):** master, park, beast, dream, cherish e as 16 `ancient_*` continuam
com as receitas nativas do Cobblemon.

### 3.2 Outras receitas

| Resultado | Receita |
| --- | --- |
| 5 Discos de Ferro | 1 linha: `#c:ingots/iron` ×3 |
| Bigorna de Poké Ball | shaped `###` / `## ` / `###` com `#c:ingots/iron` (8 lingotes, "C" aberto à direita) |
| Bigorna Mecânica | shaped `III` / `IPI` / `IFI`: I = `minecraft:iron_bars`, P = `minecraft:sticky_piston`, F = `minecraft:furnace` |
| Martelo | shaped `MMM` / `MSM` / ` S ` (5 do material + 2 gravetos) |
| Poké Ball | **shapeless**: `<bola>_lid` + `minecraft:stone_button` + `pokeball_base` → 1 (só botão de **pedra**) |

### 3.3 Desativar as receitas nativas do Cobblemon

Para cada bola com disco (as 22 + as 5 do Cobblemon), sobrescrever
`data/cobblemon/recipe/<bola>.json` com uma condição de carga sempre falsa do Fabric
(`fabric:load_conditions`; conferir os ids exatos no `fabric-resource-conditions-api-v1` 0.116.17).
Conferir em gametest que, depois de carregar, `cobblemon:poke_ball` (etc.) **não existe** como
receita e que `cobblemon:master_ball` e as `ancient_*` **continuam** existindo. Conferir também a
ordem de prioridade entre data packs de mods no Fabric (o nosso mod depende do Cobblemon).

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
se count >= 5:            # 15 era para a barra de alumínio, que não existe aqui
    count = 0             # o excesso é descartado
    estágio += 1          # som ANVIL_LAND 0.15/1.0; gasta 1 de durabilidade
    se estágio == 3:      # disco -> tampa / disco de ferro -> base de ferro
        transforma o item; estágio = 0; som WOODEN_BUTTON_CLICK_OFF 0.1/3.0
```

Marteladas por tampa = 3 × ceil(5 / força): madeira 15, pedra 9, ferro/ouro/gemas 6, diamante e
pedras de evolução 3. Salvar no NBT o item e o estágio (o Pixelmon não salvava o `count` parcial).

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

- O BER desenha o item em cima da bigorna com os **modelos do Pixelmon** que a Ballworks importou
  (`models/block/anvil_states/<bola>ball_disc_flat|stage1|stage2` + a tampa
  `models/item/anvil_states/<bola>balllid`): estágio 0 = flat, 1 = stage1, 2 = stage2, pronto =
  tampa. Disco/base de ferro: `iron_disc_flat` e a base.
- Os modelos precisam de correção (ver auditoria): translação (+1.5, +3.3, +8) no flat e
  (+1.5, +3.2, +8) nos outros, UV explícito (o UV automático pega pixels fora do sprite) e os 23
  modelos de tampa apontam para `item/lids/<bola>balllid`, que não existe (o arquivo real é
  `item/lids/<bola>_lid.png`). Registrar os modelos extras pelo `ModelLoadingPlugin` do Fabric.
- Sem modelo/textura do Pixelmon: azure, citrine, roseate, slate, verdant (sem disco) e beast, dream,
  park (sem tampa). Para as 5 coloridas, gerar a textura recolorindo a da Poké Ball, ou usar o item
  como fallback.
- Altura: a bigorna do Pixelmon tinha o topo em ~11.7/16 e os modelos de disco foram feitos para essa
  altura; a bigorna da Ballworks tem o topo em 15/16. Ajustar a bigorna para as proporções do
  Pixelmon ou transladar os discos.

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
- **Tempo:** 200 ticks (10 s) por item. Entrada → saída: disco → tampa, disco de ferro → base de
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
- [ ] Converter recursos para 1.21 (pastas, `result.id`, `c:`), apagar receitas de mods ausentes,
      `ball_mold` e o martelar na grade
- [ ] Itens: apricorns cozidos, discos, tampas, disco/base de ferro, martelos (força/durabilidade)
- [ ] Receitas: cozimento, discos (tabela 3.1), disco de ferro, bigornas, martelos, montagem
- [ ] Desativar as receitas nativas das bolas forjáveis (3.3) + gametest
- [ ] Bigorna: bloco, block entity, interação (5.1), marteladas (5.2/5.3), sons, NBT/sync
- [ ] Bigorna: BER com os modelos do Pixelmon corrigidos (5.4)
- [ ] Bigorna Mecânica: bloco, block entity com funis, menu/tela, combustível, luz/partículas, som, BER
- [ ] Tipo de receita `cobbleballworks:anvil` + plugin EMI
- [ ] Traduções en_us/pt_br completas
- [ ] Gametests da seção 8 todos verdes
- [ ] Testar num servidor com o modpack completo e no client (render, GUI, EMI)
- [ ] Versão 1.0.0 e jar em `build/libs`

## 10. Fora do escopo por enquanto

Cadeia do alumínio (bauxita, placa), martelo de netherite (1.16+), Bigorna Mecânica de 2 blocos com
animação, auto-recarga ligada por padrão, discos para master/park/beast/dream/cherish/ancient.
