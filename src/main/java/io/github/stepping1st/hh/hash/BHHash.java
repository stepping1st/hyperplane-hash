package io.github.stepping1st.hh.hash;


import org.apache.commons.math.random.RandomData;
import io.github.stepping1st.hh.Op;


/**
 * Bilinear Hyperplane(BH) Hash.
 */
public class BHHash implements Hash<double[], int[], int[]> {
    private final double[] randu;
    private final double[] randv;
    private final int m;
    private final int l;

    /**
     * @param d  data dim
     * @param m  single hasher of the compond hasher
     * @param l  hash tables
     * @param rd random data
     */
    public BHHash(final int d, final int m, final int l, final RandomData rd) {
        this.m = m;
        this.l = l;

        int size = m * l * d;
        randu = new double[size];
        randv = new double[size];
        for (int i = 0; i < size; i++) {
            randu[i] = rd.nextGaussian(0.0f, 1.0f);
            randv[i] = rd.nextGaussian(0.0f, 1.0f);
        }
    }

    @Override
    public final int[] data(final double[] data) {
        assert randu.length == m * l * data.length;
        int[] sigs = new int[l];
        for (int i = 0; i < l; ++i) {
            int sig = 0;
            for (int j = 0; j < m; ++j) {
                int pos = (i * m + j) * data.length;
                double val1 = Op.dot(data, pos, randu);
                double val2 = Op.dot(data, pos, randv);
                int sign = boolToInt(0 < val1 * val2);
                sig = (sig << 1) | sign;
            }
            sigs[i] = sig;
        }
        return sigs;
    }

    @Override
    public final int[] query(final double[] query) {
        assert randu.length == m * l * query.length;
        int[] sigs = new int[l];
        for (int i = 0; i < l; ++i) {
            int sig = 0;
            for (int j = 0; j < m; ++j) {
                int pos = (i * m + j) * query.length;
                double val1 = Op.dot(query, pos, randu);
                double val2 = Op.dot(query, pos, randv);
                int sign = boolToInt(!(0 < val1 * val2));
                sig = (sig << 1) | sign;
            }
            sigs[i] = sig;
        }
        return sigs;
    }

    private int boolToInt(final boolean sign) {
        if (sign) {
            return 1;
        } else {
            return 0;
        }
    }

}
