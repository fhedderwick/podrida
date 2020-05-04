package podrida.model.estadistica;

import java.util.List;

public class EstadisticasUsuario {

    private final List<String> _lineas;
    private final String _username;
    private int _partidosTotales;
    private int _partidosPrimero;
    private int _partidosSegundo;
    private int _partidosUltimo;
    private int _partidosInvicto;
    private int _partidosAbandonados;
    private int _promedioPuntuacion;
    private int _promedioPuntuacionMaximo;

    private int _puntosTotales;
    private int _ranking;
    
    public EstadisticasUsuario(final String username, final List<String> lineas) {
        _username = username;
        _lineas = lineas;
        for(final String linea : lineas){
            final EstadisticaJugadorPartido ejp = EstadisticaJugadorPartido.leerLinea(linea);
            if(ejp != null){
                procesar(ejp);
            } else {
                System.out.println("Hay un error en la linea \"" + linea + "\" del archivo de datos del usuario " + _username);
            }
        }
//        _promedioPuntuacion;
//        _promedioPuntuacionMaximo;
    }
    
    private void procesar(final EstadisticaJugadorPartido ejp){
//        final JsonObject jo = ejp.toJsonObject();
        _partidosTotales++;
        if(ejp.getPuesto() == 1){
            _partidosPrimero++;
        }
        if(ejp.getPuesto() == 2){
            _partidosSegundo++;
        }
        if(ejp.getPuesto() == ejp.getCantJugadores()){
            _partidosUltimo++;
        }
        if(ejp.getPuesto() == -1){
            _partidosAbandonados++;
        }
        if(ejp.getCumplidas() == ejp.getCantBazasJugadas() && ejp.getFalladas() == 0){
            _partidosInvicto++;
        }
    }

    public String getUsername() {
        return _username;
    }

    public int getPartidosTotales() {
        return _partidosTotales;
    }

    public int getPartidosPrimero() {
        return _partidosPrimero;
    }

    public int getPartidosSegundo() {
        return _partidosSegundo;
    }

    public int getPartidosUltimo() {
        return _partidosUltimo;
    }

    public int getPartidosInvicto() {
        return _partidosInvicto;
    }

    public int getPartidosAbandonados() {
        return _partidosAbandonados;
    }

    public int getPromedioPuntuacion() {
        return _promedioPuntuacion;
    }

    public int getPromedioPuntuacionMaximo() {
        return _promedioPuntuacionMaximo;
    }

    public int getPuntosTotales() {
        return _puntosTotales;
    }

    public int getRanking() {
        return _ranking;
    }

}
