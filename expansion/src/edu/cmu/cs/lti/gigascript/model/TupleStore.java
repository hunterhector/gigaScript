package edu.cmu.cs.lti.gigascript.model;

import org.mapdb.Bind;
import org.mapdb.DBMaker;
import org.mapdb.Fun;
import org.mapdb.HTreeMap;

import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 3/29/14
 * Time: 5:12 PM
 */
public class TupleStore {

    public static void main(String[] args) {
        //primary map
        HTreeMap<Long,String> map = DBMaker.newTempHashMap();

        // inverse mapping for primary map
        NavigableSet<Fun.Tuple2<String, Long>> inverseMapping = new TreeSet<Fun.Tuple2<String, Long>>();
        //NOTE: you may also use Set provided by MapDB to make it persistent

        // bind inverse mapping to primary map, so it is auto-updated
        Bind.mapInverse(map, inverseMapping);

        map.put(10L,"value2");
        map.put(1111L,"value");
        map.put(1112L,"value");
        map.put(11L,"val");

        //now find all keys for given value
        for(Long key: Fun.filter(inverseMapping, "value")){
            System.out.println("Key for 'value' is: "+key);
        }
    }

}
