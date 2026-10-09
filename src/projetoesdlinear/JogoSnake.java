package projetoesdlinear;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 *
 * @author enzocosta
 */
public class JogoSnake {

    // Guarda a posicao e a cor da fruta
    public static class Fruta {
        public Posicao posicao;
        public final int cor;

        // construtor que prepara o objeto 
        Fruta( Posicao posicao, int cor ) {
            this.posicao = posicao;
            this.cor = cor;
        }
    }

    // Variaveis constantes da classe
    public final Configuracao config;
    public final Deque<Posicao> cobra = new ArrayDeque<>();
    public final Queue<Integer> fila = new ArrayDeque<>();
    public final Deque<Integer> pilha = new ArrayDeque<>();
    public final List<Fruta> frutas = new ArrayList<>();
    public final List<Posicao> paredes = new ArrayList<>();
    private final Random sorteio;

    // Variaveis
    public Posicao descarte;
    public Posicao veneno;
    public Posicao chave;
    public int pontos;
    public int frutasComidas;
    public int direcaoX = 1;
    public int direcaoY;
    public int proximaDirecaoX = 1;
    public int proximaDirecaoY;
    private boolean direcaoEscolhidaNestePasso;
    public int passos;
    private int pontosParaParede = 3;
    public boolean acabou;
    public String mensagem = "";

    public JogoSnake( Configuracao config ) {
        this( config, new Random() );
    }
    
    // Construtor do jogo
    JogoSnake( Configuracao config, Random sorteio ) {
        this.config = config; // atualiza o valor do objeto com ovalor passado 
        this.sorteio = sorteio;
        // pega o valor do meio de linhas e colunas
        int x = config.colunas() / 2; 
        int y = config.linhas() / 2;
        
        // insere uma nova cabeça no inicio do deque
        cobra.addFirst( new Posicao( x, y ) );
        // monta o corpo inicial acrescentando uma posição ao fim do deque
        cobra.addLast( new Posicao( x - 1, y ) );
        cobra.addLast( new Posicao( x - 2, y ) );

        // IF QUE SELECIONAM OS MODOS DO JOGO
        if ( config.usa( Modo.FILA ) ){
          // for que sorteia a proxima cor da fila de frutas 
            for ( int i = 0; i < 4; i++ ){
                fila.add( sorteio.nextInt( 4 ) );
            }
        }
        if ( config.usa( Modo.PILHA ) ){
            descarte = sortearCasaLivre(); //sorteia o lugar da casa de descarte
        }
        if ( config.usa( Modo.CHAVE ) ){
            chave = sortearCasaLivre(); // sorteia a posicao da chave
            mensagem = "Pegue a chave para liberar as frutas.";
        }
        if ( config.usa( Modo.VENENO ) ){ 
            veneno = sortearCasaLivre(); // sorteia a posicao do veneno
        }
        completarFrutas();
    }

    // abre o metodo cabeca e  ve a primeira posicao da cabeca no deck
    public Posicao cabeca() {
        return cobra.peekFirst();
    }

    // Metodo mudarDirecao aceita
    public boolean mudarDirecao( int dx, int dy ) {
        if ( direcaoEscolhidaNestePasso || dx == direcaoX && dy == direcaoY
                || dx == -direcaoX && dy == -direcaoY ){
            return false;
        }
        proximaDirecaoX = dx;
        proximaDirecaoY = dy;
        direcaoEscolhidaNestePasso = true;
        return true;
    }

