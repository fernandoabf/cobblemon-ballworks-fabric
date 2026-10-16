# Como jogar — Cobblemon Ballworks

A forja de Pokébolas do Pixelmon antigo dentro do Cobblemon. Toda bola que tem receita no Cobblemon
passa a ser feita assim (e só assim): a receita da bancada do Cobblemon agora faz **discos**, que
viram **tampas** na bigorna, e a bola é montada com a tampa.

## A cadeia

1. **Cozinhe as bolotas (apricorns)** na fornalha: Bolota Vermelha → Bolota Vermelha Cozida, e assim
   por diante. (A fornalha não faz mais corante de bolota.)
2. **Faça os discos** na bancada com a **mesma receita da bola no Cobblemon**, trocando as bolotas
   cruas por cozidas. O material do centro continua o mesmo do Cobblemon: cobre, ferro, ouro ou
   diamante, conforme o nível da bola. Exemplo: 4 Bolotas Vermelhas Cozidas em cruz + 1 lingote de
   cobre no centro → 4 Discos de Pokébola. A tabela completa está no fim desta página (e no EMI).
3. **Faça discos de ferro:** 3 lingotes de ferro numa linha → 5 Discos de Ferro.
4. **Martele na Bigorna de Pokébola:** clique direito com o disco na bigorna e **segure o clique
   esquerdo com um martelo** até o disco virar **tampa** (o disco de ferro vira **base de ferro**).
   Ou coloque os discos na **Bigorna Mecânica**, que faz isso sozinha queimando combustível.
5. **Monte a bola** na bancada, em qualquer posição (cabe na grade 2×2 do inventário):
   **Tampa + Botão de Pedra + Base de Ferro → 1 bola**. Botão de madeira não serve.

## Bigorna de Pokébola

- **Receita:** 8 lingotes de ferro: `###` / `## ` / `###` (a grade cheia sem o canto do meio à
  direita).
- **Colocar:** clique direito com um disco (mão principal ou secundária) numa bigorna vazia. Dá para
  ficar com o martelo na mão principal e os discos na secundária.
- **Martelar:** segure o clique esquerdo com um martelo. Cada "quebra" completa é uma martelada; o
  martelo nunca quebra a bigorna. Cada martelada soma a força do martelo; a cada **5 de força**
  (Bola Onírica: 10; Bola Mestra e Bola Criatura: 15) a peça avança um estágio, e no 3º estágio vira
  tampa. O martelo perde 1 de durabilidade por estágio.
- **Pegar a tampa:** continue martelando (a peça pronta vai para o inventário sem gastar o martelo)
  ou clique direito com a mão principal.
- **Tirar um disco pela metade:** clique direito com a mão principal (o progresso se perde).
- **Quebrar:** com a mão ou picareta; a bigorna e a peça em cima caem no chão.

## Martelos

Receita: `MMM` / `MSM` / ` S ` (5 do material + 2 gravetos). Martelo melhor bate mais forte **e** mais
rápido. Consertam na bigorna comum com o material da receita. Rubi, safira e alumínio só têm receita
se algum mod do pack tiver esses materiais.

**Encantamentos:** os martelos aceitam **Eficiência**, **Inquebrável** e **Remendo**, na mesa de
encantamentos ou com livro na bigorna comum.
- **Eficiência** (e o efeito Pressa) deixa as marteladas mais **rápidas**, como deixa a mineração. A
  quantidade de marteladas não muda, porque ela depende da força do martelo. O limite é uma martelada
  a cada 5 ticks, a mesma pausa que o jogo dá entre dois blocos quebrados: com Eficiência alta, um
  martelo forte chega nesse limite.
- **Inquebrável** faz o martelo gastar menos.
- **Remendo** conserta o martelo com experiência.

**Martelo de Netherite:** como as ferramentas de netherite do vanilla, sai da **mesa de ferraria**:
Molde de Aprimoramento de Netherite + Martelo de Diamante + Lingote de Netherite. Mantém os
encantamentos e o desgaste do martelo de diamante, não queima na lava e conserta com lingote de
netherite. É o melhor martelo: termina a Bola Mestra e a Bola Criatura em 6 marteladas.

