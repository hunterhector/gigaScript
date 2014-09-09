package edu.cmu.cs.lti.gigascript.processor;

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
import edu.cmu.cs.lti.gigascript.model.AgigaRelation;
import edu.cmu.cs.lti.gigascript.util.Configuration;
import edu.cmu.cs.lti.gigascript.util.GeneralUtils;
import edu.jhu.agiga.*;
import gnu.trove.map.hash.TIntLongHashMap;
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
public class IeBasedTupleExtractor {
    public static final String BLACK_LIST_MODE = "blacklist";
    public static final String WHITE_LIST_MODE = "whitelist";

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

                //A linked set could retain the sequence and filter identical triples
                Set<Triple<AgigaArgument, AgigaArgument, AgigaRelation>> regularTuples = new LinkedHashSet<Triple<AgigaArgument, AgigaArgument, AgigaRelation>>();
                Set<Triple<AgigaArgument, AgigaArgument, AgigaRelation>> appossetiveTuples = new LinkedHashSet<Triple<AgigaArgument, AgigaArgument, AgigaRelation>>();

                for (AgigaSentence sent : doc.getSents()) {
                    try {
                        npClauseIe.readParse(sent);
                        npClauseIe.detectClauses();
                        npClauseIe.generatePropositions();

                        AgigaSentenceWrapper sentenceWrapper = new AgigaSentenceWrapper(sent);

//                        System.out.println(sentenceWrapper.getSentenceStr());

                        for (Proposition p : npClauseIe.getPropositions()) {
                            List<List<Integer>> constituentIndices = p.indices();

                            //ignore non-triples
                            if (constituentIndices.size() <= 2) {
                                continue;
                            }

//                            System.out.println(p);

                            //assume in triple model
                            AgigaArgument arg0s = createArgument(constituentIndices.get(0), docWrapper, sentenceWrapper, sent);
                            AgigaArgument arg1s = createArgument(constituentIndices.get(2), docWrapper, sentenceWrapper, sent);

                            //two types of relations are output by Clausie, one type does not have indices
                            AgigaRelation relation;
                            if (constituentIndices.get(1).size() == 0 || constituentIndices.get(1).get(0) < 0) {
                                relation = new AgigaRelation(p.relation());
                                if (relation.getRelationType().equals(AgigaRelation.APPOSITION_TYPE)) {
                                    appossetiveTuples.add(Triple.of(arg0s, arg1s, relation));
                                    gigaStorage.addAppossitiveTuples(arg0s, arg1s, docId);
                                }
                            } else {
                                relation = new AgigaRelation(sent, constituentIndices.get(1));
                                Triple<AgigaArgument, AgigaArgument, AgigaRelation> tuple = Triple.of(arg0s, arg1s, relation);
                                if (GeneralUtils.tupleFilter(tuple,sentenceWrapper)) {
                                    if (!regularTuples.contains(tuple)) {
                                        regularTuples.add(tuple);
                                    }

                                }
                            }
                        }
                    } catch (NullPointerException e) {
                        logger.log(Level.WARNING, String.format("Giving up on Null Pointer.\n%s", AgigaUtil.getSentenceString(sent)));
                        logger.log(Level.WARNING, e.getMessage(), e);
                    } catch (StackOverflowError e) {
                        logger.log(Level.WARNING, String.format("Giving up on StackoverFlow.\n%s", AgigaUtil.getSentenceString(sent)));
                    } catch (ClassCastException e) {
                        logger.log(Level.WARNING, String.format("Giving up on ClassCastException.\n%s", AgigaUtil.getSentenceString(sent)));
                    }
                }

                ArrayList<Triple<AgigaArgument, AgigaArgument, AgigaRelation>> allTupleList = new ArrayList<Triple<AgigaArgument, AgigaArgument, AgigaRelation>>(regularTuples);

                TIntLongHashMap tIdMap = new TIntLongHashMap();

                for (int t1 = 0; t1 < allTupleList.size() - 1; t1++) {
                    for (int t2 = t1 + 1; t2 < allTupleList.size(); t2++) {
                        Triple<AgigaArgument, AgigaArgument, AgigaRelation> tuple1 = allTupleList.get(t1);
                        Triple<AgigaArgument, AgigaArgument, AgigaRelation> tuple2 = allTupleList.get(t2);

                        Pair<AgigaArgument, AgigaArgument> tuple1Keys = Pair.of(tuple1.getLeft(), tuple1.getMiddle());
                        Pair<AgigaArgument, AgigaArgument> tuple2Keys = Pair.of(tuple2.getLeft(), tuple2.getMiddle());

                        long t1Id;
                        if (tIdMap.containsKey(t1)) {
                            t1Id = tIdMap.get(t1);
                        } else {
                            t1Id = saveTuple(tuple1, gigaStorage, docId);
                            tIdMap.put(t1, t1Id);
                        }

                        long t2Id;
                        if (tIdMap.containsKey(t2)) {
                            t2Id = tIdMap.get(t2);
                        } else {
                            t2Id = saveTuple(tuple2, gigaStorage, docId);
                            tIdMap.put(t2, t2Id);
                        }

                        int tupleDist = t2 - t1;
                        int sentDist = tuple2.getLeft().getSentenceIndex() - tuple1.getLeft().getSentenceIndex();
                        int[][] equalities = new int[2][2];

                        equalities[0][0] = docWrapper.sameArgument(tuple1Keys.getLeft(), tuple2Keys.getLeft()) ? 1 : 0;
                        equalities[0][1] = docWrapper.sameArgument(tuple1Keys.getLeft(), tuple2Keys.getRight()) ? 1 : 0;
                        equalities[1][0] = docWrapper.sameArgument(tuple1Keys.getRight(), tuple2Keys.getLeft()) ? 1 : 0;
                        equalities[1][1] = docWrapper.sameArgument(tuple1Keys.getRight(), tuple2Keys.getRight()) ? 1 : 0;

                        gigaStorage.addGigaBigram(t1Id, t2Id, sentDist, tupleDist, equalities);
                    }
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


    private static long saveTuple(Triple<AgigaArgument, AgigaArgument, AgigaRelation> tuple, GigaStorage store, String docId) {
        AgigaArgument leftArg = tuple.getLeft();
        AgigaArgument rightArg = tuple.getMiddle();
        AgigaRelation relation = tuple.getRight();

        //the orignal tuples
        long tupleId = store.addGigaTuple(leftArg, rightArg, relation, docId);

        return tupleId;
    }


    private static AgigaArgument createArgument(List<Integer> indices, AgigaDocumentWrapper docWrapper, AgigaSentenceWrapper sentenceWrapper, AgigaSentence sent) {
        int headwordIndex = sentenceWrapper.getHeadWordIndex(indices);
        List<AgigaToken> tokens = sentenceWrapper.getTokens();
//        int headwordIndex = indices.get(0);

        AgigaToken headWord = sentenceWrapper.getTokens().get(headwordIndex);
        String type = docWrapper.getArgumentSemanticType(sent, headWord, indices);

        AgigaArgument argument = new AgigaArgument(sent.getSentIdx(), headwordIndex);

        String lemma = AgigaUtil.getLemma(tokens.get(headwordIndex));
        lemma = GeneralUtils.getNiceTupleForm(lemma);
        argument.setHeadWordLemma(lemma);
        argument.setEntityType(type);

        return argument;
    }

}