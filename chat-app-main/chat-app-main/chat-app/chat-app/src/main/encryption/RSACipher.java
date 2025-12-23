package main.encryption;

import java.security.*;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;

/**
 * RSA Kütüphaneli Şifreleme
 * Anahtar değişimi ve mesaj şifreleme için RSA-2048
 */
public class RSACipher implements EncryptionAlgorithm {

    private static final String ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;

    private PublicKey publicKey;
    private PrivateKey privateKey;

    /**
     * Yeni RSA anahtar çifti oluşturur
     */
    public RSACipher() {
        generateKeyPair();
    }

    /**
     * Mevcut public key ile oluşturur (sadece şifreleme için)
     */
    public RSACipher(PublicKey publicKey) {
        this.publicKey = publicKey;
        this.privateKey = null;
    }

    /**
     * Mevcut key pair ile oluşturur
     */
    public RSACipher(PublicKey publicKey, PrivateKey privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    private void generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
            keyGen.initialize(KEY_SIZE);
            KeyPair pair = keyGen.generateKeyPair();
            this.publicKey = pair.getPublic();
            this.privateKey = pair.getPrivate();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("RSA anahtar üretme hatası: " + e.getMessage(), e);
        }
    }

    @Override
    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("RSA şifreleme hatası: " + e.getMessage(), e);
        }
    }

    @Override
    public String decrypt(String cipherText) {
        if (privateKey == null) {
            throw new RuntimeException("RSA çözme için private key gerekli!");
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] decodedBytes = Base64.getDecoder().decode(cipherText);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("RSA çözme hatası: " + e.getMessage(), e);
        }
    }

    /**
     * Public key'i Base64 olarak döndürür
     */
    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * Private key'i Base64 olarak döndürür
     */
    public String getPrivateKeyBase64() {
        if (privateKey == null)
            return null;
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * Base64 encoded public key'den PublicKey objesi oluşturur
     */
    public static PublicKey decodePublicKey(String base64Key) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            java.security.spec.X509EncodedKeySpec spec = new java.security.spec.X509EncodedKeySpec(keyBytes);
            KeyFactory factory = KeyFactory.getInstance(ALGORITHM);
            return factory.generatePublic(spec);
        } catch (Exception e) {
            throw new RuntimeException("Public key decode hatası: " + e.getMessage(), e);
        }
    }
}