| Martelo | Força | Durabilidade | Marteladas por disco (força 5) | Onírica (10) | Mestra/Criatura (15) |
| --- | --- | --- | --- | --- | --- |
| Madeira | 1 | 59 | 15 | 30 | 45 |
| Pedra | 2 | 131 | 9 | 15 | 24 |
| Ferro | 3 | 250 | 6 | 12 | 15 |
| Ouro | 4 | 32 | 6 | 9 | 12 |
| Diamante | 5 | 1561 | 3 | 6 | 9 |
| Netherite | 8 | 2031 | 3 | 6 | 6 |
| Alumínio | 3 | 200 | 6 | 12 | 15 |
| Rubi | 4 | 300 | 6 | 9 | 12 |
| Safira | 4 | 300 | 6 | 9 | 12 |
| Ametista | 4 | 300 | 6 | 9 | 12 |
| Pedra de Fogo | 6 | 1561 | 3 | 6 | 9 |
| Pedra d'Água | 6 | 1561 | 3 | 6 | 9 |
| Pedra de Folha | 4 | 250 | 6 | 9 | 12 |
| Pedra do Trovão | 6 | 1561 | 3 | 6 | 9 |
| Pedra do Sol | 6 | 1561 | 3 | 6 | 9 |
| Pedra da Lua | 6 | 1561 | 3 | 6 | 9 |
| Pedra da Alvorada | 6 | 1561 | 3 | 6 | 9 |
| Pedra do Crepúsculo | 6 | 1561 | 3 | 6 | 9 |

## Bigorna Mecânica (a forja automática)

- **Receita:** `III` / `IPI` / `IFI` — barras de ferro (I), pistão grudento (P) e fornalha (F).
- Funciona como uma fornalha: **entrada** (discos), **combustível** (qualquer combustível de
  fornalha) e **saída**. 1 carvão = 8 discos. Cada disco leva 10 s (Bola Onírica 20 s, Bola Mestra e
  Bola Criatura 30 s). Não usa martelo.
- Só acende quando tem disco e espaço na saída; depois de acesa, queima o combustível até o fim. Balde
  de lava devolve o balde vazio.
- **Funis:** qualquer lado alimenta (o disco vai para a entrada, o combustível para o combustível); um
  funil embaixo puxa só as peças prontas (e o balde vazio). Não usa redstone.
- Precisa de picareta para quebrar; o conteúdo cai no chão.

## Receitas dos discos

Formato da cruz: ` C ` / `ECD` / ` B ` (cima, esquerda, centro, direita, baixo). Cores (sempre
**cozidas**): V vermelho, A amarelo, Vd verde, Az azul, R rosa, P preto, B branco. T1–T4 = material
de nível do Cobblemon: T1 cobre, T2 ferro, T3 ouro, T4 diamante. J/JC/JP = Jaspe/Jaspe Celeste/Jaspe
Preto (tumblestones). **Força** = força por estágio na bigorna.

