package podrida.model;

import java.util.List;
import podrida.model.estadistica.EstadisticasUsuario;

public class User {
    
    private final String _username;
    private final EstadisticasUsuario _estadisticasUsuario;
    private String _token;
    private final Long _loggedSince;

    public User(final String username, final List<String> estadistica) {
        _username = username;
        _estadisticasUsuario = new EstadisticasUsuario(username,estadistica);
        _loggedSince = System.currentTimeMillis();
    }
    
    public EstadisticasUsuario getEstadistica(){
        return _estadisticasUsuario;
    }

    public String getUsername() {
        return _username;
    }

    public void setToken(final String token) {
        _token = token;
    }
    
    public String getToken() {
        return _token;
    }
    
}
