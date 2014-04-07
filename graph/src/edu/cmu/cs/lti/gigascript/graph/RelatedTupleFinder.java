package edu.cmu.cs.lti.gigascript.graph;

import com.google.common.base.Joiner;
import es.yrbcn.graph.weighted.WeightedPageRank;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.webgraph.labelling.ArcLabelledImmutableGraph;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 4/7/14
 * Time: 1:14 AM
 */
public class RelatedTupleFinder {
    public static void main(String[] argv) throws IOException, IllegalAccessException {
        int scaledTargetNodeId = 1075;

        String tuplePath = "";
        int offset = -1;


        ArcLabelledImmutableGraph graph = GraphUtils.loadAsArcLablelled("storage/graph/", "edgeSent", false);

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

        GraphUtils.ToTuple(wrapper.topKIndex(10), offset, HostMap.loadFromIdMap(new File(tuplePath)));
    }
}
