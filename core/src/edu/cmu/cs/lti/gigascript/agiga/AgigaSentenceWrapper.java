package edu.cmu.cs.lti.gigascript.agiga;

import edu.jhu.agiga.AgigaSentence;
import edu.jhu.agiga.AgigaToken;
import edu.jhu.agiga.AgigaTypedDependency;

import java.io.PrintStream;
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

    public int getHeadWordIndex(List<Integer> indices) {
        int firstIndex = indices.get(0);
        AgigaToken firstToken = tokens.get(firstIndex);

        String pos = firstToken.getPosTag();

        if (pos.equals("PP") || pos.equals("TO") || pos.equals("IN")){
            if (toDep.containsKey(firstIndex)){
                return toDep.get(firstIndex);
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

    public String getSentenceLemmaStr(){
        String str = "";
        for (AgigaToken token : tokens) {
            str += token.getLemma();
            str += " ";
        }
        return str;
    }
}
