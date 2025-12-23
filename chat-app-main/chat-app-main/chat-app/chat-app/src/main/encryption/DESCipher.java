package main.encryption;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

/**
 * DES Kütüphaneli Şifreleme
 * javax.crypto kütüphanesi kullanılarak DES/CBC/PKCS5Padding implementasyonu
 */
public class DESCipher implements EncryptionAlgorithm {

    private static final String ALGORITHM = "DES";
    private static final String TRANSFORMATION = "DES/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 8; // 64 bit = 8 byte (56 bit efektif)
    private static final int IV_SIZE = 8; // DES için IV

    private final SecretKeySpec secretKey;
    private final byte[] iv;

    /**
     * Verilen 8 karakterlik anahtar ile DES şifreleyici oluşturur
     * 
     * @param key 8 karakter (64-bit) anahtar
     */
    public DESCipher(String key) {
        if (key == null || key.length() < KEY_SIZE) {
            key = padKey(key, KEY_SIZE);
        } else if (key.length() > KEY_SIZE) {
            key = key.substring(0, KEY_SIZE);
        }
        this.secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        this.iv = generateIV();
    }

    /**
     * Varsayılan anahtar ile oluşturur
     */
    public DESCipher() {
        this("DES_KEY!");
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
        // Sabit IV (ödev için basitlik adına)
        return "12345678".getBytes(StandardCharsets.UTF_8);
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
            throw new RuntimeException("DES şifreleme hatası: " + e.getMessage(), e);
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
            throw new RuntimeException("DES çözme hatası: " + e.getMessage(), e);
        }
    }

    public String getKeyBase64() {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }
}
