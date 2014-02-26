package edu.cmu.cs.lti.gigascript.agiga;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import edu.jhu.agiga.AgigaSentence;
import edu.jhu.agiga.AgigaToken;

import java.util.List;

/**
 * Created by zhengzhongliu on 2/26/14.
 */
public class AgigaUtil {
    static Joiner spaceJoiner = Joiner.on(" ");

    public static AgigaToken getCorrespondingToken(AgigaSentence sentence, int index){
        return sentence.getTokens().get(index);
    }

    public static String getSentenceString(AgigaSentence sent){
        return spaceJoiner.join(Lists.transform(sent.getTokens(), new Function<AgigaToken, String>() {
            @Override
            public String apply(final AgigaToken token) {
                return token.getWord();
            }
        }));
    }

    public static  String getLemma(AgigaSentence sent, int index){
        return getCorrespondingToken(sent,index).getLemma();
    }

    public static String getLemmaForPhrase(AgigaSentence sent, List<Integer> indices){
        StringBuffer builder = new StringBuffer();
        List<AgigaToken> tokens = sent.getTokens();

        //assuming the head first format
        for (int i = 1; i< indices.size();i++){
            AgigaToken token = tokens.get(i);
            builder.append(token.getLemma());
            builder.append(" ");
        }
        return builder.toString().trim();
    }



}
