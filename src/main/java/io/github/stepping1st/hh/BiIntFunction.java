package io.github.stepping1st.hh;


import java.io.Serializable;


/**
 * Represents an operation that accepts two input arguments and returns
 * result.
 *
 * @param <T> return type
 */
public interface BiIntFunction<T> extends Serializable {
    /**
     * return T from the given argument.
     *
     * @param a the input argument
     * @param b the input argument
     * @return T
     */
    T apply(int a, int b);
}
