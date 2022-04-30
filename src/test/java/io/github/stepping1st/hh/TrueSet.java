package io.github.stepping1st.hh;

import java.util.Arrays;


public class TrueSet {
    private final Query query;
    private final IdxVal[] idxes;

    public TrueSet(Query query, IdxVal[] idxes) {
        this.query = query;
        this.idxes = idxes;
    }

    public static TrueSet of(Query query) {
        double[][] data = query.data();
        IdxVal[] idxes = new IdxVal[data.length];
        Dist dist = query.dist();
        for (int i = 0; i < data.length; i++) {
            idxes[i] = new IdxVal(i, dist.distance(query.query(), data[i]));
        }
        Arrays.sort(idxes);
        return new TrueSet(query, idxes);
    }

    public TrueSet slice(int size) {
        int end = Math.min(size, idxes.length);
        return new TrueSet(query, Arrays.copyOf(idxes, end));
    }

    public Query query() {
        return query;
    }

    public IdxVal[] idxes() {
        return idxes;
    }

    public double[][] data() {
        double[][] data = query.data();
        double[][] result = new double[idxes.length][];
        for (int i = 0; i < idxes.length; i++) {
            result[i] = data[idxes[i].idx()];
        }
        return result;
    }

}
