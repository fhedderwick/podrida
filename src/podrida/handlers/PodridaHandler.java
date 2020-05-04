package podrida.handlers;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.net.ServerSocket;
import podrida.model.Instruccion;
import static podrida.utils.Constants.RESOURCES_CALL;
import static podrida.utils.Constants.RESTART;
import podrida.utils.Utils;

public class PodridaHandler extends AbstractHandler {
    
    private final static String WS_PORT_PLACEHOLDER = "###WS_PORT###";
    
    private final int _wsPort;
    private final ServerSocket _acceptingSocket;
    private final JsonObject _encodedSoundReply = Utils.encodeSound("resources/buzzer.ogg");
    private final JsonObject _encodedImagesReply = Utils.encodeImages("resources/cards/");
    
    public PodridaHandler(final int wsPort, final ServerSocket acceptingSocket){
        _wsPort = wsPort;
        _acceptingSocket = acceptingSocket;
    }
    
    @Override
    public String processRequest(final HttpExchange httpExchange, final String method) {
        final JsonObject response;
        final String responseAsString;
        if(GET.equals(method)){
            //Se lee cada vez para soportar cambios on the fly, podria sacarse luego de que el frontend quede listo
            responseAsString = Utils.readFile("resources/game.html").replace(WS_PORT_PLACEHOLDER, String.valueOf(_wsPort));
        } else if(POST.equals(method)){
            final String query = httpExchange.getRequestURI().getQuery().trim();
            if(query.startsWith(RESOURCES_CALL)){
                final JsonObject resources = new JsonObject();
                resources.add("cards",_encodedImagesReply);
                resources.add("sound",_encodedSoundReply);
                response = Utils.createJsonReply(true,Instruccion.INSTRUCCION_DESCONOCIDA,resources);
                responseAsString = response.toString();
            } else if(query.startsWith(RESTART)){
                try {
                    _acceptingSocket.close();
                } catch (final IOException ex) {
                    ex.printStackTrace();
                }
                if(_acceptingSocket.isClosed()){
                    response = Utils.createJsonReply(true,Instruccion.INSTRUCCION_DESCONOCIDA,"El juego se reiniciara");
                } else {
                    response = Utils.createJsonReply(false,Instruccion.INSTRUCCION_DESCONOCIDA,"Error al intentar reiniciar");
                }
                responseAsString = response.toString();
            } else {
                response = Utils.createJsonReply(false,Instruccion.INSTRUCCION_DESCONOCIDA,"Solicitud no soportada");
                responseAsString = response.toString();
            }
        } else {
            response = Utils.createJsonReply(false,Instruccion.INSTRUCCION_DESCONOCIDA,"Metodo no soportado");
            responseAsString = response.toString();
        }
        return responseAsString;
    }
}
