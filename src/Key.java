

/**
 * Class Key represents the key used for the AES encryption and decrytpion
 *
 * @author J.Houssem
 * @version 1.7
 */



public class Key {

    private static final int KEY_SIZE_128 = 128;
    private static final int KEY_SIZE_192 = 192;
    private static final int KEY_SIZE_256 = 256;

    public static final int NB_VALUE = 4;
    private int Nk;
    private int Nb = NB_VALUE;
    private int Nr;


    // AES-128 Nk=4, Nr=10, Nb=4
    // AES-192 Nk=6, Nr=12, Nb=4
    // AES-256 Nk=8, Nr=14, Nb=4

    /**
     * initialises the Key used for AES the algorithm and the values nb,nk,nr.
     * The number of columns Nb is the block size divided by 64.
     * The number of colums of the cipher key Nk
     * The number of rounds Nr : is predefined for Nr = 10 (128bits), Nr = 12 (192bits), Nr = 14 (256bits)
     * of the cipher key, equal to Nb.
     *
     * @param hexBlockLength the length of the the key
     */
    public Key(int hexBlockLength) {
        switch (hexBlockLength) {
            case KEY_SIZE_128 / 4 -> {
                Nk = 4;
                Nr = 10;
            }
            case KEY_SIZE_192 / 4 -> {
                Nk = 6;
                Nr = 12;
            }
            case KEY_SIZE_256 / 4 -> {
                Nk = 8;
                Nr = 14;
            }
            default -> throw new UnsupportedOperationException(
                    "key length should only be:128, 192 or 256");
        }
    }

    /**
     * checks if the key size is valid. A valid key size is 128, 192, 256bits
     *
     * @param keySize int the size of the key in bits
     * @return boolean true if the key size is valid and false if not
     */
    public boolean isValidKeySize(int keySize) {
        return keySize == Key.KEY_SIZE_128 ||
                keySize == Key.KEY_SIZE_192 ||
                keySize == Key.KEY_SIZE_256;

    }


    /**
     * expands the key for the AES algorithm : The AES key expansion algorithm takes
     * as input a four-word (16-byte) key and produces a linear array of 44 words (176 bytes).
     *
     * @param key represented in array of bytes
     * @param w word represented in array of int
     */

    private void keyExpansion(byte[] key, int[] w) {
        int iTemp = 0;
        int i = 0;

        //    K[n] : W[i] = K[n-1] : W[i] xor K[n] : W[i-1]
        //    K[n] : W[0] = K[n-1] : W[0] xor SubByte (K[n-1] : W[3]>>8) xor Rcon[i]

        //K1 :
        //    w0 = 0f 15 71 c9
        //    w1 = 47 d9 e8 59
        //    w2 = 0c b7 ad
        //    w3 = af 7f 67 98
        //
        //    K2:W0 = K1:W0 xor SubByte (K1: W3>>8) xor Rcon[2]
        //    0f 15 71 c9 xor SubByte (af 7f 67 98) >>8) xor 02 00 00 00(from the table)
        //    0f 15 71 c9 xor SubByte (7f 67 98 af) xor 02 00 00 00 = df 90 37 b0

        while (i < Nk) {
            w[i] = byteToInt(key[4 * i], key[4 * i + 1], key[4 * i + 2],
                    key[4 * i + 3]);
            i++;
        }

        i = Nk;
        SubByteBox subBox = new SubByteBox();

        while (i < Nb * (Nr + 1)) {
            iTemp = w[i - 1];
            if (i % Nk == 0) {
                iTemp = subWord(rotWord(iTemp)) ^ subBox.Rcon[i / Nk];
            } else if (Nk > 6 && i % Nk == 4) {
                iTemp = subWord(iTemp);
            }
            w[i] = w[i - Nk] ^ iTemp;
            i++;
        }
    }


    private byte getByte(int value, int iByte) {
        return (byte) ((value >>> (iByte * 8)) & 0x000000ff);
    }

    private int subWord(int word) {
        int newWord = 0;
        newWord ^= (int) subBoxTransform((byte) (word >>> 24)) & 0x000000ff;
        newWord <<= 8;

        newWord ^= (int) subBoxTransform((byte) ((word & 0xff0000) >>> 16)) &
                0x000000ff;
        newWord <<= 8;

        newWord ^= (int) subBoxTransform((byte) ((word & 0xff00) >>> 8)) &
                0x000000ff;
        newWord <<= 8;

        newWord ^= (int) subBoxTransform((byte) (word & 0xff)) & 0x000000ff;

        return newWord;
    }

    /**
     * transform a single byte by applying the state_subBox transformation
     *
     * @param value
     * @return updated byte
     */
    private byte subBoxTransform(byte value) {

        byte bUpper, bLower;
        bUpper = (byte) ((byte) (value >> 4) & 0x0f);
        // test the out of index case
        bLower = (byte) (value & 0x0f);
        SubByteBox subBox = new SubByteBox();
        return subBox.subBox[bUpper][bLower];
    }

    private static int rotWord(int word) {
        return (word << 8) ^ ((word >> 24) & 0x000000ff);
    }

    private static int byteToInt(byte b1, byte b2, byte b3, byte b4) {
        int word = 0;
        word ^= ((int) b1) << 24;

        word ^= (((int) b2) & 0x000000ff) << 16;

        word ^= (((int) b3) & 0x000000ff) << 8;

        word ^= (((int) b4) & 0x000000ff);
        return word;
    }



    /**
     * create a key expansion for specified key(byte array)
     *
     * @param key byte[] AES key
     * @return int[] AES key expansion
     */
    public int[] createKeyExpansion(byte[] key) {
        int[] w = new int[Nb * (Nr + 1)];
        keyExpansion(key, w);
        return w;
    }

    /**
     * generates a random key
     *
     * @return array of bytes
     */
    public byte[] createKey(int nk) {
        byte[] key = new byte[4 * nk];
        java.util.Random rndGen = new java.util.Random();
        rndGen.nextBytes(key);
        return key;
    }

    public int getNk() {
        return Nk;
    }

    public int getNb() {
        return Nb;
    }

    public int getNr() {
        return Nr;
    }
}