    // Metodo que guarda/faz andar sozinho apenas com a direcao da cobra
    public void passo() {
        if ( acabou ){
            return;
        }
        passos++;
        direcaoX = proximaDirecaoX; // horizontal
        direcaoY = proximaDirecaoY; // vertical
        direcaoEscolhidaNestePasso = false; // se a cobra vira

        // move a cabeca da cobra uma casa e ve se ela atravessa ou nao a parede ( morreu ou nao )
        Posicao destino = normalizar( cabeca().mover( direcaoX, direcaoY ) );
        Fruta fruta = encontrarFruta( destino ); // procura a fruta na oarte em que a cabeca esta
        boolean cresce = fruta != null; // a cobra cresce apenas quando a cabeca encontra a fruta
        if ( destino == null || colisaoCorpo( destino, cresce )
                || colisaoParede( destino ) ){
            acabou = true;
            mensagem = "A cobra bateu!";
            return;
        }
        
        
        cobra.addFirst( destino ); // deque: a cabeça entra pelo início
        if ( !cresce ){
            cobra.removeLast(); // e a cauda sai pelo fim
        }
        // verifica se a cabeca da cobra esta no local de descarte
        if ( descarte != null && destino.equals( descarte ) ){
            entregarPilha();
        }
        if ( fruta != null ){
            comer( fruta );
        }
        if ( acabou ){
            return;
        }
        if ( chave != null && destino.equals( chave ) ){
            chave = null;
            mensagem = "Chave encontrada! Agora colete as frutas.";
            completarFrutas();
        }
        if ( veneno != null && destino.equals( veneno ) ){
            pontos = Math.max( 0, pontos - 1 );
            mensagem = "Veneno! Você perdeu um ponto.";
            veneno = null;
            veneno = sortearCasaLivre();
        }
        if ( config.usa( Modo.PAREDE ) ){
            criarParedes();
        }
        if ( config.usa( Modo.ALADA ) && passos % 3 == 0 ){
            moverFrutaAlada();
        }
    }

    
    // metodo normalizar verifica se esta no modo pacifico e leva a cobra pro lado oposto
    private Posicao normalizar( Posicao p ) {
        if ( dentro( p ) ){
            return p;
        }
        // utiliza o resto da divisao e eh usado para colocar a cobra do outro lado da parede
        if ( config.usa( Modo.PACIFICO ) ){
            return new Posicao( Math.floorMod( p.x(), config.colunas() ),
                                Math.floorMod( p.y(), config.linhas() ) );
        }
        return null;
    }

    // metodo que derifica se a cobra esta detro do tabuleiro
    private boolean dentro( Posicao p ) {
        return p.x() >= 0 && p.x() < config.colunas()
                && p.y() >= 0 && p.y() < config.linhas();
    }

    // Metodo que verifica se o corpo e o destino se colidem
    private boolean colisaoCorpo( Posicao p, boolean cresce ) {
        if ( config.usa( Modo.PACIFICO ) ){
            return false;
        }
        int indice = 0;
        for ( Posicao parte : cobra ) {
            boolean caudaVaiSair = !cresce && indice == cobra.size() - 1;
            if ( parte.equals( p ) && !caudaVaiSair ){
                return true;
            }
            indice++;
        }
        return false;
    }

    // metodo que verifica se colidiu com a parede
    private boolean colisaoParede( Posicao p ) {
        if ( config.usa( Modo.PACIFICO ) ){
            return false;
        }
        return p != null && paredes.contains( p );
    }

    // Metodo comer
    private void comer( Fruta fruta ) {
        // remove da lista as frutas que a cobra comeu
        frutas.remove( fruta );
        // se esta no modo fila e come a cor diferente da ordem a pessoa perde
        if ( config.usa( Modo.FILA ) ){
            if ( fruta.cor != fila.peek() ){
                acabou = true;
                mensagem = "Fruta fora da ordem da fila!";
                return;
            }
            fila.remove(); //  sai o primeiro alvo
            fila.add( sorteio.nextInt( 4 ) ); // entra outro no fim
        }
        if ( config.usa( Modo.PILHA ) ){
            pilha.push( fruta.cor ); // a fruta aguarda no topo da pilha
            mensagem = "Leve a carga até o descarte.";
        } else {
            pontos++;
        }
        frutasComidas++;
        if ( config.usa( Modo.CHAVE ) && frutas.isEmpty() ){
            chave = sortearCasaLivre();
            if ( chave == null ){
                acabou = true;
                mensagem = "Você ocupou todo o mapa!";
            } else {
                mensagem = "Pegue a chave para liberar mais frutas.";
            }
        }
        completarFrutas(); // atualiza as frutas da rodada
    }

    // Metodo que entrega a pilha 
    private void entregarPilha() {
        // verifica se a pilha esta vazia
        if ( pilha.isEmpty() ){
            return;
        }
        int quantidade = 0;
        // enquanto houver fruta na piha
        while ( !pilha.isEmpty() ) {
            pilha.pop(); // pop: a última fruta é entregue primeiro
            pontos++;
            quantidade++;
            if ( cobra.size() > 2 ){
                cobra.removeLast();
            }
        }
        mensagem = quantidade + " frutas entregues no descarte!";
    }

