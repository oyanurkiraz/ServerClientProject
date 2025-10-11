package main.encryption;

public interface EncryptionAlgorithm {
    String encrypt(String plainText);
    String decrypt(String cipherText);
}
