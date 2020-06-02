package podrida;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Configuracion {

    public static Configuracion crearConfiguracion() {
        return new Configuracion();
    }

    private final boolean _ultimaRondaIndia;
    private final int _cantBarajas;
    private final int _cantBazasMaximaForzada;
    
    public static Configuracion crearConfiguracion(final String parametros) {
        try{
            return new Configuracion(JsonParser.parseString(parametros).getAsJsonObject()).valid();
        } catch (final Exception e){
            e.printStackTrace();
            return null;
        }
    }
      
    private Configuracion() {
        _ultimaRondaIndia = true;
        _cantBarajas = 1;
        _cantBazasMaximaForzada = -1;
    }
    
    private Configuracion(final JsonObject parametros) {
        _ultimaRondaIndia = parametros.get("ultimaRondaIndia").getAsBoolean();
        _cantBarajas = parametros.get("cantBarajas").getAsInt();
        _cantBazasMaximaForzada = parametros.get("cantBazasMaximaForzada").getAsInt();
    }

    private Configuracion valid() {
        if(_cantBarajas > 0 && _cantBarajas < 4){
            return this;
        }
        return null;
    }
    
    public int getCantBazasMaximaForzada(){
        return _cantBazasMaximaForzada;
    }
    
    public int getCantBarajas(){
        return _cantBarajas;
    }
    
    public boolean isUltimaRondaIndia(){
        return _ultimaRondaIndia;
    }

    public String getMessage() {
        final StringBuilder sb = new StringBuilder();
        sb.append("El juego esta configurado en ");
        sb.append(_cantBarajas);
        sb.append(" baraja");
        if(_cantBarajas > 1){
            sb.append("s");
        }
        sb.append(", con ronda maxima de ");
        sb.append(_cantBazasMaximaForzada);
        sb.append(" bazas y ");
        sb.append(_ultimaRondaIndia?"con ":"sin ");
        sb.append("ronda final india.");
        return sb.toString();
    }

}
