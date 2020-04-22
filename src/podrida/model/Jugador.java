package podrida.model;

import com.google.gson.JsonArray;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Jugador {

    private final String _nombre;
    private final String _id;
    private final List<Carta> _cartas;
    private boolean _quieroEmpezar = false;
    
    private int _bazasGanadas = 0;
    
    private Carta _cartaJugada = null;
    private int _numeroElegido;
    
    public Jugador(final String nombre) {
        _nombre = nombre;
        _id = UUID.randomUUID().toString();
        _cartas = new ArrayList<>();
    }

    public List<Carta> verCartas(){
        return _cartas;
    }
    
    public void setNumeroElegido(final int numero){
        _numeroElegido = numero;
    }
    
    public Carta peekCartaJugada(){
        return _cartaJugada;
    }
    
    public Carta getCartaJugada(){
        final Carta temp = _cartaJugada;
        _cartaJugada = null;
        return temp;
    }
    
    public String getNombre(){
        return _nombre;
    }
    
    public void sumarBaza() {
        _bazasGanadas++;
    }
    
    public void vaciarBazasGanadas() {
        _bazasGanadas = 0;
    }

    public void setCartaJugada(final Carta carta) {
        _cartaJugada = carta;
    }
    
    public int getPedido(){
        return _numeroElegido;
    }
    
    public int puntuar(){
        int puntaje = 0;
        if(_bazasGanadas == _numeroElegido){
            puntaje += 10;
            puntaje += _bazasGanadas;
        } else {
            puntaje -= Math.abs(_bazasGanadas - _numeroElegido);
        }
        return puntaje;
    }
    
    public int getBazasGanadas(){
        return _bazasGanadas;
    }

    public void recibirCartas(final List<Carta> cartas) {
        _cartas.addAll(cartas);
    }

    public boolean pedirEmpezar() {
        if(_quieroEmpezar){
            return false;
        }
        _quieroEmpezar = true;
        return true;
    }

    public boolean getQuieroEmpezar() {
        return _quieroEmpezar;
    }

    public Carta getCarta(final String idCarta) {
        int index = 0;
        for(final Carta cartaJugador : _cartas){
            if(cartaJugador.getId().equals(idCarta)){
                return _cartas.remove(index);
            }
            index++;
        }
        return null;
    }
    
    public Carta getCartaIndia() {
        return _cartas.isEmpty() ? null : _cartas.remove(0);
    }

    public String getId() {
        return _id;
    }
    
    public static List<Jugador> mockJugadores(final int cant) {
        final List<Jugador> jugadores = new ArrayList<>();
        for(int i=0; i<cant; i++){
            final Jugador jugador = new Jugador("nombre_"+i);
            jugador.pedirEmpezar();
            jugadores.add(jugador);
        }
        return jugadores;
    }

    public JsonArray peekCartas() {
        final JsonArray ja = new JsonArray();
        for(final Carta carta : _cartas){
            ja.add(carta.getId());
        }
        return ja;
    }

    public void setQuieroEmpezar(final boolean b) {
        _quieroEmpezar = b;
    }
    
}
