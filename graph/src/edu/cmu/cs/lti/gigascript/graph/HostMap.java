package edu.cmu.cs.lti.gigascript.graph;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 3/18/14
 * Time: 12:11 PM
 */
public class HostMap {

   public static TObjectIntHashMap<String> loadToIdMap(File tupleFile) throws IOException {
       TObjectIntHashMap<String> idMap = new TObjectIntHashMap<String>();

       System.out.println("Reading the tuple mapping");
       FileReader file = new FileReader(tupleFile);
       BufferedReader br = new BufferedReader(file);

       String line;
       while ((line = br.readLine()) != null) {
           String[] fields = line.split("\t");
           idMap.put(fields[0], Integer.parseInt(fields[1]));
       }

       return idMap;
   }

    public static TIntObjectHashMap<String> loadFromIdMap(File tupleFile) throws IOException {
        TIntObjectHashMap<String> idMap = new TIntObjectHashMap<String>();

        System.out.println("Reading the tuple mapping");
        FileReader file = new FileReader(tupleFile);
        BufferedReader br = new BufferedReader(file);

        String line;
        while ((line = br.readLine()) != null) {
            String[] fields = line.split("\t");
            idMap.put(Integer.parseInt(fields[1]),fields[0]);
        }

        return idMap;
    }

    public static void main(String[] args) throws IOException {
        Runtime runtime = Runtime.getRuntime();

        long usableFreeMemory= runtime.maxMemory()
                -Runtime.getRuntime().totalMemory()
                +Runtime.getRuntime().freeMemory();
        System.out.println("Free memory before (MB): " + usableFreeMemory/(1024 * 1024 *1.0));

        TObjectIntHashMap<String> idMap = loadToIdMap(new File("scripts/tuplesOther"));

        System.out.println(idMap.size());

        usableFreeMemory = runtime.maxMemory()
                -Runtime.getRuntime().totalMemory()
                +Runtime.getRuntime().freeMemory();
        System.out.println("Free memory after (MB): " + usableFreeMemory/(1024 * 1024 *1.0));
    }
}
