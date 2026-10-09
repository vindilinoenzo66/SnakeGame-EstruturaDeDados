package projetoesdlinear;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.util.Deque;
import org.libsdl.SDL; // import para conseguir usar o controle

/**
 *
 * @author luism
 */

// Classe SnakeGame que herda os metodos da EngineFrame
public class SnakeGame extends EngineFrame {

    // Indica qual janela esta no momento
    private enum State { MENU, JOGANDO, GAMEOVER }

    private static final int LARGURA_BASE = 1120; // da tela
    private static final int ALTURA_BASE = 760; // da tela
    private static final Color FUNDO = new Color( 14, 21, 31 ); // Azul escuro / grafite
    private static final Color PAINEL = new Color( 24, 35, 49 ); // Azul escuro um pouco mais claro que o fundo
    private static final Color TEXTO = new Color( 234, 241, 247 ); // Branco Gelo
    private static final Color CINZA = new Color( 152, 171, 184 ); // Cinza
    private static final Color VERDE = new Color( 81, 211, 137 ); // Verde
    // Array das cores das frutas
    private static final Color[] CORES_FRUTA = {
        new Color( 243, 91, 101 ), // Vermelho
        new Color( 255, 197, 77 ), // Aamarelo
        new Color( 99, 190, 255 ), // Azul claro
        new Color( 195, 126, 250 ) // Lilas
    };
    // Array das cores da cobra
    private static final Color[] CORES_COBRA = {
        VERDE, new Color( 76, 177, 245 ), // Azul neon
        new Color( 245, 123, 177 ), // ROsa
        new Color( 255, 167, 78 ),  // Laranja
        new Color( 184, 135, 246 ), // ROxo
        new Color( 230, 239, 245 ) // Branco acinzentado
    };
    
    // Variaveis que definem o tamanho do tabuleiro
    private static final int AREA_X = 30; // iniciam em x=30
    private static final int AREA_Y = 125; // iniciam em y = 125
    private static final int AREA_LARGURA = 640;
    private static final int AREA_ALTURA = 570;
    
    // Mapeamento do USB gamepad (081f:e401), visto pelo Linux como joystick genérico.
    private static final String MAPEAMENTO_USB_GAMEPAD =
            "030000001f08000001e4000010010000,USB gamepad,"
            + "a:b0,b:b1,x:b2,y:b3,back:b8,start:b9,"
            + "leftshoulder:b4,rightshoulder:b5,leftx:a0,lefty:a1,"
            + "dpup:-a1,dpdown:+a1,dpleft:-a0,dpright:+a0,platform:Linux,";

    // Variaveis de controle
    private final Configuracao config = new Configuracao(); // guarda as opsoes de jogo
    private State estado = State.MENU; // guarda o estado da tela e define como estado inicial MENU
    private JogoSnake jogo;  // instancia da classe jogosnake
    private double contador;
    private boolean pausado;
    private int celula;
    private int tabX;
    private int tabY;
    private boolean aguardandoInicio;
    private boolean entradaLiberada;
    private double tempoDeJogo;
    private int controleAtivo = -1;
    private int focoMenu;
    private double esperaControleMenu;

    public SnakeGame() {
        super( LARGURA_BASE, ALTURA_BASE, "SnakeGame & Estruturas de Dados",
                60, true, false, true, false, false, false );
    }

    
    // Metodo create 
    @Override
    public void create() {
        setExitKey( KEY_NULL ); // Esc volta ao menu,
        // O create roda antes do primeiro ciclo da JSGE, quando ela procura controles SDL
        try {
            SDL.SDL_GameControllerAddMapping( MAPEAMENTO_USB_GAMEPAD );
        } catch ( Throwable exc ) {
            System.err.println( "Não foi possível carregar o mapeamento do USB gamepad: "
                    + exc.getMessage() );
        }
    }

    @Override
    public void update( double delta ) {
        // verifica se esta na regiao retangular do botao sair
        if ( clicou( 960, 24, 125, 40 ) ){
            System.exit( 0 ); 
            return;
        }
        // chama metodo que verifica os comandos do controle/teclado
        atualizarControle();
        // direciona cada caso para seu atualizar
        switch ( estado ) {
            case MENU -> atualizarMenu( delta );
            case JOGANDO -> atualizarJogo( delta );
            case GAMEOVER -> atualizarFim();
        }
    }

    // Metodo atualizar controle 
    private void atualizarControle() {
        if ( controleAtivo >= 0 && isGamepadAvailable( controleAtivo ) ){
            return;
        }
        controleAtivo = -1;
        for ( int id = GAMEPAD_1; id <= GAMEPAD_4; id++ ) {
            if ( isGamepadAvailable( id ) ){
                controleAtivo = id;
                esperaControleMenu = 0;
                break;
            }
        }
    }


