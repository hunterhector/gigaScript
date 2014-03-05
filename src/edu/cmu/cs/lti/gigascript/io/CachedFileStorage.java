package edu.cmu.cs.lti.gigascript.io;

import com.google.common.collect.Table.Cell;
import edu.cmu.cs.lti.gigascript.model.AgigaArgument;
import edu.cmu.cs.lti.gigascript.model.BigramInfo;
import edu.cmu.cs.lti.gigascript.util.Configuration;
import edu.cmu.cs.lti.gigascript.util.IOUtils;
import gnu.trove.iterator.TObjectIntIterator;
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

    private String logPath;

    public CachedFileStorage(Configuration config) {
        super(config);
        outputPrefix = config.get("edu.cmu.cs.lti.gigaScript.file.prefix");
        logPath = config.get("edu.cmu.cs.lti.gigaScript.log");
    }

    @Override
    public long addGigaTuple(AgigaArgument arg0, AgigaArgument arg1, String relation) {
        return cacheTuple(arg0,arg1,relation);
    }

    @Override
    public void addGigaBigram(long t1, long t2, int sentDistance, int tupleDistance, int[][] equality) {
       cacheBigram(t1,t2, sentDistance,tupleDistance, equality);
    }

    private void writeTuple(Writer writer) throws IOException {
        TObjectIntIterator<Triple<String, String, String>> iter = tupleIds.iterator();

        while (iter.hasNext()){
            iter.advance();
            writer.write(iter.key().toString());//the key is the tuple, a primary key
            int tupleId = iter.value();
            writer.write("\t"+ tupleId);
            writer.write("\t" + tupleCount.get(tupleId));
            writer.write("\t"+ tupleEntityTypes.get(tupleId));
            writer.write("\n");
        }
    }

    private void writeBigram(Writer writer) throws IOException{
        for (Cell<Long,Long,BigramInfo> cell  : bigramInfoTable.cellSet()){
            writer.write(cell.getRowKey()+"\t"+cell.getColumnKey()+"\t"); //this pair is the primary key
            BigramInfo info = cell.getValue();

            IOUtils.writeMap(writer,info.getSentenceDistanceCount(),":",",");

            writer.write("\t");

            IOUtils.writeMap(writer, info.getTupleDistanceCount(), ":", ",");

            writer.write("\t");

            IOUtils.writeList(writer, info.getTupleEqualityCount(), ",");

            writer.write("\n");
        }
    }

    @Override
    public void flush() {
//        System.out.println("\nBegin flushing");
        Writer tupleWriter = null;
        Writer cooccWriter = null;

        try {
            tupleWriter = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outputPrefix + outputTupleStoreName + outputFileId)));
            cooccWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPrefix + outputCooccStoreName + outputFileId)));

            writeTuple(tupleWriter);
            writeBigram(cooccWriter);
        } catch (IOException ex) {
            System.err.println("Write file failure, I recommend you check it! See the log for more detail: "+logPath);
            logger.log(Level.SEVERE,ex.getMessage(),ex);
        }finally{
            try {
                if (tupleWriter != null) {
                    tupleWriter.close();
                }
                if (cooccWriter != null) {
                    cooccWriter.close();
                }
            } catch (IOException e) {
                System.err.println("Close writer failure, I recommend you check it! See the log for more detail: "+logPath);
                e.printStackTrace();
            }
        }
        // clear memory
        super.flush();
        outputFileId++;

//        System.out.println("\nEnd flushing");
    }
}
