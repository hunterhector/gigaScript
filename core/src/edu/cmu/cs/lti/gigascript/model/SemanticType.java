package edu.cmu.cs.lti.gigascript.model;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 2/26/14
 * Time: 1:17 AM
 */
public class SemanticType {
    String type;

    public SemanticType(){

    }

    public SemanticType(String type){
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
