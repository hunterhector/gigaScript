package edu.cmu.cs.lti.gigascript.graph;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 3/18/14
 * Time: 12:11 PM
 */
public class HostMap {

   public void loadIdMap(File tupleFile) throws IOException {
       System.out.println("Reading the tuple mapping");
       FileReader file = new FileReader(tupleFile);
       BufferedReader br = new BufferedReader(file);

       String line;
       while ((line = br.readLine()) != null) {
           String[] fields = line.split("\t");
       }
   }

   public void loadCountsMap(File tupleFile) throws IOException{

   }

    public void loadTypeMap(File tupleFile){

    }
}
