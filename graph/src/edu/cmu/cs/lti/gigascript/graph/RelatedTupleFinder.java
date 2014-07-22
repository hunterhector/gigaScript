package edu.cmu.cs.lti.gigascript.graph;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import edu.cmu.cs.lti.gigascript.model.TupleInfo;
import edu.cmu.cs.lti.gigascript.util.Configuration;
import es.yrbcn.graph.weighted.WeightedPageRank;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.webgraph.labelling.ArcLabelledImmutableGraph;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Triple;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 4/7/14
 * Time: 1:14 AM
 */
public class RelatedTupleFinder {
    ArcLabelledSubGraph subgraph;

    private WeightedPageRankWrapper findRelatedTuples(ArcLabelledImmutableGraph graph, Set<Integer> targetNodes, int hop) throws IOException {
        System.out.println("Building the subgraph.");
        subgraph = new ArcLabelledSubGraph(graph, targetNodes, hop, true);
        List<Triple<Integer, Integer, Float>> arcList = subgraph.getSymmetricArcList();
        ArcLabelledImmutableGraph subGraphAsGraph = GraphUtils.buildWeightedGraphFromTriples(arcList);
        System.out.println("Number of nodes in subgraph " + subGraphAsGraph.numNodes());

        double[] zeroArray = new double[subgraph.subgraphSize];

        List<Integer> targetNodesIdOnSub = new ArrayList<Integer>(targetNodes.size());

        for (int originTargetNodeId : targetNodes) {
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

    private int[] subgraphIndicesToParentIndices(int[] subgraphIndices) {
        int[] originIndices = new int[subgraphIndices.length];
        for (int i = 0; i < subgraphIndices.length; i++) {
            originIndices[i] = subgraph.toSupergraphNode(subgraphIndices[i]);
        }
        return originIndices;
    }

    private TupleInfo[] getSupportingTuples(int expandedIndex, TIntObjectHashMap<int[]> supportMap, TIntObjectHashMap<TupleInfo> originHostMap) {
        int[] supportId = supportMap.get(expandedIndex);
        TupleInfo[] supportTuples = new TupleInfo[supportId.length];
        for (int i = 0; i < supportId.length; i++) {
            supportTuples[i] = originHostMap.get(supportId[i]);
        }
        return supportTuples;
    }

    private ArrayListMultimap<Integer, TupleInfo> gatherInstances(int targetIndex, int[] resultExpandedIndices, TIntObjectHashMap<int[]> supportMap, TIntObjectHashMap<TupleInfo> originHostMap) {
        System.out.println("Gathering related tuples for "+targetIndex);

        Set<String> targetDocuments = new HashSet<String>();

        for (TupleInfo.TupleAddress address : originHostMap.get(targetIndex).getAddresses()) {
            targetDocuments.add(address.fileAddress);
            System.out.println("Gathering starts from document : "+address.fileAddress);
        }

        Set<TupleInfo> rawResults = new HashSet<TupleInfo>();

        TObjectIntMap<TupleInfo> tuple2ExpandedView = new TObjectIntHashMap<TupleInfo>();

        for (int resultExpandedIndex : resultExpandedIndices) {
            TupleInfo[] supportingTuples = getSupportingTuples(resultExpandedIndex, supportMap, originHostMap);
            rawResults.addAll(Arrays.asList(supportingTuples));

            for (TupleInfo t : supportingTuples){
                if (tuple2ExpandedView.containsKey(t)){
                    if (supportMap.get(tuple2ExpandedView.get(t)).length < supportMap.get(resultExpandedIndex).length){
                        tuple2ExpandedView.put(t, resultExpandedIndex);
                    }
                }else {
                    tuple2ExpandedView.put(t, resultExpandedIndex);
                }
            }
        }
        Set<TupleInfo> closureTuples = getDocumentTransitiveClosureOnTuples(rawResults,targetDocuments);

        System.out.println("Number of tuples in unfiltered set: "+rawResults.size());
        System.out.println("Number of tuples in closure set: "+closureTuples.size());

        ArrayListMultimap<Integer,TupleInfo> gatheredTuples =  ArrayListMultimap.create();

        for (TupleInfo closureTuple : closureTuples){
                gatheredTuples.put(tuple2ExpandedView.get(closureTuple),closureTuple);
        }

        return gatheredTuples;
    }

    private Set<TupleInfo> getDocumentTransitiveClosureOnTuples(Set<TupleInfo> tupleSet, Set<String> sourceDocumentAddress) {
        Set<String> addressSet = new HashSet<String>();
        addressSet.addAll(sourceDocumentAddress);

        boolean hasNewInstances = true;

        Set<TupleInfo> closureSet = new HashSet<TupleInfo>();

        while(hasNewInstances) {
            hasNewInstances = false;
            for (Iterator<TupleInfo> iter = tupleSet.iterator(); iter.hasNext();) {
                TupleInfo tupleInfo = iter.next();
                boolean isRelated = false;
                for (TupleInfo.TupleAddress tupleAddress : tupleInfo.getAddresses()) {
                    if (addressSet.contains(tupleAddress.fileAddress)) {
                        isRelated = true;
                        break;
                    }else{
                        System.out.println(tupleAddress.fileAddress+" is not relavant");
                    }
                }

                if (isRelated) {
                    for (TupleInfo.TupleAddress tupleAddress : tupleInfo.getAddresses()) {
                        addressSet.add(tupleAddress.fileAddress);
                    }
                    hasNewInstances = true;
                    closureSet.add(tupleInfo);
                    iter.remove(); //remove already added in tuples
                }
            }
        }

        return closureSet;
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
        int offset = config.getInt("edu.cmu.cs.lti.gigaScript.graph.node.base");
        int largets = config.getInt("edu.cmu.cs.lti.gigaScript.graph.node.largest");
        int numNodes = largets - offset + 1;

        int hop = config.getInt("edu.cmu.cs.lti.gigaScript.graph.subgraph.hop");

        String splitedGraphPath = config.get("edu.cmu.cs.lti.gigaScript.graph.splited");

        String outputPath = config.get("edu.cmu.cs.lti.gigaScript.graph.script.outpath");

        ArcLabelledImmutableGraph graph = new FileBasedGraph(new File(splitedGraphPath), numNodes);

        Joiner commaJoiner = Joiner.on(" , ");
        Joiner lbJoiner = Joiner.on("\n");

        TIntObjectHashMap<String> expandedHostMap = HostMap.loadFromIdMap(new File(expandedTuplePath));
        TIntObjectHashMap<int[]> expandedTuple2Origins = HostMap.loadSupportMap(new File(expandedTuplePath));
        TIntObjectHashMap<TupleInfo> originTupleInfoHostMap = HostMap.loadOriginalTupleInfo(new File(originTuplePath));

        List<String> header = new ArrayList<String>();
        header.add("==========Generated Results=========");

        FileUtils.writeLines(new File(outputPath), header);
        for (String line : FileUtils.readLines(targetFile)) {
            Set<Integer> targetNodes = new HashSet<Integer>();
            int targetNode = Integer.parseInt(line);
            targetNodes.add(targetNode - offset);

            if (expandedHostMap.containsKey(targetNode)) {
                System.out.println("Target node is " + expandedHostMap.get(targetNode));
            } else {
                System.out.println("Target node not in map: " + targetNode);
            }

            RelatedTupleFinder finder = new RelatedTupleFinder();
            WeightedPageRankWrapper wrapper = finder.findRelatedTuples(graph, targetNodes, hop);

            double[] prResults = wrapper.topKScore(20);
            int[] resultExpandedIndices = finder.subgraphIndicesToParentIndices(wrapper.topKIndex(20));
//            String[] resultExpandedTupleStrs = GraphUtils.toTuple(resultExpandedIndices, offset, expandedHostMap);

            ArrayListMultimap<Integer, TupleInfo> relatedInstances = finder.gatherInstances(targetNode, resultExpandedIndices, expandedTuple2Origins, originTupleInfoHostMap);

            List<String> scriptOut = new ArrayList<String>();

            scriptOut.add("=======================");
            scriptOut.add("For tuple " + expandedHostMap.get(targetNode) + " : " + targetNode);
            scriptOut.add("=======================");

//            String scores = "";
//            String sep = "";
//            for (double score : prResults) {
//                scores += sep;
//                scores += score;
//                sep = " ";
//            }
//            scriptOut.add(scores);

            for (int resultExpandedIndex : relatedInstances.keySet()){
                String expandedTuple = expandedHostMap.get(resultExpandedIndex);
                List<TupleInfo> supportTuple = relatedInstances.get(resultExpandedIndex);
                scriptOut.add("#=====\n" + expandedTuple + " : " + commaJoiner.join(supportTuple));
            }

//            for (int resultExpandedIndex : resultExpandedIndices) {
//                String expandedTuple = expandedHostMap.get(resultExpandedIndex);
//                TupleInfo[] supportTuple = finder.getSupportingTuples(resultExpandedIndex, expandedTuple2Origins, originTupleInfoHostMap);
//                scriptOut.add("#=====\n" + expandedTuple + " : " + commaJoiner.join(supportTuple));
//            }

//            scriptOut.add(lbJoiner.join(resultExpandedTupleStrs));

            FileUtils.writeLines(new File(outputPath), scriptOut, true);
        }
    }
}
