package podrida;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import podrida.model.Carta;
import podrida.model.Jugador;

public class WebSocket {

    private static final Map<ClientThread,Jugador> _clients = new HashMap<>();

    public static void attendSocket(final Socket client, final GameManager gameManager) throws IOException, NoSuchAlgorithmException{
        System.out.println("A client connected.");
        InputStream in = client.getInputStream();
        OutputStream out = client.getOutputStream();
        Scanner s = new Scanner(in, "UTF-8");
        String data = s.useDelimiter("\\r\\n\\r\\n").next();
        Matcher get = Pattern.compile("^GET").matcher(data);
        if (get.find()) {
            Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
            match.find();
            byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                    + "Connection: Upgrade\r\n"
                    + "Upgrade: websocket\r\n"
                    + "Sec-WebSocket-Accept: "
                    + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes("UTF-8")))
                    + "\r\n\r\n").getBytes("UTF-8");
            out.write(response, 0, response.length);
        }
        final ClientThread thread = new ClientThread(client,in,out, gameManager);
        _clients.put(thread,null);
        thread.start();
    }
    
    public static Jugador removeClient(final ClientThread client){
        return _clients.remove(client);
    }

    public static void broadcast(final List<Jugador> destinatarios, final JsonObject msg){
        broadcast(destinatarios, msg.toString());
    }
    
    public static void broadcast(final List<Jugador> destinatarios, final String msg){
        System.out.println("Broadcasting: " + msg);
        for(final Entry<ClientThread,Jugador> entry : _clients.entrySet()){
            if(destinatarios.contains(entry.getValue())){
                try{
                    final ClientThread clientThread = entry.getKey();
                        clientThread.write(msg);
                }catch(final Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    static void registerPlayer(final ClientThread invoker, final Jugador jugador) {
        invoker.setClientName(jugador.getNombre());
        _clients.put(invoker,jugador);
        System.out.println("Registrado a " + jugador.getNombre() + " en " + invoker.getRemoteAddress());
    }

    static void broadcastCartas(final List<Jugador> jugadores, final JsonObject reply) {
        final JsonObject content = reply.get("content").getAsJsonObject();
        for(final Entry<ClientThread,Jugador> entry : _clients.entrySet()){
            if(jugadores.contains(entry.getValue())){
                try{
                    final JsonArray ja = new JsonArray();
                    for(final Carta carta : entry.getValue().verCartas()){
                        ja.add(carta.getId());
                    }
                    //cuidado, que este mismo reply es el que se le manda a todos!!
                    content.add("cartas", ja); //reemplaza el que le mandaron al anterior jugador destinatario
                    System.out.println("Enviando a " + entry.getValue().getNombre() + ": " + reply.toString());
                    entry.getKey().write(reply.toString());
                }catch(final Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    
    static void broadcastCartasIndias(final List<Jugador> jugadores, final JsonObject reply) {
        final JsonObject content = reply.get("content").getAsJsonObject();
        content.addProperty("rondaIndia", true);
        final JsonObject cartasJugadores = new JsonObject();
        for(final Entry<ClientThread,Jugador> entry : _clients.entrySet()){
            if(jugadores.contains(entry.getValue())){
                cartasJugadores.addProperty(entry.getValue().getNombre(), entry.getValue().verCartas().get(0).getId());
            }
        }
        for(final Entry<ClientThread,Jugador> entry : _clients.entrySet()){
            if(jugadores.contains(entry.getValue())){
                try{
                    //cuidado, que este mismo reply es el que se le manda a todos!!
                    final JsonObject copiaSinJugador = crearCopiaSinJugador(entry.getValue().getNombre(),cartasJugadores);
                    content.add("cartasJugadores", copiaSinJugador);
                    System.out.println("Enviando a " + entry.getValue().getNombre() + ": " + reply.toString());
                    entry.getKey().write(reply.toString());
                }catch(final Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    private static JsonObject crearCopiaSinJugador(final String nombre, final JsonObject cartasJugadores) {
        final JsonObject reply = new JsonObject();
        for(final Entry<String, JsonElement> entry : cartasJugadores.entrySet()){
            if(!entry.getKey().equals(nombre)){
                reply.add(entry.getKey(),entry.getValue());
            }
        }
        return reply;
    }
    
}
