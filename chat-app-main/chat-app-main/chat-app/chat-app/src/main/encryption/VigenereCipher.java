package main.encryption;

public class VigenereCipher implements EncryptionAlgorithm {
    private final String key;

    public VigenereCipher(String key) {
        this.key = key.toUpperCase().replaceAll("[^A-Z]", "");
    }

    @Override
    public String encrypt(String plainText) {
        plainText = plainText.toUpperCase(); 
        StringBuilder result = new StringBuilder();
        int keyIndex = 0;

        for (int i = 0; i < plainText.length(); i++) {
            char ch = plainText.charAt(i);

            if (Character.isLetter(ch)) { 
                char base = 'A';
                int shift = key.charAt(keyIndex % key.length()) - base; 
                result.append((char) ((ch - base + shift) % 26 + base));
                keyIndex++; 
            } else {
                result.append(ch); 
            }
        }

        return result.toString();
    }

    @Override
    public String decrypt(String cipherText) {
        cipherText = cipherText.toUpperCase();
        StringBuilder result = new StringBuilder();
        int keyIndex = 0;

        for (int i = 0; i < cipherText.length(); i++) {
            char ch = cipherText.charAt(i);

            if (Character.isLetter(ch)) {
                char base = 'A';
                int shift = key.charAt(keyIndex % key.length()) - base;
                result.append((char) ((ch - base - shift + 26) % 26 + base));
                keyIndex++;
            } else {
                result.append(ch);
            }
        }

        return result.toString();
    }
}
