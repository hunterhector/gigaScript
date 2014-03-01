package edu.cmu.cs.lti.gigascript.agiga;

import edu.cmu.cs.lti.gigascript.util.Joiners;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhengzhongliu on 2/26/14.
 */
public class AgigaArgument {
    private int keywordTokenIndex;
    private int sentenceIndex;

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

    //caution, not deep copy here.
    public void setAlternativeForms(List<String> alternativeForms) {
        this.alternativeForms = alternativeForms;
    }

    public void addAlternativeForms(String alternativeForm){
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

}