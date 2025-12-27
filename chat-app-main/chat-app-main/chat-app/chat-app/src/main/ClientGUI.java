package main;

import main.encryption.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.security.PublicKey;

public class ClientGUI extends JFrame {
    private final ChatClient client;

    // Modern Koyu Tema Renkleri
    private static final Color BG_DARK = new Color(26, 26, 46);
    private static final Color BG_PANEL = new Color(22, 33, 62);
    private static final Color BG_INPUT = new Color(15, 52, 96);
    private static final Color ACCENT = new Color(233, 69, 96);
    private static final Color ACCENT_HOVER = new Color(255, 99, 126);
    private static final Color TEXT_PRIMARY = new Color(234, 234, 234);
    private static final Color TEXT_SECONDARY = new Color(160, 160, 180);
    private static final Color SUCCESS = new Color(46, 204, 113);
    private static final Color BORDER_COLOR = new Color(40, 50, 80);

    private final JTextArea area = new JTextArea();
    private final JTextField input = new JTextField();
    private final JTextField keyField = new JTextField();

    private final JTextField portField = new JTextField("5000");
    private final JButton connectButton = createStyledButton("Bağlan", SUCCESS);
    private final JButton fileButton = createStyledButton("📁 Dosya", ACCENT);

    private final JComboBox<String> encryptionSelect = new JComboBox<>(new String[] {
            "Şifresiz Gönder",
            "--- AES/DES/RSA ---",
            "AES (Kütüphaneli)",
            "AES (Manuel)",
            "DES (Kütüphaneli)",
            "DES (Manuel)",
            "RSA",
            "--- Klasik ---",
            "AffineCipher",
            "SezarSifreleme",
            "SubstitutionCipher",
            "VigenereCipher",
            "RouteCipher",
            "ColumnarTransposition",
            "PolybiusCipher",
            "HillCipher",
            "GCDCipher"
    });

    private EncryptionAlgorithm selectedAlgorithm;

    // RSA Anahtar Değişimi için
    private RSACipher myRSA; // Kendi key pair'im
    private PublicKey peerPublicKey; // Karşı tarafın public key'i
    private RSACipher rsaForEncrypt; // Şifreleme için (karşı tarafın public key'i ile)
    private RSACipher rsaForDecrypt; // Çözme için (kendi private key'im ile)

