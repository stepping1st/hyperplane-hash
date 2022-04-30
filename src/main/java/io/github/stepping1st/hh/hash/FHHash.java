package io.github.stepping1st.hh.hash;


import io.github.stepping1st.hh.RandSampler;
import org.apache.commons.math.random.RandomData;
import io.github.stepping1st.hh.IdxVal;

import java.util.Arrays;


/**
 * Furthest Hyperplane(FH) Hash.
 */
public class FHHash implements Hash<double[][], FHHash.Transform, IdxVal[]> {
    private final int fhdim;
    private final RandSampler sampler;

    /**
     * @param d  dimension
     * @param s  scale factor of dimension
     * @param rd random data
     */
    public FHHash(final int d, final int s, final RandomData rd) {
        fhdim = d * (d + 1) / 2 + 1;
        this.sampler = new RandSampler(d, s, rd);
    }

    /**
     * @return furthest hyperplane dimension
     */
    public final int fhdim() {
        return fhdim;
    }

    @Override
    public final Transform data(final double[][] data) {
        int n = data.length;

        // calc centroid, l2-norm, and max l2-norm
        double[] norm = new double[n];
        double[] centroid = new double[fhdim];
        IdxVal[][] samples = new IdxVal[n][];

        // calc l2-norm & update max l2-norm-sqr
        @SuppressWarnings("checkstyle:LocalVariableName")
        double M = Double.MIN_VALUE;
        for (int i = 0; i < n; ++i) {
            // calc sampleData with data transformation
            IdxVal[] sample = sampler.sampling(data[i]);
            double l2 = 0.0;
            for (IdxVal w : sample) {
                l2 += Math.pow(w.value(), 2);
                centroid[w.idx()] += w.value();
            }
            norm[i] = l2;
            samples[i] = sample;
            M = Math.max(M, norm[i]);
        }

        // calc centroid and its l2-norm-sqr
        double l2centroid = 0.0D;
        for (int i = 0; i < fhdim - 1; ++i) {
            centroid[i] /= n;
            l2centroid += Math.pow(centroid[i], 2);
        }
        double last = 0.0D;
        for (int i = 0; i < n; ++i) {
            norm[i] = Math.sqrt(M - norm[i]);
            last += norm[i];
        }
        last /= n;
        centroid[fhdim - 1] = last;
        l2centroid += Math.pow(last, 2);

        // determine shiftId after shifting data objects to centroid
        IdxVal[] arr = new IdxVal[n];
        for (int i = 0; i < n; ++i) {
            double val = calcTransformDist(
                    fhdim, norm[i], l2centroid, samples[i], centroid);
            arr[i] = new IdxVal(i, val);
        }
        Arrays.sort(arr);

        return new Transform(norm, centroid, samples, M, arr);
    }

    private double calcTransformDist(final int fhdim,
                                     final double last,
                                     final double l2centroid,
                                     final IdxVal[] sample,
                                     final double[] centroid) {
        double dist = l2centroid;

        // calc the distance for the sample dimension
        for (int i = 0; i < sample.length; ++i) {
            int idx = sample[i].idx();
            double tmp = centroid[idx];
            double diff = sample[i].value() - tmp;

            dist -= Math.pow(tmp, 2);
            dist += Math.pow(diff, 2);
        }
        // calc the distance for the last coordinate
        double tmp = centroid[fhdim - 1];
        dist -= Math.pow(tmp, 2);
        dist += Math.pow(last - tmp, 2);

        return Math.sqrt(dist);
    }

    @Override
    public final IdxVal[] query(final double[] query) {
        return sampler.sampling(query);
    }

    /**
     * Transformed data.
     */
    public static class Transform {
        private final double[] norm;
        private final double[] centroid;
        private final IdxVal[][] samples;
        private final IdxVal[] dist;
        @SuppressWarnings("checkstyle:MemberName")
        private final double M;

        /**
         * @return l2-norm of sample data.
         */
        public final double[] norm() {
            return norm;
        }

        /**
         * @return centroid of sample data.
         */
        public final double[] centroid() {
            return centroid;
        }

        /**
         * @return sample data.
         */
        public final IdxVal[][] samples() {
            return samples;
        }

        /**
         * @return distance of sample data.
         */
        public final IdxVal[] dist() {
            return dist;
        }

        /**
         * @return max l2-norm-sqr.
         */
        @SuppressWarnings("checkstyle:MethodName")
        public final double M() {
            return M;
        }

        /**
         * @param norm     l2-norm of sample data
         * @param centroid centroid of sample data
         * @param samples  sample data
         * @param M        max l2-norm-sqr
         * @param dist     distance of sample data
         */
        @SuppressWarnings("checkstyle:ParameterName")
        public Transform(final double[] norm, final double[] centroid,
                         final IdxVal[][] samples, final double M,
                         final IdxVal[] dist) {
            this.norm = norm;
            this.centroid = centroid;
            this.samples = samples;
            this.M = M;
            this.dist = dist;
        }
    }

}
