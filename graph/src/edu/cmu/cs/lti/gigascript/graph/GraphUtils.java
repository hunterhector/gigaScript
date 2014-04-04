package edu.cmu.cs.lti.gigascript.graph;

import es.yrbcn.graph.weighted.WeightedArc;
import es.yrbcn.graph.weighted.WeightedBVGraph;
import es.yrbcn.graph.weighted.WeightedPageRank;
import es.yrbcn.graph.weighted.WeightedPageRankPowerMethod;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.webgraph.labelling.ArcLabelledImmutableGraph;
import org.apache.commons.io.FileUtils;
import scala.Float;
import scala.Int;
import scala.Tuple3;
import scala.collection.mutable.ListBuffer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public static ArcLabelledImmutableGraph buildWeightedGraphFromTriples(List<Tuple3<Int, Int, Float>> triples) {

        WeightedArc[] weightedArcArray = new WeightedArc[triples.size()];

        for (int i = 0; i < triples.size(); i++) {
            Tuple3<Integer, Integer, Float> triple = triples.get(i);
            weightedArcArray[i] = new WeightedArc(triple._1(), triple._2(), triple._3());
        }

        ArcLabelledImmutableGraph aig = new WeightedBVGraph(weightedArcArray);

        return aig;
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

        ArcLabelledImmutableGraph graph = buildWeightedGraphFromFile(new File("weightedEdgeSent"), 51050130);
        WeightedPageRankPowerMethod pr = new WeightedPageRankPowerMethod(graph);

        ArcLabelledSubGraph subgraph = new ArcLabelledSubGraph(graph, (targetNodes), 2, true);
        ListBuffer<Tuple3<Object, Object, Object>> arcList = subgraph.getArcList();

        ArcLabelledImmutableGraph subGraphAsGraph = GraphUtils.buildWeightedGraphFromTriples(arcList, subgraph.subgraphSize());

        double[] pageWeights = WeightedPageRankWrapper.run(subGraphAsGraph, WeightedPageRank.DEFAULT_ALPHA, false, WeightedPageRank.DEFAULT_THRESHOLD, 20, (initialVector), preferenceVector);
    }
}
