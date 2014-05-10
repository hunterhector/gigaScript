package edu.cmu.cs.lti.gigascript.agiga;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import edu.jhu.agiga.AgigaSentence;
import edu.jhu.agiga.AgigaToken;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zhengzhongliu on 2/26/14.
 */
public class AgigaUtil {
    static Joiner spaceJoiner = Joiner.on(" ");

    private static Set<String> relationBlackList = new HashSet<String>(Arrays.asList("can","cannot",
            "could","couldn't", "dare", "may", "might", "must", "need",
            "ought", "shall", "should", "shouldn't","will", "would"));

    private static Set<String> blackListPos = new HashSet<String>(Arrays.asList("MD"));

    public static String getSentenceString(AgigaSentence sent){
        return spaceJoiner.join(Lists.transform(sent.getTokens(), new Function<AgigaToken, String>() {
            @Override
            public String apply(final AgigaToken token) {
                return token.getWord();
            }
        }));
    }

    public static  String getLemma(AgigaToken token){
        return token.getLemma();
    }

    public static String getLemmaForPhrase(AgigaSentence sent, List<Integer> indices){
        StringBuffer builder = new StringBuffer();
        List<AgigaToken> tokens = sent.getTokens();

        //assuming the head first format
        for (int i = 1; i< indices.size();i++){
            AgigaToken token = tokens.get(indices.get(i));
            builder.append(token.getLemma());
            builder.append(" ");
        }
        return builder.toString().trim();
    }

    public static String getShortenLemmaForPhrase(AgigaSentence sent, List<Integer> indices){
        StringBuffer builder = new StringBuffer();
        List<AgigaToken> tokens = sent.getTokens();

        //assuming the head first format
        for (int i = 1; i< indices.size();i++){
            AgigaToken token = tokens.get(indices.get(i));
            if (!blackListPos.contains(token.getPosTag())) {
                builder.append(token.getLemma());
                builder.append(" ");
            }
        }
        return builder.toString().trim();
    }
}
