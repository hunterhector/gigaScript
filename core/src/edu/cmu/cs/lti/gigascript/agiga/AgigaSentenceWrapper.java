package edu.cmu.cs.lti.gigascript.agiga;

import edu.jhu.agiga.AgigaSentence;
import edu.jhu.agiga.AgigaToken;
import edu.jhu.agiga.AgigaTypedDependency;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Hector, Zhengzhong Liu
 * Date: 2/28/14
 * Time: 5:46 PM
 */
public class AgigaSentenceWrapper {
    List<AgigaTypedDependency> basicDependencies;
    Map<Integer, Integer> toGov = new HashMap<Integer, Integer>();
    Map<Integer, Integer> toDep = new HashMap<Integer, Integer>();
    List<AgigaToken> tokens;

    public AgigaSentenceWrapper(AgigaSentence sentence) {
        basicDependencies = sentence.getBasicDeps();
        tokens = sentence.getTokens();

        for (AgigaTypedDependency dep : basicDependencies) {
            toGov.put(dep.getDepIdx(), dep.getGovIdx());
            toDep.put(dep.getGovIdx(), dep.getDepIdx());
        }
    }

    private AgigaToken toGov(AgigaToken token) {
        int govIdx = toGov.get(token.getTokIdx());
        return govIdx == -1 ? null : tokens.get(govIdx);
    }

    private AgigaToken toDep(AgigaToken token) {
        int depIdx = toDep.get(token.getTokIdx());
        return depIdx == -1 ? null : tokens.get(depIdx);
    }

    public List<AgigaToken> getTokens() {
        return tokens;
    }

    public int getHeadWordIndex(List<Integer> indices) {
//        if (indices.size() < 2) {
//            return indices.get(0);
//        } else {
//            int headPosition = 0;
//            int headNode = indices.get(headPosition);
//
//            while (headPosition < indices.size()-1) {
//                if (toDep.containsKey(headNode)) {
//                    boolean hasParent = false;
//                    for (int i = 1; i < indices.size(); i++) {
//                        int node = indices.get(i);
//                        if (toDep.get(headNode) == node) {
//                            //update head position
//                            headPosition = i;
//                            headNode = toDep.get(headPosition);
//                            hasParent = true;
//                            break;
//                        }
//                    }
//                    if (!hasParent){
//                        return headNode;
//                    }
//                } else {
//                    return headNode;
//                }
//            }
//            return headNode;
//        }

        int firstIndex = indices.get(0);
//        AgigaToken firstToken = tokens.get(firstIndex);

        for (int i =0;i<indices.size();i++) {
            int index = indices.get(i);
            String pos = tokens.get(index).getPosTag();

            if (!(pos.equals("PP") || pos.equals("TO") || pos.equals("IN")) || pos.equals("DT")) {
                return index;
            }
        }

        return firstIndex;
    }

    public String getSentenceStr() {
        String str = "";
        for (AgigaToken token : tokens) {
            str += token.getWord();
            str += " ";
        }
        return str;
    }

    public String getSentenceLemmaStr() {
        String str = "";
        for (AgigaToken token : tokens) {
            str += token.getLemma();
            str += " ";
        }
        return str;
    }
}
