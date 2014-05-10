package edu.cmu.cs.lti.gigascript.model;

import edu.cmu.cs.lti.gigascript.agiga.AgigaUtil;
import edu.cmu.cs.lti.gigascript.util.GeneralUtils;
import edu.jhu.agiga.AgigaSentence;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 5/6/14
 * Time: 12:55 PM
 */
public class AgigaRelation {
    public static String NORMAL_TYPE = "normal";
    public static String APPOSITION_TYPE = "apposition";
    public static String POSSESSIVE_TYPE = "possessive";
    public static String UNKNOWN_TYPE = "unknown";

    String relationStr;
    String relationType;
    List<Integer> indices = new ArrayList<Integer>();
    String keywordLemma;

    int sentenceIndex;

    public AgigaRelation(AgigaSentence sent,List<Integer> indices){
        this.indices = indices;
        relationStr =  GeneralUtils.getNiceTupleForm(AgigaUtil.getShortenLemmaForPhrase(sent, indices));
        relationType = NORMAL_TYPE;
        keywordLemma = AgigaUtil.getLemma(sent.getTokens().get(indices.get(0)));
    }

    public String getKeywordLemma(){
        return keywordLemma;
    }

    public AgigaRelation(String relationStr){
        if (relationStr.equals("_is")) {
            this.relationType = APPOSITION_TYPE;
        }else if (relationStr.equals("_has")){
            this.relationType = POSSESSIVE_TYPE;
        }else{
            this.relationType = UNKNOWN_TYPE;
        }
        this.relationStr = relationStr;
        keywordLemma = relationStr;
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof AgigaRelation))
            return false;

        AgigaRelation rhs = (AgigaRelation) obj;


        boolean indiceEqual = true;

        if (indices != null && indices.size() > 0){
            if (rhs.indices != null && indices.size() > 0){
                indiceEqual = indices.get(0).equals(rhs.indices.get(0));
            }
        }

        if (relationType.equals(NORMAL_TYPE)) {
            return new EqualsBuilder().append(relationStr, rhs.relationStr).append(sentenceIndex, rhs.sentenceIndex).isEquals() && indiceEqual;
        }else{
            return new EqualsBuilder().append(relationStr,rhs.relationStr).append(relationType,rhs.relationType).isEquals();
        }
    }

    public int hashCode(){
        if (indices != null && indices.size() > 0) {
            return new HashCodeBuilder(17, 31).append(relationStr).append(sentenceIndex).append(indices.get(0)).toHashCode();
        }else{
            return new HashCodeBuilder(17, 31).append(relationStr).append(sentenceIndex).toHashCode();
        }
    }


    public String toString(){
        return String.format("%s",relationStr);
    }


    public String getRelationStr() {
        return relationStr;
    }

    public String getRelationType() {
        return relationType;
    }

    public int getSentenceIndex() {
        return sentenceIndex;
    }

    public List<Integer> getIndices() {
        return indices;
    }
}
