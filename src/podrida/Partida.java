package podrida;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import podrida.model.puntajes.Tabla;
import podrida.model.Jugador;
import podrida.model.Mazo;
import podrida.model.Carta;
import java.util.List;
import podrida.model.Instruccion;
import podrida.utils.MensajesEstandar;
import podrida.utils.Utils;

public class Partida {

    private final List<Jugador> _jugadores;
    private final Configuracion _configuracion;
    private final Mazo _mazo;
    private final Tabla _tabla;
    private boolean _momentoElegir;
    private boolean _momentoTirar;
    private boolean _momentoRepartir;
    private int _jugadorTurno;
    private int _jugadorManoInicial;
    private int _cartasJugadas = 0;
    private int _numerosElegidos = 0;
    private int _bazasJugadas = 0;
    private int _bazasQueDebenJugarseEstaRonda = 2;
    private int _bazasDeLaRondaMaxima;
    private int _sumaElegidos = 0;

    public Partida(final List<Jugador> jugadores, final Configuracion configuracion) {
        _jugadores = jugadores;
        _configuracion = configuracion;
        _jugadorTurno = 0;
        _mazo = new Mazo(configuracion.getCantBarajas());
        _bazasDeLaRondaMaxima = _mazo.getCantMaximaCartas()/ _jugadores.size();
        _tabla = new Tabla(_bazasDeLaRondaMaxima,_jugadores);
        _momentoRepartir = true;
        _momentoTirar = false;
        _momentoElegir = false;
        
    }

    public JsonObject repartir(final String idJugadorRepartidor) {
        final Jugador jugadorRepartidor = getJugador(idJugadorRepartidor);
        if(jugadorRepartidor == null){
            return Utils.createJsonReply(false,Instruccion.REPARTIR,"Jugador desconocido");
        }
        if (esElTurno(jugadorRepartidor)){
            if(_momentoRepartir) {
                final JsonObject reply = new JsonObject();
                if (_bazasQueDebenJugarseEstaRonda < _bazasDeLaRondaMaxima) {
                    _bazasQueDebenJugarseEstaRonda++;
                } else {
                    _bazasDeLaRondaMaxima = -1;
                    _bazasQueDebenJugarseEstaRonda--;
                }
                pasarTurno();
                _jugadorManoInicial = _jugadorTurno;
                _cartasJugadas = 0;
                _numerosElegidos = 0;
                _sumaElegidos = 0;
                _bazasJugadas = 0;
                for (final Jugador jugador : _jugadores) {
                    jugador.vaciarBazasGanadas();
                    jugador.setNumeroElegido(-1);
                }
                for (final Jugador jugador : _jugadores) {
                    jugador.recibirCartas(_mazo.getCartas(_bazasQueDebenJugarseEstaRonda));
                }
                _momentoRepartir = false;
                _momentoElegir = true;
//                //BORRAR DESDE AQUI
//                _momentoElegir = false;
//                _momentoTirar = true;
//                //HASTA AQUI
                reply.addProperty("turnoActual", _jugadorTurno);
                return Utils.createJsonReply(true,Instruccion.REPARTIR,reply);
            }
            return Utils.createJsonReply(false,Instruccion.REPARTIR,"No es momento de repartir");
        }
        return Utils.createJsonReply(false,Instruccion.REPARTIR,"No es tu turno");
    }

