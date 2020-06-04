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

    public String getReadableName() {
        return _valor.getCode() + " de " + _palo.name();
    }

    public enum Valor{
        UNO(1), DOS(2), TRES(3), CUATRO(4), CINCO(5), SEIS(6), SIETE(7), DIEZ(10), ONCE(11), DOCE(12);
        final int _code;
        private Valor(final int code){
            _code = code;
        }
        private int getCode() {
            return _code;
        }
    };
    public enum Palo{
        ESPADA, BASTO, ORO, COPA;
    };
}
