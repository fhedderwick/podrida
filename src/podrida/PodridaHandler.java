package podrida;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import podrida.utils.Utils;

public class PodridaHandler implements HttpHandler {
    
    private final static String GET = "GET";
    private final static String POST = "POST";
    
    private final GameManager _gameManager;
    private final JsonObject _encodedSoundReply = Utils.encodeSound("resources/buzzer.ogg");
    private final JsonObject _encodedImagesReply = Utils.encodeImages("resources/cards/");
    
    public PodridaHandler(final int maxPlayers){
        _gameManager = new GameManager(maxPlayers);
    }
    
    @Override
    public void handle(final HttpExchange t) throws IOException {
        final JsonObject response;
        final String responseAsString;
        final String method = t.getRequestMethod();
        if(GET.equals(method)){
            /*
            final String query = t.getRequestURI().getQuery().trim();
            final List<Jugador> jugadores = Jugador.mockJugadores(Integer.parseInt(query));
            responseAsString = TableroManager.generarTablero(jugadores);
            */
            /*
            podrida.model.Tabla _tabla = new podrida.model.Tabla(7,podrida.model.Jugador.mockJugadores(7));
            responseAsString = _tabla.getPuntajes().toString();
            
            */
            responseAsString = Utils.readFile("resources/game.html");
            System.out.println("quitar esta manera!");//Se lee cada vez para soportar cambios on the fly, podria sacarse luego de que el frontend quede listo
        } else if(POST.equals(method)){
            final String query = t.getRequestURI().getQuery().trim();
            if(query.startsWith("name=")){
                final String name = query.substring(5);
                if(name.length() == 0 || name.length() > 20 || Utils.containsStrangeCharacters(name)){
                    responseAsString = "Nombre invalido";
                } else if(_gameManager.existsName(name)){
                    responseAsString = "Ese nombre ya esta tomado, elegir otro";
                } else {
                    responseAsString = "Nombre libre";
                }
            } else if(query.startsWith("cartas")){
                response = _encodedImagesReply;
                responseAsString = response.toString();
            } else if(query.startsWith("sonido")){
                response = _encodedSoundReply;
                responseAsString = response.toString();
            } else {
                response = Utils.createJsonReply(false,"Solicitud no soportada");
                responseAsString = response.toString();
            }
        } else {
            response = Utils.createJsonReply(false,"Metodo no soportado");
            responseAsString = response.toString();
        }
        
        final byte[] bytesToSend = responseAsString.getBytes();
        t.sendResponseHeaders(200, bytesToSend.length);
        final OutputStream os = t.getResponseBody();
        os.write(bytesToSend);
        os.close();
    }
}
