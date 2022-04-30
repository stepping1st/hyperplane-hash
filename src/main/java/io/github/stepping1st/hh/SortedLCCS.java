package io.github.stepping1st.hh;


import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrays;

import java.io.Serializable;
import java.util.function.IntConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Sorted Longest Circular Co-Substring(LCCS).
 */
public class SortedLCCS implements Serializable {
    private static final int SCAN_SIZE = Integer.parseInt(
            System.getenv().getOrDefault("sorted_lccs.scan_size", "4")
    );

    private static final Logger LOGGER =
            LoggerFactory.getLogger(SortedLCCS.class.getName());
    private final int dim;
    private final int step;
    private final int[][] sortedidx;
    private final int[][] nextidx;
    private final int searchdim;
    private final IntNDArray data;
    private final int n;

    /**
     * @param step size of skip data
     * @param data nd array
     */
    public SortedLCCS(final int step, final IntNDArray data) {
        n = data.dims()[0];
        dim = data.dims()[1];
        this.step = step;
        searchdim = ((dim - 1) / step) + 1;
        this.data = data;

        sortedidx = getSortIdx(dim, n);
        nextidx = getNextLink(step);
    }

    /**
     * bucket-sort leveraging extents.
     */
    private int[][] getSortIdx(final int dim, final int n) {
        int[][] sortedidx = new int[dim][n];
        for (int d = 0; d < dim; ++d) {
            for (int i = 0; i < n; ++i) {
                sortedidx[d][i] = i;
            }

            int start = d;
            IntComparator cmp = new IntComparator() {
                @Override
                public int compare(final int idx1, final int idx2) {
                    CmpLoc match = compareDim(idx1, idx2, start, 0);
                    return match.cmp;
                }
            };
            IntArrays.quickSort(sortedidx[d], cmp);
        }
        return sortedidx;
    }

    private int[][] getNextLink(final int step) {
        int[][] nextlink = new int[dim][n];
        for (int d = dim - 1; 0 <= d; --d) {
            int nextdim = (d + step) % dim;
            int[] next = new int[n];
            for (int i = 0; i < n; ++i) {
                next[sortedidx(nextdim, i)] = i;
            }

            for (int i = 0; i < n; ++i) {
                int idx = sortedidx(d, i);
                nextlink[d][i] = next[idx];
            }
        }
        return nextlink;
    }

    private int sortedidx(final int dim, final int idx) {
        return sortedidx[dim][idx];
    }

    private CmpLoc compareDim(final int idx1, final int idx2,
                              final int start, final int walked) {
        int[] xp = data.value(idx1);
        int[] yp = data.value(idx2);
        return compareDim(xp, yp, start, walked);
    }

    /**
     * compare vector dimension from input index.
     *
     * @param xs     compare vector
     * @param ys     compare vector
     * @param start  compare start index
     * @param walked already compared length
     * @return compare location
     */
    private CmpLoc compareDim(final int[] xs, final int[] ys,
                              final int start, final int walked) {
        int size = dim - walked;
        for (int i = 0; i < size; ++i) {
            int idx = (start + walked + i) % dim;
            int x = xs[idx];
            int y = ys[idx];
            if (x != y) {
                return new CmpLoc(i + walked, Integer.compare(x, y));
            }
        }
        return new CmpLoc(dim, 0);
    }

    /**
     * Compare Location.
     */
    private static final class CmpLoc {
        private final int walked;
        private final int cmp;

        private CmpLoc(final int walked, final int cmp) {
            this.walked = walked;
            this.cmp = cmp;
        }
    }

    /**
     * @param scanstep n scan step
     * @param query    query vector
     * @param f        perform by index
     * @return search index and count
     */
    public final Int2IntMap search(final int scanstep,
                                   final int[] query,
                                   final IntConsumer f) {
        return candidatesByScan(scanstep, query, f);
    }

    /**
     * simple scan strategy (high data locality).
     */
    private Int2IntMap candidatesByScan(final int scanstep,
                                        final int[] query,
                                        final IntConsumer f) {
        Locs locs = findMatchedLocs(query);

        Int2IntMap checked = new Int2IntOpenHashMap();
        checked.defaultReturnValue(0);

        IntConsumer check = (idx) -> {
            int cnt = checked.get(idx);
            if (cnt == 0) {
                f.accept(idx);
            }
            checked.put(idx, cnt + 1);
        };

        BiIntConsumer checkloc = (curidx, d) -> {
            for (int i = curidx; 0 <= i && curidx - i < scanstep; --i) {
                int matchidx = sortedidx(d, i);
                check.accept(matchidx);
            }
            for (int i = curidx + 1; i < n && i - curidx - 1 < scanstep; ++i) {
                int matchidx = sortedidx(d, i);
                check.accept(matchidx);
            }
        };

        for (int i = 0; i < dim; ++i) {
            checkloc.accept(locs.idxes[i], i);
        }

        return checked;
    }

    private int[] data(final int querydim, final int idx) {
        int row = sortedidx[querydim][idx];
        return data.value(row);
    }

