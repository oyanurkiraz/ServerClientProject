package main.encryption;

public class AffineCipher implements EncryptionAlgorithm {
    private final int a;
    private final int b;
    private final int m = 26;

    public AffineCipher(int a, int b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public String encrypt(String plainText) {
        plainText = plainText.toUpperCase();
        StringBuilder result = new StringBuilder();
        for (char ch : plainText.toCharArray()) {
            if (Character.isLetter(ch)) {
                int x = ch - 'A';
                result.append((char) (((a * x + b) % m) + 'A'));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    @Override
    public String decrypt(String cipherText) {
        int a_inv = modInverse(a, m);
        StringBuilder result = new StringBuilder();
        cipherText = cipherText.toUpperCase();
        for (char ch : cipherText.toCharArray()) {
            if (Character.isLetter(ch)) {
                int y = ch - 'A';
                result.append((char) (((a_inv * (y - b + m)) % m) + 'A'));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    private int modInverse(int a, int m) {
        for (int i = 0; i < m; i++) {
            if ((a * i) % m == 1) return i;
        }
        return 1;
    }
}
