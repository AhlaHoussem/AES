

/**
 * Class of AES algorithm (AES : Advanced Encryption Standard) :
 * The AES is designed for encrypting and decrypting a plaintext using AES Algorithm with different key size (128 bits, 192 bits, 256bits)
 *
 * @author J.Houssem
 * @version 2.2
 */
public class AesAlgorithm {

    private static final int NB_VALUE = 4;
    private int Nk;
    private int Nb;
    private int Nr;
    protected String debugging = "";


    SubByteBox subByteBox = new SubByteBox();

    byte[][] subBox = subByteBox.subBox;
    byte[][] subBoxInv = subByteBox.subboxInv;


    public AesAlgorithm(Key key) {
        this.Nb = key.getNb();
        this.Nr = key.getNr();
        this.Nk = key.getNk();
    }


    /**
     * prints the elements of a two dimensional array in a string. The elements will be iterated column wise.
     *
     * @param strings_arr The two dimensional array
     * @param <T> the string
     * @return elements of the the two dimensional array
     */
    public <T> String printStringFromMatrix(T[][] strings_arr) {

        // Initializing an empty array
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < strings_arr.length; i++) {
            for (int j = 0; j < strings_arr[i].length; j++) {
                result.append(strings_arr[j][i]);
            }
        }
        return result.toString();
    }


    /**
     * prints a string in two dimensional array format. Every element of the array is consisted of two characters.
     *
     * @param string input
     * @return a string in two dimensional array format
     */
    public String printMatrixFromString(String string) {

//      input:  00112233 44556677 8899 AABB CCDDEEFF

//        output:
//        00 44 88 CC
//        11 55 99 DD
//        22 66 AA EE
//        33 77 BB FF

        String result = "";
        for (int i = 0; i < string.length() / Nb; i += 2) {
            result += string.substring(i, i + 2) + " ";
            result += string.substring(i + 8, i + 10) + " ";
            result += string.substring(i + 16, i + 18) + " ";
            result += string.substring(i + 24, i + 26) + "\n";

        }
        return result;
    }


    /**
     * converts the elements (bytes) of two dimensional array to to hexadecimal string.
     *
     * @param bytes_arr array of bytes
     * @return gives back an array of bytes
     */
    public String[][] byteArrToHexMatrix(byte[][] bytes_arr) {

        // Initializing the 2x2 array

        String[][] result = new String[4][4];
        for (int i = 0; i < bytes_arr.length; i++) {
            for (int j = 0; j < bytes_arr[i].length; j++) {
                result[j][i] = String.format("%02X", bytes_arr[j][i]);
            }
        }
        return result;
    }


    /**
     * encrypt the input message (bytesMessage).
     *
     * @param bytesMessage      byte[][] message which will be encrypted
     * @param wordsKeyExpansion int[] key expansion array (generated by key byte array)
     * @return byte[][]
     */
    public byte[][] cipher(byte[][] bytesMessage, int[] wordsKeyExpansion) {
        byte[][] state;
        state = bytesMessage;


//    After addRoundKey(0):
//    00112233445566778899AABBCCDDEEFF
//    After subBytes:
//    638293C31BFC33F5C4EEACEA4BC12816
//    After shiftRows:
//    63FCAC161BEE28C3C4C193F54B8233EA
//    After mixColumns:
//    6379E6D9F467FB76AD063CF4D2EB8AA3
//    After addRoundKey(1):
//    6379E6D9F467FB76AD063CF4D2EB8AA3

        // Initialising Cipher initial state
        state = addRoundKey(state, wordsKeyExpansion, 0);
        String[][] staty = byteArrToHexMatrix(state);

        this.debugging += "After addRoundKey(0): \n";
        this.debugging += printStringFromMatrix(staty) + "\n";

        // Adding a RoundKey(0) to the cipher state

        for (int round = 1; round <= Nr - 1; round++) {
            state = subBytes(state);          // SubByting the state
            String[][] state_subByte = byteArrToHexMatrix(state);

            this.debugging += "After subBytes : \n";
            this.debugging += printStringFromMatrix(state_subByte) + "\n";


            state = shiftRows(state);         // Shifting the rows of the states
            String[][] state_shift = byteArrToHexMatrix(state);

            this.debugging += "After shiftRows : \n";
            this.debugging += printStringFromMatrix(state_shift) + "\n";

            state = mixColumns(state);        // Mixing the rows of the states
            String[][] state_mix = byteArrToHexMatrix(state);

            this.debugging += "After mixColumns : \n";
            this.debugging += printStringFromMatrix(state_mix) + "\n";

            state = addRoundKey(state, wordsKeyExpansion, round * Nb); // Adding a RoundKey[1,9] to the cipher state
            String[][] state_add = byteArrToHexMatrix(state);

            this.debugging += "After addRoundKey(" + round + ") : \n";
            this.debugging += printStringFromMatrix(state_add) + "\n";

        }
        state = subBytes(state);
        String[][] state_subByte = byteArrToHexMatrix(state);
        this.debugging += "After subBytes : \n";
        this.debugging += printStringFromMatrix(state_subByte) + "\n";

        state = shiftRows(state);
        String[][] state_shift = byteArrToHexMatrix(state);

        this.debugging += "After shiftRows : \n";
        this.debugging += printStringFromMatrix(state_shift) + "\n";

        state = addRoundKey(state, wordsKeyExpansion, Nr * Nb);
        String[][] state_add = byteArrToHexMatrix(state);

        this.debugging += "After addRoundKey(" + Nr + ") : \n";
        this.debugging += printStringFromMatrix(state_add) + "\n";
        this.debugging += "\nThe ciphertext : \n";
        this.debugging += printStringFromMatrix(state_add) + "\n";

        return state;
    }



    /**
     * applying xor operation on the key's expansion to the state
     *
     * @param state two dimensional array of bytes
     * @param w array of int
     * @param l length
     * @return updated state : state after adding round key
     */
    private byte[][] addRoundKey(byte[][] state, int[] w, int l) {

        byte[][] stateNew = new byte[state.length][state[0].length];

        for (int i = 0; i < Nb; i++) {
            stateNew[0][i] = (byte) (state[0][i] ^ getByte(w[l + i], 3));
            stateNew[1][i] = (byte) (state[1][i] ^ getByte(w[l + i], 2));
            stateNew[2][i] = (byte) (state[2][i] ^ getByte(w[l + i], 1));
            stateNew[3][i] = (byte) (state[3][i] ^ getByte(w[l + i], 0));
        }
        return stateNew;
    }

    private byte getByte(int value, int iByte) {
        return (byte) ((value >>> (iByte * 8)) & 0x000000ff);
    }


    /**
     * calculates the stat by applying the state_subBox transformation
     *
     * @param state a byte matrix of the state
     * @return updated state : after applying the subBox transformation
     */
    private byte[][] subBytes(byte[][] state) {
        for (int i = 0; i < state.length; i++)
            for (int j = 0; j < state[i].length; j++)
                state[i][j] = subBoxTransform(state[i][j]);
        return state;
    }


    /**
     * transform a single byte by applying the state_subBox transformation
     *
     * @param value value before the subBoxTransform
     * @return updated byte
     */
    private byte subBoxTransform(byte value) {
        // 0000
        byte bUpper, bLower;
        bUpper = (byte) ((byte) (value >> 4) & 0x0f);
        // test the out of index case
        bLower = (byte) (value & 0x0f);

        return subBox[bUpper][bLower];
    }


    /**
     * shifts the rows of the state matrix :
     * first row stays the same
     * second row rotate counterclockwise by 1
     * third row rotate counterclockwise by 2
     * fourth row rotate counterclockwise by 3
     *
     * @param state state before shifting
     * @return state after shifting
     */
    private byte[][] shiftRows(byte[][] state) {
        byte[][] stateNew = new byte[state.length][state[0].length]; //initialising an empty state

        // row_0 is not shifted
        stateNew[0] = state[0];
        for (int r = 1; r < state.length; r++)
            for (int c = 0; c < state[r].length; c++)
                stateNew[r][c] = state[r][(c + shift(r, Nb)) % Nb];

        return stateNew;
    }

    private static int shift(int r, int Nb) {
        return r;
    }


    /**
     * mixes columns of the state :The four numbers of one column are modulo multiplied
     * in Rijindael's Galois field by a given matrix. The mix columns step along with the shiftRows step is the primary source
     * of diffusion in Rijindael.
     *
     * @param state state before the mixing
     * @return state after mixing
     */
    private byte[][] mixColumns(byte[][] state) {
        byte[][] stateNew = new byte[state.length][state[0].length]; //initialising an empty state

//       the predefined matrix used for the mix:
//        [[02, 03, 01, 01]
//         [01, 02, 03, 01]
//         [01, 01, 02, 03]
//         [03, 01, 01, 02]]


        // 63 * 02 + F2 * 03 + 7D * 01 + D4 * 01
        //(0)11000110 + (1)11100100 xor 00011011(0x1B) + 11110010 + 01111101 + 11010100 = 01100010 (0x62)
        for (int i = 0; i < Nb; i++) {
            stateNew[0][i] = xor4Bytes(finiteMultiplication(state[0][i], 0x02),
                    finiteMultiplication(state[1][i], 0x03),
                    state[2][i], state[3][i]);
            stateNew[1][i] = xor4Bytes(state[0][i],
                    finiteMultiplication(state[1][i], 0x02),
                    finiteMultiplication(state[2][i], 0x03),
                    state[3][i]);
            stateNew[2][i] = xor4Bytes(state[0][i], state[1][i],
                    finiteMultiplication(state[2][i], 0x02),
                    finiteMultiplication(state[3][i], 0x03));
            stateNew[3][i] = xor4Bytes(finiteMultiplication(state[0][i], 0x03),
                    state[1][i], state[2][i],
                    finiteMultiplication(state[3][i], 0x02));
        }
        return stateNew;
    }

    private byte xor4Bytes(byte b1, byte b2, byte b3, byte b4) {
        //applying the xor operator on the four bytes and gives back the result in byte
        byte result = 0;
        result ^= b1;
        result ^= b2;
        result ^= b3;
        result ^= b4;
        return result;
    }

    private byte finiteMultiplication(int v1, int v2) {
        return finiteMultiplication((byte) v1, (byte) v2);
    }

    private byte finiteMultiplication(byte v1, byte v2) {
        byte[] temp = new byte[8];
        byte bResult = 0;
        temp[0] = v1;
        for (int i = 1; i < temp.length; i++) {
            temp[i] = xtime(temp[i - 1]);
        }
        for (int i = 0; i < temp.length; i++) {
            if (getBit(v2, i) != 1) {
                temp[i] = 0;
            }
            bResult ^= temp[i];
        }
        return bResult;
    }

    private byte xtime(byte value) {
        int result;
        result = (value & 0x000000ff) * 02;
        return (byte) (((result & 0x100) != 0) ? result ^ 0x11b : result);
    }

    private static byte getBit(byte value, int i) {
        final byte[] bMasks = {(byte) 0x01, (byte) 0x02, (byte) 0x04,
                (byte) 0x08, (byte) 0x10, (byte) 0x20,
                (byte) 0x40, (byte) 0x80};
        byte bBit = (byte) (value & bMasks[i]);
        return (byte) ((byte) (bBit >> i) & (byte) 0x01);
    }


    /**
     * decrypt the input message (bytesMessage).
     *
     * @param bytesMessage      byte[][] message(encrypted) which will be decrypted
     * @param wordsKeyExpansion int[] key expansion array (generated by key byte array)
     * @return byte[][]
     */
    public byte[][] invCipher(byte[][] bytesMessage, int[] wordsKeyExpansion) {
        byte[][] state;
        state = bytesMessage;

        state = addRoundKey(state, wordsKeyExpansion, Nr * Nb);
        String[][] state_add = byteArrToHexMatrix(state);

        this.debugging += "After addRoundKey(" + Nr + ") : \n";
        this.debugging += printStringFromMatrix(state_add) + "\n";


//        After invShiftRows:
//        0CB0634ACDFE3FCA8FC1059DAF5FDA5B
//        After invSubBytes:
//        81FC005C800C251073DD36751B847A57
//        After addRoundKey(13):
//        F5110BFDF3975B35518C9B61D5A4AE6C
//        After invMixColumns:
//        713AABF2F7F04B46F13F759C9CE19A54


        for (int round = (Nr - 1); round >= 1; round--) {
            state = invShiftRows(state);

            String[][] state_invShift = byteArrToHexMatrix(state);
            this.debugging += "After inverseShiftRows : \n";
            this.debugging += printStringFromMatrix(state_invShift) + "\n";

            state = invSubBytes(state);

            String[][] state_invSubBytes = byteArrToHexMatrix(state);
            this.debugging += "After inverseSubByte : \n";
            this.debugging += printStringFromMatrix(state_invSubBytes) + "\n";

            state = addRoundKey(state, wordsKeyExpansion, round * Nb);

            String[][] state_addRound = byteArrToHexMatrix(state);
            this.debugging += "After addRoundKey(" + round + ") : \n";
            this.debugging += printStringFromMatrix(state_addRound) + "\n";

            state = invMixColumns(state);

            String[][] state_invMixColumns = byteArrToHexMatrix(state);
            this.debugging += "After inverseMixColumns : \n";
            this.debugging += printStringFromMatrix(state_invMixColumns) + "\n";
        }
        state = invShiftRows(state);

        String[][] state_invShift = byteArrToHexMatrix(state);
        this.debugging += "After inverseShiftRows : \n";
        this.debugging += printStringFromMatrix(state_invShift) + "\n";

        state = invSubBytes(state);

        String[][] state_invSubBytes = byteArrToHexMatrix(state);
        this.debugging += "After inverseSubByte : \n";
        this.debugging += printStringFromMatrix(state_invSubBytes) + "\n";

        state = addRoundKey(state, wordsKeyExpansion, 0);

        String[][] state_addRound = byteArrToHexMatrix(state); //xor it with iv vector
        this.debugging += "After addRoundKey(0) : \n";
        this.debugging += printStringFromMatrix(state_addRound) + "\n";

        this.debugging += "\nThe decrypted ciphertext is : \n";
        this.debugging += printStringFromMatrix(state_addRound) + "\n";

        return state;
    }


    /**
     * shifts inversely the rows of the state matrix :
     *
     * @param state state before inverse shifting
     * @return state after inverse shifting
     */
    private byte[][] invShiftRows(byte[][] state) {
        byte[][] stateNew = new byte[state.length][state[0].length];
        // r=0 is not shifted
        stateNew[0] = state[0];
        for (int r = 1; r < state.length; r++)
            for (int c = 0; c < state[r].length; c++)
                stateNew[r][(c + shift(r, Nb)) % Nb] = state[r][c];

        return stateNew;
    }


    /**
     * calculates the stat by applying the inverse state_subBox transformation
     *
     * @param state a byte matrix of the state
     * @return updated state : after applying the inverse subBox transformation
     */
    private byte[][] invSubBytes(byte[][] state) {
        for (int i = 0; i < state.length; i++)
            for (int j = 0; j < state[i].length; j++)
                state[i][j] = invSboxTransform(state[i][j]);
        return state;
    }


    /**
     * transform a single byte by applying the inverse state_subBox transformation
     *
     * @param value before inverse state_subBox transformation
     * @return value in byte after inverse state_subBox transformation
     */
    private byte invSboxTransform(byte value) {
        byte bUpper = 0, bLower = 0;
        bUpper = (byte) ((byte) (value >> 4) & 0x0f);
        bLower = (byte) (value & 0x0f);
        return subBoxInv[bUpper][bLower];
    }


    /**
     * mixes columns of the state inversely :The four numbers of one column are modulo multiplied
     * in Rijindael's Galois field by a given matrix. The mix columns step along with the shiftRows step is the primary source
     * of diffusion in Rijindael.
     *
     * @param state state before the mixing
     * @return state after mixing
     */
    private byte[][] invMixColumns(byte[][] state) {
        byte[][] stateNew = new byte[state.length][state[0].length];
        for (int c = 0; c < Nb; c++) {
            stateNew[0][c] = xor4Bytes(finiteMultiplication(state[0][c], 0x0e),
                    finiteMultiplication(state[1][c], 0x0b),
                    finiteMultiplication(state[2][c], 0x0d),
                    finiteMultiplication(state[3][c], 0x09));
            stateNew[1][c] = xor4Bytes(finiteMultiplication(state[0][c], 0x09),
                    finiteMultiplication(state[1][c], 0x0e),
                    finiteMultiplication(state[2][c], 0x0b),
                    finiteMultiplication(state[3][c], 0x0d));
            stateNew[2][c] = xor4Bytes(finiteMultiplication(state[0][c], 0x0d),
                    finiteMultiplication(state[1][c], 0x09),
                    finiteMultiplication(state[2][c], 0x0e),
                    finiteMultiplication(state[3][c], 0x0b));
            stateNew[3][c] = xor4Bytes(finiteMultiplication(state[0][c], 0x0b),
                    finiteMultiplication(state[1][c], 0x0d),
                    finiteMultiplication(state[2][c], 0x09),
                    finiteMultiplication(state[3][c], 0x0e));
        }
        return stateNew;

    }
}
