package main.encryption;

public class DebugSubstitution {
    public static void main(String[] args) {
        SubstitutionCipher sub = new SubstitutionCipher("QWERTYUIOPASDFGHJKLZXCVBNM");
        String msg = "Hello World"; // Sadece İngilizce karakterler
        String enc = sub.encrypt(msg);
        String dec = sub.decrypt(enc);

        System.out.println("Original:  [" + msg + "]");
        System.out.println("Encrypted: [" + enc + "]");
        System.out.println("Decrypted: [" + dec + "]");
        System.out.println("Equal: " + msg.equals(dec));
    }
}
