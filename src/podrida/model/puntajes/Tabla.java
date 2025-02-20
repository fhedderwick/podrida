package podrida.model.puntajes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import podrida.model.Jugador;

public class Tabla {
    
    private final List<Row> _rows;
    private int _rowActual = 0;
    
    public Tabla(final int rondaMaxima, final List<Jugador> jugadores) {
        _rows = new ArrayList<>();
        _rowActual = 0;
        final Row _headerRow = new Row();
        final Casilla primeraCasillaHeader = new Casilla();
        primeraCasillaHeader.setNombre("-");
        _headerRow.addCasilla(primeraCasillaHeader);
        for(final Jugador jugador : jugadores){
            final Casilla casillaNombre = new Casilla();
            casillaNombre.setNombre(jugador.getUsername());
            _headerRow.addCasilla(casillaNombre);
        }
        _rows.add(_headerRow);
        int index = 3;
        boolean creciendo = true;
        while(index > 0){
            final Row row = new Row(index,jugadores.size());
            _rows.add(row);
            if (creciendo && index < rondaMaxima) {
                index++;
            } else {
                creciendo = false;
                index--;
            }
        }
        _rowActual++;
    }

    public JsonObject calcularPuntajesRonda(final List<Jugador> jugadores) {
        final Row row = _rows.get(_rowActual);
        final JsonObject reply = new JsonObject();
        final List<Casilla> casillas = row.getCasillas();
        final List<Casilla> casillasAnteriores = _rows.get(_rowActual-1).getCasillas();
        int max = -50;
        int min = 30000;
        for(int i=1;i<casillas.size();i++){
            final JsonObject content = new JsonObject();
            final Jugador jugador = jugadores.get(i-1);
            final Casilla casilla = casillas.get(i);
            final Casilla casillaAnterior = casillasAnteriores.get(i);
            casilla.setPedido(jugador.getPedido());
            final int obtenido= jugador.puntuar();
            casilla.setBazas(jugador.getBazasGanadas());
            final int acumulado = casillaAnterior.getAcumulado() + obtenido;
            casilla.setAcumulado(acumulado);
            casilla.setTachado(false);
            casillaAnterior.setTachado(true);
            content.addProperty("total", acumulado);
            if(acumulado < min){
                min = acumulado;
            }
            if(acumulado > max){
                max = acumulado;
            }
            reply.add(jugador.getUsername(), content);
        }
        for(int i=1;i<casillas.size();i++){
            final Casilla casilla = casillas.get(i);
            casilla.setPuntera(casilla.getAcumulado() == max);
            casilla.setUltima(casilla.getAcumulado() == min);
        }
        for(final Jugador jugador : jugadores){
            final JsonObject jo = reply.get(jugador.getUsername()).getAsJsonObject();
            final int total = jo.get("total").getAsInt();
            jo.addProperty("puntero", total == max);
            jo.addProperty("ultimo", total == min);
            reply.add(jugador.getUsername(), jo);
        }
        _rowActual++;
        
        return reply;
    }

    public JsonArray getPuntajes() {
        final JsonArray reply = new JsonArray();
        final List<Casilla> casillasHeader = _rows.get(0).getCasillas();
        final JsonArray header = new JsonArray();
        for(final Casilla casilla : casillasHeader){
            header.add(casilla.getNombre());
        }
        reply.add(header);
        for(int i=1;i<_rows.size();i++){
            final JsonArray ja = new JsonArray();
            final List<Casilla> casillas = _rows.get(i).getCasillas();
            ja.add(casillas.get(0).getRondaBazas());
            for(int j=1;j<casillas.size();j++){
                ja.add(casillas.get(j).getAsJsonObject());
            }
            reply.add(ja);
        }
        return reply;
    }
    
    public JsonObject getResultadosParaGuardar(){
        final JsonObject jo = new JsonObject();
        if(_rowActual < 2){
            return null;
        }
        final Set<Integer> set = new HashSet<>();
        final Row header = _rows.get(0); //nombres
        final List<Casilla> casillasHeader = header.getCasillas();
        for(int i=1; i<casillasHeader.size(); i++){
            final String nombre = casillasHeader.get(i).getNombre();
            final JsonObject jugadorJo = new JsonObject();
            final JsonArray historia = new JsonArray();
            int puntuacionFinal = 0;
            for(int j=1; j<_rows.size();j++){
                final Casilla casilla = _rows.get(j).getCasillas().get(i);
                historia.add(casilla.getPedido());
                historia.add(casilla.getBazas());
                puntuacionFinal = casilla.getAcumulado();
            }
            jugadorJo.addProperty("puntaje",puntuacionFinal);
            set.add(puntuacionFinal);
            jugadorJo.add("historia",historia);
            jo.add(nombre,jugadorJo);
        }
        final List lista = new ArrayList<>(set);
        Collections.sort(lista,Collections.reverseOrder());
        for(final Map.Entry<String, JsonElement> entry : jo.entrySet()){
            final JsonObject obj = entry.getValue().getAsJsonObject();
            final int puntajeFinal = obj.get("puntaje").getAsInt();
            obj.addProperty("puesto",lista.indexOf(puntajeFinal) + 1);
            obj.addProperty("ultimoPuesto",lista.size());
        }
        return jo;
    }
    
}