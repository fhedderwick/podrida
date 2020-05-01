package podrida;

import podrida.model.User;
import com.google.gson.JsonObject;
import podrida.model.Jugador;
import java.util.ArrayList;
import java.util.List;
import podrida.model.Instruccion;
import podrida.utils.Utils;

public class PrePartida {
    
    private final List<Jugador> _candidatos;
    private final int _maxPlayers;
    private Configuracion _configuracion;
    
    public PrePartida(final int maxPlayers){
        _maxPlayers = maxPlayers;
        _candidatos = new ArrayList<>();
    }
    
    public synchronized Jugador agregarJugador(final ClientThread clientThread) {
        if(_candidatos.size() >= _maxPlayers){
            return null;
        }
        final Jugador jugador = new Jugador(clientThread);
        System.out.println("Registrado a " + jugador.getUsername() + " en " + clientThread.getRemoteAddress());
        _candidatos.add(jugador);
//        System.out.println("quitar este mock"); _candidatos.addAll(Jugador.mockJugadores(5));
        return jugador;
    }
    
    public synchronized boolean solicitarInicio(){
        if(_candidatos.size() < 2){
            return false;
        }
        for(final Jugador candidato : _candidatos){
            if(!candidato.getQuieroEmpezar()){
                return false;
            }
        }
        return true;
    }
    
    public Partida iniciarJuego(){ //sincronizar con agregar jugador
        if(_configuracion == null){
            _configuracion = Configuracion.crearConfiguracion();
        }
        return new Partida(_candidatos, _configuracion);
    }

    public boolean nombreExiste(final String nombre) {
        for(final Jugador candidato : _candidatos){
            if(candidato.getUsername().equals(nombre)){
                return true;
            }
        }
        return false;
    }

    public String pedirEmpezar(final String idSolicitante) {
        for(final Jugador jugador : _candidatos){
            if(jugador.getUserToken().equals(idSolicitante)){
                if(!jugador.pedirEmpezar()){
                    return "Ya fue solicitado anteriormente";
                }
                return "OK";
            }
        }
        return "Jugador no encontrado";
    }
    
    public List<Jugador> getCandidatos(){
        return _candidatos;
    }
    
    public List<ClientThread> getOnlineThreadsCandidatos(){
        final List<ClientThread> clientThreads = new ArrayList<>();
        for(final Jugador candidato : _candidatos){
            final ClientThread clientThread = candidato.getClientThread();
            if(clientThread != null && clientThread.isConnected()){
                clientThreads.add(candidato.getClientThread());
            }
        }
        return clientThreads;
    }
    
    public void quitarCandidato(final ClientThread clientThread){
        Jugador jugadorAQuitar = null;
        for(final Jugador jugador : _candidatos){
            if(jugador.getClientThread().equals(clientThread)){
                jugadorAQuitar = jugador;
                break;
            }
        }
        if(jugadorAQuitar != null){
            _candidatos.remove(jugadorAQuitar);
        }
    }

    public JsonObject setGameParams(final String idSolicitante, final String parametros) {
        if(!_candidatos.get(0).getUserToken().equals(idSolicitante)){
            return Utils.createJsonReply(false,Instruccion.CONFIGURAR_JUEGO,"Solo valido para primer jugador entrante");
        }
        _configuracion = Configuracion.crearConfiguracion(parametros);
        if(_configuracion != null){
            return Utils.createJsonReply(true, Instruccion.CONFIGURAR_JUEGO, _configuracion.getMessage());
        }
        return Utils.createJsonReply(false, Instruccion.CONFIGURAR_JUEGO, "Error al setear la configuracion de juego");
    }
    
    public String getMensajeConfiguracion(){
        return _configuracion != null ? _configuracion.getMessage() : "Juego aun no configurado";
    }

    public Jugador getCandidatoByUser(final User user) {
        for(final Jugador jugador : _candidatos){
            if(jugador.getUsername().equals(user.getUsername())){
                return jugador;
            }
        }
        return null;
    }

    public void reemplazarThread(final Jugador jugador, final ClientThread invoker) {
        jugador.replaceClientThread(invoker);
    }

}
