package main.encryption;

/**
 * Substitution Şifreleme - İngilizce alfabe (26 harf: A-Z)
 * Key: 26 harflik permütasyon (örn: QWERTYUIOPASDFGHJKLZXCVBNM)
 */
public class SubstitutionCipher implements EncryptionAlgorithm {
    private final String key;

    public SubstitutionCipher(String key) {
        this.key = key.toUpperCase();
        if (this.key.length() != 26) {
            throw new IllegalArgumentException("Substitution key must be 26 characters");
        }
    }

    @Override
    public String encrypt(String plainText) {
        StringBuilder result = new StringBuilder();
        for (char ch : plainText.toCharArray()) {
            if (Character.isLetter(ch)) {
                boolean isUpper = Character.isUpperCase(ch);
                int index = Character.toUpperCase(ch) - 'A';
                char encrypted = key.charAt(index);
                result.append(isUpper ? encrypted : Character.toLowerCase(encrypted));
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
                boolean isUpper = Character.isUpperCase(ch);
                int index = key.indexOf(Character.toUpperCase(ch));
                char decrypted = (char) ('A' + index);
                result.append(isUpper ? decrypted : Character.toLowerCase(decrypted));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }
}
