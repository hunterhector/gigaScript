package edu.cmu.cs.lti.gigascript.graph;

import com.google.common.base.Joiner;
import edu.cmu.cs.lti.gigascript.util.Configuration;
import es.yrbcn.graph.weighted.WeightedPageRank;
import gnu.trove.map.hash.TIntObjectHashMap;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.webgraph.labelling.ArcLabelledImmutableGraph;
import org.apache.commons.io.FileUtils;
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
    ArcLabelledSubGraph subgraph;

    private WeightedPageRankWrapper findRelatedTuples(ArcLabelledImmutableGraph graph, Set<Integer> targetNodes) throws IOException {
        System.out.println("Building the subgraph.");
        subgraph = new ArcLabelledSubGraph(graph, targetNodes, 1, true);
//        List<Triple<Integer, Integer, Float>> arcList = subgraph.getArcList();
        List<Triple<Integer, Integer, Float>> arcList = subgraph.getSymmetricArcList();
        ArcLabelledImmutableGraph subGraphAsGraph = GraphUtils.buildWeightedGraphFromTriples(arcList);
        System.out.println("Number of nodes in subgraph "+subGraphAsGraph.numNodes());

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

        System.out.println("Run PageRank");
        WeightedPageRankWrapper wrapper = new WeightedPageRankWrapper(subGraphAsGraph, WeightedPageRank.DEFAULT_ALPHA, true, WeightedPageRank.DEFAULT_THRESHOLD, 20, (initialVector), preferenceVector);

        return wrapper;
    }

    private int[] subgraphIndicesToParentIndices(int[] subgraphIndices){
        int[] originIndices = new int[subgraphIndices.length];
        for (int i = 0; i< subgraphIndices.length;i++){
            originIndices[i] = subgraph.toSupergraphNode(subgraphIndices[i]);
        }
        return originIndices;
    }

    private String[] getSupportingTuples(int expandedIndex,  TIntObjectHashMap<int[]> supportMap, TIntObjectHashMap<String> originHostMap){
        int[] supportId = supportMap.get(expandedIndex);
        String[] supportTuple = new String[supportId.length];
        for (int i =0 ; i< supportId.length ; i++){
            supportTuple[i] = originHostMap.get(supportId[i]);
        }
        return supportTuple;
    }

    public static void main(String[] argv) throws IOException, IllegalAccessException {
        String configPath = "settings.properties";
        if (argv.length > 1) {
            configPath = argv[0];
        }

        Configuration config = new Configuration(new File(configPath));

        String targetNodePath = config.get("edu.cmu.cs.lti.gigaScript.graph.node.targets.path");
        File targetFile = new File(targetNodePath);

        String expandedTuplePath = config.get("edu.cmu.cs.lti.gigaScript.graph.node.map.expand.path");
        String originTuplePath = config.get("edu.cmu.cs.lti.gigaScript.graph.node.map.origin.path");
        int offset = config.getInt("edu.cmu.cs.lti.gigaScript.graph.node.base"); //78195158;

        String storePath = config.get("edu.cmu.cs.lti.gigaScript.graph.base.dir");
        String graphName = config.get("edu.cmu.cs.lti.gigaScript.graph.name");

        String outputPath = config.get("edu.cmu.cs.lti.gigaScript.graph.script.outpath");

        ArcLabelledImmutableGraph graph = GraphUtils.loadAsArcLablelled(storePath, graphName, false);

        Joiner linebreakJoiner = Joiner.on("\n");
        Joiner commaJoiner = Joiner.on(" , ");

        TIntObjectHashMap<String> expandedHostMap = HostMap.loadFromIdMap(new File(expandedTuplePath));
        TIntObjectHashMap<int[]> supportMap = HostMap.loadSupportMap(new File(expandedTuplePath));
        TIntObjectHashMap<String> originHostMap = HostMap.loadFromIdMap(new File(originTuplePath));

        List<String> header =new ArrayList<String>();
        header.add("==========Generated Results=========");

        FileUtils.writeLines(new File(outputPath),header );
        for (String line: FileUtils.readLines(targetFile)) {
            Set<Integer> targetNodes = new HashSet<Integer>();
            int targetNode = Integer.parseInt(line);
            targetNodes.add(targetNode-offset);

            RelatedTupleFinder finder = new RelatedTupleFinder();
            WeightedPageRankWrapper wrapper = finder.findRelatedTuples(graph,targetNodes);

            double[] prResults = wrapper.topKScore(20);
            int[] resultExpandedIndices = finder.subgraphIndicesToParentIndices(wrapper.topKIndex(20));
            String[] relatedTuples = GraphUtils.ToTuple(resultExpandedIndices, offset, expandedHostMap);

            List<String> scriptOut = new ArrayList<String>();

            if (expandedHostMap.containsKey(targetNode)) {
                System.out.println("Target node is " + expandedHostMap.get(targetNode));
            }else{
                System.out.println("Target node not in map: "+targetNode);
            }

            scriptOut.add("=======================");
            scriptOut.add("For tuple "+expandedHostMap.get(targetNode)+" : "+targetNode);
            scriptOut.add("=======================");
            String scores="";
            String sep="";
            for (double score : prResults) {
                scores += sep;
                scores += score;
                sep = " ";
            }
            scriptOut.add(scores);

            for (int resultExpandedIndex: resultExpandedIndices){
                String expandedTuple = expandedHostMap.get(resultExpandedIndex);
                String[] supportTuple = finder.getSupportingTuples(resultExpandedIndex,supportMap,originHostMap);
                scriptOut.add("#=====\n"+expandedTuple + " : "+commaJoiner.join(supportTuple));
            }

//            scriptOut.add(linebreakJoiner.join(relatedTuples));

            FileUtils.writeLines(new File(outputPath), scriptOut,true);
        }
    }
}
