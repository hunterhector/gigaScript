package edu.cmu.cs.lti.gigascript.graph;

import com.google.common.base.Joiner;
import edu.cmu.cs.lti.gigascript.util.Configuration;
import es.yrbcn.graph.weighted.WeightedPageRank;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.webgraph.labelling.ArcLabelledImmutableGraph;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

    private WeightedPageRankWrapper findRelatedTuples(ArcLabelledImmutableGraph graph, Set<Integer> targetNodes){
        System.err.println("Building the subgraph.");
        ArcLabelledSubGraph subgraph = new ArcLabelledSubGraph(graph, targetNodes, 2, true);
        List<Triple<Integer, Integer, Float>> arcList = subgraph.getArcList();
        ArcLabelledImmutableGraph subGraphAsGraph = GraphUtils.buildWeightedGraphFromTriples(arcList);
        System.err.println("Number of nodes in subgraph "+subGraphAsGraph.numNodes());

        double[] zeroArray = new double[subgraph.subgraphSize];

        List<Integer> targetNodesIdOnSub = new ArrayList<Integer>(targetNodes.size());

        for (int originTargetNodeId : targetNodes){
            targetNodesIdOnSub.add(subgraph.fromSupergraphNode(originTargetNodeId));
        }

        //initial vector assign to nodes at the start of pagerank
        DoubleArrayList initialVector = new DoubleArrayList(zeroArray); //build up during buildReferentArcList
        //preference vector for pagerank
        DoubleArrayList preferenceVector = new DoubleArrayList(zeroArray);

        for (int targetNodeIdOnSub : targetNodesIdOnSub) {
            initialVector.set(targetNodeIdOnSub, 1);
            preferenceVector.set(targetNodeIdOnSub, 1);
        }

        System.err.println("Run PageRank");
        WeightedPageRankWrapper wrapper = new WeightedPageRankWrapper(subGraphAsGraph, WeightedPageRank.DEFAULT_ALPHA, false, WeightedPageRank.DEFAULT_THRESHOLD, 20, (initialVector), preferenceVector);

        return wrapper;
    }


    public static void main(String[] argv) throws IOException, IllegalAccessException {
        String configPath = "settings.properties";
        if (argv.length > 1) {
            configPath = argv[0];
        }

        Configuration config = new Configuration(new File(configPath));

        String targetNodePath = config.get("edu.cmu.cs.lti.gigaScript.graph.node.targets.path");
        File targetFile = new File(targetNodePath);

        String tuplePath = config.get("edu.cmu.cs.lti.gigaScript.graph.node.mapping.path");
        int offset = config.getInt("edu.cmu.cs.lti.gigaScript.graph.node.base"); //78195158;

        String storePath = config.get("edu.cmu.cs.lti.gigaScript.graph.base.dir");
        String graphName = config.get("edu.cmu.cs.lti.gigaScript.graph.name");

        ArcLabelledImmutableGraph graph = GraphUtils.loadAsArcLablelled(storePath, graphName, false);

        Joiner commaJoiner = Joiner.on(" , ");

        for (String line: FileUtils.readLines(targetFile)) {
            Set<Integer> targetNodes = new HashSet<Integer>();
            String[] parts = line.split(" ");
            targetNodes.add(Integer.parseInt(parts[1])-offset);

            RelatedTupleFinder finder = new RelatedTupleFinder();
            WeightedPageRankWrapper wrapper = finder.findRelatedTuples(graph,targetNodes);

            List<Pair<Integer, Double>> prResults = wrapper.topK(10);
            System.out.println(commaJoiner.join(prResults));
            String[] relatedTuples = GraphUtils.ToTuple(wrapper.topKIndex(10), offset, HostMap.loadFromIdMap(new File(tuplePath)));
            System.out.println(commaJoiner.join(relatedTuples));
        }
    }
}
