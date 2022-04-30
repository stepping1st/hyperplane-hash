package io.github.stepping1st.hh.hash;


import org.apache.commons.math.random.RandomData;
import io.github.stepping1st.hh.Op;


/**
 * Multilinear Hyperplane(MH) Hash.
 */
public class MHHash implements Hash<double[], int[], int[]> {
    private final double[] randv;
    @SuppressWarnings("checkstyle:MemberName")
    private final int M;
    private final int m;
    private final int l;

    /**
     * @param dim data dim
     * @param m   single hasher of the compond hasher
     * @param l   hash tables
     * @param M   proj vector used for a single hasher
     * @param rd  random data
     */
    @SuppressWarnings("checkstyle:ParameterName")
    public MHHash(final int dim, final int m, final int l,
                  final int M, final RandomData rd) {
        this.m = m;
        this.l = l;
        this.M = M;

        int size = m * l * M * dim;
        randv = new double[size];
        for (int i = 0; i < size; i++) {
            randv[i] = rd.nextGaussian(0.0f, 1.0f);
        }
    }

    @Override
    public final int[] data(final double[] data) {
        assert randv.length == m * l * M * data.length;
        int[] sigs = new int[l];
        for (int i = 0; i < l; ++i) {
            int sig = 0;
            for (int j = 0; j < m; ++j) {
                int pos = (i * m + j) * M * data.length;
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
        assert randv.length == m * l * M * query.length;
        int[] sigs = new int[l];
        for (int i = 0; i < l; ++i) {
            int sig = 0;
            for (int j = 0; j < m; ++j) {
                int pos = (i * m + j) * M * query.length;
                double val = hash(query, pos);
                int sign = boolToInt(!(0 < val));
                sig = (sig << 1) | sign;
            }
            sigs[i] = sig;
        }
        return sigs;
    }

    private double hash(final double[] query, final int pos) {
        double val = 1D;
        for (int k = 0; k < M; ++k) {
            val *= Op.dot(query, pos + k * query.length, randv);
        }
        return val;
    }

    private int boolToInt(final boolean bool) {
        if (bool) {
            return 1;
        } else {
            return 0;
        }
    }

}
