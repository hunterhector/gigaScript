package edu.cmu.cs.lti.gigascript.demo;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 4/15/14
 * Time: 8:32 PM
 */
public class WordNetDemo {


    public static String getMatchedSemanticTypeBfs(IDictionary dict, ISynset synset, Set<String> targeSemanticTypes) {
        LinkedList<ISynset> hyperNyms = getHyperNyms(dict, synset);

        while (!hyperNyms.isEmpty()){
            ISynset hyperNym = hyperNyms.poll();
            for (IWord iword : hyperNym.getWords()) {
                String hyperNymLemma = iword.getLemma();
                if (targeSemanticTypes.contains(hyperNymLemma)) {
                    return hyperNymLemma;
                }
            }
            hyperNyms.addAll(getHyperNyms(dict,hyperNym));
        }

        return "-";
    }

    public static LinkedList<ISynset> getHyperNyms(IDictionary dict, ISynset synset) {
        LinkedList<ISynset> hyperNyms = new LinkedList<ISynset>();
        for (ISynsetID sid : synset.getRelatedSynsets(Pointer.HYPERNYM)) {
            hyperNyms.add(dict.getSynset(sid));
        }
        return hyperNyms;
    }

    public static void main(String[] args) throws IOException {
        File targetSemanticTypes = new File("semantic_types");
        Set<String> semanticTypes = new HashSet<String>();
        for (String semanticType : FileUtils.readLines(targetSemanticTypes)){
            semanticTypes.add(semanticType.trim());
        }

        URL url = new URL("file", null, "/Users/zhengzhongliu/tools/wnDict/");

        //construct the Dictionary object and open it
        IDictionary dict = new Dictionary(url);
        dict.open();

        IIndexWord idxWord = dict.getIndexWord("talk", POS.NOUN);

        for (IWordID wordId : idxWord.getWordIDs()) {
            IWord word = dict.getWord(wordId);
            System.out.println("For "+word);
            ISynset synset = word.getSynset();
            System.out.println(getMatchedSemanticTypeBfs(dict, synset, semanticTypes));
        }
    }
}