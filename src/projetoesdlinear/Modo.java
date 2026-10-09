package projetoesdlinear;

/**
 *
 * @author enzocosta
 */

// Define a enumeração dos nomes de cada modo
public enum Modo {

    CLASSICO( "Clássico", "Paredes e corpo encerram a partida" ),
    FILA( "Ordem das frutas", "Coma as cores na ordem da fila" ),
    PILHA( "Coleta e entrega", "Empilhe e entregue no descarte" ),
    PACIFICO( "Pacífico", "Atravesse paredes e o próprio corpo" ),
    ALADA( "Fruta alada", "A fruta se move pelo mapa" ),
    PAREDE( "Parede", "Obstáculos surgem ao pontuar" ),
    VENENO( "Veneno", "Fruta roxa tira um ponto" ),
    CHAVE( "Chave", "Pegue a chave antes das frutas" );
    public final String nome;
    public final String descricao;

    // Construtor do modo que prepara um novo objeto com nome e descricao
    Modo( String nome, String descricao ) {
        this.nome = nome;
        this.descricao = descricao;
    }

}
