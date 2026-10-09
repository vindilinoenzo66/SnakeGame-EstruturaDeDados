package projetoesdlinear;

import java.util.EnumSet;

/**
 *
 * @author enzocosta
 */
public class Configuracao {

    // Variaveis iniciais para o jogo
    public Modo modo = Modo.CLASSICO;
    public boolean liquidificador;
    public final EnumSet<Modo> modosMisturados = EnumSet.of( Modo.CLASSICO );
    public int velocidade = 1;
    public int tamanho = 1;
    public int quantidadeFrutas = 0;
    public int corCobra = 0;

    // Metodo que decide se esta ou nao no liquidificador
    public boolean usa( Modo modo ) {
        if ( liquidificador ){
            return modosMisturados.contains( modo );
        } else {
            return this.modo == modo;
        }
    }

    // Metodo que define a quantidade de colunas de acordo com o tamanho do mapa
    public int colunas() {
        return new int[]{ 10, 17, 24 }[tamanho];
    }

    // Metodo que define a quantidade de linhas de acordo com o tamanho do mapa
    public int linhas() {
        return new int[]{ 9, 15, 21 }[tamanho];
    }

    // Metodo que define a quantidade de tempo a andar cada quadradinho de acordo com o tamanho do mapa
    public double tempoPorPasso() {
        return new double[]{ 0.24, 0.15, 0.09 }[velocidade];
    }

    // Limite de frutas a ser escolido
    public int limiteFrutas() {
        return new int[]{ 1, 3, 5, Integer.MAX_VALUE }[quantidadeFrutas];
    }

    // Metodos que definem o nome de cada velocidade, tamanho, quantidade e cor
    public String nomeVelocidade() {
        return new String[]{ "Tartaruga", "Cobrinha", "Coelho" }[velocidade];
    }
    public String nomeTamanho() {
        return new String[]{ "Pequeno 10x9", "Padrão 17x15", "Grande 24x21" }[tamanho];
    }
    public String nomeQuantidade() {
        return new String[]{ "1", "3", "5", "Lotação" }[quantidadeFrutas];
    }
    public String nomeCor() {
        return new String[]{ "Verde", "Azul", "Rosa", "Laranja", "Roxo", "Branco" }[corCobra];
    }

}
