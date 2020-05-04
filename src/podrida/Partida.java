package podrida;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import podrida.model.puntajes.Tabla;
import podrida.model.Jugador;
import podrida.model.Mazo;
import podrida.model.Carta;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import podrida.model.Baza;
import podrida.model.Espectador;
import podrida.model.Instruccion;
import podrida.model.estadistica.EstadisticaJugadorPartido;
import podrida.utils.MensajesEstandar;
import podrida.utils.Utils;

public class Partida {

    private final String _idPartida;
    private final List<Jugador> _jugadores;
    private final Map<String,EstadisticaJugadorPartido> _estadisticasPartido;
    private final List<Espectador> _espectadores;
    private final Configuracion _configuracion;
    private final Mazo _mazo;
    private final Tabla _tabla;
    private boolean _momentoElegir;
    private boolean _momentoTirar;
    private boolean _momentoRepartir;
    private boolean _ended;
    private int _jugadorTurno;
    private int _jugadorManoInicial;
    private int _cartasJugadas = 0;
    private int _numerosElegidos = 0;
    private int _bazasJugadas = 0;
    private int _bazasQueDebenJugarseEstaRonda = 2;
    private int _bazasDeLaRondaMaxima;
    private int _sumaElegidos = 0;
    private Long _time = System.currentTimeMillis();

    public Partida(final List<Jugador> candidatos, final Configuracion configuracion) {
        _idPartida = UUID.randomUUID().toString();
        _jugadores = loadJugadores(candidatos,_idPartida);
        _estadisticasPartido = new HashMap<>();
        _espectadores = new ArrayList<>();
        _configuracion = configuracion;
        _jugadorTurno = 0;
        _mazo = new Mazo(configuracion.getCantBarajas());
        final int cantBazasMaximaForzada = _configuracion.getCantBazasMaximaForzada();
        final int cantBazasMaximaCalculada = _mazo.getCantMaximaCartas()/ _jugadores.size();
        if(cantBazasMaximaForzada < 3 || cantBazasMaximaForzada > cantBazasMaximaCalculada){
            _bazasDeLaRondaMaxima = cantBazasMaximaCalculada;
        } else {
            _bazasDeLaRondaMaxima = cantBazasMaximaForzada;
        }
        _tabla = new Tabla(_bazasDeLaRondaMaxima,_jugadores);
        _momentoRepartir = true;
        _momentoTirar = false;
        _momentoElegir = false;
        _ended = false;
    }

    public JsonObject repartir(final String idJugadorRepartidor) {
        final Jugador jugadorRepartidor = getJugador(idJugadorRepartidor);
        if(jugadorRepartidor == null){
            return Utils.createJsonReply(false,Instruccion.REPARTIR,"Jugador desconocido");
        }
        if (esElTurno(jugadorRepartidor)){
            if(_momentoRepartir) {
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
                return Utils.createJsonReply(true,Instruccion.REPARTIR,String.valueOf(_jugadorTurno));
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
                if(waitTimeFinished()) {
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
                                _ended = true;
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
                            startWaitTime();
                            return Utils.createJsonReply(true,Instruccion.TIRAR_CARTA,reply);
                        }
                        //baza terminada, mostrar quien se la llevo
                        reply.addProperty("ganadorBaza", ganadorBaza);
                        reply.addProperty("bazasActuales", _jugadores.get(ganadorBaza).getBazasGanadas());
                        reply.addProperty("jugador", jugadorTiradorIndex);
                        reply.addProperty("cartaJugada", carta.getId());
                        reply.addProperty("turnoActual", _jugadorTurno);
                        startWaitTime();
                        return Utils.createJsonReply(true,Instruccion.TIRAR_CARTA,reply);
                    }
                    //carta jugada, pasar turno al siguiente
                    reply.addProperty("jugador", jugadorTiradorIndex);
                    reply.addProperty("cartaJugada", carta.getId());
                    reply.addProperty("turnoActual", _jugadorTurno);
                    return Utils.createJsonReply(true,Instruccion.TIRAR_CARTA,reply);
                }
                return Utils.createJsonReply(false,Instruccion.TIRAR_CARTA,"Esperar a que se limpien las cartas de la ronda anterior");
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
        final String sender;
        if(jugadorPeticionante == null){
            sender = "Espectador";
//            return Utils.createJsonReply(false,Instruccion.MENSAJE_ESTANDAR,"Jugador desconocido");
        } else {
            sender = jugadorPeticionante.getUsername();
        }
        final String mensaje = MensajesEstandar.get(parametro);
        if(mensaje == null){
            return Utils.createJsonReply(false,Instruccion.MENSAJE_ESTANDAR,"No se encontro el mensaje solicitado");
        }
        return Utils.createJsonReply(true, Instruccion.MENSAJE_ESTANDAR, sender + ": \"" + mensaje + "\"");
    }
    
