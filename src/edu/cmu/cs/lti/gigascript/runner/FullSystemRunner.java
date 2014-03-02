package edu.cmu.cs.lti.gigascript.runner;

import de.mpii.clausie.NoParseClausIE;
import de.mpii.clausie.Proposition;
import edu.cmu.cs.lti.gigascript.agiga.AgigaArgument;
import edu.cmu.cs.lti.gigascript.agiga.AgigaDocumentWrapper;
import edu.cmu.cs.lti.gigascript.agiga.AgigaUtil;
import edu.cmu.cs.lti.gigascript.io.CacheBasedStorage;
import edu.cmu.cs.lti.gigascript.io.CachedFileStorage;
import edu.cmu.cs.lti.gigascript.io.GigaDB;
import edu.cmu.cs.lti.gigascript.io.GigaStorage;
import edu.cmu.cs.lti.gigascript.util.Configuration;
import edu.jhu.agiga.AgigaDocument;
import edu.jhu.agiga.AgigaPrefs;
import edu.jhu.agiga.AgigaSentence;
import edu.jhu.agiga.StreamingDocumentReader;
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
 * Created by zhengzhongliu on 2/25/14.
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
        FileHandler fh = new FileHandler(config.get("edu.cmu.cs.lti.gigaScript.clausie.errorOut"));
        rootLogger.addHandler(fh);
        rootLogger.setUseParentHandlers(false);

        //Config logger for errors
        Logger logger = Logger.getLogger(FullSystemRunner.class.getName());
