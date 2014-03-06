package edu.cmu.cs.lti.gigascript.io;

import com.google.common.io.Files;
import de.mpii.clausie.NoParseClausIE;
import edu.jhu.agiga.AgigaDocument;
import edu.jhu.agiga.AgigaPrefs;
import edu.jhu.agiga.AgigaSentence;
import edu.jhu.agiga.StreamingDocumentReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Created with IntelliJ IDEA.
 * User: Hector, Zhengzhong Liu
 * Date: 2/11/14
 * Time: 2:25 PM
 */
public class AgigaReader {
    public static void main(String[] argv) throws IOException {
        long startTime = System.currentTimeMillis();
        long batchStartTime = startTime;

        AgigaPrefs prefs = new AgigaPrefs();
        prefs.setAll(true);

        System.out.println("Parsing XML...");

        System.out.println("Number of documents processed:");

        FileOutputStream sentenceOut = new FileOutputStream(new File("sentences.txt"));
        PrintStream out = new PrintStream(sentenceOut);

        File folder = new File(System.getProperty("user.home") + "/Downloads/agiga_sample_1");

        File[] listOfFiles = folder.listFiles();

        NoParseClausIE npClauseIe = new NoParseClausIE(out, "clausie.properties");

        for (File currentFile : listOfFiles) {
            String fileName = currentFile.getName();
            String extension = Files.getFileExtension(fileName);
            if (!extension.equals("gz")) {
                continue;
            }

            System.out.println("Processing " + fileName);

            StreamingDocumentReader reader = new StreamingDocumentReader(currentFile.getAbsolutePath(), prefs);
            for (AgigaDocument doc : reader) {
//                for (AgigaSentence sent : doc.getSents()) {
//                    IOUtils.printSentence(sent, out);
//                    try {
//
//                        AgigaSentenceWrapper wrapper = new AgigaSentenceWrapper(sent);
//
//                    npClauseIe.readParse(sent);
//                    npClauseIe.detectClauses();
//                    npClauseIe.generatePropositions();
//
//                    AgigaSentenceWrapper sentenceWrapper = new AgigaSentenceWrapper(sent);
//
//                    for (Proposition p : npClauseIe.getPropositions()) {
//                        List<List<Integer>> constituentIndices = p.indices();
//                    }
//                    }catch (NullPointerException e) {
//                         String.format("Giving up on Null Pointer.\n%s", AgigaUtil.getSentenceString(sent));
////                        e.printStackTrace();
//                    } catch (StackOverflowError e) {
//                        String.format("Giving up on StackoverFlow.\n%s", AgigaUtil.getSentenceString(sent));
//                    }
//                }
                if (doc.getSents().size() > 100) {
                    System.out.println(reader.getNumDocs() + " " + doc.getSents().size());
                }
            }
            System.out.println();

            long batchTime = System.currentTimeMillis() - batchStartTime;
            int numDocsInBatch = reader.getNumDocs();
            double batchTimeInMinute = batchTime * 1.0 / 6e4;
            System.out.println(String.format("Process %d document in %.2f minutes in this batch, Average speed: %.2f doc/min.", numDocsInBatch, batchTimeInMinute, numDocsInBatch / batchTimeInMinute));
            batchStartTime = System.currentTimeMillis();
        }
        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println("Overall processing time takes " + totalTime / 6e4 + " minutes");
    }
}