    private boolean controlePressionado( int botao ) {
        return controleAtivo >= 0 && isGamepadButtonPressed( controleAtivo, botao );
    }

    private boolean controleSegurado( int botao ) {
        return controleAtivo >= 0 && isGamepadButtonDown( controleAtivo, botao );
    }

    // metodo atualizar menu para teclado
    private void atualizarMenu( double delta ) {
        if ( isKeyPressed( KEY_UP ) ){
            moverFocoMenu( 0, -1 );
        }
        if ( isKeyPressed( KEY_DOWN ) ){
            moverFocoMenu( 0, 1 );
        }
        if ( isKeyPressed( KEY_LEFT ) ){
            moverFocoMenu( -1, 0 );
        }
        if ( isKeyPressed( KEY_RIGHT ) ){
            moverFocoMenu( 1, 0 );
        }
        if ( isKeyPressed( KEY_V ) ){
            config.velocidade = ( config.velocidade + 1 ) % 3;
        }
        if ( isKeyPressed( KEY_T ) ){
            config.tamanho = ( config.tamanho + 1 ) % 3;
        }
        if ( isKeyPressed( KEY_F ) ){
            config.quantidadeFrutas = ( config.quantidadeFrutas + 1 ) % 4;
        }
        if ( isKeyPressed( KEY_C ) ){
            trocarCor();
        }
        if ( isKeyPressed( KEY_L ) ){
            trocarLiquidificador();
        }
        int[] numeros = { KEY_ONE, KEY_TWO, KEY_THREE, KEY_FOUR,
                          KEY_FIVE, KEY_SIX, KEY_SEVEN, KEY_EIGHT };
        for ( int i = 0; i < numeros.length; i++ ) {
            if ( isKeyPressed( numeros[i] ) ){
                selecionarModo( Modo.values()[i] );
            }
        }
        if ( isKeyPressed( KEY_SPACE ) ){
            ativarFocoMenu();
        }
        if ( isKeyPressed( KEY_ENTER ) ){
            iniciar();
        }

        if ( isMouseButtonPressed( MOUSE_BUTTON_LEFT ) ){
            int mx = mouseVirtualX();
            int my = mouseVirtualY();
            for ( int i = 0; i < 14; i++ ) {
                int[] area = areaItemMenu( i );
                if ( dentroBotao( mx, my, area[0], area[1], area[2], area[3] ) ){
                    focoMenu = i;
                    ativarFocoMenu();
                    break;
                }
            }
        }
        atualizarMenuControle( delta );
    }

    // metodo atualizar menu para controle
    private void atualizarMenuControle( double delta ) {
        if ( controleAtivo < 0 || estado != State.MENU ){
            return;
        }
        if ( controlePressionado( GAMEPAD_BUTTON_MIDDLE_RIGHT ) ){
            iniciar();
            return;
        }
        if ( controlePressionado( GAMEPAD_BUTTON_RIGHT_FACE_DOWN ) ){
            ativarFocoMenu();
        }
        if ( estado != State.MENU ){
            return;
        }
        if ( controlePressionado( GAMEPAD_BUTTON_RIGHT_FACE_LEFT ) ){
            trocarCor();
        }
        esperaControleMenu = Math.max( 0, esperaControleMenu - delta );
        if ( esperaControleMenu > 0 ){
            return;
        }

        int dx = 0;
        int dy = 0;
        if ( controleSegurado( GAMEPAD_BUTTON_LEFT_FACE_UP ) ){
            dy = -1;
        } else if ( controleSegurado( GAMEPAD_BUTTON_LEFT_FACE_DOWN ) ){
            dy = 1;
        } else if ( controleSegurado( GAMEPAD_BUTTON_LEFT_FACE_LEFT ) ){
            dx = -1;
        } else if ( controleSegurado( GAMEPAD_BUTTON_LEFT_FACE_RIGHT ) ){
            dx = 1;
        } else {
            double eixoX = getGamepadAxisMovement( controleAtivo, GAMEPAD_AXIS_LEFT_X );
            double eixoY = getGamepadAxisMovement( controleAtivo, GAMEPAD_AXIS_LEFT_Y );
            if ( Math.abs( eixoX ) > 0.55 || Math.abs( eixoY ) > 0.55 ){ // 0.55 pois o aanalogico tem que ser apertado mais de 50%
                if ( Math.abs( eixoX ) > Math.abs( eixoY ) ){ // ignora toques sutis
                    if ( eixoX > 0 ){
                        dx = 1;
                    } else {
                        dx = -1;
                    }
                } else {
                    if ( eixoY > 0 ){
                        dy = 1;
                    } else {
                        dy = -1;
                    }
                }
            }
        }
        if ( dx != 0 || dy != 0 ){
            moverFocoMenu( dx, dy );
            esperaControleMenu = 0.18; // tempo para aceita proxima instrução
        }
    }

