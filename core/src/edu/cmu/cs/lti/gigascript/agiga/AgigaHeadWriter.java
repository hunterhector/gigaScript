package edu.cmu.cs.lti.gigascript.agiga;

import com.google.common.io.Files;
import edu.cmu.cs.lti.gigascript.util.Configuration;
import edu.jhu.agiga.AgigaDocument;
import edu.jhu.agiga.AgigaPrefs;
import edu.jhu.agiga.AgigaSentence;
import edu.jhu.agiga.StreamingDocumentReader;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 3/25/14
 * Time: 3:59 PM
 */
public class AgigaHeadWriter {
    public static void main(String[] argv) throws IOException, NoSuchAlgorithmException {

        String propPath = "settings.properties";
        if (argv.length < 1) {
            System.err.println("Missing property file argument! Will try default property.");
        } else {
            propPath = argv[0];
        }

        Configuration config = new Configuration(new File(propPath));

        AgigaPrefs prefs = new AgigaPrefs();
        prefs.setAll(true);

        boolean consoleMode = config.get("edu.cmu.cs.lti.gigaScript.console.mode").equals("console");

        String outputPath = config.get("edu.cmu.cs.lti.gigaScript.sentences.out.path");

        File outputDir = new File(outputPath);

        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                System.err.println("Cannot create directory to store :" + outputDir.getCanonicalPath());
            } else {
                System.out.println("Created directory to store");
            }
        }

        String corpusPath = config.get("edu.cmu.cs.lti.gigaScript.agiga.dir");

        File folder = new File(corpusPath);

        File[] listOfFiles = folder.listFiles();

        FileOutputStream sentenceOut = new FileOutputStream(new File(outputPath + "/" + "allHeads.txt"));
        PrintStream out = new PrintStream(sentenceOut);
        Writer writer = new PrintWriter(out, true);

        System.out.println("Parsing XML...");

        long startTime = System.currentTimeMillis();
        long batchStartTime = startTime;

        for (File currentFile : listOfFiles) {
            String fileName = currentFile.getName();
            String extension = Files.getFileExtension(fileName);
            if (!extension.equals("gz")) {
                continue;
            }

            System.out.println("Processing " + fileName);

            StreamingDocumentReader reader = new StreamingDocumentReader(currentFile.getAbsolutePath(), prefs);

            for (AgigaDocument doc : reader) {
                int count = 0;

                String topSents = "";
                for (AgigaSentence sent : doc.getSents()) {
                    topSents+=AgigaUtil.getSentenceString(sent);
                    count++;
                    //take the first few sentences
                    if (count == 5) {
                        break;
                    }
                }

                MessageDigest m = MessageDigest.getInstance("MD5");
                m.reset();
                m.update(topSents.getBytes());
                byte[] digest = m.digest();
                BigInteger bigInt = new BigInteger(1,digest);
                String hashtext = bigInt.toString(16);

                writer.write(hashtext+" "+doc.getDocId());
                writer.write("\n");

                if (consoleMode) {
                    //nice progress view when we can view it in the console
                    System.out.print("\r" + reader.getNumDocs());
                } else {
                    if (reader.getNumDocs() % 500 == 0)
                        //this will be more readable if we would like to direct the console output to file
                        System.out.print(reader.getNumDocs() + " ");
                }
            }

            System.out.println();

            long batchTime = System.currentTimeMillis() - batchStartTime;
            int numDocsInBatch = reader.getNumDocs();
            double batchTimeInMinute = batchTime * 1.0 / 6e4;
            System.out.println(String.format("Process %d document in %.2f minutes in this batch, Average speed: %.2f doc/min.", numDocsInBatch, batchTimeInMinute, numDocsInBatch / batchTimeInMinute));
            batchStartTime = System.currentTimeMillis();
        }
        writer.close();

        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println("\nOverall processing time takes " + totalTime * 1.0 / 6e4 + " minutes");
    }
}