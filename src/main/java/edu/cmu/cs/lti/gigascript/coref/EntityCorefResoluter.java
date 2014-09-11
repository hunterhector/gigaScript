package edu.cmu.cs.lti.gigascript.coref;

import edu.cmu.cs.lti.gigascript.model.GigaEntity;
import edu.cmu.cs.lti.gigascript.model.GigaMention;
import edu.cmu.cs.lti.gigascript.model.MultiDocEntity;
import edu.cmu.cs.lti.gigascript.util.StringUtils;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.lang3.tuple.Triple;

import java.io.PrintStream;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 9/10/14
 * Time: 5:29 PM
 */
public class EntityCorefResoluter {
    public List<MultiDocEntity> multiDocCoref(List<MultiDocEntity> rollingEntities, List<Collection<GigaEntity>> entitesByDoc, PrintStream out) {
        for (Collection<GigaEntity> anEntitesByDoc : entitesByDoc) {
            List<MultiDocEntity> newEntities = new ArrayList<MultiDocEntity>();

            for (GigaEntity entity : anEntitesByDoc) {
                newEntities.add(new MultiDocEntity(entity));
            }

            rollingEntities = pairDocCorf(rollingEntities, newEntities);
        }

        Iterator<MultiDocEntity> iter = rollingEntities.iterator();

        while (iter.hasNext()) {
            MultiDocEntity e = iter.next();
            if (e.decay()) {
                iter.remove();
                e.writeOut(out);
            }
        }

        return rollingEntities;
    }

    public List<MultiDocEntity> pairDocCorf(List<MultiDocEntity> rollingEntities, List<MultiDocEntity> newEntities) {
        List<MultiDocEntity> combinedEntities = new ArrayList<MultiDocEntity>();

        //cal mentions surface similarity
        List<List<GigaMention>> rollingEntityMentions = new ArrayList<List<GigaMention>>();
        for (MultiDocEntity rollingEntity : rollingEntities) {
            ArrayList<GigaMention> usefulMentions = new ArrayList<GigaMention>();
            for (GigaEntity entity : rollingEntity.getEntities()) {
                usefulMentions.add(entity.getRepresentative());
            }
            rollingEntityMentions.add(usefulMentions);
        }

        List<List<GigaMention>> newEntityMentions = new ArrayList<List<GigaMention>>();

        for (MultiDocEntity newEntity : newEntities) {
            ArrayList<GigaMention> usefulMentions = new ArrayList<GigaMention>();

            for (GigaEntity entity : newEntity.getEntities()) {
                usefulMentions.add(entity.getRepresentative());
            }
            newEntityMentions.add(usefulMentions);
        }

        PriorityQueue<Triple<Double, Integer, Integer>> allSimScores = new PriorityQueue<Triple<Double, Integer, Integer>>();

        double threshold = 0.5;

        TIntIntHashMap entityMapping = new TIntIntHashMap();

        for (int i = 0; i < rollingEntityMentions.size(); i++) {
            for (int j = 0; j < newEntityMentions.size(); j++) {
                List<GigaMention> rollingMentionList = rollingEntityMentions.get(i);
                List<GigaMention> newMentionList = newEntityMentions.get(j);

                double mentionSim = getMentionSimilarities(rollingMentionList, newMentionList);
                if (mentionSim > threshold) {
                    allSimScores.add(Triple.of(1 - mentionSim, i, j));
                }
            }
        }

        TIntHashSet mappedEntities1 = new TIntHashSet();
        TIntHashSet mappedEntities2 = new TIntHashSet();

        while (allSimScores.size() != 0) {
            Triple<Double, Integer, Integer> mapping = allSimScores.poll();
            int idx1 = mapping.getMiddle();
            int idx2 = mapping.getRight();
            if (!mappedEntities1.contains(idx1) && !mappedEntities2.contains(idx2)) {
                entityMapping.put(idx1, idx2);

                MultiDocEntity entity1 = rollingEntities.get(idx1);
                MultiDocEntity entity2 = newEntities.get(idx2);

                entity1.merge(entity2);

                combinedEntities.add(entity1);

                //System.out.println("Coreference between " + entity1.getEntities().get(0).getRepresentative() + " and " + entity2.getEntities().get(0).getRepresentative());
                //System.out.println("Score is "+ (1 - mapping.getLeft()));

                mappedEntities1.add(idx1);
                mappedEntities2.add(idx2);
            }
        }

        for (int i = 0; i < rollingEntities.size(); i++) {
            if (!mappedEntities1.contains(i)) {
                combinedEntities.add(rollingEntities.get(i));
            }
        }

        for (int i = 0; i < newEntities.size(); i++) {
            if (!mappedEntities2.contains(i)) {
                combinedEntities.add(newEntities.get(i));
            }
        }

        return combinedEntities;
    }


    private double getMentionSimilarities(List<GigaMention> usefulMention1, List<GigaMention> usefulMention2) {
        double similarity = 0;

        if (usefulMention1.size() == 0 || usefulMention2.size() == 0){
            return 0;
        }

        int sameType = 0;
        int differentType = 0;
        for (GigaMention mention1 : usefulMention1) {
            for (GigaMention mention2 : usefulMention2) {
                similarity += StringUtils.tokenDiceScore(mention1.getTokenStrs(), mention2.getTokenStrs());
                if (mention1.getEntityType() != null && mention2.getEntityType() != null){
                    if (!mention1.getEntityType().equals("O") && !mention2.getEntityType().equals("O")){
                        if (mention1.getEntityType().equals(mention2.getEntityType())){
                            sameType += 1;
                        }else{
                            differentType +=1 ;
                        }
                    }
                }

            }
        }

        if (sameType < differentType){
            return 0;
        }

        return similarity / (usefulMention1.size() * usefulMention2.size());
    }


//    private List<GigaMention> getUsefulMentions(GigaEntity entity) {
//        List<GigaMention> usefulMentions = new ArrayList<GigaMention>();
//
//        for (GigaMention mention : entity.getMentions()) {
//            if (mention.getEntityType() != null) {
//                System.out.println("Adding useful mention "+ mention.tokensToString()+ ' ' + mention.getEntityType());
//                usefulMentions.add(mention);
//            }
//        }
//
//        return usefulMentions;
//    }


}
