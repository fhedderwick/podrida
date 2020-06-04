package podrida.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import podrida.model.Carta;
import podrida.model.Jugador;

public class Stats {

//    private static final Map<String,Integer> _randomInts = new HashMap<>();
    private static final Map<String,Map<String,Integer>> _cantCartas = new HashMap<>();
    private static final Map<String,Integer> _cantGlobalCartas = new HashMap<>();
    
//    static void logRandomInt(final int value) {
//        final String val = String.valueOf(value);
//        final Integer cant = _randomInts.containsKey(val) ? _randomInts.get(val) : 0;
//        _randomInts.put(val, cant+1);
//    }

    public static void logCartasParaJugador(final Jugador jugador, final List<Carta> cartas) {
        final String nombreJugador = jugador.getUsername();
        if(!_cantCartas.containsKey(nombreJugador)){
            _cantCartas.put(nombreJugador,new HashMap<>());
        }
        final Map<String,Integer> playerMap = _cantCartas.get(nombreJugador);
        for(final Carta carta : cartas){
            final String idCarta = carta.getId();
            final Integer cant = playerMap.containsKey(idCarta) ? playerMap.get(idCarta) : 0;
            playerMap.put(idCarta, cant+1);
            
            final Integer cantGlobal = _cantGlobalCartas.containsKey(idCarta) ? _cantGlobalCartas.get(idCarta) : 0;
            _cantGlobalCartas.put(idCarta, cantGlobal+1);
        }
    }

    public static void dump(final String matchId) {
        final File file = new File(".",matchId + ".txt");
        if(file.exists()){
            System.out.println("Error, el archivo de estadisticas ya existe");
            return;
        }
        try {
            final FileWriter fw = new FileWriter(file,true);
            
//            fw.write("Numeros random / Limite superior:");
//            fw.write(System.lineSeparator());
//            for(final Entry<String, Integer> entry : _randomInts.entrySet()){
//                fw.write(entry.getKey() + " - " + entry.getValue());
//                fw.write(System.lineSeparator());
//            }
            fw.write("Cartas por jugador:");
            fw.write(System.lineSeparator());
            for(final Entry<String, Map<String,Integer>> entry : _cantCartas.entrySet()){
                fw.write("\tCartas de jugador: " + entry.getKey());
                fw.write(System.lineSeparator());
                for(final Entry<String, Integer> subentry : entry.getValue().entrySet()){
                    fw.write("\t\t" + subentry.getKey() + "\t" + subentry.getValue());
                    fw.write(System.lineSeparator());
                }
            }
            fw.write("Cartas totales:");
            fw.write(System.lineSeparator());
            for(final Entry<String, Integer> entry : _cantGlobalCartas.entrySet()){
                fw.write("\t" + entry.getKey() + "\t" + entry.getValue());
                fw.write(System.lineSeparator());
            }
            
            fw.close();
        } catch (final IOException ex) {
            ex.printStackTrace();
            System.out.println("Error al escribir estadisticas del juego " + matchId);
        }
    }

}
