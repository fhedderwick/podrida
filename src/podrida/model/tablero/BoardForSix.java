package podrida.model.tablero;

import java.util.List;
import podrida.model.Jugador;

public final class BoardForSix extends Board{
    
    @Override
    protected String createBoard(final List<Jugador> jugadores){
        final StringBuilder sb = new StringBuilder();
        sb.append("<table>");
        sb.append("<tr>");
        sb.append("<td align='center' id='indicadorBazas'></td>");
        sb.append("<td>").append(createSquare(jugadores.get(4).getUsername())).append("</td>");
        sb.append("<td>").append(createSquare(jugadores.get(3).getUsername())).append("</td>");
        sb.append("<td></td>");
        sb.append("</tr>");
        sb.append("<tr>");
        sb.append("<td>").append(createSquare(jugadores.get(5).getUsername())).append("</td>");
        sb.append("<td></td>");
        sb.append("<td></td>");
        sb.append("<td>").append(createSquare(jugadores.get(2).getUsername())).append("</td>");
        sb.append("</tr>");
        sb.append("<tr>");
        sb.append("<td></td>");
        sb.append("<td>").append(createSquare(jugadores.get(0).getUsername())).append("</td>");
        sb.append("<td>").append(createSquare(jugadores.get(1).getUsername())).append("</td>");
        sb.append("<td></td>");
        sb.append("</tr>");
        sb.append("</table>");
        return sb.toString();
    }
    
}
