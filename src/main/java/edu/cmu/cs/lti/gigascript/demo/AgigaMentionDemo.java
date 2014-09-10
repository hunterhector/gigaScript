package edu.cmu.cs.lti.gigascript.demo;

import com.google.common.io.Files;
import edu.cmu.cs.lti.gigascript.agiga.AgigaSentenceWrapper;
import edu.jhu.agiga.AgigaDocument;
import edu.jhu.agiga.AgigaPrefs;
import edu.jhu.agiga.AgigaSentence;
import edu.jhu.agiga.StreamingDocumentReader;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 9/10/14
 * Time: 4:21 PM
 */
public class AgigaMentionDemo {


    public static void main(String[] args) {
        AgigaPrefs prefs = new AgigaPrefs();
        prefs.setAll(true);

        System.out.println("Parsing XML...");

        System.out.println("Number of documents processed:");

        File folder = new File(System.getProperty("user.home") + "/Downloads/agiga_sample");

        File[] listOfFiles = folder.listFiles();

        for (File currentFile : listOfFiles)

        {
            String fileName = currentFile.getName();
            String extension = Files.getFileExtension(fileName);
            if (!extension.equals("gz")) {
                continue;
            }

            System.out.println("Processing " + fileName);

            StreamingDocumentReader reader = new StreamingDocumentReader(currentFile.getAbsolutePath(), prefs);
            for (AgigaDocument doc : reader) {
                for (AgigaSentence sent : doc.getSents()) {
                    AgigaSentenceWrapper wrapper = new AgigaSentenceWrapper(sent);
                }
            }
        }
    }
}
