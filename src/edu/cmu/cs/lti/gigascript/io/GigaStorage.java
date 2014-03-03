package edu.cmu.cs.lti.gigascript.io;

import edu.cmu.cs.lti.gigascript.agiga.AgigaArgument;

/**
 * Created with IntelliJ IDEA.
 * User: Hector, Zhengzhong Liu
 * Date: 3/1/14
 * Time: 4:10 PM
 */
public abstract class GigaStorage {
    public abstract long addGigaTuple(AgigaArgument arg0, AgigaArgument arg1, String relation);
    public abstract void addGigaBigram(long t1, long t2, int distance, int[][] equality);
}