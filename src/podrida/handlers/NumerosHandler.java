package podrida.handlers;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import podrida.model.Instruccion;
import podrida.utils.Utils;

public class NumerosHandler extends AbstractHandler {
    
    @Override
    protected String processRequest(final HttpExchange httpExchange, final String method) {
        final JsonObject response;
        final String responseAsString;
        if(GET.equals(method)){
            //Se lee cada vez para soportar cambios on the fly, podria sacarse luego de que el frontend quede listo
            responseAsString = Utils.readFile("resources/numeros.html");
        } else {
            response = Utils.createJsonReply(false,Instruccion.INSTRUCCION_DESCONOCIDA,"Metodo no soportado");
            responseAsString = response.toString();
        }
        return responseAsString;
    }

}
