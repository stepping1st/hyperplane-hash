package io.github.stepping1st.hh;


import java.util.Arrays;


/**
 * Int type n-dim array.
 */
public class IntNDArray implements NDArray<int[]> {
    private final int[] dims;
    private final int[] dimlens;
    private final int[] buffer;

    /**
     * @param dims dim array
     */
    public IntNDArray(final int[] dims) {
        int d = dims.length;
        int[] dimlens = new int[d + 1];
        dimlens[d] = 1;
        for (int i = d - 1; 0 <= i; --i) {
            dimlens[i] = dimlens[i + 1] * dims[i];
        }
        this.dimlens = dimlens;
        this.buffer = new int[dimlens[0]];
        this.dims = dims;
    }

    /**
     * @param value set value
     * @param idxes set dim
     */
    public final void set(final int value, final int... idxes) {
        assert dims.length == idxes.length;
        int idx = 0;
        int d = dimlens.length - 1;
        for (int i = 0; i < d; i++) {
            idx = idx + dimlens[i + 1] * idxes[i];
        }
        buffer[idx] = value;
    }

    @Override
    public final int[] values() {
        return buffer;
    }

    @Override
    public final int[] value(final int... idxes) {
        assert idxes.length <= dims.length - 1;
        int start = 0;
        int d = idxes.length;
        for (int i = 0; i < d; i++) {
            start = start + dimlens[i + 1] * idxes[i];
        }
        int end = start + dimlens[d];
        return Arrays.copyOfRange(buffer, start, end);
    }

    @Override
    public final int[] dims() {
        return dims;
    }

}
