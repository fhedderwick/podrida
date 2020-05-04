package podrida.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import podrida.ClientThread;
import podrida.Partida;
import podrida.PrePartida;
import podrida.RequestBody;
import podrida.WebSocket;
import podrida.model.Carta;
import podrida.model.Espectador;
import podrida.utils.Utils;
import podrida.model.Instruccion;
import static podrida.model.Instruccion.EXPULSAR_JUGADOR;
import podrida.model.Jugador;
import podrida.model.User;

public class PodridaManager extends GameManager {

    private final List<ClientThread> _clientThreads;
    private final PrePartida _prePartida;
    private final UserManager _userManager;
    private final int _maxPlayers;
    private Partida _juego = null;

    public PodridaManager(final int maxPlayers, final UserManager userManager) {
        _clientThreads = new ArrayList<>();
        _maxPlayers = maxPlayers;
        _userManager = userManager;
        _prePartida = new PrePartida(maxPlayers);
    }

    public boolean existsName(final String nombre) {
        return _prePartida.nombreExiste(nombre);
    }

    public synchronized JsonObject processRequest(final ClientThread invoker, final String requestBody) {
        System.out.println("Request recibido: " + requestBody);
//        if(requestBody.length() > ){
//            return Utils.createJsonReply(false,Instruccion.INSTRUCCION_DESCONOCIDA,"Mensaje demasiado largo.");
//        }
        final RequestBody body = RequestBody.parseRequestBody(requestBody);
        if (body == null) {
            return Utils.createJsonReply(false, Instruccion.INSTRUCCION_DESCONOCIDA, "ERROR");
        }
        final String idSolicitante = body.getIdSolicitante();
        final Instruccion instruccion = body.getInstruccion();
        final String parametro = body.getParametro();

        final JsonObject reply;
        switch (instruccion) {
            case ENTRAR:
                final User user = _userManager.getLoggedUserByToken(idSolicitante);
                if (user == null) {
                    return Utils.createJsonReply(false, instruccion, "No se encuentra el usuario");
                }
                //el usuario logueado es asociado a este nuevo thread
                invoker.associateUser(user);
                if (hasStarted()) {
                    final Jugador jugador;
                    jugador = _juego.getJugadorByUsername(user.getUsername());
                    if(jugador == null){
                        System.out.println("No se encontro jugador (REGISTRADO) en esta partida que tuviera ese username");System.out.flush();
                        return Utils.createJsonReply(false, instruccion, "No se puede, el juego ya esta iniciado");
                    } else {
                        System.out.println("Se encontro jugador (REGISTRADO) en esta partida con ese username!");System.out.flush();
                    }
                    final JsonObject reconnectionData = reconexion(jugador, invoker);
                    if (reconnectionData != null) {
                        return Utils.createJsonReply(true, instruccion, reconnectionData);
                    }
                    return Utils.createJsonReply(false, instruccion, "No se pudo reconectar");
                }
//                aca deberia ver, porque los candidatos bajados se borran directamente
                final JsonObject jo = new JsonObject();
                final Jugador jugador = _prePartida.getCandidatoByUser(user);
                if (jugador == null) {
                    final Jugador nuevoJugador = _prePartida.agregarJugador(invoker);
                    if (nuevoJugador == null) {
                        return Utils.createJsonReply(false, instruccion, "No se puede agregar, ya se alcanzo el maximo de jugadores (." + _maxPlayers + ").");
                    }
                    jo.addProperty("username", nuevoJugador.getUsername());
                } else {
                    System.out.println("REEMPLAZANDO THREAD");
                    _prePartida.reemplazarThread(jugador, invoker);
                    jo.addProperty("username", jugador.getUsername());
                }
                invoker.setConnected(true);
                enviarEstadoPrejuego();
                return Utils.createJsonReply(true, instruccion, jo);
            case GET_JUGADORES:
                break;
            case CONFIGURAR_JUEGO:
                if (hasStarted()) {
                    return Utils.createJsonReply(false, instruccion, "No se puede, el juego ya esta iniciado");
                }
                reply = _prePartida.setGameParams(idSolicitante, parametro.trim());
                if (!reply.get("status").getAsBoolean()) {
                    return reply;
                }
                WebSocket.broadcast(_prePartida.getOnlineThreadsCandidatos(), reply);
                return null;
            case EMPEZAR:
                if (hasStarted()) {
                    return Utils.createJsonReply(false, instruccion, "No se puede, el juego ya esta iniciado");
                }
//                verificar que haya cantidad buena de jugadores!!!;
                String rta = _prePartida.pedirEmpezar(idSolicitante);
                if (!"OK".equals(rta)) {
                    return Utils.createJsonReply(false, instruccion, rta);
                }
                if (!_prePartida.solicitarInicio()) {
                    final JsonArray statusJugadores = createPlayerStatusArray();
                    WebSocket.broadcast(_prePartida.getOnlineThreadsCandidatos(), Utils.createJsonReply(true, Instruccion.GET_JUGADORES, statusJugadores.toString()));
                    return null;
                }
                _juego = _prePartida.iniciarJuego();
                final JsonObject datosInicio = new JsonObject();
                final JsonArray jugadores = new JsonArray();
                for (final Jugador j : _juego.getJugadores()) {
                    jugadores.add(j.getUsername());
                }
                datosInicio.add("jugadores", jugadores);
                datosInicio.addProperty("cantCartas", _juego.getCantMaximaCartas());
                datosInicio.addProperty("tablero", TableroManager.generarTablero(_juego.getJugadores()));
                WebSocket.broadcast(_prePartida.getOnlineThreadsCandidatos(), Utils.createJsonReply(true, instruccion, datosInicio));
                return null;
            case VER_TABLA:
                if (!hasStarted()) {
                    return Utils.createJsonReply(false, instruccion, "Aun no ha empezado la partida");
                }
                return Utils.createJsonReply(true, instruccion, _juego.getTabla());
            case TIRAR_CARTA:
                if (!hasStarted()) {
                    return Utils.createJsonReply(false, instruccion, "Aun no ha empezado la partida");
                }
                reply = _juego.tirar(idSolicitante, parametro);
                if (!reply.get("status").getAsBoolean()) {
                    return reply;
                }
                WebSocket.broadcast(_juego.getOnlineThreadsJugadoresYEspectadores(), reply);
                if(_juego.hasEnded()){
                    final Map results = _juego.getResults();
                    if(!results.isEmpty()){
                        if(!_userManager.writeStats(results)){
                            System.out.println("Se guardo la estadistica de esta partida");
                        } else {
                            System.out.println("Error al guardar la estadistica de esta partida");
                        }
                    } else {
                        System.out.println("No se encontraron datos para persistir");
                    }
                }
                return null;
            case ELEGIR_NUMERO:
                if (!hasStarted()) {
                    return Utils.createJsonReply(false, instruccion, "Aun no ha empezado la partida");
                }
                final int numeroElegido = Utils.parsearNumero(parametro);
                if (numeroElegido < 0) {
                    return Utils.createJsonReply(false, instruccion, "Numero invalido");
                }
                reply = _juego.elegir(idSolicitante, numeroElegido);
                if (!reply.get("status").getAsBoolean()) {
                    return reply;
                }
                WebSocket.broadcast(_juego.getOnlineThreadsJugadoresYEspectadores(), reply);
                return null;
            case REPARTIR:
                if (!hasStarted()) {
                    return Utils.createJsonReply(false, instruccion, "Aun no ha empezado la partida");
                }
                reply = _juego.repartir(idSolicitante);
                if (!reply.get("status").getAsBoolean()) {
                    return reply;
                }
                if (!_juego.esRondaIndia()) {
                    WebSocket.broadcastCartas(_juego.getJugadores(), reply);
                } else {
                    WebSocket.broadcastCartasIndias(_juego.getJugadores(), reply);
                }
                WebSocket.broadcastToSpectators(_juego.getBazasQueDebenJugarseEstaRonda(),_juego.getEspectadores(), reply);
                return null;
            case MENSAJE_ESTANDAR:
                if (!hasStarted()) {
                    return Utils.createJsonReply(false, instruccion, "Aun no ha empezado la partida");
                }
                reply = _juego.decirMensajeEstandar(idSolicitante, parametro);
                if (!reply.get("status").getAsBoolean()) {
                    return reply;
                }
                WebSocket.broadcast(_juego.getOnlineThreadsJugadoresYEspectadores(), reply);
                return null;
            case VER_ESTADISTICAS:
                reply = Utils.createJsonReply(true, instruccion, "contenido del leaderboard");
                if (!reply.get("status").getAsBoolean()) {
                    return Utils.createJsonReply(false, instruccion, "Error al obtener las estadisticas");
                }
                return reply;
            case VER_ESTADISTICAS_USUARIO:
                if (!hasStarted()) {
                    return Utils.createJsonReply(false, instruccion, "Aun no ha empezado la partida");
                }
                final JsonArray estadisticasUsuario = _userManager.getEstadisticasUsuariosAsHtmlTable(parametro);
                if(estadisticasUsuario == null){
                    return Utils.createJsonReply(false, instruccion, "El jugador solicitado no esta registrado o no esta jugando este partido");
                }
                return Utils.createJsonReply(true, instruccion, estadisticasUsuario);
            case ENVIAR_MENSAJE:
                if (!hasStarted()) {
                    return Utils.createJsonReply(false, instruccion, "Aun no ha empezado la partida");
                }
                reply = _juego.enviarMensaje(idSolicitante, parametro);
                if (!reply.get("status").getAsBoolean()) {
                    return reply;
                }
                WebSocket.broadcast(_juego.getOnlineThreadsJugadoresYEspectadores(), reply);
                return null;
            case REGISTRAR_ESPECTADOR:
                if (!hasStarted()) {
                    return Utils.createJsonReply(false, instruccion, "Aun no ha empezado la partida");
                }
                reply = _juego.registrarEspectador(new Espectador(invoker));
//                if (!reply.get("status").getAsBoolean()) {
//                    return reply;
//                }
                invoker.setConnected(true);
                WebSocket.broadcast(_juego.getOnlineThreadsJugadoresYEspectadores(), reply);
                final JsonObject gameData = crearDatosDeJuego();
                if (gameData != null) {
                    return Utils.createJsonReply(true, instruccion, gameData);
                }
                return Utils.createJsonReply(false, instruccion, "No se pudo entrar");
            case HEARTBEAT:
                if(invoker.isConnected()){
                    return Utils.createJsonReply(true, instruccion, "");
                }
                return null;
            case EXPULSAR_JUGADOR:
//                getJugadorByNombre();
//                if(jugador == null){
//                    return Utils.createJsonReply(false, instruccion, "No se puede echar a un jugador con conexion activa!");
//                }
//                A ese jugador se le considera que pidio 0 bazas, que sus cartas son todos peores que un 4, y no tiene turno
//                Se considera que suma siempre 0 puntos, y se le considera abandonador
//                Broadcast 91
//                return null;
                return Utils.createJsonReply(false, instruccion, "Aun no esta implementado");
            case INSTRUCCION_DESCONOCIDA:
            default:
        };
        return Utils.createJsonReply(false, Instruccion.INSTRUCCION_DESCONOCIDA, "Pedido no reconocido");
    }

