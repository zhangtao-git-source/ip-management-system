import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;

public class SimpleServer {
    public static void main(String[] args) throws Exception {
        int port = 8080;
        String dir = args.length > 0 ? args[0] : ".";
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new FileHandler(dir));
        server.setExecutor(null);
        System.out.println("Server started at http://localhost:" + port);
        System.out.println("Serving files from: " + new File(dir).getAbsolutePath());
        server.start();
    }

    static class FileHandler implements HttpHandler {
        String baseDir;
        FileHandler(String baseDir) { this.baseDir = baseDir; }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            File file = new File(baseDir, path.substring(1));
            if (!file.exists()) {
                String resp = "404 Not Found";
                exchange.sendResponseHeaders(404, resp.length());
                exchange.getResponseBody().write(resp.getBytes());
                exchange.getResponseBody().close();
                return;
            }
            String contentType = "text/html";
            if (path.endsWith(".css")) contentType = "text/css";
            else if (path.endsWith(".js")) contentType = "application/javascript";
            else if (path.endsWith(".png")) contentType = "image/png";
            exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
            byte[] bytes = Files.readAllBytes(file.toPath());
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.getResponseBody().close();
        }
    }
}
