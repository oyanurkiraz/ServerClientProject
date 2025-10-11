package main.encryption;

public class SubstitutionCipher implements EncryptionAlgorithm {
    private final String key;

    public SubstitutionCipher(String key) {
        this.key = key.toUpperCase();
        if (key.length() != 26) {
            throw new IllegalArgumentException("Substitution key must be 26 characters");
        }
    }

    @Override
    public String encrypt(String plainText) {
        plainText = plainText.toUpperCase();
        StringBuilder result = new StringBuilder();
        for (char ch : plainText.toCharArray()) {
            if (Character.isLetter(ch)) {
                result.append(key.charAt(ch - 'A'));
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
        for (char ch : cipherText.toCharArray()) {
            if (Character.isLetter(ch)) {
                result.append((char) ('A' + key.indexOf(ch)));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }
}