    private JsonArray createPlayerStatusArray() {
        final JsonArray estadoJugadores = new JsonArray();
        for (final Jugador candidato : _prePartida.getCandidatos()) {
            final JsonObject jug = new JsonObject();
            jug.addProperty("name", candidato.getUsername());
            jug.addProperty("status", candidato.getQuieroEmpezar());
            estadoJugadores.add(jug);
        }
        return estadoJugadores;
    }

    private boolean hasStarted() {
        return _juego != null;
    }

    private void enviarEstadoPrejuego() {
        final JsonArray estadoJugadores = createPlayerStatusArray();
        final JsonObject broadcastNewPlayers = Utils.createJsonReply(true, Instruccion.GET_JUGADORES, estadoJugadores.toString());
        WebSocket.broadcast(_prePartida.getOnlineThreadsCandidatos(), broadcastNewPlayers);
        final JsonObject broadcastConfiguration = Utils.createJsonReply(true, Instruccion.CONFIGURAR_JUEGO, _prePartida.getMensajeConfiguracion());
        WebSocket.broadcast(_prePartida.getOnlineThreadsCandidatos(), broadcastConfiguration);
    }

    public void desconexion(final ClientThread clientThread) {
        clientThread.setConnected(false);
        if (!hasStarted()) {
            _prePartida.quitarCandidato(clientThread);
            enviarEstadoPrejuego();
        } else {
            final Jugador jugador = _juego.getJugadorByClientThread(clientThread);
            if (jugador != null) {
                final JsonObject data = new JsonObject();
                data.addProperty("conexion", false);
                data.addProperty("nombre", jugador.getUsername());
                final JsonObject mensaje = Utils.createJsonReply(true, Instruccion.INFORMAR_CAMBIO_CONEXION, data);
//                _juego.broadcast
                WebSocket.broadcast(_juego.getOnlineThreadsJugadoresYEspectadores(), mensaje);
            } else {
                final Espectador espectador = _juego.getEspectadorByClientThread(clientThread);
                if(espectador != null){
                    WebSocket.broadcast(_juego.getOnlineThreadsJugadoresYEspectadores(), _juego.quitarEspectador(espectador));;
                }
            }
        }
    }

