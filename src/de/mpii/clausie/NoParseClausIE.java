package de.mpii.clausie;

import edu.jhu.agiga.*;
import edu.stanford.nlp.pipeline.ParserAnnotatorUtils;
import edu.stanford.nlp.trees.Tree;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created with IntelliJ IDEA.
 * User: Hector, Zhengzhong Liu
 * Date: 2/11/14
 * Time: 2:47 PM
 */
public class NoParseClausIE extends ClausIE {
    public NoParseClausIE(Tree depTree) {
        this.depTree = depTree;
        this.semanticGraph = ParserAnnotatorUtils
                .generateUncollapsedDependencies(depTree);
    }

    public static void main(String[] argv) throws IOException {
        String path = "/Users/hector/Downloads/nyt_eng_199407.xml.gz";
        StreamingDocumentReader reader = new StreamingDocumentReader(path, new AgigaPrefs());

        OutputStream out = System.out;

        PrintStream dout = new PrintStream(out);


        for (AgigaDocument doc : reader) {
            for (AgigaSentence sent : doc.getSents()) {
                NoParseClausIE npClauseIe = new NoParseClausIE(sent.getStanfordContituencyTree());
                dout.println(npClauseIe.getSemanticGraph().toFormattedString());

                for (AgigaToken token : sent.getTokens()) {
                    dout.print(token.getWord());
                    dout.print(" ");
                }

                dout.println();

                npClauseIe.detectClauses();
                for (Clause clause : npClauseIe.getClauses()) {
                    dout.print("#   - ");
                    dout.print(clause.toString());
                    dout.println();
                }

                npClauseIe.generatePropositions();
                for (Proposition p : npClauseIe.getPropositions()) {
                    for (String c : p.constituents) {
                        dout.print("\t\"");
                        dout.print(c);
                        dout.print("\"");
                    }
                    dout.println();
                }

            }
        }


    }
}