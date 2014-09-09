package edu.cmu.cs.lti.gigascript.processor;

import com.google.common.io.Files;
import edu.cmu.cs.lti.gigascript.agiga.AgigaDocumentWrapper;
import edu.cmu.cs.lti.gigascript.agiga.AgigaSentenceWrapper;
import edu.cmu.cs.lti.gigascript.io.CacheBasedStorage;
import edu.cmu.cs.lti.gigascript.io.CachedFileStorage;
import edu.cmu.cs.lti.gigascript.io.GigaDB;
import edu.cmu.cs.lti.gigascript.util.Configuration;
import edu.jhu.agiga.AgigaDocument;
import edu.jhu.agiga.AgigaPrefs;
import edu.jhu.agiga.AgigaSentence;
import edu.jhu.agiga.StreamingDocumentReader;
import org.apache.commons.io.FileUtils;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 9/4/14
 * Time: 2:37 PM
 */
public abstract class AbstractSentenceBasedGigwordProcessor {
    public static final String BLACK_LIST_MODE = "blacklist";
    public static final String WHITE_LIST_MODE = "whitelist";

    public void process(String[] args) throws IOException, ConfigurationException {
        String propPath = "settings.properties";
        if (args.length < 1) {
            System.err.println("Missing property file argument! Will try default property.");
        } else {
            propPath = args[0];
        }

        Configuration config = new Configuration(new File(propPath));
        Logger rootLogger = Logger.getLogger("edu.cmu.cs.lti");
        FileHandler fh = new FileHandler(config.get("edu.cmu.cs.lti.gigaScript.log"));
        fh.setLevel(Level.FINE);
        rootLogger.addHandler(fh);
        rootLogger.setUseParentHandlers(false);

        //Config logger for errors
        Logger logger = Logger.getLogger(IeBasedTupleExtractor.class.getName());
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);

        //Set mode
        boolean consoleMode = config.get("edu.cmu.cs.lti.gigaScript.console.mode").equals("console");

        //Subset only?
        String filterMode = config.get("edu.cmu.cs.lti.gigaScript.filterMode");

        Set<String> docIdsToFilter = new HashSet<String>();
        Set<String> filesToFilter = new HashSet<String>();

        if (filterMode.equals(BLACK_LIST_MODE) || filterMode.equals(WHITE_LIST_MODE)) {
            File filterFile = new File(config.get("edu.cmu.cs.lti.gigaScript.filterFile"));
            for (String line : FileUtils.readLines(filterFile, "ascii")) {
                String docId = line.trim();
                docIdsToFilter.add(docId);
                String zipName = docId.split("\\.")[0].toLowerCase().substring(0, "nyt_eng_200001".length()) + ".xml.gz";
                filesToFilter.add(zipName);
            }
            logger.log(Level.INFO, "We use the chosen " + docIdsToFilter.size() + " documents as " + filterMode);
        }


        //Prepare storage
        String storageMethod = config.get("edu.cmu.cs.lti.gigaScript.tupleStorage");
        CacheBasedStorage gigaStorage;
        if (storageMethod.equals("db")) {
            gigaStorage = new GigaDB(config);
        } else {
            gigaStorage = new CachedFileStorage(config);
        }

        int docNum2Flush = config.getInt("edu.cmu.cs.lti.gigaScript.flush.size");

        //Prepare data source
        String corpusPath = config.get("edu.cmu.cs.lti.gigaScript.agiga.dir");
        //trim the header to reduce storage size
        String agigaHeader = config.get("edu.cmu.cs.lti.gigaScript.agiga.trimHeader");

        logger.log(Level.INFO, "Reading corpus form directory : " + corpusPath);

        File folder = new File(corpusPath);

        if (!folder.exists()) {
            logger.log(Level.SEVERE, "Input directory not found: " + corpusPath);
            throw new ConfigurationException("Cannot find input directory");
        }

        File[] listOfFiles = folder.listFiles();

        if (listOfFiles == null) {
            logger.log(Level.SEVERE, "Cannot list the give directory: " + folder.getCanonicalPath());
            System.exit(1);
        }


        long startTime = System.currentTimeMillis();
        long batchStartTime = startTime;

        int processed = 0;

        for (File currentFile : listOfFiles) {
            String extension = Files.getFileExtension(currentFile.getName());
            if (!extension.equals("gz")) {
                continue;
            }

            if (filterMode.equals(WHITE_LIST_MODE)) {
                if (!filesToFilter.contains(currentFile.getName())) {
                    continue;
                }
            }

            StreamingDocumentReader reader = new StreamingDocumentReader(currentFile.getAbsolutePath(), new AgigaPrefs());

            String docId;
            for (AgigaDocument doc : reader) {
                System.out.println("Start Processing Archive" + doc.getDocId());
                docId = doc.getDocId();

                if (filterMode.equals(WHITE_LIST_MODE)) {
                    if (!docIdsToFilter.contains(docId)) {
                        continue;
                    }
                    logger.log(Level.FINE, "Processing whitelisted document: " + docId);
                } else if (filterMode.equals(BLACK_LIST_MODE)) {
                    if (docIdsToFilter.contains(docId)) {
                        logger.log(Level.FINE, "Ignoring blacklisted document: " + docId);
                        continue;
                    }
                }

                docId = docId.replaceFirst(agigaHeader, "");
                processed++;

                AgigaDocumentWrapper docWrapper = new AgigaDocumentWrapper(doc);

                for (AgigaSentence sent : doc.getSents()) {
                    processSentence(new AgigaSentenceWrapper(sent),docWrapper);
                }

                if (processed % docNum2Flush == 0) {
                    logger.log(Level.FINEST, "Flush for " + docNum2Flush + " documents");
                    gigaStorage.flush();
                }
                if (consoleMode) {
                    //nice progress view when we can view it in the console
                    System.out.print("\r" + processed);
                } else {
                    if (processed % 500 == 0) {
                        //this will be more readable if we would like to direct the console output to file
                        System.out.print(processed + " ");
                    }
                }
            }

            logger.log(Level.FINEST, "Flush for document end");
            gigaStorage.flush();

            System.out.println();

            long batchTime = System.currentTimeMillis() - batchStartTime;
            int numDocsInBatch = reader.getNumDocs();
            double batchTimeInMinute = batchTime * 1.0 / 6e4;
            System.out.println(String.format("Process %d document in %.2f minutes in this batch, Average speed: %.2f doc/min.", numDocsInBatch, batchTimeInMinute, numDocsInBatch / batchTimeInMinute));

            batchStartTime = System.currentTimeMillis();
            //flush release memory
//            gigaStorage.flush();
        }

        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println("\nOverall processing time takes " + totalTime * 1.0 / 6e4 + " minutes");
    }


    protected abstract void processSentence(AgigaSentenceWrapper sent, AgigaDocumentWrapper doc);

}