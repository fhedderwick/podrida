package podrida.model;

import podrida.ClientThread;

public class Espectador {

    private final ClientThread _clientThread;
    
    public Espectador(final ClientThread clientThread) {
        _clientThread = clientThread;
    }
    
    public ClientThread getClientThread() {
        return _clientThread;
    }
    
    public boolean isOnline(){
        return _clientThread.isConnected();
    }
    
    public String getRemoteAddress(){
        return _clientThread.getRemoteAddress();
    }
    
}
