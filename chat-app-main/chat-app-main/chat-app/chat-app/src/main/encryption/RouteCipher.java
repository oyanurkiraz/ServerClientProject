package main.encryption;

public class RouteCipher implements EncryptionAlgorithm {
    private final int key; // Grid boyutu

    public RouteCipher(int key) {
        this.key = key;
    }

    @Override
    public String encrypt(String text) {
        char[] chars = text.toCharArray();
        char[] result = new char[chars.length];
        int index = 0;
        for (int col = 0; col < key; col++) {
            for (int row = col; row < chars.length; row += key) {
                result[index++] = chars[row];
            }
        }
        return new String(result);
    }

    @Override
    public String decrypt(String text) {
        char[] chars = text.toCharArray();
        char[] result = new char[chars.length];
        int index = 0;
        int fullColumns = chars.length / key;
        int extra = chars.length % key;

        int[] colLengths = new int[key];
        for (int i = 0; i < key; i++) {
            colLengths[i] = fullColumns + (i < extra ? 1 : 0);
        }

        int pos = 0;
        for (int col = 0; col < key; col++) {
            int len = colLengths[col];
            for (int row = 0; row < len; row++) {
                int target = row * key + col;
                if (target < chars.length) {
                    result[target] = chars[pos++];
                }
            }
        }
        return new String(result);
    }
}
