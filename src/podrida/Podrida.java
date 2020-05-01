package podrida;

import podrida.managers.PodridaManager;
import podrida.handlers.PodridaHandler;
import podrida.utils.Utils;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import podrida.handlers.DefaultHandler;
import podrida.handlers.LoginHandler;
import podrida.handlers.NumerosHandler;
import podrida.handlers.RegisterHandler;
import podrida.handlers.UnregisterHandler;
import podrida.managers.UserManager;
import podrida.utils.MensajesEstandar;

public class Podrida {

    private static int port = 8000;
    private static int wsPort = 8001;
    private static int maxConnections = 10;
    
    public static void main(final String[] args) throws Exception {
        if(args.length == 2){
            port = Utils.parsearNumero(args[0]);
            wsPort = Utils.parsearNumero(args[1]);
            if(port < 0 || wsPort < 0){
                System.out.println("Error en los parametros");
                return;
            }
        } 
        MensajesEstandar.loadMessages("resources/mensajes.txt");
        if(!MensajesEstandar.isLoaded()){
            System.out.println("No se encontro el archivo de mensajes");
            return;
        }
        final UserManager userManager = new UserManager("resources/users");
        final PodridaManager gameManager = new PodridaManager(maxConnections, userManager);
        final HttpServer server = HttpServer.create(new InetSocketAddress(port), maxConnections);
        server.createContext("/", new DefaultHandler());
        server.createContext("/login", new LoginHandler(userManager));
        server.createContext("/register", new RegisterHandler(userManager));
        server.createContext("/unregister", new UnregisterHandler(userManager));
        server.createContext("/numeros", new NumerosHandler());
        server.createContext("/podrida", new PodridaHandler(wsPort,maxConnections,userManager));
        server.setExecutor(null); // creates a default executor
        server.start();
        
        //creo que si se bajan todos se rompe
        
        final ServerSocket podridaSocket = new ServerSocket(wsPort, maxConnections);
        try {
            boolean running = true;
            System.out.println("Server has started on 127.0.0.1:" + wsPort + ".\r\nWaiting for a connection...");
            while (running) {
                try {
                    final Socket client = podridaSocket.accept();
                    WebSocket.attendSocket(client,gameManager);
                } catch (final Exception e) {
                    System.out.println("I/O error: ");
                    e.printStackTrace();
                }
            }
        } finally {
            podridaSocket.close();
        }
    }
        
}
