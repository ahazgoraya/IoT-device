import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.net.HttpURLConnection;


public class IFTTTServer {
  public Map<String,String> events = new HashMap<String,String>();
  public final String PAGESTART = "<html><body><center><h1>Known values</h1></center><body><center><table>";
  public final String PAGEEND = "</table></center></body></html>";

  public class Handler implements HttpHandler {
    public void handle(HttpExchange xchg) throws IOException {
      if (xchg.getRequestMethod().equalsIgnoreCase("GET")) {
        try {
          StringBuffer sb = new StringBuffer(PAGESTART);
          synchronized(events) {
            for(String s: events.keySet()) 
              sb.append("<tr><td>" + s + "</td><td>" + events.get(s) + "</td></tr>");
          }
          String s = sb.toString();
          xchg.sendResponseHeaders(HttpURLConnection.HTTP_OK, s.length());
          OutputStream os = xchg.getResponseBody();
          os.write(s.getBytes());
          os.close();
          xchg.close();
          return;
        } catch(Exception e) {
          System.out.println("Error " + e);
          return;
        }
      }
          
      if (xchg.getRequestMethod().equalsIgnoreCase("POST")) {
        try {
          Headers requestHeaders = xchg.getRequestHeaders();
  
          // expect POST as a JSON
          URI uri = xchg.getRequestURI();
          String kind = requestHeaders.getFirst("Content-type");
          if(!"application/json".equals(kind)) {
          System.out.println("bad application kind " + kind);
            xchg.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
            xchg.close();
            return;
          } 
  
          // expect URI in IFTTT format
          String[] parts = uri.toString().split("/");
          if(parts.length < 2) {
          System.out.println("bad application uri " + uri);
            xchg.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
            xchg.close();
            return;
          } 
      
          // obtain POST text as a string
          int contentLength = Integer.parseInt(requestHeaders.getFirst("Content-length"));
          InputStream is = xchg.getRequestBody();
          byte[] data = new byte[contentLength];
          int length = is.read(data);
          String s = new String(data);
          System.out.println(s);

          // update the map
          synchronized(events) {
            events.put(uri.toString(), s);
          }
  
          xchg.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
          xchg.close();
   
          } catch (Exception e) {
            System.out.println("Error " + e);
          }
      }
    }
  }

  public void serve(int port) throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
    Handler handler = new Handler();
    server.createContext("/", new Handler());
    server.start();
  }

  public static void main(String[] args) throws IOException {
    final int PORT = 8080;
    int port = PORT;
    switch(args.length) {
    case 0:
      break;
    case 1:
      port = Integer.parseInt(args[0]);
      break;
    default :
      System.out.println("Usage: IFTTTServer [port]");
      System.exit(1);
    }
    (new IFTTTServer()).serve(port);
    /*NOTREACHED*/
  }
}
