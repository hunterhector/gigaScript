package edu.cmu.cs.lti.gigascript.graph;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 3/18/14
 * Time: 9:36 PM
 */
public class GraphMaker {

    private static void makeBigramCountGraph(){

    }

    public static void main(String[] args) throws IOException {
        File bigramFile = new File(args[0]);
        System.out.println("Reading the bigram counts");
        FileReader file = new FileReader(bigramFile);
        BufferedReader br = new BufferedReader(file);

        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.trim().split("\t");

            if (parts.length != 5) {
                continue;
            }
        }
    }
}
