package edu.cmu.cs.lti.gigascript.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 2/26/14
 * Time: 1:16 AM
 */
public class AgigaArgument {
    private int keywordTokenIndex;
    private int sentenceIndex;

    private String headWordLemma;
    private String entityType;

    public AgigaArgument(int sentenceIndex, int keywordTokenIndex) {
        this.keywordTokenIndex = keywordTokenIndex;
        this.sentenceIndex = sentenceIndex;
    }

    public int getSentenceIndex() {
        return sentenceIndex;
    }

    public void setSentenceIndex(int sentenceIndex) {
        this.sentenceIndex = sentenceIndex;
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
        if (!(obj instanceof AgigaArgument))
            return false;

        AgigaArgument rhs = (AgigaArgument) obj;

        return new EqualsBuilder().append(keywordTokenIndex, rhs.keywordTokenIndex).append(sentenceIndex, rhs.sentenceIndex).isEquals();
    }

    public int hashCode(){
        return new HashCodeBuilder(17,31).append(keywordTokenIndex).append(sentenceIndex).toHashCode();
    }


    public String toString(){
        return String.format("[%s]@(%d,%d)",headWordLemma,sentenceIndex,keywordTokenIndex);
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
}