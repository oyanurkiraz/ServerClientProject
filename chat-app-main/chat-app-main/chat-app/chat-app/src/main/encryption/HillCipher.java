package main.encryption;

/**
 * Hill Şifreleme - İngilizce alfabe (26 harf: A-Z)
 * Key formatı:
 * - Harfler: "GYBNQKURP" (9 harf = 3x3 matris)
 * - Sayılar: "6 24 1 13 16 10 20 17 15" (boşlukla ayrılmış)
 */
public class HillCipher implements EncryptionAlgorithm {
    private final int[][] keyMatrix;
    private final int[][] invKeyMatrix;
    private final int size;
    private static final int M = 26;

    public HillCipher(String key) throws Exception {
        int[] numbers;

        // Key formatını belirle
        if (key.contains(" ")) {
            // Sayı formatı: "6 24 1 13 16 10 20 17 15"
            String[] parts = key.trim().split("\\s+");
            numbers = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                numbers[i] = Integer.parseInt(parts[i]) % M;
            }
        } else {
            // Harf formatı: "GYBNQKURP"
            String upperKey = key.toUpperCase();
            numbers = new int[upperKey.length()];
            for (int i = 0; i < upperKey.length(); i++) {
                numbers[i] = (upperKey.charAt(i) - 'A') % M;
            }
        }

        // Kare matris kontrolü
        int n = (int) Math.sqrt(numbers.length);
        if (n * n != numbers.length) {
            throw new Exception("Key kare matris olmalı! (ör. 4,9,16 eleman)");
        }

        size = n;
        keyMatrix = new int[n][n];
        int idx = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                keyMatrix[i][j] = numbers[idx++];
            }
        }

        // Ters matrisi hesapla
        invKeyMatrix = calculateInverseMatrix(keyMatrix, n);
    }

    private int[][] calculateInverseMatrix(int[][] matrix, int n) throws Exception {
        int det = determinant(matrix, n);
        int detInv = modInverse(det, M);

        if (detInv == -1) {
            throw new Exception("Matrisin mod " + M + " tersi yok!");
        }

        int[][] adj = adjugate(matrix, n);
        int[][] inv = new int[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                inv[i][j] = (detInv * adj[i][j] % M + M) % M;
            }
        }

        return inv;
    }

    private int determinant(int[][] matrix, int n) {
        if (n == 1)
            return matrix[0][0];
        if (n == 2) {
            return (matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0]);
        }

        int det = 0;
        for (int c = 0; c < n; c++) {
            int[][] minor = new int[n - 1][n - 1];
            for (int i = 1; i < n; i++) {
                int colIdx = 0;
                for (int j = 0; j < n; j++) {
                    if (j == c)
                        continue;
                    minor[i - 1][colIdx++] = matrix[i][j];
                }
            }
            int sign = (c % 2 == 0) ? 1 : -1;
            det += sign * matrix[0][c] * determinant(minor, n - 1);
        }
        return det;
    }

    private int[][] adjugate(int[][] matrix, int n) {
        int[][] adj = new int[n][n];

        if (n == 1) {
            adj[0][0] = 1;
            return adj;
        }

        if (n == 2) {
            adj[0][0] = matrix[1][1];
            adj[0][1] = -matrix[0][1];
            adj[1][0] = -matrix[1][0];
            adj[1][1] = matrix[0][0];
            return adj;
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int[][] minor = new int[n - 1][n - 1];
                int mi = 0;
                for (int r = 0; r < n; r++) {
                    if (r == i)
                        continue;
                    int mj = 0;
                    for (int c = 0; c < n; c++) {
                        if (c == j)
                            continue;
                        minor[mi][mj++] = matrix[r][c];
                    }
                    mi++;
                }
                int sign = ((i + j) % 2 == 0) ? 1 : -1;
                adj[j][i] = sign * determinant(minor, n - 1);
            }
        }
        return adj;
    }

    private int modInverse(int a, int m) {
        a = ((a % m) + m) % m;
        for (int i = 1; i < m; i++) {
            if ((a * i) % m == 1)
                return i;
        }
        return -1;
    }

    @Override
    public String encrypt(String text) {
        // Sadece harfleri al
        StringBuilder cleanText = new StringBuilder();
        for (char ch : text.toUpperCase().toCharArray()) {
            if (ch >= 'A' && ch <= 'Z') {
                cleanText.append(ch);
            }
        }

        String input = cleanText.toString();

        // Padding
        while (input.length() % size != 0) {
            input += "X";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i += size) {
            int[] vec = new int[size];
            for (int j = 0; j < size; j++) {
                vec[j] = input.charAt(i + j) - 'A';
            }

            for (int r = 0; r < size; r++) {
                int sum = 0;
                for (int c = 0; c < size; c++) {
                    sum += keyMatrix[r][c] * vec[c];
                }
                sb.append((char) (((sum % M) + M) % M + 'A'));
            }
        }
        return sb.toString();
    }

    @Override
    public String decrypt(String text) {
        // Sadece harfleri al
        StringBuilder cleanText = new StringBuilder();
        for (char ch : text.toUpperCase().toCharArray()) {
            if (ch >= 'A' && ch <= 'Z') {
                cleanText.append(ch);
            }
        }

        String input = cleanText.toString();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i += size) {
            int[] vec = new int[size];
            for (int j = 0; j < size; j++) {
                vec[j] = input.charAt(i + j) - 'A';
            }

            for (int r = 0; r < size; r++) {
                int sum = 0;
                for (int c = 0; c < size; c++) {
                    sum += invKeyMatrix[r][c] * vec[c];
                }
                sb.append((char) (((sum % M) + M) % M + 'A'));
            }
        }
        return sb.toString();
    }
}
