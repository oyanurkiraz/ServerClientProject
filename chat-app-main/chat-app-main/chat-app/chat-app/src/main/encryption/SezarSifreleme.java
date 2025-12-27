package main.encryption;

/**
 * Sezar Şifreleme - İngilizce alfabe (26 harf: A-Z)
 */
public class SezarSifreleme implements EncryptionAlgorithm {
    private final int shift;

    public SezarSifreleme(int shift) {
        this.shift = shift % 26;
    }

    @Override
    public String encrypt(String plainText) {
        StringBuilder result = new StringBuilder();
        for (char ch : plainText.toCharArray()) {
            if (Character.isLetter(ch)) {
                char base = Character.isUpperCase(ch) ? 'A' : 'a';
                result.append((char) ((ch - base + shift + 26) % 26 + base));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    @Override
    public String decrypt(String cipherText) {
        StringBuilder result = new StringBuilder();
        for (char ch : cipherText.toCharArray()) {
            if (Character.isLetter(ch)) {
                char base = Character.isUpperCase(ch) ? 'A' : 'a';
                result.append((char) ((ch - base - shift + 26) % 26 + base));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }
}
