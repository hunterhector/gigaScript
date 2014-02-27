package edu.cmu.cs.lti.gigascript.agiga;

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

    public AgigaArgument(){

    }

    public AgigaArgument(int keywordTokenIndex, int sentenceIndex){
        this.keywordTokenIndex = keywordTokenIndex;
        this.sentenceIndex = sentenceIndex;
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
}