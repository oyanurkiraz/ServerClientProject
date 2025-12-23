package main;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class ServerGUI extends JFrame {

    // Modern Koyu Tema Renkleri
    private static final Color BG_DARK = new Color(26, 26, 46);
    private static final Color BG_PANEL = new Color(22, 33, 62);
    private static final Color BG_INPUT = new Color(15, 52, 96);
    private static final Color ACCENT = new Color(233, 69, 96);
    private static final Color TEXT_PRIMARY = new Color(234, 234, 234);
    private static final Color TEXT_SECONDARY = new Color(160, 160, 180);
    private static final Color SUCCESS = new Color(46, 204, 113);
    private static final Color DANGER = new Color(231, 76, 60);
    private static final Color BORDER_COLOR = new Color(40, 50, 80);

    private final ChatServer server;
    private final int port;

    private final JTextArea logArea = new JTextArea();
    private JButton startStopButton;
    private JLabel statusIndicator;

    public ServerGUI(int portNumber) {

        this.port = portNumber;
        this.server = new ChatServer(this.port, this);

        initializeGUI();

        startStopButton.addActionListener(e -> toggleServer());

        logMessage("🔧 Sunucu hazır. Başlatmak için butona tıklayın.");
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            private Color currentBg = bgColor;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(currentBg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(currentBg.brighter());
                } else {
                    g2.setColor(currentBg);
                }

                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));

                g2.setColor(TEXT_PRIMARY);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);

                g2.dispose();
            }

            public void setButtonColor(Color color) {
                this.currentBg = color;
                repaint();
            }
        };
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(TEXT_PRIMARY);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(180, 45));
        return button;
    }

    private void initializeGUI() {
        setTitle("🖥️ Chat Sunucusu (Port: " + this.port + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_DARK);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_PANEL);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Title and Status
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("🖥️ Server Control Panel");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_PRIMARY);
        titlePanel.add(titleLabel);

        // Status Indicator
        statusIndicator = new JLabel("● Çevrimdışı") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        statusIndicator.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusIndicator.setForeground(DANGER);
        titlePanel.add(statusIndicator);

        headerPanel.add(titlePanel, BorderLayout.WEST);

        // Control Button
        startStopButton = createStyledButton("▶ Sunucuyu Başlat", SUCCESS);
        headerPanel.add(startStopButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Log Area
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(BG_DARK);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Log Header
        JPanel logHeader = new JPanel(new BorderLayout());
        logHeader.setBackground(BG_DARK);
        logHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel logLabel = new JLabel("📋 Sunucu Logları");
        logLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logLabel.setForeground(TEXT_SECONDARY);
        logHeader.add(logLabel, BorderLayout.WEST);

        JButton clearBtn = new JButton("🗑️ Temizle");
        clearBtn.setBackground(BG_INPUT);
        clearBtn.setForeground(TEXT_SECONDARY);
        clearBtn.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        clearBtn.setFocusPainted(false);
        clearBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearBtn.addActionListener(e -> logArea.setText(""));
        logHeader.add(clearBtn, BorderLayout.EAST);

        centerPanel.add(logHeader, BorderLayout.NORTH);

        logArea.setEditable(false);
        logArea.setBackground(new Color(15, 15, 25));
        logArea.setForeground(new Color(0, 255, 136)); // Terminal yeşili
        logArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        logArea.setCaretColor(SUCCESS);
        logArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(new LineBorder(BORDER_COLOR, 2, true));
        scrollPane.getViewport().setBackground(new Color(15, 15, 25));

        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(BG_PANEL);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel footerLabel = new JLabel("🔐 Secure Chat Server v1.0 | Port: " + this.port);
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerLabel.setForeground(TEXT_SECONDARY);
        footerPanel.add(footerLabel);

        add(footerPanel, BorderLayout.SOUTH);

        // Center window
        setLocationRelativeTo(null);
    }

    private void toggleServer() {
        if (!server.isRunning()) {
            // UI'ı hemen güncelle - sunucu başlamadan önce
            startStopButton.setText("■ Sunucuyu Durdur");
            statusIndicator.setText("● Çevrimiçi");
            statusIndicator.setForeground(SUCCESS);
            startStopButton.setEnabled(true);

            new Thread(() -> {
                try {
                    server.startServer();
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        logMessage("❌ Sunucu başlatılırken hata: " + e.getMessage());
                    });
                }
                // Sunucu durduğunda buraya gelir
                SwingUtilities.invokeLater(() -> {
                    startStopButton.setText("▶ Sunucuyu Başlat");
                    statusIndicator.setText("● Çevrimdışı");
                    statusIndicator.setForeground(DANGER);
                });
            }, "Server-Thread").start();
        } else {
            server.stopServer();
            logMessage("🛑 Sunucu DURDURULDU.");
        }
    }

    public void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = java.time.LocalTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}