    // metodo ativarFocoMenu
    private void ativarFocoMenu() {
        // seleciona entre as posicoes de 0 a 7 do array
        if ( focoMenu < 8 ){
            selecionarModo( Modo.values()[focoMenu] );
        } else {
            switch ( focoMenu ) {
                case 8 -> trocarCor();
                case 9 -> config.velocidade = ( config.velocidade + 1 ) % 3;
                case 10 -> config.tamanho = ( config.tamanho + 1 ) % 3;
                case 11 -> config.quantidadeFrutas = ( config.quantidadeFrutas + 1 ) % 4;
                case 12 -> iniciar();
                case 13 -> trocarLiquidificador();
                default -> { }
            }
        }
    }

    // Metodo areaItemMenu define o tamanho dos botoes
    private int[] areaItemMenu( int item ) {
        // Cada array guarda x, y, largura e altura do botao
        if ( item < 8 ){
            // Oito modos em 3 colunas e 3 linhas
            int coluna = item % 3;
            int linha = item / 3;
            int x = 44 + coluna * 345;
            int y = 170 + linha * 105;
            return new int[]{ x, y, 320, 82 };
        } else if ( item == 8 ){
            return new int[]{ 45, 555, 240, 48 }; // cor
        } else if ( item == 9 ){
            return new int[]{ 300, 555, 240, 48 }; // velocidade
        } else if ( item == 10 ){
            return new int[]{ 555, 555, 240, 48 }; // tamanho
        } else if ( item == 11 ){
            return new int[]{ 810, 555, 265, 48 }; // frutas
        } else if ( item == 13 ){
            return new int[]{ 734, 380, 320, 82 }; // liquidificador
        } else {
            return new int[]{ 815, 675, 260, 55 }; // iniciar
        }
    }

    // Metodo moverfocomenu
    private void moverFocoMenu( int dx, int dy ) {
        // encontra o centro do botão
        int[] atual = areaItemMenu( focoMenu );
        int centroX = atual[0] + atual[2] / 2;
        int centroY = atual[1] + atual[3] / 2;
        
        int melhor = focoMenu;
        int menorDistancia = Integer.MAX_VALUE;
        // for que percorre os 14 botoes
        for ( int i = 0; i < 14; i++ ) {
            if ( i == focoMenu ){
                continue;
            }
            int[] area = areaItemMenu( i );
            int diferencaX = area[0] + area[2] / 2 - centroX;
            int diferencaY = area[1] + area[3] / 2 - centroY;
            int distanciaFrontal;
            if ( dx != 0 ){
                distanciaFrontal = diferencaX * dx;
            } else {
                distanciaFrontal = diferencaY * dy;
            }
            if ( distanciaFrontal <= 0 ){
                continue;
            }
            int distanciaLateral;
            if ( dx != 0 ){
                distanciaLateral = Math.abs( diferencaY );
            } else {
                distanciaLateral = Math.abs( diferencaX );
            }
            int distancia = distanciaFrontal + distanciaLateral * 3;
            if ( distancia < menorDistancia ){
                menorDistancia = distancia;
                melhor = i;
            }
        }
        focoMenu = melhor;
    }

    // Metodo selecionarModo
    private void selecionarModo( Modo modo ) {
        // se o liquidificador estiver ativo eh possivel misturar os modos
        if ( config.liquidificador ){
            // se o modo estiver na lista
            if ( config.modosMisturados.contains( modo ) ){
                if ( config.modosMisturados.size() > 1 ){ // se estiver remove
                    config.modosMisturados.remove( modo );
                }
            } else { // se nao estiver adiciona
                config.modosMisturados.add( modo );
            }
            // se o modo liquidificador estiver desligado
        } else { 
            config.modo = modo;
        }
        
        focoMenu = modo.ordinal();
    }

    // Metodo trocarLiquidificador
    private void trocarLiquidificador() {
        // inverte o estado atual do liquidificador
        config.liquidificador = !config.liquidificador;
        // se o modo liquidificador for ligado
        if ( config.liquidificador ){
            config.modosMisturados.clear(); // limpa a lista
            config.modosMisturados.add( config.modo ); // adiciona o modo selecionado
        // se o modo for desligado
        } else if ( !config.modosMisturados.contains( config.modo ) ){
            // pega da lista o primeiro modo das mistura via iterador e define com omodo padrao do jogo
            config.modo = config.modosMisturados.iterator().next();
        }
        focoMenu = 13;
    }

