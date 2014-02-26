package edu.cmu.cs.lti.gigascript.io;

import edu.stanford.nlp.trees.PennTreeReader;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeReader;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphFactory;

import java.io.IOException;
import java.io.StringReader;

/**
 * Created with IntelliJ IDEA.
 * User: Hector, Zhengzhong Liu
 * Date: 2/10/14
 * Time: 9:57 PM
 */
public class ParseTreeReader {

    public static Tree readTreeFromString (String parseStr) {
        TreeReader treeReader = new PennTreeReader(new StringReader(parseStr));

        Tree inputTree = null;
        try{
            inputTree = treeReader.readTree();
        }catch(IOException e){
            e.printStackTrace();
        }
        return inputTree;
    }

    public static SemanticGraph readSemanticGraphFromParseString (String parseStr){
        Tree parseTree = readTreeFromString(parseStr);
        SemanticGraph graph = SemanticGraphFactory.makeFromTree(parseTree);
        return graph;
    }

    public static void main(String args[]) throws Exception {


        String parseStr = "( (S (PP (IN With) (NP (NP (NP (DT the) (NN nation) (POS 's)) (NN attention)) (VP (VBN riveted) (ADVP (RB again)) (PP (IN on) (NP (DT a) (NNP Los) (NNP Angeles) (NN courtroom)))))) (, ,) (NP (DT a) (NN knife) (NN dealer)) (VP (VBD testified) (SBAR (IN that) (S (NP (NNP O.J.) (NNP Simpson)) (VP (VBD bought) (NP (DT a) (JJ 15-inch) (NN knife)) (PP (NP (CD five) (NNS weeks)) (IN before) (NP (NP (NP (DT the) (VBG slashing) (NNS deaths)) (PP (IN of) (NP (PRP$ his) (NN ex-wife)))) (CC and) (NP (PRP$ her) (NN friend)))))))) (. .)))";
        SemanticGraph graph = readSemanticGraphFromParseString(parseStr);

        System.out.println(graph.toFormattedString());
    }
}
