package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class ClientHandler extends Thread {
    private final Socket socket;
    private final ChatServer server;
    private PrintWriter writer;
    private String clientId; 

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
        this.clientId = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
    }

    @Override
    public void run() {
        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String clientMessage;
            while ((clientMessage = reader.readLine()) != null) {
                if (clientMessage.startsWith("FILE:")) {
                    try {
                        String[] parts = clientMessage.split(":", 4);
                        if (parts.length == 4) {
                            String fileName = parts[1];
                            String mimeType = parts[2];
                            int size = parts[3].length();
                            server.getGui().logMessage("[Dosya geldi: " + fileName + ", tip: " + mimeType + ", şifreli boyut: " + size + "]");
                        }
                    } catch (Exception ex) {
                        server.getGui().logMessage("[Dosya log hatası: " + ex.getMessage() + "]");
                    }
                    server.broadcast(clientMessage); 
                } else {
                    server.handleIncomingMessage(clientMessage, this);
                }
            }
            
        } catch (IOException e) {
            server.getGui().logMessage("İstemci bağlantısı kesildi veya hata oluştu: " + e.getMessage());
        } finally {
            server.removeClient(this);
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }
    
    public String getClientId() {
        return clientId;
    }
}