package podrida.model.tablero;

import java.util.List;
import podrida.model.Jugador;

public abstract class Board {
    
    public static String getBoard(final List<Jugador> jugadores) {
        switch(jugadores.size()){
            case 3: return new BoardForThree().createBoard(jugadores);
            case 4: return new BoardForFour().createBoard(jugadores);
            case 5: return new BoardForFive().createBoard(jugadores);
            case 6: return new BoardForSix().createBoard(jugadores);
            case 7: return new BoardForSeven().createBoard(jugadores);
            case 8: return new BoardForEight().createBoard(jugadores);
            case 9: return new BoardForNine().createBoard(jugadores);
            case 10: return new BoardForTen().createBoard(jugadores);
            default:
        };
        return "";
        
    }
    
    protected abstract String createBoard(final List<Jugador> jugadores);
    
    protected static String createSquare(final String name){
        final StringBuilder sb = new StringBuilder();
        sb.append("<table id='table_");
        sb.append(name);
        sb.append("' class='bordered padded'>");
        sb.append("<tr id='square_");
        sb.append(name);
        sb.append("' class='bordered padded'><td align='center' colspan='3'>");
        sb.append(name);
        sb.append("</td></tr>");
        sb.append("<tr><td style='text-align:center;vertical-align:middle'><button onclick='verEstadisticasUsuario(\"");
        sb.append(name);
        sb.append("\")'>STATS</td>");
        sb.append("<td style='text-align:center;vertical-align:middle'><button onclick='forzar(\"");
        sb.append(name);
        sb.append("\")'>FORZAR</td>");
        sb.append("<td style='text-align:center;vertical-align:middle'><button disabled onclick='kick(\"");
        sb.append(name);
        sb.append("\")'>ECHAR</td>");
        sb.append("</td></tr>");
        sb.append("<tr class='bordered padded'><td align='center'>Bazas</td>");
        sb.append("<td align='center' class='bordered padded' height='50' rowspan='2' id='img_").append(name).append("'>");
        sb.append("</td>");
        sb.append("<td align='center'>Pts</td></tr>");
        sb.append("<tr><td align='center' id='bazas_").append(name).append("'");
        sb.append(" bgcolor='#FFFFFF' >-");
        sb.append("</td><td align='center' id='pts_").append(name).append("'>0</td></tr>");
        sb.append("</table>");
        return sb.toString();
    }

}
