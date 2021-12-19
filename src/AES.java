import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Class of AES algorithm (AES : Advanced Encryption Standard) :
 * The AES is designed for encrypting and decrypting a plaintext using AES Algorithm.
 *
 * @author J.Houssem, E.Bora, D.Yunus
 * @version 1.8
 */
public class AES {

    public static void main(String[] args) throws IOException {
        AES aes = new AES();
        long startTime = -System.nanoTime();
        aes.runSimulation(args);
        long stopTime = System.nanoTime();
        System.out.println("the running time is : " + Long.sum(stopTime, startTime) + "  nano sec");
    }

    public void runSimulation(String[] args) throws IOException {

        String option = args[0];// TODO change this args[0];
        String mode = args[1]; // TODO change this args[1];

        InputOutput project = new InputOutput();


        ArrayList<String> providedKey = project.textFileToArrayList(args[2]);
        ArrayList<String> providedPlaintext = project.textFileToArrayList(args[3]);
        providedPlaintext.removeAll(Collections.singleton(""));

        // In case of decryption, the encrypted plaintext will be the one to decrypt. TODO encryption should be done first
        if (option.equals("d")){providedPlaintext = project.textFileToArrayList(args[4]);}


        String key = providedKey.get(0).replaceAll(" ", ""); // len(key) = 32 = 128 bits
        byte[] key_byte = decode(key);
        Key keyFile = new Key(key.length());
        int[] wordsKeyExpansion = keyFile.createKeyExpansion(key_byte); // expanding the key
        byte[] VectorIV = keyFile.createKey(4); //  creating a random IV vector for the cbc mode


        for (int k = 0; k < providedPlaintext.size(); k++) {

            String plainText = providedPlaintext.get(k).replaceAll(" ", ""); // len(plaintext) is fixed = 32 hex char


            AesAlgorithm aesAlgorithm = new AesAlgorithm(keyFile);

            String step_by_step = aesAlgorithm.debugging; // initialising the debugging output

            ArrayList<String> output = new ArrayList<>();
            ArrayList<String> debuggingOutput = new ArrayList<>();
            String result = "";



            if (option.equals("e")) { // encryption

                byte[] text_byte = decode(plainText);

                if (mode.equals("cbc")) {
                    step_by_step += "The Encryption mode is Cipher block chaining (CBC). " +
                            "The initialized IV vector is: \n" + encode(VectorIV);

                    // random vector XOR the plaintext
                    byte[] arg = new byte[16];
                    int i = 0;
                    for (byte b : text_byte){
                        arg[i] = (byte) (b ^ VectorIV[i++]);}
                    text_byte = arg;

                } else {
                    step_by_step += "The Encryption mode is Electronic codebook (ECB) ";

                }

                byte[][] bytesMessageMatrix = arrayToMatrix(text_byte);

                step_by_step += "\n\n\t\t\t\t\t\t THE ENCRYPTION PHASE (" + (k + 1) +  ")" + "\n\n\n";
                step_by_step += "The Plaintext is :\n";
                step_by_step += aesAlgorithm.printMatrixFromString(encode(text_byte)) + "\n"; // plaintext in matrix form
                step_by_step += "The CipherKey is : \n";
                step_by_step += aesAlgorithm.printMatrixFromString(encode(key_byte)) + "\n"; // ChiperKey in matrix form

                // the aes encryption part
                String[][] encrypted_cipher_text = aesAlgorithm.byteArrToHexMatrix(aesAlgorithm.cipher(bytesMessageMatrix, wordsKeyExpansion));

                step_by_step += aesAlgorithm.debugging;
                debuggingOutput.add(step_by_step); // Print the outputs of the intermediate steps. plaintext enc debugging File will be create


                // creating the list of outputs
                for (int i = 0; i < encrypted_cipher_text.length; i++) {
                    for (int j = 0; j < encrypted_cipher_text[0].length; j++) {
                        result += (encrypted_cipher_text[j][i]);
                    }
                }

                if (mode.equals("cbc")) { VectorIV = decode(result);} // cbc Mode

                output.add(result);
                project.arrayListToTextFile(output, args[4]); // creating the enc plaintext enc file
                project.arrayListToTextFile(debuggingOutput, args[6]); // creating the enc debugging file


            } else if (option.equals("d")) {

                byte[] text_byte = decode(plainText);
                byte[][] bytesMessageMatrix = arrayToMatrix(text_byte);


                if (mode.equals("cbc")) {

                    // Using the same IV vector for the cbc decryption
                    ArrayList<String> vectorFile = project.textFileToArrayList(args[6]);
                    String IV_vector = vectorFile.get(1);

                    // assigning the same Iv vector for encryption only at the first line
                    if (k == 0) {VectorIV = decode(IV_vector);}

                    step_by_step += "The Decryption mode is Cipher block chaining (CBC). The initialized IV vector is: \n" + IV_vector;
                } else {
                    step_by_step += "The Decryption mode is Electronic codebook (ECB) ";

                }


                // the aes decryption part
                String[][] decrypted_cipher_text = aesAlgorithm.byteArrToHexMatrix(aesAlgorithm.invCipher(bytesMessageMatrix, wordsKeyExpansion));

                // initialising the debugging output of decryption
                step_by_step += "\n\n\t\t\t\t\t\t THE DECRYPTION PHASE (" + (k + 1) + ")" + "\n\n\n";
                step_by_step += aesAlgorithm.debugging;
                debuggingOutput.add(step_by_step);


                // creating the list of outputs
                for (int i = 0; i < decrypted_cipher_text.length; i++) {
                    for (int j = 0; j < decrypted_cipher_text[0].length; j++) {
                        result += (decrypted_cipher_text[j][i]);
                    }
                }

                if (mode.equals("cbc")) {
                    byte[] byte_result = decode(result);

                    // the result of the decryption XOR the plaintext
                    byte[] arg = new byte[16];
                    int i = 0;
                    for (byte b : byte_result) {
                        arg[i] = (byte) (b ^ VectorIV[i++]);
                    }

                    byte_result = arg;
                    result = encode(byte_result);
                    VectorIV = text_byte;  // assigning the plaintext to the Vector IV: vector will be xoring the initial plaintext
                }

                output.add(result);
                project.arrayListToTextFile(output, args[5]);
                project.arrayListToTextFile(debuggingOutput, args[7]); // dec debugging


            } else {
                throw new IllegalArgumentException("Invalid option was given");
            }
        }
    }


