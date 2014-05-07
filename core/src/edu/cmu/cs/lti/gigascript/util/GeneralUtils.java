package edu.cmu.cs.lti.gigascript.util;

import edu.cmu.cs.lti.gigascript.agiga.AgigaSentenceWrapper;
import edu.cmu.cs.lti.gigascript.model.AgigaArgument;
import edu.cmu.cs.lti.gigascript.model.AgigaRelation;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 4/12/14
 * Time: 5:33 PM
 */
public class GeneralUtils {

    private static Set<String> relationBlackList = new HashSet<String>(Arrays.asList("be", "is",
            "was", "were", "are", "has", "have", "had", "be not", "is not", "was not", "were not",
            "are not", "has not", "have not", "had not", "to be", "to have","there be"));

    private static Set<String> actorBlackList =  new HashSet<String>(Arrays.asList("IN","TO","PP","MD",
            "POS","PDT","SYM","UH","CC","DT"));

    /**
     * Clean the two ends of a string to get some easy to process output
     *
     * @return
     */
    public static String cleanEnds(String str) {
        return str.replaceFirst("^[\\p{Punct}\\s]+", "").replaceAll("[\\p{Punct}\\s]+$", "");
    }

    /**
     * Replace some characters that I found annoying cuz I will naively use them as seperator!
     *
     * @return
     */
    public static String replaceSeps(String str) {
        //so, comma , tab , newlines , please become space
        return str.replaceAll(",", " ").replaceAll("\\t", " ").replace("\n", " ");
    }

    public static String getNiceTupleForm(String str) {
        return replaceSeps(cleanEnds(str));
    }

    public static <L, M, R> String triple2Str(Triple<L, M, R> triple, String split) {
        return String.format("%s%s%s%s%s", getStrRepre(triple.getLeft()), split, getStrRepre(triple.getMiddle()), split, getStrRepre(triple.getRight()));
    }

    public static <L, R> String pair2Str(Pair<L, R> pair, String split) {
        return String.format("%s%s%s", getStrRepre(pair.getLeft()), split, getStrRepre(pair.getRight()));
    }

    public static <T> String getStrRepre(T obj) {
        return obj == null ? "-" : obj.toString().trim();
    }

    public static boolean tupleFilter(Triple<AgigaArgument, AgigaArgument, AgigaRelation> tuple, AgigaSentenceWrapper sentWrapper) {
        AgigaRelation aRelation = tuple.getRight();
        AgigaArgument arg0 = tuple.getLeft();
        AgigaArgument arg1 = tuple.getMiddle();

        int headWordIndex0 = arg0.getKeywordTokenIndex();
        int headWordIndex1 = arg1.getKeywordTokenIndex();
        String headword1Pos = sentWrapper.getTokens().get(headWordIndex0).getPosTag();
        String headword2Pos = sentWrapper.getTokens().get(headWordIndex1).getPosTag();

        String relation = aRelation.getRelationStr().trim();
        List<String> relationPos = new ArrayList<String>();

        boolean isFirst = true;

        for (int index : aRelation.getIndices()) {
            if (!isFirst) {
                relationPos.add(sentWrapper.getTokens().get(index).getPosTag());
            }
            isFirst = false;
        }

        //sometimes super long relations are generated, not interested in those
        if (relation.split(" ").length > 5) {
            return false;
        }

        if (relation.equals("")) {
            return false;
        }

        if (relationBlackList.contains(aRelation.getKeywordLemma())) {
//            System.out.println("relation blacklist "+tuple);
            return false;
        }

        if (arg0.getHeadWordLemma().equals("") || arg1.getHeadWordLemma().equals("")) {
            return false;
        }


        if (headword1Pos.startsWith("V") || headword1Pos.startsWith("J") || headword1Pos.startsWith("R") || headword1Pos.startsWith("W")) {
//            System.out.println("incorrect pos 1 "+tuple+" "+headword1Pos);
            return false;
        }

        if (headword2Pos.startsWith("V") || headword2Pos.startsWith("W")) {
//            System.out.println("incorrect pos 2 "+tuple+" "+headword2Pos);
            return false;
        }

        if (actorBlackList.contains(headword1Pos) || actorBlackList.contains(headword2Pos)){
//            System.out.println("actor black list "+tuple+" "+headword1Pos+" "+headword2Pos);
            return false;
        }

        boolean noVerbInRelation = true;
        for (String pos : relationPos){
            if (pos.startsWith("V")){
                noVerbInRelation = false;
                break;
            }
        }

//        if (noVerbInRelation)
//            System.out.println("No verb in relation "+tuple);

        return !noVerbInRelation;
    }


    public static void main(String[] argv) {
        //do some testing

        String noise = " ^& *139,0" + "\t" + "-a1" + " / / ";

        System.out.println(cleanEnds(noise));
        System.out.println(replaceSeps(cleanEnds(noise)));
    }

}
