package main.encryption;

import java.util.Base64;
import java.nio.charset.StandardCharsets;

/**
 * AES-128 Manuel Implementasyon (Kütüphanesiz)
 * 
 * Bu implementasyon eğitim amaçlıdır ve AES'in çalışma prensiplerini gösterir:
 * - SubBytes (S-Box substitution) - Dinamik olarak hesaplanır
 * - ShiftRows (Satır kaydırma)
 * - MixColumns (Sütun karıştırma)
 * - AddRoundKey (Anahtar ekleme)
 * - Key Expansion (Anahtar genişletme)
 * 
 * S-Box değerleri runtime'da GF(2^8) alanında matematiksel olarak hesaplanır:
 * 1. Multiplicative Inverse in GF(2^8) with irreducible polynomial x^8 + x^4 +
 * x^3 + x + 1
 * 2. Affine Transformation
 */
public class ManualAES implements EncryptionAlgorithm {

    private static final int BLOCK_SIZE = 16; // 128 bit = 16 byte
    private static final int KEY_SIZE = 16; // AES-128
    private static final int ROUNDS = 10; // AES-128 için 10 round

    private final byte[] key;
    private final byte[][] roundKeys;

    // S-Box ve Inverse S-Box - dinamik olarak hesaplanır
    private final int[] sbox;
    private final int[] invSbox;

    // Round constants - dinamik olarak hesaplanır
    private final int[] rcon;

    /**
     * GF(2^8) alanında çarpma işlemi
     * Irreducible polynomial: x^8 + x^4 + x^3 + x + 1 (0x11B)
     */
    private static int gfMultiply(int a, int b) {
        int result = 0;
        while (b > 0) {
            if ((b & 1) != 0) {
                result ^= a;
            }
            a <<= 1;
            if ((a & 0x100) != 0) {
                a ^= 0x11B; // x^8 + x^4 + x^3 + x + 1
            }
            b >>= 1;
        }
        return result & 0xFF;
    }

    /**
     * GF(2^8) alanında multiplicative inverse hesapla
     * Extended Euclidean Algorithm kullanarak
     */
    private static int gfInverse(int a) {
        if (a == 0)
            return 0;

        // Fermat's Little Theorem kullanarak: a^(-1) = a^(254) in GF(2^8)
        int result = a;
        for (int i = 0; i < 6; i++) {
            result = gfMultiply(result, result);
            result = gfMultiply(result, a);
        }
        result = gfMultiply(result, result);
        return result;
    }

    /**
     * Affine transformation for S-Box
     * b'_i = b_i XOR b_(i+4 mod 8) XOR b_(i+5 mod 8) XOR b_(i+6 mod 8) XOR b_(i+7
     * mod 8) XOR c_i
     * where c = 0x63
     */
    private static int affineTransform(int b) {
        int result = 0;
        for (int i = 0; i < 8; i++) {
            int bit = ((b >> i) & 1) ^
                    ((b >> ((i + 4) % 8)) & 1) ^
                    ((b >> ((i + 5) % 8)) & 1) ^
                    ((b >> ((i + 6) % 8)) & 1) ^
                    ((b >> ((i + 7) % 8)) & 1) ^
                    ((0x63 >> i) & 1); // c = 0x63
            result |= (bit << i);
        }
        return result;
    }

    /**
     * Inverse Affine transformation for Inverse S-Box
     */
    private static int invAffineTransform(int b) {
        // First apply inverse affine, then take inverse in GF(2^8)
        int result = 0;
        for (int i = 0; i < 8; i++) {
            int bit = ((b >> ((i + 2) % 8)) & 1) ^
                    ((b >> ((i + 5) % 8)) & 1) ^
                    ((b >> ((i + 7) % 8)) & 1) ^
                    ((0x05 >> i) & 1); // inverse constant
            result |= (bit << i);
        }
        return result;
    }

    /**
     * S-Box'ı matematiksel olarak hesapla
     * Her byte için: S-Box[i] = AffineTransform(GF_Inverse(i))
     */
    private int[] generateSBox() {
        int[] sboxTable = new int[256];
        for (int i = 0; i < 256; i++) {
            // 1. GF(2^8)'de multiplicative inverse bul
            int inverse = gfInverse(i);
            // 2. Affine transformation uygula
            sboxTable[i] = affineTransform(inverse);
        }
        return sboxTable;
    }

    /**
     * Inverse S-Box'ı matematiksel olarak hesapla
     * Her byte için: InvS-Box[i] = GF_Inverse(InvAffineTransform(i))
     */
    private int[] generateInvSBox() {
        int[] invSboxTable = new int[256];
        for (int i = 0; i < 256; i++) {
            // 1. Inverse Affine transformation uygula
            int invAffine = invAffineTransform(i);
            // 2. GF(2^8)'de multiplicative inverse bul
            invSboxTable[i] = gfInverse(invAffine);
        }
        return invSboxTable;
    }

