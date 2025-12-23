package main.encryption;

public class HillCipher implements EncryptionAlgorithm {
    private final int[][] keyMatrix;
    private final int size;

    public HillCipher(String key) throws Exception {
        // Key formatı: "6 24 1 13 16 10 20 17 15" (3x3 matrix)
        String[] parts = key.trim().split("\\s+");
        int n = (int) Math.sqrt(parts.length);
        if (n * n != parts.length) throw new Exception("Key kare matris olmalı! (ör. 4,9,16 eleman)");
        size = n;
        keyMatrix = new int[n][n];
        int idx = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                keyMatrix[i][j] = Integer.parseInt(parts[idx++]) % 26;
            }
        }
    }

    private int charToNum(char c) {
        return c - 'A';
    }

    private char numToChar(int n) {
        return (char) (n + 'A');
    }

    @Override
    public String encrypt(String text) {
        text = text.toUpperCase().replaceAll("[^A-Z]", "");
        while (text.length() % size != 0)
            text += "X"; // padding

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i += size) {
            int[] vec = new int[size];
            for (int j = 0; j < size; j++)
                vec[j] = charToNum(text.charAt(i + j));

            for (int r = 0; r < size; r++) {
                int sum = 0;
                for (int c = 0; c < size; c++)
                    sum += keyMatrix[r][c] * vec[c];
                sb.append(numToChar(sum % 26));
            }
        }
        return sb.toString();
    }

    @Override
    public String decrypt(String text) {
        // Not: Hill şifrelemede çözüm için matrisin mod 26 tersinin alınması gerekir.
        // Bu kısım basitleştirilmiş örnektir (sadece encrypt testi için kullan).
        return "[HillCipher: decrypt() henüz uygulanmadı]";
    }
}