    // Metodo trocar cor
    private void trocarCor() {
        // pega a cor atual + 1 e divide pelo tamanho do array de cores
        config.corCobra = ( config.corCobra + 1 ) % CORES_COBRA.length;
    }

    // Metodo iniciar
    private void iniciar() {
        jogo = new JogoSnake( config );
        contador = 0;
        tempoDeJogo = 0;
        pausado = false;
        aguardandoInicio = true;
        entradaLiberada = false;
        calcularTabuleiro();
        estado = State.JOGANDO;
    }

    // metodo atualizarJogo
    private void atualizarJogo( double delta ) {
        // se apertar esc, botao do controle ou clicar no menu -> vai pro menu
        if ( isKeyPressed( KEY_ESCAPE ) || controlePressionado( GAMEPAD_BUTTON_RIGHT_FACE_RIGHT )
                || clicou( 840, 24, 110, 40 ) ){
            estado = State.MENU;
            return;
        }
        
        if ( aguardandoInicio ){
            // Espera soltar a tecla ou o mouse usados para sair do menu.
            if ( !entradaLiberada ){
                entradaLiberada = !isKeyDown( KEY_ENTER ) && !isKeyDown( KEY_SPACE )
                        && !isMouseButtonDown( MOUSE_BUTTON_LEFT )
                        && !controleSegurado( GAMEPAD_BUTTON_RIGHT_FACE_DOWN )
                        && !controleSegurado( GAMEPAD_BUTTON_MIDDLE_RIGHT );
                return;
            }
            boolean teclaDeInicio = isKeyDown( KEY_ENTER ) && isKeyPressed( KEY_ENTER )
                    || isKeyDown( KEY_SPACE ) && isKeyPressed( KEY_SPACE );
            boolean controleIniciou = controlePressionado( GAMEPAD_BUTTON_RIGHT_FACE_DOWN )
                    || controlePressionado( GAMEPAD_BUTTON_MIDDLE_RIGHT );
            boolean cliqueDeInicio = isMouseButtonDown( MOUSE_BUTTON_LEFT )
                    && clicou( botaoInicioX(), botaoInicioY(), 260, 55 );
            if ( teclaDeInicio || cliqueDeInicio || controleIniciou ){
                aguardandoInicio = false;
            }
            return;
        }
        if ( isKeyPressed( KEY_P ) || controlePressionado( GAMEPAD_BUTTON_MIDDLE_RIGHT )
                || clicou( 720, 24, 110, 40 ) ){
            pausado = !pausado;
        }
        if ( pausado ){
            return;
        }
        if ( isKeyPressed( KEY_C ) || controlePressionado( GAMEPAD_BUTTON_RIGHT_FACE_LEFT ) ){
            trocarCor();
        }
        double intervalo = jogo.config.tempoPorPasso();
        if ( jogo.pilha.size() >= 8 ){
            intervalo *= 1.5;
        }
        boolean virou = false;
        if ( isKeyDown( KEY_W ) || isKeyDown( KEY_UP ) ){
            virou |= jogo.mudarDirecao( 0, -1 );
        }
        if ( isKeyDown( KEY_D ) || isKeyDown( KEY_RIGHT ) ){
            virou |= jogo.mudarDirecao( 1, 0 );
        }
        if ( isKeyDown( KEY_S ) || isKeyDown( KEY_DOWN ) ){
            virou |= jogo.mudarDirecao( 0, 1 );
        }
        if ( isKeyDown( KEY_A ) || isKeyDown( KEY_LEFT ) ){
            virou |= jogo.mudarDirecao( -1, 0 );
        }
        virou |= moverPeloControle();
        if ( virou ){
            contador = Math.max( contador, intervalo - 0.04 );
        }
        tempoDeJogo += delta;
        contador += delta;
        while ( contador >= intervalo && estado == State.JOGANDO ) {
            contador -= intervalo;
            jogo.passo();
            if ( jogo.acabou ){
                estado = State.GAMEOVER;
            }
        }
    }

