package podrida.utils;

import com.google.gson.JsonArray;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import podrida.model.Carta;
import podrida.model.Jugador;
import podrida.model.Mazo;

public class Stats {

    private static final Map<Carta,List<String>> _mapa = new HashMap<>();

    public Stats(final Mazo mazo) {
        for(final Carta carta : mazo.getCartas(mazo.getCantMaximaCartas())){
            _mapa.put(carta, new ArrayList<>());
            mazo.devolverAlMazo(carta);
        }
    }
    
    public void logCartasParaJugador(final Jugador jugador, final List<Carta> cartas) {
        final String nombreJugador = jugador.getUsername();
        for(final Carta carta : cartas){
            final List<String> poseedores = _mapa.get(carta);
            poseedores.add(nombreJugador);
        }
    }
    
    public void dump(final String matchId) {
        final File file = new File(".",matchId + ".txt");
        if(file.exists()){
            System.out.println("Error, el archivo de estadisticas ya existe");
            return;
        }
        try {
            final FileWriter fw = new FileWriter(file,true);
            for(final Entry<Carta, List<String>> entry : _mapa.entrySet()){
                final StringBuilder sb = new StringBuilder();
                sb.append(entry.getKey().getId());
                sb.append("\t");
                final List<String> poseedores = entry.getValue();
                sb.append(poseedores.size());
                for(final String poseedor : poseedores){
                    sb.append("\t");
                    sb.append(poseedor);
                }
                fw.write(sb.toString());
                fw.write(System.lineSeparator());
            }
            fw.close();
        } catch (final IOException ex) {
            ex.printStackTrace();
            System.out.println("Error al escribir estadisticas del juego " + matchId);
        }
    }

    public JsonArray getAsHtmlTable() {
        final JsonArray ja = new JsonArray();
        try{
            ja.add("<tr class='bordered padded'><td class='bordered padded'>Carta</td><td class='bordered padded'>Apariciones</td></tr>");
            for(final Entry<Carta, List<String>> entry : _mapa.entrySet()){
                final int apariciones = entry.getValue().size();
                if(apariciones == 0){
                    continue;
                }
                final StringBuilder sb = new StringBuilder();
                sb.append("<tr class='bordered padded'><td class='bordered padded'>");
                sb.append(entry.getKey().getReadableName());
                sb.append("</td><td class='bordered padded'>");
                sb.append(apariciones);
                sb.append("</td></tr>");
                ja.add(sb.toString());
            }
        }catch(final Exception e){
            e.printStackTrace();
        }
        return ja;
    }

}