    public JsonObject enviarMensaje(final String idJugadorElector, final String mensaje) {
        final Jugador jugadorPeticionante = getJugador(idJugadorElector);
        final String sender;
        if(jugadorPeticionante == null){
            sender = "Espectador";
//            return Utils.createJsonReply(false,Instruccion.ENVIAR_MENSAJE,"Jugador desconocido");
        } else {
            sender = jugadorPeticionante.getUsername();
        }
        if(Utils.containsStrangeCharacters(mensaje.replaceAll(" ", "").replaceAll("\\?", ""))){
            return Utils.createJsonReply(false,Instruccion.ENVIAR_MENSAJE,"Mensaje no enviable");
        }
        return Utils.createJsonReply(true, Instruccion.ENVIAR_MENSAJE, sender + ": " + mensaje);
    }
    
    public JsonObject registrarEspectador(final Espectador espectador) {
        System.out.println("Registrado espectador en " + espectador.getRemoteAddress());
        _espectadores.add(espectador);
        return Utils.createJsonReply(true, Instruccion.MENSAJE_ESTANDAR, "Ha entrado un espectador! Ahora son " + _espectadores.size() + " espectadores.");
    }
    
    public JsonObject quitarEspectador(final Espectador espectador){
        _espectadores.remove(espectador);
        return Utils.createJsonReply(true, Instruccion.MENSAJE_ESTANDAR, "Se ha ido un espectador! Ahora son " + _espectadores.size() + " espectadores.");
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
            if(jugador.getUserToken().equals(idJugador)){
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

    public Jugador getJugadorByUsername(final String username) {
        for(final Jugador jugador : _jugadores){
            if(jugador.getUsername().equals(username)){
                return jugador;
            }
        }
        return null;
    }
    
    public Jugador getJugadorByClientThread(final ClientThread clientThread) {
        for(final Jugador jugador : _jugadores){
            if(jugador.getClientThread() == clientThread){
                return jugador;
            }
        }
        return null;
    }
    
    public void reemplazarThread(final Jugador jugador, final ClientThread invoker) {
        jugador.replaceClientThread(invoker);
    }
    
    public List<ClientThread> getOnlineThreadsJugadores(){
        final List<ClientThread> clientThreads = new ArrayList<>();
        for(final Jugador jugador : _jugadores){
            final ClientThread jugadorThread = jugador.getClientThread();
            if(jugadorThread != null && jugadorThread.isConnected()){
                clientThreads.add(jugadorThread);
            }
        }
        return clientThreads;
    }
    
    public List<ClientThread> getOnlineThreadsEspectadores(){
        final List<ClientThread> clientThreads = new ArrayList<>();
        for(final Espectador espectador : _espectadores){
            final ClientThread espectadorThread = espectador.getClientThread();
            if(espectadorThread != null && espectadorThread.isConnected()){
                clientThreads.add(espectadorThread);
            }
        }
        return clientThreads;
    }
    
    public List<ClientThread> getOnlineThreadsJugadoresYEspectadores(){
        final List<ClientThread> clientThreads = getOnlineThreadsJugadores();
        clientThreads.addAll(getOnlineThreadsEspectadores());
        return clientThreads;
    }
    
    public Espectador getEspectadorByClientThread(final ClientThread clientThread) {
        for(final Espectador espectador : _espectadores){
            if(espectador.getClientThread() == clientThread){
                return espectador;
            }
        }
        return null;
    }

    public List<Espectador> getEspectadores() {
        return _espectadores;
    }

    private List<Jugador> loadJugadores(final List<Jugador> candidatos, final String _idPartida) {
        final List<Jugador> jugadores = new ArrayList<>();
        for(final Jugador candidato : candidatos){
            jugadores.add(candidato);
            candidato.setIdPartidoActual(_idPartida);
        }
        Collections.sort(jugadores);
        return jugadores;
    }

    public boolean hasEnded() {
        return _ended;
    }

    public final Map<String,EstadisticaJugadorPartido> getResults() {
        final Map<String,EstadisticaJugadorPartido> results = new HashMap<>();
        final JsonObject puntajes = _tabla.getResultadosParaGuardar();
        if(puntajes == null){
            return results;
        }
        for(final Map.Entry<String, JsonElement> entry : puntajes.entrySet()){
//            results.put(jugador.getUsername(),calculateValues(jugador));
            System.out.println(entry.toString());
            final JsonObject jo = entry.getValue().getAsJsonObject();
            final int puntaje = jo.get("puntaje").getAsInt();
            final int puesto = jo.get("puesto").getAsInt();
            final List<Baza> bazas = crearListaBazas(jo.get("historia").getAsJsonArray());
            final EstadisticaJugadorPartido ejp = new EstadisticaJugadorPartido
            (_idPartida,_jugadores.size(),_configuracion.getCantBarajas(),_bazasJugadas,puntaje,puesto,bazas);
            results.put(entry.getKey(), ejp);
        }
        return results;
    }
    
    private List<Baza> crearListaBazas(final JsonArray historia){
        final List<Baza> lista = new ArrayList<>();
        for(int i=0; i<historia.size(); i+=2){
            lista.add(new Baza(historia.get(i).getAsInt(),historia.get(i+1).getAsInt()));
        }
        return lista;
    }
    
    private void startWaitTime(){
        _time = System.currentTimeMillis();
    }
    
    private boolean waitTimeFinished(){
        return System.currentTimeMillis() - _time > 2500;
    }
    
}
