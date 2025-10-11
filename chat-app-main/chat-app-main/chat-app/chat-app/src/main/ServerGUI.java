package main;

import javax.swing.*;
import java.awt.*;

public class ServerGUI extends JFrame {
    
    private final ChatServer server;
    private final int port; 
    
    private final JTextArea logArea = new JTextArea(); 
    private final JButton startStopButton = new JButton("Sunucuyu Baslat");

    public ServerGUI(int portNumber) {
        
        this.port = portNumber;

        this.server = new ChatServer(this.port, this); 
        
        initializeGUI(); 
    
        startStopButton.addActionListener(e -> toggleServer());
        
        logMessage("Sunucu hazir. Baslatmak icin butona tiklayin.");
    }
    
    private void initializeGUI() {
        setTitle("Chat Sunucusu (Port: " + this.port + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLayout(new BorderLayout());

        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);
        
        JPanel northPanel = new JPanel(new FlowLayout());
        northPanel.add(startStopButton);
        add(northPanel, BorderLayout.NORTH);
    }
    
    private void toggleServer() {
        if (!server.isRunning()) {
            new Thread(() -> {
                try {
                    server.startServer();
                    SwingUtilities.invokeLater(() -> {
                        startStopButton.setText("Sunucuyu Durdur");
                        logMessage("### Sunucu baslatildi. baglantilar bekleniyor. ###");
                    });
                } catch (Exception e) {
                    logMessage("Sunucu baslatilirken hata: " + e.getMessage());
                }
            }).start();
        } else {
            server.stopServer();
            startStopButton.setText("Sunucuyu Baslat");
            logMessage("### Sunucu DURDURULDU. ###");
        }
    }

    public void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
        });
    }
}