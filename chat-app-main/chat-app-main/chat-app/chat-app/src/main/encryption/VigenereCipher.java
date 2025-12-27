package main.encryption;

/**
 * Vigenere Şifreleme - İngilizce alfabe (26 harf: A-Z)
 */
public class VigenereCipher implements EncryptionAlgorithm {
    private final String key;

    public VigenereCipher(String key) {
        this.key = key.toUpperCase().replaceAll("[^A-Z]", "");
    }

    @Override
    public String encrypt(String plainText) {
        StringBuilder result = new StringBuilder();
        int keyIndex = 0;

        for (char ch : plainText.toCharArray()) {
            if (Character.isLetter(ch)) {
                char base = Character.isUpperCase(ch) ? 'A' : 'a';
                int shift = key.charAt(keyIndex % key.length()) - 'A';
                result.append((char) ((Character.toUpperCase(ch) - 'A' + shift) % 26 + base));
                keyIndex++;
            } else {
                result.append(ch);
            }
        }

        return result.toString();
    }

    @Override
    public String decrypt(String cipherText) {
        StringBuilder result = new StringBuilder();
        int keyIndex = 0;

        for (char ch : cipherText.toCharArray()) {
            if (Character.isLetter(ch)) {
                char base = Character.isUpperCase(ch) ? 'A' : 'a';
                int shift = key.charAt(keyIndex % key.length()) - 'A';
                result.append((char) ((Character.toUpperCase(ch) - 'A' - shift + 26) % 26 + base));
                keyIndex++;
            } else {
                result.append(ch);
            }
        }

        return result.toString();
    }
}
