package io.github.stepping1st.hh;


/**
 * Operation utils.
 */
public final class Op {

    private Op() {
    }

    /**
     * dot product a and b.
     *
     * @param a   input vector
     * @param b   input vector
     * @param dim dimension
     * @return dot product value
     */
    public static double dot(final double[] a,
                             final double[] b,
                             final int dim) {
        double v = 0;
        for (int i = 0; i < dim; i++) {
            v += a[i] * b[i];
        }
        return v;
    }

    /**
     * dot product a and b.
     *
     * @param a     input vector
     * @param start start of b
     * @param b     input vector
     * @return dot product value
     */
    public static double dot(final double[] a,
                             final int start,
                             final double[] b) {
        double v = 0;
        for (int i = 0; i < a.length; i++) {
            v += a[i] * b[i + start];
        }
        return v;
    }


    /**
     * dot product a and b.
     *
     * @param a input vector
     * @param b input vector
     * @return dot product value
     */
    public static double dot(final double[] a, final double[] b) {
        assert a.length == b.length;
        double v = 0;
        for (int i = 0; i < a.length; i++) {
            v += a[i] * b[i];
        }
        return v;
    }

    /**
     * normalize vector.
     *
     * @param v input vector
     * @return nomalized value
     */
    public static double norm(final double[] v) {
        return Math.sqrt(dot(v, v));
    }

    /**
     * concat two vector.
     *
     * @param data input vector
     * @param dim  right appended vector
     * @return combined vector
     */
    public static double[][] concat(final double[][] data, final double[] dim) {
        int d = dim(data);
        assert data.length == dim.length;
        double[][] result = new double[data.length][d + 1];
        for (int i = 0; i < result.length; i++) {
            double[] row = data[i];
            System.arraycopy(row, 0, result[i], 0, row.length);
            result[i][d] = dim[i];
        }
        return result;
    }

    /**
     * fill fixed size array with value.
     *
     * @param size  size of array
     * @param value fill value
     * @return array
     */
    public static double[] fill(final int size, final double value) {
        double[] result = new double[size];
        for (int i = 0; i < size; i++) {
            result[i] = value;
        }
        return result;
    }

    /**
     * shift the data by the center and find the max l2-norm to the center.
     *
     * @param data input vector
     * @return normalized data
     */
    public static double[][] normalize(final double[][] data) {
        int dim = dim(data);
        double[][] result = new double[data.length][dim];
        double[] mins = new double[dim];
        double[] maxs = new double[dim];
        for (int d = 0; d < dim; d++) {
            double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
            for (double[] ds : data) {
                double v = ds[d];
                min = Math.min(v, min);
                max = Math.max(v, max);
            }
            mins[d] = min;
            maxs[d] = max;
        }
        for (int i = 0; i < data.length; i++) {
            double sumnorm = 0.0F;
            for (int d = 0; d < dim; d++) {
                double center = (mins[d] + maxs[d]) / 2D;
                double v = data[i][d];
                double diff = v - center;
                result[i][d] = diff;
                sumnorm += Math.pow(diff, 2);
            }
            double norm = Math.sqrt(sumnorm);
            for (int d = 0; d < dim; d++) {
                result[i][d] /= norm;
            }
        }
        return result;
    }

    private static int dim(final double[][] data) {
        if (0 < data.length) {
            return data[0].length;
        } else {
            return 0;
        }
    }
}
