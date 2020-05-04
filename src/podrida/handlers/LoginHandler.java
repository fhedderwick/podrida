package podrida.handlers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.io.InputStream;
import podrida.managers.UserManager;
import podrida.model.Instruccion;
import static podrida.utils.Constants.BLOCKED_STRING;
import podrida.utils.Utils;

public class LoginHandler extends AbstractHandler {
    
    private final UserManager _userManager;
    
    public LoginHandler(final UserManager userManager){
        _userManager = userManager;
    }
    
    @Override
    protected String processRequest(final HttpExchange httpExchange, final String method) {
        final JsonObject response;
        final String responseAsString;
        if(POST.equals(method)){
            final Headers requestHeaders = httpExchange.getRequestHeaders();
            String charset = "UTF-8";
            if(requestHeaders != null){
                final String contentType = requestHeaders.get("Content-type").get(0);
                final String[] split = contentType.split(";");
                for(final String string : split){
                    if(string.trim().contains("charset=")){
                        charset = string.trim().replace("charset=", "");
                        break;
                    }
                }
            }
            final InputStream requestBody = httpExchange.getRequestBody();
            final String requestBodyAsString = Utils.readInputStream(requestBody,charset);
            try{
                final JsonObject requestBodyAsJsonObject = JsonParser.parseString(requestBodyAsString).getAsJsonObject();
                final String username = requestBodyAsJsonObject.get("username").getAsString();
                final String pass = requestBodyAsJsonObject.get("pass").getAsString();
                final String token = _userManager.login(username,pass);
                if(token != null){
                    if(BLOCKED_STRING.equals(token)){
                        response = Utils.createJsonReply(false,Instruccion.INSTRUCCION_DESCONOCIDA,"El usuario esta bloqueado");
                    } else {
                        response = Utils.createJsonReply(true,Instruccion.INSTRUCCION_DESCONOCIDA,token);
                    }
                } else {
                    response = Utils.createJsonReply(false,Instruccion.INSTRUCCION_DESCONOCIDA,"Usuario o clave invalidos");
                }
                responseAsString = response.toString();
            } catch(final Exception e) {
                e.printStackTrace();
                return Utils.createJsonReply(false,Instruccion.INSTRUCCION_DESCONOCIDA,"Error en la solicitud").toString();
            }
        } else {
            response = Utils.createJsonReply(false,Instruccion.INSTRUCCION_DESCONOCIDA,"Metodo no soportado");
            responseAsString = response.toString();
        }
        return responseAsString;
    }
}
 