//        logger.setUseParentHandlers(false);
//        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);

        //Prepare storage
        String storageMethod = config.get("edu.cmu.cs.lti.gigaScript.tupleStorage");
        CacheBasedStorage gigaStorage;
        if (storageMethod.equals("db")){
            gigaStorage = new GigaDB(config);
        }else{
            gigaStorage = new CachedFileStorage(config);
        }

        //Prepare data source
        String corpusPath = config.get("edu.cmu.cs.lti.gigaScript.agiga.dir");

        logger.log(Level.INFO,"Reading corpus form directory : "+corpusPath);

        File folder = new File(corpusPath);

        if (!folder.exists()){
             logger.log(Level.SEVERE,"Input directory not found: "+corpusPath);
             throw new ConfigurationException();
        }

        File[] listOfFiles = folder.listFiles();

        if (listOfFiles == null){
            logger.log(Level.SEVERE,"Cannot list the give directory: "+folder.getCanonicalPath());
            System.exit(1);
        }

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < listOfFiles.length; i++) {
            File currentFile = listOfFiles[i];

            StreamingDocumentReader reader = new StreamingDocumentReader(currentFile.getAbsolutePath(), new AgigaPrefs());
            //Prepare IO
            OutputStream out = System.out;
            NoParseClausIE npClauseIe = new NoParseClausIE(out);

            System.out.println("Processing achrive: "+ currentFile.getName());

            for (AgigaDocument doc : reader) {
                AgigaDocumentWrapper wrapper = new AgigaDocumentWrapper(doc);

                //A linked set could retain the sequence and filter identical triples
                Set<Triple<AgigaArgument,AgigaArgument, String>> allTuples = new LinkedHashSet<Triple<AgigaArgument,AgigaArgument, String>>();

                for (AgigaSentence sent : doc.getSents()) {
                    try {
                        npClauseIe.readParse(sent);
                        npClauseIe.detectClauses();
                        npClauseIe.generatePropositions();

//                        IOUtils.printSentence(sent,new PrintStream(System.out));

                        for (Proposition p : npClauseIe.getPropositions()) {
                            List<List<Integer>> constituentIndices = p.indices();

                            //ignore non-triples
                            if (constituentIndices.size() <= 2){
                                continue;
                            }

                            //assume in triple mode
                            AgigaArgument arg0s = populateArguments(constituentIndices.get(0), wrapper, sent);
                            AgigaArgument arg1s = populateArguments(constituentIndices.get(2), wrapper, sent);

                            String relation = null;
                            String relationPosition = "";
                            if (constituentIndices.get(1).get(0) < 0) {
                                relation = p.relation();
                            } else {
                                relation = AgigaUtil.getLemmaForPhrase(sent, constituentIndices.get(1));
                            }
                            relationPosition=""+constituentIndices.get(1).get(0);

//                            System.out.println(p);
//                            System.out.println("Arg0s: "+arg0s+", Arg1s: "+arg1s+", Relation: "+" "+relationPosition+"@"+relation);

                            allTuples.add(Triple.of(arg0s, arg1s, relation));
                        }
                    } catch (NullPointerException e) {
                        logger.log(Level.WARNING, String.format("Giving up on Null Pointer.\n%s", AgigaUtil.getSentenceString(sent)));

                    } catch (StackOverflowError e) {
                        logger.log(Level.WARNING, String.format("Giving up on StackoverFlow.\n%s", AgigaUtil.getSentenceString(sent)));
                    }
                }

                //store the referencing ids that this argument holds in the database, it could be a list of id because each alternative form will take up one
                Map<Pair<AgigaArgument, AgigaArgument>, List<Long>> tuple2StorageIdMapping = new HashMap<Pair<AgigaArgument, AgigaArgument>, List<Long>>();


                ArrayList<Triple<AgigaArgument,AgigaArgument,String>> allTupleList = new ArrayList<Triple<AgigaArgument, AgigaArgument, String>>(allTuples);

                for (int t1 = 0; t1 < allTupleList.size() - 1; t1++) {
                    for (int t2 = t1 + 1; t2 < allTupleList.size(); t2++) {
                        Triple<AgigaArgument,AgigaArgument, String> tuple1 = allTupleList.get(t1);
                        Triple<AgigaArgument,AgigaArgument, String> tuple2 = allTupleList.get(t2);

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
                        boolean[][] equalities = new boolean[2][2];

                        equalities[0][0] = wrapper.sameArgument(tuple1Keys.getLeft(), tuple2Keys.getLeft());
                        equalities[0][1] = wrapper.sameArgument(tuple1Keys.getLeft(), tuple2Keys.getRight());
                        equalities[1][0] = wrapper.sameArgument(tuple1Keys.getRight(), tuple2Keys.getLeft());
                        equalities[1][1] = wrapper.sameArgument(tuple1Keys.getRight(), tuple2Keys.getRight());

//                        for (long t1Id : tuple2StorageIdMapping.get(tuple1Keys)) {
//                            if (t1Id < 0)
//                                continue;
//                            for (long t2Id : tuple2StorageIdMapping.get(tuple2Keys)) {
//                                if (t2Id < 0){
//                                    continue;
//                                }
//                                gigaStorage.addGigaBigram(t1Id, t2Id, tupleDist, equalities);
//                            }
//                        }
                    }
                }
                System.out.print("\r" + reader.getNumDocs());
            }

            //flush release memory
            gigaStorage.flush();
        }

        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println("\nOverall processing time takes " + totalTime / 6e4 + " minutes");
    }


    private static List<Long> saveTuple(Triple<AgigaArgument, AgigaArgument, String> tuple, GigaStorage store) {
        List<Long> tupleIds = new ArrayList<Long>();
        for (String arg0 : tuple.getLeft().getAlternativeForms()) {
            for (String arg1 : tuple.getMiddle().getAlternativeForms()) {
                long tupleId = store.addGigaTuple(arg0, arg1, tuple.getRight());
                tupleIds.add(tupleId);
            }
        }
        return tupleIds;
    }

    private static AgigaArgument populateArguments( List<Integer> indices, AgigaDocumentWrapper wrapper, AgigaSentence sent) {
        Pair<String, Integer> semanticTypeWithIndex = wrapper.getArgumentSemanticType(sent, indices);

        String type = semanticTypeWithIndex.getLeft();
        int keywordIndex = semanticTypeWithIndex.getRight() > 0? semanticTypeWithIndex.getRight() : indices.get(0);

        AgigaArgument argument = new AgigaArgument(sent.getSentIdx(),keywordIndex);

        String lemma;
        if (keywordIndex > 0) {
            lemma = AgigaUtil.getLemma(sent, keywordIndex);
        } else {
            lemma = AgigaUtil.getLemma(sent, indices.get(0));
        }

        argument.addAlternativeForms(lemma);

        //when there is a nontrivial type
        if (type != null && !type.equals("O"))
            argument.addAlternativeForms(type);

        return argument;
    }
}