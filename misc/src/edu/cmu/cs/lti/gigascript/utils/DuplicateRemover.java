package edu.cmu.cs.lti.gigascript.utils;

import edu.cmu.cs.lti.gigascript.io.SplittedFileLinesIterator;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 3/29/14
 * Time: 8:06 PM
 */
public class DuplicateRemover {
    private boolean isBigram = false;

    public DuplicateRemover(boolean isBigram) throws IOException {
        this.isBigram = isBigram;
    }

    public void doRemove(Writer writer, Iterator<String> dupoutIter,Iterator<String> originIter) throws IOException {
        if (dupoutIter.hasNext() && originIter.hasNext()) {
            String dupStr = dupoutIter.next();
            String originStr = originIter.next();

            while(true) {
                int compare = compareStr(dupStr, originStr);

                System.out.println(dupStr);
                System.out.println(originStr);
                System.out.println(compare);

                if (compare == 0) {
                    System.out.println("Removing duplicates!");
                    writer.write(getRemovedResult(dupStr, originStr));
                    if (!(originIter.hasNext() && dupoutIter.hasNext())){
                        break;
                    }
                    originStr= originIter.next();
                    dupStr = dupoutIter.next();
                } else if (compare > 0) {
                    System.out.println("origin forward");
                    //dup is larger, so origin is behind
                    if (!originIter.hasNext()){
                        break;
                    }
                    originStr= originIter.next();
                    writer.write(originStr+"\n");
                } else {
                    System.out.println("dup forward");
                    //dup is behind
                    if (!dupoutIter.hasNext()){
                        break;
                    }
                    dupStr = dupoutIter.next();
                }
                Scanner in = new Scanner( System.in );
                in.nextLine();
            }
        }

        while (originIter.hasNext()){
            String originStr = originIter.next();
            writer.write(originStr+"\n");
        }
    }

    private int compareStr(String line1, String line2) {
        if (isBigram) {
            return compareBigram(line1, line2);
        } else {
            return compareTuple(line1, line2);
        }
    }

    private int compareTuple(String line1, String line2) {
        String[] parts1 = line1.split("\t");
        String[] parts2 = line2.split("\t");

//        System.out.println(parts1[0]+" "+parts2[0]+" "+parts1[0].compareTo(parts2[0]));

        if (parts1.length == 0 ){
            System.out.println("=========");
            System.out.println(line1);
            System.out.println(line2);
            return -1;
        }else if (parts2.length == 0){
            System.out.println("=========");
            System.out.println(line1);
            System.out.println(line2);
            return 1;
        }

        return parts1[0].compareTo(parts2[0]);
    }

    private int compareBigram(String line1, String line2) {
        String[] parts1 = line1.split("\t");
        String[] parts2 = line2.split("\t");

        if (parts1.length < 2 ){
            System.out.println("=========");
            System.out.println(line1);
            System.out.println(line2);
            return -1;
        }else if(parts2.length < 2){
            System.out.println("=========");
            System.out.println(line1);
            System.out.println(line2);
            return 1;
        }

        return (parts1[0]+parts1[1]).compareTo(parts2[0]+parts2[1]);
    }

    private String getRemovedResult(String dup, String origin) {
        if (isBigram) {
            return getBigramResult(dup, origin);
        } else {
            return getTupleResult(dup, origin);
        }
    }

    private String getTupleResult(String dup, String origin) {
        String[] dupParts = dup.split("\t");
        String[] originParts = origin.split("\t");

        int dupCount = Integer.parseInt(dupParts[2]);
        int originCount = Integer.parseInt(originParts[2]);

        int newCount = originCount - dupCount;

        return String.format("%s\t%s\t%d\t%s\t%s\n",dupParts[0],dupParts[1],newCount,dupParts[3],dupParts[4]);
    }

    private String getBigramResult(String dup, String origin) {
        String[] dupParts = dup.split("\t");
        String[] originParts = origin.split("\t");

        TIntIntHashMap dupSentDist = string2Map(dupParts[2]);
        TIntIntHashMap originSentDist = string2Map(originParts[2]);

        TIntIntHashMap dupTupleDist = string2Map(dupParts[3]);
        TIntIntHashMap originTupleDist = string2Map(originParts[3]);

        removeDupFromMap(originSentDist,dupSentDist);
        removeDupFromMap(originTupleDist,dupTupleDist);

        return String.format("%s\t%s\t%s\t%s\t%s\n",originParts[0],originParts[1],map2String(originSentDist),map2String(originTupleDist),originParts[4]);
    }

    private void removeDupFromMap(TIntIntHashMap originMap, TIntIntHashMap dupMap ){
        for (int key : dupMap.keys()){
            if (originMap.containsKey(key)) {
                if (originMap.get(key) > dupMap.get(key)) {
                    originMap.adjustValue(key, -dupMap.get(key));
                }
            }
        }
    }

    private String map2String(TIntIntHashMap map){
        String str = "";

        int [] keys = map.keys();
        Arrays.sort(map.keys());

        String det = "";
        for (int dist : keys){
            str += det;
            str += String.format("%d:%d",dist,map.get(dist));
            det = ",";
        }

        return str;
    }

    private TIntIntHashMap string2Map(String mapStr){
        String[] parts = mapStr.split(",");
        TIntIntHashMap distCounts = new TIntIntHashMap();
        for (String distPair : parts){
            String[] pair = distPair.split(":");
            if (pair.length != 2){
                System.err.println("Wrong map string " + mapStr);
            }else{
                distCounts.put(Integer.parseInt(pair[0]), Integer.parseInt(pair[1]));
            }
        }
        return distCounts;
    }

    public static void main(String[] args) throws IOException {
        String dupFilePath = args[0];
        String dupFileBasename = "";

        String originFilePath = args[1];
        String originFileBasename = "";

        boolean isBigram = false;

        if (args.length == 3){
            isBigram = true;
        }

        String outputFilePath = "dup_removed";

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

        DuplicateRemover remover  = new DuplicateRemover(isBigram);
        remover.doRemove(writer,dupoutIter,originIter);

        writer.close();
    }

}
