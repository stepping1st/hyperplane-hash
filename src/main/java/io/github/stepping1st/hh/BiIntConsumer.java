package io.github.stepping1st.hh;


import java.io.Serializable;


/**
 * Represents an operation that accepts two input arguments and returns no
 * result.
 */
public interface BiIntConsumer extends Serializable {
    /**
     * Performs this operation on the given argument.
     *
     * @param a the input argument
     * @param b the input argument
     */
    void accept(int a, int b);
}
