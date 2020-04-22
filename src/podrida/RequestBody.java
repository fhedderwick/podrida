package podrida;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import podrida.model.Instruccion;

public class RequestBody {

    private String _idSolicitante = null;
    private Instruccion _instruccion = null;
    private String _parametro = null;
    private boolean _error = true;

    public static RequestBody parseRequestBody(final String requestBody) {
        try {
            final RequestBody req = new RequestBody(requestBody);
            if (!req._error) {
                return req;
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private RequestBody(final String requestBody) {
        try {
            final JsonObject jo = JsonParser.parseString(requestBody).getAsJsonObject();
            _idSolicitante = jo.get("id").getAsString();
            final String instruccion = jo.get("com").getAsString();
            _instruccion = Instruccion.getInstruccion(instruccion);
            _parametro = jo.get("param").getAsString();
            _error = false;
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public String getIdSolicitante() {
        return _idSolicitante;
    }

    public Instruccion getInstruccion() {
        return _instruccion;
    }

    public String getParametro() {
        return _parametro;
    }

}
