package edu.cmu.cs.lti.gigascript.runner;

import com.google.common.io.Files;
import de.mpii.clausie.NoParseClausIE;
import de.mpii.clausie.Proposition;
import edu.cmu.cs.lti.gigascript.agiga.AgigaDocumentWrapper;
import edu.cmu.cs.lti.gigascript.agiga.AgigaSentenceWrapper;
import edu.cmu.cs.lti.gigascript.agiga.AgigaUtil;
import edu.cmu.cs.lti.gigascript.io.CacheBasedStorage;
import edu.cmu.cs.lti.gigascript.io.CachedFileStorage;
import edu.cmu.cs.lti.gigascript.io.GigaDB;
import edu.cmu.cs.lti.gigascript.io.GigaStorage;
import edu.cmu.cs.lti.gigascript.model.AgigaArgument;
import edu.cmu.cs.lti.gigascript.util.Configuration;
import edu.jhu.agiga.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 2/25/14
 * Time: 6:03 PM
 */
public class FullSystemRunner {
    public static void main(String[] args) throws IOException, ClassNotFoundException, ConfigurationException {
        String propPath = "settings.properties";
        if (args.length < 1) {
            System.err.println("Missing property file argument! Will try default property.");
        } else {
            propPath = args[0];
        }

        Configuration config = new Configuration(new File(propPath));

        Logger rootLogger = Logger.getLogger("edu.cmu.cs.lti");
        FileHandler fh = new FileHandler(config.get("edu.cmu.cs.lti.gigaScript.log"));
        rootLogger.addHandler(fh);
        rootLogger.setUseParentHandlers(false);

        //Config logger for errors
        Logger logger = Logger.getLogger(FullSystemRunner.class.getName());
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);

        //Set mode
        boolean consoleMode = config.get("edu.cmu.cs.lti.gigaScript.console.mode").equals("console");

        //Subset only?
        boolean doFilter = config.getBoolean("edu.cmu.cs.lti.gigaScript.filter");

        Set<String> docIdsToFilter = new HashSet<String>();
        Set<String> filesToFilter = new HashSet<String>();

