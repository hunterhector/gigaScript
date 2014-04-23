package edu.cmu.cs.lti.gigascript.io;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import edu.cmu.cs.lti.gigascript.model.AgigaArgument;
import edu.cmu.cs.lti.gigascript.model.BigramInfo;
import edu.cmu.cs.lti.gigascript.util.Configuration;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 3/2/14
 * Time: 12:50 AM
 */
public abstract class CacheBasedStorage extends GigaStorage {
    //All these collections need to be clean periodically!

    TObjectIntHashMap<Triple<String, String, String>> tupleIds = new TObjectIntHashMap<Triple<String, String, String>>();
    List<Pair<String, String>> tupleEntityTypes = new ArrayList<Pair<String, String>>();
    TIntArrayList tupleCount = new TIntArrayList();
    List<List<String>> tupleSource = new ArrayList<List<String>>();
    Table<Long, Long, BigramInfo> bigramInfoTable = HashBasedTable.create();

    String additionalStr = "";

    boolean hasNoTuples = true;

    int outputFileId = 0;
    String outputTupleStoreName = "Tuples";
    String outputCooccStoreName = "BigramCounts";
    boolean useLowerCase = true;

    public CacheBasedStorage(Configuration config) {
        outputTupleStoreName = config.get("edu.cmu.cs.lti.gigaScript.tuple.storage.name");
        outputCooccStoreName = config.get("edu.cmu.cs.lti.gigaScript.bigram.storage.name");
        useLowerCase = config.getBoolean("edu.cmu.cs.lti.gigaScript.lowercase");
        outputFileId = Integer.parseInt(config.getOrElse("edu.cmu.cs.lti.gigaScript.baseFileId","0"));
    }

    protected long cacheTuple(AgigaArgument arg0, AgigaArgument arg1, String relation,String docId) {
        Triple<String, String, String> tuple;
        if (useLowerCase) {
            tuple = Triple.of(arg0.getHeadWordLemma().toLowerCase(), arg1.getHeadWordLemma().toLowerCase(), relation.toLowerCase());
        }else{
            tuple = Triple.of(arg0.getHeadWordLemma(), arg1.getHeadWordLemma(), relation);
        }
        int tupleId;

        boolean newTuple = true;

        if (hasNoTuples) {
            tupleId = 0;
            tupleIds.put(tuple, tupleId);
            hasNoTuples = false;
        } else {
            int newId = tupleCount.size();
            int oldId = tupleIds.putIfAbsent(tuple, newId);
            if (oldId != 0) {
                tupleId = oldId;
                newTuple = false;
            } else {
                tupleId = newId;
            }
        }

        if (newTuple) {
            tupleEntityTypes.add(Pair.of(arg0.getEntityType(), arg1.getEntityType()));
            List<String> newSource = new ArrayList<String>();
            newSource.add(docId);
            tupleSource.add(newSource);
            tupleCount.add(1);
        } else {
            tupleCount.set(tupleId, tupleCount.get(tupleId) + 1);
            tupleSource.get(tupleId).add(docId);
        }

        return tupleId;
    }

    protected void cacheBigram(long t1, long t2, int sentDistance, int tupleDistance, int[][] equality) {
        //store the natural order only
        if (bigramInfoTable.contains(t1, t2)) {
            bigramInfoTable.get(t1, t2).observe(sentDistance, tupleDistance, equality);
        } else {
            BigramInfo bigramInfo = new BigramInfo(sentDistance, tupleDistance, equality);
            bigramInfoTable.put(t1, t2, bigramInfo);
        }
    }

    public void setAdditionalStr(String additionalStr){
        this.additionalStr = additionalStr;
    }

    /**
     * Method for clean up and flush to disk
     */
    public void flush() {
        //clear memory
        tupleCount.reset();
        tupleIds.clear();
        tupleEntityTypes.clear();
        bigramInfoTable.clear();
        tupleSource.clear();
        hasNoTuples = true;
    }
}
