package edu.cmu.cs.lti.gigascript.graph;

import it.unimi.dsi.webgraph.labelling.ArcLabelledImmutableGraph;

import java.io.File;
import java.io.IOException;

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
        File edgeFile = new File(args[0]);

        int nodeNumber = 50943700;
        int nodeBase = 0;
        int scaledTargetNodeId = 1075;

        System.err.println("Building the full graph");
        ArcLabelledImmutableGraph graph = GraphUtils.buildWeightedGraphFromFile(new File("edgeFile"), nodeNumber);
        GraphUtils.storeWeightedGraph(graph, "storage/graph/", "edgeSent");

    }
}
