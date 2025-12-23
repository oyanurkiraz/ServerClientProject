package main.encryption;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * AES-128 Kütüphaneli Şifreleme
 * javax.crypto kütüphanesi kullanılarak AES/CBC/PKCS5Padding implementasyonu
 */
public class AESCipher implements EncryptionAlgorithm {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 16; // 128 bit = 16 byte
    private static final int IV_SIZE = 16; // CBC için IV

    private final SecretKeySpec secretKey;
    private final byte[] iv;

    /**
     * Verilen 16 karakterlik anahtar ile AES şifreleyici oluşturur
     * 
     * @param key 16 karakter (128-bit) anahtar
     */
    public AESCipher(String key) {
        if (key == null || key.length() < KEY_SIZE) {
            // Anahtar kısa ise padding yap
            key = padKey(key, KEY_SIZE);
        } else if (key.length() > KEY_SIZE) {
            key = key.substring(0, KEY_SIZE);
        }
        this.secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        this.iv = generateIV();
    }

    /**
     * Varsayılan anahtar ile oluşturur (test için)
     */
    public AESCipher() {
        this("AES_DEFAULT_KEY!");
    }

    private String padKey(String key, int length) {
        if (key == null)
            key = "";
        StringBuilder sb = new StringBuilder(key);
        while (sb.length() < length) {
            sb.append('0');
        }
        return sb.toString();
    }

    private byte[] generateIV() {
        // Sabit IV kullanıyoruz (ödev için basitlik adına)
        // Gerçek uygulamada her şifreleme için yeni IV üretilmeli
        return "1234567890ABCDEF".getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("AES şifreleme hatası: " + e.getMessage(), e);
        }
    }

    @Override
    public String decrypt(String cipherText) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            byte[] decodedBytes = Base64.getDecoder().decode(cipherText);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES çözme hatası: " + e.getMessage(), e);
        }
    }

    /**
     * Kullanılan anahtarı Base64 olarak döndürür
     */
    public String getKeyBase64() {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    /**
     * Kullanılan IV'yi Base64 olarak döndürür
     */
    public String getIVBase64() {
        return Base64.getEncoder().encodeToString(iv);
    }
}
