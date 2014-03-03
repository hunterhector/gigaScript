package edu.cmu.cs.lti.gigascript.io;

import edu.cmu.cs.lti.gigascript.agiga.AgigaArgument;
import edu.cmu.cs.lti.gigascript.util.Configuration;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 3/2/14
 * Time: 12:50 AM
 */
public abstract class CacheBasedStorage extends GigaStorage {
    TObjectIntHashMap<Triple<String, String, String>> tupleCount = new TObjectIntHashMap<Triple<String, String, String>>();
    TObjectIntHashMap<Triple<String, String, String>> tuplesId = new TObjectIntHashMap<Triple<String, String, String>>();

    Map<Triple<String, String, String>,Pair<String,String>> tuple2EntityType = new HashMap<Triple<String, String, String>, Pair<String, String>>();

    Map<Pair<Long, Long>, TObjectIntHashMap<Integer>> tupleCooccCount = new HashMap<Pair<Long, Long>, TObjectIntHashMap<Integer>>();
    Map<Pair<Long, Long>, TObjectIntHashMap<Integer>> tupleReverseCooccCount = new HashMap<Pair<Long, Long>, TObjectIntHashMap<Integer>>();

    Map<Pair<Long, Long>, TObjectIntHashMap<String>> tupleEqualityCount = new HashMap<Pair<Long, Long>, TObjectIntHashMap<String>>();
    Map<Pair<Long, Long>, TObjectIntHashMap<String>> tupleReverseEqualityCount = new HashMap<Pair<Long, Long>, TObjectIntHashMap<String>>();


    int outputFileId = 0;
    String outputPrefix;
    String outputTupleStoreName = "Tuples";
    String outputCooccStoreName = "BigramCounts";

    public CacheBasedStorage(Configuration config) {
        outputTupleStoreName = config.get("edu.cmu.cs.lti.gigaScript.tuple.storage.name");
        outputCooccStoreName = config.get("edu.cmu.cs.lti.gigaScript.bigram.storage.name");
    }

    protected long cacheTuple(AgigaArgument arg0, AgigaArgument arg1, String relation) {
        Triple<String, String, String> tuple = Triple.of(arg0.getHeadWordLemma(), arg1.getHeadWordLemma(), relation);

        tuple2EntityType.put(tuple,Pair.of(arg0.getEntityType(),arg1.getEntityType()));

        tupleCount.adjustOrPutValue(tuple, 1, 1);

        tuplesId.putIfAbsent(tuple,tupleCount.size()-1);

        return tuplesId.get(tuple);
    }

    protected void cacheBigram(long t1, long t2, int distance, int[][] equality) {
        Pair<Long, Long> tuplePair = Pair.of(t1, t2);
        Pair<Long, Long> reverseTuplePair = Pair.of(t1, t2);

        //store the natural order
        //1 coocc counts
        if (!tupleCooccCount.containsKey(tuplePair)) {
            TObjectIntHashMap<Integer> directedTupleDistCount = new TObjectIntHashMap<Integer>();
            directedTupleDistCount.put(distance, 1);
            tupleCooccCount.put(tuplePair, directedTupleDistCount);
        } else {
            TObjectIntHashMap<Integer> tupleDistCount = tupleCooccCount.get(tuplePair);
            tupleDistCount.adjustOrPutValue(distance, 1, 1);
        }

        //2 equality counts
        if (!tupleEqualityCount.containsKey(tuplePair)) {
            TObjectIntHashMap<String> directedTupleEqualityCount = new TObjectIntHashMap<String>();
            directedTupleEqualityCount.put("E11", equality[0][0]);
            directedTupleEqualityCount.put("E12", equality[0][1]);
            directedTupleEqualityCount.put("E21", equality[1][0]);
            directedTupleEqualityCount.put("E22", equality[1][1]);
            tupleEqualityCount.put(tuplePair, directedTupleEqualityCount);
        } else {
            TObjectIntHashMap<String> directedTupleEqualityCount = tupleEqualityCount.get(tuplePair);
            directedTupleEqualityCount.adjustOrPutValue("E11", equality[0][0], equality[0][0]);
            directedTupleEqualityCount.adjustOrPutValue("E12", equality[0][1], equality[0][1]);
            directedTupleEqualityCount.adjustOrPutValue("E21", equality[1][0], equality[1][0]);
            directedTupleEqualityCount.adjustOrPutValue("E22", equality[1][1], equality[1][1]);
        }

        //store the reverse order
        //1 coocc counts
        if (!tupleReverseCooccCount.containsKey(reverseTuplePair)) {
            TObjectIntHashMap<Integer> undirectedTupleDistCount = new TObjectIntHashMap<Integer>();
            undirectedTupleDistCount.put(distance, 1);
            tupleReverseCooccCount.put(reverseTuplePair, undirectedTupleDistCount);
        } else {
            TObjectIntHashMap<Integer> tupleDistCount = tupleReverseCooccCount.get(reverseTuplePair);
            tupleDistCount.adjustOrPutValue(distance, 1, 1);
        }

        //2 equality counts
        if (!tupleReverseEqualityCount.containsKey(tuplePair)) {
            TObjectIntHashMap<String> reverseEqualityCount = new TObjectIntHashMap<String>();
            reverseEqualityCount.put("E11", equality[1][1]);
            reverseEqualityCount.put("E12", equality[1][0]);
            reverseEqualityCount.put("E21", equality[0][1]);
            reverseEqualityCount.put("E22", equality[0][0]);
            tupleReverseEqualityCount.put(tuplePair, reverseEqualityCount);
        } else {
            TObjectIntHashMap<String> equalityCount = tupleReverseEqualityCount.get(tuplePair);
            equalityCount.adjustOrPutValue("E11", equality[0][0], equality[1][1]);
            equalityCount.adjustOrPutValue("E12", equality[0][1], equality[1][0]);
            equalityCount.adjustOrPutValue("E21", equality[1][0], equality[0][1]);
            equalityCount.adjustOrPutValue("E22", equality[1][1], equality[0][0]);
        }
    }


    /**
     * Method for clean up and flush to disk
     */
    public void flush() {
        //clear memory
        tupleCount = new TObjectIntHashMap<Triple<String, String, String>>();
        tuplesId = new TObjectIntHashMap<Triple<String, String, String>>();

        tupleCooccCount = new HashMap<Pair<Long, Long>, TObjectIntHashMap<Integer>>();
        tupleReverseCooccCount = new HashMap<Pair<Long, Long>, TObjectIntHashMap<Integer>>();
    }
}
