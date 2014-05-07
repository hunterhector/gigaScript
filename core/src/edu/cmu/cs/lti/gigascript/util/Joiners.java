package edu.cmu.cs.lti.gigascript.util;

import com.google.common.base.Joiner;

/**
 * Created with IntelliJ IDEA.
 * User: Hector, Zhengzhong Liu
 * Date: 2/28/14
 * Time: 2:04 PM
 */
public class Joiners {

    private static final Joiner spaceJoiner = Joiner.on(" ");

    private static final Joiner commaJoiner = Joiner.on(",");

    private static final Joiner tabJoiner = Joiner.on("\t");

    private static final Joiner colonJoiner = Joiner.on(":");

    public static String spaceJoin(Iterable<?> parts){
        return spaceJoiner.join(parts);
    }

    public static String colonJoin(Iterable<?> parts) {return colonJoiner.join(parts);}

    public static String commaJoin(Iterable<?> parts){
        return commaJoiner.join(parts);
    }

    public static String tabJoin(Iterable<?> parts){
        return tabJoiner.join(parts);
    }
}
