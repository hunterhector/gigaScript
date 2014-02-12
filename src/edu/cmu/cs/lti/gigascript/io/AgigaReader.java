package edu.cmu.cs.lti.gigascript.io;

import edu.jhu.agiga.AgigaDocument;
import edu.jhu.agiga.AgigaPrefs;
import edu.jhu.agiga.AgigaSentence;
import edu.jhu.agiga.StreamingDocumentReader;

/**
 * Created with IntelliJ IDEA.
 * User: Hector, Zhengzhong Liu
 * Date: 2/11/14
 * Time: 2:25 PM
 */
public class AgigaReader {
    public static void main(String[] argv) {
        String path = "/Users/hector/Downloads/nyt_eng_199407.xml.gz";

        long startTime = System.currentTimeMillis();

        AgigaPrefs prefs = new AgigaPrefs();
        prefs.setAll(true);

        System.out.println("Parsing XML...");

        System.out.println("Number of documents processed:");
        StreamingDocumentReader reader = new StreamingDocumentReader(path, prefs);
        for (AgigaDocument doc : reader) {
            for (AgigaSentence sent : doc.getSents()) {
            }
            System.out.print("\r"+reader.getNumDocs());
        }
        System.out.println();

        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println(String.format("Number of files : %d",reader.getNumDocs()));
        System.out.println(String.format("Number of sentences : %d", reader.getNumSents()));
        System.out.println("Overall processing time takes " + totalTime / 6e4 + " minutes");
    }

}
