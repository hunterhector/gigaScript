package edu.cmu.cs.lti.gigascript.agiga;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import edu.cmu.cs.lti.gigascript.edu.cmu.cs.lti.lexical.SemanticType;
import edu.jhu.agiga.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zhengzhongliu on 2/26/14.
 */
public class AgigaWrapper {
    Table<Integer, Integer, SemanticType> typeMapping;
    List<Set<Pair<Integer, Integer>>> corefChains;

    public AgigaWrapper(AgigaDocument document) {
        typeMapping = HashBasedTable.create();
        corefChains = new ArrayList<Set<Pair<Integer, Integer>>>();

        List<AgigaCoref> corefs = document.getCorefs();
        SemanticType type = new SemanticType();
        for (AgigaCoref coref : corefs) {
            Set<Pair<Integer,Integer>> corefChain = new HashSet<Pair<Integer, Integer>>();
            for (AgigaMention mention : coref.getMentions()) {
                int sentIndex = mention.getSentenceIdx();
                for (int i = mention.getStartTokenIdx(); i < mention.getEndTokenIdx(); i++) {
                    typeMapping.put(sentIndex, i, type);
                    AgigaToken token = document.getSents().get(sentIndex).getTokens().get(i);
                    if (token.getNerTag() != null && !token.getNerTag().equals("")) {
                        type.setType(token.getNerTag());
                    }
                    corefChain.add(Pair.of(sentIndex,i));
                }
            }
            corefChains.add(corefChain);
        }
    }

    public boolean sameArgument(Pair<Integer, Integer> sentTokenIndex1, Pair<Integer, Integer> sentTokenIndex2){
        if (sentTokenIndex1.equals(sentTokenIndex2)) return true;
        else{
            for (Set<Pair<Integer, Integer>> corefChain : corefChains){
               if (corefChain.contains(sentTokenIndex1) && corefChain.contains(sentTokenIndex2))
                   return true;
            }
        }
        return false;
    }

    public String getSemanticType(AgigaSentence sentence, int index) {
        SemanticType type = typeMapping.get(sentence.getSentIdx(), index);
        return type == null ? null : type.getType();
    }

    public Pair<String, Integer> getArgumentSemanticType(AgigaSentence sentence, List<Integer> indices) {
        if (indices.isEmpty()) {
            return null;
        } else if (indices.get(0) == -1) {
            return null;
        } else {
            String headWordType = getSemanticType(sentence, indices.get(0));

            if (headWordType != null) {
                return Pair.of(headWordType, indices.get(0));
            } else {
                String phraseSemanticType = null;
                int semanticWordIndex = -1;

                for (Integer i : indices) {
                    String newType = getSemanticType(sentence, i);
                    if (newType != null) {
                        phraseSemanticType = newType;
                        semanticWordIndex = i;
                    }
                }

                return Pair.of(phraseSemanticType, semanticWordIndex);
            }
        }
    }

}