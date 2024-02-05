package edu.arep.taller;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.io.*;
/**
 * The HttpServer class represents a simple HTTP server.
 */
public class HttpServer {
    
    private ServerSocket serverSocket;
    private boolean running;
    /**
     * Constructs a new HttpServer object.
     */
    public HttpServer() {
        this.serverSocket = null;
        this.running = true;
    }
    /**
     * The main method to start the HTTP server.
     *
     * @param args command-line arguments
     * @throws URISyntaxException 
     */
    public static void main(String[] args) throws URISyntaxException {
        HttpServer httpServer = new HttpServer();
        httpServer.start(35000);
    }
    /**
     * Starts the HTTP server on the specified port.
     *
     * @param port the port number to listen on
     * @throws URISyntaxException 
     */
    public void start(int port) throws URISyntaxException {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + port);
            e.printStackTrace();
            System.exit(1);
        }

        while (running) {
            handleClientRequest();
        }
    }
    /**
     * Handles the client request.
     * @throws URISyntaxException 
     */
    private void handleClientRequest() throws URISyntaxException {
        try (Socket clientSocket = serverSocket.accept();
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

            String inputLine, outputLine;
            boolean firstLine = true;
            String uriStr = "";

            while ((inputLine = in.readLine()) != null) {
                if (firstLine) {
                    uriStr = inputLine.split(" ")[1];
                    firstLine = false;
                }
                System.out.println("Received: " + inputLine);
                if (!in.ready()) {
                    break;
                }
            }

            URI requestUri = new URI(uriStr);

            try {
                outputLine = httpResponse(requestUri);
            }catch (Exception e){
                outputLine = httpError();
            }
            out.println(outputLine);

        } catch (IOException e) {
            System.err.println("Error handling client request: " + e.getMessage());
        }
    }
    /**
     * Stops the HTTP server.
     */
    public void stop() {
        running = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
    }
    
    /**
     * Generates HTML content for the HTTP response.
     *
     * @return the HTML content
     */
    public static String httpResponse(URI uriResquest) throws IOException {

        String outputLine = "HTTP/1.1 200 OK\r\n"
                + "Content-Type:text/html\r\n"
                + "\r\n";
        
        Path file = Paths.get("target/classes/edu/arep/taller/resources" + uriResquest.getPath());

        Charset charset = Charset.forName("UTF-8");

        BufferedReader reader = Files.newBufferedReader(file, charset);

        String line = null;

        while ((line = reader.readLine()) != null) {
            System.out.println(line);
            outputLine += line;
        }


        return outputLine;

    }
    private static String httpError() {
        String outputLine = "HTTP/1.1 400 Not Found\r\n"
                + "Content-Type:text/html\r\n"
                + "\r\n"
                + "<!DOCTYPE html>\n"
                + "<html>\n"
                + "    <head>\n"
                + "        <title>Error Not found</title>\n"
                + "        <meta charset=\"UTF-8\">\n"
                + "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "    </head>\n"
                + "    <body>\n"
                + "        <h1>Error</h1>\n"
                + "    </body>\n";
        return outputLine;

    }

}