| Bola | Cima | Esq. | Centro | Dir. | Baixo | Discos | Força |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Pokébola | V | V | T1 | V | V | 4 | 5 |
| Bola Grande | Az | V | T2 | V | Az | 4 | 5 |
| Bola Ultra | P | A | T3 | A | P | 4 | 5 |
| Bola Nível | P | R | T2 | R | V | 4 | 5 |
| Bola Lua | A | Az | T2 | P | A | 4 | 5 |
| Bola Amizade | A | Vd | T2 | Vd | V | 4 | 5 |
| Bola Amor | B | R | T3 | R | R | 4 | 5 |
| Bola Safári | A | Vd | T1 | Vd | B | 4 | 5 |
| Bola Pesada | P | Az | T2 | Az | P | 4 | 5 |
| Bola Veloz | V | A | T2 | A | B | 4 | 5 |
| Bola Repetição | P | V | T3 | V | A | 4 | 5 |
| Bola Tempo | V | B | T3 | B | P | 4 | 5 |
| Bola Ninho | Vd | A | T2 | A | Vd | 4 | 5 |
| Bola Tela | P | Az | T2 | Az | B | 4 | 5 |
| Bola Mergulho | B | Az | T2 | Az | Az | 4 | 5 |
| Bola Luxo | P | B | T3 | V | P | 4 | 5 |
| Bola Cura | R | B | T1 | B | R | 4 | 5 |
| Bola Crepúsculo | Vd | P | T3 | P | Vd | 4 | 5 |
| Bola Prêmio | B | B | T1 | B | B | 4 | 5 |
| Bola Esporte | B | V | T2 | V | V | 4 | 5 |
| Bola Rápida | Az | A | T3 | A | Az | 4 | 5 |
| Bola Isca | V | Az | T2 | Az | Vd | 4 | 5 |
| Bola Citrina | A | A | T1 | A | A | 4 | 5 |
| Bola Verdejante | Vd | Vd | T1 | Vd | Vd | 4 | 5 |
| Bola Cerúlea | Az | Az | T1 | Az | Az | 4 | 5 |
| Bola Rosada | R | R | T1 | R | R | 4 | 5 |
| Bola Ardósia | P | P | T1 | P | P | 4 | 5 |
| Bola Parque | V | Vd | T2 | Vd | Vd | 4 | 5 |
| Bola Onírica | V | R | T4 | R | Az | 4 | 10 |
| Bola Criatura | `GEG` / `EDE` / `GEG` (D = T4, E = `echo_shard`, G = `#c:ingots/gold`) |  |  |  |  | 8 | 15 |
| Bola Mestra | `shulker_shell` | `#c:ingots/netherite` | `nether_star` | `#c:ingots/netherite` | `shulker_shell` | 1 | 15 |
| Bola Estima ¹ | V | `#c:ingots/gold` | T4 | `#c:ingots/gold` | P | 4 | 10 |
| Pokébola Ancestral | V | J | T1 | J | V | 4 | 5 |
| Grande Bola Ancestral | V | J | T2 | J | Az | 4 | 5 |
| Ultra Bola Ancestral | A | J | T3 | J | P | 4 | 5 |
| Bola Pesada Ancestral | P | JP | T1 | JP | P | 4 | 5 |
| Bola Massiva Ancestral | P | JP | T2 | JP | P | 4 | 5 |
| Bola Titânica Ancestral | P | JP | T3 | JP | P | 4 | 5 |
| Bola Pena Ancestral | Az | JC | T1 | JC | B | 4 | 5 |
| Bola Asa Ancestral | Az | JC | T2 | JC | B | 4 | 5 |
| Bola Jato Ancestral | Az | JC | T3 | JC | B | 4 | 5 |
| Bola Marfim Ancestral | B | J | T1 | J | B | 4 | 5 |
| Bola Citrina Ancestral | A | J | T1 | J | A | 4 | 5 |
| Bola Verdejante Ancestral | Vd | J | T1 | J | Vd | 4 | 5 |
| Bola Cerúlea Ancestral | Az | J | T1 | J | Az | 4 | 5 |
| Bola Rosada Ancestral | R | J | T1 | J | R | 4 | 5 |
| Bola Ardósia Ancestral | P | J | T1 | J | P | 4 | 5 |
| Bola Origem Ancestral ¹ | JC | `#c:ingots/netherite` | `nether_star` | `#c:ingots/netherite` | JP | 1 | 15 |

¹ A Bola Estima (Cherish) e a Bola Origem Ancestral não têm receita no Cobblemon, então as receitas de
disco delas são do próprio mod, na mesma cruz: a Estima no nível da Onírica e a Origem no da Mestra.

## Configuração do servidor

`config/cobbleballworks.json` (criado na primeira execução):

| Opção | Padrão | O que faz |
| --- | --- | --- |
| `hammerDamagePerStage` | 1 | Durabilidade que o martelo perde por estágio (o Pixelmon até a 7.2.0 usava 3). |
| `allowAnvilAutoreloading` | false | Depois de martelar uma tampa para fora da bigorna, puxa o próximo disco igual do inventário. |
| `mechanicalAnvilSound` | true | A Bigorna Mecânica faz um "clank" metálico a cada peça pronta. |
| `disableOtherModsBallRecipes` | true | Desliga também as receitas de outros mods que fazem uma das bolas da forja (como as do Cobblemon Parts Refabricated), para toda bola sair da forja. |

## Avisos para o modpack

- Instale o mod **no servidor e no client**.
- O **Cobblemon Parts Refabricated** deixa de fazer bolas: as receitas de bola dele (todas as 48,
  inclusive Estima e Origem) são desligadas. As peças dele continuam no jogo sem uso, então dá para
  tirar o mod do pack.
- Com o **EMI** instalado, as receitas das duas bigornas aparecem em categorias próprias, com as
  marteladas de cada martelo.
