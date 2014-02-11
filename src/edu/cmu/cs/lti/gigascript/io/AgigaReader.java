package edu.cmu.cs.lti.gigascript.io;

import edu.jhu.agiga.AgigaDocument;
import edu.jhu.agiga.AgigaPrefs;
import edu.jhu.agiga.AgigaSentence;
import edu.jhu.agiga.StreamingDocumentReader;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphFactory;

/**
 * Created with IntelliJ IDEA.
 * User: Hector, Zhengzhong Liu
 * Date: 2/11/14
 * Time: 2:25 PM
 */
public class AgigaReader {

    public static void readSingleAgigaZip(String filePath) {
        StreamingDocumentReader reader = new StreamingDocumentReader(filePath, new AgigaPrefs());
        for (AgigaDocument doc : reader) {
            for (AgigaSentence sent : doc.getSents()) {
                SemanticGraph graph = SemanticGraphFactory.makeFromTree(sent.getStanfordContituencyTree());
                System.out.print(graph.toFormattedString());
            }
        }
    }


    public static void main(String[] argv){
        String path = "/Users/hector/Downloads/nyt_eng_199407.xml.gz";
        readSingleAgigaZip(path);
    }

}
