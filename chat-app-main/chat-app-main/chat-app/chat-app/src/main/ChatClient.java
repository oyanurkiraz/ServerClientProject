package main;

import java.io.*;
import java.net.*;

public class ChatClient {
    private Socket socket;
    private BufferedOutputStream outputStream;
    private volatile boolean connected = false;

    public Socket getSocket() {
        return socket;
    }

    public void connect(String host, int port) throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), 3000);
        socket.setKeepAlive(true);
        socket.setTcpNoDelay(true);
        socket.setOOBInline(true); // OOB data inline olarak alınsın
        outputStream = new BufferedOutputStream(socket.getOutputStream());
        connected = true;
    }

    public boolean isConnected() {
        if (!connected)
            return false;
        if (socket == null || socket.isClosed() || !socket.isConnected()) {
            connected = false;
            return false;
        }

        // Aktif bağlantı testi - sendUrgentData bağlantı kopmuşsa hemen exception
        // fırlatır
        try {
            socket.sendUrgentData(0xFF);
            return true;
        } catch (IOException e) {
            connected = false;
            return false;
        }
    }

    public void setDisconnected() {
        connected = false;
    }

    public void disconnect() {
        connected = false;
        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException ignored) {
        }
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
        outputStream = null;
        socket = null;
    }

    public boolean sendMessage(String message) {
        // Önce bağlantıyı aktif olarak test et
        if (!isConnected()) {
            return false;
        }

        try {
            byte[] data = (message + "\n").getBytes("UTF-8");
            outputStream.write(data);
            outputStream.flush();
            return true;
        } catch (IOException e) {
            connected = false;
            return false;
        }
    }

    public boolean sendFile(String fileName, String mimeType, String base64Data) {
        return sendMessage("FILE:" + fileName + ":" + mimeType + ":" + base64Data);
    }
}
