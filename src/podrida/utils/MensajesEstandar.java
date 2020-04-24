package podrida.utils;

import java.util.ArrayList;
import java.util.List;

public class MensajesEstandar {

    private static final List<String> _mensajes = new ArrayList<>();
    
    public static void loadMessages(final String rutaArchivoMensajes){
        _mensajes.addAll(Utils.readFileLines(rutaArchivoMensajes));
    }
    
    public static String get(final String parametro){
        final int index = Utils.parsearNumero(parametro);
        if(index < 0 || index >= _mensajes.size()){
            return null;
        }
        return _mensajes.get(index);
    }
    
    public static boolean isLoaded() {
        return !_mensajes.isEmpty();
    }
    
}