    // Metodo completar frutas 
    private void completarFrutas() {
        // se houver alguma chave nao aparece as frutas
        if ( config.usa( Modo.CHAVE ) && chave != null ){
            return;
        }
        // calcula a quantidade de frutas que cabem no mapa
        int limite = Math.min( config.limiteFrutas(), config.colunas() * config.linhas() );
        
        // so gera novas frutas se nao estiver no modo chave ou se nao houver chaves
        if ( !config.usa( Modo.CHAVE ) || frutas.isEmpty() ){
            while ( frutas.size() < limite ) {
                Posicao casa = sortearCasaLivre();
                if ( casa == null ){
                    break;
                }
                int cor = sorteio.nextInt( 4 );
                // se estiver no modo fila e nao houver a cor no mapa essa cor eh adicionada no comeco da fila
                if ( config.usa( Modo.FILA ) && !existeAlvoDaFila() ){
                    cor = fila.peek();
                }
                frutas.add( new Fruta( casa, cor ) );
            }
        }
        // verifica novamente se tem ou nao a cor da fila e se ncessario faz a troca
        if ( config.usa( Modo.FILA ) && !existeAlvoDaFila() && !frutas.isEmpty() ){
            Fruta anterior = frutas.get( 0 );
            frutas.set( 0, new Fruta( anterior.posicao, fila.peek() ) );
        }
        // se nao houver mais espaco para gerar frutas o jogador ganha
        if ( frutas.isEmpty() ){
            acabou = true;
            mensagem = "Você ocupou todo o mapa!";
        }
    }

    // metodo existeAlvoDaFila que garante que tenha a fruta certa
    private boolean existeAlvoDaFila() {
        // se a fila estiver vazia retorna false
        if ( fila.isEmpty() ){
            return false;
        }
        // faz com que verifique a cor das frutas no mapa e as compare com as da fila
        for ( Fruta fruta : frutas ) {
            // se encontrar a cor certa o jogo pode continuar
            if ( fruta.cor == fila.peek() ){
                return true;
            }
        }
        return false;
    }

    // metodo encontrarfruta
    private Fruta encontrarFruta( Posicao p ) {
        // se receber uma posicao null retorna null
        if ( p == null ){
            return null;
        }
        // for que percorre todas as posicoes das frutas no mapa
        for ( Fruta fruta : frutas ) {
            // se encontrar retorna a posicao da fruta
            if ( fruta.posicao.equals( p ) ){
                return fruta;
            }
        }
        return null;
    }

    // moverFrutaAlada
    private void moverFrutaAlada() {
        // percorre todas as frutas do jogo
        for ( Fruta fruta : frutas ) {
            // direcoes posiveis das frutas andaresm
            int[][] direcoes = { { 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 } };
            int inicio = sorteio.nextInt( 4 ); // sorteia um ponto de inicio
            // for que pecorre as 4 opcoes de direcao
            for ( int i = 0; i < 4; i++ ) {
                // %4 para andar em circulo
                int[] d = direcoes[( inicio + i ) % 4];
                // calcula uma nova posicao em um ponto vizinho
                Posicao nova = fruta.posicao.mover( d[0], d[1] );
                // se a casa estiver livre atualiza a posicao e break
                if ( dentro( nova ) && !ocupada( nova ) ){
                    fruta.posicao = nova;
                    break;
                }
            }
        }
    }

    // metodo Criar paredes
    private void criarParedes() {
        //enquando os pontos forem >= +3
        while ( pontos >= pontosParaParede ) {
            // onde a cabeca vai estar
            Posicao proximaCasa = normalizar( cabeca().mover( direcaoX, direcaoY ) );
            //sorteia e verifica se pode colocar uma parede em uma casa vazia
            Posicao nova = sortearCasaLivre( proximaCasa );
            if ( nova != null ){
                paredes.add( nova );
            }
            pontosParaParede += 3;
        }
    }

    // metodo sortearCasaLivre
    private Posicao sortearCasaLivre() {
        return sortearCasaLivre( null );
    }

    // METODO SORTEARCASALIVRE
    private Posicao sortearCasaLivre( Posicao evitar ) {
        // lista temporaria livres
        List<Posicao> livres = new ArrayList<>();
        // for que percorre linhas e colunas
        for ( int y = 0; y < config.linhas(); y++ ) {
            for ( int x = 0; x < config.colunas(); x++ ) {
                Posicao p = new Posicao( x, y );
                //  se a casa esta vazia e diferente de onde deve evitar, adiciona na lista livre
                if ( !ocupada( p ) && !p.equals( evitar ) ){
                    livres.add( p );
                }
            }
        }
        // se a lista livres estiver vazia, significa que esta 100% lotado e retorna null
        if ( livres.isEmpty() ){
            return null;
        } else { // se houver opcoes, sorteia de forma aleatoria um valor valido
            return livres.get( sorteio.nextInt( livres.size() ) );
        }
    }
    
    
    // metodo ocupada
    private boolean ocupada( Posicao p ) {
        // faz a checagem de colisao com todas as opcoes possiveis
        return cobra.contains( p ) || p.equals( descarte ) || p.equals( chave )
                || p.equals( veneno ) || paredes.contains( p ) || encontrarFruta( p ) != null;
    }

}
