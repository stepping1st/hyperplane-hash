package io.github.stepping1st.hh;


import java.io.Serializable;


/**
 * Index and Value.
 */
public class IdxVal implements Comparable<IdxVal>, Serializable {
    private final int idx;
    private final double value;

    /**
     * @param idx   index
     * @param value weight value
     */
    public IdxVal(final int idx, final double value) {
        this.idx = idx;
        this.value = value;
    }

    /**
     * @return index
     */
    public final int idx() {
        return idx;
    }

    /**
     * @return weight value
     */
    public final double value() {
        return value;
    }

    @Override
    public final int compareTo(final IdxVal o) {
        int cmp = Double.compare(value, o.value);
        if (cmp == 0) {
            return Integer.compare(idx, o.idx);
        } else {
            return cmp;
        }
    }

    @Override
    public final String toString() {
        return "IdxVal{"
                + "idx=" + idx
                + ", value=" + value
                + '}';
    }
}
