package edu.cmu.cs.lti.gigascript.agiga;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

/**
 * Created by zhengzhongliu on 2/26/14.
 */
public class AgigaArgument {
    private int keywordTokenIndex;
    private int sentenceIndex;
    private int documentIndex;

    // store alternative forms of the argument in order to make generization possible
    // for example, we could save the entity type of the argument here
    private List<String> alternativeForms;

    public AgigaArgument() {

    }

    public AgigaArgument(int documentIndex, int sentenceIndex, int keywordTokenIndex) {
        this.keywordTokenIndex = keywordTokenIndex;
        this.sentenceIndex = sentenceIndex;
        this.documentIndex = documentIndex;
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

    public void setAlternativeForms(List<String> alternativeForms) {
        this.alternativeForms = alternativeForms;
    }

    public int getDocumentIndex() {
        return documentIndex;
    }

    public void setDocumentIndex(int documentIndex) {
        this.documentIndex = documentIndex;
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof AgigaArgument))
            return false;

        AgigaArgument rhs = (AgigaArgument) obj;
        return new EqualsBuilder().append(keywordTokenIndex, rhs.keywordTokenIndex).append(sentenceIndex, rhs.sentenceIndex).append(documentIndex,rhs.documentIndex).isEquals();
    }

    public int hashCode(){
        return new HashCodeBuilder(17,31).append(keywordTokenIndex).append(sentenceIndex).append(documentIndex).toHashCode();
    }


}