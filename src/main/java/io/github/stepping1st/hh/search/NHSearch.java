package io.github.stepping1st.hh.search;


import io.github.stepping1st.hh.IdxVal;
import io.github.stepping1st.hh.IntNDArray;
import io.github.stepping1st.hh.Query;
import io.github.stepping1st.hh.SortedLCCS;
import io.github.stepping1st.hh.hash.Hash;
import io.github.stepping1st.hh.Dist;

import java.util.List;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.Collections;
import java.util.function.IntConsumer;


/**
 * Nearest Hyperplane(NH) Search based on LCCS Bucketing Framework.
 */
public class NHSearch implements Search<Query> {
    private final Hash<double[][], int[][], int[]> hash;
    private final SortedLCCS bucketerp;
    private final int m;

    /**
     * @param hash hash
     * @param m    single hasher of the compond hasher
     * @param data input data
     */
    public NHSearch(final Hash<double[][], int[][], int[]> hash,
                    final int m,
                    final double[][] data) {
        this.hash = hash;
        this.m = m;
        int n = data.length;
        IntNDArray arr = new IntNDArray(new int[]{n, m});
        int[][] sigs = hash.data(data);
        for (int i = 0; i < n; i++) {
            int[] sig = sigs[i];
            for (int j = 0; j < m; j++) {
                arr.set(sig[j], i, j);
            }

        }
        // sort arr data index by value per dim
        this.bucketerp = new SortedLCCS(1, arr);
    }

    @Override
    public final List<IdxVal> nns(final Query param) {
        double[][] data = param.data();
        double[] query = param.query();
        int top = param.top();
        Dist fun = param.dist();

        PriorityQueue<IdxVal> queue = new PriorityQueue<>(
                new Comparator<IdxVal>() {
                    @Override
                    public int compare(final IdxVal o1, final IdxVal o2) {
                        return -Double.compare(o1.value(), o2.value());
                    }
                });

        int[] sigs = hash.query(query);
        int step = (top + m - 1) / m;

        // binary search signature from sorted index.
        // the more similar the signatures, the better the search results.
        bucketerp.search(step, sigs, new IntConsumer() {
            @Override
            public void accept(final int key) {
                double dist = fun.distance(query, data[key]);
                queue.add(new IdxVal(key, dist));
                if (top < queue.size()) {
                    queue.poll();
                }
            }
        });

        List<IdxVal> result = new ArrayList<>();
        while (0 < queue.size()) {
            IdxVal w = queue.poll();
            result.add(w);
        }
        Collections.reverse(result);
        return result;
    }

}