    // metodo moverPeloControle
    private boolean moverPeloControle() {
        // se nao houver controle ativo return false
        if ( controleAtivo < 0 ){
            return false;
        }
        
        // verificacao das setas do controle
        if ( controleSegurado( GAMEPAD_BUTTON_LEFT_FACE_UP ) ){
            return jogo.mudarDirecao( 0, -1 );
        }
        if ( controleSegurado( GAMEPAD_BUTTON_LEFT_FACE_RIGHT ) ){
            return jogo.mudarDirecao( 1, 0 );
        }
        if ( controleSegurado( GAMEPAD_BUTTON_LEFT_FACE_DOWN ) ){
            return jogo.mudarDirecao( 0, 1 );
        }
        if ( controleSegurado( GAMEPAD_BUTTON_LEFT_FACE_LEFT ) ){
            return jogo.mudarDirecao( -1, 0 );
        }
        // leitura do analogico esquerdo
        double eixoX = getGamepadAxisMovement( controleAtivo, GAMEPAD_AXIS_LEFT_X );
        double eixoY = getGamepadAxisMovement( controleAtivo, GAMEPAD_AXIS_LEFT_Y );
        // zona morta para controle com analógico
        if ( Math.abs( eixoX ) < 0.40 && Math.abs( eixoY ) < 0.40 ){
            return false;
        }
        // movimentacao do analogico
        if ( Math.abs( eixoX ) > Math.abs( eixoY ) ){
            if ( eixoX > 0 ){
                return jogo.mudarDirecao( 1, 0 );
            } else {
                return jogo.mudarDirecao( -1, 0 );
            }
        } else {
            if ( eixoY > 0 ){
                return jogo.mudarDirecao( 0, 1 );
            } else {
                return jogo.mudarDirecao( 0, -1 );
            }
        }
    }

    // metodo atualizarFIm
    private void atualizarFim() {
        // checagem de reiniciar partida
        if ( isKeyPressed( KEY_R ) || isKeyPressed( KEY_ENTER )
                || controlePressionado( GAMEPAD_BUTTON_RIGHT_FACE_DOWN )
                || controlePressionado( GAMEPAD_BUTTON_MIDDLE_RIGHT )
                || clicou( 722, 430, 335, 55 ) ){
            iniciar();
        }
        // chegagem do menu
        if ( isKeyPressed( KEY_ESCAPE ) || isKeyPressed( KEY_M )
                || controlePressionado( GAMEPAD_BUTTON_RIGHT_FACE_RIGHT )
                || clicou( 722, 500, 335, 55 ) ){
            estado = State.MENU;
        }
    }

    // metodo clicou, verifica se clicou em algum botao retornando true ou false
    private boolean clicou( int x, int y, int largura, int altura ) {
        return isMouseButtonPressed( MOUSE_BUTTON_LEFT )
                && dentroBotao( mouseVirtualX(), mouseVirtualY(), x, y, largura, altura );
    }

    // metodo dentroBotao, com base as variaveis passadas do clicou verifica se foi dentro do botao
    private boolean dentroBotao( int mx, int my, int x, int y, int largura, int altura ) {
        return mx >= x && mx < x + largura && my >= y && my < y + altura;
    }

    // Mantem as proporcoes do jogo em qualquer tamanho de tela
    private double escalaTela() {
        if ( getScreenWidth() <= 0 || getScreenHeight() <= 0 ){
            return 1;
        }
        return Math.min( (double) getScreenWidth() / LARGURA_BASE,
                         (double) getScreenHeight() / ALTURA_BASE );
    }

    private double margemTelaX() {
        return ( getScreenWidth() - LARGURA_BASE * escalaTela() ) / 2;
    }

    private double margemTelaY() {
        return ( getScreenHeight() - ALTURA_BASE * escalaTela() ) / 2;
    }

    private int mouseVirtualX() {
        return (int) Math.floor( ( getMouseX() - margemTelaX() ) / escalaTela() );
    }

    private int mouseVirtualY() {
        return (int) Math.floor( ( getMouseY() - margemTelaY() ) / escalaTela() );
    }

    // metodo draw desenha as telas do jogo usando switch case
    @Override
    public void draw() {
        clearBackground( FUNDO );
        AffineTransform transformacao = getGraphics2D().getTransform();
        translate( margemTelaX(), margemTelaY() );
        scale( escalaTela(), escalaTela() );
        switch ( estado ) {
            case MENU -> desenharMenu();
            case JOGANDO -> desenharJogo();
            case GAMEOVER -> {
                desenharJogo();
                desenharFim();
            }
        }
        botao( 960, 24, 125, 40, "Sair", false );
        getGraphics2D().setTransform( transformacao );
    }

