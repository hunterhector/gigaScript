package edu.cmu.cs.lti.gigascript.io;

import edu.cmu.cs.lti.gigascript.agiga.AgigaSentenceWrapper;
import edu.cmu.cs.lti.gigascript.util.IOUtils;
import edu.jhu.agiga.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Created with IntelliJ IDEA.
 * User: Hector, Zhengzhong Liu
 * Date: 2/11/14
 * Time: 2:25 PM
 */
public class AgigaReader {
    public static void main(String[] argv) throws FileNotFoundException {
        String path = System.getProperty("user.home") + "/Downloads/agiga_sample/nyt_eng_199407.xml.gz";

        long startTime = System.currentTimeMillis();

        AgigaPrefs prefs = new AgigaPrefs();
        prefs.setAll(true);

        System.out.println("Parsing XML...");

        System.out.println("Number of documents processed:");
        StreamingDocumentReader reader = new StreamingDocumentReader(path, prefs);

        FileOutputStream sentenceOut = new FileOutputStream(new File("sentences.txt"));
        PrintStream out = new PrintStream(sentenceOut);

        for (AgigaDocument doc : reader) {
            for (AgigaSentence sent : doc.getSents()) {
                IOUtils.printSentence(sent, out);
                System.out.println(sent);

                AgigaSentenceWrapper wrapper = new AgigaSentenceWrapper(sent);

                for (AgigaToken token : sent.getTokens()){
                    System.out.println(token.getTokIdx()+" "+token.getWord()+ " " +token.getNerTag());
                }
            }
            System.out.print("\r" + reader.getNumDocs());
        }
        System.out.println();

        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println(String.format("Number of files : %d", reader.getNumDocs()));
        System.out.println(String.format("Number of sentences : %d", reader.getNumSents()));
        System.out.println("Overall processing time takes " + totalTime / 6e4 + " minutes");
    }
}