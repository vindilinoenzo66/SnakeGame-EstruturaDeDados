# SnakeGame & Estruturas de Dados

Jogo da cobrinha desenvolvido em Java para estudar estruturas de dados de forma interativa. O projeto usa a [JSimple Game Engine (JSGE), de David Buzatto](https://github.com/davidbuzatto/JSGE), para a janela, o desenho e a leitura dos comandos. As regras da partida estão nas classes do próprio projeto.

O jogo tem **oito modos**, um **Liquidificador** para combinar regras, opções de velocidade, tamanho do mapa, quantidade de frutas e cor da cobra. A interface se ajusta ao espaço disponível preservando as proporções, e a partida só começa após o comando do jogador.

## Como executar

### Pelo NetBeans

1. Instale o **JDK 17 ou superior** e abra esta pasta como projeto no NetBeans.
2. Em **Propriedades do Projeto → Executar**, escolha `projetoesdlinear.SnakeGame` como classe principal.
3. Execute o projeto. No menu, escolha o modo e pressione **Enter** ou clique em **Iniciar partida**. Na tela seguinte, pressione **Espaço**, **Enter** ou clique em **Começar**.

### Pelo terminal no Linux

Os arquivos `.jar` necessários já estão em `lib/`. A partir da raiz deste projeto:

```bash
mkdir -p build/manual-classes
javac --release 17 -encoding UTF-8 \
  -cp 'lib/JSGE-Latest/*:lib/AlgoritmosEstruturasDeDados/*' \
  -d build/manual-classes src/projetoesdlinear/*.java

java --enable-native-access=ALL-UNNAMED \
  -cp 'build/manual-classes:lib/JSGE-Latest/*:lib/AlgoritmosEstruturasDeDados/*' \
  projetoesdlinear.SnakeGame
```

No Windows, o separador entre os caminhos do *classpath* é `;` em vez de `:`. Para usar controle USB, o sistema também precisa disponibilizar as bibliotecas nativas usadas pela JSGE/SDL; o teclado pode ser usado independentemente do controle.

## Modos de jogo

| Modo | Regra |
|---|---|
| **Clássico** | Coma frutas para crescer e pontuar. Bater na borda ou no corpo encerra a partida. |
| **Ordem das frutas** | Coma a cor indicada no início da fila. Comer outra cor encerra a partida. Depois do acerto, a próxima cor passa para o início da fila. |
| **Coleta e entrega** | As frutas coletadas entram na pilha. Os pontos são recebidos ao levar a carga até a casa de descarte, marcada com **D**. |
| **Pacífico** | A cobra atravessa a borda e reaparece do lado oposto; também pode atravessar o próprio corpo e as paredes criadas pelo modo Parede. |
| **Fruta alada** | As frutas normais tentam mudar de casa a cada três movimentos da cobra. |
| **Parede** | Uma nova parede aparece a cada três pontos alcançados. Bater nela encerra a partida, exceto quando Pacífico também está ativo. |
| **Veneno** | A fruta roxa marcada com **!** retira um ponto, sem deixar a pontuação negativa. Ela reaparece em outra casa livre. |
| **Chave** | Pegue a chave marcada com **C** para liberar uma rodada de frutas. Após a última fruta da rodada, surge outra chave. |

O **Liquidificador** permite ativar vários modos na mesma partida. As regras escolhidas são aplicadas juntas; por exemplo, em **Ordem das frutas + Coleta e entrega**, é preciso respeitar a fila de cores e depois levar as frutas ao descarte. O modo Clássico fornece a base do jogo e não acrescenta uma regra especial quando combinado com outros.

## Controles

| Situação | Teclado e mouse | Controle USB |
|---|---|---|
| Menu | **1–8** escolhem modos; **L** liga/desliga o Liquidificador; **V**, **T**, **F** e **C** mudam velocidade, tamanho, frutas e cor; setas mudam o foco; **Espaço** ativa o item; **Enter** abre a partida. Também é possível clicar nos itens. | Direcional ou analógico esquerdo navega; **A** seleciona; **X** troca a cor; **Start** abre a partida. |
| Tela de início | **Espaço**, **Enter** ou clique em **Começar** inicia o movimento e o cronômetro. | **A** ou **Start** inicia. |
| Partida | **WASD** ou setas movem; **P** pausa; **C** troca a cor; **Esc** volta ao menu. | Direcional ou analógico esquerdo move; **Start** pausa; **X** troca a cor; **B** volta ao menu. |
| Fim de jogo | **R** ou **Enter** reinicia; **M** ou **Esc** volta ao menu. | **A** ou **Start** reinicia; **B** volta ao menu. |

O botão **Sair**, no canto superior direito, fecha o jogo em qualquer tela. O código procura controles disponíveis nas entradas 1 a 4 da JSGE. Há um mapeamento específico para o USB gamepad `081f:e401` no Linux; outros controles dependem de serem reconhecidos pela biblioteca e pelo sistema.

## Opções e estruturas de dados

O mapa tem três tamanhos: **10 × 9**, **17 × 15** ou **24 × 21** casas. Há três velocidades e opções de **1**, **3**, **5** frutas simultâneas ou **Lotação**. O placar mostra pontos, tempo, frutas comidas e o tamanho do corpo. O cronômetro para durante a pausa e ao terminar a partida.

| Estrutura | Uso no jogo |
|---|---|
| `Deque<Posicao>` | Corpo da cobra: a cabeça entra no início e a cauda sai do fim. |
| `Queue<Integer>` | Fila das cores exigidas em Ordem das frutas (**FIFO**). |
| `Deque<Integer>` | Pilha das frutas aguardando entrega (**LIFO**). |
| `List<Fruta>` e `List<Posicao>` | Frutas e paredes presentes no mapa. |
| `EnumSet<Modo>` | Conjunto de modos ativos no Liquidificador. |

## Organização do código

| Arquivo | Responsabilidade |
|---|---|
| [`SnakeGame.java`](src/projetoesdlinear/SnakeGame.java) | Janela JSGE, telas, entrada, cronômetro e desenho. |
| [`JogoSnake.java`](src/projetoesdlinear/JogoSnake.java) | Estado da partida, movimento, colisões, frutas, pontuação e regras dos modos. |
| [`Configuracao.java`](src/projetoesdlinear/Configuracao.java) | Opções escolhidas no menu e consulta aos modos ativos. |
| [`Modo.java`](src/projetoesdlinear/Modo.java) | Enumeração dos oito modos e textos exibidos no menu. |
| [`Posicao.java`](src/projetoesdlinear/Posicao.java) | Coordenadas de uma casa do tabuleiro. |

## Créditos

Projeto desenvolvido por **Enzo e Luís** a partir do projeto base de estruturas de dados disponibilizado pelo **Prof. Dr. David Buzatto**. A biblioteca [JSGE](https://github.com/davidbuzatto/JSGE) também é de autoria do professor.
