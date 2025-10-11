package main;

import javax.swing.SwingUtilities;

public class ChatClientApp {
    public static void main(String[] args) {
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                    ClientGUI clientFrame = new ClientGUI();
                clientFrame.setVisible(true);
            }
        });
    }
}
