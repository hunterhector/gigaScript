package edu.cmu.cs.lti.gigascript.graph;

import com.google.common.base.Joiner;
import es.yrbcn.graph.weighted.WeightedArc;
import es.yrbcn.graph.weighted.WeightedBVGraph;
import es.yrbcn.graph.weighted.WeightedPageRank;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.labelling.ArcLabelledImmutableGraph;
import it.unimi.dsi.webgraph.labelling.BitStreamArcLabelledImmutableGraph;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 4/2/14
 * Time: 1:21 PM
 */
public class GraphUtils {
    /**
     * Build from the Integer list file a ArclabelledImmutableGraph, the label actually encode weight of an arc,
     * but it need a parameter to specify number of nodes this graph will have
     *
     * @param integerListFile
     * @param numNodes
     * @return an ArclabelledImmutableGraph representing the es.yrbcn.graph.weighted graph
     */
    public static ArcLabelledImmutableGraph buildWeightedGraphFromFile(File integerListFile, int numNodes) throws IOException {
        List<WeightedArc> weightedArcArray = new ArrayList<WeightedArc>();
        for (String line : FileUtils.readLines(integerListFile)) {
            WeightedArc arc = new WeightedArc(line);
            weightedArcArray.add(arc);
        }

        ArcLabelledImmutableGraph aig = new WeightedBVGraph(weightedArcArray.toArray(new WeightedArc[weightedArcArray.size()]), numNodes);

        return aig;
    }

    /**
     * Build a weighted graph from triples list,
     *
     * @param triples
     * @return an ArclabelledImmutableGraph representing the weighted graph
     */
    public static ArcLabelledImmutableGraph buildWeightedGraphFromTriples(List<Triple<Integer, Integer, Float>> triples) {

        WeightedArc[] weightedArcArray = new WeightedArc[triples.size()];

        for (int i = 0; i < triples.size(); i++) {
            Triple<Integer, Integer, Float>  triple = triples.get(i);
            weightedArcArray[i] = new WeightedArc(triple.getLeft(), triple.getMiddle(), triple.getRight());
        }

        ArcLabelledImmutableGraph aig = new WeightedBVGraph(weightedArcArray);

        return aig;
    }

    public static void storeWeightedGraph(ArcLabelledImmutableGraph g, String path, String basename) throws IOException {
        String fullPath = path+basename;
        System.err.println("Storing weighted graph to "+fullPath);
        System.err.println("Storing labels");
        BitStreamArcLabelledImmutableGraph.store(g,fullPath,basename+ArcLabelledImmutableGraph.UNDERLYINGGRAPH_SUFFIX);
        System.err.println("Compressing graph");
        BVGraph.store(g,fullPath+ArcLabelledImmutableGraph.UNDERLYINGGRAPH_SUFFIX);
        System.err.println("Graph stored.");
    }

    public static BVGraph loadBVGraph(String path, String basename) throws IOException {
        String fullPath = path + basename;
        System.err.println("Loading the Graph from "+fullPath);
        BVGraph g = BVGraph.load(fullPath);
        return g;
    }

    public static ArcLabelledImmutableGraph loadAsArcLablelled(String path, String baseName, boolean offline) throws IOException {
        String fullPath = path + baseName;
        System.err.println("Loading the graph from " + fullPath + " as ArcLablelledGraph");
        ArcLabelledImmutableGraph g = (offline) ? ArcLabelledImmutableGraph.loadOffline(fullPath) : ArcLabelledImmutableGraph.load(fullPath);
        System.err.println("Done");
        return g;
    }

    public static void main(String[] argv) throws IOException, IllegalAccessException {
        int scaledTargetNodeId = 1075;

        ArcLabelledImmutableGraph graph = loadAsArcLablelled("storage/graph/", "edgeSent", false);

        System.err.println("Building the subgraph");
        Set<Integer> targetNodes = new HashSet<Integer>();
        targetNodes.add(scaledTargetNodeId);

        ArcLabelledSubGraph subgraph = new ArcLabelledSubGraph(graph, (targetNodes), 2, true);
        List<Triple<Integer, Integer, Float>> arcList = subgraph.getArcList();
        ArcLabelledImmutableGraph subGraphAsGraph = GraphUtils.buildWeightedGraphFromTriples(arcList);
        System.err.println("Number of nodes in subgraph "+subGraphAsGraph.numNodes());

        double[] zeroArray = new double[subgraph.subgraphSize];
        int targetNodeIdOnSub = subgraph.fromSupergraphNode(scaledTargetNodeId);
        //initial vector assign to nodes at the start of pagerank
        DoubleArrayList initialVector = new DoubleArrayList(zeroArray); //build up during buildReferentArcList
        //preference vector for pagerank
        DoubleArrayList preferenceVector = new DoubleArrayList(zeroArray);
        initialVector.set(targetNodeIdOnSub, 1);
        preferenceVector.set(targetNodeIdOnSub, 1);


        System.err.println("Run PageRank");
        WeightedPageRankWrapper wrapper = new WeightedPageRankWrapper(subGraphAsGraph, WeightedPageRank.DEFAULT_ALPHA, false, WeightedPageRank.DEFAULT_THRESHOLD, 20, (initialVector), preferenceVector);
        List<Pair<Integer, Double>> prResults = wrapper.topK(10);

        Joiner commaJoiner = Joiner.on(" , ");

        System.out.println(commaJoiner.join(prResults));
    }
}
