package edu.cmu.cs.lti.gigascript.io;

import edu.cmu.cs.lti.gigascript.agiga.AgigaArgument;
import edu.cmu.cs.lti.gigascript.util.Configuration;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.io.*;
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

    public CachedFileStorage(Configuration config) {
        super(config);
        outputPrefix = config.get("edu.cmu.cs.lti.gigaScript.file.prefix");
    }

    @Override
    public long addGigaTuple(AgigaArgument arg0, AgigaArgument arg1, String relation) {
        return cacheTuple(arg0,arg1,relation);
    }

    @Override
    public void addGigaBigram(long t1, long t2, int distance, int[][] equality) {
       cacheBigram(t1,t2,distance,equality);
    }

    private void writeTuple(Writer writer, int id) throws IOException {
        for (Triple<String, String, String> tuple : tupleCount.keySet()) {
            writer.write(tuple.toString());
            writer.write("\t" + tupleCount.get(tuple));
            writer.write("\t"+ tuple2EntityType.get(tuple));
            writer.write("\t" + id+"_"+tuplesId.get(tuple));
            writer.write("\n");
        }
    }

    private void writeBigram(Writer writer, int id) throws IOException{
        for (Pair<Long, Long> tupleIdx : tupleCooccCount.keySet()){
            writer.write(id+"_"+tupleIdx.toString());
            writer.write("\t");
            TObjectIntHashMap<Integer> counts = tupleCooccCount.get(tupleIdx);
            for (int dist :  counts.keySet()){
                writer.write(dist+":"+counts.get(dist));
                writer.write(",");
            }
              
            TObjectIntHashMap<String> equalities = tupleEqualityCount.get(tupleIdx);
            writer.write(equalities.get("E11")+",");
            writer.write(equalities.get("E12")+",");
            writer.write(equalities.get("E21")+",");
            writer.write(equalities.get("E22"));

            writer.write("\n");
        }
    }

    @Override
    public void flush() {
        Writer tupleWriter = null;
        Writer cooccWriter = null;
        Writer reverseCooccWriter = null;

        try {
            tupleWriter = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outputPrefix + outputTupleStoreName + outputFileId)));

            cooccWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPrefix + outputCooccStoreName + outputFileId)));

            reverseCooccWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPrefix + outputCooccStoreName + outputFileId + "r")));

            writeTuple(tupleWriter,outputFileId);
            writeBigram(cooccWriter,outputFileId);
            writeBigram(reverseCooccWriter,outputFileId);
        } catch (IOException ex) {
            System.err.println("Write file failure, I recommend you check it!");
            logger.log(Level.SEVERE,ex.getMessage(),ex);
        }finally{
            try {
                if (tupleWriter != null) {
                    tupleWriter.close();
                }
                if (cooccWriter != null) {
                    cooccWriter.close();
                }
                if (reverseCooccWriter != null) {
                    reverseCooccWriter.close();
                }
            } catch (IOException e) {
                System.err.println("Close writer failure, I recommend you check it!");
                e.printStackTrace();
            }
        }
        // clear memory
        super.flush();
        outputFileId++;
    }
}
