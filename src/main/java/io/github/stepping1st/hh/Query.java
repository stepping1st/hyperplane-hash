package io.github.stepping1st.hh;


import java.io.Serializable;


/**
 * Parameter query for {@link io.github.stepping1st.hh.search.Search}.
 */
public class Query implements Serializable {
    private final double[] query;
    private final double[][] data;
    private final int top;
    private final int limit;
    private final Dist dist;

    /**
     * @param query query vector
     * @param data  data vector
     * @param top   top n
     * @param limit candidate limit
     * @param dist  distance function
     */
    public Query(final double[] query, final double[][] data,
                 final int top, final int limit,
                 final Dist dist) {
        this.query = query;
        this.data = data;
        this.top = top;
        this.limit = limit;
        this.dist = dist;
    }

    /**
     * @return query query vector
     */
    public final double[] query() {
        return query;
    }

    /**
     * @return data vector
     */
    public final double[][] data() {
        return data;
    }

    /**
     * @return top n size
     */
    public final int top() {
        return top;
    }

    /**
     * @return limit search limit size
     */
    public final int limit() {
        return limit;
    }

    /**
     * @return dist from query and data
     */
    public final Dist dist() {
        return dist;
    }

    /**
     * @param dist distance function
     * @return copy query object
     */
    @SuppressWarnings("checkstyle:DesignForExtension")
    public Query copy(final Dist dist) {
        return new Query(query, data, top, limit, dist);
    }
}
