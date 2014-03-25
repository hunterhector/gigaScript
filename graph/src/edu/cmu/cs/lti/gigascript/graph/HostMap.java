package edu.cmu.cs.lti.gigascript.graph;

import gnu.trove.map.hash.TByteByteHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 3/18/14
 * Time: 12:11 PM
 */
public class HostMap {

   public static TObjectIntHashMap<String> loadIdMap(File tupleFile) throws IOException {
       TObjectIntHashMap<String> idMap = new TObjectIntHashMap<String>();

       System.out.println("Reading the tuple mapping");
       FileReader file = new FileReader(tupleFile);
       BufferedReader br = new BufferedReader(file);

       String line;
       while ((line = br.readLine()) != null) {
           String[] fields = line.split("\t");
           if (fields.length != 5){
               System.out.print(line);
           }
           idMap.put(fields[0], Integer.parseInt(fields[1]));
       }

       return idMap;
   }

   public static TObjectIntHashMap<String> loadCountsMap(File tupleFile) throws IOException{
       TObjectIntHashMap<String> countMap = new TObjectIntHashMap<String>();
       System.out.println("Reading the count mapping");
       FileReader file = new FileReader(tupleFile);
       BufferedReader br = new BufferedReader(file);

       String line;
       while ((line = br.readLine()) != null) {
           String[] fields = line.split("\t");
           if (fields.length != 5){
               System.out.print(line);
           }
           countMap.put(fields[0], Integer.parseInt(fields[2]));
       }

       return countMap;
   }

    public static void loadTypeMap(File tupleFile) throws IOException {
        TByteByteHashMap typeMap = new TByteByteHashMap();
        System.out.println("Reading the type mapping");
        FileReader file = new FileReader(tupleFile);
        BufferedReader br = new BufferedReader(file);

        String line;
        while ((line = br.readLine()) != null) {
            String[] fields = line.split("\t");
            if (fields.length != 5){
                System.out.print(line);
            }
        }

    }

    public static void main(String[] args) throws IOException {
        Runtime runtime = Runtime.getRuntime();

        long usableFreeMemory= runtime.maxMemory()
                -Runtime.getRuntime().totalMemory()
                +Runtime.getRuntime().freeMemory();
        System.out.println("Free memory before (MB): " + usableFreeMemory/(1024 * 1024 *1.0));

        TObjectIntHashMap<String> idMap = loadIdMap(new File("scripts/tuplesOther"));

        System.out.println(idMap.size());

        usableFreeMemory = runtime.maxMemory()
                -Runtime.getRuntime().totalMemory()
                +Runtime.getRuntime().freeMemory();
        System.out.println("Free memory after (MB): " + usableFreeMemory/(1024 * 1024 *1.0));
    }
}
