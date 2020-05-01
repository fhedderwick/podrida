package podrida.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    
    private final String _username;
    private final List<String> _history;
    private String _token;
    private final Long _loggedSince;
    private final boolean _guest;

    public User(final String username) {
        this(username,new ArrayList<>(),true);
    }

    public User(final String username, final List<String> history) {
        this(username, history, false);
    }
    
    private User(final String username, final List<String> history, final boolean guest) {
        _username = username;
        _history = history;
        _guest = guest;
        _loggedSince = System.currentTimeMillis();
    }

    public String getUsername() {
        return _username;
    }

//    public List<String> getHistory() {
//        return _history;
//    }

    public void setToken(final String token) {
        _token = token;
    }
    
    public boolean isGuest(){
        return _guest;
    }

    public String getToken() {
        return _token;
    }

}
