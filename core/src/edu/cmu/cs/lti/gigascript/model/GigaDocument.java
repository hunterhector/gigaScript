package edu.cmu.cs.lti.gigascript.model;

import com.google.common.collect.HashBasedTable;
import edu.jhu.agiga.AgigaToken;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 9/10/14
 * Time: 12:28 AM
 */
public class GigaDocument {
    private final HashBasedTable<GigaEntity,AgigaToken,String> entityWithRoles;

    private final String docId;

    public GigaDocument(String docId, HashBasedTable<GigaEntity,AgigaToken,String> entityWithRoles){
        this.entityWithRoles = entityWithRoles;
        this.docId = docId;
    }

    public HashBasedTable<GigaEntity,AgigaToken,String> getEntityWithRoles(){
        return entityWithRoles;
    }

    public String getDocId(){
        return  docId;
    }

}
