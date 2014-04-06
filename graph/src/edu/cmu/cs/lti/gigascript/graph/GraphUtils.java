package edu.cmu.cs.lti.gigascript.graph;

import es.yrbcn.graph.weighted.WeightedArc;
import es.yrbcn.graph.weighted.WeightedBVGraph;
import es.yrbcn.graph.weighted.WeightedPageRank;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.labelling.ArcLabelledImmutableGraph;
import it.unimi.dsi.webgraph.labelling.BitStreamArcLabelledImmutableGraph;
import org.apache.commons.io.FileUtils;
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

    public BVGraph loadBVGraph(String path, String basename) throws IOException {
        String fullPath = path + basename;
        System.err.println("Loading the Graph from "+fullPath);
        BVGraph g = BVGraph.load(fullPath);
        return g;
    }

    public ArcLabelledImmutableGraph loadAsArcLablelled(String path, String baseName, boolean offline) throws IOException {
        String fullPath = path + baseName;
        System.err.println("Loading the graph from " + fullPath + " as ArcLablelledGraph");
        ArcLabelledImmutableGraph g = (offline) ? ArcLabelledImmutableGraph.loadOffline(fullPath) : ArcLabelledImmutableGraph.load(fullPath);
        System.err.println("Done");
        return g;
    }

    public static void main(String[] argv) throws IOException {
        int nodeNumber = 51050130;
        int nodeBase = 78195157;
        int scaledTargetNodeId = 110658549 - nodeBase - 1;

        double[] zeroArray = new double[nodeNumber];
        //initial vector assign to nodes at the start of pagerank
        DoubleArrayList initialVector = new DoubleArrayList(zeroArray); //build up during buildReferentArcList
        //preference vector for pagerank
        DoubleArrayList preferenceVector = new DoubleArrayList(zeroArray);
        Set<Integer> targetNodes = new HashSet<Integer>();
        targetNodes.add(scaledTargetNodeId);
        initialVector.set(scaledTargetNodeId, 1);
        preferenceVector.set(scaledTargetNodeId, 1);

        System.err.println("Building the full graph");
        ArcLabelledImmutableGraph graph = buildWeightedGraphFromFile(new File("weightedEdgeSent"), 51050130);
        storeWeightedGraph(graph,"storage/graph/","edgeSent");

        System.err.println("Building the subgraph");
        ArcLabelledSubGraph subgraph = new ArcLabelledSubGraph(graph, (targetNodes), 2, true);
        List<Triple<Integer, Integer, Float>> arcList = subgraph.getArcList();
        ArcLabelledImmutableGraph subGraphAsGraph = GraphUtils.buildWeightedGraphFromTriples(arcList);

        System.err.println("Run PageRank");
        double[] pageWeights = WeightedPageRankWrapper.run(subGraphAsGraph, WeightedPageRank.DEFAULT_ALPHA, false, WeightedPageRank.DEFAULT_THRESHOLD, 20, (initialVector), preferenceVector);
        Arrays.sort(pageWeights);
        double[] tops = Arrays.copyOfRange(pageWeights,pageWeights.length-4,pageWeights.length);
        System.out.println(Arrays.toString(tops));
    }
}
