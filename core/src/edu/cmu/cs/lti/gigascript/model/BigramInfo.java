package edu.cmu.cs.lti.gigascript.model;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * The class to save the bigram information, the bigram themselves are not saved here
 * <p/>
 * Created with IntelliJ IDEA.
 * User: Hector, Zhengzhong Liu
 * Date: 3/4/14
 * Time: 3:46 PM
 */
public class BigramInfo {
    /**
     * Store the distances, tuple distance and sentence distance
     */
    TIntIntHashMap tupleDistanceCount = new TIntIntHashMap();
    TIntIntHashMap sentenceDistanceCount = new TIntIntHashMap();

    /**
     * Use integer to encode the equality to save memory, with the following binary coding
     * E11 : 0  (00)
     * E12 : 1  (01)
     * E21 : 2  (10)
     * E22 : 3  (11)
     */
    TIntArrayList tupleEqualityCount = new TIntArrayList();

    /**
     * Construct from we first see this tuple
     *
     * @param tupleDistance    The tuple distance that we first see it
     * @param sentenceDistance The sentence distance when we first see it.
     * @param equality         The Equality constraint when we first see it.
     */
    public BigramInfo(int sentenceDistance, int tupleDistance, int[][] equality) {
        tupleDistanceCount.put(tupleDistance, 1);
        sentenceDistanceCount.put(sentenceDistance, 1);
        tupleEqualityCount.add(equality[0][0]);
        tupleEqualityCount.add(equality[0][1]);
        tupleEqualityCount.add(equality[1][0]);
        tupleEqualityCount.add(equality[1][1]);
    }

    /**
     * A new observation of the bigram
     *
     * @param tupleDistance The observed tuple distance
     * @param sentenceDistance The observed sentence distance
     * @param equality The observed equality
     */
    public void observe( int sentenceDistance,int tupleDistance, int[][] equality){
        tupleDistanceCount.adjustOrPutValue(tupleDistance, 1, 1);
        sentenceDistanceCount.adjustOrPutValue(sentenceDistance, 1,1);
        tupleEqualityCount.set(0, equality[0][0] + tupleEqualityCount.get(0));
        tupleEqualityCount.set(1, equality[0][1] + tupleEqualityCount.get(1));
        tupleEqualityCount.set(2, equality[1][0] + tupleEqualityCount.get(2));
        tupleEqualityCount.set(3, equality[1][1] + tupleEqualityCount.get(3));
    }


    public TIntIntHashMap getTupleDistanceCount() {
        return tupleDistanceCount;
    }

    public TIntIntHashMap getSentenceDistanceCount() {
        return sentenceDistanceCount;
    }



    public TIntArrayList getTupleEqualityCount() {
        return tupleEqualityCount;
    }


//    TIntIntHashMap tupleReverseDistanceCount = new TIntIntHashMap();
//    TIntIntHashMap sentenceReverseDistanceCount = new TIntIntHashMap();
//    TIntArrayList tupleReverseEqualityCount = new TIntArrayList();


//    /**
//     * Construct when we first see a tuple in reverse order
//     * @param sentenceDistance
//     * @param tupleDistance
//     * @param equality
//     * @param reverse  just to distinguish it from the natural order constructor
//     */
//    public BigramInfo(int sentenceDistance, int tupleDistance, int[][] equality, boolean reverse) {
//        tupleReverseDistanceCount.put(tupleDistance, 1);
//        sentenceReverseDistanceCount.put(sentenceDistance, 1);
//        tupleReverseEqualityCount.add(equality[0][0]);
//        tupleReverseEqualityCount.add(equality[1][0]);
//        tupleReverseEqualityCount.add(equality[0][1]);
//        tupleReverseEqualityCount.add(equality[1][1]);
//    }
//
//    public void observeReverse(int sentenceDistance,  int tupleDistance,  int[][] equality){
//        tupleReverseDistanceCount.adjustOrPutValue(tupleDistance, 1, 1);
//        sentenceReverseDistanceCount.adjustOrPutValue(sentenceDistance, 1, 1);
//        tupleReverseEqualityCount.set(0, equality[0][0] + tupleEqualityCount.get(0));
//        tupleReverseEqualityCount.set(1, equality[1][0] + tupleEqualityCount.get(0));
//        tupleReverseEqualityCount.set(2, equality[0][1] + tupleEqualityCount.get(0));
//        tupleReverseEqualityCount.set(3, equality[1][1] + tupleEqualityCount.get(0));
//    }
//
//    public TIntIntHashMap getTupleReverseDistanceCount() {
//        return tupleReverseDistanceCount;
//    }
//
//    public TIntIntHashMap getSentenceReverseDistanceCount() {
//        return sentenceReverseDistanceCount;
//    }
//
//
//    public TIntArrayList getTupleReverseEqualityCount() {
//        return tupleReverseEqualityCount;
//    }

}
