package podrida;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import podrida.model.Carta;
import podrida.utils.Utils;
import podrida.model.Instruccion;
import podrida.model.Jugador;

public class GameManager {

    private final PrePartida _prePartida;
    private final int _maxPlayers;
    private Partida _juego = null;
    private final Map<String,Jugador> _disconnectedPlayers;
    
    public GameManager(final int maxPlayers){
        _maxPlayers = maxPlayers;
        _prePartida = new PrePartida(maxPlayers);
        _disconnectedPlayers = new HashMap<>();
    }
    
    public boolean existsName(final String nombre){
        return _prePartida.nombreExiste(nombre);
    }
    
    public synchronized JsonObject processRequest(final ClientThread invoker, final String requestBody) {
        System.out.println("Request recibido: " + requestBody);
//        if(requestBody.length() > ){
//            return Utils.createJsonReply(false,Instruccion.INSTRUCCION_DESCONOCIDA,"Mensaje demasiado largo.");
//        }
        final RequestBody body = RequestBody.parseRequestBody(requestBody);
        if(body == null){
            return Utils.createJsonReply(false,Instruccion.INSTRUCCION_DESCONOCIDA,"ERROR");
        }
        final String idSolicitante = body.getIdSolicitante();
        final Instruccion instruccion = body.getInstruccion();
        final String parametro = body.getParametro();
        
        final JsonObject reply;
        switch(instruccion){
            case ENTRAR: 
                if(hasStarted()){
                    if(esReconexion(invoker.getClientIp())){
                        final JsonObject reconnectionData = reconexion(invoker);
                        if(reconnectionData != null){
                            return Utils.createJsonReply(true,instruccion,reconnectionData);
                        }
                        return Utils.createJsonReply(false,instruccion,"No se pudo reconectar");
                    }
                    return Utils.createJsonReply(false,instruccion,"No se puede, el juego ya esta iniciado");
                }
                if(parametro.trim().length() == 0 || parametro.trim().length() > 30 || Utils.containsStrangeCharacters(parametro.trim())){
                    return Utils.createJsonReply(false,instruccion,"Nombre invalido");
                }
                if(_prePartida.nombreExiste(parametro.trim())){
//                    WebSocket.removeClient(invoker);
                    return Utils.createJsonReply(false,instruccion,"Ese nombre ya esta tomado, elegir otro");
                }
                final Jugador jugador = _prePartida.agregarJugador(parametro.trim());
                if(jugador == null){
                    return Utils.createJsonReply(false,Instruccion.ENTRAR,"No se puede agregar, ya se alcanzo el maximo de jugadores (." + _maxPlayers + ").");
                }
                WebSocket.registerPlayer(invoker,jugador);
                enviarEstadoPrejuego();
                final JsonObject jo = new JsonObject();
                jo.addProperty("userId",jugador.getId());
                return Utils.createJsonReply(true,instruccion,jo);
            case GET_JUGADORES:
                break;
            case CONFIGURAR_JUEGO:
                if(hasStarted()){
                    return Utils.createJsonReply(false,instruccion,"No se puede, el juego ya esta iniciado");
                }
                reply = _prePartida.setGameParams(idSolicitante,parametro.trim());
                if(!reply.get("status").getAsBoolean()){
                    return reply;
                }
                WebSocket.broadcast(_prePartida.getCandidatos(), reply);
                return null;
            case EMPEZAR: 
                if(hasStarted()){
                    return Utils.createJsonReply(false,instruccion,"No se puede, el juego ya esta iniciado");
                }
                String rta = _prePartida.pedirEmpezar(idSolicitante);
                if(!"OK".equals(rta)){
                    return Utils.createJsonReply(false,instruccion,rta);
                }
                if(!_prePartida.solicitarInicio()){
                    final JsonArray statusJugadores = createPlayerStatusArray();
                    WebSocket.broadcast(_prePartida.getCandidatos(),Utils.createJsonReply(true,Instruccion.GET_JUGADORES,statusJugadores.toString()));
                    return null;
                }
                _juego = _prePartida.iniciarJuego();
                final JsonObject datosInicio = new JsonObject();
                final JsonArray jugadores = new JsonArray();
                for(final Jugador j : _juego.getJugadores()){
                    jugadores.add(j.getNombre());
                }
                datosInicio.add("jugadores", jugadores);
                datosInicio.addProperty("cantCartas",_juego.getCantMaximaCartas());
                datosInicio.addProperty("tablero",TableroManager.generarTablero(_juego.getJugadores()));
                WebSocket.broadcast(_prePartida.getCandidatos(),Utils.createJsonReply(true, instruccion, datosInicio));
                return null;
            case VER_TABLA: 
                if(!hasStarted()){
                    return Utils.createJsonReply(false, instruccion,"Aun no ha empezado la partida");
                }
                return Utils.createJsonReply(true, instruccion,_juego.getTabla());
            case TIRAR_CARTA:
                if(!hasStarted()){
                    return Utils.createJsonReply(false, instruccion,"Aun no ha empezado la partida");
                }
                reply =  _juego.tirar(idSolicitante, parametro);
                if(!reply.get("status").getAsBoolean()){
                    return reply;
                }
                WebSocket.broadcast(_juego.getJugadores(), reply);
                return null;
            case ELEGIR_NUMERO:
                if(!hasStarted()){
                    return Utils.createJsonReply(false, instruccion,"Aun no ha empezado la partida");
                }
                final int numeroElegido = Utils.parsearNumero(parametro);
                if(numeroElegido < 0){
                    return Utils.createJsonReply(false, instruccion,"Numero invalido");
                }
                reply =  _juego.elegir(idSolicitante, numeroElegido);
                if(!reply.get("status").getAsBoolean()){
                    return reply;
                }
                WebSocket.broadcast(_juego.getJugadores(), reply);
                return null;
            case REPARTIR:
                if(!hasStarted()){
                    return Utils.createJsonReply(false, instruccion,"Aun no ha empezado la partida");
                }
                reply = _juego.repartir(idSolicitante);
                if(!reply.get("status").getAsBoolean()){
                    return reply;
                }
                if(!_juego.esRondaIndia()){
                    WebSocket.broadcastCartas(_juego.getJugadores(),reply);
                }else{
                    WebSocket.broadcastCartasIndias(_juego.getJugadores(),reply);
                }
                return null;
            case MENSAJE_ESTANDAR:
                if(!hasStarted()){
                    return Utils.createJsonReply(false, instruccion,"Aun no ha empezado la partida");
                }
                reply =  _juego.decirMensajeEstandar(idSolicitante,parametro);
                if(!reply.get("status").getAsBoolean()){
                    return reply;
                }
                WebSocket.broadcast(_juego.getJugadores(), reply);
                return null;
            case ENVIAR_MENSAJE:
                if(!hasStarted()){
                    return Utils.createJsonReply(false, instruccion,"Aun no ha empezado la partida");
                }
                reply =  _juego.enviarMensaje(idSolicitante,parametro);
                if(!reply.get("status").getAsBoolean()){
                    return reply;
                }
                WebSocket.broadcast(_juego.getJugadores(), reply);
                return null;
            case INSTRUCCION_DESCONOCIDA:
            default:
        };
        return Utils.createJsonReply(false,Instruccion.INSTRUCCION_DESCONOCIDA,"Pedido no reconocido");
    }
    
