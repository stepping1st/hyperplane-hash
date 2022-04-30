package io.github.stepping1st.hh;


import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.apache.commons.math.random.RandomData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Arrays;


/**
 * Reverse Query-Aware LSH(RQALSH).
 */
public class RQALSH implements Serializable {
    private static final double CHECK_ERROR = 1e-6;
    private static final Logger LOGGER = LoggerFactory
            .getLogger(RQALSH.class.getName());
    private static final int SCAN_SIZE = Integer.parseInt(
            System.getenv().getOrDefault("rqalsh.search_size", "64")
    );
    private static final int MAX_SEARCH_SIZE = Integer.parseInt(
            System.getenv().getOrDefault("rqalsh.max_search_size", "100000")
    );
    private final int n;
    private final int dim;
    private final int m;
    private final int[] index;
    private final double[] a;
    private final IdxVal[] tables;

    /**
     * @param n     number of data
     * @param dim   dimension of data
     * @param m     number of hash tables
     * @param index index of data
     * @param norm  norm of data dim
     * @param data  index and weight of data
     * @param rd    random data
     */
    public RQALSH(final int n, final int dim, final int m,
                  final int[] index, final double[] norm,
                  final IdxVal[][] data, final RandomData rd) {
        this.n = n;
        this.dim = dim;
        this.m = m;
        this.index = index;

        // generate hash functions
        a = new double[m * dim];
        for (int i = 0; i < m * dim; ++i) {
            a[i] = rd.nextGaussian(0.0f, 1.0D);
        }

        // allocate space for tables
        tables = new IdxVal[m * n];

        for (int i = 0; i < n; ++i) {
            // calc the hash values of P*f(o)
            int idx = index[i];
            for (int j = 0; j < m; ++j) {
                IdxVal[] w = data[idx];
                double val = calcHashValue(w.length, j, norm[idx], w);
                tables[j * n + i] = new IdxVal(i, val);
            }
        }

        // sort hash tables in ascending order of hash values
        for (int i = 0; i < m; ++i) {
            int start = i * n;
            int end = start + n;
            Arrays.sort(tables, start, end);
        }
    }

    /**
     * furthest neighbor search.
     *
     * @param l         separation threshold
     * @param limit     candidates limit
     * @param R         limited search range.
     *                  if it finds a candidate with
     *                  {@literal dist(hash(query) - hash(data)) < range},
     *                  the search is stopped.
     * @param sampledim sample dimension
     * @param query     query object
     * @return candidates
     */
    @SuppressWarnings("checkstyle:ParameterName")
    public final IntList fns(final int l, final int limit, final double R,
                             final int sampledim, final IdxVal[] query) {
        // simply check all data if #candidates is equal to the cardinality
        if (n <= limit) {
            IntList cands = new IntArrayList();
            for (int i = 0; i < n; ++i) {
                if (index != null && -1 < index[i]) {
                    cands.add(index[i]);
                } else {
                    cands.add(i);
                }
            }
            return cands;
        }

        // dynamic separation counting
        SearchPosition pos = getSearchPosition(sampledim, query);
        return dynamicSeparationCounting(l, limit, R, pos);
    }

    /**
     * dynamic separation counting.
     *
     * @param l     separation threshold
     * @param limit candidates limit
     * @param R     limited search range
     */
    @SuppressWarnings("checkstyle:ParameterName")
    private IntList dynamicSeparationCounting(final int l,
                                              final int limit,
                                              final double R,
                                              final SearchPosition position) {
        // grid width
        double w = 1.0D;
        // search radius
        double radius = findRadius(w, position);
        // bucket width
        double width = radius * w / 2.0D;
        // limited search range
        @SuppressWarnings("checkstyle:AvoidInlineConditionals")
        double range = R < CHECK_ERROR ? 0.0D : R * w / 2.0D;
        CountParam param = new CountParam(n, m, radius, width);

        while (param.more) {
            // step 1: initialization
            param.bucket = 0;
            Arrays.fill(param.bucketflag, true);

            fnSearch(position, param, l, limit, range);
            // step 3: stop condition
            if (m <= param.range || limit <= param.cands.size()) {
                break;
            }
            // step 4: update radius
            param.radius = param.radius / 2.0D;
            param.width = param.radius * w / 2.0D;
        }

        return param.cands;
    }

