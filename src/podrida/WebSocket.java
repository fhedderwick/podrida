package podrida;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import podrida.managers.GameManager;
import podrida.model.Carta;
import podrida.model.Espectador;
import podrida.model.Jugador;
import podrida.utils.Utils;

public class WebSocket {

    public synchronized static void attendSocket(final Socket socket, final GameManager gameManager) throws Exception{
        final ClientThread clientThread = handshake(socket, gameManager);
        gameManager.registerThread(clientThread);
    }
    
    private static ClientThread handshake(final Socket socket, final GameManager gameManager) throws Exception{
        System.out.println("A client connected.");
        final InputStream in = socket.getInputStream();
        final OutputStream out = socket.getOutputStream();
        final Scanner s = new Scanner(in, "UTF-8");
        final String data = s.useDelimiter("\\r\\n\\r\\n").next();
        final Matcher get = Pattern.compile("^GET").matcher(data);
        if (get.find()) {
            final Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
            match.find();
            final byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                    + "Connection: Upgrade\r\n"
                    + "Upgrade: websocket\r\n"
                    + "Sec-WebSocket-Accept: "
                    + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes("UTF-8")))
                    + "\r\n\r\n").getBytes("UTF-8");
            out.write(response, 0, response.length);
            out.flush();
        }
        return new ClientThread(socket,in,out, gameManager);
    }
    
    public static void broadcast(final List<ClientThread> destinatarios, final JsonObject msg){
        broadcast(destinatarios, msg.toString());
    }
    
    public static void broadcast(final List<ClientThread> destinatarios, final String msg){
        System.out.println("Broadcasting: " + msg);
        for(final ClientThread clientThread : destinatarios){
            try{
                clientThread.write(msg);
            }catch(final Exception e){
                e.printStackTrace();
            }
        }
    }
    
    public static void broadcastToSpectators(final int bazasEstaRonda, final List<Espectador> espectadores, final JsonObject reply){
        final int turnoActual = reply.get("content").getAsInt();
            try{
                final JsonArray ja = new JsonArray();
                for(int i=0; i<bazasEstaRonda; i++){
                    ja.add("unknown");
                }
                final JsonObject content = new JsonObject();
                content.addProperty("turnoActual",turnoActual);
                content.add("cartas", ja);
                reply.addProperty("content",content.toString());
                for(final Espectador espectador : espectadores){
                    if(espectador.isOnline()){
                        System.out.println("Enviando a espectador en " + espectador.getRemoteAddress() + ": " + reply.toString());
                        espectador.getClientThread().write(reply.toString());
                    }
                }
            }catch(final Exception e){
                e.printStackTrace();
            }
    }

    public static void broadcastCartas(final List<Jugador> jugadores, final JsonObject reply) {
        final int turnoActual = reply.get("content").getAsInt();
        for(final Jugador jugador : jugadores){
            try{
                final JsonArray ja = new JsonArray();
                for(final Carta carta : jugador.verCartas()){
                    ja.add(carta.getId());
                }
                final JsonObject jsonToPlayer = Utils.copyJsonReply(reply);
                final JsonObject content = new JsonObject();
                content.addProperty("turnoActual",turnoActual);
                content.add("cartas", ja);
                jsonToPlayer.addProperty("content",content.toString());
                System.out.println("Enviando a " + jugador.getUsername() + ": " + jsonToPlayer.toString());
                jugador.getClientThread().write(jsonToPlayer.toString());
            }catch(final Exception e){
                e.printStackTrace();
            }
        }
    }
    
    public static void broadcastCartasIndias(final List<Jugador> jugadores, final JsonObject reply) {
        final int turnoActual = reply.get("content").getAsInt();
        final JsonObject cartasJugadores = new JsonObject();
        for(final Jugador jugador : jugadores){
            cartasJugadores.addProperty(jugador.getUsername(), jugador.verCartas().get(0).getId());
        }
        for(final Jugador jugador : jugadores){
            try{
                final JsonObject copiaSinJugador = crearCopiaSinJugador(jugador.getUsername(),cartasJugadores);
                final JsonObject jsonToPlayer = Utils.copyJsonReply(reply);
                final JsonObject content = new JsonObject();
                content.addProperty("turnoActual",turnoActual);
                content.addProperty("rondaIndia", true);
                content.add("cartasJugadores", copiaSinJugador);
                jsonToPlayer.addProperty("content", content.toString());
                System.out.println("Enviando a " + jugador.getUsername() + ": " + jsonToPlayer.toString());
                jugador.getClientThread().write(jsonToPlayer.toString());
            }catch(final Exception e){
                e.printStackTrace();
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
