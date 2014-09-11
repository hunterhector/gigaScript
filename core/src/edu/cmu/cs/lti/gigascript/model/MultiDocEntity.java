package edu.cmu.cs.lti.gigascript.model;

import com.google.common.collect.ArrayListMultimap;
import edu.jhu.agiga.AgigaToken;
import org.apache.commons.lang3.tuple.Pair;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 9/10/14
 * Time: 6:36 PM
 */
public class MultiDocEntity {
    private  List<GigaEntity> entities  = new ArrayList<GigaEntity>();
    private int life;
    private int age = 0;

    public static final int DEFAULT_LIFE = 50;

    public MultiDocEntity(GigaEntity entity){
        this(entity,DEFAULT_LIFE);
    }

    public MultiDocEntity(GigaEntity entity, int life){
        entities.add(entity);
        this.life = life;
    }


    public MultiDocEntity(List<GigaEntity> entities){
        this(entities,DEFAULT_LIFE);
    }

    public MultiDocEntity(List<GigaEntity> entities, int life){
        for (GigaEntity e : entities){
            this.entities.add(e);
        }
        this.life = life;
    }

    public List<GigaEntity> getEntities(){
        return entities;
    }

    public void merge(MultiDocEntity md){
        for (GigaEntity e : md.getEntities()){
            entities.add(e);
        }
        this.age = -1;
    }

    public boolean decay(){
        age ++;
        return life == age;
    }

    public void writeOut(PrintStream out){
        ArrayListMultimap<String, AgigaToken> r2p = ArrayListMultimap.create();

        for (GigaEntity entity : entities) {
            for (Map.Entry<GigaMention, List<Pair<AgigaToken, String>>> mentionWithArgument : entity.getMentioinWithArguments().entrySet()) {
                List<Pair<AgigaToken, String>> arguments = mentionWithArgument.getValue();

                for (Pair<AgigaToken, String> argument : arguments) {
                    AgigaToken predicate = argument.getKey();
                    String role = argument.getValue();
                    r2p.put(role, predicate);
                }
            }
            writeLine("##Chaining entity: ",out);
            writeLine(entity.getRepresentative().tokensToString(),out);
        }

        for (String role : r2p.keySet()) {
            writeLine("##As role : " + role,out);
            for (AgigaToken token : r2p.get(role)) {
                writeLine(token.getWord(),out);
            }
        }

        writeLine("",out);
        writeLine("",out);
    }


    private void writeLine(String str, PrintStream out) {
        out.println(str);
    }
}
