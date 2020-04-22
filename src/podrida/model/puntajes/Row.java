package podrida.model.puntajes;

import java.util.ArrayList;
import java.util.List;

public class Row {

    private final List<Casilla> _casillas;

    public Row() {
        _casillas = new ArrayList<>();
    }

    public Row(final int rondaBazas, final int cantColumnas) {
        _casillas = new ArrayList<>();
        final Casilla casilla = new Casilla();
        casilla.setRondaBazas(rondaBazas);
        _casillas.add(casilla);
        for (int i = 0; i < cantColumnas; i++) {
            _casillas.add(new Casilla());
        }
    }

    public void addCasilla() {
        _casillas.add(new Casilla());
    }

    public void addCasilla(final Casilla casilla) {
        _casillas.add(casilla);
    }

    public List<Casilla> getCasillas() {
        return _casillas;
    }

}
