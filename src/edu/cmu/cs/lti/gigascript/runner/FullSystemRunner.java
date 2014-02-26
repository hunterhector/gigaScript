package edu.cmu.cs.lti.gigascript.runner;

import de.mpii.clausie.NoParseClausIE;
import de.mpii.clausie.Proposition;
import edu.cmu.cs.lti.gigascript.agiga.AgigaUtil;
import edu.cmu.cs.lti.gigascript.agiga.AgigaWrapper;
import edu.cmu.cs.lti.gigascript.db.GigaDB;
import edu.cmu.cs.lti.gigascript.util.Configuration;
import edu.cmu.cs.lti.gigascript.util.IOUtils;
import edu.jhu.agiga.AgigaDocument;
import edu.jhu.agiga.AgigaPrefs;
import edu.jhu.agiga.AgigaSentence;
import edu.jhu.agiga.StreamingDocumentReader;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by zhengzhongliu on 2/25/14.
 */
public class FullSystemRunner {
    public static void main(String[] args) throws IOException, ClassNotFoundException {

        String propPath = "settings.properties";
        if (args.length < 1) {
            System.err.println("Missing property file argument! Will try default property.");
        } else {
            propPath = args[0];
        }

        Configuration config = new Configuration(new File(propPath));

        //Config logger for errors
        Logger logger = Logger.getLogger(FullSystemRunner.class.getName());
        FileHandler fh = new FileHandler(config.get("edu.cmu.cs.lti.gigaScript.clausie.errorOut"));
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
        //Remove console logging
        logger.setUseParentHandlers(false);

        //Prepare database
        GigaDB gigaDB = new GigaDB(config);
        gigaDB.connectOrCreate();

        //Prepare data source
        String corpusPath = config.get("edu.cmu.cs.lti.gigaScript.agiga.dir");

        File folder = new File(corpusPath);

        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            StreamingDocumentReader reader = new StreamingDocumentReader(listOfFiles[i].getAbsolutePath(), new AgigaPrefs());
            //Prepare IO
            OutputStream out = System.out;
            NoParseClausIE npClauseIe = new NoParseClausIE(out);

            long startTime = System.currentTimeMillis();

            for (AgigaDocument doc : reader) {
                AgigaWrapper wrapper = new AgigaWrapper(doc);

                List<Triple<Pair<Pair<Integer, Integer>, List<String>>, Pair<Pair<Integer, Integer>, List<String>>, String>> allTuples = new ArrayList<Triple<Pair<Pair<Integer, Integer>, List<String>>, Pair<Pair<Integer, Integer>, List<String>>, String>>();

                for (AgigaSentence sent : doc.getSents()) {
//                    IOUtils.printSentence(sent,new PrintStream(System.out));
                    try {
                        npClauseIe.readParse(sent);
                        npClauseIe.detectClauses();
                        npClauseIe.generatePropositions();

                        for (Proposition p : npClauseIe.getPropositions()) {
                            List<List<Integer>> constituentIndices = p.indices();

                            //ignore non-triples
                            if (constituentIndices.size() <= 2){
                                continue;
                            }

                            //assume in triple mode
                            Pair<Pair<Integer, Integer>, List<String>> arg0s = populateArguments(p.subject(), constituentIndices.get(0), wrapper, sent);
                            Pair<Pair<Integer, Integer>, List<String>> arg1s = populateArguments(p.argument(0), constituentIndices.get(2), wrapper, sent);

                            String relation = null;
                            if (constituentIndices.get(1).get(0) == -1) {
                                relation = p.relation();
                            } else {
                                relation = AgigaUtil.getLemmaForPhrase(sent, constituentIndices.get(1));
                            }

                            System.out.println(p);

                            allTuples.add(Triple.of(arg0s, arg1s, relation));
                        }
                    } catch (NullPointerException e) {
                        logger.log(Level.WARNING, String.format("Giving up on Null Pointer.\n%s", AgigaUtil.getSentenceString(sent)));

                    } catch (StackOverflowError e) {
                        logger.log(Level.WARNING, String.format("Giving up on StackoverFlow.\n%s", AgigaUtil.getSentenceString(sent)));
                    }
                }

                //store tuples for this document
                Map<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>, List<Long>> dbReferenceMapping = new HashMap<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>, List<Long>>();

                for (int t1 = 0; t1 < allTuples.size() - 1; t1++) {
                    for (int t2 = t1 + 1; t2 < allTuples.size(); t2++) {
                        Triple<Pair<Pair<Integer, Integer>, List<String>>, Pair<Pair<Integer, Integer>, List<String>>, String> tuple1 = allTuples.get(t1);
                        Triple<Pair<Pair<Integer, Integer>, List<String>>, Pair<Pair<Integer, Integer>, List<String>>, String> tuple2 = allTuples.get(t2);

                        Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> tuple1Keys = Pair.of(tuple1.getLeft().getKey(), tuple1.getMiddle().getKey());
                        Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> tuple2Keys = Pair.of(tuple2.getLeft().getKey(), tuple2.getMiddle().getKey());


                        System.out.println(tuple1);
                        System.out.println(tuple2);
                        if (!dbReferenceMapping.containsKey(tuple1Keys)) {
//                            dbReferenceMapping.put(tuple1Keys, saveTuple(tuple1, gigaDB));
                            dbReferenceMapping.put(tuple1Keys, new ArrayList<Long>());
                        }

                        if (!dbReferenceMapping.containsKey(tuple2Keys)) {
//                            dbReferenceMapping.put(tuple2Keys, saveTuple(tuple2, gigaDB));
                            dbReferenceMapping.put(tuple2Keys,new ArrayList<Long>());
                        }

                        int dist = t2 - t1;
                        boolean[][] equalities = new boolean[2][2];

                        equalities[0][0] = wrapper.sameArgument(tuple1Keys.getLeft(), tuple2Keys.getLeft());
                        equalities[0][1] = wrapper.sameArgument(tuple1Keys.getLeft(), tuple2Keys.getRight());
                        equalities[1][0] = wrapper.sameArgument(tuple1Keys.getRight(), tuple2Keys.getLeft());
                        equalities[1][1] = wrapper.sameArgument(tuple1Keys.getRight(), tuple2Keys.getRight());

                        for (long t1Id : dbReferenceMapping.get(tuple1Keys)) {
                            if (t1Id == -1)
                                continue;
                            for (long t2Id : dbReferenceMapping.get(tuple2Keys)) {
                                if (t2Id == -1){
                                    continue;
                                }
                                gigaDB.addGigaBigram(t1Id, t2Id, dist, equalities);
                            }
                        }
                    }
                }
                System.out.print("\r" + reader.getNumDocs());
            }

            long totalTime = System.currentTimeMillis() - startTime;
            System.out.println("\nOverall processing time takes " + totalTime / 6e4 + " minutes");
        }
    }


    private static List<Long> saveTuple(Triple<Pair<Pair<Integer, Integer>, List<String>>, Pair<Pair<Integer, Integer>, List<String>>, String> tuple, GigaDB db) {
        List<Long> tupleIds = new ArrayList<Long>();
        for (String arg0 : tuple.getLeft().getValue()) {
            for (String arg1 : tuple.getMiddle().getValue()) {
                long tupleId = db.addGigaTuple(arg0, arg1, tuple.getRight());
                tupleIds.add(tupleId);
            }
        }
        return tupleIds;
    }

    private static Pair<Pair<Integer, Integer>, List<String>> populateArguments(String constituent, List<Integer> indices, AgigaWrapper wrapper, AgigaSentence sent) {
        Pair<String, Integer> semanticTypeWithIndex = wrapper.getArgumentSemanticType(sent, indices);

        List<String> arguments = new ArrayList<String>();

        String type = semanticTypeWithIndex.getLeft();
        int keywordIndex = semanticTypeWithIndex.getRight();

        String lemma;
        if (keywordIndex > 0) {
            lemma = AgigaUtil.getLemma(sent, keywordIndex);
        } else {
            lemma = AgigaUtil.getLemma(sent, indices.get(0));
        }
        arguments.add(lemma);
        if (type != null)
            arguments.add(type);

        return Pair.of(Pair.of(sent.getSentIdx(), keywordIndex), arguments);
    }
}
