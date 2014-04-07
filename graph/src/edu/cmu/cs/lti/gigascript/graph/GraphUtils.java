package edu.cmu.cs.lti.gigascript.graph;

import es.yrbcn.graph.weighted.WeightedArc;
import es.yrbcn.graph.weighted.WeightedBVGraph;
import gnu.trove.map.hash.TIntObjectHashMap;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.labelling.ArcLabelledImmutableGraph;
import it.unimi.dsi.webgraph.labelling.BitStreamArcLabelledImmutableGraph;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Triple;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    public static String[] ToTuple(int[] ids, int offset, TIntObjectHashMap<String> fromIdMap){
        String[] tuples = new String[ids.length];

        for (int i = 0 ; i< ids.length;i ++){
            int id = ids[i];
            tuples[i] = fromIdMap.get(id+offset);
        }

        return tuples;
    }
}