    private JsonArray createPlayerStatusArray(){
        final JsonArray estadoJugadores = new JsonArray();
        for(final Jugador candidato : _prePartida.getCandidatos()){
            final JsonObject jug = new JsonObject();
            jug.addProperty("name",candidato.getNombre());
            jug.addProperty("status",candidato.getQuieroEmpezar());
            estadoJugadores.add(jug);
        }
        return estadoJugadores;
    }

    public void quitarCandidato(final Jugador jugador) {
        _prePartida.quitarCandidato(jugador);
    }
    
    private boolean hasStarted(){
        return _juego != null;
    }
    
    private void enviarEstadoPrejuego(){
        final JsonArray estadoJugadores = createPlayerStatusArray();
        final JsonObject broadcastNewPlayers = Utils.createJsonReply(true,Instruccion.GET_JUGADORES,estadoJugadores.toString());
        WebSocket.broadcast(_prePartida.getCandidatos(),broadcastNewPlayers);
        final JsonObject broadcastConfiguration = Utils.createJsonReply(true,Instruccion.CONFIGURAR_JUEGO,_prePartida.getMensajeConfiguracion());
        WebSocket.broadcast(_prePartida.getCandidatos(),broadcastConfiguration);
    }
    
    public void desconexion(final ClientThread clientThread){
//        /192.168.1.4:50027
        final Jugador jugador = WebSocket.removeClient(clientThread);
        if(jugador != null){
            if(!hasStarted()){
                jugador.setQuieroEmpezar(false);
                quitarCandidato(jugador);
                enviarEstadoPrejuego();
            } else{
                _disconnectedPlayers.put(clientThread.getClientIp(),jugador);
                final JsonObject data = new JsonObject();
                data.addProperty("conexion", false);
                data.addProperty("nombre", jugador.getNombre());
                final JsonObject mensaje = Utils.createJsonReply(true,Instruccion.INFORMAR_CAMBIO_CONEXION,data);
                WebSocket.broadcast(_juego.getJugadores(), mensaje);
            }
        }    
    }
    
