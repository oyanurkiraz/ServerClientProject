package main.encryption;

public class GCDCipher implements EncryptionAlgorithm {
    private final int a;
    private final int b;

    // Key constructor ile verilecek
    public GCDCipher(int a, int b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public String encrypt(String text) {
        int gcd = computeGCD(a, b);
        StringBuilder encrypted = new StringBuilder();

        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                char base = Character.isUpperCase(c) ? 'A' : 'a';
                // Harfi gcd kadar kaydır, mod 26
                char shifted = (char) ((c - base + gcd) % 26 + base);
                encrypted.append(shifted);
            } else {
                encrypted.append(c); // harf değilse olduğu gibi ekle
            }
        }

        return "GCD(" + a + ", " + b + ") = " + gcd + " → Şifreli Mesaj: " + encrypted.toString();
    }

    @Override
    public String decrypt(String text) {
        int gcd = computeGCD(a, b);
        StringBuilder decrypted = new StringBuilder();

        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                char base = Character.isUpperCase(c) ? 'A' : 'a';
                // Ters kaydır
                char shifted = (char) ((c - base - gcd + 26) % 26 + base);
                decrypted.append(shifted);
            } else {
                decrypted.append(c);
            }
        }

        return "GCD(" + a + ", " + b + ") = " + gcd + " → Çözülmüş Mesaj: " + decrypted.toString();
    }

    // Öklid algoritması
    private int computeGCD(int x, int y) {
        while (y != 0) {
            int temp = y;
            y = x % y;
            x = temp;
        }
        return Math.abs(x);
    }
}