    //Metodo desenharMenu
    private void desenharMenu() {
        drawText( "SNAKEGAME & ESTRUTURAS DE DADOS", 42, 32, 30, VERDE );
        drawText( "Escolha um modo ou misture os oito no Liquidificador.", 44, 83, 17, CINZA );
        if ( controleAtivo >= 0 ){
            drawText( "Controle USB conectado: direcional/analógico navega, A seleciona, Start inicia",
                    44, 116, 14, CINZA );
            drawText( "X muda a cor", 44, 137, 13, CINZA );
        }
        for ( int i = 0; i < Modo.values().length; i++ ) {
            Modo modo = Modo.values()[i];
            int[] area = areaItemMenu( i );
            boolean ativo = config.usa( modo );
            botao( area[0], area[1], area[2], area[3], ( i + 1 ) + "  " + modo.nome, ativo );
            Color corDescricao;
            if ( ativo ){
                corDescricao = FUNDO;
            } else {
                corDescricao = CINZA;
            }
            drawText( modo.descricao, area[0] + 20, area[1] + 45, 13, corDescricao );
        }
        int[] areaLiquidificador = areaItemMenu( 13 );
        String estadoLiquidificador;
        Color corDescricaoLiquidificador;
        String instrucaoLiquidificador;
        if ( config.liquidificador ){
            estadoLiquidificador = "LIGADO";
            corDescricaoLiquidificador = FUNDO;
            instrucaoLiquidificador = "Clique nos modos ou use 1-8 para adicioná-los/removê-los (mínimo 1).";
        } else {
            estadoLiquidificador = "DESLIGADO";
            corDescricaoLiquidificador = CINZA;
            instrucaoLiquidificador = "Ative o Liquidificador para selecionar vários modos.";
        }
        botao( areaLiquidificador[0], areaLiquidificador[1], areaLiquidificador[2],
                areaLiquidificador[3], "LIQUIDIFICADOR: " + estadoLiquidificador,
                config.liquidificador );
        drawText( "Combine modos", areaLiquidificador[0] + 20,
                areaLiquidificador[1] + 45, 13, corDescricaoLiquidificador );
        drawText( instrucaoLiquidificador, 45, 503, 15, CINZA );
        botao( 45, 555, 240, 48, "C  Cor: " + config.nomeCor(), false );
        fillCircle( 268, 578, 9, CORES_COBRA[config.corCobra] );
        botao( 300, 555, 240, 48, "V  " + config.nomeVelocidade(), false );
        botao( 555, 555, 240, 48, "T  " + config.nomeTamanho(), false );
        botao( 810, 555, 265, 48, "F  Frutas: " + config.nomeQuantidade(), false );
        drawText( "Teclado: 1-8 modos  |  L liquidificador  |  Espaço seleciona  |  Enter inicia",
                45, 693, 14, CINZA );
        botao( 815, 675, 260, 55, "INICIAR PARTIDA", true );
        if ( controleAtivo >= 0 ){
            int[] foco = areaItemMenu( focoMenu );
            drawRectangle( foco[0] - 3, foco[1] - 3, foco[2] + 6, foco[3] + 6,
                           new Color( 255, 222, 103 ) );
        }
    }

