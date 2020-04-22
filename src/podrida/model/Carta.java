package podrida.model;

public class Carta implements Comparable<Carta>{

    private final Valor _valor;
    private final Palo _palo;
    private final int _valorRelativo;
    private final String _id;
    
    public Carta(final Valor valor, final Palo palo, final int valorRelativo, final String id){
        _valor = valor;
        _palo = palo;
        _valorRelativo = valorRelativo;
        _id = id;
    }

    public boolean superaA(final Carta supuestaPeor) {
        if(supuestaPeor == null){
            return true;
        }
        return _valorRelativo > supuestaPeor.getValorRelativo();
    }
    
    public Valor getValor(){
        return _valor;
    }
    
    public Palo getPalo(){
        return _palo;
    }
    
    public int getValorRelativo(){
        return _valorRelativo;
    }

    public String getId() {
        return _id;
    }

    @Override
    public int compareTo(final Carta other) {
        return this.getValorRelativo() - other.getValorRelativo();
    }

    public enum Valor{
        UNO, DOS, TRES, CUATRO, CINCO, SEIS, SIETE, DIEZ, ONCE, DOCE;
    };
    public enum Palo{
        ESPADA, BASTO, ORO, COPA;
    };
}
