package edu.cmu.cs.lti.gigascript.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to represent a cross document entity
 *
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 9/9/14
 * Time: 3:13 PM
 */
public class GigaEntity {
    private GigaMention representative;

    private List<GigaMention> mentions;

    public GigaEntity(){
        mentions = new ArrayList<GigaMention>();
    }

    public void addMention(GigaMention mention){
        mentions.add(mention);
    }

    public GigaMention getRepresentative() {
        return representative;
    }

    public void setRepresentative(GigaMention representative) {
        this.representative = representative;
    }

    public List<GigaMention> getMentions(){
        return mentions;
    }
}
