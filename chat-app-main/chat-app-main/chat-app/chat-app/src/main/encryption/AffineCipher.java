package main.encryption;

/**
 * Affine Şifreleme - İngilizce alfabe (26 harf: A-Z)
 * E(x) = (ax + b) mod 26
 * D(y) = a^(-1)(y - b) mod 26
 */
public class AffineCipher implements EncryptionAlgorithm {
    private final int a;
    private final int b;
    private static final int M = 26;

    public AffineCipher(int a, int b) {
        this.a = a;
        this.b = b % M;
    }

    @Override
    public String encrypt(String plainText) {
        StringBuilder result = new StringBuilder();
        for (char ch : plainText.toCharArray()) {
            if (Character.isLetter(ch)) {
                char base = Character.isUpperCase(ch) ? 'A' : 'a';
                int x = Character.toUpperCase(ch) - 'A';
                int encrypted = (a * x + b) % M;
                result.append((char) (encrypted + base));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    @Override
    public String decrypt(String cipherText) {
        int a_inv = modInverse(a, M);
        StringBuilder result = new StringBuilder();

        for (char ch : cipherText.toCharArray()) {
            if (Character.isLetter(ch)) {
                char base = Character.isUpperCase(ch) ? 'A' : 'a';
                int y = Character.toUpperCase(ch) - 'A';
                int decrypted = (a_inv * (y - b + M)) % M;
                result.append((char) (decrypted + base));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    private int modInverse(int a, int m) {
        a = a % m;
        for (int i = 1; i < m; i++) {
            if ((a * i) % m == 1)
                return i;
        }
        return 1;
    }
}
