package io.github.stepping1st.hh;


import java.io.Serializable;


/**
 * n-dimension array.
 *
 * @param <T> object type
 */
public interface NDArray<T> extends Serializable {
    /**
     * @return dimension array
     */
    int[] dims();

    /**
     * @return array values
     */
    T values();

    /**
     * @param idxes dim index
     * @return array value
     */
    T value(int... idxes);
}
