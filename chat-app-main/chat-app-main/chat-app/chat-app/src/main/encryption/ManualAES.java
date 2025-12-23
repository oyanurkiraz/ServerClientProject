package main.encryption;

import java.util.Base64;
import java.nio.charset.StandardCharsets;

/**
 * AES-128 Manuel Implementasyon (Kütüphanesiz)
 * 
 * Bu implementasyon eğitim amaçlıdır ve AES'in çalışma prensiplerini gösterir:
 * - SubBytes (S-Box substitution)
 * - ShiftRows (Satır kaydırma)
 * - MixColumns (Sütun karıştırma)
 * - AddRoundKey (Anahtar ekleme)
 * - Key Expansion (Anahtar genişletme)
 */
public class ManualAES implements EncryptionAlgorithm {

    private static final int BLOCK_SIZE = 16; // 128 bit = 16 byte
    private static final int KEY_SIZE = 16; // AES-128
    private static final int ROUNDS = 10; // AES-128 için 10 round

    private final byte[] key;
    private final byte[][] roundKeys;

    // AES S-Box
    private static final int[] SBOX = {
            0x63, 0x7c, 0x77, 0x7b, 0xf2, 0x6b, 0x6f, 0xc5, 0x30, 0x01, 0x67, 0x2b, 0xfe, 0xd7, 0xab, 0x76,
            0xca, 0x82, 0xc9, 0x7d, 0xfa, 0x59, 0x47, 0xf0, 0xad, 0xd4, 0xa2, 0xaf, 0x9c, 0xa4, 0x72, 0xc0,
            0xb7, 0xfd, 0x93, 0x26, 0x36, 0x3f, 0xf7, 0xcc, 0x34, 0xa5, 0xe5, 0xf1, 0x71, 0xd8, 0x31, 0x15,
            0x04, 0xc7, 0x23, 0xc3, 0x18, 0x96, 0x05, 0x9a, 0x07, 0x12, 0x80, 0xe2, 0xeb, 0x27, 0xb2, 0x75,
            0x09, 0x83, 0x2c, 0x1a, 0x1b, 0x6e, 0x5a, 0xa0, 0x52, 0x3b, 0xd6, 0xb3, 0x29, 0xe3, 0x2f, 0x84,
            0x53, 0xd1, 0x00, 0xed, 0x20, 0xfc, 0xb1, 0x5b, 0x6a, 0xcb, 0xbe, 0x39, 0x4a, 0x4c, 0x58, 0xcf,
            0xd0, 0xef, 0xaa, 0xfb, 0x43, 0x4d, 0x33, 0x85, 0x45, 0xf9, 0x02, 0x7f, 0x50, 0x3c, 0x9f, 0xa8,
            0x51, 0xa3, 0x40, 0x8f, 0x92, 0x9d, 0x38, 0xf5, 0xbc, 0xb6, 0xda, 0x21, 0x10, 0xff, 0xf3, 0xd2,
            0xcd, 0x0c, 0x13, 0xec, 0x5f, 0x97, 0x44, 0x17, 0xc4, 0xa7, 0x7e, 0x3d, 0x64, 0x5d, 0x19, 0x73,
            0x60, 0x81, 0x4f, 0xdc, 0x22, 0x2a, 0x90, 0x88, 0x46, 0xee, 0xb8, 0x14, 0xde, 0x5e, 0x0b, 0xdb,
            0xe0, 0x32, 0x3a, 0x0a, 0x49, 0x06, 0x24, 0x5c, 0xc2, 0xd3, 0xac, 0x62, 0x91, 0x95, 0xe4, 0x79,
            0xe7, 0xc8, 0x37, 0x6d, 0x8d, 0xd5, 0x4e, 0xa9, 0x6c, 0x56, 0xf4, 0xea, 0x65, 0x7a, 0xae, 0x08,
            0xba, 0x78, 0x25, 0x2e, 0x1c, 0xa6, 0xb4, 0xc6, 0xe8, 0xdd, 0x74, 0x1f, 0x4b, 0xbd, 0x8b, 0x8a,
            0x70, 0x3e, 0xb5, 0x66, 0x48, 0x03, 0xf6, 0x0e, 0x61, 0x35, 0x57, 0xb9, 0x86, 0xc1, 0x1d, 0x9e,
            0xe1, 0xf8, 0x98, 0x11, 0x69, 0xd9, 0x8e, 0x94, 0x9b, 0x1e, 0x87, 0xe9, 0xce, 0x55, 0x28, 0xdf,
            0x8c, 0xa1, 0x89, 0x0d, 0xbf, 0xe6, 0x42, 0x68, 0x41, 0x99, 0x2d, 0x0f, 0xb0, 0x54, 0xbb, 0x16
    };

