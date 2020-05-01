package podrida;

import com.google.gson.JsonElement;
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
            final JsonElement idSolicitante = jo.get("id");
            final JsonElement parametro = jo.get("param");
            _idSolicitante = idSolicitante != null ? idSolicitante.getAsString() : "";
            final String instruccion = jo.get("com").getAsString();
            _instruccion = Instruccion.getInstruccion(instruccion);
            _parametro = parametro != null ? parametro.getAsString() : "";
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
