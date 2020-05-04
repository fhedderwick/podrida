package podrida.model;

public class Baza {
    
    private final int _pedido;
    private final int _llevado;
    
    public Baza(final int pedido, final int llevado){
        _pedido = pedido;
        _llevado = llevado;
    }

    public int getPedido() {
        return _pedido;
    }

    public int getLlevado() {
        return _llevado;
    }
    
}