        if (doFilter) {
            File filterFile = new File(config.get("edu.cmu.cs.lti.gigaScript.filterFile"));
            for (String line : FileUtils.readLines(filterFile, "ascii")) {
                String docId = line.trim();
                docIdsToFilter.add(docId);
                String zipName = docId.split("\\.")[0].toLowerCase().substring(0,"nyt_eng_200001".length()) + ".xml.gz";
                filesToFilter.add(zipName);
            }
            logger.log(Level.INFO, "Will only produce result for the chosen " + docIdsToFilter.size() + " documents.");
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

        String clausIeConfigPath = config.get("edu.cmu.cs.lti.gigaScript.clausie.properties");
        OutputStream out = System.out;
        NoParseClausIE npClauseIe = new NoParseClausIE(out, clausIeConfigPath);

        long startTime = System.currentTimeMillis();
        long batchStartTime = startTime;

        int processed = 0;

        for (File currentFile : listOfFiles) {
            String extension = Files.getFileExtension(currentFile.getName());
            if (!extension.equals("gz")) {
                continue;
            }

            if (!filesToFilter.contains(currentFile.getName())) {
                continue;
            }

            StreamingDocumentReader reader = new StreamingDocumentReader(currentFile.getAbsolutePath(), new AgigaPrefs());
            System.out.println("Processing achrive: " + currentFile.getName());

            String docId = "";
            for (AgigaDocument doc : reader) {
                docId = doc.getDocId();
                if (doFilter) {
                    if (!docIdsToFilter.contains(doc.getDocId())) {
                        continue;
                    }
                }

                gigaStorage.setAdditionalStr(docId);
                processed++;

                AgigaDocumentWrapper docWrapper = new AgigaDocumentWrapper(doc);

                //A linked set could retain the sequence and filter identical triples
                Set<Triple<AgigaArgument, AgigaArgument, String>> allTuples = new LinkedHashSet<Triple<AgigaArgument, AgigaArgument, String>>();

                for (AgigaSentence sent : doc.getSents()) {
                    try {
                        npClauseIe.readParse(sent);
                        npClauseIe.detectClauses();
                        npClauseIe.generatePropositions();

//                        IOUtils.printSentence(sent,new PrintStream(System.out));

                        AgigaSentenceWrapper sentenceWrapper = new AgigaSentenceWrapper(sent);

                        for (Proposition p : npClauseIe.getPropositions()) {
                            List<List<Integer>> constituentIndices = p.indices();

                            //ignore non-triples
                            if (constituentIndices.size() <= 2) {
                                continue;
                            }

//                            System.out.println("-----");
//                            System.out.println(p.toString());
//                            System.out.println(constituentIndices.get(0));
//                            System.out.println(constituentIndices.get(1));
//                            System.out.println(constituentIndices.get(2));

                            //assume in triple model
                            AgigaArgument arg0s = createArgument(constituentIndices.get(0), docWrapper, sentenceWrapper, sent);
                            AgigaArgument arg1s = createArgument(constituentIndices.get(2), docWrapper, sentenceWrapper, sent);

                            String relation;
                            if (constituentIndices.get(1).size() == 0 || constituentIndices.get(1).get(0) < 0) {
                                relation = p.relation();
                            } else {
                                relation = AgigaUtil.getLemmaForPhrase(sent, constituentIndices.get(1));
                            }

                            allTuples.add(Triple.of(arg0s, arg1s, relation));
                        }
                    } catch (NullPointerException e) {
                        logger.log(Level.WARNING, String.format("Giving up on Null Pointer.\n%s", AgigaUtil.getSentenceString(sent)));
                        logger.log(Level.WARNING, e.getMessage(), e);
//                        e.printStackTrace();
                    } catch (StackOverflowError e) {
                        logger.log(Level.WARNING, String.format("Giving up on StackoverFlow.\n%s", AgigaUtil.getSentenceString(sent)));
                    }
                }

                //store the referencing ids that this argument holds in the database, it could be a list of id because each alternative form will take up one
                Map<Pair<AgigaArgument, AgigaArgument>, List<Long>> tuple2StorageIdMapping = new HashMap<Pair<AgigaArgument, AgigaArgument>, List<Long>>();

                ArrayList<Triple<AgigaArgument, AgigaArgument, String>> allTupleList = new ArrayList<Triple<AgigaArgument, AgigaArgument, String>>(allTuples);

                for (int t1 = 0; t1 < allTupleList.size() - 1; t1++) {
                    for (int t2 = t1 + 1; t2 < allTupleList.size(); t2++) {
                        Triple<AgigaArgument, AgigaArgument, String> tuple1 = allTupleList.get(t1);
                        Triple<AgigaArgument, AgigaArgument, String> tuple2 = allTupleList.get(t2);

                        Pair<AgigaArgument, AgigaArgument> tuple1Keys = Pair.of(tuple1.getLeft(), tuple1.getMiddle());
                        Pair<AgigaArgument, AgigaArgument> tuple2Keys = Pair.of(tuple2.getLeft(), tuple2.getMiddle());

                        if (!tuple2StorageIdMapping.containsKey(tuple1Keys)) {
                            tuple2StorageIdMapping.put(tuple1Keys, saveTuple(tuple1, gigaStorage));
                        }

                        if (!tuple2StorageIdMapping.containsKey(tuple2Keys)) {
                            tuple2StorageIdMapping.put(tuple2Keys, saveTuple(tuple2, gigaStorage));
                        }

                        int tupleDist = t2 - t1;
                        int sentDist = tuple2.getLeft().getSentenceIndex() - tuple1.getLeft().getSentenceIndex();
                        int[][] equalities = new int[2][2];

                        equalities[0][0] = docWrapper.sameArgument(tuple1Keys.getLeft(), tuple2Keys.getLeft()) ? 1 : 0;
                        equalities[0][1] = docWrapper.sameArgument(tuple1Keys.getLeft(), tuple2Keys.getRight()) ? 1 : 0;
                        equalities[1][0] = docWrapper.sameArgument(tuple1Keys.getRight(), tuple2Keys.getLeft()) ? 1 : 0;
                        equalities[1][1] = docWrapper.sameArgument(tuple1Keys.getRight(), tuple2Keys.getRight()) ? 1 : 0;

                        for (long t1Id : tuple2StorageIdMapping.get(tuple1Keys)) {
                            if (t1Id < 0)
                                continue;
                            for (long t2Id : tuple2StorageIdMapping.get(tuple2Keys)) {
                                if (t2Id < 0) {
                                    continue;
                                }
                                gigaStorage.addGigaBigram(t1Id, t2Id, sentDist, tupleDist, equalities);
                            }
                        }
                    }
                }
                if (processed % docNum2Flush == 0) {
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


    private static List<Long> saveTuple(Triple<AgigaArgument, AgigaArgument, String> tuple, GigaStorage store) {
        List<Long> tupleIds = new ArrayList<Long>();

        AgigaArgument leftArg = tuple.getLeft();
        AgigaArgument rightArg = tuple.getMiddle();
        String relation = tuple.getRight();

//        System.out.println(leftArg.getHeadWordLemma()+" "+rightArg.getHeadWordLemma()+" "+relation);

        //the orignal tuples
        long tupleId = store.addGigaTuple(leftArg, rightArg, relation);

        tupleIds.add(tupleId);

        return tupleIds;
    }

    private static AgigaArgument createArgument(List<Integer> indices, AgigaDocumentWrapper docWrapper, AgigaSentenceWrapper sentenceWrapper, AgigaSentence sent) {
        int headwordIndex = sentenceWrapper.getHeadWordIndex(indices);
        AgigaToken headWord = sent.getTokens().get(headwordIndex);
        String type = docWrapper.getArgumentSemanticType(sent, headWord, indices);

        AgigaArgument argument = new AgigaArgument(sent.getSentIdx(), indices.get(0));

        String lemma = AgigaUtil.getLemma(sent, headwordIndex);

        argument.setHeadWordLemma(lemma);
        argument.setEntityType(type);

        return argument;
    }
}