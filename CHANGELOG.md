# Changelog

O que muda em cada versão do port para Fabric 1.21.1. O jar de cada versão fica nas
[Releases](https://github.com/fernandoabf/cobblemon-ballworks-fabric/releases); o guia do jogador é o
[`docs/COMO-JOGAR.md`](docs/COMO-JOGAR.md).

## 1.2.0 — 2026-10-16

**O custo das bolas agora acompanha a captura.** O Cobblemon escolhe o material da receita pelo
"nível" da bola, não pelo quanto ela ajuda a capturar: a Bola Nível (até ×4), a Tela (×3) e a
Mergulho (×3,5) custavam ferro, menos que a Ultra (×2, ouro), e a Rápida (×5 no primeiro turno)
custava o mesmo que a Ultra. Agora o metal do centro do disco, quantos discos saem e a força na
bigorna seguem o multiplicador de captura de cada bola no Cobblemon 1.8.2 (as situacionais pelo
melhor caso). O formato e as bolotas da receita continuam os do Cobblemon.

| Captura | Centro do disco | Discos | Força |
| --- | --- | --- | --- |
| ×1 | cobre | 4 | 5 |
| ×1,5 | ferro | 4 | 5 |
| ×2 a ×2,5 | ouro | 4 | 5 |
| ×3 a ×4 na condição dela | diamante | 4 | 10 |
| ×5 a ×8 na condição dela | diamante | 2 | 10 |
| garantida | netherite + estrela do Nether | 1 | 15 |

- Mudaram 19 bolas: Amizade e Luxo ficaram mais baratas; Safári e Parque, mais caras; as situacionais
  (Tela, Mergulho, Crepúsculo, Repetição, Ninho, Tempo, Nível, Lua, Pesada, Isca, Veloz e Onírica)
  passaram para diamante com força 10; Rápida, Amor e Criatura dão 2 discos.
- A Bola Estima (×1) virou básica: bolotas vermelhas cozidas em cruz, uma preta embaixo e cobre no
  centro.
- A tabela de receitas do guia ganhou a coluna da captura.

## 1.1.0 — 2026-10-16

- **Martelo de Netherite:** sai da mesa de ferraria (Molde de Aprimoramento de Netherite + Martelo de
  Diamante + Lingote de Netherite), como as ferramentas de netherite do vanilla. Força 8,
  durabilidade 2031, não queima na lava e mantém os encantamentos e o desgaste do martelo de
  diamante.
- **Bola Estima e Bola Origem Ancestral** também passam pela forja. O Cobblemon não tem receita para
  elas, então as receitas de disco são do mod.
- **Receitas de Pokébola de outros mods são desligadas**, como as do Cobblemon Parts Refabricated:
  toda bola sai da forja. Dá para desligar isso com `disableOtherModsBallRecipes` na config.
- **Martelos encantáveis:** Eficiência (e o efeito Pressa) deixa as marteladas mais rápidas, até uma
  a cada 5 ticks, a mesma pausa que o jogo dá entre dois blocos; Inquebrável e Remendo funcionam como
  nas ferramentas. O martelo de madeira também ganha com a Eficiência.
- **A prensa da Bigorna Mecânica se mexe:** enquanto a máquina trabalha, ela desce uma vez por
  segundo e martela a peça.

## 1.0.1 — 2026-10-16

- As peças na bigorna são desenhadas com a Pokébola 3D do próprio Cobblemon: o disco é a metade de
  cima achatada, que vai subindo a cada estágio até virar a tampa, com as cores da bola. O disco e a
  base de ferro são a metade de baixo em ferro.

## 1.0.0 — 2026-10-16

Primeira versão do port para Fabric 1.21.1 e Cobblemon 1.8.2, refeito para ficar igual à forja de
Pokébolas do Pixelmon antigo.

- Bolotas cozidas na fornalha; a receita de cada bola no Cobblemon passa a fazer **discos**, com as
  bolotas cozidas.
- **Bigorna de Pokébola:** coloque o disco e segure o clique esquerdo com um martelo até ele virar
  tampa. Tampa + botão de pedra + base de ferro = 1 bola.
- 17 martelos com a força e a durabilidade do Pixelmon.
- **Bigorna Mecânica:** uma fornalha que martela sozinha, com funis.
- 46 bolas do Cobblemon na forja, com sprites de disco e tampa derivados dos do Cobblemon.
- Plugin do EMI (opcional), config do servidor e traduções en_us/pt_br.
