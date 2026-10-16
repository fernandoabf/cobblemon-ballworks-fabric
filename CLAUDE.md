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

Só o toolchain está pronto: o build gera o jar, o gametest headless sobe com o **Cobblemon real** e
passa um teste de fumaça. Os recursos herdados (`src/main/resources/assets` e `data`) ainda estão no
formato 1.20 e precisam ser convertidos (o Minecraft 1.21 ignora as pastas antigas em silêncio).
Nenhum item, bloco ou receita foi portado ainda.

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
client. Quando instalar, o mod **Cobblemon Parts Refabricated** tem que sair do modpack, porque ele
sobrescreve as mesmas receitas de Poké Ball do Cobblemon.
