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
    private final TupleAddress[] addresses;

    public TupleInfo(String wholeTupleStr){
        String[] fields = wholeTupleStr.trim().split("\t");
        this.tupleStr = fields[0];
        this.tupleId = Integer.parseInt(fields[1]);

        String[] addressStrs = fields[5].split(":");

        this.addresses = new TupleAddress[addressStrs.length];
        for (int i = 0 ; i< addressStrs.length ; i++){
            addresses[i] = new TupleAddress(addressStrs[i]);
        }
    }

    public String getTupleStr() {
        return tupleStr;
    }

    public TupleAddress[] getAddresses() {
        return addresses;
    }

    public int getTupleId() {
        return tupleId;
    }

    public final class TupleAddress{
        public final String fileAddress;
        public final int sentenceId;
        public final int arg0TokenId;
        public final int arg1TokenId;

        public TupleAddress(String fileAddress, int sentId, int arg0Id, int arg1Id){
            this.fileAddress = fileAddress;
            this.sentenceId = sentId;
            this.arg0TokenId = arg0Id;
            this.arg1TokenId = arg1Id;
        }

        public TupleAddress(String fullAddress){
            String[] address = fullAddress.split(",");
            if (address.length != 4){
                this.fileAddress = "";
                this.sentenceId =  -1;
                this.arg0TokenId = -1;
                this.arg1TokenId = -1;
            }else {
                this.fileAddress = address[0];
                this.sentenceId =Integer.parseInt(address[1]);
                this.arg0TokenId = Integer.parseInt(address[2]);
                this.arg1TokenId = Integer.parseInt(address[3]);
            }
        }

        public String toString(){
            return String.format("%s,%d,%d,%d",fileAddress,sentenceId,arg0TokenId,arg1TokenId);
        }
    }

    @Override
    public String toString(){
        return String.format("%s@%s",tupleStr, Joiners.colonJoin(Arrays.asList(addresses)));
    }
}