    public JsonObject tirar(final String idJugadorTirador, final String parametro) {
        final Jugador jugadorTirador = getJugador(idJugadorTirador);
        if(jugadorTirador == null){
            return Utils.createJsonReply(false,Instruccion.TIRAR_CARTA,"Jugador desconocido");
        }
        if (esElTurno(jugadorTirador)) {
            if (_momentoTirar) {
                final JsonObject reply = new JsonObject();
                final Carta carta;
                if(!"unknown".equals(parametro)){
                    carta = jugadorTirador.getCarta(parametro);
                } else {
                    carta = jugadorTirador.getCartaIndia();
                }
                if(carta == null){
                    return Utils.createJsonReply(false,Instruccion.TIRAR_CARTA,"No tenes esa carta");
                }
                jugadorTirador.setCartaJugada(carta);
                _cartasJugadas++;
                final int jugadorTiradorIndex = _jugadorTurno;
                pasarTurno();
                if (_cartasJugadas == _jugadores.size()) {
                    _cartasJugadas = 0;
                    //en este momento el turno lo tiene el que tiro primero en esta baza
                    int ganadorBaza = calcularGanadorBaza();
                    _jugadores.get(ganadorBaza).sumarBaza();
                    _jugadorTurno = ganadorBaza;
                    _bazasJugadas++;
                    if (_bazasJugadas == _bazasQueDebenJugarseEstaRonda) {
                        final JsonObject puntajesRonda = _tabla.calcularPuntajesRonda(_jugadores);
                        if (_bazasQueDebenJugarseEstaRonda == 1) {
                            reply.addProperty("ganadorBaza", ganadorBaza);
                            reply.addProperty("bazasActuales", _jugadores.get(ganadorBaza).getBazasGanadas());
                            reply.addProperty("jugador", jugadorTiradorIndex);
                            reply.addProperty("cartaJugada", carta.getId());
                            reply.add("puntajes", puntajesRonda);
                            reply.addProperty("end", true);
                            reply.addProperty("turnoActual", -1);
                            _momentoRepartir = false;
                            _momentoTirar = false;
                            _momentoElegir = false;
                            return Utils.createJsonReply(true,Instruccion.TIRAR_CARTA,reply);
                        }
                        _momentoRepartir = true;
                        _momentoTirar = false;
                        _jugadorTurno = _jugadorManoInicial; //turno del que reparte
                        reply.addProperty("ganadorBaza", ganadorBaza);
                        reply.addProperty("bazasActuales", _jugadores.get(ganadorBaza).getBazasGanadas());
                        reply.addProperty("jugador", jugadorTiradorIndex);
                        reply.addProperty("cartaJugada", carta.getId());
                        reply.add("puntajes", puntajesRonda);
                        reply.addProperty("turnoActual", _jugadorTurno);
                        return Utils.createJsonReply(true,Instruccion.TIRAR_CARTA,reply);
                    }
                    //baza terminada, mostrar quien se la llevo
                    reply.addProperty("ganadorBaza", ganadorBaza);
                    reply.addProperty("bazasActuales", _jugadores.get(ganadorBaza).getBazasGanadas());
                    reply.addProperty("jugador", jugadorTiradorIndex);
                    reply.addProperty("cartaJugada", carta.getId());
                    reply.addProperty("turnoActual", _jugadorTurno);
                    return Utils.createJsonReply(true,Instruccion.TIRAR_CARTA,reply);
                }
                //carta jugada, pasar turno al siguiente
                reply.addProperty("jugador", jugadorTiradorIndex);
                reply.addProperty("cartaJugada", carta.getId());
                reply.addProperty("turnoActual", _jugadorTurno);
                return Utils.createJsonReply(true,Instruccion.TIRAR_CARTA,reply);
            }
            return Utils.createJsonReply(false,Instruccion.TIRAR_CARTA,"No es momento de tirar");
        }
        return Utils.createJsonReply(false,Instruccion.TIRAR_CARTA,"No es tu turno");
    }

    public JsonObject elegir(final String idJugadorElector, final int numeroElegido) {
        final Jugador jugadorElector = getJugador(idJugadorElector);
        if(jugadorElector == null){
            return Utils.createJsonReply(false,Instruccion.ELEGIR_NUMERO,"Jugador desconocido");
        }
        if (esElTurno(jugadorElector)) {
            if (_momentoElegir) {
                final JsonObject reply = new JsonObject();
                if (numeroValido(numeroElegido)) {
                    reply.addProperty("jugador",_jugadorTurno);
                    pasarTurno();
                    _numerosElegidos++;
                    jugadorElector.setNumeroElegido(numeroElegido);
                    _sumaElegidos += numeroElegido;
                    reply.addProperty("numero",numeroElegido);
                    reply.addProperty("turnoActual",_jugadorTurno);
                    final int numeroNoValido = getNumeroNoValido();
                    if (_numerosElegidos == _jugadores.size() - 1 && numeroNoValido >= 0) {
                        reply.addProperty("numeroNoValido",numeroNoValido);
                        return Utils.createJsonReply(true,Instruccion.ELEGIR_NUMERO,reply);
                    } else if (_numerosElegidos == _jugadores.size()) {
                        _momentoElegir = false;
                        _momentoTirar = true;
                        reply.addProperty("mensaje",crearMensajeDesviacion());
                        return Utils.createJsonReply(true,Instruccion.ELEGIR_NUMERO,reply);
                    }
                    return Utils.createJsonReply(true,Instruccion.ELEGIR_NUMERO,reply);
                }
                return Utils.createJsonReply(false,Instruccion.ELEGIR_NUMERO,"No se puede elgir ese numero");
            }
            return Utils.createJsonReply(false,Instruccion.ELEGIR_NUMERO,"No es momento de elegir numero");
        }
        return Utils.createJsonReply(false,Instruccion.ELEGIR_NUMERO,"No es tu turno");
    }
    
