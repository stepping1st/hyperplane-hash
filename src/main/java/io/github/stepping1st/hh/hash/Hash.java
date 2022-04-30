package io.github.stepping1st.hh.hash;


import java.io.Serializable;


/**
 * Hash Algorithm.
 *
 * @param <INPUT> input type
 * @param <DATA>  data type
 * @param <QUERY> query type
 */
public interface Hash<INPUT, DATA, QUERY> extends Serializable {
    /**
     * hashing input.
     *
     * @param input input data
     * @return hashed data
     */
    DATA data(INPUT input);

    /**
     * hashing query data.
     *
     * @param query query vector
     * @return hashed query
     */
    QUERY query(double[] query);

}
