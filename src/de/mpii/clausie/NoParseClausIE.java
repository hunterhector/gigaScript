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

    public NoParseClausIE() {
        this.options = new Options();
    }

    public void readParse(Tree depTree) {
        clear();
        this.depTree = depTree;
        this.semanticGraph = ParserAnnotatorUtils
                .generateUncollapsedDependencies(depTree);
    }

    public static void main(String[] argv) throws IOException {
        String path = "/Users/hector/Downloads/nyt_eng_199407.xml.gz";
        StreamingDocumentReader reader = new StreamingDocumentReader(path, new AgigaPrefs());

        OutputStream out = System.out;

        PrintStream dout = new PrintStream(out);

        NoParseClausIE npClauseIe = new NoParseClausIE();

        npClauseIe.options.print(out);

        long startTime = System.currentTimeMillis();


//        System.out.println("Parsing XML");
//        for (AgigaDocument doc : reader) {
//
//        }
//        System.out.println("Number of docs: " + reader.getNumDocs());
//        long readingTime = System.currentTimeMillis();
//        System.out.println("Reading one gzip takes " + (readingTime - startTime) / 6e4 + " minutes");


        int i = 0;
        for (AgigaDocument doc : reader) {
            i++;
            System.out.println(i);

            for (AgigaSentence sent : doc.getSents()) {

                try {
                    npClauseIe.readParse(sent.getStanfordContituencyTree());
//                dout.println(npClauseIe.getSemanticGraph().toFormattedString());
//
//                for (AgigaToken token : sent.getTokens()) {
//                    dout.print(token.getWord());
//                    dout.print(" ");
//                }
//                dout.println();


                    npClauseIe.detectClauses();
//                for (Clause clause : npClauseIe.getClauses()) {
//                    dout.print("#   - ");
//                    dout.print(clause.toString());
//                    dout.println();
//                }

                    npClauseIe.generatePropositions();
//                for (Proposition p : npClauseIe.getPropositions()) {
//                    for (String c : p.constituents) {
//                        dout.print("\t\"");
//                        dout.print(c);
//                        dout.print("\"");
//                    }
//                    dout.println();
//                }
                } catch (Exception e) {
                    for (AgigaToken token : sent.getTokens()) {
                        dout.print(token.getWord());
                        dout.print(" ");
                    }
                    dout.println();
                    e.printStackTrace();
                }   catch (StackOverflowError e){
                    for (AgigaToken token : sent.getTokens()) {
                        dout.print(token.getWord());
                        dout.print(" ");
                    }
                    dout.println();
                    System.err.println("Giving up on StackoverFlow");
                }
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println("Overall processing time takes " + totalTime / 6e4 + " minutes");
    }
}