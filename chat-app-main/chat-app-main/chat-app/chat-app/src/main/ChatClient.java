package main;

import java.io.*;
import java.net.*;

public class ChatClient {
    public Socket getSocket() {
        return socket;
    }
    private Socket socket;
    private PrintWriter out;

    public void connect(String host, int port) throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), 3000);
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
        out = null;
    }

    public void sendMessage(String message) {
        if (out != null) out.println(message);
    }

    public void sendFile(String fileName, String mimeType, String base64Data) {
        if (out != null) {
            out.println("FILE:" + fileName + ":" + mimeType + ":" + base64Data);
        }
    }
}
