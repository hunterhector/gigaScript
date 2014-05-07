package edu.cmu.cs.lti.gigascript.util;

import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.Pointer;

import java.util.LinkedList;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 4/15/14
 * Time: 9:56 PM
 */
public class WordNetUtils {
    public static String getMatchedSemanticTypeBfs(IDictionary dict, ISynset synset, Set<String> targeSemanticTypes) {
        LinkedList<ISynset> hyperNyms = getHyperNyms(dict, synset,true);

        while (!hyperNyms.isEmpty()){
            ISynset hyperNym = hyperNyms.poll();
            for (IWord iword : hyperNym.getWords()) {
                String hyperNymLemma = iword.getLemma();
                if (targeSemanticTypes.contains(hyperNymLemma)) {
                    return hyperNymLemma;
                }
            }
            hyperNyms.addAll(getHyperNyms(dict,hyperNym,false));
        }

        return "-";
    }

    public static LinkedList<ISynset> getHyperNyms(IDictionary dict, ISynset synset, boolean includeSelf) {
        LinkedList<ISynset> hyperNyms = new LinkedList<ISynset>();
        if (includeSelf){
            hyperNyms.add(synset);
        }

        for (ISynsetID sid : synset.getRelatedSynsets(Pointer.HYPERNYM)) {
            hyperNyms.add(dict.getSynset(sid));
        }
        return hyperNyms;
    }
}
