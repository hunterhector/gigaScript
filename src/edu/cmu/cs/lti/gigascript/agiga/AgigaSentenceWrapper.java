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
    List<AgigaToken> tokens;

    public AgigaSentenceWrapper(AgigaSentence sentence) {
        basicDependencies = sentence.getBasicDeps();
        tokens = sentence.getTokens();

        for (AgigaTypedDependency dep : basicDependencies) {
            toGov.put(dep.getDepIdx(), dep.getGovIdx());
        }
    }

    private AgigaToken toGov(AgigaToken token) {
        int govIdx = toGov.get(token.getTokIdx());
        return govIdx == -1 ? null : tokens.get(govIdx);
    }

//    public AgigaToken getHeadWordFromPhrase(List<Integer> indices) {
//        if (indices.size() == 1) {
//            return tokens.get(indices.get(0));
//        } else {
//            int headIndexPosition = 0;
//            AgigaToken tempHeadNode = tokens.get(headIndexPosition);
//            for (int i = 0; i < indices.size(); i++) {
//                AgigaToken token = tokens.get(i);
//                AgigaToken gov = toGov(token);
//
//                if (gov == null){
//                   return token;
//                }else if (gov.equals(token)){
//                    return;
//                }
//
//            }
//
//            return tokens.get(indices.get(headIndexPosition));
//        }
//    }
}
