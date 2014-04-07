package edu.cmu.cs.lti.gigascript.graph;

import edu.cmu.cs.lti.gigascript.util.Configuration;
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
    public static void main(String[] args) throws IOException {
        String configPath = "settings.properties";
        if (args.length > 1) {
            configPath = args[0];
        }
        Configuration config = new Configuration(new File(configPath));

        String storePath = config.get("edu.cmu.cs.lti.gigaScript.graph.basedir");
        String graphName = config.get("edu.cmu.cs.lti.gigaScript.graph.name");

        File edgeFile = new File(config.get("edu.cmu.cs.lti.gigaScript.graph.edges"));

        int nodeBase = config.getInt("edu.cmu.cs.lti.gigaScript.graph.node.base"); //0;
        int largestNode = config.getInt("edu.cmu.cs.lti.gigaScript.graph.node.largest");
        int nodeNumber =largestNode-nodeBase + 1;

        int arcNumber = config.getInt("edu.cmu.cs.lti.gigaScript.graph.arc.number");

        boolean edgeSorted = config.getBoolean("edu.cmu.cs.lti.gigaScript.graph.edge.sorted");

        File outputDir = new File(storePath);

        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                System.err.println("Cannot create directory to store graph on :" + outputDir.getCanonicalPath());
            } else{
                System.out.println("Created directory to write graph");
            }
        }else{
            System.out.println("Storing the graph at "+outputDir);
        }

        System.out.println("Building the full graph");
        ArcLabelledImmutableGraph graph = GraphUtils.buildWeightedGraphFromFile(edgeFile, nodeNumber,arcNumber,nodeBase, edgeSorted);

        GraphUtils.storeWeightedGraph(graph, storePath, graphName);
    }
}