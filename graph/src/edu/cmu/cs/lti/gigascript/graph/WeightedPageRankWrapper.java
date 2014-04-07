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
        for (int i = results.size() - min ; i< results.size(); i++){
            topkIndex[count] = results.get(i).getLeft();
            count ++;
        }
        return topkIndex;
    }

    public List<Pair<Integer, Double>> topK(int k) throws IllegalAccessException {
        if (results == null) {
            throw new IllegalAccessException("Haven't run pagerank yet!");
        } else {
            int min = k > results.size() ? results.size() : k;
            return results.subList(results.size()-min,results.size());
        }
    }

    public List<Pair<Integer, Double>> run() throws IOException {
        pr.stepUntil(finalStop);
        double[] rank = pr.rank;

        PriorityQueue<Pair<Integer, Double>> queue = new PriorityQueue<Pair<Integer, Double>>(rank.length, new RankPairComparator());

        for (int i = 0; i < rank.length; i++) {
            queue.add(Pair.of(i, rank[i]));
        }

        Iterator<Pair<Integer,Double>> iter = queue.iterator();
        results =  new ArrayList<Pair<Integer, Double>>(queue.size());
        while (iter.hasNext()){
            results.add(iter.next());
        }

        return results;
    }


    class RankPairComparator implements Comparator<Pair<Integer, Double>> {
        @Override
        public int compare(Pair<Integer, Double> o1, Pair<Integer, Double> o2) {
            return o1.getValue().compareTo(o2.getValue());
        }
    }
}
