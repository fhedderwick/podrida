package podrida.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;

public abstract class AbstractHandler implements HttpHandler{

    protected final static String GET = "GET";
    protected final static String POST = "POST";
    
    @Override
    public void handle(final HttpExchange t) throws IOException {
        final String responseAsString = processRequest(t, t.getRequestMethod());
        final byte[] bytesToSend = responseAsString.getBytes();
        t.sendResponseHeaders(200, bytesToSend.length);
        final OutputStream os = t.getResponseBody();
        os.write(bytesToSend);
        os.close();
    }
    
    protected abstract String processRequest(final HttpExchange httpExchange, final String method);

}
