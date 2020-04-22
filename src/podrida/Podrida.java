package podrida;

import podrida.utils.Utils;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Podrida {

    private static int port = 8000;
    private static int wsPort = 8001;
    private static int maxConnections = 10;
    
    public static void main(final String[] args) throws Exception {
        if(args.length > 0){
            port = Utils.parsearNumero(args[0]);
            if(port < 0){
                return;
            }
        } 
        
        final HttpServer server = HttpServer.create(new InetSocketAddress(port), maxConnections);
        server.createContext("/podrida", new PodridaHandler(maxConnections));
        server.setExecutor(null); // creates a default executor
        server.start();
        
        final GameManager gameManager = new GameManager(maxConnections);
        final ServerSocket serverSocket = new ServerSocket(wsPort, maxConnections);
            try {
                
                boolean running = true;
                System.out.println("Server has started on 127.0.0.1:" + wsPort + ".\r\nWaiting for a connection...");
                while (running) {
                    try {
                        final Socket client = serverSocket.accept();
                        WebSocket.attendSocket(client,gameManager);
                    } catch (final IOException e) {
                        System.out.println("I/O error: ");
                        e.printStackTrace();
                    }
                }
            } finally {
                serverSocket.close();
            }
    }
        
}