    /**
     * step 2: (R,c)-FN search.
     *
     * @param pos   hash position param
     * @param param furthest nearest search param
     * @param l     separation threshold
     * @param limit candidates limit
     * @param range search range
     */
    private void fnSearch(final SearchPosition pos, final CountParam param,
                          final int l, final int limit, final double range) {
        for (int num = 0; param.more(limit); num++) {
            if (MAX_SEARCH_SIZE < num) {
                LOGGER.warn("MAX_SEARCH{} < limit{}", MAX_SEARCH_SIZE, num);
                param.more = false;
                return;
            }
            int j = num % m;

            // CANNOT add !rangeFlag[j] as condition, because the
            // rangeFlag[j] for large radius will affect small radius
            if (!param.bucketflag[j]) {
                continue;
            }

            int start = j * n;
            double queryval = pos.queryval[j];
            double ldist = -1.0D, rdist = -1.0D;

            // step 2.1: scan left part of bucket
            int lpos = pos.leftpos[j], rpos = pos.rightpos[j];
            for (int cnt = 0; cnt < SCAN_SIZE; cnt++, lpos++) {
                ldist = Float.MAX_VALUE;
                if (lpos < rpos) {
                    ldist = Math.abs(queryval - tables[start + lpos].value());
                } else {
                    break;
                }
                if (ldist < param.width || ldist < range) {
                    break;
                }

                int id = tables[start + lpos].idx();
                if (++param.freq[id] == l) {
                    if (index != null && -1 < index[id]) {
                        param.cands.add(index[id]);
                    } else {
                        param.cands.add(id);
                    }
                    if (limit <= param.cands.size()) {
                        return;
                    }
                }
            }
            pos.leftpos[j] = lpos;

            // step 2.2: scan right part of bucket
            for (int cnt = 0; cnt < SCAN_SIZE; cnt++, rpos--) {
                rdist = Float.MAX_VALUE;
                if (lpos < rpos) {
                    rdist = Math.abs(queryval - tables[start + rpos].value());
                } else {
                    break;
                }
                if (rdist < param.width || rdist < range) {
                    break;
                }

                int id = tables[start + rpos].idx();
                if (++param.freq[id] == l) {
                    if (index != null && -1 < index[id]) {
                        param.cands.add(index[id]);
                    } else {
                        param.cands.add(id);
                    }
                    if (limit <= param.cands.size()) {
                        return;
                    }
                }
            }
            pos.rightpos[j] = rpos;

            // step 2.3: check whether this bucket is finished scanned
            if (rpos <= lpos || (ldist < param.width && rdist < param.width)) {
                if (param.bucketflag[j]) {
                    param.bucketflag[j] = false;
                    ++param.bucket;
                }
            }
            if (rpos <= lpos || (ldist < range && rdist < range)) {
                if (param.bucketflag[j]) {
                    param.bucketflag[j] = false;
                    ++param.bucket;
                }
                if (param.rangeflag[j]) {
                    param.rangeflag[j] = false;
                    ++param.range;
                }
            }
        }
    }

    /**
     * calculate hash value.
     *
     * @param tid  hash table id
     * @param data input data
     * @return hash value
     */
    private double calcHashValue(final int tid, final double[] data) {
        return Op.dot(data, a, tid * dim);
    }

    /**
     * calculate hash value.
     *
     * @param d    dimension for calc hash value
     * @param tid  hash table id
     * @param last the last coordinate of input data
     * @param data input data
     * @return hash value
     */
    private double calcHashValue(final int d, final int tid,
                                 final double last, final IdxVal[] data) {
        int start = tid * dim;
        float val = 0.0f;
        for (int i = 0; i < d; ++i) {
            int idx = data[i].idx();
            val += a[start + idx] * data[i].value();
        }
        return val + a[start + dim - 1] * last;
    }

