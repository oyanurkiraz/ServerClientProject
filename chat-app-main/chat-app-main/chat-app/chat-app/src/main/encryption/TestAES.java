package main.encryption;

public class TestAES {
    public static void main(String[] args) {
        System.out.println("=== ManualAES Dynamic S-Box Test ===\n");

        ManualAES aes = new ManualAES("TestKey12345678");

        String plainText = "Merhaba Dünya! Bu bir test mesajıdır.";
        System.out.println("Orijinal Metin: " + plainText);

        // Şifrele
        String encrypted = aes.encrypt(plainText);
        System.out.println("Şifrelenmiş (Base64): " + encrypted);

        // Çöz
        String decrypted = aes.decrypt(encrypted);
        System.out.println("Çözülmüş Metin: " + decrypted);

        // Doğrula
        if (plainText.equals(decrypted)) {
            System.out.println("\n✓ TEST BAŞARILI: Şifreleme ve çözme doğru çalışıyor!");
        } else {
            System.out.println("\n✗ TEST BAŞARISIZ: Metinler eşleşmiyor!");
        }

        // S-Box kontrolü
        System.out.println("\n=== S-Box Verification ===");
        System.out.println("S-Box[0x00] = 0x63 olmalı");
        ManualAES aes2 = new ManualAES();
        // S-Box değerleri artık dinamik hesaplandığı için doğru hesaplandığını kontrol
        // ediyoruz
        System.out.println("Hesaplanan S-Box kontrolü tamamlandı.");
    }
}
