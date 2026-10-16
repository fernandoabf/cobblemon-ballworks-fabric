# CLAUDE.md — Cobblemon Ballworks (port Fabric 1.21.1)

Responda e escreva os textos para o usuário em **português do Brasil**. Código, ids e comentários de
código em inglês (como no resto do projeto).

## O que é

Port do mod Cobblemon Ballworks (Forge 1.20.1, de xaiver/cxaiverb, MIT) para **Fabric 1.21.1 +
Cobblemon 1.8.2**, refeito para ficar **igual à forja de Poké Balls do Pixelmon antigo**: apricorn
cozido → disco → bigorna com martelo (segurar clique esquerdo) → tampa; tampa + botão de pedra +
base de ferro → Poké Ball; e a Bigorna Mecânica, uma "fornalha" que martela sozinha.

- **Especificação e checklist:** [`docs/PLANO.md`](docs/PLANO.md). Siga a ordem do checklist e marque
  o que terminar.
- **Fontes da especificação:** [`docs/pesquisa/`](docs/pesquisa). `pixelmon-codigo-fonte.md` é a mais
  confiável (análise dos jars reais do Pixelmon); `auditoria-recursos-ballworks.md` lista o que está
  quebrado nos recursos herdados e como converter cada coisa.
- **Código original (Forge):** [`legacy/forge-1.20.1/`](legacy/forge-1.20.1). Só referência: não
  compila e não deve ser executado. O port reescreve tudo em `src/`.

## Estado atual

**Versão 1.2.0**, com o checklist do plano completo, menos o teste com o modpack inteiro (que
depende do modpack do usuário) e as bases próprias de cada bola (próxima atualização, seção 9 do
plano). As 48 bolas do Cobblemon passam pela forja: o disco usa o formato e as bolotas cozidas da
receita do Cobblemon, com o metal do centro, os discos e a força pela captura da bola (`CatchClass`;
Estima e Origem Ancestral, sem receita no Cobblemon, têm receitas do mod), vira tampa na bigorna e a
bola é tampa + botão de pedra + base de ferro. Receitas de bola de outros mods (Cobblemon Parts
Refabricated) são desligadas. Estão prontos a Bigorna de Pokébola, os 18 martelos (o de netherite
sai da mesa de ferraria; encantamentos funcionam), a Bigorna Mecânica com a prensa animada, o plugin
do EMI, a config do servidor e as traduções en_us/pt_br. Os 38 gametests passam no servidor headless
com o Cobblemon real e o showcase confere o client (render, GUI, EMI), os dois em dev e em produção.
Guia do jogador: [`docs/COMO-JOGAR.md`](docs/COMO-JOGAR.md).

Mapa do código (`com.xaiver.cobbleballworks`):

- `content/`: regras e dados (`ForgeBall`: as 48 bolas e a `CatchClass` de cada; `HammerMaterial`;
  `AnvilRules`; `ApricornColor`; `DisabledRecipes`).
- `registry/`: registros (itens, blocos, block entities, menu, tipo de receita, tags, aba criativa).
- `block/` e `block/entity/`: as duas bigornas; `menu/`: o menu da Bigorna Mecânica; `item/`:
  martelos e `AnvilStrikes` (o servidor confere o ritmo das marteladas); `recipe/AnvilRecipe`: o tipo
  `cobbleballworks:anvil`; `config/BallworksConfig`.
- `src/main/java/.../mixin/RecipeManagerMixin`: tira as receitas nativas das bolas do Cobblemon (os
  overrides de datapack perdem para o Cobblemon, ver 3.3 do plano).
- `src/client`: renderers (`PieceRenderer` desenha a peça na bigorna com a metade da Pokébola 3D do
  Cobblemon, `models/piece`, achatada nos estágios), a tela da Bigorna Mecânica e o plugin do EMI
  (`compat/emi`).
- `tools/`: `derive_ball_parts.py` gera os sprites de disco e tampa, `derive_piece_models.py` os
  modelos 3D das peças (`models/piece`) e a textura de ferro, os dois a partir do jar do Cobblemon;
  `make_icon.py` monta o ícone do mod e `make_netherite_hammer.py` o sprite do martelo de netherite.

## Ambiente e comandos

Precisa de **JDK 21**. O `build.gradle` para com uma mensagem clara se o Gradle rodar em outro Java.
Se não houver JDK 21 na máquina, instale um (por exemplo `apt-get install -y openjdk-21-jdk-headless`,
ou o Temurin 21) e aponte o `JAVA_HOME`.

