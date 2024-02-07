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
    public void handleClientRequest() throws URISyntaxException {
        try (Socket clientSocket = serverSocket.accept();
             OutputStream outputStream = clientSocket.getOutputStream();
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
    
            String inputLine;
            String uriStr = "";
    
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.isEmpty()) {
                    break;
                }
                if (uriStr.isEmpty()) {
                    uriStr = inputLine.split(" ")[1];
                }
            }
    
            URI requestUri = new URI(uriStr);
    
            try {
                httpResponse(requestUri, outputStream);
            } catch (Exception e) {
                e.printStackTrace();
                String errorResponse = httpError();
                outputStream.write(errorResponse.getBytes());
                outputStream.flush();
            }
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
     * @param uriRequest the requested URI
     * @param outputStream the output stream to write the response to
     * @throws IOException if an I/O error occurs
     */
    public static void httpResponse(URI uriRequest, OutputStream outputStream) throws IOException {
        Path filePath = Paths.get("src/main/resources" + uriRequest.getPath());
        byte[] fileBytes = Files.readAllBytes(filePath);
    
        String contentType;
        if (uriRequest.getPath().contains("html")) {
            contentType = "text/html";
        } else if (uriRequest.getPath().contains("css")) {
            contentType = "text/css";
        } else if (uriRequest.getPath().endsWith("js")) {
            contentType = "application/javascript";
        } else if (uriRequest.getPath().endsWith("png")) {
            contentType = "image/png";
        }else {
            contentType = "text/plain";
        }
    
        String responseHeader = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + contentType + "\r\n" +
                                "Content-Length: " + fileBytes.length + "\r\n" +
                                "\r\n";
        
        outputStream.write(responseHeader.getBytes());
        outputStream.write(fileBytes);
        outputStream.flush();
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