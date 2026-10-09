package projetoesdlinear;

/**
 *
 * @author luism
 */

// Define um valor imutavel de x e y para a posicao
public record Posicao( int x, int y ) {

    // chama o metodo mover e  cria uma posicao mexendo x e y
    public Posicao mover( int dx, int dy ) {
        return new Posicao( x + dx, y + dy ); // sempre atualizanod x e y
    }

}
