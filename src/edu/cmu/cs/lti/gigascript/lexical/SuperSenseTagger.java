package edu.cmu.cs.lti.gigascript.lexical;

import edu.cmu.cs.lti.gigascript.util.WordNetUtils;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 2/26/14
 * Time: 1:17 AM
 */
public class SuperSenseTagger {
    private static boolean upperFormmating = false;
    public static void main(String[] args) throws IOException {
        String inputPath = args[0];
        String outputPath = args[1];
        String wnPath = args[2];// /Users/zhengzhongliu/tools/wnDict/
        String semanticTypesPath = args[3]; // semantic_types

        if (args.length > 4){
            upperFormmating = true;
            System.out.println("will use upper formatting");
        }

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

        URL url = new URL("file", null, wnPath);

        //construct the Dictionary object and open it
        IDictionary dict = new Dictionary(url);
        dict.open();

        //the target semantic types
        File targetSemanticTypes = new File(semanticTypesPath);
        Set<String> semanticTypes = new HashSet<String>();
        for (String semanticType : FileUtils.readLines(targetSemanticTypes)){
            semanticTypes.add(semanticType.trim());
        }

        while ((line = br.readLine()) != null) {
            String[] parts = line.trim().split("\t",6);

            if (parts.length < 5) {
                continue;
            }

            String[] tuple = parts[0].split(",");

            String arg1Type = parts[3];
            String arg2Type = parts[4];

            String tail = parts[5];

            if (tuple.length >= 3) {
                String arg2 = tuple[tuple.length - 2];
                String arg1 = tuple[0];

                if (arg1Type.equals("-") || arg1Type.equals("MISC") || arg1Type.equals("O") ||  arg1Type.equals("null")) {
                    arg1Type = getWordNetSense(arg1, dict, semanticTypes);
                }

                if (arg2Type.equals("-") || arg2Type.equals("MISC") || arg1Type.equals("O") ||  arg1Type.equals("null")) {
                    arg2Type = getWordNetSense(arg2, dict, semanticTypes);
                }

                bw.write(String.format("%s\t%s\t%s\t%s\t%s\t%s\n", parts[0], parts[1], parts[2], arg1Type, arg2Type, tail));
            }
        }
        br.close();
        bw.close();
    }

    private static String getWordNetSense(String str, IDictionary dict, Set<String> semanticTypes) {
        int limit = 3;
        try {
            IIndexWord idxWord = dict.getIndexWord(str, POS.NOUN);
            if (idxWord == null) {
                return "-";
            }

            for (IWordID wordID : idxWord.getWordIDs()) {
                IWord word = dict.getWord(wordID);
                ISynset synset = word.getSynset();

                String semanticType = WordNetUtils.getMatchedSemanticTypeBfs(dict, synset, semanticTypes);

                if (!semanticType.equals("-")) {
                    if (!upperFormmating)
                        return "_"+semanticType;
                    else
                        return semanticType.toUpperCase();
                }
                limit --;
                if (limit == 0){
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "-";
    }
}