    private JsonObject reconexion(final ClientThread clientThread){
        final Jugador jugadorReconectado = _disconnectedPlayers.remove(clientThread.getClientIp());
        WebSocket.registerPlayer(clientThread, jugadorReconectado);
        
        final List<Jugador> jugadores = new ArrayList<>();
        for(final Jugador jugadorValido : _juego.getJugadores()){
            if(jugadorReconectado != jugadorValido){
                jugadores.add(jugadorValido);
            }
        }
        final JsonObject data = new JsonObject();
        data.addProperty("conexion", true);
        data.addProperty("nombre", jugadorReconectado.getNombre());
        final JsonObject mensaje = Utils.createJsonReply(true,Instruccion.INFORMAR_CAMBIO_CONEXION,data);
        WebSocket.broadcast(jugadores, mensaje);
        final JsonObject reply = new JsonObject();
        reply.add("datos",crearDatosDeJuego(jugadorReconectado));
        return reply;
    }
    
    private boolean esReconexion(final String remoteAddress){
       return _disconnectedPlayers.containsKey(remoteAddress);
    }

    private JsonObject crearDatosDeJuego(final Jugador jugadorReconectado) {
        final JsonObject gameData = new JsonObject();
        final JsonArray datosJugadores = new JsonArray();
        int pedidas = 0;
        for(final Jugador j : _juego.getJugadores()){
            final JsonObject datosJugador = new JsonObject();
            datosJugador.addProperty("nombre",j.getNombre());
            final Carta cartaJugada = j.peekCartaJugada();
            datosJugador.addProperty("cartaJugada",cartaJugada==null?"blank":cartaJugada.getId());
            datosJugador.addProperty("bazasGanadas",j.getBazasGanadas());
            final int pedidasJugador = j.getPedido();
            datosJugador.addProperty("bazasPedidas",pedidasJugador);
            if(pedidasJugador != -1){
                pedidas += pedidasJugador;
            }
            datosJugadores.add(datosJugador);
        }
        gameData.add("datosJugadores", datosJugadores);
        gameData.addProperty("tablero",TableroManager.generarTablero(_juego.getJugadores()));
        gameData.addProperty("turno", _juego.getTurno());
        gameData.addProperty("cantCartas", _juego.getCantMaximaCartas());
        gameData.addProperty("username", jugadorReconectado.getNombre());
        gameData.addProperty("userId", jugadorReconectado.getId());
        final JsonArray cartasEnMano = jugadorReconectado.peekCartas();
        for(int i=cartasEnMano.size(); i<_juego.getBazasQueDebenJugarseEstaRonda(); i++){
            cartasEnMano.add("blank");
        }
        gameData.add("cartas", cartasEnMano);
        gameData.addProperty("pedidas", pedidas);
        return gameData;
    }

}
