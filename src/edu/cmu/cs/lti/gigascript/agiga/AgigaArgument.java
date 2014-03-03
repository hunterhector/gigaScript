package edu.cmu.cs.lti.gigascript.agiga;

import edu.cmu.cs.lti.gigascript.util.Joiners;
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
public class AgigaArgument {
    private int keywordTokenIndex;
    private int sentenceIndex;

    private String headWordLemma;
    private String entityType;

    // store alternative forms of the argument in order to make generization possible
    // for example, we could save the entity type of the argument here
    private List<String> alternativeForms;

    public AgigaArgument() {

    }

    public AgigaArgument(int sentenceIndex, int keywordTokenIndex) {
        this.keywordTokenIndex = keywordTokenIndex;
        this.sentenceIndex = sentenceIndex;
        this.alternativeForms = new ArrayList<String>();
    }


    public int getKeywordTokenIndex() {
        return keywordTokenIndex;
    }

    public void setKeywordTokenIndex(int keywordTokenIndex) {
        this.keywordTokenIndex = keywordTokenIndex;
    }

    public int getSentenceIndex() {
        return sentenceIndex;
    }

    public void setSentenceIndex(int sentenceIndex) {
        this.sentenceIndex = sentenceIndex;
    }

    public List<String> getAlternativeForms() {
        return alternativeForms;
    }

    private void addAlternativeForms(String alternativeForm){
        alternativeForms.add(alternativeForm);
    }

    public Pair<Integer, Integer> getIndexingPair(){
       return Pair.of(keywordTokenIndex,sentenceIndex);
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
        return String.format("[%s]@(%d,%d)",Joiners.commaJoin(alternativeForms),sentenceIndex,keywordTokenIndex);
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        addAlternativeForms(entityType);
        this.entityType = entityType;
    }

    public String getHeadWordLemma() {
        return headWordLemma;
    }

    public void setHeadWordLemma(String headWordLemma) {
        addAlternativeForms(headWordLemma);
        this.headWordLemma = headWordLemma;
    }
}