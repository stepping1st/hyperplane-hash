package io.github.stepping1st.hh.hash;


import io.github.stepping1st.hh.RandSampler;
import org.apache.commons.math.random.RandomData;
import io.github.stepping1st.hh.IdxVal;


/**
 * Nearest Hyperplane(NH) Hash.
 */
public class NHHash implements Hash<double[][], int[][], int[]> {
    private final RandSampler sampler;
    private final int nhdim;
    private final int m;
    private final double w;
    private final double[] proja;
    private final double[] projb;

    /**
     * @param d  dimension
     * @param m  single hasher of the compond hasher
     * @param s  scale factor of dimension
     * @param w  bucket width
     * @param rd random data
     */
    public NHHash(final int d, final int m, final int s,
                  final double w, final RandomData rd) {
        this.m = m;
        this.w = w;
        this.nhdim = d * (d + 1) / 2 + 1;
        this.sampler = new RandSampler(d, s, rd);

        // sample random projection variables
        int projsize = m * nhdim;
        proja = new double[projsize];
        for (int i = 0; i < projsize; i++) {
            proja[i] = rd.nextGaussian(0.0f, 1.0f);
        }
        projb = new double[m];
        for (int i = 0; i < m; i++) {
            projb[i] = rd.nextGaussian(0.0f, 1.0f);
        }
    }

    /**
     * get signature of data.
     *
     * @param data input data object o
     */
    private Signature samplingSignature(final double[] data) {
        double[] projs = new double[m];
        // calc sample with data transformation
        IdxVal[] sample = sampler.sampling(data);
        // calc the signature of sampleData
        for (int i = 0; i < m; ++i) {
            int start = i * nhdim;
            double val = 0.0;
            for (IdxVal w : sample) {
                val += proja[start + w.idx()] * w.value();
            }
            projs[i] = val;
        }
        return new Signature(projs, norm(sample));
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

    @Override
    public final int[][] data(final double[][] data) {
        // projected data sampling and normalize
        int n = data.length;
        double m = Double.MIN_VALUE;
        Signature[] sample = new Signature[n];

        // calc sample hash values
        for (int i = 0; i < n; ++i) {
            Signature sig = samplingSignature(data[i]);
            sample[i] = sig;
            m = Math.max(sig.norm, m);
        }

        int[][] sigs = new int[n][];
        for (int i = 0; i < n; ++i) {
            Signature sampled = sample[i];
            double lastcoord = Math.sqrt(m - sampled.norm);
            double[] proj = sampled.value;
            int[] sig = new int[this.m];
            for (int j = 0; j < this.m; ++j) {
                double val = proj[j] + lastcoord * proja[(j + 1) * nhdim - 1];
                double v = (val + projb[j]) / w;
                sig[j] = (int) v;
            }
            sigs[i] = sig;
        }
        return sigs;
    }

    @Override
    public final int[] query(final double[] query) {
        int[] sig = new int[m];
        // calc sample with query transformation
        IdxVal[] sample = sampler.sampling(query);

        // calc the signature of sample_data
        for (int i = 0; i < m; ++i) {
            double val = 0.0D;
            for (int j = 0; j < sample.length; ++j) {
                int idx = sample[j].idx();
                val += proja[i * nhdim + idx] * sample[j].value();
            }
            sig[i] = (int) ((val + projb[i]) / w);
        }
        return sig;
    }

    /**
     * Signature of data.
     */
    private static final class Signature {
        /**
         * projection values.
         */
        private final double[] value;

        /**
         * l2-norm-sqr of f(o).
         */
        private final double norm;

        /**
         * @param value projection values
         * @param norm  l2-norm-sqr of f(o)
         */
        private Signature(final double[] value, final double norm) {
            this.value = value;
            this.norm = norm;
        }
    }
}
