package edu.cmu.cs.lti.gigascript.io;

import edu.cmu.cs.lti.gigascript.util.Configuration;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Hector, Zhengzhong Liu
 * Date: 3/1/14
 * Time: 4:05 PM
 */
public class CachedFileStorage extends CacheBasedStorage {
    public static Logger logger = Logger.getLogger(GigaDB.class.getName());

    TObjectIntHashMap<Triple<String, String, String>> tupleCount = new TObjectIntHashMap<Triple<String, String, String>>();
    Map<Triple<String, String, String>, Integer> tuplesId = new HashMap<Triple<String, String, String>, Integer>();


    Map<Pair<Long, Long>, TObjectIntHashMap<Integer>> tupleCooccCount = new HashMap<Pair<Long, Long>, TObjectIntHashMap<Integer>>();
    Map<Pair<Long, Long>, TObjectIntHashMap<Integer>> tupleReverseCooccCount = new HashMap<Pair<Long, Long>, TObjectIntHashMap<Integer>>();

    int outputSuffix = 0;
    String outputPrefix;
    String outputTupleKeyword = "tuple";
    String outputCooccKeyword = "coocc";
    String outputReverseCooccKeyword = "coocc_r";

    public CachedFileStorage(Configuration config) {
        outputPrefix = config.get("edu.cmu.cs.lti.gigaScript.file.prefix");
    }

    @Override
    public long addGigaTuple(String arg0, String arg1, String relation) {
        Triple<String, String, String> tuple = Triple.of(arg0, arg1, relation);
        int id = tupleCount.size();
        tuplesId.put(tuple, id);
        tupleCount.adjustOrPutValue(tuple, 1, 1);
        return id;
    }

    @Override
    public void addGigaBigram(long t1, long t2, int distance, boolean[][] equality) {
        Pair<Long, Long> tuplePair = Pair.of(t1, t2);
        Pair<Long, Long> reverseTuplePair = Pair.of(t1, t2);

        //store the natural order
        if (tupleCooccCount.containsKey(tuplePair)) {
            TObjectIntHashMap<Integer> directedTupleDistCount = new TObjectIntHashMap<Integer>();
            directedTupleDistCount.put(distance, 1);
            tupleCooccCount.put(tuplePair, directedTupleDistCount);
        } else {
            TObjectIntHashMap<Integer> tupleDistCount = tupleCooccCount.get(tuplePair);
            tupleDistCount.adjustOrPutValue(distance, 1, 1);
        }

        //store the reverse order
        if (tupleReverseCooccCount.containsKey(reverseTuplePair)) {
            TObjectIntHashMap<Integer> undirectedTupleDistCount = new TObjectIntHashMap<Integer>();
            undirectedTupleDistCount.put(distance, 1);
            tupleReverseCooccCount.put(reverseTuplePair, undirectedTupleDistCount);
        } else {
            TObjectIntHashMap<Integer> tupleDistCount = tupleReverseCooccCount.get(reverseTuplePair);
            tupleDistCount.adjustOrPutValue(distance, 1, 1);
        }
    }

    private void writeTuple(Writer writer) throws IOException {
        for (Triple<String, String, String> tuple : tupleCount.keySet()) {
            writer.write(tuple.toString());
            writer.write(" ");
            writer.write(tupleCount.get(tuple));
            writer.write(" ");
            writer.write(tuplesId.get(tuple));
        }
    }

    private void writeBigram(Writer writer) throws IOException{
        for (Pair<Long, Long> tupleIdx : tupleCooccCount.keySet()){
            writer.write(tupleIdx.toString());
            writer.write(" ");
            TObjectIntHashMap<Integer> counts = tupleCooccCount.get(tupleIdx);
            for (int dist :  counts.keySet()){
                writer.write(dist+":"+counts.get(dist));
                writer.write(" ");
            }
        }
    }

    @Override
    public void flush() {
        Writer tupleWriter = null;
        Writer cooccWriter = null;
        Writer reverseCooccWriter = null;

        try {
            tupleWriter = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outputPrefix + outputTupleKeyword + outputSuffix)));

            cooccWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPrefix + outputCooccKeyword + outputSuffix)));

            reverseCooccWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPrefix + outputReverseCooccKeyword + outputSuffix)));

            writeTuple(tupleWriter);
            writeBigram(cooccWriter);
            writeBigram(reverseCooccWriter);
        } catch (IOException ex) {
            System.err.println("Write file failure, I recommend you check it!");
            logger.log(Level.SEVERE,ex.getMessage(),ex);
        }

        //clear memory
        tupleCount = new TObjectIntHashMap<Triple<String, String, String>>();
        tuplesId = new HashMap<Triple<String, String, String>, Integer>();

        tupleCooccCount = new HashMap<Pair<Long, Long>, TObjectIntHashMap<Integer>>();
        tupleReverseCooccCount = new HashMap<Pair<Long, Long>, TObjectIntHashMap<Integer>>();

        outputSuffix++;
    }
}
