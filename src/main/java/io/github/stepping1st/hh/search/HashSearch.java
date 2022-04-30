package io.github.stepping1st.hh.search;


import io.github.stepping1st.hh.hash.Hash;
import io.github.stepping1st.hh.HashBucket;
import io.github.stepping1st.hh.IdxVal;
import io.github.stepping1st.hh.Dist;
import io.github.stepping1st.hh.Query;

import java.util.List;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.Collections;
import java.util.function.IntConsumer;


/**
 * Hash Neighbor Search.
 */
public class HashSearch implements Search<Query> {
    private final HashBucket buckets;
    private final Hash<double[], int[], int[]> hash;

    /**
     * @param hash    hash algorithm
     * @param data    data
     * @param buckets bucket
     */
    public HashSearch(final Hash<double[], int[], int[]> hash,
                      final double[][] data,
                      final HashBucket buckets) {
        this.hash = hash;
        this.buckets = buckets;
        int n = data.length;
        for (int i = 0; i < n; i++) {
            int[] sig = hash.data(data[i]);
            buckets.insert(i, sig);
        }
    }

    @Override
    public final List<IdxVal> nns(final Query param) {
        double[][] data = param.data();
        double[] query = param.query();
        int top = param.top();
        int limit = param.limit();

        int[] sig = hash.query(query);
        PriorityQueue<IdxVal> queue = new PriorityQueue<>(
                new Comparator<IdxVal>() {
                    @Override
                    public int compare(final IdxVal o1, final IdxVal o2) {
                        return -Double.compare(o1.value(), o2.value());
                    }
                });
        Dist fun = param.dist();
        buckets.search(sig, limit, new IntConsumer() {
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


