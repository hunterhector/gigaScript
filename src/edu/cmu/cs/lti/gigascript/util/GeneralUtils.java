package edu.cmu.cs.lti.gigascript.util;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 4/12/14
 * Time: 5:33 PM
 */
public class GeneralUtils {

    /**
     * Clean the two ends of a string to get some easy to process output
     * @return
     */
    public static String cleanEnds(String str){
        return str.replaceFirst("^[\\p{Punct}\\s]+", "").replaceAll("[\\p{Punct}\\s]+$", "");
    }

    /**
     * Replace some characters that I found annoying cuz I will naively use them as seperator!
     * @return
     */
    public static String replaceSeps(String str){
        //so, comma and tab, please become space
        return str.replaceAll(","," ").replaceAll("\\t"," ");
    }

    public static String getNiceTupleForm(String str){
        return replaceSeps(cleanEnds(str));
    }

    public static  <L,M,R> String  triple2Str(Triple<L,M,R> triple,String split){
        return String.format("%s%s%s%s%s",getStrRepre(triple.getLeft()),split,getStrRepre(triple.getMiddle()),split,getStrRepre(triple.getRight()));
    }

    public static <L,R> String pair2Str(Pair<L,R> pair, String split){
        return String.format("%s%s%s",getStrRepre(pair.getLeft()),split,getStrRepre(pair.getRight()));
    }

    public static <T> String getStrRepre(T obj){
        return obj == null ? "-" : obj.toString();
    }

    public static void main(String[] argv){
        //do some testing

        String noise = " ^& *139,0"+"\t"+"-a1"+" / / ";

        System.out.println(cleanEnds(noise));
        System.out.println(replaceSeps(cleanEnds(noise)));
    }

}
