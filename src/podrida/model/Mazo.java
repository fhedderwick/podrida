package podrida.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import podrida.utils.Utils;

public class Mazo {

    private final List<Carta> _mazo;
    private final int _cantMaximaCartas;

    public void devolverAlMazo(final Carta carta){
        _mazo.add(carta);
    }
    public void devolverAlMazo(final List<Carta> cartas){
        for(final Carta carta : cartas){
            devolverAlMazo(carta);
        }
    }
    
    public Carta getCarta(){
        return _mazo.remove(Utils.getRandomInt(_mazo.size()));
    }
    
    public List<Carta> getCartas(final int cant){
        final List<Carta> cartas = new ArrayList<>();
        for(int i=0; i<cant; i++){
            cartas.add(getCarta());
        }
        Collections.sort(cartas);
        return cartas;
    }
    
    public Mazo(final int barajasEspanolas){
        _mazo = new ArrayList<>();
        for(int i=0;i<barajasEspanolas;i++){
            addBarajaEspanola(String.valueOf((char) (i + 'A')));
        }
        _cantMaximaCartas = _mazo.size();
    }
    
    public int getCantMaximaCartas(){
        return _cantMaximaCartas;
    }
    
    public final void addBarajaEspanola(){
        addBarajaEspanola("");
    }
    
    public final void addBarajaEspanola(final String prefix){
        _mazo.add(new Carta(Carta.Valor.UNO, Carta.Palo.ESPADA, 14, prefix + "1E"));
        _mazo.add(new Carta(Carta.Valor.DOS, Carta.Palo.ESPADA, 9, prefix + "2E"));
        _mazo.add(new Carta(Carta.Valor.TRES, Carta.Palo.ESPADA, 10, prefix + "3E"));
        _mazo.add(new Carta(Carta.Valor.CUATRO, Carta.Palo.ESPADA, 1, prefix + "4E"));
        _mazo.add(new Carta(Carta.Valor.CINCO, Carta.Palo.ESPADA, 2, prefix + "5E"));
        _mazo.add(new Carta(Carta.Valor.SEIS, Carta.Palo.ESPADA, 3, prefix + "6E"));
        _mazo.add(new Carta(Carta.Valor.SIETE, Carta.Palo.ESPADA, 12, prefix + "7E"));
        _mazo.add(new Carta(Carta.Valor.DIEZ, Carta.Palo.ESPADA, 5, prefix + "10E"));
        _mazo.add(new Carta(Carta.Valor.ONCE, Carta.Palo.ESPADA, 6, prefix + "11E"));
        _mazo.add(new Carta(Carta.Valor.DOCE, Carta.Palo.ESPADA, 7, prefix + "12E"));
        
        _mazo.add(new Carta(Carta.Valor.UNO, Carta.Palo.BASTO, 13, prefix + "1B"));
        _mazo.add(new Carta(Carta.Valor.DOS, Carta.Palo.BASTO, 9, prefix + "2B"));
        _mazo.add(new Carta(Carta.Valor.TRES, Carta.Palo.BASTO, 10, prefix + "3B"));
        _mazo.add(new Carta(Carta.Valor.CUATRO, Carta.Palo.BASTO, 1, prefix + "4B"));
        _mazo.add(new Carta(Carta.Valor.CINCO, Carta.Palo.BASTO, 2, prefix + "5B"));
        _mazo.add(new Carta(Carta.Valor.SEIS, Carta.Palo.BASTO, 3, prefix + "6B"));
        _mazo.add(new Carta(Carta.Valor.SIETE, Carta.Palo.BASTO, 4, prefix + "7B"));
        _mazo.add(new Carta(Carta.Valor.DIEZ, Carta.Palo.BASTO, 5, prefix + "10B"));
        _mazo.add(new Carta(Carta.Valor.ONCE, Carta.Palo.BASTO, 6, prefix + "11B"));
        _mazo.add(new Carta(Carta.Valor.DOCE, Carta.Palo.BASTO, 7, prefix + "12B"));
        
        _mazo.add(new Carta(Carta.Valor.UNO, Carta.Palo.ORO, 8, prefix + "1O"));
        _mazo.add(new Carta(Carta.Valor.DOS, Carta.Palo.ORO, 9, prefix + "2O"));
        _mazo.add(new Carta(Carta.Valor.TRES, Carta.Palo.ORO, 10, prefix + "3O"));
        _mazo.add(new Carta(Carta.Valor.CUATRO, Carta.Palo.ORO, 1, prefix + "4O"));
        _mazo.add(new Carta(Carta.Valor.CINCO, Carta.Palo.ORO, 2, prefix + "5O"));
        _mazo.add(new Carta(Carta.Valor.SEIS, Carta.Palo.ORO, 3, prefix + "6O"));
        _mazo.add(new Carta(Carta.Valor.SIETE, Carta.Palo.ORO, 11, prefix + "7O"));
        _mazo.add(new Carta(Carta.Valor.DIEZ, Carta.Palo.ORO, 5, prefix + "10O"));
        _mazo.add(new Carta(Carta.Valor.ONCE, Carta.Palo.ORO, 6, prefix + "11O"));
        _mazo.add(new Carta(Carta.Valor.DOCE, Carta.Palo.ORO, 7, prefix + "12O"));
        
        _mazo.add(new Carta(Carta.Valor.UNO, Carta.Palo.COPA, 8, prefix + "1C"));
        _mazo.add(new Carta(Carta.Valor.DOS, Carta.Palo.COPA, 9, prefix + "2C"));
        _mazo.add(new Carta(Carta.Valor.TRES, Carta.Palo.COPA, 10, prefix + "3C"));
        _mazo.add(new Carta(Carta.Valor.CUATRO, Carta.Palo.COPA, 1, prefix + "4C"));
        _mazo.add(new Carta(Carta.Valor.CINCO, Carta.Palo.COPA, 2, prefix + "5C"));
        _mazo.add(new Carta(Carta.Valor.SEIS, Carta.Palo.COPA, 3, prefix + "6C"));
        _mazo.add(new Carta(Carta.Valor.SIETE, Carta.Palo.COPA, 4, prefix + "7C"));
        _mazo.add(new Carta(Carta.Valor.DIEZ, Carta.Palo.COPA, 5, prefix + "10C"));
        _mazo.add(new Carta(Carta.Valor.ONCE, Carta.Palo.COPA, 6, prefix + "11C"));
        _mazo.add(new Carta(Carta.Valor.DOCE, Carta.Palo.COPA, 7, prefix + "12C"));
    }
    
}
