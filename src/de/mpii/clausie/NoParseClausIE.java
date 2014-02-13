package de.mpii.clausie;

import edu.cmu.cs.lti.gigascript.io.IOUtils;
import edu.jhu.agiga.*;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.ParserAnnotatorUtils;
import edu.stanford.nlp.trees.Tree;

import java.io.*;

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

        for (IndexedWord word : semanticGraph.vertexListSorted()) {
            System.out.println(word);
        }
    }

    public static void main(String[] argv) throws IOException {
        String path = System.getProperty("user.home") + "/Downloads/nyt_eng_199407.xml.gz";
        StreamingDocumentReader reader = new StreamingDocumentReader(path, new AgigaPrefs());

        OutputStream out = System.out;

        FileOutputStream errorOutStream = new FileOutputStream(new File("error.txt"));

        PrintStream dout = new PrintStream(out);

        PrintStream eout = new PrintStream(errorOutStream);

        NoParseClausIE npClauseIe = new NoParseClausIE();

        npClauseIe.options.print(out);

        long startTime = System.currentTimeMillis();

        for (AgigaDocument doc : reader) {
            for (AgigaSentence sent : doc.getSents()) {

                try {
                    npClauseIe.readParse(sent.getStanfordContituencyTree());
                dout.println(npClauseIe.getSemanticGraph().toFormattedString());

                for (AgigaToken token : sent.getTokens()) {
                    dout.print(token.getWord());
                    dout.print(" ");
                }
                dout.println();


                    npClauseIe.detectClauses();
                for (Clause clause : npClauseIe.getClauses()) {
                    for (int index = 0; index < clause.constituents.size(); index++) {
                        Constituent constituent = clause.constituents.get(index);
                        dout.println(constituent.getType()+"--"+constituent.rootString()+"--"+(constituent instanceof IndexedConstituent));
                    }

                    dout.print("#   - ");
                    dout.print(clause.toString());
                    dout.println();
                }

                    npClauseIe.generatePropositions();
//                for (Proposition p : npClauseIe.getPropositions()) {
//                    for (String c : p.constituents) {
//                        dout.print("\t\"");
//                        dout.print(c);
//                        dout.print("\"");
//                    }
//                    dout.println();
//                }
                } catch (NullPointerException e) {
                    eout.println("Giving up on Null Pointer");
                    IOUtils.printSentence(sent, eout);
                } catch (StackOverflowError e) {
                    eout.println("Giving up on StackoverFlow");
                    IOUtils.printSentence(sent, eout);
                }
            }

            System.out.print("\r" + reader.getNumDocs());
        }

        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println("\nOverall processing time takes " + totalTime / 6e4 + " minutes");
    }


}