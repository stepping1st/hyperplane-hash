package io.github.stepping1st.hh;


/**
 * Furthest Hyperplane(FH) Query.
 */
public class FHQuery extends Query {
    private final int l;

    /**
     * @param query query
     * @param data  data
     * @param top   top n
     * @param limit candidate limit
     * @param l     separation threshold
     * @param dist  distance from query and data
     */
    public FHQuery(final double[] query, final double[][] data,
                   final int top, final int limit,
                   final int l, final Dist dist) {
        super(query, data, top, limit, dist);
        this.l = l;
    }

    /**
     * @return separation threshold
     */
    public final int l() {
        return l;
    }

    @Override
    public final FHQuery copy(final Dist dist) {
        return new FHQuery(query(), data(), top(), limit(), l, dist);
    }
}
