package io.github.stepping1st.hh.search;


import it.unimi.dsi.fastutil.ints.IntList;
import org.apache.commons.math.random.RandomData;
import io.github.stepping1st.hh.hash.FHHash;
import io.github.stepping1st.hh.FHQuery;
import io.github.stepping1st.hh.IdxVal;
import io.github.stepping1st.hh.RQALSH;
import io.github.stepping1st.hh.Dist;

import java.util.List;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;


/**
 * Furthest Hyperplane(FH) Neighbor Search.
 */
public class FHSearch implements Search<FHQuery> {
    @SuppressWarnings("checkstyle:MemberName")
    private final double M;
    private final List<RQALSH> hashs;
    private final FHHash hash;
    private static final int MAX_BLOCK_NUM = Integer.parseInt(
            System.getenv().getOrDefault("fh_search.max_block_num", "25000")
    );

    /**
     * @param fhhash furthest hash
     * @param b      interval ratio
     * @param m      number of hash tables
     * @param data   input data
     * @param rd     random data
     */
    public FHSearch(final FHHash fhhash, final double b, final int m,
                    final double[][] data, final RandomData rd) {
        this.hashs = new ArrayList<RQALSH>();
        this.hash = fhhash;

        int n = data.length;
        int fhdim = fhhash.fhdim();
        FHHash.Transform norm = fhhash.data(data);
        this.M = norm.M();

        IdxVal[] dists = norm.dist();
        int[] index = new int[n];
        for (int i = 0; i < n; ++i) {
            index[i] = dists[i].idx();
        }

        //  divide datasets into blocks and build hash tables for each block
        int start = 0;
        while (start < n) {
            // partition block
            double minradius = b * dists[start].value();
            int block = start, cnt = 0;
            while (block < n && minradius < dists[block].value()) {
                ++block;
                if (MAX_BLOCK_NUM <= ++cnt) {
                    break;
                }
            }
            // add block
            int[] hashidx = Arrays.copyOfRange(index, start, index.length);

            // hash sampleData into m bucketing.
            // and sort table value by hash value
            RQALSH hash = new RQALSH(
                    cnt, fhdim, m, hashidx, norm.norm(), norm.samples(), rd);
            this.hashs.add(hash);
            start += cnt;
        }
        assert (start == n);
    }

    @Override
    public final List<IdxVal> nns(final FHQuery param) {
        double[][] data = param.data();
        double[] query = param.query();
        int l = param.l();
        int top = param.top();

        IdxVal[] sample = getSampleQuery(query);

        // point-to-hyperplane NNS
        int limit = param.limit() + top - 1;
        double fixval = 2 * M;
        PriorityQueue<IdxVal> queue = new PriorityQueue<>(
                new Comparator<IdxVal>() {
                    @Override
                    public int compare(final IdxVal o1, final IdxVal o2) {
                        return -Double.compare(o1.value(), o2.value());
                    }
                });
        Dist fun = param.dist();

        for (RQALSH hash : this.hashs) {
            // check candidates returned by rqalsh
            double kfndist = -1.0D;
            if (top <= queue.size()) {
                double kdist = queue.peek().value();
                kfndist = Math.sqrt(fixval - 2 * kdist * kdist);
            }
            // scan range search by distance between query and data
            IntList list = hash.fns(l, limit,
                    kfndist, sample.length, sample);
            for (int idx : list) {
                double dist = fun.distance(query, data[idx]);
                queue.add(new IdxVal(idx, dist));
                if (top < queue.size()) {
                    queue.poll();
                }
            }
            int size = list.size();
            limit -= size;
            if (limit <= 0) {
                break;
            }
        }

        List<IdxVal> result = new ArrayList<>();
        while (0 < queue.size()) {
            IdxVal w = queue.poll();
            result.add(w);
        }
        Collections.reverse(result);
        return result;
    }

    private IdxVal[] getSampleQuery(final double[] query) {
        // calc sampleQuery with query transformation
        IdxVal[] sample = hash.query(query);
        double norm = norm(sample);

        // multiply lambda
        double lambda = Math.sqrt(M / norm);
        for (int i = 0; i < sample.length; ++i) {
            IdxVal w = sample[i];
            sample[i] = new IdxVal(w.idx(), w.value() * lambda);
        }
        return sample;
    }

    /**
     * l2-norm-sqr of f(o).
     */
    private double norm(final IdxVal[] idxvals) {
        double norm = 0.0;
        for (IdxVal w : idxvals) {
            norm += Math.pow(w.value(), 2);
        }
        return norm;
    }
}
