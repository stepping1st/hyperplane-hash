package io.github.stepping1st.hh;


import org.apache.commons.math.random.RandomData;

import java.io.Serializable;
import java.util.Arrays;


/**
 * Random Sampler.
 */
public class RandSampler implements Serializable {
    private final int dim;
    private final int afterdim;
    private final int sampledim;
    private final RandomData rd;

    /**
     * @param dim dimension
     * @param s   scale factor of dimension
     * @param rd  random dat
     */
    public RandSampler(final int dim, final int s, final RandomData rd) {
        this.dim = dim;
        this.afterdim = dim * (dim + 1) / 2 + 1;
        this.sampledim = dim * s;
        this.rd = rd;
    }

    /**
     * sampling vector from input.
     *
     * @param data input vector
     * @return sampled vector
     */
    public final IdxVal[] sampling(final double[] data) {
        IdxVal[] sample = new IdxVal[sampledim];
        // 1: calc probability vector and the l2-norm-square of data
        double[] prob = probabilityVector(dim, data);

        // 2: randomly sample coordinate of data
        // as the coordinate of sample data
        int cnt = 0;
        boolean[] checked = new boolean[afterdim];

        // 2.1: first consider the largest coordinate
        int sid = dim - 1;

        checked[sid] = true;
        sample[cnt] = new IdxVal(sid, Math.pow(data[sid], 2));
        ++cnt;

        // 2.2: consider the combination of the left coordinates
        for (int i = 1; i < sampledim; ++i) {
            int idx = searchIdxFrom(dim - 1, prob);
            int idy = searchIdxFrom(dim, prob);
            if (idx > idy) {
                int tmp = idx;
                idx = idy;
                idy = tmp;
            }

            if (idx == idy) {
                sid = idx;
                if (!checked[sid]) {
                    // calc the square coordinates of sample data
                    checked[sid] = true;
                    sample[cnt] = new IdxVal(sid, Math.pow(data[idx], 2));
                    ++cnt;
                }
            } else {
                sid = dim + (idx * dim - idx * (idx + 1) / 2) + (idy - idx - 1);
                if (!checked[sid]) {
                    // calc the differential coordinates of sampleData
                    checked[sid] = true;
                    sample[cnt] = new IdxVal(sid, data[idx] * data[idy]);
                    ++cnt;
                }
            }
        }
        return Arrays.copyOfRange(sample, 0, cnt);
    }

    /**
     * get probability vector.
     *
     * @param dim  data dimension
     * @param data input data
     * @return probability vector
     */
    private double[] probabilityVector(final int dim, final double[] data) {
        double[] prob = new double[dim];
        prob[0] = data[0] * data[0];
        for (int i = 1; i < dim; ++i) {
            prob[i] = prob[i - 1] + data[i] * data[i];
        }
        return prob;
    }

    /**
     * binary search index based on prob vector.
     *
     * @param prob probability vector
     * @return idx
     */
    private int searchIdxFrom(final int d, final double[] prob) {
        double end = prob[d - 1];
        assert 0 < end : String.format("must 0 < sigma(%f)", end);
        double rnd = rd.nextGaussian(0.0f, end);
        int idx = Arrays.binarySearch(prob, 0, d, rnd);
        if (0 <= idx) {
            return idx;
        } else {
            return Math.max(0, -(idx + 2));
        }
    }
}
