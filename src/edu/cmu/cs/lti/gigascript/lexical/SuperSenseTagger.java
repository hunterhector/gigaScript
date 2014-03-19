package edu.cmu.cs.lti.gigascript.lexical;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;

import java.io.*;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 2/26/14
 * Time: 1:17 AM
 */
public class SuperSenseTagger {
    public static void main(String[] args) throws IOException {
        String inputPath = args[0];
        String outputPath = args[1];

        File outFile = new File(outputPath);

        // if file doesnt exists, then create it
        if (!outFile.exists()) {
            outFile.createNewFile();
        }

        FileWriter fw = new FileWriter(outFile.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);

        String line;
        FileReader file = new FileReader(inputPath);
        BufferedReader br = new BufferedReader(file);

        String wordNetDirectory = "/Users/zhengzhongliu/tools/wnDict/";
        URL url = new URL("file", null, wordNetDirectory);

        //construct the Dictionary object and open it
        IDictionary dict = new Dictionary(url);
        dict.open();

        while ((line = br.readLine()) != null) {
            String[] parts = line.trim().split("\t");

            if (parts.length != 5) {
                continue;
            }

            String[] tuple = parts[0].split(",");

            String arg1Type = parts[3];
            String arg2Type = parts[4];

            if (tuple.length >= 3) {
                String arg2 = tuple[tuple.length - 2];
                String arg1 = tuple[0];

                if (arg1Type.equals("-") || arg1Type.equals("MISC") || arg1Type.equals("O")) {
                    arg1Type = getWordNetSense(arg1, dict);
                }

                if (arg2Type.equals("-") || arg2Type.equals("MISC") || arg1Type.equals("O")) {
                    arg2Type = getWordNetSense(arg2, dict);
                }

                bw.write(String.format("%s\t%s\t%s\t%s\t%s\n", parts[0], parts[1], parts[2], arg1Type, arg2Type));
            }
        }
        br.close();
        bw.close();
    }

    private static String getWordNetSense(String str, IDictionary dict) {
        try {
            IIndexWord idxWord = dict.getIndexWord(str, POS.NOUN);
            if (idxWord == null) {
                return "-";
            }

            IWordID wordID = idxWord.getWordIDs().get(0);
            IWord word = dict.getWord(wordID);
            ISynset synset = word.getSynset();
            String LexFileName = synset.getLexicalFile().getName();
            return LexFileName.replace("noun.", "");
        } catch (Exception e) {
            return "-";
        }
    }
}
