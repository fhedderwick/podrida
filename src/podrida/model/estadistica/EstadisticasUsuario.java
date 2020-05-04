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

    public String getAsHtmlTable() {
//        final JsonObject jo = new JsonObject();
//        jo.addProperty("nombreJugador",_username);
//        jo.addProperty("partidosTotales",_partidosTotales);
//        jo.addProperty("partidosPrimero",_partidosPrimero);
//        jo.addProperty("partidosSegundo",_partidosSegundo);
//        jo.addProperty("partidosUltimo",_partidosUltimo);
//        jo.addProperty("partidosInvicto",_partidosInvicto);
//        jo.addProperty("partidosAbandonados",_partidosAbandonados);
//        jo.addProperty("promedioPuntuacion",_promedioPuntuacion);
//        jo.addProperty("promedioPuntuacionMaximo",_promedioPuntuacionMaximo);
//        jo.addProperty("puntosTotales",_puntosTotales);
//        jo.addProperty("ranking",_ranking);
//        return jo;
        final StringBuilder sb = new StringBuilder();
        sb.append("<table class='bordered padded'>");
//        align='center' class='bordered padded'
        sb.append("<tr class='bordered padded'><td class='bordered padded'>Jugador</td><td class='bordered padded'>").append(_username).append("</td></tr>");
        sb.append("<tr class='bordered padded'><td class='bordered padded'>Partidos jugados</td><td class='bordered padded'>").append(_partidosTotales).append("</td></tr>");
        sb.append("<tr class='bordered padded'><td class='bordered padded'>Triunfos</td><td class='bordered padded'>").append(_partidosPrimero).append("</td></tr>");
        sb.append("<tr class='bordered padded'><td class='bordered padded'>Segundo puestos</td><td class='bordered padded'>").append(_partidosSegundo).append("</td></tr>");
        sb.append("<tr class='bordered padded'><td class='bordered padded'>Ultimo lugar</td><td class='bordered padded'>").append(_partidosUltimo).append("</td></tr>");
        sb.append("<tr class='bordered padded'><td class='bordered padded'>Invictos</td><td class='bordered padded'>").append(_partidosInvicto).append("</td></tr>");
        sb.append("<tr class='bordered padded'><td class='bordered padded'>Abandonos</td><td class='bordered padded'>").append(_partidosAbandonados).append("</td></tr>");
//        sb.append("<tr><td>Promedio puntuacion total</td><td>").append(_promedioPuntuacion).append("</td></tr>");
//        sb.append("<tr><td>Promedio puntuacion maximo</td><td>").append(_promedioPuntuacionMaximo).append("</td></tr>");
//        sb.append("<tr><td>Puntos totales</td><td>").append(_puntosTotales).append("</td></tr>");
//        sb.append("<tr><td>Ranking</td><td>").append(_ranking).append("</td></tr>");
        sb.append("</table>");
        return sb.toString();
    }
}
