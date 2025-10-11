package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatServer {

    private final int port;
    private final ServerGUI gui;
    private ServerSocket serverSocket;
    private final AtomicBoolean isRunning = new AtomicBoolean(false); 
    
    private final List<ClientHandler> clients = new ArrayList<>(); 

    public ChatServer(int port, ServerGUI gui) {
        this.port = port;
        this.gui = gui;
    }
    
    public ServerGUI getGui() {
        return gui;
    }

    public void startServer() throws IOException {
        if (isRunning.get()) {
            gui.logMessage("Hata: Sunucu zaten çalışıyor.");
            return;
        }

        try {
            serverSocket = new ServerSocket(port);
            isRunning.set(true);
            gui.logMessage("Sunucu baslatildi, port: " + port);

            while (isRunning.get()) {
                gui.logMessage("Yeni baglanti bekleniyor...");
                Socket clientSocket = serverSocket.accept();
                
                ClientHandler newClient = new ClientHandler(clientSocket, this);
                clients.add(newClient); 
                newClient.start();      

                gui.logMessage("Yeni istemci baglandi: " + clientSocket.getInetAddress().getHostAddress() + ". Toplam istemci: " + clients.size());
            }
        } 
        catch (IOException e) { 
            if (isRunning.get()) {
                gui.logMessage("Sunucu dinleme hatası: " + e.getMessage());
            }
        } finally { 
            isRunning.set(false);
            closeServer();
        }
    }

    public void stopServer() {
        if (!isRunning.get()) return;

        isRunning.set(false); 
        gui.logMessage("Sunucu kapatılıyor...");
        
        closeServer();
        
        for (ClientHandler client : clients) {
        }
        clients.clear();
    }
    
    public boolean isRunning() {
        return isRunning.get();
    }
    
    private void closeServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            gui.logMessage("Sunucu kapatılırken hata: " + e.getMessage());
        }
        gui.logMessage("Sunucu başarıyla kapatıldı.");
    }


    public void removeClient(ClientHandler client) {
        clients.remove(client);
        gui.logMessage("İstemci ayrıldı. Kalan istemci: " + clients.size());
    }

    public void handleIncomingMessage(String encryptedMessage, ClientHandler sender) {
        gui.logMessage("Gelen Şifreli Mesaj [" + sender.getClientId() + "]: " + encryptedMessage);
        
        broadcast(encryptedMessage); 
    }
    
    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message); 
        }
    }
}