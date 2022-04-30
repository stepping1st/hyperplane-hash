package io.github.stepping1st.hh.hash;


import org.apache.commons.math.random.RandomData;


/**
 * Embedding Hyperplane(EH) Hash.
 */
public class EHHash implements Hash<double[], int[], int[]> {
    private final double[] randv;
    private final int m;
    private final int l;

    /**
     * @param d  data dim
     * @param m  single hasher of the compond hasher
     * @param l  hash tables
     * @param rd random data
     */
    public EHHash(final int d, final int m, final int l, final RandomData rd) {
        this.m = m;
        this.l = l;

        int size = m * l * d * d;
        randv = new double[size];
        for (int i = 0; i < size; i++) {
            randv[i] = rd.nextGaussian(0.0f, 1.0f);
        }
    }

    @Override
    public final int[] data(final double[] data) {
        assert randv.length == m * l * data.length * data.length;
        int[] sigs = new int[l];
        for (int i = 0; i < l; ++i) {
            int sig = 0;
            for (int j = 0; j < m; ++j) {
                int pos = (i * m + j) * data.length * data.length;
                double val = hash(data, pos);
                int sign = boolToInt(0 < val);
                sig = (sig << 1) | sign;
            }
            sigs[i] = sig;
        }
        return sigs;
    }

    @Override
    public final int[] query(final double[] query) {
        assert randv.length == m * l * query.length * query.length;
        int[] sigs = new int[l];
        for (int i = 0; i < l; ++i) {
            int sig = 0;
            for (int j = 0; j < m; ++j) {
                int pos = (i * m + j) * query.length * query.length;
                double val = hash(query, pos);
                int sign = boolToInt(!(0 < val));
                sig = (sig << 1) | sign;
            }
            sigs[i] = sig;
        }
        return sigs;
    }

    private double hash(final double[] data, final int pos) {
        double val = 0.0D;
        for (int d1 = 0; d1 < data.length; ++d1) {
            for (int d2 = 0; d2 < data.length; ++d2) {
                val += data[d1] * data[d2] * randv[pos + d1 * data.length + d2];
            }
        }
        return val;
    }

    private int boolToInt(final boolean sign) {
        if (sign) {
            return 1;
        } else {
            return 0;
        }
    }

}
