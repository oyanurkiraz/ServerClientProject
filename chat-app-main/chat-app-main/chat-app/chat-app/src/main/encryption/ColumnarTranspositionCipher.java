package main.encryption;

import java.util.Arrays;

public class ColumnarTranspositionCipher implements EncryptionAlgorithm {

    private final String key;

    public ColumnarTranspositionCipher(String key) {
        this.key = key.toUpperCase(); // Key büyük harfe çevrilir
    }

    @Override
    public String encrypt(String text) {
        text = text.replaceAll("\\s+", ""); // Boşlukları kaldır
        int cols = key.length();
        int rows = (int) Math.ceil((double) text.length() / cols);

        char[][] grid = new char[rows][cols];

        // Grid'i doldur, boş kalanları 'X' ile doldur
        int index = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (index < text.length()) {
                    grid[r][c] = text.charAt(index++);
                } else {
                    grid[r][c] = 'X';
                }
            }
        }

        // Key sırasına göre sütunları oku
        Character[] keyArr = new Character[cols];
        for (int i = 0; i < cols; i++) keyArr[i] = key.charAt(i);

        StringBuilder cipher = new StringBuilder();
        Character[] sortedKey = keyArr.clone();
        Arrays.sort(sortedKey);

        // Şifreleme: Sütunları sıralı anahtara göre oku
        for (char ch : sortedKey) {
            int col = -1;
            for (int i = 0; i < cols; i++) {
                // KRİTİK DÜZELTME: keyArr[i] != null KONTROLÜ EKLENDİ
                if (keyArr[i] != null && keyArr[i] == ch) {
                    col = i;
                    keyArr[i] = null; // Aynı harfi bir daha kullanmamak için null
                    break;
                }
            }
            
            // Eğer col bulunamazsa (mantıksal hata/sıralama hatası)
            if (col == -1) {
                // Bu durumda kodunuzun mantığı hatalıdır, ancak çalışmaya devam etmek için
                // bu sütunu atlayabilir veya hata fırlatabilirsiniz. 
                // Şimdilik hatasız devam ediyoruz.
                continue; 
            }

            for (int r = 0; r < rows; r++) {
                cipher.append(grid[r][col]);
            }
        }

        return cipher.toString();
    }

    @Override
    public String decrypt(String text) {
        int cols = key.length();
        int rows = (int) Math.ceil((double) text.length() / cols);
        char[][] grid = new char[rows][cols];

        // Decrypt için de keyArr'ı yeniden oluşturuyoruz
        Character[] keyArr = new Character[cols];
        for (int i = 0; i < cols; i++) keyArr[i] = key.charAt(i);

        Character[] sortedKey = keyArr.clone();
        Arrays.sort(sortedKey);

        // Şifreli metni sütun sütun geri yerleştir
        int index = 0;
        for (char ch : sortedKey) {
            int col = -1;
            for (int i = 0; i < cols; i++) {
                // KRİTİK DÜZELTME: keyArr[i] != null KONTROLÜ EKLENDİ
                if (keyArr[i] != null && keyArr[i] == ch) {
                    col = i;
                    keyArr[i] = null;
                    break;
                }
            }
            
            // Eğer col bulunamazsa
            if (col == -1) {
                continue;
            }
            
            for (int r = 0; r < rows; r++) {
                if (index < text.length())
                    grid[r][col] = text.charAt(index++);
            }
        }

        // Grid'i satır satır oku
        StringBuilder plain = new StringBuilder();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                plain.append(grid[r][c]);
            }
        }

        return plain.toString().replaceAll("X+$", ""); // Son X'leri temizle
    }
}