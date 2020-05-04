package podrida.model.estadistica;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import podrida.model.Baza;
import static podrida.utils.Constants.SEMICOLON;

public class EstadisticaJugadorPartido {

    private final String _idPartido;
    private final int _cantJugadores;
    private final int _cantBarajas;
    private final int _puntosObtenidos;
    private final int _cantBazasJugadas;
    private final int _puesto;
    private final List<Baza> _bazas;
    private int _bazasTotalesLlevadas = 0;
    private int _bazasTotalesPedidas = 0;
    private int _cumplidas = 0;
    private int _falladas = 0;

    public static EstadisticaJugadorPartido leerLinea(final String linea){
        final String[] split = linea.split(";");
        if(split.length < 6){
            return null;
        }
        try{
            return new EstadisticaJugadorPartido(split);
        }catch(final NumberFormatException nfe){
            nfe.printStackTrace();
        }
        return null;
    }
    
    private EstadisticaJugadorPartido(final String[] split){
        //    idPartido;cantJugadores;cantBarajas;cantBazasJugadas;puntos;puesto;PEDIDO;LLEVADO;...;
        _idPartido = split[0];
        _cantJugadores = Integer.valueOf(split[1]);
        _cantBarajas = Integer.valueOf(split[2]);
        _cantBazasJugadas = Integer.valueOf(split[3]);
        _puntosObtenidos = Integer.valueOf(split[4]);
        _puesto = Integer.valueOf(split[5]);
        _bazas = new ArrayList<>();
        if(_puesto != -1){
            for(int i=6;i < 6 + 2*_cantBazasJugadas;i+=2){
                final int pedido = Integer.valueOf(split[i]);
                final int llevado = Integer.valueOf(split[i+1]);
                final Baza baza = new Baza(pedido,llevado);
                _bazas.add(baza);
                _bazasTotalesPedidas += pedido;
                _bazasTotalesLlevadas += llevado;
                if(pedido == llevado){
                    _cumplidas++;
                } else {
                    _falladas++;
                }
            }
        }
    }

    public EstadisticaJugadorPartido(final String idPartida, final int cantJugadores, final int cantBarajas, final int bazasJugadas, final int puntaje, int puesto, final List<Baza> bazas) {
        _idPartido = idPartida;
        _cantJugadores = cantJugadores;
        _cantBarajas = cantBarajas;
        _cantBazasJugadas = bazasJugadas;
        _puntosObtenidos = puntaje;
        _puesto = puesto;
        _bazas = bazas;
    }

//    public JsonObject toJsonObject(){
//        final JsonObject jo = new JsonObject();
//        jo.addProperty("idPartido",_idPartido);
////        jo.addProperty("cantJugadores",_cantJugadores);
////        jo.addProperty("cantBarajas",_cantBarajas);
////        jo.addProperty("puntosObtenidos",_puntosObtenidos);
////        jo.addProperty("puesto",_puesto);
////        jo.addProperty("bazasTotalesLlevadas",_bazasTotalesLlevadas);
////        jo.addProperty("bazasTotalesPedidas",_bazasTotalesPedidas);
//        return jo;
//    }

    public String getIdPartido() {
        return _idPartido;
    }

    public int getCantJugadores() {
        return _cantJugadores;
    }

    public int getCantBarajas() {
        return _cantBarajas;
    }

    public int getPuntosObtenidos() {
        return _puntosObtenidos;
    }

    public int getCantBazasJugadas() {
        return _cantBazasJugadas;
    }

    public int getPuesto() {
        return _puesto;
    }

    public int getBazasTotalesLlevadas() {
        return _bazasTotalesLlevadas;
    }

    public int getBazasTotalesPedidas() {
        return _bazasTotalesPedidas;
    }

    public int getCumplidas() {
        return _cumplidas;
    }

    public int getFalladas() {
        return _falladas;
    }

    public String getAsWritableString() {
        //    idPartido;cantJugadores;cantBarajas;cantBazasJugadas;puntos;puesto;PEDIDO;LLEVADO;...;
        final StringBuilder sb = new StringBuilder();
        sb.append(_idPartido).append(SEMICOLON);
        sb.append(_cantJugadores).append(SEMICOLON);
        sb.append(_cantBarajas).append(SEMICOLON);
        sb.append(_cantBazasJugadas).append(SEMICOLON);
        sb.append(_puntosObtenidos).append(SEMICOLON);
        sb.append(_puesto).append(SEMICOLON);
        for(final Baza baza : _bazas){
            sb.append(baza.getPedido()).append(SEMICOLON);
            sb.append(baza.getLlevado()).append(SEMICOLON);
        }
        return sb.toString();
    }
    
}
