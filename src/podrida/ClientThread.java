package podrida;

import podrida.managers.PodridaManager;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import podrida.managers.GameManager;
import podrida.model.User;

public class ClientThread extends Thread{

    final Socket _socket;
    private final OutputStream _out;
    private final InputStream _in;
    private final PodridaManager _gameManager;
    private User _user;
    private final String _remoteAddress;
    private final String _clientIp;
    private final int _port;
    private boolean _connected;
    
    public ClientThread(final Socket socket, final InputStream in, final OutputStream out, final GameManager gameManager) {
        _socket = socket;
        _remoteAddress = _socket.getRemoteSocketAddress().toString();
        _clientIp = _socket.getInetAddress().toString();
        _port = _socket.getPort();
        _in = in;
        _out = out;
        _gameManager = (PodridaManager) gameManager;
    }
    
    @Override
    public void run() {
        int read;
        int index = 0;
        Frame frame = null;
        try {
            while ((read = _in.read()) != -1) {
                if(index == 0){
                    if(frame == null || !frame.continues()){
                        frame = new Frame(read);
                    }else{
                        frame.addFrame(new Frame(read));
                    }
                    index++;
                    continue;
                } else {
                    frame.addByte(read);
                }
                if(!frame.isFinished()){
                    index++;
                } else {
                    index = 0;
                    if(!frame.continues()){
                        frame.mergePayloads();
                        processFrame(frame);
                        frame = null;
                    }
                }
            }
            System.out.println("Client at " + _remoteAddress + " disconnected OK");
            _gameManager.desconexion(this);
        } catch (final IOException e) {
//            line=this.getName(); //reused String line for getting thread name
            e.printStackTrace();
            System.out.println("IO Error/ Client at " + _remoteAddress + " terminated abruptly");
            _gameManager.desconexion(this);
        }
    }
    
    private void processFrame(final Frame frame) throws IOException{
        switch(frame.getOpcode()){
            case 0x0: attendContinuation(); break; //es decir, es el mismo opcode que el anterior
            case 0x1: attendText(frame.getPayload()); break;
            case 0x2: attendBinary(); break;
            case 0x8: attendClose(); break;
            case 0x9: attendPing(); break;
            case 0xA: attendPong(); break;
            default: attendUnknownOpcode();
        };
    }
    
    private void attendContinuation(){
        
    }
    private void attendText(final String framePayload) throws IOException{
        final JsonObject jo = _gameManager.processRequest(this,framePayload);
        if(jo != null){
            System.out.println("Enviando a " + (_user != null ? _user.getUsername() : "(desconocido)") + ": " + jo.toString());
            write(jo.toString());
        }
    }
    private void attendBinary(){
        
    }
    private void attendClose()throws IOException{
        _socket.close();
//        todo
    }
    
    private void attendPing(){
        
    }
    private void attendPong(){
        
    }
    private void attendUnknownOpcode(){
        
    }
    
    public synchronized void write(final String string) throws IOException{
        final List<byte[]> frames = Frame.framize(string);
        if(frames.size() > 1){
            System.out.println("Message will be sent in " + frames.size() + " frames.");
        }
        for(final byte[] serializedFrame : frames){
            _out.write(serializedFrame);
            _out.flush();
        }
    }
    
    public String getRemoteAddress() {
        return _remoteAddress;
    }

    public String getClientIp(){
        return _clientIp;
    }

    public User getUser() {
        return _user;
    }

    public void associateUser(final User user) {
        _user = user;
    }

    public String getUsername() {
        return _user.getUsername();
    }

    public String getUserToken() {
        return _user.getToken();
    }

    public boolean isConnected() {
        return _connected;
    }

    public void setConnected(final boolean val) {
        _connected = val;
    }
    
}
