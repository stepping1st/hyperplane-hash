package io.github.stepping1st.hh;


/**
 * Distance Extractor.
 */
public interface Dist {
    /**
     * @param query query vector
     * @param data  data vector
     * @return distance from query and data
     */
    double distance(double[] query, double[] data);

    /**
     * simplification value of DP2H.
     */
    Dist ABS_DOT = new Dist() {
        @Override
        public double distance(final double[] query, final double[] data) {
            return Math.abs(Op.dot(query, data));
        }
    };

    /**
     * cosine distance.
     */
    Dist COS = new Dist() {
        @Override
        public double distance(final double[] query, final double[] data) {
            return 1 - Op.dot(query, data) / (Op.norm(query) * Op.norm(data));
        }
    };

    /**
     * distance from a data point p to the hyperplane query q.
     * DP2H aim to find the k closest data points to the hyperplane query.
     */
    Dist DP2H = new Dist() {
        @Override
        public double distance(final double[] query, final double[] data) {
            int last = query.length - 1;
            return Math.abs(query[last] + Op.dot(data, query, last))
                    / Math.sqrt(Op.dot(query, query, last));
        }
    };

    /**
     * @param value name of Dist
     * @return return Dist implement
     */
    static Dist valueOf(String value) {
        if ("ABS_DOT".equals(value)) {
            return Dist.ABS_DOT;
        } else if ("COS".equals(value)) {
            return Dist.COS;
        } else {
            return Dist.DP2H;
        }
    }
}
