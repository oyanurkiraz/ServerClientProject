package main;

public class ChatServerApp {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ServerGUI(5000).setVisible(true); 
            }
        });
    }
}