    // metodo desenhar jogo
    private void desenharJogo() {
        // calcula o tamanho do tabuleiro
        calcularTabuleiro();
        drawText( "SNAKEGAME & ESTRUTURAS DE DADOS", 34, 24, 24, VERDE );
        drawText( "WASD / SETAS: MOVER   |   P: PAUSA   |   C: COR", 35, 72, 15, CINZA );
        if ( controleAtivo >= 0 ){
            drawText( "Controle USB conectado", 715, 79, 14, CINZA );
            drawText( "Start pausa | B menu | X cor", 715, 101, 13, CINZA );
        }
        if ( !aguardandoInicio ){
            String textoPausa;
            if ( pausado ){
                textoPausa = "CONTINUAR";
            } else {
                textoPausa = "PAUSAR";
            }
            botao( 720, 24, 110, 40, textoPausa, false );
        }
        botao( 840, 24, 110, 40, "MENU", false );
        int largura = jogo.config.colunas() * celula;
        int altura = jogo.config.linhas() * celula;
        fillRectangle( tabX - 5, tabY - 5, largura + 10, altura + 10, PAINEL );
        for ( int y = 0; y < jogo.config.linhas(); y++ ) {
            for ( int x = 0; x < jogo.config.colunas(); x++ ) {
                Color corCasa;
                if ( ( x + y ) % 2 == 0 ){
                    corCasa = new Color( 31, 46, 56 ); // azul fosco 1
                } else {
                    corCasa = new Color( 36, 52, 62 ); // azul fosco 2
                }
                fillRectangle( tabX + x * celula + 1, tabY + y * celula + 1,
                        celula - 2, celula - 2, corCasa ); // desenha a celula deixando a margem de 1 pixel
            }
        }
        for ( Posicao parede : jogo.paredes ) {
            bloco( parede, new Color( 120, 129, 140 ) ); // cinza cor da parede
        }
        if ( jogo.chave != null ){
            bloco( jogo.chave, new Color( 255, 211, 82 ) ); // amarelo chave
            textoCasa( jogo.chave, "C", FUNDO );
        }
        if ( jogo.veneno != null ){
            Posicao p = jogo.veneno;
            int x = tabX + p.x() * celula;
            int y = tabY + p.y() * celula;
            fillCircle( x + celula / 2, y + celula / 2, celula * 0.36,
                    new Color( 154, 70, 173 ) ); // roxo veneno
            textoCasa( p, "!", TEXTO );
        }
        if ( jogo.descarte != null ){
            bloco( jogo.descarte, new Color( 88, 186, 229 ) ); // azul descarte
            textoCasa( jogo.descarte, "D", FUNDO );
        }
        for ( JogoSnake.Fruta fruta : jogo.frutas ) desenharFruta( fruta );
        desenharCobra( jogo.cobra, CORES_COBRA[config.corCobra] );
        desenharHud();
        if ( aguardandoInicio ){
            desenharInicio();
        }
        if ( pausado && estado == State.JOGANDO ){
            fillRectangle( tabX, tabY + altura / 2 - 42, largura, 84, new Color( 14, 21, 31, 225 ) ); //azul escruto semitransparente, fundo da pausa
            drawText( "PAUSADO - pressione P", tabX + 45, tabY + altura / 2 - 14, 25, TEXTO );
        }
    }

    // A quantidade de casas muda, mas o tabuleiro usa sempre a tela toda
    private void calcularTabuleiro() {
        celula = Math.min( AREA_LARGURA / jogo.config.colunas(),
                           AREA_ALTURA / jogo.config.linhas() );
        tabX = AREA_X + ( AREA_LARGURA - jogo.config.colunas() * celula ) / 2;
        tabY = AREA_Y + ( AREA_ALTURA - jogo.config.linhas() * celula ) / 2;
    }

    private int botaoInicioX() {
        return tabX + ( jogo.config.colunas() * celula - 260 ) / 2;
    }

    private int botaoInicioY() {
        return tabY + jogo.config.linhas() * celula / 2 + 35;
    }

    private void desenharInicio() {
        int largura = jogo.config.colunas() * celula;
        int altura = jogo.config.linhas() * celula;
        fillRectangle( tabX, tabY, largura, altura, new Color( 14, 21, 31, 225 ) );
        String titulo = "PRONTO PARA JOGAR?";
        int tituloX = tabX + ( largura - measureText( titulo, 27 ) ) / 2;
        drawText( titulo, tituloX, tabY + altura / 2 - 85, 27, VERDE );
        String instrucao = "A cobra e o cronômetro aguardam você.";
        int instrucaoX = tabX + ( largura - measureText( instrucao, 16 ) ) / 2;
        drawText( instrucao, instrucaoX, tabY + altura / 2 - 34, 16, TEXTO );
        botao( botaoInicioX(), botaoInicioY(), 260, 55, "COMEÇAR", true );
        String ajuda = "Espaço / Enter / A / Start ou clique";
        int ajudaX = tabX + ( largura - measureText( ajuda, 15 ) ) / 2;
        drawText( ajuda, ajudaX, botaoInicioY() + 70, 15, CINZA );
    }

    private void bloco( Posicao p, Color cor ) {
        int margem = Math.max( 2, celula / 12 );
        fillRectangle( tabX + p.x() * celula + margem, tabY + p.y() * celula + margem,
                       celula - 2 * margem, celula - 2 * margem, cor );
    }

    private void textoCasa( Posicao p, String texto, Color cor ) {
        int fonte = Math.max( 17, celula / 2 );
        int x = tabX + p.x() * celula + ( celula - measureText( texto, fonte ) ) / 2;
        int y = tabY + p.y() * celula + ( celula - fonte ) / 2;
        drawText( texto, x, y, fonte, cor );
    }

    private void desenharFruta( JogoSnake.Fruta fruta ) {
        Posicao p = fruta.posicao;
        int x = tabX + p.x() * celula;
        int y = tabY + p.y() * celula;
        fillCircle( x + celula / 2, y + celula / 2, celula * 0.36, CORES_FRUTA[fruta.cor] );
        if ( jogo.config.usa( Modo.ALADA ) ){
            drawLine( x + celula * 0.05, y + celula * 0.24,
                      x + celula * 0.27, y + celula * 0.40, TEXTO );
            drawLine( x + celula * 0.73, y + celula * 0.40,
                      x + celula * 0.95, y + celula * 0.24, TEXTO );
        }
    }