    public JsonObject decirMensajeEstandar(final String idJugadorPeticionante, final String parametro) {
        final Jugador jugadorPeticionante = getJugador(idJugadorPeticionante);
        if(jugadorPeticionante == null){
            return Utils.createJsonReply(false,Instruccion.MENSAJE_ESTANDAR,"Jugador desconocido");
        }
        final String mensaje = MensajesEstandar.get(parametro);
        if(mensaje == null){
            return Utils.createJsonReply(false,Instruccion.MENSAJE_ESTANDAR,"No se encontro el mensaje solicitado");
        }
        return Utils.createJsonReply(true, Instruccion.MENSAJE_ESTANDAR, jugadorPeticionante.getNombre() + ": \"" + mensaje + "\"");
    }
    
    public JsonObject enviarMensaje(final String idJugadorElector, final String mensaje) {
        final Jugador jugadorElector = getJugador(idJugadorElector);
        if(jugadorElector == null){
            return Utils.createJsonReply(false,Instruccion.ENVIAR_MENSAJE,"Jugador desconocido");
        }
        if(Utils.containsStrangeCharacters(mensaje.replaceAll(" ", "").replaceAll("\\?", ""))){
            return Utils.createJsonReply(false,Instruccion.ENVIAR_MENSAJE,"Mensaje no enviable");
        }
        return Utils.createJsonReply(true, Instruccion.ENVIAR_MENSAJE, jugadorElector.getNombre() + ": " + mensaje);
    }

    private boolean esElTurno(final Jugador jugador) {
        return jugador == _jugadores.get(_jugadorTurno);
    }

    private boolean numeroValido(final int numeroElegido) {
        if(numeroElegido > _bazasQueDebenJugarseEstaRonda){
            return false;
        }
        if(_numerosElegidos == _jugadores.size()-1){
            return _sumaElegidos + numeroElegido != _bazasQueDebenJugarseEstaRonda;
        }
        return true;
    }

    private int calcularGanadorBaza() {
        int ganadorPorElMomento = -1;
        Carta cartaGanadoraPorElMomento = null;
        for (int i = 0, j=_jugadorTurno; i < _jugadores.size() ; i++) {
            final Jugador jugador = _jugadores.get(j);
            final Carta carta = jugador.getCartaJugada();
            _mazo.devolverAlMazo(carta);
            if(carta.superaA(cartaGanadoraPorElMomento)){
                ganadorPorElMomento = j;
                cartaGanadoraPorElMomento = carta;
            }
            j = (j + 1) % _jugadores.size();
        }
        return ganadorPorElMomento;
    }

    public JsonArray getTabla(){
        return _tabla.getPuntajes();
    }

    private Jugador getJugador(final String idJugador) {
        for(final Jugador jugador : _jugadores){
            if(jugador.getId().equals(idJugador)){
                return jugador;
            }
        }
        return null;
    }

    public List<Jugador> getJugadores() {
        return _jugadores;
    }

    private int getNumeroNoValido() {
        return _bazasQueDebenJugarseEstaRonda - _sumaElegidos;
    }
    
    private void pasarTurno(){
//        if(true) return;
        _jugadorTurno = (_jugadorTurno + 1) % _jugadores.size();
    }

    private String crearMensajeDesviacion() {
        if(_bazasQueDebenJugarseEstaRonda > _sumaElegidos){
            if(_bazasQueDebenJugarseEstaRonda - _sumaElegidos == 1){
                return "Hay uno que se pasa!";
            }
            return "Se pasan " + (_bazasQueDebenJugarseEstaRonda - _sumaElegidos) + "!";
        } else {
             if(_sumaElegidos -_bazasQueDebenJugarseEstaRonda  == 1){
                return "Hay uno que no llega!";
            }
            return "Hay " + (_sumaElegidos - _bazasQueDebenJugarseEstaRonda) + " que no llegan!";
        }
    }

    public int getTurno() {
        return _jugadorTurno;
    }
    
    public int getBazasQueDebenJugarseEstaRonda(){
        return _bazasQueDebenJugarseEstaRonda;
    }

    public boolean esRondaIndia() {
        return _bazasQueDebenJugarseEstaRonda == 1 && _configuracion.isUltimaRondaIndia();
    }

    public int getCantMaximaCartas() {
        return _mazo.getCantMaximaCartas();
    }

}
