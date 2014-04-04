package edu.cmu.cs.lti.gigascript.agiga;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import edu.cmu.cs.lti.gigascript.model.AgigaArgument;
import edu.cmu.cs.lti.gigascript.model.SemanticType;
import edu.jhu.agiga.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Hector, Zhengzhong Liu
 * Date: 2/26/14
 * Time: 11:16 PM
 */
public class AgigaDocumentWrapper {
    AgigaDocument document;
    Table<Integer, Integer, SemanticType> typeMapping;
    List<Set<Pair<Integer, Integer>>> corefChains;
    String documentText;

    public AgigaDocumentWrapper(AgigaDocument document) {
        this.document = document;
        typeMapping = HashBasedTable.create();
        corefChains = new ArrayList<Set<Pair<Integer, Integer>>>();

        List<AgigaCoref> corefs = document.getCorefs();
        SemanticType type = new SemanticType();
        for (AgigaCoref coref : corefs) {
            Set<Pair<Integer, Integer>> corefChain = new HashSet<Pair<Integer, Integer>>();
            for (AgigaMention mention : coref.getMentions()) {
                int sentIndex = mention.getSentenceIdx();

                for (int i = mention.getStartTokenIdx(); i < mention.getEndTokenIdx(); i++) {
                    //This procedure populate the entity type among coreference mentions, which might hurt the performance sometime
                    //currently disabled
//                    typeMapping.put(sentIndex, i, type);
//                    AgigaToken token = document.getSents().get(sentIndex).getTokens().get(i);
//                    if (token.getNerTag() != null && !token.getNerTag().equals("O")) {
//                        type.setType(token.getNerTag());
//                    }
                    corefChain.add(Pair.of(sentIndex, i));
                }
            }
            corefChains.add(corefChain);
        }
    }

    public String getText() {
        if (documentText == null) {
            documentText = "";
            //build document text here
            for (AgigaSentence sent : document.getSents()) {
                AgigaSentenceWrapper sWrapper = new AgigaSentenceWrapper(sent);
                documentText += sWrapper.getSentenceStr() + "\n";
            }
        }
        return documentText;
    }

    public boolean sameArgument(AgigaArgument argument1, AgigaArgument argument2) {
        if (argument1.equals(argument2)) return true;
        else {
            for (Set<Pair<Integer, Integer>> corefChain : corefChains) {
                if (corefChain.contains(argument1.getIndexingPair()) && corefChain.contains(argument2.getIndexingPair()))
                    return true;
            }
        }
        return false;
    }

    public String getSemanticType(AgigaSentence sentence, AgigaToken token) {
        int index = token.getTokIdx();

        // trust the token's own Named Entity Tag
        if (!token.getNerTag().equals("O") && !token.getNerTag().equals("")) {
            return token.getNerTag();
        }

        // if the previous one didn't give anything, use the coreferenced type
        SemanticType type = typeMapping.get(sentence.getSentIdx(), index);
        return type == null ? null : type.getType();
    }

    /**
     * Return the semantic type and the index of the word that is used to get this type
     *
     * @param sentence
     * @param indices
     * @return
     */
    public String getArgumentSemanticType(AgigaSentence sentence, AgigaToken headword, List<Integer> indices) {
        if (headword == null) {
            return null;
        } else {
            String headWordType = getSemanticType(sentence, headword);

            //phrase type is too noisy
            return headWordType;
//            if (headWordType != null) {
//                return headWordType;
//            } else {
//                String phraseSemanticType = null;
//
//                for (Integer i : indices) {
//                    String newType = getSemanticType(sentence, sentence.getTokens().get(i));
//                    if (newType != null) {
//                        phraseSemanticType = newType;
//                    }
//                }
//                return phraseSemanticType;
//            }
        }
    }


}