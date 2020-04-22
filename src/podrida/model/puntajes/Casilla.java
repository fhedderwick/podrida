package podrida.model.puntajes;

import com.google.gson.JsonObject;

public class Casilla{
    private int _pedido;
    private int _bazas;
    private int _acumulado;
    private boolean _tachado;
    private String _nombre;
    private int _rondaBazas;
    private boolean _puntera;
    private boolean _ultima;
    
    public Casilla(){
        _pedido = 0;
        _bazas = 0;
        _acumulado = 0;
        _tachado = false;
        _nombre = "";
        _rondaBazas = 0;
        _puntera = false;
        _ultima = false;
    }

    public void setPedido(final int pedido) {
        this._pedido = pedido;
    }

    public void setBazas(final int bazas) {
        this._bazas = bazas;
    }

    public void setAcumulado(final int acumulado) {
        this._acumulado = acumulado;
    }

    public void setTachado(final boolean tachado) {
        this._tachado = tachado;
    }

    public String getNombre() {
        return _nombre;
    }

    public void setNombre(final String nombre) {
        this._nombre = nombre;
    }

    public JsonObject getAsJsonObject() {
        final JsonObject jo = new JsonObject();
        jo.addProperty("pedido", _pedido);
        jo.addProperty("bazas", _bazas);
        jo.addProperty("acumulado", _acumulado);
        jo.addProperty("tachado", _tachado);
        jo.addProperty("puntera", _puntera);
        jo.addProperty("ultima", _ultima);
        return jo;
    }

    public int getRondaBazas() {
        return _rondaBazas;
    }

    public void setRondaBazas(final int rondaBazas) {
        _rondaBazas = rondaBazas;
    }

    public int getAcumulado() {
        return _acumulado;
    }
    
    public void setPuntera(final boolean puntera){
        _puntera = puntera;
    }
    
    public void setUltima(final boolean ultima){
        _ultima = ultima;
    }
    
}
