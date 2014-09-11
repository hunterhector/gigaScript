package edu.cmu.cs.lti.gigascript.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 9/10/14
 * Time: 12:28 AM
 */
public class GigaDocument {

    private List<GigaEntity> entities = new ArrayList<GigaEntity>();

    private final String docId;

    public GigaDocument(String docId){
        this.docId = docId;
    }

    public void addEntity(GigaEntity entity){
        entities.add(entity);
    }

    public List<GigaEntity> getEntities(){
        return entities;
    }

    public String getDocId(){
        return  docId;
    }

    public boolean isEmpty(){
        return entities.isEmpty();
    }

}
