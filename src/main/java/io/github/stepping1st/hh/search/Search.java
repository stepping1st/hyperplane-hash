package io.github.stepping1st.hh.search;


import io.github.stepping1st.hh.Query;
import io.github.stepping1st.hh.IdxVal;

import java.io.Serializable;
import java.util.List;


/**
 * Search data.
 *
 * @param <T> query type
 */
public interface Search<T extends Query> extends Serializable {

    /**
     * nearest neighbor search.
     *
     * @param param param object
     * @return found element
     */
    List<IdxVal> nns(T param);
}