        private String encode ( byte[] data){

            String result = "";
            for (byte i : data) {
                result += String.format("%02X", i);
            }
            return result;
        }

        private byte[] decode (String data){

            // Initializing the hex string and byte array

            // String s = "2f4a33";
            byte[] result = new byte[data.length() / 2];
            for (int i = 0; i < result.length; i++) {
                int index = i * 2;

                // Using parseInt() method of Integer class
                int val = Integer.parseInt(data.substring(index, index + 2), 16);
                result[i] = (byte) val;
            }
            return result;
        }

        private String[] intToHex ( int[] int_arr){

            String[] result = new String[int_arr.length];
            // Initializing the hex string and byte array
            for (int i = 0; i < int_arr.length; i++) {
                if (int_arr[i] == 0) {
                    result[i] = String.format("%08X", (byte) int_arr[i]);
                } else {
                    String tmp = Integer.toHexString(int_arr[i]);
                    result[i] = tmp.toUpperCase();
                }
            }
            return result;
        }

        private byte[][] arrayToMatrix ( byte[] arr){

            byte[][] bytesMessage = new byte[4][4];

            int j = 0;
            for (int i = 0; i < arr.length; i += 4) {
                bytesMessage[0][j] = arr[i];
                bytesMessage[1][j] = arr[i + 1];
                bytesMessage[2][j] = arr[i + 2];
                bytesMessage[3][j] = arr[i + 3];
                j++;

            }
            return bytesMessage;
        }

    }