    private JsonObject reconexion(final Jugador jugador, final ClientThread clientThread) {
        _juego.reemplazarThread(jugador, clientThread);
        final JsonObject data = new JsonObject();
        data.addProperty("conexion", true);
        data.addProperty("nombre", jugador.getUsername());
        final JsonObject mensaje = Utils.createJsonReply(true, Instruccion.INFORMAR_CAMBIO_CONEXION, data);
        WebSocket.broadcast(_juego.getOnlineThreadsJugadoresYEspectadores(), mensaje);
        final JsonObject reply = new JsonObject();
        reply.add("datos", crearDatosDeJuego(jugador));
        return reply;
    }

    private JsonObject crearDatosDeJuego(final Jugador jugadorReconectado) {
        final JsonObject gameData = new JsonObject();
        Utils.mergeJsonObjects(gameData,crearDatosDeJuego());
        gameData.addProperty("username", jugadorReconectado.getUsername());
        gameData.addProperty("userId", jugadorReconectado.getUserToken());
        final JsonArray cartasEnMano = jugadorReconectado.peekCartas();
        for (int i = cartasEnMano.size(); i < _juego.getBazasQueDebenJugarseEstaRonda(); i++) {
            cartasEnMano.add("blank"); //completo con cartas blancas las que ya tiro este jugador
        }
        gameData.add("cartas", cartasEnMano); //piso las cartas de espectador
        return gameData;
    }
    