    // Inverse S-Box
    private static final int[] INV_SBOX = {
            0x52, 0x09, 0x6a, 0xd5, 0x30, 0x36, 0xa5, 0x38, 0xbf, 0x40, 0xa3, 0x9e, 0x81, 0xf3, 0xd7, 0xfb,
            0x7c, 0xe3, 0x39, 0x82, 0x9b, 0x2f, 0xff, 0x87, 0x34, 0x8e, 0x43, 0x44, 0xc4, 0xde, 0xe9, 0xcb,
            0x54, 0x7b, 0x94, 0x32, 0xa6, 0xc2, 0x23, 0x3d, 0xee, 0x4c, 0x95, 0x0b, 0x42, 0xfa, 0xc3, 0x4e,
            0x08, 0x2e, 0xa1, 0x66, 0x28, 0xd9, 0x24, 0xb2, 0x76, 0x5b, 0xa2, 0x49, 0x6d, 0x8b, 0xd1, 0x25,
            0x72, 0xf8, 0xf6, 0x64, 0x86, 0x68, 0x98, 0x16, 0xd4, 0xa4, 0x5c, 0xcc, 0x5d, 0x65, 0xb6, 0x92,
            0x6c, 0x70, 0x48, 0x50, 0xfd, 0xed, 0xb9, 0xda, 0x5e, 0x15, 0x46, 0x57, 0xa7, 0x8d, 0x9d, 0x84,
            0x90, 0xd8, 0xab, 0x00, 0x8c, 0xbc, 0xd3, 0x0a, 0xf7, 0xe4, 0x58, 0x05, 0xb8, 0xb3, 0x45, 0x06,
            0xd0, 0x2c, 0x1e, 0x8f, 0xca, 0x3f, 0x0f, 0x02, 0xc1, 0xaf, 0xbd, 0x03, 0x01, 0x13, 0x8a, 0x6b,
            0x3a, 0x91, 0x11, 0x41, 0x4f, 0x67, 0xdc, 0xea, 0x97, 0xf2, 0xcf, 0xce, 0xf0, 0xb4, 0xe6, 0x73,
            0x96, 0xac, 0x74, 0x22, 0xe7, 0xad, 0x35, 0x85, 0xe2, 0xf9, 0x37, 0xe8, 0x1c, 0x75, 0xdf, 0x6e,
            0x47, 0xf1, 0x1a, 0x71, 0x1d, 0x29, 0xc5, 0x89, 0x6f, 0xb7, 0x62, 0x0e, 0xaa, 0x18, 0xbe, 0x1b,
            0xfc, 0x56, 0x3e, 0x4b, 0xc6, 0xd2, 0x79, 0x20, 0x9a, 0xdb, 0xc0, 0xfe, 0x78, 0xcd, 0x5a, 0xf4,
            0x1f, 0xdd, 0xa8, 0x33, 0x88, 0x07, 0xc7, 0x31, 0xb1, 0x12, 0x10, 0x59, 0x27, 0x80, 0xec, 0x5f,
            0x60, 0x51, 0x7f, 0xa9, 0x19, 0xb5, 0x4a, 0x0d, 0x2d, 0xe5, 0x7a, 0x9f, 0x93, 0xc9, 0x9c, 0xef,
            0xa0, 0xe0, 0x3b, 0x4d, 0xae, 0x2a, 0xf5, 0xb0, 0xc8, 0xeb, 0xbb, 0x3c, 0x83, 0x53, 0x99, 0x61,
            0x17, 0x2b, 0x04, 0x7e, 0xba, 0x77, 0xd6, 0x26, 0xe1, 0x69, 0x14, 0x63, 0x55, 0x21, 0x0c, 0x7d
    };

    // Round constants
    private static final int[] RCON = {
            0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80, 0x1b, 0x36
    };

    public ManualAES(String key) {
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
                    temp[j] = (byte) SBOX[temp[j] & 0xFF];
                }

                // XOR with Rcon
                temp[0] ^= RCON[i / 4 - 1];
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
                state[i][j] = (byte) SBOX[state[i][j] & 0xFF];
            }
        }
    }

    // InvSubBytes
    private void invSubBytes(byte[][] state) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                state[i][j] = (byte) INV_SBOX[state[i][j] & 0xFF];
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
