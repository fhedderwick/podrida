package podrida.handlers;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import podrida.model.User;
import podrida.managers.UserManager;
import podrida.model.Instruccion;
import podrida.utils.Utils;

public class PodridaHandler extends AbstractHandler {
    
    private final static String GUEST_KEY = "guest=";
    private final static String RESOURCES_CALL = "resources";
    private final static String WS_PORT_PLACEHOLDER = "###WS_PORT###";
    private final static String GUEST_PREFIX = "_";
    
    private final int _wsPort;
    private final UserManager _userManager;
    private final JsonObject _encodedSoundReply = Utils.encodeSound("resources/buzzer.ogg");
    private final JsonObject _encodedImagesReply = Utils.encodeImages("resources/cards/");
    
    public PodridaHandler(final int wsPort, final int maxPlayers, final UserManager userManager){
        _wsPort = wsPort;
        _userManager = userManager;
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
            if(query.startsWith(GUEST_KEY)){
                final String name = query.substring(GUEST_KEY.length());
                if(name.length() == 0 || name.length() > 20 || Utils.containsStrangeCharacters(name)){
                    response = Utils.createJsonReply(false,Instruccion.INSTRUCCION_DESCONOCIDA,"Nombre invalido");
                    responseAsString = response.toString();
                } else if(_userManager.existsName(name)){
                    response = Utils.createJsonReply(false,Instruccion.INSTRUCCION_DESCONOCIDA,"Ese nombre ya esta tomado, elegir otro");
                    responseAsString = response.toString();
                } else {
                    final User guestUser = _userManager.addGuest(GUEST_PREFIX + name);
                    response = Utils.createJsonReply(true,Instruccion.INSTRUCCION_DESCONOCIDA, guestUser.getToken());
                    responseAsString = response.toString();
                }
            } else if(query.startsWith(RESOURCES_CALL)){
                final JsonObject resources = new JsonObject();
                resources.add("cards",_encodedImagesReply);
                resources.add("sound",_encodedSoundReply);
                response = Utils.createJsonReply(true,Instruccion.INSTRUCCION_DESCONOCIDA,resources);
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
