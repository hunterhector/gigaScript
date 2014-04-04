package edu.cmu.cs.lti.gigascript;

import org.mapdb.DBMaker;

import java.util.concurrent.ConcurrentNavigableMap;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 3/29/14
 * Time: 5:12 PM
 */
public class tupleStore {
    ConcurrentNavigableMap treeMap = DBMaker.newTempTreeMap();



}
