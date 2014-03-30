package edu.cmu.cs.lti.gigascript.utils;

import com.sun.org.apache.bcel.internal.generic.DUP;
import edu.cmu.cs.lti.gigascript.io.SplittedFileLinesIterator;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 3/29/14
 * Time: 8:06 PM
 */
public class DuplicateRemover {
    private boolean isBigram = true;
    private TObjectIntHashMap<String> dupCounts = new TObjectIntHashMap<String>();

    public DuplicateRemover() {

    }

    public void doRemove(Writer writer, Iterator<String> dupoutIter,Iterator<String> originIter) throws IOException {
        if (dupoutIter.hasNext() && originIter.hasNext()) {
            String dupStr = dupoutIter.next();
            String originStr = originIter.next();

            if (checkSame(dupStr,originStr)){
                writer.write(getRemovedResult(dupStr,originStr));
            }else{
                writer.write(originStr);
            }
        }
    }

    private boolean checkSame(String line1, String line2) {
        if (isBigram) {
            return checkTupleSame(line1,line2);
        } else {
            return checkBigramSame(line1,line2);
        }
    }

    private boolean checkTupleSame(String line1, String line2) {
        String[] parts1 = line1.split("\t");
        String[] parts2 = line2.split("\t");

        if (parts1.length == 0 || parts2.length == 0){
            System.out.println("=========");
            System.out.println(line1);
            System.out.println(line2);
            return false;
        }

        return parts1[0].equals(parts2[0]);
    }

    private boolean checkBigramSame(String line1, String line2) {
        String[] parts1 = line1.split("\t");
        String[] parts2 = line2.split("\t");

        if (parts1.length < 2 || parts2.length < 2){
            System.out.println("=========");
            System.out.println(line1);
            System.out.println(line2);
            return false;
        }

        return (parts1[0]+parts1[1]).equals(parts2[0]+parts2[1]);
    }

    private String getRemovedResult(String dup, String origin) {
        if (isBigram) {
            return getBigramResult(dup, origin);
        } else {
            return getTupleResult(dup, origin);
        }
    }

    private String getTupleResult(String dup, String origin) {

    }

    private String getBigramResult(String dup, String origin) {

    }

    public static void main(String[] args) throws IOException {
        String dupFilePath = "";
        String dupFileBasename = "";

        String originFilePath = "";
        String originFileBasename = "";

        String outputFilePath = "";

        File dupFile = new File(dupFilePath);
        File originalFile = new File(originFilePath);

        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFilePath)));

        Iterator<String> dupoutIter;
        Iterator<String> originIter;

        if (dupFile.isDirectory()) {
            dupoutIter = new SplittedFileLinesIterator(dupFile, dupFileBasename);
        } else {
            dupoutIter = FileUtils.lineIterator(dupFile);
        }

        if (originalFile.isDirectory()) {
            originIter = new SplittedFileLinesIterator(originalFile, originFileBasename);
        } else {
            originIter = FileUtils.lineIterator(originalFile);
        }

        DuplicateRemover remover  = new DuplicateRemover();
        remover.doRemove(writer,dupoutIter,originIter);
    }

}
