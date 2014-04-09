package edu.cmu.cs.lti.gigascript.graph;

import es.yrbcn.graph.weighted.WeightedPageRank;
import es.yrbcn.graph.weighted.WeightedPageRankPowerMethod;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.webgraph.labelling.ArcLabelledImmutableGraph;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: hector
 * Date: 7/1/12
 * Time: 3:00 AM
 */
public class WeightedPageRankWrapper {
    List<Pair<Integer, Double>> results;
    WeightedPageRankPowerMethod pr;
    WeightedPageRank.StoppingCriterion finalStop;

    public WeightedPageRankWrapper(ArcLabelledImmutableGraph g, double alpha, boolean stronglyPreferential, double threshold, int maxIter, DoubleList start, DoubleList preference) {
        pr = new WeightedPageRankPowerMethod(g);
        pr.alpha = alpha;
        pr.stronglyPreferential = stronglyPreferential;
        pr.start = start;
        pr.preference = preference;

        WeightedPageRank.NormDeltaStoppingCriterion deltaStop = new WeightedPageRank.NormDeltaStoppingCriterion(threshold);
        WeightedPageRank.IterationNumberStoppingCriterion iterStop = new WeightedPageRank.IterationNumberStoppingCriterion(maxIter);
        finalStop = WeightedPageRank.or(deltaStop, iterStop);

        try {
            run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int[] topKIndex(int k) throws IllegalAccessException {
        if (results == null) {
            throw new IllegalAccessException("Haven't run pagerank yet!");
        }
        int min = k > results.size() ? results.size() : k;
        int[] topkIndex = new int[min];

        int count = 0;
//        for (int i = min -1  ; i >= 0; i--){
        for (int i = results.size() -1 ; i >= results.size() - min; i--){
            topkIndex[count] = results.get(i).getLeft();
            count ++;
        }
        return topkIndex;
    }

    public double[] topKScore(int k) throws IllegalAccessException {
        if (results == null) {
            throw new IllegalAccessException("Haven't run pagerank yet!");
        } else {
            int min = k > results.size() ? results.size() : k;
//            List<Pair<Integer, Double>> topKScore = results.subList(0, min);
            double[] topkScores = new double[min];

            int count = 0;
//        for (int i = min -1  ; i >= 0; i--){
            for (int i = results.size() -1 ; i >= results.size() - min; i--){
                topkScores[count] = results.get(i).getRight();
                count ++;
            }
            return topkScores;
        }
    }

    public List<Pair<Integer, Double>> run() throws IOException {
        pr.stepUntil(finalStop);
        double[] rank = pr.rank;

        results =  new ArrayList<Pair<Integer, Double>>(rank.length);

        for (int i = 0; i < rank.length; i++) {
            results.add(Pair.of(i, rank[i]));
        }

        Collections.sort(results,new RankPairComparator());

        return results;
    }

    class RankPairComparator implements Comparator<Pair<Integer, Double>> {
        @Override
        public int compare(Pair<Integer, Double> o1, Pair<Integer, Double> o2) {
            return o1.getValue().compareTo(o2.getValue());
        }
    }
}