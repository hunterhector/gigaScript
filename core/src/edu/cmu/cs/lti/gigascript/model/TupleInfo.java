package edu.cmu.cs.lti.gigascript.model;

import edu.cmu.cs.lti.gigascript.util.Joiners;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 7/21/14
 * Time: 2:23 PM
 */
public class TupleInfo {
    private int tupleId;
    private final String tupleStr;
    private final String[] addresses;

    public TupleInfo(String wholeTupleStr){
        String[] fields = wholeTupleStr.trim().split("\t");
        this.tupleStr = fields[0];
        this.tupleId = Integer.parseInt(fields[1]);

        addresses = fields[5].split(":");
    }

    public String getTupleStr() {
        return tupleStr;
    }

    public String[] getAddresses() {
        return addresses;
    }

    public int getTupleId() {
        return tupleId;
    }

    @Override
    public String toString(){
        return String.format("%s@%s",tupleStr, Joiners.colonJoin(Arrays.asList(addresses)));
    }
}