    private JsonArray crearDatosCartasEspectador() {
        final JsonArray cartasEnMano = new JsonArray();
        for (int i = 0; i < _juego.getBazasQueDebenJugarseEstaRonda(); i++) {
            cartasEnMano.add("unknown");
        }
        return cartasEnMano;
    }
    
    private JsonObject crearDatosDeJuego() {
        final JsonObject gameData = new JsonObject();
        final JsonArray datosJugadores = new JsonArray();
        int pedidas = 0;
        for (final Jugador j : _juego.getJugadores()) {
            final JsonObject datosJugador = new JsonObject();
            datosJugador.addProperty("nombre", j.getUsername());
            final Carta cartaJugada = j.peekCartaJugada();
            datosJugador.addProperty("cartaJugada", cartaJugada == null ? "blank" : cartaJugada.getId());
            datosJugador.addProperty("bazasGanadas", j.getBazasGanadas());
            final int pedidasJugador = j.getPedido();
            datosJugador.addProperty("bazasPedidas", pedidasJugador);
            if (pedidasJugador != -1) {
                pedidas += pedidasJugador;
            }
            datosJugadores.add(datosJugador);
        }
        gameData.add("datosJugadores", datosJugadores);
        gameData.addProperty("tablero", TableroManager.generarTablero(_juego.getJugadores()));
        gameData.addProperty("turno", _juego.getTurno());
        gameData.addProperty("cantCartas", _juego.getCantMaximaCartas());
        gameData.addProperty("pedidas", pedidas);
        gameData.add("cartas", crearDatosCartasEspectador());
        return gameData;
    }

    @Override
    public void registerThread(final ClientThread clientThread) {
        _clientThreads.add(clientThread);
        clientThread.start();
    }

    public void disconectAll() {
        for(final ClientThread ct : _clientThreads){
            ct.setConnected(false);
        }
        _clientThreads.clear();
    }

}
