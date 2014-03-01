package edu.cmu.cs.lti.gigascript.runner;

import de.mpii.clausie.NoParseClausIE;
import de.mpii.clausie.Proposition;
import edu.cmu.cs.lti.gigascript.agiga.AgigaArgument;
import edu.cmu.cs.lti.gigascript.agiga.AgigaDocumentWrapper;
import edu.cmu.cs.lti.gigascript.agiga.AgigaUtil;
import edu.cmu.cs.lti.gigascript.db.GigaDB;
import edu.cmu.cs.lti.gigascript.util.Configuration;
import edu.cmu.cs.lti.gigascript.util.IOUtils;
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
    public static void main(String[] args) throws IOException, ClassNotFoundException, ConfigurationException {
        //Config logger for errors
        Logger logger = Logger.getLogger(FullSystemRunner.class.getName());

        String propPath = "settings.properties";
        if (args.length < 1) {
            System.err.println("Missing property file argument! Will try default property.");
        } else {
            propPath = args[0];
        }

        Configuration config = new Configuration(new File(propPath));

        FileHandler fh = new FileHandler(config.get("edu.cmu.cs.lti.gigaScript.clausie.errorOut"));
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
        //Remove console logging
//        logger.setUseParentHandlers(false);

        //Prepare database
        GigaDB gigaDB = new GigaDB(config);
        gigaDB.connectOrCreate();

        //Prepare data source
        String corpusPath = config.get("edu.cmu.cs.lti.gigaScript.agiga.dir");

        logger.log(Level.INFO,"Reading corpus form directory : "+corpusPath);

        File folder = new File(corpusPath);

        if (!folder.exists()){
             logger.log(Level.SEVERE,"Input directory not found");
             throw new ConfigurationException();
        }

        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            StreamingDocumentReader reader = new StreamingDocumentReader(listOfFiles[i].getAbsolutePath(), new AgigaPrefs());
            //Prepare IO
            OutputStream out = System.out;
            NoParseClausIE npClauseIe = new NoParseClausIE(out);

            long startTime = System.currentTimeMillis();

            for (AgigaDocument doc : reader) {
                AgigaDocumentWrapper wrapper = new AgigaDocumentWrapper(doc);

                List<Triple<AgigaArgument,AgigaArgument, String>> allTuples = new ArrayList<Triple<AgigaArgument,AgigaArgument, String>>();

                for (AgigaSentence sent : doc.getSents()) {
                    try {
                        npClauseIe.readParse(sent);
                        npClauseIe.detectClauses();
                        npClauseIe.generatePropositions();

                        IOUtils.printSentence(sent,new PrintStream(System.out));

                        for (Proposition p : npClauseIe.getPropositions()) {
                            List<List<Integer>> constituentIndices = p.indices();

                            //temporary solution, make the indices zero based
                            for (int u = 0; u< constituentIndices.size();u++){
                                for (int v = 0; v< constituentIndices.get(u).size(); v++){
                                    if (constituentIndices.get(u).get(v) > 0)
                                     constituentIndices.get(u).set(v,constituentIndices.get(u).get(v)-1);
                                }
                            }

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

                            System.out.println(p);
                            System.out.println("Arg0s: "+arg0s+", Arg1s: "+arg1s+", Relation: "+" "+relationPosition+"@"+relation);

                            allTuples.add(Triple.of(arg0s, arg1s, relation));
                        }
                    } catch (NullPointerException e) {
                        logger.log(Level.WARNING, String.format("Giving up on Null Pointer.\n%s", AgigaUtil.getSentenceString(sent)));

                    } catch (StackOverflowError e) {
                        logger.log(Level.WARNING, String.format("Giving up on StackoverFlow.\n%s", AgigaUtil.getSentenceString(sent)));
                    }
                }

                //store the referencing ids that this argument holds in the database, it could be a list of id because each alternative form will take up one
                Map<Pair<AgigaArgument, AgigaArgument>, List<Long>> dbTuple2Mapping = new HashMap<Pair<AgigaArgument, AgigaArgument>, List<Long>>();

                for (int t1 = 0; t1 < allTuples.size() - 1; t1++) {
                    for (int t2 = t1 + 1; t2 < allTuples.size(); t2++) {
                        Triple<AgigaArgument,AgigaArgument, String> tuple1 = allTuples.get(t1);
                        Triple<AgigaArgument,AgigaArgument, String> tuple2 = allTuples.get(t2);

                        Pair<AgigaArgument, AgigaArgument> tuple1Keys = Pair.of(tuple1.getLeft(), tuple1.getMiddle());
                        Pair<AgigaArgument, AgigaArgument> tuple2Keys = Pair.of(tuple2.getLeft(), tuple2.getMiddle());

                        if (!dbTuple2Mapping.containsKey(tuple1Keys)) {
                            dbTuple2Mapping.put(tuple1Keys, saveTuple(tuple1, gigaDB));
//                            dbTuple2Mapping.put(tuple1Keys, new ArrayList<Long>());
                        }

                        if (!dbTuple2Mapping.containsKey(tuple2Keys)) {
                            dbTuple2Mapping.put(tuple2Keys, saveTuple(tuple2, gigaDB));
//                            dbTuple2Mapping.put(tuple2Keys,new ArrayList<Long>());
                        }

                        int tupleDist = t2 - t1;
                        int sentDist = tuple2.getLeft().getSentenceIndex() - tuple1.getLeft().getSentenceIndex();
                        boolean[][] equalities = new boolean[2][2];

                        equalities[0][0] = wrapper.sameArgument(tuple1Keys.getLeft(), tuple2Keys.getLeft());
                        equalities[0][1] = wrapper.sameArgument(tuple1Keys.getLeft(), tuple2Keys.getRight());
                        equalities[1][0] = wrapper.sameArgument(tuple1Keys.getRight(), tuple2Keys.getLeft());
                        equalities[1][1] = wrapper.sameArgument(tuple1Keys.getRight(), tuple2Keys.getRight());

                        for (long t1Id : dbTuple2Mapping.get(tuple1Keys)) {
                            if (t1Id < 0)
                                continue;
                            for (long t2Id : dbTuple2Mapping.get(tuple2Keys)) {
                                if (t2Id < 0){
                                    continue;
                                }
                                gigaDB.addGigaBigram(t1Id, t2Id, tupleDist, equalities);
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


    private static List<Long> saveTuple(Triple<AgigaArgument, AgigaArgument, String> tuple, GigaDB db) {
        List<Long> tupleIds = new ArrayList<Long>();
        for (String arg0 : tuple.getLeft().getAlternativeForms()) {
            for (String arg1 : tuple.getMiddle().getAlternativeForms()) {
                long tupleId = db.addGigaTuple(arg0, arg1, tuple.getRight());
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