```bash
./gradlew build          # jar: build/libs/cobbleballworks-<versão>.jar
./gradlew runGametest    # servidor de gametest headless com o Cobblemon real; sai sozinho; != 0 se falhar
./gradlew genSources     # fontes descompiladas do MC 1.21.1 (mojmap) para conferir assinaturas
```

- Na primeira configuração o Gradle baixa o jar oficial do Cobblemon 1.8.2 do Modrinth para
  `run-libs/` (git-ignored) e confere o SHA-1. Ele só é usado em runtime de desenvolvimento.
- Resultado do gametest: `build/gametest/junit.xml` e `build/gametest/logs/latest.log`.
- Conferência visual no client (sem monitor): `xvfb-run -a -s "-screen 0 1280x720x24" ./gradlew
  runShowcase` sobe o client com o mod de dev `src/showcase`, monta cenas num mundo plano, salva
  screenshots em `build/showcase/screenshots/` e fecha sozinho (precisa de Xvfb e Mesa; ~2 min).
  O EMI roda junto (só em dev); `-PshowcaseLang=pt_br` tira as fotos com o jogo em português.
- **Produção** (antes de uma versão): `./gradlew runProductionGametest` roda os mesmos gametests num
  servidor Fabric de produção (instalador/launcher oficiais, nomes intermediary, nosso jar remapeado
  e os jars publicados do Fabric API e do Cobblemon; resultado em `build/prodgametest/`), e
  `xvfb-run -a -s "-screen 0 1280x720x24" ./gradlew runProductionShowcase` faz o showcase num client
  de produção com o EMI publicado (`build/prodshowcase/screenshots/`). Pegam o que o dev esconde:
  reflexão por nome mojmap, mixin sem remapear, dependência que só existe em dev.
- Alternativa sem JDK local: `./dgradle.sh <tarefas>` (mesmo Gradle dentro do Docker).

## Regras do projeto

- **Antes de commitar:** `./gradlew build runGametest` tem que passar. Toda mecânica nova ganha
  gametest (seção 8 do plano).
- **APIs do 1.21.1:** confira as assinaturas nas fontes descompiladas (`genSources`) e nas fontes do
  Fabric API em vez de confiar na memória. Muita coisa mudou do 1.20.1 para o 1.21.1 (componentes de
  item, `HolderLookup.Provider` no NBT, `useItemOn`/`useWithoutItem`, `MapCodec` nos blocos, formato
  de receitas).
- `src/main` não pode usar `net.minecraft.client.*` (o Loom bloqueia); renderer, telas e modelos vão
  em `src/client`. O mod roda nos dois lados: servidor dedicado e client.
- Mantenha os créditos e a licença: autor original no `fabric.mod.json`, `LICENSE` MIT, e o aviso de
  que modelos/texturas importados do Pixelmon não são MIT (README).
- Não commite `run-libs/`, `build/` ou jars de terceiros. Não coloque no repositório dados de
  servidor (endereços, senhas, chaves): o repositório é público.
- Commits pequenos, mensagem em inglês, um passo do checklist por vez.

## Armadilhas já conhecidas

- **Loom:** a 1.13.6 é a última que aceita Gradle 8.14; a 1.14+ exige Gradle 9.2.
- **Jar-in-jar do Cobblemon:** o Loom apaga os jars aninhados das dependências remapeadas. A task
  `extractCobblemonJij` extrai GraalJS/ICU4J/Mongo do jar real para o classpath de dev. Não remova.
- **Classe de gametest:** tem que ser `class` com construtor público, não `object`. Use
  `@GameTest(template = FabricGameTest.EMPTY_STRUCTURE)`.
- **Ruído inofensivo no log do gametest:** "Failed to load properties from file: server.properties",
  "No data fixer registered for cobblemon:...", mixins do Cobblemon sem alvo, "duplicate input class
  ... package-info" no primeiro build.
- **Fontes do Fabric API:** vêm em nomes intermediary no Maven; o Loom as remapeia para mojmap em
  `.gradle/loom-cache/remapped_mods/`.
- **`splitEnvironmentSourceSets()`:** o `genSources` gera dois jars (`minecraft-common` e
  `minecraft-clientOnly`) em `.gradle/loom-cache/minecraftMaven/`.

## Entrega

O resultado é o jar de `build/libs/`. Ele vai nos dois lados: na pasta `mods/` do servidor e na do
client. O **Cobblemon Parts Refabricated** pode sair do modpack: as receitas de bola dele (as 48) são
desligadas pelo mixin de receitas (config `disableOtherModsBallRecipes`), então as peças dele ficam sem
uso.