    /**
     * search data range for query vector.
     *
     * @param query    query vector
     * @param querydim compare dimension of query
     * @return (lowidx, lowLen, highLen) that query in [lowidx, highidx]
     * or lowidx == 0 or highidx == N-1
     */
    private Loc getLoc(final int[] query, final int querydim) {
        int low = 0;
        int high = n - 1;
        int[] datalow = data(querydim, low);
        int[] datahigh = data(querydim, high);
        CmpLoc lowloc = compareDim(query, datalow, querydim, 0);
        CmpLoc highloc = compareDim(query, datahigh, querydim, 0);

        return binarySearchLoc(
                query, querydim, low, lowloc.walked,
                high, highloc.walked);
    }

    /**
     * binary search with linear when interval is small.
     *
     * @return (lowidx, lowLen, highLen) that query in [lowidx, highidx]
     * or lowidx == 0 or highidx == N-1
     */
    private Loc binarySearchLoc(final int[] query, final int querydim,
                                final int low, final int lowlen,
                                final int high, final int highlen) {
        BiIntFunction<CmpLoc> cmp = (i, prev) -> {
            int[] dpa = data(querydim, i);
            return compareDim(query, dpa, querydim, prev);
        };

        int nextlow = low;
        int nextlowlen = lowlen;
        int nexthigh = high;
        int nexthighlen = highlen;
        while (nextlow < nexthigh - SCAN_SIZE) {
            int curdim = Math.min(lowlen, highlen);
            // binary search
            int mid = (nextlow + nexthigh) / 2;
            CmpLoc loc = cmp.apply(mid, curdim);

            // if query < mid:
            if (loc.cmp == -1) {
                // which means mid < query
                nexthigh = mid;
                nexthighlen = loc.walked;
            } else {
                nextlow = mid;
                nextlowlen = loc.walked;
            }
        }

        return scanLoc(query, querydim, nextlow, nextlowlen,
                nexthigh, nexthighlen);
    }

    /**
     * assume the length of matching of low and high are lowlen.
     */
    private Loc scanLoc(final int[] query, final int querydim,
                        final int low, final int lowlen,
                        final int high, final int highlen) {
        int lastdim = lowlen;
        int mindim = Math.min(lowlen, highlen);
        for (int i = low + 1; i < high; ++i) {
            int[] dpi = data(querydim, i);
            CmpLoc loc = compareDim(query, dpi, querydim, mindim);
            if (loc.cmp == -1) {
                return new Loc(i - 1, lastdim, loc.walked);
            }
            lastdim = loc.walked;
        }
        // reach the end
        return new Loc(high - 1, lastdim, highlen);
    }

    private Locs findMatchedLocs(final int[] query) {
        // binary search
        Loc loc = getLoc(query, 0);
        int idx = loc.idx, lowlen = loc.lowlen, highlen = loc.highlen;
        int[] idxes = new int[dim];
        int[] lowlens = new int[dim];
        int[] highlens = new int[dim];
        // store the res for multi-probe lsh
        idxes[0] = idx;
        lowlens[0] = lowlen;
        highlens[0] = highlen;

        for (int i = 1; i < searchdim; ++i) {
            int d = i * step;
            int lowidx = nextidx[d - step][idx];
            int highidx = nextidx[d - step][idx + 1];

            // set lowLen, highLen range within step size
            if (lowlen < step) {
                lowlen = 0;
                lowidx = 0;
            } else if (lowlen != this.dim) {
                lowlen -= step;
            }

            if (highlen < step) {
                highlen = 0;
                highidx = n - 1;
            } else if (highlen != this.dim) {
                highlen -= step;
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("lowidx={} lowLen={} highidx={} highLen={}",
                        lowidx, lowlen, highidx, highlen);
            }

            Loc found = binarySearchLoc(
                    query, d, lowidx, lowlen, highidx, highlen);
            // store the res for multi-probe lsh
            idxes[d] = found.idx;
            lowlens[d] = found.lowlen;
            highlens[d] = found.highlen;
        }
        return new Locs(idxes, lowlens, highlens);
    }

    /**
     * Location of data array.
     */
    private static final class Locs {
        private final int[] idxes;
        private final int[] lowlens;
        private final int[] highlens;

        /**
         * @param idxes    index array
         * @param lowlens  low indexes
         * @param highlens high indexes
         */
        private Locs(final int[] idxes,
                     final int[] lowlens,
                     final int[] highlens) {
            this.idxes = idxes;
            this.lowlens = lowlens;
            this.highlens = highlens;
        }
    }

    /**
     * Location of data.
     */
    private static final class Loc {
        private final int idx;
        private final int lowlen;
        private final int highlen;

        /**
         * @param idx     index
         * @param lowlen  low index
         * @param highlen hight index
         */
        private Loc(final int idx, final int lowlen, final int highlen) {
            this.idx = idx;
            this.lowlen = lowlen;
            this.highlen = highlen;
        }
    }
}
