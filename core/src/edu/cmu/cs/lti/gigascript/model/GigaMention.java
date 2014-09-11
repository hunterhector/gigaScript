package edu.cmu.cs.lti.gigascript.model;

import edu.jhu.agiga.AgigaToken;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 2/26/14
 * Time: 1:16 AM
 */
public class GigaMention {
    private final int keywordTokenIndex;
    private final int sentenceIndex;

    private String headWordLemma;
    private String entityType;

    private final List<AgigaToken> tokens;

    private boolean isRepresantative = false;

    public GigaMention(int sentenceIndex, int keywordTokenIndex, List<AgigaToken> tokens, String entityType){
        this.keywordTokenIndex = keywordTokenIndex;
        this.sentenceIndex = sentenceIndex;

        this.tokens = new ArrayList<AgigaToken>();
        for (AgigaToken token : tokens){
            this.tokens.add(token);
        }
        this.entityType = entityType;
    }

    public GigaMention(int sentenceIndex, int keywordTokenIndex, AgigaToken token, String entityType){
        this.tokens = new ArrayList<AgigaToken>();
            tokens.add(token);

        this.keywordTokenIndex = keywordTokenIndex;
        this.sentenceIndex = sentenceIndex;
        this.entityType = entityType;
    }

    public List<AgigaToken> getTokens(){
        return tokens;
    }

    public int getSentenceIndex() {
        return sentenceIndex;
    }

    public int getKeywordTokenIndex() { return keywordTokenIndex; }

    public Pair<Integer, Integer> getIndexingPair(){
       return Pair.of(sentenceIndex,keywordTokenIndex);
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof GigaMention))
            return false;

        GigaMention rhs = (GigaMention) obj;

        return new EqualsBuilder().append(keywordTokenIndex, rhs.keywordTokenIndex).append(sentenceIndex, rhs.sentenceIndex).isEquals();
    }

    public int hashCode(){
        return new HashCodeBuilder(17,31).append(keywordTokenIndex).append(sentenceIndex).toHashCode();
    }


    public String toString(){
        return String.format("[%s]@(%d,%d)",tokensToString(),sentenceIndex,keywordTokenIndex);
    }

    public String tokensToString(){
        StringBuilder str = new StringBuilder();
        String splitter = "";
        for (AgigaToken token : tokens){
            str.append(splitter);
            str.append(token.getWord());
            splitter = " ";
        }
        return str.toString();
    }

    public List<String> getTokenStrs(){
        List<String> tokenStrs = new ArrayList<String>();
        for (AgigaToken token : tokens){
            tokenStrs.add(token.getWord());
        }
        return tokenStrs;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getHeadWordLemma() {
        return headWordLemma;
    }

    public void setHeadWordLemma(String headWordLemma) {
        this.headWordLemma = headWordLemma;
    }

    public boolean isRepresantative() {
        return isRepresantative;
    }

    public void setRepresantative(boolean isRepresantative) {
        this.isRepresantative = isRepresantative;
    }
}