    private void desenharCobra( Deque<Posicao> corpo, Color cor ) {
        int indice = 0;
        for ( Posicao p : corpo ) {
            Color corParte;
            if ( indice == 0 ){
                corParte = cor;
            } else {
                corParte = new Color( cor.getRed(), cor.getGreen(), cor.getBlue(), 190 );
            }
            bloco( p, corParte );
            if ( indice == 0 ){
                fillCircle( tabX + p.x() * celula + celula * 0.65,
                            tabY + p.y() * celula + celula * 0.35,
                            Math.max( 2, celula * 0.07 ), FUNDO );
            }
            indice++;
        }
    }

    private void desenharHud() {
        int x = 710;
        fillRectangle( x, 120, 375, 610, PAINEL );
        drawText( "PONTOS", x + 22, 140, 17, CINZA );
        drawText( String.valueOf( jogo.pontos ), x + 20, 168, 40, VERDE );
        drawText( "TEMPO", x + 210, 140, 17, CINZA );
        drawText( formatarTempo(), x + 207, 172, 28, TEXTO );
        drawText( "Frutas comidas: " + jogo.frutasComidas, x + 22, 226, 17, TEXTO );
        drawText( "Deque - corpo: " + jogo.cobra.size() + " partes", x + 22, 258, 17, TEXTO );
        drawText( "addFirst(cabeça) / removeLast(cauda)", x + 22, 285, 13, CINZA );

        int y = 326;
        if ( jogo.config.usa( Modo.FILA ) ){
            drawText( "FILA - próxima cor (FIFO)", x + 22, y, 17, TEXTO );
            int i = 0;
            for ( Integer cor : jogo.fila ) {
                fillCircle( x + 40 + i * 45, y + 48, 13, CORES_FRUTA[cor] );
                if ( i == 0 ){
                    drawText( "↑", x + 32, y + 65, 17, TEXTO );
                }
                i++;
            }
            y += 110;
        }
        if ( jogo.config.usa( Modo.PILHA ) ){
            drawText( "PILHA - carga (LIFO)", x + 22, y, 17, TEXTO );
            int i = 0;
            for ( Integer cor : jogo.pilha ) {
                if ( i >= 6 ){
                    break;
                }
                fillRectangle( x + 25, y + 32 + i * 25, 160, 22, CORES_FRUTA[cor] );
                if ( i == 0 ){
                    drawText( "← topo", x + 195, y + 32, 15, CINZA );
                }
                i++;
            }
            drawText( "Entrega no bloco D", x + 22, y + 202, 15, CINZA );
            y += 240;
        }
        if ( jogo.mensagem != null && !jogo.mensagem.isEmpty() ){
            drawText( jogo.mensagem, x + 20, 687, 15, TEXTO );
        }
    }

    private void desenharFim() {
        fillRectangle( 700, 340, 390, 250, new Color( 14, 21, 31, 245 ) );
        String titulo;
        if ( jogo.mensagem.equals( "Você ocupou todo o mapa!" ) ){
            titulo = "VITÓRIA!";
        } else {
            titulo = "FIM DE JOGO";
        }
        drawText( titulo, 725, 358, 27, VERDE );
        drawText( "Pontuação: " + jogo.pontos, 725, 397, 18, TEXTO );
        drawText( "Tempo: " + formatarTempo(), 905, 397, 18, TEXTO );
        botao( 722, 430, 335, 55, "R / Enter / A - Jogar de novo", true );
        botao( 722, 500, 335, 55, "M / Esc / B - Menu", false );
    }

    private String formatarTempo() {
        long segundos = (long) tempoDeJogo;
        return String.format( "%02d:%02d", segundos / 60, segundos % 60 );
    }

    private void botao( int x, int y, int largura, int altura, String texto, boolean ativo ) {
        Color corBotao;
        Color corBorda;
        Color corTexto;
        if ( ativo ){
            corBotao = VERDE;
            corBorda = VERDE;
            corTexto = FUNDO;
        } else {
            corBotao = PAINEL;
            corBorda = new Color( 62, 86, 101 );
            corTexto = TEXTO;
        }
        fillRectangle( x, y, largura, altura, corBotao );
        drawRectangle( x, y, largura, altura, corBorda );
        drawText( texto, x + 13, y + 9, 17, corTexto );
    }

    public static void main( String[] args ) {
        new SnakeGame();
    }

}
