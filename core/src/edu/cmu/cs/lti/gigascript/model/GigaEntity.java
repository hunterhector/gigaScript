package edu.cmu.cs.lti.gigascript.model;

import edu.jhu.agiga.AgigaToken;
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

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

    private LinkedHashMap<GigaMention,List<Pair<AgigaToken, String>>> mentionWithArguments;

    public GigaEntity(){
        mentionWithArguments = new LinkedHashMap<GigaMention, List<Pair<AgigaToken, String>>>();
    }

    public void addMention(GigaMention mention, List<Pair<AgigaToken ,String>> roles){
        mentionWithArguments.put(mention, roles);
    }

    public GigaMention getRepresentative() {
        return representative;
    }

    public void setRepresentative(GigaMention representative) {
        this.representative = representative;
    }

    public Set<GigaMention> getMentions(){
        return mentionWithArguments.keySet();
    }

    public LinkedHashMap<GigaMention, List<Pair<AgigaToken, String>>> getMentioinWithArguments(){
        return mentionWithArguments;
    }

    public boolean isEmpty(){
        return mentionWithArguments.isEmpty();
    }

}