    public ClientGUI() {
        this.client = new ChatClient();

        // RSA key pair'i başlangıçta oluştur
        this.myRSA = new RSACipher();
        this.rsaForDecrypt = new RSACipher(myRSA.getPublicKey(), myRSA.getPrivateKey());

        initializeGUI();
        updateAlgorithm();
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bgColor.brighter());
                } else {
                    g2.setColor(bgColor);
                }

                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));

                g2.setColor(TEXT_PRIMARY);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(TEXT_PRIMARY);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 35));
        return button;
    }

    private void styleTextField(JTextField field) {
        field.setBackground(BG_INPUT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setBackground(BG_INPUT);
        combo.setForeground(TEXT_PRIMARY);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? ACCENT : BG_INPUT);
                setForeground(TEXT_PRIMARY);
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return this;
            }
        });
    }

    private String guessMimeType(String fileName) {
        String ext = fileName.toLowerCase();
        if (ext.endsWith(".jpg") || ext.endsWith(".jpeg"))
            return "image/jpeg";
        if (ext.endsWith(".png"))
            return "image/png";
        if (ext.endsWith(".gif"))
            return "image/gif";
        if (ext.endsWith(".mp4"))
            return "video/mp4";
        if (ext.endsWith(".pdf"))
            return "application/pdf";
        if (ext.endsWith(".txt"))
            return "text/plain";
        return "application/octet-stream";
    }

    private void initializeGUI() {
        setTitle("💬 Secure Chat Client");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(550, 650);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_DARK);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_PANEL);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("🔐 Secure Chat");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Connection Panel
        JPanel connectionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        connectionPanel.setOpaque(false);

        JLabel portLabel = new JLabel("Port:");
        portLabel.setForeground(TEXT_SECONDARY);
        portLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        connectionPanel.add(portLabel);

        portField.setPreferredSize(new Dimension(70, 35));
        styleTextField(portField);
        connectionPanel.add(portField);
        connectionPanel.add(connectButton);

        headerPanel.add(connectionPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Main Chat Area
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(BG_DARK);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        area.setEditable(false);
        area.setBackground(BG_PANEL);
        area.setForeground(TEXT_PRIMARY);
        area.setFont(new Font("Consolas", Font.PLAIN, 13));
        area.setCaretColor(TEXT_PRIMARY);
        area.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        scrollPane.getViewport().setBackground(BG_PANEL);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Encryption Options Panel
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        optionsPanel.setBackground(BG_DARK);

        JLabel encLabel = new JLabel("🔒 Şifreleme:");
        encLabel.setForeground(TEXT_SECONDARY);
        encLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        optionsPanel.add(encLabel);

        encryptionSelect.setPreferredSize(new Dimension(180, 35));
        styleComboBox(encryptionSelect);
        optionsPanel.add(encryptionSelect);

        JLabel keyLabel = new JLabel("🔑 Key:");
        keyLabel.setForeground(TEXT_SECONDARY);
        keyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        optionsPanel.add(keyLabel);

        keyField.setPreferredSize(new Dimension(100, 35));
        styleTextField(keyField);
        keyField.setToolTipText("Şifreleme anahtarını girin");
        optionsPanel.add(keyField);

        optionsPanel.add(fileButton);

        centerPanel.add(optionsPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        // Input Panel
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(BG_PANEL);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        input.setToolTipText("Mesajınızı yazın...");
        styleTextField(input);
        input.setPreferredSize(new Dimension(0, 45));
        inputPanel.add(input, BorderLayout.CENTER);

        JButton sendButton = createStyledButton("Gönder ➤", ACCENT);
        sendButton.setPreferredSize(new Dimension(110, 45));
        sendButton.addActionListener(e -> sendMessage());
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(inputPanel, BorderLayout.SOUTH);

        // Event Listeners
        encryptionSelect.addActionListener(e -> updateAlgorithm());
        connectButton.addActionListener(e -> connectToServer());
        input.addActionListener(e -> sendMessage());
        fileButton.addActionListener(e -> sendFile());

        new Thread(this::receiveMessages, "Client-Receive-Thread").start();

        // Center window
        setLocationRelativeTo(null);
    }

    private void sendMessage() {
        String msg = input.getText().trim();
        if (!msg.isEmpty()) {
            try {
                updateAlgorithm();

                if (!client.isConnected()) {
                    appendMessage("⚠️ Hata: Sunucuya bağlı değil!", ACCENT);
                    connectButton.setEnabled(true);
                    return;
                }

                String toSend = (selectedAlgorithm != null) ? selectedAlgorithm.encrypt(msg) : msg;
                boolean sent = client.sendMessage(toSend);

                if (!sent) {
                    appendMessage("🔌 Sunucu bağlantısı kesildi! Mesaj gönderilemedi.", ACCENT);
                    connectButton.setEnabled(true);
                    return;
                }

                if (selectedAlgorithm != null) {
                    appendMessage("📤 Ben: " + msg + "\n   🔐 Şifreli: " + toSend, SUCCESS);
                } else {
                    appendMessage("📤 Ben: " + msg + " (Şifresiz)", TEXT_SECONDARY);
                }

                input.setText("");
            } catch (Exception ex) {
                appendMessage("❌ Mesaj gönderme hatası: " + ex.getMessage(), ACCENT);
            }
        }
    }

    private void appendMessage(String message, Color color) {
        SwingUtilities.invokeLater(() -> {
            area.append(message + "\n");
        });
    }

    private void sendFile() {
        if (!client.isConnected()) {
            appendMessage("⚠️ Hata: Sunucuya bağlı değil! Dosya gönderilemez.", ACCENT);
            connectButton.setEnabled(true);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File file = chooser.getSelectedFile();
            try {
                String fileName = file.getName();
                String mimeType = guessMimeType(fileName);
                byte[] fileBytes = java.nio.file.Files.readAllBytes(file.toPath());

                updateAlgorithm();
                String fileText = java.util.Base64.getEncoder().encodeToString(fileBytes);
                String toSend = (selectedAlgorithm != null) ? selectedAlgorithm.encrypt(fileText) : fileText;

                boolean sent = client.sendFile(fileName, mimeType, toSend);

                if (!sent) {
                    appendMessage("🔌 Sunucu bağlantısı kesildi! Dosya gönderilemedi.", ACCENT);
                    connectButton.setEnabled(true);
                    return;
                }

                if (selectedAlgorithm != null) {
                    appendMessage("📁 Şifreli dosya gönderildi: " + fileName, SUCCESS);
                } else {
                    appendMessage("📁 Dosya gönderildi: " + fileName, TEXT_SECONDARY);
                }
            } catch (Exception ex) {
                appendMessage("❌ Dosya gönderme hatası: " + ex.getMessage(), ACCENT);
            }
        }
    }

    private void connectToServer() {
        String portStr = portField.getText().trim();
        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException ex) {
            appendMessage("⚠️ Port numarası hatalı!", ACCENT);
            return;
        }
        connectButton.setEnabled(false);
        appendMessage("🔄 Sunucuya bağlanılıyor: localhost:" + port, TEXT_SECONDARY);
        new Thread(() -> {
            try {
                client.connect("127.0.0.1", port);

                // Bağlantı kurulduğunda RSA public key'i gönder
                String myPublicKeyBase64 = myRSA.getPublicKeyBase64();
                client.sendMessage("RSA_PUBKEY:" + myPublicKeyBase64);

                SwingUtilities.invokeLater(() -> {
                    appendMessage("✅ Sunucuya bağlandı: localhost:" + port, SUCCESS);
                    System.out.println("[LOG] RSA public key gönderildi.");
                    connectButton.setEnabled(false);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    appendMessage("❌ Bağlantı hatası: " + ex.getMessage(), ACCENT);
                    connectButton.setEnabled(true);
                });
            }
        }, "Client-Connect-Thread").start();
    }

    private void receiveMessages() {
        while (true) {
            try {
                // İlk bağlantı kurulana kadar bekle
                while (!client.isConnected()) {
                    Thread.sleep(500);
                }

                java.io.BufferedReader in = new java.io.BufferedReader(
                        new java.io.InputStreamReader(client.getSocket().getInputStream()));

                while (client.isConnected()) {
                    try {
                        String line = in.readLine();
                        if (line == null) {
                            // Sunucu bağlantıyı kapattı
                            handleDisconnection();
                            break;
                        }

                        if (line.startsWith("RSA_PUBKEY:")) {
                            // Karşı tarafın public key'ini al
                            String peerKeyBase64 = line.substring("RSA_PUBKEY:".length());
                            try {
                                peerPublicKey = RSACipher.decodePublicKey(peerKeyBase64);
                                rsaForEncrypt = new RSACipher(peerPublicKey);
                                System.out.println("[LOG] Karşı tarafın RSA public key'i alındı. RSA şifreleme hazır!");
                            } catch (Exception e) {
                                SwingUtilities.invokeLater(() -> {
                                    appendMessage("❌ RSA public key decode hatası: " + e.getMessage(), ACCENT);
                                });
                            }
                        } else if (line.startsWith("FILE:")) {
                            String[] parts = line.split(":", 4);
                            if (parts.length == 4) {
                                handleIncomingFile(parts[1], parts[2], parts[3]);
                            }
                        } else {
                            final String msg = line;
                            // RSA şifreli mesaj mı kontrol et ve çöz
                            String decryptedMsg = msg;
                            String prefix = "";
                            if (rsaForDecrypt != null && msg.length() > 100) {
                                // RSA şifreli mesaj olabilir - çözmeyi dene
                                try {
                                    decryptedMsg = rsaForDecrypt.decrypt(msg);
                                    prefix = "🔓 [RSA Çözüldü] ";
                                } catch (Exception e) {
                                    // RSA ile çözülemedi - normal mesaj olarak göster
                                    prefix = "";
                                    decryptedMsg = msg;
                                }
                            }
                            final String displayMsg = prefix + decryptedMsg;
                            SwingUtilities.invokeLater(() -> {
                                appendMessage("📩 [Gelen] " + displayMsg, TEXT_PRIMARY);
                            });
                        }
                    } catch (java.net.SocketTimeoutException ste) {
                        // Timeout normal - bağlantı hala açık olabilir, devam et
                        continue;
                    }
                }

            } catch (java.io.IOException ioe) {
                // I/O hatası - bağlantı kesildi
                handleDisconnection();
            } catch (Exception ex) {
                if (client.isConnected()) {
                    handleDisconnection();
                }
            }
        }
    }

    private void handleDisconnection() {
        client.disconnect();
        SwingUtilities.invokeLater(() -> {
            appendMessage("🔌 Sunucu bağlantısı kesildi!", ACCENT);
            connectButton.setEnabled(true);
        });
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
    }

    private void handleIncomingFile(String fileName, String mimeType, String encryptedBase64) {
        updateAlgorithm();
        String base64 = encryptedBase64;

        // RSA seçili ise rsaForDecrypt kullan, diğerleri için selectedAlgorithm
        EncryptionAlgorithm decryptAlgo = selectedAlgorithm;
        String selected = (String) encryptionSelect.getSelectedItem();
        if ("RSA".equals(selected) && rsaForDecrypt != null) {
            decryptAlgo = rsaForDecrypt;
        }

        if (decryptAlgo != null) {
            try {
                base64 = decryptAlgo.decrypt(encryptedBase64);
            } catch (Exception ex) {
                appendMessage("❌ Dosya şifresi çözülemedi: " + ex.getMessage(), ACCENT);
                return;
            }
        }

        try {
            byte[] fileBytes = java.util.Base64.getDecoder().decode(base64);
            if (mimeType.startsWith("image/")) {
                javax.swing.ImageIcon icon = new javax.swing.ImageIcon(fileBytes);
                javax.swing.JLabel imgLabel = new javax.swing.JLabel(icon);
                javax.swing.JOptionPane.showMessageDialog(this, imgLabel, "📷 Gelen Fotoğraf: " + fileName,
                        javax.swing.JOptionPane.PLAIN_MESSAGE);
                appendMessage("📷 Fotoğraf geldi: " + fileName, SUCCESS);
            } else {
                java.nio.file.Path outPath = java.nio.file.Paths.get("gelen_" + fileName);
                java.nio.file.Files.write(outPath, fileBytes);
                appendMessage("📥 Dosya kaydedildi: " + outPath.toString(), SUCCESS);
            }
        } catch (Exception e) {
            appendMessage("❌ Dosya kaydetme hatası: " + e.getMessage(), ACCENT);
        }
    }

    private void updateAlgorithm() {
        String selected = (String) encryptionSelect.getSelectedItem();
        String key = keyField.getText().trim();

        try {
            switch (selected) {
                case "Şifresiz Gönder":
                case "--- AES/DES/RSA ---":
                case "--- Klasik ---":
                    selectedAlgorithm = null;
                    break;
                case "AES (Kütüphaneli)":
                    if (key.isEmpty())
                        key = "AES_DEFAULT_KEY!";
                    selectedAlgorithm = new AESCipher(key);
                    break;
                case "AES (Manuel)":
                    if (key.isEmpty())
                        key = "MAES_DEFAULT_KEY";
                    selectedAlgorithm = new ManualAES(key);
                    break;
                case "DES (Kütüphaneli)":
                    if (key.isEmpty())
                        key = "DES_KEY!";
                    selectedAlgorithm = new DESCipher(key);
                    break;
                case "DES (Manuel)":
                    if (key.isEmpty())
                        key = "MDES_KEY";
                    selectedAlgorithm = new ManualDES(key);
                    break;
                case "RSA":
                    if (rsaForEncrypt == null) {
                        throw new Exception(
                                "RSA için önce karşı tarafın public key'i alınmalı! Bağlantı sonrası bekleyin.");
                    }
                    selectedAlgorithm = rsaForEncrypt;
                    break;
                case "AffineCipher":
                    if (!key.contains(","))
                        throw new Exception("Affine için key formatı: a,b");
                    String[] parts = key.split(",");
                    int a = Integer.parseInt(parts[0].trim());
                    int b = Integer.parseInt(parts[1].trim());
                    selectedAlgorithm = new AffineCipher(a, b);
                    break;
                case "SezarSifreleme":
                    if (key.isEmpty())
                        throw new Exception("Sezar için key boş olamaz");
                    selectedAlgorithm = new SezarSifreleme(Integer.parseInt(key));
                    break;
                case "SubstitutionCipher":
                    if (key.length() != 26)
                        throw new Exception("Substitution için key 26 harf olmalı");
                    selectedAlgorithm = new SubstitutionCipher(key);
                    break;
                case "VigenereCipher":
                    if (key.isEmpty())
                        throw new Exception("Vigenere için key boş olamaz");
                    selectedAlgorithm = new VigenereCipher(key);
                    break;
                case "RouteCipher":
                    if (key.isEmpty())
                        throw new Exception("RouteCipher için key boş olamaz");
                    selectedAlgorithm = new RouteCipher(Integer.parseInt(key));
                    break;
                case "ColumnarTransposition":
                    if (key.isEmpty())
                        throw new Exception("ColumnarTransposition için key boş olamaz");
                    selectedAlgorithm = new ColumnarTranspositionCipher(key);
                    break;
                case "PolybiusCipher":
                    selectedAlgorithm = new PolybiusCipher();
                    break;
                case "HillCipher":
                    if (key.isEmpty())
                        throw new Exception("HillCipher için key boş olamaz");
                    selectedAlgorithm = new HillCipher(key);
                    break;
                case "GCDCipher":
                    if (!key.contains(","))
                        throw new Exception("GCD için key formatı: a,b");
                    String[] gcdParts = key.split(",");
                    if (gcdParts.length != 2)
                        throw new Exception("GCD key iki sayıdan oluşmalı: a,b");
                    int x = Integer.parseInt(gcdParts[0].trim());
                    int y = Integer.parseInt(gcdParts[1].trim());
                    selectedAlgorithm = new GCDCipher(x, y);
                    break;
                default:
                    selectedAlgorithm = null;
                    break;
            }
        } catch (Exception e) {
            selectedAlgorithm = null;
            appendMessage("⚠️ Key hatalı: " + selected + " -> " + e.getMessage(), ACCENT);
        }
    }

    public void displayMessage(String sender, String message) {
        appendMessage(sender + ": " + message, TEXT_PRIMARY);
    }
}
