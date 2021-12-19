import java.util.Scanner;
import java.io.IOException;
import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;


import static java.lang.System.out;


/**
 * Class inputOutput reads a text file and make an ArrayList out of it
 * and writes a text file from an ArrayList
 *
 * @author J.Houssem
 * @version 1.0
 */


public class InputOutput {


    /**
     * gives an ArrayList out of a text File
     *
     * @param path the path of the text file
     * @return ArrayList the ArrayList of every line in the file
     * @throws IOException An exception will be thrown when an error will occur during the reading of the file
     */

    public ArrayList<String> textFileToArrayList(String path) throws IOException {

//        out.println("\t\t\t\t\t\tMyFileIO starts");

        // read input file (first argument)
//        out.println("\t\t\t\t\t\tMyFileIO - read input file");

        //String inputFileName = args[0];
        File myInputFile = new File(path);
        ArrayList<String> inputArray = new ArrayList<>();
        try {
            Scanner myReader = new Scanner(myInputFile);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                //System.out.println(data);
                inputArray.add(data);
            }

        } catch (FileNotFoundException e) {
//            out.println("An exception was thrown while reading the file");
            e.printStackTrace();
        }
        return inputArray;
    }


    /**
     * writes a text file out of an given Array and path
     *
     * @param outputList the final list which will be exported to a text file
     * @param path       the path of the created text file
     * @throws IOException An exception will be thrown when an error will occur during the writing of the file
     */

    public void arrayListToTextFile(ArrayList<String> outputList, String path) throws IOException {


        // write output file (second argument)
//        out.println("\t\t\t\t\t\tMyFileIO - write output file");

        //String myOutputFile = args[1];
        File myOutputFile = new File(path);
        try {
            if (myOutputFile.createNewFile()) {
//                out.println("File created: ");

                FileWriter myWriter = new FileWriter(path);
                for (int i = 0; i < outputList.size(); i++) {
                    myWriter.write(outputList.get(i));
                    myWriter.write("\n");
                }
                myWriter.close();

            } else {
//                out.println("File already exists.");
//                out.println("The output has been appended to the existing file");

                FileWriter myWriter = new FileWriter(path, true);
//                myWriter.write("\n");
                for (int i = 0; i < outputList.size(); i++) {
                    myWriter.write(outputList.get(i));
                    myWriter.write("\n");
                }
                myWriter.close();
            }

        } catch (IOException e) {
//            out.println("An exception was thrown while writing the file");
            e.printStackTrace();
        }

//        out.println("MyFileIO ends");
    }


}