    /**
     * Round Constants (Rcon) hesapla
     * Rcon[i] = x^(i-1) in GF(2^8)
     */
    private int[] generateRcon() {
        int[] rconTable = new int[10];
        int val = 1;
        for (int i = 0; i < 10; i++) {
            rconTable[i] = val;
            val = gfMultiply(val, 2);
        }
        return rconTable;
    }

    public ManualAES(String key) {
        // S-Box'ları matematiksel olarak hesapla
        this.sbox = generateSBox();
        this.invSbox = generateInvSBox();
        this.rcon = generateRcon();

        if (key == null || key.length() < KEY_SIZE) {
            key = padKey(key, KEY_SIZE);
        } else if (key.length() > KEY_SIZE) {
            key = key.substring(0, KEY_SIZE);
        }
        this.key = key.getBytes(StandardCharsets.UTF_8);
        this.roundKeys = keyExpansion();
    }

    public ManualAES() {
        this("MAES_DEFAULT_KEY");
    }

    private String padKey(String key, int length) {
        if (key == null)
            key = "";
        StringBuilder sb = new StringBuilder(key);
        while (sb.length() < length) {
            sb.append('0');
        }
        return sb.toString();
    }

    // Key Expansion - 128-bit key -> 11 round keys
    private byte[][] keyExpansion() {
        byte[][] w = new byte[44][4]; // 44 words for AES-128

        // First 4 words are the original key
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                w[i][j] = key[i * 4 + j];
            }
        }

        // Generate remaining words
        for (int i = 4; i < 44; i++) {
            byte[] temp = w[i - 1].clone();

            if (i % 4 == 0) {
                // RotWord
                byte t = temp[0];
                temp[0] = temp[1];
                temp[1] = temp[2];
                temp[2] = temp[3];
                temp[3] = t;

                // SubWord
                for (int j = 0; j < 4; j++) {
                    temp[j] = (byte) sbox[temp[j] & 0xFF];
                }

                // XOR with Rcon
                temp[0] ^= rcon[i / 4 - 1];
            }

            // XOR with w[i-4]
            for (int j = 0; j < 4; j++) {
                w[i][j] = (byte) (w[i - 4][j] ^ temp[j]);
            }
        }

        // Convert to round keys (11 keys of 16 bytes each)
        byte[][] keys = new byte[ROUNDS + 1][16];
        for (int r = 0; r <= ROUNDS; r++) {
            for (int c = 0; c < 4; c++) {
                for (int row = 0; row < 4; row++) {
                    keys[r][row * 4 + c] = w[r * 4 + c][row];
                }
            }
        }

        return keys;
    }

    // SubBytes - S-Box substitution
    private void subBytes(byte[][] state) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                state[i][j] = (byte) sbox[state[i][j] & 0xFF];
            }
        }
    }

    // InvSubBytes
    private void invSubBytes(byte[][] state) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                state[i][j] = (byte) invSbox[state[i][j] & 0xFF];
            }
        }
    }

    // ShiftRows
    private void shiftRows(byte[][] state) {
        // Row 1: shift left by 1
        byte temp = state[1][0];
        state[1][0] = state[1][1];
        state[1][1] = state[1][2];
        state[1][2] = state[1][3];
        state[1][3] = temp;

        // Row 2: shift left by 2
        temp = state[2][0];
        state[2][0] = state[2][2];
        state[2][2] = temp;
        temp = state[2][1];
        state[2][1] = state[2][3];
        state[2][3] = temp;

        // Row 3: shift left by 3 (or right by 1)
        temp = state[3][3];
        state[3][3] = state[3][2];
        state[3][2] = state[3][1];
        state[3][1] = state[3][0];
        state[3][0] = temp;
    }

    // InvShiftRows
    private void invShiftRows(byte[][] state) {
        // Row 1: shift right by 1
        byte temp = state[1][3];
        state[1][3] = state[1][2];
        state[1][2] = state[1][1];
        state[1][1] = state[1][0];
        state[1][0] = temp;

        // Row 2: shift right by 2
        temp = state[2][0];
        state[2][0] = state[2][2];
        state[2][2] = temp;
        temp = state[2][1];
        state[2][1] = state[2][3];
        state[2][3] = temp;

        // Row 3: shift right by 3 (or left by 1)
        temp = state[3][0];
        state[3][0] = state[3][1];
        state[3][1] = state[3][2];
        state[3][2] = state[3][3];
        state[3][3] = temp;
    }

    // Galois Field multiplication
    private byte gmul(byte a, byte b) {
        byte p = 0;
        for (int i = 0; i < 8; i++) {
            if ((b & 1) != 0) {
                p ^= a;
            }
            boolean hiBitSet = (a & 0x80) != 0;
            a <<= 1;
            if (hiBitSet) {
                a ^= 0x1b;
            }
            b >>= 1;
        }
        return p;
    }

    // MixColumns
    private void mixColumns(byte[][] state) {
        for (int c = 0; c < 4; c++) {
            byte s0 = state[0][c];
            byte s1 = state[1][c];
            byte s2 = state[2][c];
            byte s3 = state[3][c];

            state[0][c] = (byte) (gmul(s0, (byte) 2) ^ gmul(s1, (byte) 3) ^ s2 ^ s3);
            state[1][c] = (byte) (s0 ^ gmul(s1, (byte) 2) ^ gmul(s2, (byte) 3) ^ s3);
            state[2][c] = (byte) (s0 ^ s1 ^ gmul(s2, (byte) 2) ^ gmul(s3, (byte) 3));
            state[3][c] = (byte) (gmul(s0, (byte) 3) ^ s1 ^ s2 ^ gmul(s3, (byte) 2));
        }
    }

    // InvMixColumns
    private void invMixColumns(byte[][] state) {
        for (int c = 0; c < 4; c++) {
            byte s0 = state[0][c];
            byte s1 = state[1][c];
            byte s2 = state[2][c];
            byte s3 = state[3][c];

            state[0][c] = (byte) (gmul(s0, (byte) 14) ^ gmul(s1, (byte) 11) ^ gmul(s2, (byte) 13) ^ gmul(s3, (byte) 9));
            state[1][c] = (byte) (gmul(s0, (byte) 9) ^ gmul(s1, (byte) 14) ^ gmul(s2, (byte) 11) ^ gmul(s3, (byte) 13));
            state[2][c] = (byte) (gmul(s0, (byte) 13) ^ gmul(s1, (byte) 9) ^ gmul(s2, (byte) 14) ^ gmul(s3, (byte) 11));
            state[3][c] = (byte) (gmul(s0, (byte) 11) ^ gmul(s1, (byte) 13) ^ gmul(s2, (byte) 9) ^ gmul(s3, (byte) 14));
        }
    }

    // AddRoundKey
    private void addRoundKey(byte[][] state, int round) {
        for (int c = 0; c < 4; c++) {
            for (int r = 0; r < 4; r++) {
                state[r][c] ^= roundKeys[round][r * 4 + c];
            }
        }
    }

    // Convert byte array to state matrix
    private byte[][] toState(byte[] input) {
        byte[][] state = new byte[4][4];
        for (int i = 0; i < 16; i++) {
            state[i % 4][i / 4] = input[i];
        }
        return state;
    }

    // Convert state matrix to byte array
    private byte[] fromState(byte[][] state) {
        byte[] output = new byte[16];
        for (int i = 0; i < 16; i++) {
            output[i] = state[i % 4][i / 4];
        }
        return output;
    }

    // Encrypt single block
    private byte[] encryptBlock(byte[] input) {
        byte[][] state = toState(input);

        // Initial round
        addRoundKey(state, 0);

        // Main rounds
        for (int round = 1; round < ROUNDS; round++) {
            subBytes(state);
            shiftRows(state);
            mixColumns(state);
            addRoundKey(state, round);
        }

        // Final round (no MixColumns)
        subBytes(state);
        shiftRows(state);
        addRoundKey(state, ROUNDS);

        return fromState(state);
    }

    // Decrypt single block
    private byte[] decryptBlock(byte[] input) {
        byte[][] state = toState(input);

        // Initial round
        addRoundKey(state, ROUNDS);

        // Main rounds (reverse order)
        for (int round = ROUNDS - 1; round > 0; round--) {
            invShiftRows(state);
            invSubBytes(state);
            addRoundKey(state, round);
            invMixColumns(state);
        }

        // Final round
        invShiftRows(state);
        invSubBytes(state);
        addRoundKey(state, 0);

        return fromState(state);
    }

    @Override
    public String encrypt(String plainText) {
        byte[] input = plainText.getBytes(StandardCharsets.UTF_8);

        // PKCS7 Padding
        int padLen = BLOCK_SIZE - (input.length % BLOCK_SIZE);
        byte[] padded = new byte[input.length + padLen];
        System.arraycopy(input, 0, padded, 0, input.length);
        for (int i = input.length; i < padded.length; i++) {
            padded[i] = (byte) padLen;
        }

        // Encrypt blocks
        byte[] output = new byte[padded.length];
        for (int i = 0; i < padded.length; i += BLOCK_SIZE) {
            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(padded, i, block, 0, BLOCK_SIZE);
            byte[] encrypted = encryptBlock(block);
            System.arraycopy(encrypted, 0, output, i, BLOCK_SIZE);
        }

        return Base64.getEncoder().encodeToString(output);
    }

    @Override
    public String decrypt(String cipherText) {
        byte[] input = Base64.getDecoder().decode(cipherText);
        byte[] output = new byte[input.length];

        // Decrypt blocks
        for (int i = 0; i < input.length; i += BLOCK_SIZE) {
            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(input, i, block, 0, BLOCK_SIZE);
            byte[] decrypted = decryptBlock(block);
            System.arraycopy(decrypted, 0, output, i, BLOCK_SIZE);
        }

        // Remove PKCS7 Padding
        int padLen = output[output.length - 1] & 0xFF;
        if (padLen > 0 && padLen <= BLOCK_SIZE) {
            byte[] unpadded = new byte[output.length - padLen];
            System.arraycopy(output, 0, unpadded, 0, unpadded.length);
            return new String(unpadded, StandardCharsets.UTF_8);
        }

        return new String(output, StandardCharsets.UTF_8);
    }
}
