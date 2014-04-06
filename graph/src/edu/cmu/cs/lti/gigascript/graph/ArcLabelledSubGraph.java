package edu.cmu.cs.lti.gigascript.graph;

import it.unimi.dsi.webgraph.labelling.ArcLabelledImmutableGraph;
import it.unimi.dsi.webgraph.labelling.ArcLabelledNodeIterator;
import it.unimi.dsi.webgraph.labelling.Label;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 4/4/14
 * Time: 2:37 PM
 */
public class ArcLabelledSubGraph {
    private ArcLabelledImmutableGraph superGraph;
    private Set<Integer> nodeSet;
    private int hop;
    private boolean removeNonPositiveWeightedArc;

    int supergraphNumNodes;
    int[] supergraphNode;
    int[] initialSubGraphNode;
    List<Triple<Integer,Integer,Float>> initialArcList;
    int[] subgraphNode;
    List<Triple<Integer, Integer, Float>> subgraphArcList;
    int subgraphSize;


    public ArcLabelledSubGraph(ArcLabelledImmutableGraph superGraph,Set<Integer> nodeSet, int hop, boolean removeNonPositiveWeightedArc){
        this.superGraph = superGraph;
        this.nodeSet = nodeSet;
        this.hop = hop;
        this.removeNonPositiveWeightedArc = removeNonPositiveWeightedArc;

        supergraphNumNodes = superGraph.numNodes();
        subgraphNode = ArrayUtils.toPrimitive(nodeSet.toArray(new Integer[nodeSet.size()]));
        Arrays.sort(subgraphNode);
        initialSubGraphNode = subgraphNode;
        updateSuperGraphRecord(initialSubGraphNode);

        initialArcList = buildSelfConnectedArcList(initialSubGraphNode);

        Pair<int[], List<Triple<Integer, Integer, Float>>> res = build(initialSubGraphNode, initialArcList, hop);
        subgraphNode = res.getLeft();
        subgraphArcList = res.getRight();

        subgraphSize = subgraphNode.length;
        System.err.println(String.format("Subgraph size: %d;  Supergraph size: %d.",subgraphSize,supergraphNumNodes));

        if (subgraphSize > 0 && subgraphNode[subgraphSize - 1] >= supergraphNumNodes)
            throw new IllegalArgumentException("Subnode index out of bounds (larger than supergraph number of nodes): "+subgraphNode[subgraphSize - 1]);
    }

    /**
     * Recursively expand the sub graph to reach the N-hop neighbour.
     *
     * @param currentSubGraphNode The current sub graph nodes array
     * @param currentArcList The current arc list
     * @param n number of hops desired to reach from the current graph
     * @return  A tuple containing the expanded sub graph nodes and arc list
     */
    private Pair<int[], List<Triple<Integer,Integer,Float>>> build(int[] currentSubGraphNode,List<Triple<Integer,Integer,Float>> currentArcList,int n) {
        if ( n == 0){
            return Pair.of(currentSubGraphNode,currentArcList);
        }else{
            ArcLabelledNodeIterator iter = superGraph.nodeIterator();
            iter.next();

            int lastVisited = 0;
            Set<Integer> newNodes = new HashSet<Integer>();
            Map<Pair<Integer,Integer>,Float> newArcs = new HashMap<Pair<Integer,Integer>,Float>();

            for (int currIdx : currentSubGraphNode){
                int step = currIdx - lastVisited;
                iter.skip(step);
                lastVisited = currIdx;
                int outdegree = iter.outdegree();
                int[] succs = iter.successorArray();
                Label[] labels = iter.labelArray();

                for (int pos = 0; pos< outdegree; pos++){
                    int succIdx = succs[pos];
                    float weight = labels[pos].getFloat();

                    if (supergraphNode[succIdx] == -1){
                        newNodes.add(succIdx);
                        newArcs.put(Pair.of(currIdx, succIdx), weight);
                    }
                }
            }

            int[] newSubGraphNode = updateGraphNodes(currentSubGraphNode, newNodes);
            List<Triple<Integer, Integer, Float>> expandArcList = expandArcList(currentArcList, newArcs);

            return Pair.of(newSubGraphNode,expandArcList);
        }
    }

    private List<Triple<Integer,Integer,Float>> expandArcList(List<Triple<Integer,Integer,Float>> oldArcList, Map<Pair<Integer,Integer>,Float> newArcMap){
        for (Map.Entry<Pair<Integer,Integer>,Float> entry : newArcMap.entrySet()){
            int currIdx = entry.getKey().getLeft();
            int succIdx = entry.getKey().getRight();
            float weight = entry.getValue();

            if (supergraphNode[succIdx] >= 0){
                if (!removeNonPositiveWeightedArc || weight >0.0){
                    oldArcList.add(Triple.of(supergraphNode[currIdx], supergraphNode[succIdx],weight));
                }
            }
        }
        return oldArcList;
    }

    private int[] updateGraphNodes(int[] oldSubGraphNode, Set<Integer> newNodes){
        List<Integer> newSubGraphNode = Arrays.asList(ArrayUtils.toObject(oldSubGraphNode));
        newSubGraphNode.addAll(newNodes);
        int[] newSubGraphNodePrimitive = ArrayUtils.toPrimitive(newSubGraphNode.toArray(new Integer[newSubGraphNode.size()]));
        updateSuperGraphRecord(newSubGraphNodePrimitive);
        return newSubGraphNodePrimitive;
    }


    private void updateSuperGraphRecord(int[] sortedNodes) {
        for (int i =0 ; i < sortedNodes.length ; i++){
            supergraphNode[sortedNodes[i]] = i;
        }
    }

    public int toSupergraphNode(int x) {
        if (x < 0 || x>= subgraphSize) throw new IllegalArgumentException();
        return subgraphNode[x];
    }

    /**
     * Return the corresponding subgraph index of a supergraph index
     * @param x The supergraph index
     * @return  The corresponding subgraph index
     */
    public int fromSupergraphNode(int x) {
        if (x < 0 || x>= supergraphNumNodes) throw new IllegalArgumentException();
        return supergraphNode[x];
    }

    /**
     * Return the subgraph as a arc list, each arc is defined by 2 Integer and a weight
     * @return A ListBuffer that represent the graph
     */
    public List<Triple<Integer, Integer, Float>> getArcList() {
            return subgraphArcList;
    }

    private List<Triple<Integer,Integer,Float>> buildSelfConnectedArcList(int[] graphNodes) {
        System.err.println("Building initial Arc List.");
        List<Triple<Integer,Integer,Float>> tmpArcList = new ArrayList<Triple<Integer, Integer, Float>>();
        ArcLabelledNodeIterator iter = superGraph.nodeIterator();
        iter.next();

        int lastVisited = 0;

        for (int currIdx : graphNodes){
            int step = currIdx - lastVisited;
            iter.skip(step);
            lastVisited = currIdx;

            int outdegree = iter.outdegree();
            int[] succs = iter.successorArray();
            Label[] labels = iter.labelArray();

            for (int pos = 0 ; pos < outdegree ; pos++){
                int succIdx = succs[pos];
                float weight = labels[pos].getFloat();

                if ( supergraphNode[succIdx] >= 0){
                    if (!removeNonPositiveWeightedArc || weight > 0.0){
                        Triple<Integer,Integer,Float> t = Triple.of(supergraphNode[currIdx], supergraphNode[succIdx],weight);
                        tmpArcList.add(t);
                    }
                }
            }
        }

        return tmpArcList;
    }
}
