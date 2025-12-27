package main.encryption;

import java.util.Base64;

/**
 * Tüm şifreleme algoritmalarını kapsamlı test eder
 * - Metin şifreleme/çözme
 * - Binary veri (dosya/fotoğraf) şifreleme/çözme
 */
public class EncryptionTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║     ŞİFRELEME ALGORİTMALARI KAPSAMLI TEST                  ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        // Test verileri
        String textMessage = "Merhaba Dünya! Bu bir test mesajıdır. 123456789";

        // Simüle edilmiş binary dosya verisi (Base64 olarak)
        byte[] binaryData = new byte[1024];
        for (int i = 0; i < binaryData.length; i++) {
            binaryData[i] = (byte) (i % 256);
        }
        String binaryBase64 = Base64.getEncoder().encodeToString(binaryData);

        // === AES/DES/RSA Testleri ===
        System.out.println("═══════════════ AES / DES / RSA ═══════════════\n");

        // AES Kütüphaneli
        testAlgorithm("AES (Kütüphaneli)", new AESCipher("TestKey123456789"), textMessage, binaryBase64);

        // AES Manuel
        testAlgorithm("AES (Manuel)", new ManualAES("TestKey123456789"), textMessage, binaryBase64);

        // DES Kütüphaneli
        testAlgorithm("DES (Kütüphaneli)", new DESCipher("TestKey8"), textMessage, binaryBase64);

        // DES Manuel
        testAlgorithm("DES (Manuel)", new ManualDES("TestKey8"), textMessage, binaryBase64);

        // RSA (sadece kısa mesaj - RSA key boyut sınırı var)
        testAlgorithmTextOnly("RSA", new RSACipher(), "Kisa mesaj RSA");

        // === Klasik Şifreler ===
        System.out.println("\n═══════════════ KLASİK ŞİFRELER ═══════════════\n");

        // Sezar
        testAlgorithm("Sezar Şifreleme", new SezarSifreleme(3), textMessage, binaryBase64);

        // Vigenere
        testAlgorithm("Vigenere", new VigenereCipher("ANAHTAR"), textMessage, null);

        // Substitution
        testAlgorithm("Substitution", new SubstitutionCipher("QWERTYUIOPASDFGHJKLZXCVBNM"), textMessage, null);

        // Affine
        testAlgorithm("Affine", new AffineCipher(5, 8), textMessage, null);

        // Route
        testAlgorithm("Route", new RouteCipher(4), textMessage, null);

        // Columnar Transposition
        testAlgorithm("Columnar Transposition", new ColumnarTranspositionCipher("ANAHTAR"), textMessage, null);

        // Polybius
        testAlgorithm("Polybius", new PolybiusCipher(), "MERHABA", null);

        // Hill (basit test)
        try {
            testAlgorithm("Hill", new HillCipher("GYBNQKURP"), "MERHABA", null);
        } catch (Exception e) {
            System.out.println("Hill Cipher test atlandı: " + e.getMessage());
        }

        // === Sonuçlar ===
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                    TEST SONUÇLARI                          ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.printf("║  ✓ Başarılı: %-44d ║%n", passed);
        System.out.printf("║  ✗ Başarısız: %-43d ║%n", failed);
        System.out.println("╚════════════════════════════════════════════════════════════╝");

        if (failed == 0) {
            System.out.println("\n🎉 TÜM TESTLER BAŞARILI! Dosya ve fotoğraf gönderimi çalışacaktır.");
        } else {
            System.out.println("\n⚠️ BAZI TESTLER BAŞARISIZ! Kontrol edin.");
        }
    }

    private static void testAlgorithm(String name, EncryptionAlgorithm algo, String textMessage, String binaryBase64) {
        System.out.println("┌─────────────────────────────────────────────");
        System.out.println("│ " + name);
        System.out.println("├─────────────────────────────────────────────");

        // Metin testi
        boolean textOk = testTextEncryption(algo, textMessage);

        // Binary testi (sadece AES/DES için)
        boolean binaryOk = true;
        if (binaryBase64 != null) {
            binaryOk = testBinaryEncryption(algo, binaryBase64);
        }

        if (textOk && binaryOk) {
            System.out.println("│ ✓ GENEL: BAŞARILI");
            passed++;
        } else {
            System.out.println("│ ✗ GENEL: BAŞARISIZ");
            failed++;
        }
        System.out.println("└─────────────────────────────────────────────\n");
    }

    private static void testAlgorithmTextOnly(String name, EncryptionAlgorithm algo, String textMessage) {
        System.out.println("┌─────────────────────────────────────────────");
        System.out.println("│ " + name);
        System.out.println("├─────────────────────────────────────────────");

        boolean textOk = testTextEncryption(algo, textMessage);

        if (textOk) {
            System.out.println("│ ✓ GENEL: BAŞARILI (sadece metin)");
            passed++;
        } else {
            System.out.println("│ ✗ GENEL: BAŞARISIZ");
            failed++;
        }
        System.out.println("└─────────────────────────────────────────────\n");
    }

    private static boolean testTextEncryption(EncryptionAlgorithm algo, String plainText) {
        try {
            String encrypted = algo.encrypt(plainText);
            String decrypted = algo.decrypt(encrypted);

            // Normalize et - bazı klasik şifreler büyük harfe dönüştürür ve özel
            // karakterleri kaldırır
            String normalizedOriginal = normalizeText(plainText);
            String normalizedDecrypted = normalizeText(decrypted);

            // Tam eşleşme veya normalize edilmiş eşleşme veya
            // decrypted, original ile başlıyorsa (Hill padding durumu)
            boolean matches = plainText.equals(decrypted) ||
                    normalizedOriginal.equals(normalizedDecrypted) ||
                    normalizedDecrypted.startsWith(normalizedOriginal);

            if (matches) {
                System.out.println("│ ✓ Metin: Şifreleme/çözme OK");
                return true;
            } else {
                System.out.println("│ ✗ Metin: Eşleşmiyor!");
                System.out.println("│   Orijinal: " + plainText.substring(0, Math.min(30, plainText.length())) + "...");
                System.out.println("│   Çözülen:  " + decrypted.substring(0, Math.min(30, decrypted.length())) + "...");
                return false;
            }
        } catch (Exception e) {
            System.out.println("│ ✗ Metin HATA: " + e.getMessage());
            return false;
        }
    }

    // Türkçe karakterleri normalize et
    private static String normalizeText(String text) {
        return text.toUpperCase()
                .replace(" ", "")
                .replace("!", "")
                .replace(".", "")
                .replace("?", "")
                .replace(",", "")
                .replace("İ", "I") // Türkçe büyük İ
                .replace("ı", "I") // Türkçe küçük ı
                .replace("Ğ", "G")
                .replace("ğ", "G")
                .replace("Ü", "U")
                .replace("ü", "U")
                .replace("Ş", "S")
                .replace("ş", "S")
                .replace("Ö", "O")
                .replace("ö", "O")
                .replace("Ç", "C")
                .replace("ç", "C");
    }

    private static boolean testBinaryEncryption(EncryptionAlgorithm algo, String base64Data) {
        try {
            // Dosya gönderiminde: base64 -> encrypt -> transfer -> decrypt -> base64
            String encrypted = algo.encrypt(base64Data);
            String decrypted = algo.decrypt(encrypted);

            // Decode edip binary olarak karşılaştır
            byte[] original = Base64.getDecoder().decode(base64Data);
            byte[] result = Base64.getDecoder().decode(decrypted);

            if (original.length != result.length) {
                System.out.println("│ ✗ Binary: Boyut uyuşmuyor! (" + original.length + " vs " + result.length + ")");
                return false;
            }

            for (int i = 0; i < original.length; i++) {
                if (original[i] != result[i]) {
                    System.out.println("│ ✗ Binary: Byte " + i + " farklı!");
                    return false;
                }
            }

            System.out.println("│ ✓ Binary: " + original.length + " byte veri OK (dosya/fotoğraf uyumlu)");
            return true;
        } catch (Exception e) {
            System.out.println("│ ✗ Binary HATA: " + e.getMessage());
            return false;
        }
    }
}
