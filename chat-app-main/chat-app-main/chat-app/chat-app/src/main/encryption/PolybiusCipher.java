package main.encryption;

public class PolybiusCipher implements EncryptionAlgorithm {
    private final char[][] square = {
            {'A', 'B', 'C', 'D', 'E'},
            {'F', 'G', 'H', 'I', 'K'},
            {'L', 'M', 'N', 'O', 'P'},
            {'Q', 'R', 'S', 'T', 'U'},
            {'V', 'W', 'X', 'Y', 'Z'}
    };

    @Override
    public String encrypt(String text) {
        text = text.toUpperCase().replace("J", "I");
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 5; j++) {
                        if (square[i][j] == c) {
                            sb.append(i + 1).append(j + 1).append(" ");
                        }
                    }
                }
            } else if (c == ' ') {
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }

    @Override
    public String decrypt(String text) {
        StringBuilder sb = new StringBuilder();
        text = text.replaceAll("\\s+", "");
        for (int i = 0; i < text.length(); i += 2) {
            int row = Character.getNumericValue(text.charAt(i)) - 1;
            int col = Character.getNumericValue(text.charAt(i + 1)) - 1;
            if (row >= 0 && row < 5 && col >= 0 && col < 5) {
                sb.append(square[row][col]);
            }
        }
        return sb.toString();
    }
}
