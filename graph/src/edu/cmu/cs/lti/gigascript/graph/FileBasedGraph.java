package edu.cmu.cs.lti.gigascript.graph;

import es.yrbcn.graph.weighted.FixedWidthFloatLabel;
import es.yrbcn.graph.weighted.WeightedArc;
import it.unimi.dsi.webgraph.AbstractLazyIntIterator;
import it.unimi.dsi.webgraph.labelling.ArcLabelledImmutableGraph;
import it.unimi.dsi.webgraph.labelling.ArcLabelledNodeIterator;
import it.unimi.dsi.webgraph.labelling.Label;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 *  A graph with file based structure, assuming each node is stored in one file, which is
 *  an adjacent list in the following format: "this_node<TAB>successor<TAB>weight".
 *
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 7/19/14
 * Time: 4:40 PM
 */
public class FileBasedGraph extends ArcLabelledImmutableGraph{

    /**
     * The prototype of the labels used by this class.
     */
    final private FixedWidthFloatLabel prototype;

    /**
     * The number of nodes, computed at construction time by triple inspection.
     */
    final private int numNodes;

    private File graphDir;

    private int currentNode = -1;

    private List<WeightedArc> outArcList;

    public FileBasedGraph(File graphDir, int numNodes) throws IOException {
        this.graphDir = graphDir;
        this.prototype = new FixedWidthFloatLabel("FOO");
        this.numNodes = numNodes;


        if (!graphDir.exists() && !graphDir.isDirectory()){
            throw new IllegalArgumentException("Provided directory does not exist "+graphDir.getCanonicalPath());
        }
    }

    private void toNode(int node) {
        if (currentNode == node){
            return;
        }

        File arcFile = new File(graphDir,Integer.toString(node));
        if (!arcFile.exists()){
            throw new IllegalArgumentException(String.format("Cannot access node %d, please check graph dir",node));
        }

        outArcList = new LinkedList<WeightedArc>();

        try {
            for (String line : FileUtils.readLines(arcFile)){
                if (!line.trim().isEmpty()) {
                    WeightedArc arc = new WeightedArc(line);
                    outArcList.add(arc);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final class ArcIterator extends AbstractLazyIntIterator implements ArcLabelledNodeIterator.LabelledArcIterator {
        private final FixedWidthFloatLabel label;
        private final int from;
        private int pos = -1; //position of the pointer in the arc list

        private ArcIterator(FixedWidthFloatLabel label, int from) {
            this.label = label;
            this.from = from;
            toNode(from);
        }

        public Label label() {
            label.value = outArcList.get(pos).weight;
            return label;
        }

        public int nextInt() {
            pos ++;
            if (pos >= outArcList.size()) return -1;
            return (int) (outArcList.get(pos).dest);
        }
    }

    @Override
    public ArcLabelledImmutableGraph copy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ArcLabelledNodeIterator.LabelledArcIterator successors(int x) {
        return new ArcIterator(prototype.copy(),x);
    }

    @Override
    public int outdegree(int x) {
        toNode(x);
        return outArcList.size();
    }

    @Override
    public boolean randomAccess() {
        return true;
    }

    @Override
    public int numNodes() {
        return numNodes;
    }

    @Override
    public Label prototype() {
        return prototype;
    }

}