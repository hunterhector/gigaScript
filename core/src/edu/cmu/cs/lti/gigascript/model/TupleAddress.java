package edu.cmu.cs.lti.gigascript.model;

public final class TupleAddress {
    public final String fileAddress;
    public final int sentenceId;
    public final int arg0TokenId;
    public final int arg1TokenId;

    public TupleAddress(String fileAddress, int sentId, int arg0Id, int arg1Id) {
        this.fileAddress = fileAddress;
        this.sentenceId = sentId;
        this.arg0TokenId = arg0Id;
        this.arg1TokenId = arg1Id;
    }

    public TupleAddress(String fullAddress) {
        String[] address = fullAddress.split(",");
        if (address.length != 4) {
            this.fileAddress = "";
            this.sentenceId = -1;
            this.arg0TokenId = -1;
            this.arg1TokenId = -1;
        } else {
            this.fileAddress = address[0];
            this.sentenceId = Integer.parseInt(address[1]);
            this.arg0TokenId = Integer.parseInt(address[2]);
            this.arg1TokenId = Integer.parseInt(address[3]);
        }
    }

    public String toString() {
        return String.format("%s,%d,%d,%d", fileAddress, sentenceId, arg0TokenId, arg1TokenId);
    }
}