    /**
     * calculate hash value.
     *
     * @param d    dimension for calc hash value
     * @param tid  hash table id
     * @param data input data
     * @return hash value
     */
    private double calcHashValue(final int d, final int tid,
                                 final IdxVal[] data) {
        int start = tid * dim;
        float val = 0.0f;
        for (int i = 0; i < d; ++i) {
            int idx = data[i].idx();
            val += a[start + idx] * data[i].value();
        }
        return val;
    }

    /**
     * hash positions parameters.
     *
     * @param sampledim sample dimension
     * @param query     query object
     * @return hash positions param
     */
    private SearchPosition getSearchPosition(final int sampledim,
                                             final IdxVal[] query) {
        double[] queryval = new double[m];
        int[] leftpos = new int[m], rightpos = new int[m];
        for (int i = 0; i < m; ++i) {
            queryval[i] = calcHashValue(sampledim, i, 0.0F, query);
            leftpos[i] = 0;
            rightpos[i] = n - 1;
        }
        return new SearchPosition(queryval, leftpos, rightpos);
    }

    /**
     * find proper radius.
     *
     * @param w grid width
     * @return radius
     */
    private double findRadius(final double w,
                              final SearchPosition position) {
        // find projected distance closest to the query in each hash tables
        double[] arr = new double[m * 2];
        int num = 0;
        for (int i = 0; i < m; ++i) {
            int lpos = position.leftpos[i];
            int rpos = position.rightpos[i];
            if (lpos < rpos) {
                double queryval = position.queryval[i];
                arr[num++] = Math.abs(tables[i * n + lpos].value() - queryval);
                arr[num++] = Math.abs(tables[i * n + rpos].value() - queryval);
            }
        }
        Arrays.sort(arr, 0, num);

        // find the median distance and return the new radius
        double dist = -1.0D;
        if (num % 2 == 0) {
            dist = (arr[num / 2 - 1] + arr[num / 2]) / 2.0D;
        } else {
            dist = arr[num / 2];
        }

        int kappa = (int) Math.ceil(Math.log(2.0D * dist / w) / Math.log(2.0D));
        return Math.pow(2.0, kappa);
    }

    /**
     * Search Position.
     */
    private static final class SearchPosition {
        /**
         * m hash values of query.
         */
        private final double[] queryval;

        /**
         * left positions for m hash tables.
         */
        private final int[] leftpos;

        /**
         * right positions for m hash tables.
         */
        private final int[] rightpos;

        /**
         * @param queryval m hash values of query
         * @param leftpos  left positions for m hash tables
         * @param rightpos right positions for m hash tables
         */
        private SearchPosition(final double[] queryval,
                               final int[] leftpos,
                               final int[] rightpos) {
            this.queryval = queryval;
            this.leftpos = leftpos;
            this.rightpos = rightpos;
        }
    }

    /**
     * Count Parameter.
     */
    private static final class CountParam {
        private final int m;

        /**
         * separation frequency for n data points.
         */
        private final int[] freq;

        /**
         * range flag for m hash tables.
         */
        private final boolean[] rangeflag;

        /**
         * bucket flag for m hash tables.
         */
        private final boolean[] bucketflag;

        /**
         * number of search range flag.
         **/
        private int range = 0;

        /**
         * number of bucket.
         */
        private int bucket = 0;

        /**
         * search more.
         */
        private boolean more = true;

        /**
         * search radius.
         */
        private double radius;

        /**
         * bucket width.
         */
        private double width;
        private final IntList cands = new IntArrayList();

        /**
         * @param n      number of data
         * @param m      number of hash tables
         * @param radius search radius
         * @param width  bucket width
         */
        private CountParam(final int n, final int m,
                           final double radius, final double width) {
            freq = new int[n];
            this.m = m;
            rangeflag = new boolean[m];
            bucketflag = new boolean[m];
            Arrays.fill(rangeflag, true);
            this.radius = radius;
            this.width = width;
        }

        /**
         * @param limit candidate limit
         * @return has more condition
         */
        private boolean more(final int limit) {
            return bucket < m && range < m && cands.size() < limit;
        }
    }

}
