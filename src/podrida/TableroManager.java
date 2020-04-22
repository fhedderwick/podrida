package podrida;

import java.util.List;
import podrida.model.Jugador;
import podrida.model.tablero.Board;

public class TableroManager {
    
    public static String generarTablero(final List<Jugador> jugadores) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<div>");
        sb.append(createHeader());
        sb.append(Board.getBoard(jugadores));
        sb.append(createFooter());
        sb.append("</div>");
        return sb.toString();
    }
    
    private static String createHeader(){
        final StringBuilder sb = new StringBuilder();
        sb.append("<div id='cardSpace'><table>");
        sb.append("<tr>");
//        sb.append("<td><button onclick='solicitarRepartirClick()'>REPARTIR</button></td>");
        sb.append("<td><button onclick='sendChatClick()'>CHAT</button></td></tr>");
        sb.append("</table></div>");
//        botonera de cartas, con botones de arriba abajo y tirar;
//        boton ver puntajes;
        return sb.toString();
    }
    
    private static String createFooter(){
//        chat;
        return "";
    }
    
}
