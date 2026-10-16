# Pixelmon antigo: fatos levantados (codigo-fonte)

Gerado por um agente de pesquisa (ângulo: codigo-fonte). Cada fato traz versão, confiança e fontes.


## A. Bigorna manual: receita

- **[high]** Receita da Bigorna do Pixelmon (pixelmon:anvil; pt-BR 'Bigorna'): crafting shaped '###' / '## ' / '###' com barras de ferro = 8 iron ingots (formato de 'C' aberto no meio à direita) -> 1 bigorna. Idêntica em 1.7.10 (3.5.1, registrada em código: 'XXX','XX ','XXX'), em todo o 1.12.2 Reforged (6.0.0, 7.0.7, 8.3.6) e em 1.16.5 9.0.0 (tag forge:ingots/iron).
  - Versão: 1.7.10 3.5.1; 1.12.2 6.0.0-8.4.3; 1.16.5 9.0.0
  - Fonte: Pixelmon-1.12.2-7.0.7.jar (https://github.com/Flashback083/PixelmonCookbook/blob/master/libs/Pixelmon-1.12.2-7.0.7.jar) :: assets/pixelmon/recipes/anvil.json
  - Fonte: https://download.nodecdn.net/containers/reforged/universal/release/Pixelmon-1.12.2-6.0.0-universal.jar :: assets/pixelmon/recipes/anvil.json
  - Fonte: Pixelmon-1.7.10-3.5.1-universal.jar (https://archive.org/download/old-pixelmon-versions/1.7.10/Pixelmon%203.5.1.zip) :: com/pixelmonmod/pixelmon/config/PixelmonRecipes.class (constant pool 'XXX','XX ', Items.iron_ingot)
  - Fonte: https://download.nodecdn.net/containers/reforged/universal/release/Pixelmon-1.16.5-9.0.0-universal.jar :: data/pixelmon/recipes/machines/anvil.json

## A. Bigorna manual: propriedades do bloco

- **[high]** BlockAnvil 1.12.2: Material WOOD, dureza 1.0, resistência 2000, SoundType METAL, hitbox pequena AABB(0.2,0,0.2)-(0.8,0.7,0.8), não-opaco, rotaciona (FACING) ao ser colocado, dropa a própria bigorna (sem exigir ferramenta), renderizada por TESR (ModelAnvil + pixelmon:textures/blocks/anvil.png) com o item em cima. Em 1.7.10 3.5.1: material wood, dureza 1.0, soundTypeAnvil, caixa de largura total e 0.8 de altura. O fork Pixelmon Generations 8.2.2 tem o mesmo código (setHardness(1.0F), setResistance(2000.0F), SoundType.METAL).
  - Versão: 1.12.2 6.0.0-8.4.3 (1.7.10 3.5.1 com pequenas diferenças)
  - Fonte: (arquivo de análise local) linhas 6-35 (construtor) e 37-50 (AABB) — javap de com/pixelmonmod/pixelmon/blocks/machines/BlockAnvil.class do jar 7.0.7
  - Fonte: https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_stable/39-1.12/mcp_stable-39-1.12.zip :: fields.csv (field_151575_d=WOOD, field_185852_e=METAL)
  - Fonte: (arquivo de análise local) (1.7.10; field_149788_p=soundTypeAnvil via mcp_stable-12-1.7.10)
  - Fonte: https://github.com/MartyAlana/Open-Pixelmon-Generations/blob/master/patches/java/com/pixelmongenerations/common/block/machines/BlockAnvil.java.patch (linhas 46-49)

## A. Bigorna manual: como colocar o disco

- **[high]** Colocar = clique DIREITO (onBlockActivated) na bigorna VAZIA segurando o item: coloca 1 unidade e consome 1 do stack. Aceita somente: qualquer disco de apricorn (ItemPokeballDisc), Disco de Ferro, Disco de Alumínio e Barra de Alumínio (8.x aceita qualquer 'ingotAluminum' do OreDict). Tampas/bases/placas não podem ser colocadas à mão. Em 1.12 o código usa player.getHeldItem(hand), então funciona também pela mão secundária (ex.: martelo na mão principal e discos na off-hand; clique direito carrega). Som ao colocar: BLOCK_ANVIL_LAND vol 0.15 pitch 1.0 (7.x/8.x); vol 0.8 em 6.0.0; 'random.anvil_land' 0.15/0.9 em 1.7.10.
  - Versão: 1.12.2 6.0.0-8.4.3; 1.7.10 3.5.1 (só mão principal)
  - Fonte: (arquivo de análise local) linhas 183-330 (func_180639_a, bytecode 158-312)
  - Fonte: Pixelmon-1.12.2-8.3.6-server.jar (https://github.com/Flashback083/FlashSpec/blob/master/libs/Pixelmon-1.12.2-8.3.6-server.jar) :: BlockAnvil.class (PixelmonOres.itemMatches 'ingotAluminum')
  - Fonte: mcp_stable-39-1.12 fields.csv: field_187689_f=BLOCK_ANVIL_LAND
  - Fonte: (arquivo de análise local) linhas 161-268

## A. Bigorna manual: como retirar o item

- **[high]** Retirar = clique DIREITO com a MÃO PRINCIPAL numa bigorna ocupada: o item que estiver nela no momento (disco cru ou meio martelado, tampa, base ou placa) vai para o inventário (DropItemHelper.giveItemStack; se cheio cai no chão), seja lá o que se esteja segurando (inclusive o martelo). Não há troca: com a bigorna ocupada o clique só retira. O estágio visual volta a 0 (progresso perdido); em 7.0.7 o contador interno de força ('count') não é zerado nessa retirada (quirk). Som: BLOCK_WOOD_BUTTON_CLICK_OFF vol 0.1 pitch 3.0 em 7.x/8.x; nenhum som em 6.0.0 e 1.7.10.
  - Versão: 1.12.2 6.0.0-8.4.3; 1.7.10 3.5.1
  - Fonte: (arquivo de análise local) linhas 183-260 (bytecode 37-157: itemOnAnvil=null, state=0, giveItemStack)
  - Fonte: mcp_stable-39-1.12 fields.csv: field_187883_gR=BLOCK_WOOD_BUTTON_CLICK_OFF
  - Fonte: Pixelmon-1.12.2-6.0.0-universal.jar (CDN) :: BlockAnvil.class (sem playSound na retirada)

## A. Bigorna manual: martelar é clique ESQUERDO

- **[high]** Martelar = clique ESQUERDO (minerar) a bigorna com um martelo na mão principal. ItemHammer.onBlockStartBreak intercepta a quebra da bigorna Pixelmon: no servidor chama TileEntityAnvil.blockHit(força, player) e SEMPRE retorna true, então a bigorna nunca é quebrada por martelo; martelar bigorna vazia não faz nada e não gasta durabilidade. O martelo não pode ser usado com clique direito para martelar (clique direito apenas coloca/retira). Mesmo esquema em 1.6.2 (2.3.1), 1.7.10 (3.5.1) e 1.12.2.
  - Versão: 1.6.2 2.3.1; 1.7.10 3.5.1; 1.12.2 6.0.0-8.4.3; 1.16.5 9.0.0
  - Fonte: (arquivo de análise local) linhas 71-147 (onBlockStartBreak -> blockHit em l.97; retorno iconst_1)
  - Fonte: (arquivo de análise local) (onBlockStartBreak)
  - Fonte: (arquivo de análise local) (onBlockStartBreak ainda presente em 1.16.5 9.0.0)

## A. Bigorna manual: segurar o botão repete as marteladas

- **[medium]** Como a martelada acontece quando o 'progresso de mineração' do bloco chega a 100%, segurar o botão esquerdo martela repetidamente: a cada quebra completada conta 1 martelada, o progresso zera e o cliente vanilla espera 5 ticks antes de recomeçar. Durante cada martelada aparecem as rachaduras de mineração na bigorna e os sons de 'hit' do SoundType METAL; o efeito/som de quebra (playEvent 2001) não toca porque onBlockStartBreak cancela antes. Em criativo cada clique é uma martelada instantânea.
  - Versão: 1.12.2 (inferido da mecânica vanilla 1.12 + código Pixelmon)
  - Fonte: (arquivo de análise local) linhas 71-147
  - Fonte: Inferência a partir de Forge 1.12.2 PlayerInteractionManager.tryHarvestBlock / PlayerControllerMP.onPlayerDestroyBlock (onBlockStartBreak chamado antes da remoção; blockHitDelay=5)

## A/B. Força da martelada por tipo de martelo

- **[high]** Cada martelada soma uma 'força' = (int) getDestroySpeed do martelo contra a bigorna. ItemHammer: Madeira 1, Pedra 2, Ferro 3, Alumínio 3, Ouro 4, Diamante 5. GenericHammer: Rubi, Safira, Ametista, Cristal e Pedra Folha = 4; Pedra Fogo, Água, Trovão, Sol, Lua, Alvorada (Dawn) e Crepúsculo (Dusk) = 6; qualquer outro = 1. O mesmo valor é a velocidade de mineração, então martelo melhor bate mais forte E mais rápido. Em 1.6.2/1.7.10 só existiam Madeira 1, Pedra 2, Ferro 3, Ouro 4, Diamante 5 (o martelo de alumínio do 3.5.1 caía no padrão 1).
  - Versão: 1.12.2 6.0.0-8.4.3; 1.7.10 3.5.1; 1.6.2 2.3.1
  - Fonte: (arquivo de análise local) linhas 155-196 (func_150893_a: fconst_1/fconst_2/3.0/4.0/5.0)
  - Fonte: (arquivo de análise local) linhas 20-88 (4.0f l.55, 6.0f l.85)
  - Fonte: (arquivo de análise local) (getStrVsBlock WOOD 1, STONE 2, IRON 3, GOLD 4, EMERALD 5)
  - Fonte: (arquivo de análise local) linhas 113-148

## A. Regra de progresso (marteladas necessárias)

- **[high]** TileEntityAnvil.blockHit: count += força; quando count >= neededHits, count volta a 0 (o excesso é descartado) e state avança 1 (toca som); ao chegar em state==3 o item se transforma e state volta a 0. neededHits = 5 para discos (de apricorn, ferro e alumínio) -> tampa/base; 15 para Barra de Alumínio -> Placa de Alumínio. Ou seja, 3 estágios de 5 de força por tampa. Valores idênticos em 2.3.1 (1.6.2), 3.5.1 (1.7.10), 6.0.0, 7.0.7, 8.3.6 e 8.4.3. O NBT salva só o item e o estágio (ItemOnAnvil/ItemStackOnAnvil + AnvilItemState), não o 'count' parcial.
  - Versão: 1.6.2 2.3.1; 1.7.10 3.5.1; 1.12.2 6.0.0-8.4.3
  - Fonte: (arquivo de análise local) linhas 139-640 (bipush 15 em l.147; iconst_5 em l.178; state==3 em l.239-240 e l.579-580; getLidFromEnum l.608); NBT l.28-89
  - Fonte: (arquivo de análise local) linhas 134-670 (8.3.6, mesma lógica)
  - Fonte: (arquivo de análise local) (bipush 15 / iconst_5 / state 3)
  - Fonte: (arquivo de análise local) linhas 88-188

## A. Marteladas por item (derivado do código)

- **[high]** Marteladas para disco -> tampa/base (3 x ceil(5/força)): Madeira 15; Pedra 9; Ferro e Alumínio 6; Ouro, Rubi, Safira, Ametista, Cristal, Pedra Folha 6; Diamante 3; Pedras Fogo/Água/Trovão/Sol/Lua/Alvorada/Crepúsculo 3. Barra de Alumínio -> Placa (3 x ceil(15/força)): Madeira 45; Pedra 24; Ferro/Alumínio 15; força 4 = 12; Diamante 9; força 6 = 9. Depende do martelo, sim.
  - Versão: 1.12.2 6.0.0-8.4.3 (madeira..diamante idem em 1.7.10)
  - Fonte: Cálculo a partir de TileEntityAnvil.blockHit (neededHits 5/15, 3 estágios, excesso descartado) e ItemHammer/GenericHammer.func_150893_a — mesmos dumps citados acima

## A. Tempo por martelada (derivado)

- **[medium]** Bigorna com dureza 1.0 e Material WOOD (não exige ferramenta) => progresso por tick = força/30 => ceil(30/força) ticks + 5 ticks de pausa vanilla: Madeira ~35 ticks (1,75 s), Pedra ~20 (1,0 s), Ferro/Alumínio ~15 (0,75 s), força 4 ~13 (0,65 s), Diamante ~11 (0,55 s), força 6 ~10 (0,5 s). Uma tampa de Poké Ball leva ~26 s com martelo de madeira e ~1,7 s com diamante (sem Haste/Efficiency, no chão).
  - Versão: 1.12.2 (derivado)
  - Fonte: Derivado de ForgeHooks.blockStrength (digSpeed/hardness/30 quando canHarvest) + BlockAnvil dureza 1.0 + ItemHammer.func_150893_a; dumps jp707/blocks_machines_BlockAnvil.txt l.6-10 e jp707/items_ItemHammer.txt l.155-196

## A. Estágios visuais na bigorna

- **[high]** O TESR (RenderTileEntityAnvil) desenha sobre a bigorna: disco state 0 = ModelDiscFlat, state 1 = ModelDiscStage1, state 2 = ModelDiscStage2, resultado (tampa/base) = ModelDiscHemiSphere. Textura: pixelmon:textures/pokeballs/anvil/<bola> para discos/tampas de apricorn; Disco/Base de FERRO e de ALUMÍNIO usam a mesma textura pokeballs/irondisc.png. Alumínio: state 0 = ModelIngot, 1 = ModelPlateStage2, 2 = ModelPlateStage3, Placa pronta = ModelPlate (textura blocks/aluminium/ingot.png). Os mesmos 4 modelos de disco já existiam em 1.6.2 e 1.7.10.
  - Versão: 1.12.2 6.0.0-8.4.3 (modelos ModelDisc* desde 1.6.2 2.3.1)
  - Fonte: (arquivo de análise local) (state 0/1/2 -> ModelDiscFlat/Stage1/Stage2; ModelDiscHemiSphere; ModelIngot/ModelPlateStage2/ModelPlateStage3/ModelPlate)
  - Fonte: Pixelmon-1.12.2-7.0.7.jar :: RenderTileEntityAnvil.class constant pool ('pixelmon:textures/pokeballs/anvil/', 'pixelmon:textures/pokeballs/irondisc.png', 'pixelmon:textures/blocks/aluminium/ingot.png')
  - Fonte: Pixelmon 2.3.1 / 3.5.1: pixelmon/client/models/discs/ModelDiscFlat|Stage1|Stage2|HemiSphere.class

## A. Sons da bigorna

- **[high]** 7.x/8.x: estágio avançado em disco = BLOCK_ANVIL_LAND vol 0.15 pitch 1.0; estágio da barra de alumínio = ANVIL_LAND 0.25/1.0; quando o disco vira tampa/base toca também BLOCK_WOOD_BUTTON_CLICK_OFF 0.1/3.0; coletar o pronto (martelando) e retirar com clique direito = WOOD_BUTTON_CLICK_OFF 0.1/3.0; auto-recarga = ANVIL_LAND 0.15/0.9. 6.0.0 (dez/2017): colocar = ANVIL_LAND 0.8; estágio = ANVIL_LAND 0.2; coletar martelando = ENTITY_PLAYER_LEVELUP 0.5/1.0 (o 'ding' de level up). 1.7.10: 'random.anvil_land' 0.15/0.9 ao colocar, 0.2 por estágio, 0.25 alumínio, 'random.levelup' 0.5 ao coletar. Equivalentes modernos: ANVIL_LAND, WOODEN_BUTTON_CLICK_OFF, PLAYER_LEVELUP.
  - Versão: 1.7.10 3.5.1; 1.12.2 6.0.0; 1.12.2 7.0.7-8.4.3
  - Fonte: (arquivo de análise local) l.232-236, l.315-318, l.572-576, l.615-618
  - Fonte: (arquivo de análise local) l.203-205, l.286-288, l.542-545
  - Fonte: (arquivo de análise local) l.133-134, 188-189, 283-285, 364-365
  - Fonte: mcp_stable-39-1.12 fields.csv: field_187689_f=BLOCK_ANVIL_LAND, field_187883_gR=BLOCK_WOOD_BUTTON_CLICK_OFF, field_187802_ec=ENTITY_PLAYER_LEVELUP

## A. Retirar o resultado martelando + auto-recarga

- **[high]** Com tampa, Base de Ferro/Alumínio ou Placa de Alumínio pronta em cima, continuar martelando: ao acumular força >= 5 o item vai direto para o inventário (sem gastar durabilidade). Se a config General 'allowAnvilAutoreloading' (padrão false no código; texto: 'Allow the anvil to replace finished items with starting item from player's inventory without manual placing.') estiver true, a bigorna em seguida puxa sozinha o próximo disco correspondente (mesma bola; ou disco de ferro/alumínio; ou barra) do inventário principal ou da off-hand. Existe desde 3.5.1 (1.7.10); em 2.3.1 (1.6.2) só dava para retirar com clique direito.
  - Versão: 1.7.10 3.5.1; 1.12.2 6.0.0-8.4.3
  - Fonte: (arquivo de análise local) l.267-532
  - Fonte: (arquivo de análise local) l.15 e l.986-987 (default iconst_0); @Node(category=General, nameOverride=allowAnvilAutoreloading)
  - Fonte: Pixelmon-1.12.2-7.0.7.jar :: assets/pixelmon/lang/en_US.lang (pixelmon.config.allowAnvilAutoreloading.comment)

## A. Quebrar a bigorna com item em cima

- **[high]** Quebrar a bigorna (com mão/qualquer ferramenta que não seja martelo) dropa a própria bigorna e também spawna um EntityItem do item que estava nela (1 unidade, na forma atual: disco se não terminou — progresso perdido; tampa/base se já pronta) em (x, y+0.5, z) com pickup delay de 10 ticks. Com martelo é impossível quebrá-la (onBlockStartBreak cancela).
  - Versão: 1.12.2 6.0.0-8.4.3; 1.7.10 3.5.1
  - Fonte: (arquivo de análise local) l.77-127 (func_176206_d: new EntityItem(... y+0.5 ...), setPickupDelay(10) em l.117) e func_180660_a (drop = PixelmonBlocks.anvil)
  - Fonte: (arquivo de análise local) (onBlockDestroyed: EntityItem, delay 10)

## B. Martelos: lista (1.12.2)

- **[high]** 18 martelos no 1.12.2 Reforged: wood_hammer, stone_hammer, iron_hammer, gold_hammer, diamond_hammer, aluminium_hammer (classe ItemHammer) + ruby_hammer, sapphire_hammer, amethyst_hammer, crystal_hammer, fire_stone_hammer, water_stone_hammer, leaf_stone_hammer, thunder_stone_hammer, sun_stone_hammer, moon_stone_hammer, dawn_stone_hammer, dusk_stone_hammer (classe GenericHammer, que também tem os efeitos de clique direito das ferramentas de pedra de evolução). Não existe martelo de Ice Stone/Shiny Stone. Nomes pt-BR: Martelo de Madeira/Pedra/Ferro/Ouro/Diamante/Alumínio, 'Rubi Martelo', 'Safira Martelo', 'Pedra de Fogo Martelo' etc.
  - Versão: 1.12.2 6.0.0-8.4.3
  - Fonte: (arquivo de análise local) l.336-460 (new ItemHammer/GenericHammer com nomes de registro)
  - Fonte: Pixelmon-1.12.2-7.0.7.jar :: assets/pixelmon/lang/en_US.lang e pt_BR.lang (item.*_hammer.name)

## B. Martelos: lista em versões antigas

- **[high]** Em 1.7.10 (3.5.1, dez/2015) havia só 6 martelos: Wood, Stone, Iron, Gold, Diamond e Aluminium Hammer (todos ItemHammer, nomes internos hammerwood/hammerstone/...). Em 1.6.2 (2.3.1, ago/2013) os martelos vanilla-tier já existiam. Os martelos de gema e de pedra de evolução aparecem até 6.0.0 (1.12.2).
  - Versão: 1.6.2 2.3.1; 1.7.10 3.5.1
  - Fonte: (arquivo de análise local) l.600-640 (new ItemHammer x6)
  - Fonte: Pixelmon 2.3.1 (https://archive.org/download/old-pixelmon-versions/1.6.2/Pixelmon%202.3.1%20install.zip) :: pixelmon/items/ItemHammer.class
  - Fonte: https://download.nodecdn.net/containers/reforged/universal/release/Pixelmon-1.12.2-6.0.0-universal.jar :: assets/pixelmon/recipes/*_hammer.json

## B. Martelos: receitas

- **[high]** Todos os martelos: shaped 'MMM' / 'MSM' / ' S ' (5 do material + 2 gravetos) -> 1 martelo. Materiais: madeira = qualquer tábua (ore plankWood), pedra = cobblestone, ferro/ouro = lingote, diamante, alumínio = pixelmon:aluminium_ingot (8.x ore ingotAluminum), rubi = pixelmon:ruby, safira/ametista/cristal (8.x gemSapphire/gemAmethyst/gemCrystal), e o item da pedra de evolução correspondente. 1.7.10 usa o mesmo formato ('WWW','WSW',' S ').
  - Versão: 1.12.2 6.0.0-8.4.3; 1.7.10 3.5.1
  - Fonte: Pixelmon-1.12.2-7.0.7.jar :: assets/pixelmon/recipes/{wooden,stone,iron,gold,diamond,aluminium,ruby,sapphire,amethyst,crystal,fire_stone,water_stone,leaf_stone,thunder_stone,sun_stone,moon_stone,dawn_stone,dusk_stone}_hammer.json
  - Fonte: Pixelmon-1.12.2-8.3.6-server.jar :: assets/pixelmon/recipes/*_hammer.json (ore_dict)
  - Fonte: Pixelmon-1.7.10-3.5.1 :: PixelmonRecipes.class (constant pool 'WWW','WSW',' S ')

## B. Martelos: durabilidade

- **[high]** Durabilidade = maxUses do ToolMaterial: Madeira 59, Pedra 131, Ferro 250, Ouro 32, Diamante 1561 (vanilla); Alumínio 200; Rubi/Safira/Ametista/Cristal 300; Pedra Folha 250; Pedra Fogo/Água 1561; Pedra Trovão/Sol/Lua/Alvorada/Crepúsculo 1561 (material 'ELEMENTSTONE'). Os martelos são ItemTool(2.0F de dano, 1.0F de velocidade), efetivos só na bigorna.
  - Versão: 1.12.2 6.0.0-8.4.3 (alumínio 200 também em 1.7.10)
  - Fonte: (arquivo de análise local) (static {}: EnumHelper.addToolMaterial ALUMINUM 2/200/6.5, RUBY/SAPHIRE/AMETHYST/CRYSTAL 2/300/6.5, FIRESTONE/WATERSTONE 3/1561/8.0, LEAFSTONE 2/250/6.0, ELEMENTSTONE 3/1561/12.0)
  - Fonte: (arquivo de análise local) l.5-10 (super(2.0F,1.0F,...))
  - Fonte: Pixelmon-1.7.10-3.5.1 :: com/pixelmonmod/pixelmon/Pixelmon.class (ALUMINUM 2, 200, 6.5, 14)

## B. Martelos: desgaste por uso

- **[high]** Só gasta durabilidade a martelada que AVANÇA um estágio (marteladas intermediárias, coleta do pronto e bigorna vazia não gastam). Valor (evento HammerDamage): 3 por estágio em 2.3.1, 3.5.1, 6.0.0, 7.0.7 e 7.2.0 (= 9 por tampa); 1 por estágio em 8.3.6 e 8.4.3 (= 3 por tampa). Sem desgaste em criativo (7.x+). Quando o dano já é >= máximo numa martelada que avança estágio, o martelo simplesmente some da mão (setHeldItem EMPTY / destroyCurrentEquippedItem), sem animação de quebra vanilla.
  - Versão: 1.6.2 2.3.1 até 1.12.2 7.2.0 = 3; 1.12.2 8.3.6-8.4.3 = 1
  - Fonte: (arquivo de análise local) l.99-123 (iconst_3 em l.122; isCreative l.86-90)
  - Fonte: diff 7.0.7 vs 8.3.6 de ItemHammer.class: iconst_3 -> iconst_1 (Pixelmon-1.12.2-8.3.6-server.jar)
  - Fonte: Pixelmon-1.12.2-7.2.0-server.jar (https://github.com/DaeM0nS/PixelSkills/blob/master/libs/Pixelmon-1.12.2-7.2.0-server.jar) :: ItemHammer.class bytecode 113 iconst_3
  - Fonte: (arquivo de análise local) (iconst_3 + attemptDamageItem; destroyCurrentEquippedItem)

## B. Martelos: tampas por martelo (derivado)

- **[medium]** Tampas aproximadas até o martelo sumir (sem Unbreaking). Regra 3/estágio (<=7.2.0 e 1.7.10): Madeira ~7, Pedra ~15, Ferro ~28, Ouro ~4, Diamante ~174, Alumínio ~22, gemas ~33, Pedra Folha ~28, demais pedras ~174. Regra 1/estágio (8.x): Madeira 20, Pedra 44, Ferro 83, Ouro 11, Diamante 520, Alumínio 67, gemas 100, Pedra Folha 83, demais pedras 520.
  - Versão: 1.12.2 (derivado)
  - Fonte: Cálculo a partir de maxUses (config_PixelmonItemsTools) e HammerDamage 3 ou 1 por estágio (ItemHammer), 3 estágios por tampa

## B. Martelos em 1.16+

- **[high]** 1.16.5 adiciona netherite_hammer (smithing: diamond_hammer + netherite_ingot), silver_hammer, platinum_hammer (e texturas de copper/'beef' hammer). Em 1.16.5 9.1.13 o HammerItem virou ferramenta de mineração (ToolType HAMMER, tumblestones, multiplicador de durabilidade em config) e a bigorna (AnvilBlock) ficou só decorativa, sem forja.
  - Versão: 1.16.5 9.0.0 / 9.1.13
  - Fonte: https://download.nodecdn.net/containers/reforged/universal/release/Pixelmon-1.16.5-9.1.13-universal.jar :: data/pixelmon/recipes/netherite_hammer_smithing.json, equipment/*/…_hammer.json
  - Fonte: (arquivo de análise local) (sem método de uso) e items_HammerItem.txt (PixelmonTags.HAMMER, tumblestones, getHammerDurabilityLossMultiplier)

## C. Discos: apricorns COZIDOS

- **[high]** Todos os discos usam apricorn COZIDO (pixelmon:cooked_<cor>_apricorn), nunca cru. O cozimento é na fornalha vanilla: apricorn cru -> cozido, 0.1 XP (1.12.2 FurnaceRecipes.addSmelting; 1.16.5 JSON smelting cookingtime 200). Não existe máquina especial de cozinhar apricorn no 1.12.2 (nenhuma outra classe referencia apricorn cozido). Nomes pt-BR: 'Apricorn Vermelho Cozido' etc.; stack 64.
  - Versão: 1.6.2 2.3.1; 1.7.10 3.5.1; 1.12.2 6.0.0-8.4.3; 1.16.5
  - Fonte: (arquivo de análise local) (registerFurnaceRecipes: func_151396_a(..., 0.1f) x7)
  - Fonte: Pixelmon-1.12.2-7.0.7.jar :: assets/pixelmon/recipes/*_ball_disc.json
  - Fonte: https://download.nodecdn.net/containers/reforged/universal/release/Pixelmon-1.16.5-9.1.13-universal.jar :: data/pixelmon/recipes/furnace/red_apricorn.json

## C. Discos: formato e quantidade por bola

- **[high]** Disco = crafting shaped de UMA linha com 3 apricorns cozidos (3x1; vale em qualquer linha da grade e espelhado) -> N discos: Poké: Vermelho,Vermelho,Vermelho -> 5; Great: Azul,Vermelho,Azul -> 2; Ultra: Preto,Amarelo,Preto -> 1; Level: Amarelo,Vermelho,Preto -> 2; Moon: Azul,Amarelo,Preto -> 2; Friend: Vermelho,Amarelo,Verde -> 3; Love: Rosa x3 -> 3; Safari: Verde,Verde,Amarelo -> 3; Heavy: Azul x3 -> 3; Fast: Vermelho,Amarelo,Vermelho -> 3; Repeat: Vermelho,Preto,Vermelho -> 3; Timer: Vermelho,Preto,Branco -> 3; Nest: Verde,Amarelo,Verde -> 3; Net: Preto,Azul,Preto -> 3; Dive: Azul,Rosa,Azul -> 3; Luxury: Branco,Vermelho,Preto -> 3; Heal: Branco,Rosa,Branco -> 3; Dusk: Verde,Preto,Verde -> 3; Premier: Branco,Vermelho,Branco -> 3; Sport: Vermelho,Branco,Vermelho -> 3; Quick: Azul,Amarelo,Azul -> 3; Lure: Verde,Vermelho,Verde -> 3. Mesmas cores/quantidades em 1.7.10 (EnumPokeballs.recipe/quantityMade, 'XYZ') e 1.6.2 (sem Sport/Quick/Lure).
  - Versão: 1.12.2 6.0.0-8.4.3; 1.7.10 3.5.1; 1.6.2 2.3.1 (subconjunto)
  - Fonte: Pixelmon-1.12.2-7.0.7.jar :: assets/pixelmon/recipes/<bola>_ball_disc.json (22 arquivos; idênticos em 6.0.0 e 8.3.6)
  - Fonte: (arquivo de análise local) (static {}: recipe[] + quantityMade) e jp1710/config_PixelmonRecipes.txt l.3190-3339 (loop 'XYZ' getCookedApricorn)
  - Fonte: (arquivo de análise local)

## C. Bolas sem disco/receita

- **[high]** Não há disco nem receita de anvil para Master, Park, Cherish e GS Ball (excluídas explicitamente no loop de receitas de 1.7.10), nem para Beast/Dream. Em 8.x (1.12.2) a Dream Ball é feita no Infusor (chesto berry + Dusk Ball) e há a Mug Ball decorativa. Total craftável via bigorna: 22 bolas no 1.12.2/1.7.10 (19 em 1.6.2).
  - Versão: 1.7.10 3.5.1; 1.12.2 6.0.0-8.4.3
  - Fonte: (arquivo de análise local) l.3204-3253 (MasterBall/ParkBall/CherishBall/GSBall pulados)
  - Fonte: Pixelmon-1.12.2-8.3.6-server.jar :: assets/pixelmon/recipes/dream_ball.json (pixelmon:infusion) e mug_ball.json

## C. Disco -> Tampa

- **[high]** Na bigorna cada disco vira 1 tampa da mesma bola (PixelmonItemsPokeballs.getLidFromEnum): ex. 'Poké Ball Disc'/'Disco de Pokébola' -> 'Poké Ball Lid'/'Tampa de Pokébola'. Discos e tampas empilham até 64 (aba criativa Poké Balls). Em 1.6.2 a tampa era o ID do disco - 20.
  - Versão: 1.12.2 6.0.0-8.4.3; 1.7.10 3.5.1; 1.6.2 2.3.1
  - Fonte: (arquivo de análise local) l.603-609
  - Fonte: (arquivo de análise local) e items_ItemPokeballLid.txt (maxStackSize 64)
  - Fonte: Pixelmon-1.12.2-7.0.7.jar :: assets/pixelmon/lang/pt_BR.lang (item.*_ball_disc.name / item.*_ball_lid.name)

## C. Bases de ferro e alumínio

- **[high]** Disco de Ferro: shaped 1 linha com 3 iron ingots -> 5 Discos de Ferro. Disco de Alumínio: 1 linha com 3 Barras de Alumínio -> 3 Discos de Alumínio (8.x aceita ore ingotAluminum). Na bigorna (5 de força x 3 estágios) ou na Bigorna Mecânica: Disco de Ferro -> 1 Base de Ferro; Disco de Alumínio -> 1 Base de Alumínio. As duas bases são intercambiáveis para TODAS as bolas (constante POKEBALL_BASE = pixelmon:iron_base | pixelmon:aluminum_base). Em 1.6.2 (2.3.1) só existiam disco/base de ferro.
  - Versão: 1.7.10 3.5.1; 1.12.2 6.0.0-8.4.3
  - Fonte: Pixelmon-1.12.2-7.0.7.jar :: assets/pixelmon/recipes/iron_disc.json, aluminum_disc.json, _constants.json
  - Fonte: (arquivo de análise local) l.587-602 (ironDisc->ironBase, aluDisc->aluBase)
  - Fonte: (arquivo de análise local) (ironDisc x5, aluDisc x3; duas receitas shapeless por bola)
  - Fonte: (arquivo de análise local) l.155-172

## C/F. Cadeia do alumínio

- **[high]** Minério de Bauxita (Bauxite Ore, gerado no mundo por WorldGenBauxiteOre) -> fornalha -> Barra de Alumínio (1.0 XP no 1.12.2; 0.7 em 1.16.5). Barra de Alumínio na bigorna manual (15 de força x 3) -> Placa de Alumínio ('Placa de Alumínio'), ingrediente de PC, Healer, peças da Trade Machine, Fossil Machine/Cleaner, Camera, Item Finder, Gym Sign etc. Barras também fazem ferramentas/armadura/martelo de alumínio e o Disco de Alumínio.
  - Versão: 1.12.2 6.0.0-8.4.3 (cadeia já existia em 1.6.2/1.7.10)
  - Fonte: (arquivo de análise local) l.98-107 (bauxite -> aluminiumIngot, fconst_1)
  - Fonte: Pixelmon-1.12.2-7.0.7.jar :: assets/pixelmon/recipes/pc.json, healer.json, trade_*.json, fossil_*.json, camera.json, item_finder.json (pixelmon:aluminium_plate)
  - Fonte: https://download.nodecdn.net/containers/reforged/universal/release/Pixelmon-1.16.5-9.1.13-universal.jar :: data/pixelmon/recipes/furnace/bauxite_ore.json (0.7)

## D. Montagem final da Poké Ball

- **[high]** Receita SHAPELESS (posição livre, sem 'tampa em cima/base embaixo'): <bola> Lid + minecraft:stone_button + (Base de Ferro OU Base de Alumínio) -> 1 Poké Ball daquele tipo. Só botão de PEDRA (não aceita botão de madeira). Sem exceções entre as 22 bolas craftáveis do 1.12.2; em 1.7.10 eram duas receitas shapeless por bola (botão de pedra + base de ferro/alumínio + tampa).
  - Versão: 1.7.10 3.5.1; 1.12.2 6.0.0-8.4.3
  - Fonte: Pixelmon-1.12.2-7.0.7.jar :: assets/pixelmon/recipes/<bola>_ball.json (crafting_shapeless: <bola>_ball_lid, minecraft:stone_button, #POKEBALL_BASE) + _constants.json
  - Fonte: (arquivo de análise local) l.3290-3339 (addShapelessRecipe: Blocks.stone_button + ironBase/aluBase + getLid())

## D/F. Diferenças em 1.16+

- **[high]** 1.16.5 9.0.0 (mai/2022): sumiram os discos de apricorn — as TAMPAS passam a ser craftadas direto dos apricorns cozidos com os mesmos padrões/quantidades dos discos antigos; disco de ferro/alumínio continua sendo martelado em base (bigorna ou Bigorna Mecânica); bola = tampa + botão de pedra + base (tag pokeball_bases = ferro/alumínio). 1.16.5 9.1.13 (nov/2024): sem forja — bases craftadas direto (3 lingotes -> 5 bases: ferro, alumínio, prata, platina; 3 meias-lajes de madeira -> 5 wooden_base); tampa única com NBT PokeBallID; quantidades mudaram (Great 3, Heal 5, Nest 5, Premier 5, Safari 5, Sport 5, Moon 3, Dusk/Level/Luxury/Quick/Repeat/Timer/Ultra 1); bases por tier: Poké/Heal/Nest/Premier/Safari/Sport = qualquer base; Great/Dive/Fast/Friend/Heavy/Level/Love/Lure/Moon/Net = base de prata; Ultra/Dusk/Luxury/Park/Quick/Repeat/Timer = base de platina; bolas Hisui = base de madeira.
  - Versão: 1.16.5 9.0.0; 1.16.5 9.1.13
  - Fonte: https://download.nodecdn.net/containers/reforged/universal/release/Pixelmon-1.16.5-9.0.0-universal.jar :: data/pixelmon/recipes/pokeball/{lid,disc,ball}/*.json, data/pixelmon/tags/items/pokeball_bases.json
  - Fonte: https://download.nodecdn.net/containers/reforged/universal/release/Pixelmon-1.16.5-9.1.13-universal.jar :: data/pixelmon/recipes/pokeball/{lid,base,ball}/*.json
  - Fonte: CDN referenciado em https://github.com/EnvyWare/Pixelmon-MDK/blob/main/build.gradle (repositório ivy download.nodecdn.net/containers/reforged/universal/release)

## E. Bigorna Mecânica: existe? (versões)

- **[high]** Sim, a 'forja automática' é a Bigorna Mecânica (Mechanical Anvil; pt-BR 'Bigorna Mecânica'). Não existe em 1.6.2 (2.3.1). Existe em 1.7.10 3.5.1 (bloco + GUI, mas SEM receita de crafting registrada e com textura plana de lado de fornalha), em todo o 1.12.2 Reforged (6.0.0, 7.0.7, 8.3.6, 8.4.3: craftável e animada) e em 1.16.5 9.0.0 (só discos de ferro/alumínio/platina -> bases). Removida até 1.16.5 9.1.13 (só restam assets/modelos).
  - Versão: 1.7.10 3.5.1; 1.12.2 6.0.0-8.4.3; 1.16.5 9.0.0
  - Fonte: Pixelmon 2.3.1 (archive.org old-pixelmon-versions/1.6.2) :: lista de classes sem MechanicalAnvil
  - Fonte: Pixelmon-1.7.10-3.5.1 :: com/pixelmonmod/pixelmon/blocks/machines/BlockMechanicalAnvil.class + TileEntityMechanicalAnvil.class; nenhuma referência em PixelmonRecipes.class
  - Fonte: https://download.nodecdn.net/containers/reforged/universal/release/Pixelmon-1.12.2-6.0.0-universal.jar :: assets/pixelmon/recipes/mechanical_anvil..json
  - Fonte: Pixelmon-1.16.5-9.0.0 :: MechanicalAnvilBlock/MechanicalAnvilTileEntity.class; Pixelmon-1.16.5-9.1.13 :: sem essas classes

## E. Bigorna Mecânica: receita

- **[high]** Shaped 'III' / 'IPI' / 'IFI': I = barras de ferro (minecraft:iron_bars, 7), P = pistão grudento (sticky_piston), F = fornalha (furnace) -> 1 pixelmon:mechanical_anvil. Igual em 6.0.0 (arquivo 'mechanical_anvil..json'), 7.0.7, 8.3.6 e 1.16.5 9.0.0. Em 1.7.10 3.5.1 não havia receita.
  - Versão: 1.12.2 6.0.0-8.4.3; 1.16.5 9.0.0
  - Fonte: Pixelmon-1.12.2-7.0.7.jar :: assets/pixelmon/recipes/mechanical_anvil.json
  - Fonte: Pixelmon-1.16.5-9.0.0 :: data/pixelmon/recipes/machines/mechanical_anvil.json

## E. Bigorna Mecânica: bloco e visual

- **[high]** 1.12.2: Material IRON, dureza 2.5, MultiBlock 1x2x1 (dois blocos de altura; a TileEntity fica só no bloco de BAIXO), rotacionável; emite luz 15 enquanto roda; partículas de fumaça + chama na frente enquanto roda (como fornalha); modelo SMD animado (models/blocks/mech_anvil: prensa/martelo) com textura mechanvil_on/off.png e o disco visível em cima (balls/base_ball|great_ball|heavy_ball|net_ball|timer_ball + textures/blocks/mechanvilballs/<Bola>.png; ferro/alumínio = IronAluminium.png); ciclo de animação de 200 ticks. Em 1.7.10 era um bloco cúbico comum com a textura 'furnace_side' em todas as faces, sem animação.
  - Versão: 1.12.2 6.0.0-8.4.3; 1.7.10 3.5.1
  - Fonte: (arquivo de análise local) l.6-12 (IRON, MultiBlock(1,2.0,1), 2.5), l.48 (luz 15), l.123-231 (SMOKE_NORMAL/FLAME)
  - Fonte: Pixelmon-1.12.2-7.0.7.jar :: com/pixelmonmod/pixelmon/blocks/MultiBlock.class (createTileEntity só em EnumMultiPos.BASE)
  - Fonte: Pixelmon-1.12.2-7.0.7.jar :: RenderTileEntityMechanicalAnvil.class / ModelMechanicalAnvil.class (constant pool mech_anvil.pqc, mechvilballs)
  - Fonte: (arquivo de análise local) (func_149651_a registra 'furnace_side')
  - Fonte: https://github.com/MartyAlana/Open-Pixelmon-Generations/blob/master/patches/java/com/pixelmongenerations/common/block/machines/BlockMechanicalAnvil.java.patch (super(Material.IRON, 1, 2.0D, 1); setHardness(2.5F))

## E. Bigorna Mecânica: GUI e slots

- **[high]** Tem GUI: clique direito (mão principal, em qualquer um dos 2 blocos) abre um container idêntico ao da fornalha, desenhado com a textura vanilla textures/gui/container/furnace.png e título 'Mechanical Anvil'/'Bigorna Mecânica': slot 0 = entrada (56,17), slot 1 = combustível (56,53), slot 2 = saída SlotFurnaceOutput (116,35), inventário do jogador em (8,84)/(8,142); chama (56,36) mostra o combustível restante e a seta (79,34, 24 px) o progresso de 200 ticks.
  - Versão: 1.12.2 6.0.0-8.4.3 (GUI equivalente em 1.7.10 3.5.1)
  - Fonte: (arquivo de análise local) l.10-60
  - Fonte: (arquivo de análise local) (ResourceLocation 'textures/gui/container/furnace.png'; getBurnTimeRemainingScaled(13); getHammerProgressScaled(24))
  - Fonte: (arquivo de análise local) (func_180639_a: openGui EnumGuiContainer.MechanicalAnvil)

## E. Bigorna Mecânica: combustível e tempo

- **[high]** Funciona como uma fornalha: aceita QUALQUER combustível de fornalha com o mesmo tempo de queima vanilla (TileEntityFurnace.getItemBurnTime; carvão = 1600 ticks = 8 itens), só acende/consome combustível quando há algo que possa martelar e espaço na saída; balde de lava devolve o balde vazio; uma vez acesa queima até o fim. 200 ticks (10 s) por item, 1 entrada -> 1 saída (disco de apricorn -> tampa; Disco de Ferro -> Base de Ferro; Disco de Alumínio -> Base de Alumínio); saída empilha até 64. Não usa martelo nem durabilidade. Mesmos números em 1.7.10, 1.12.2 e 1.16.5 9.0.0.
  - Versão: 1.7.10 3.5.1; 1.12.2 6.0.0-8.4.3; 1.16.5 9.0.0
  - Fonte: (arquivo de análise local) l.697-1138 (update: func_145952_a l.761, sipush 200 l.831; canHammer; hammerItem; getHammerResult)
  - Fonte: (arquivo de análise local) (8.3.6, mesmos 200 ticks)
  - Fonte: (arquivo de análise local) (sipush 200)

## E. Bigorna Mecânica: o que aceita (alumínio)

- **[high]** 1.12.2: a entrada aceita SÓ discos — qualquer disco de apricorn, Disco de Ferro e Disco de Alumínio; NÃO aceita Barra de Alumínio (Placa de Alumínio só na bigorna manual). Ou seja, faz tampas e também bases de ferro/alumínio. 1.7.10 3.5.1: também aceitava barras 'ingotAluminum' (OreDict) e produzia Placa de Alumínio. 1.16.5 9.0.0: só BallDiscItem (discos de ferro/alumínio/platina -> bases).
  - Versão: 1.12.2 6.0.0-8.4.3; 1.7.10 3.5.1; 1.16.5 9.0.0
  - Fonte: (arquivo de análise local) l.281-321 (func_94041_b) e l.1099-1133 (getHammerResult)
  - Fonte: (arquivo de análise local) (func_94041_b com OreDictionary 'ingotAluminum'; getHammerResult aluminiumIngot -> aluminiumPlate)
  - Fonte: (arquivo de análise local) (instanceof BallDiscItem)

## E. Bigorna Mecânica: funil/hopper e redstone

- **[high]** É ISidedInventory expondo os 3 slots em todas as faces: inserção validada por slot (discos -> entrada, combustível -> slot de combustível), então funis/dropper alimentam por qualquer lado; extração permitida só do slot de saída (e de balde vazio no slot de combustível); a entrada não pode ser extraída. Como a TileEntity existe só no bloco de baixo do multibloco, a automação precisa mirar o bloco inferior (inferido). Não tem nenhum controle por redstone nem saída para comparador (nenhum código de redstone/comparador nas classes). Quebrar dropa o conteúdo do inventário.
  - Versão: 1.12.2 6.0.0-8.4.3 (sided inventory igual em 1.7.10 3.5.1)
  - Fonte: (arquivo de análise local) l.323-390 (getSlotsForFace {0,1,2}; canInsertItem -> isItemValidForSlot; canExtractItem: 0 false, 1 só Items.BUCKET (field_151133_ar), 2 true)
  - Fonte: (arquivo de análise local) (breakBlock: InventoryHelper.dropInventoryItems)
  - Fonte: grep sem ocorrências de isBlockPowered/getRedstonePower/hasComparatorInputOverride em BlockMechanicalAnvil, TileEntityMechanicalAnvil e MultiBlock (7.0.7, 8.3.6, 3.5.1)

## E. Bigorna Mecânica: som

- **[medium]** Enquanto roda, uma vez por ciclo de animação de 200 ticks o código toca ENTITY_IRONGOLEM_DEATH (vol 0.7, pitch 1.0) — um 'clank' metálico pesado. Em 8.3.6/8.4.3 a condição usa isRunning() (servidor), então deve ser audível; em 7.0.7 a chamada está atrás de um campo só sincronizado no cliente e com player null, então provavelmente não toca (não testado em jogo).
  - Versão: 1.12.2 7.0.7 vs 8.3.6-8.4.3
  - Fonte: (arquivo de análise local) l.859-880
  - Fonte: (arquivo de análise local) l.700-725
  - Fonte: mcp_stable-39-1.12 fields.csv: field_187599_cE=ENTITY_IRONGOLEM_DEATH

## F. Árvores de apricorn

- **[high]** 7 cores (preto, branco, rosa, verde, azul, amarelo, vermelho). 1.12.2: 6 estágios (0-5); cresce no random tick com 25% de chance (evento GrowthChance) se a luz no bloco de cima >= 9; vira 2 blocos de altura a partir do estágio 3; madura no estágio 5. Clique direito na árvore madura dá EXATAMENTE 1 apricorn direto no inventário e a árvore volta ao estágio 3 (não é destruída). Farinha de osso (IGrowable) avança 1 estágio; Wailmer Pail avança 1 estágio uma vez por dia de Minecraft por árvore (senão: 'It looks well watered today.' / 'Parece que esta bem regado hoje.'). Em 1.7.10 a chance era 1/3 por random tick, mesma colheita (1 apricorn, volta ao estágio 3).
  - Versão: 1.12.2 6.0.0-8.4.3; 1.7.10 3.5.1
  - Fonte: (arquivo de análise local) l.456-704 (func_180639_a stage 5 -> giveItemStack + setStage(3); func_180650_b 0.25f, luz >= 9) e l.722-870 (canGrow/grow)
  - Fonte: (arquivo de análise local) (numStages=6; wasWateredToday)
  - Fonte: Pixelmon-1.12.2-7.0.7.jar :: com/pixelmonmod/pixelmon/items/ItemWailmerPail.class
  - Fonte: (arquivo de análise local) (nextInt(3), luz >= 9)

## F. Plantar apricorn

- **[medium]** Clique direito com um apricorn na face de cima de grama ou terra planta uma árvore estágio 0 (consome 1; som de grama), se a config allowPlanting estiver ligada e o chunk não passar de maximumPlants (conta árvores de apricorn + de berry). A árvore se mantém sobre grama, terra ou terra arada.
  - Versão: 1.12.2 7.0.7
  - Fonte: (arquivo de análise local) (func_180614_a: PixelmonConfig.allowPlanting, Blocks.GRASS/DIRT, countTileEntitiesOfType, maximumPlants)
  - Fonte: (arquivo de análise local) l.870-930 (canBlockStay: GRASS/DIRT/FARMLAND)
  - Fonte: Pixelmon-1.12.2-7.0.7.jar :: lang en_US (pixelmon.config.allowPlanting / maximumPlants.comment)

## F. Nomes pt-BR (fidelidade de UI)

- **[high]** pt_BR.lang do 1.12.2: Bigorna; Bigorna Mecânica (também título da GUI); Disco de Pokébola / Disco de Grande Pokébola / Disco de Ultra Pokébola...; Tampa de Pokébola / Tampa de Grande Pokébola...; Disco de Ferro; Base de Ferro; Disco de Alumínio; Base de Alumínio; Barra de Alumínio; Placa de Alumínio; Apricorn Vermelho/Azul/... Cozido; Martelo de Madeira/Pedra/Ferro/Ouro/Diamante/Alumínio; 'Rubi Martelo', 'Safira Martelo', 'Cristal Martelo', 'Ametista Martelo', 'Pedra de Fogo Martelo' etc.; Árvore de Apricorn; Bauxita (Minério); Balde de Wailmer.
  - Versão: 1.12.2 7.0.7
  - Fonte: Pixelmon-1.12.2-7.0.7.jar :: assets/pixelmon/lang/pt_BR.lang

## F. Conquistas antigas ligadas à cadeia

- **[medium]** O lang ainda carrega as conquistas da era pré-1.12: 'Kurt's Apprentice!' = 'Cook an Apricorn' e 'Gotta catch 'em all!' = 'Craft a Poké Ball'. No 1.12.2 viraram advancements de receita (ex.: receita da bigorna desbloqueada ao ter barra de ferro).
  - Versão: pré-1.12 (strings mantidas no 1.12.2 7.0.7)
  - Fonte: Pixelmon-1.12.2-7.0.7.jar :: assets/pixelmon/lang/en_US.lang (achievement.ApricornChieve, achievement.PokeballChieve)
  - Fonte: Pixelmon-1.12.2-7.0.7.jar :: assets/pixelmon/advancements/recipes/machines/anvil.json

## F. Linha do tempo do sistema (1.6.2 -> 1.12.2)

- **[medium]** O sistema bigorna+martelo+disco já existia em Pixelmon 2.3.1 (MC 1.6.2, ago/2013) com os mesmos números centrais (disco: 5 de força x 3 estágios; barra de alumínio: 15 x 3; força 1-5 por tier vanilla; 3 de durabilidade por estágio), mas sem disco/base de alumínio, sem coletar martelando, sem auto-recarga, sem Bigorna Mecânica e com 19 bolas craftáveis. Em 3.5.1 (1.7.10, dez/2015) já havia alumínio, coleta martelando, auto-recarga e a Bigorna Mecânica (sem receita). 6.0.0 (1.12.2, dez/2017) acrescenta martelos de gema/pedra de evolução, receita e modelo animado da Bigorna Mecânica. 1.8.9 (4.x) e 1.10.2 (5.x) não foram inspecionados, mas ficam entre versões idênticas no núcleo, e o fork Pixelmon Generations (1.12.2, derivado do código 1.10.2) tem as mesmas classes/assinaturas.
  - Versão: 1.6.2 2.3.1; 1.7.10 3.5.1; 1.12.2 6.0.0; (1.8.9/1.10.2 inferido)
  - Fonte: https://archive.org/details/old-pixelmon-versions (1.6.2/Pixelmon 2.3.1 install.zip; 1.7.10/Pixelmon 3.5.1.zip)
  - Fonte: (arquivo de análise local) e items_ItemHammer.txt
  - Fonte: https://github.com/MartyAlana/Open-Pixelmon-Generations/tree/master/patches/java/com/pixelmongenerations (ItemHammer.java.patch: super(2.0F, 1.0F, material, effectiveBlocks); AnvilEvent.java.patch: neededHits, force, damage, isCollecting)
  - Fonte: EmeraldsMirror listing (datas): https://github.com/EmeraldsMirror/emeraldsmirror/blob/master/downloads/pixelmon.html

## G. Ballworks vs Pixelmon: interação na bigorna

- **[high]** A Ballworks atual diverge do original: usa clique DIREITO com 3 marteladas fixas para qualquer martelo e diz que tiers só mudam durabilidade (README l.82, l.128, l.229-237; AnvilRecipes.kt l.20-21 DEFAULT_LID_HITS=3). No Pixelmon: clique ESQUERDO (mineração) com força por tier (15 marteladas de madeira até 3 de diamante por tampa), clique direito só coloca/retira, e retirar também é possível martelando o pronto.
  - Versão: 1.12.2 6.0.0-8.4.3 (referência)
  - Fonte: (arquivo de análise local) linhas 82, 128, 229-237
  - Fonte: (arquivo de análise local) linhas 15-21
  - Fonte: jp707 ItemHammer/TileEntityAnvil (ver fatos A)

## G. Ballworks vs Pixelmon: receitas e números

- **[high]** Divergências da Ballworks: bigorna com 7 ferros 'iii / i / iii' (README l.217-222) vs 8 ferros '###/## /###' no Pixelmon; durabilidade dos martelos madeira 60/pedra 132/ouro 500/diamante 1024/outros 250 (README l.95-99) vs Pixelmon 59/131/32/1561, alumínio 200, gemas 300, Pedra Folha 250, demais pedras 1561; 'lid blank' sempre x5 com receita de 4 apricorns crus em cruz (README l.38-42) vs disco em linha de 3 apricorns COZIDOS com rendimento por bola (5/2/1/3...); montagem com ingrediente central cobre/ferro/ouro/diamante/nether star (README l.48-62) vs Pixelmon = tampa + botão de pedra + base de ferro ou alumínio. Coincide: 3 lingotes de ferro em linha -> 5 bases (Pixelmon: 5 Discos de Ferro).
  - Versão: 1.12.2 6.0.0-8.4.3 (referência)
  - Fonte: (arquivo de análise local) linhas 36-62, 92-104, 214-223
  - Fonte: Pixelmon-1.12.2-7.0.7.jar :: assets/pixelmon/recipes/anvil.json, *_hammer.json, *_ball_disc.json, *_ball.json, iron_disc.json

## G. Modelos exportados na Ballworks (anvil_states)

- **[medium]** Os modelos importados em ballworks-orig/src/main/resources/assets/cobbleballworks/models/block/anvil_states correspondem às classes Pixelmon: <bola>_disc_flat/_stage1/_stage2 = ModelDiscFlat/ModelDiscStage1/ModelDiscStage2 (elementos RedTip/RedTop/RedFront/RedBottom/RedRight/RedLeft/RedBack, altura sobe 11.7->12.2->12.4); models/item/anvil_states/<bola>balllid = ModelDiscHemiSphere (tampa); aluminium_ingot = ModelIngot; aluminium_plate_stage2/stage3 = ModelPlateStage2/3 (estados 1 e 2 do lingote); aluminium_plate_stage1 (um único cubo 'Plate') é na verdade a Placa pronta (ModelPlate). Pelo renderer original, disco/base de alumínio usam a MESMA textura irondisc.png (o aluminum_disc.png da Ballworks não existia no Pixelmon 1.12).
  - Versão: 1.12.2 (mapeamento)
  - Fonte: (arquivo de análise local) (credit 'Extracted from Pixelmon legacy anvil ... work-state model')
  - Fonte: (arquivo de análise local) (state->modelo; irondisc.png para iron e alu)

## F. Onde estão os assets originais (referência)

- **[high]** Os jars universais oficiais no CDN do Reforged contêm todos os assets originais da cadeia, legíveis por range-request sem baixar o jar inteiro (330-500 MB): textures/items/pokeballs/<bola>disc.png e <bola>lid.png, irondisc.png, ironbase.png, textures/blocks/anvil.png, textures/blocks/aluminium/ingot.png, textures/pokeballs/anvil/*, textures/blocks/mechanvil_on/off.png, textures/blocks/mechanvilballs/*.png e models/blocks/mech_anvil/*.smd (animação). São assets de terceiros (direitos do Pixelmon) — usar só como referência.
  - Versão: 1.12.2 6.0.0 / 8.4.3
  - Fonte: https://download.nodecdn.net/containers/reforged/universal/release/Pixelmon-1.12.2-6.0.0-universal.jar (listagem do diretório central)
  - Fonte: https://download.nodecdn.net/containers/reforged/universal/release/Pixelmon-1.12.2-8.4.3-universal.jar
  - Fonte: Script de leitura remota: (arquivo de análise local)

## Perguntas em aberto

- Versão exata (entre 3.5.1/1.7.10 de dez-2015 e 6.0.0/1.12.2 de dez-2017) em que a Bigorna Mecânica ganhou receita de crafting e o modelo animado de 2 blocos: não achei jars de Pixelmon 1.8.9 (4.x) nem 1.10.2 (5.x) no GitHub, no CDN do Reforged (404) nem no Internet Archive. A página do EmeraldsMirror lista Pixelmon-1.8.9-4.2.3 e 1.10.2-5.1.2, mas os downloads estão offline.
- Como a Bigorna Mecânica era obtida no 3.5.1 (1.7.10), já que não há receita registrada no código: só criativo/comando? Loot? Não verificado.
- Comportamento real em 1.8.9/1.10.2 (dano de 3 por estágio, sons no estilo do 6.0.0) é só inferido entre 1.7.10 e 1.12.2 idênticos; não verificado diretamente.
- Em que versão o desgaste por estágio caiu de 3 para 1 (está entre 7.2.0 e 8.3.6).
- Em que versão entraram os martelos de gema e de pedra de evolução (ausentes no 3.5.1, presentes no 6.0.0).
- Tempo real de cada martelada não foi medido em jogo; valores derivados da fórmula vanilla (força/30 por tick + 5 ticks de pausa).
- Se o som ENTITY_IRONGOLEM_DEATH da Bigorna Mecânica toca de fato em 7.x (análise de código sugere que não; em 8.x sim). Não testado em jogo.
- Versão exata do 1.16.5 (entre 9.0.0 de mai-2022 e 9.1.13 de nov-2024) em que a forja na bigorna e a Bigorna Mecânica foram removidas.
- Se os modelos exportados na Ballworks (conversão de TESR 1.12) batem 1:1 com a geometria das classes ModelDisc*/ModelPlate* do Pixelmon; o próprio README da Ballworks cita problemas de coordenadas.
- Cobblemon tem bolas sem equivalente craftável no Pixelmon antigo (Azure, Citrine, Roseate, Slate, Verdant, Beast, Dream, Park, Master, Ancient etc.): não há receita original de disco para elas. Precisa de decisão de design.
- Valor de allowAnvilAutoreloading nos arquivos de config que vinham com o jogo: o padrão no código é false, mas servidores podiam ligar. A experiência do usuário pode ter tido auto-recarga.
