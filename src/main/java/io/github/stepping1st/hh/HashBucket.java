package io.github.stepping1st.hh;


import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

import java.io.Serializable;
import java.util.Random;
import java.util.function.IntConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * HashBucket for data.
 */
public class HashBucket implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(
            HashBucket.class.getName()
    );
    private final int l;
    private final int mask;
    private final Long2ObjectMap<IntList>[] buckets;
    private final Random rd = new Random();

    /**
     * @param n number of data
     * @param l tables
     */
    public HashBucket(final int n, final int l) {
        this.l = l;
        int max = 1;
        while (max < n) {
            max <<= 1;
        }
        --max;
        this.mask = max;
        this.buckets = new Long2ObjectMap[l];
        for (int i = 0; i < l; i++) {
            buckets[i] = new Long2ObjectOpenHashMap<>();
        }
    }

    /**
     * insert key and decode.
     *
     * @param key   index of data
     * @param dcode signature of data
     */
    public final void insert(final int key, final int[] dcode) {
        for (int j = 0; j < l; ++j) {
            long hashcode32 = dcode[j] & mask;
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("insert j={}, code={}, mask={}",
                        j, hashcode32, mask);
            }
            IntList found = getOrInsert(buckets[j], hashcode32);
            found.add(key);
            if (1 < found.size()) {
                int n = rd.nextInt(found.size());
                swap(found, found, n, found.size() - 1);
            }
        }
    }

    /**
     * search data from signature.
     *
     * @param qcode    signature of data
     * @param limit    candidate limit
     * @param consumer consumer for search data
     * @return index and count of search data
     */
    public final Long2IntMap search(final int[] qcode,
                                    final int limit,
                                    final IntConsumer consumer) {
        Long2IntMap candidate = new Long2IntOpenHashMap();
        candidate.defaultReturnValue('\0');
        for (int j = 0; j < l; ++j) {
            int hashcode32 = qcode[j] & mask;
            IntList bucket = getOrEmpty(buckets[j], hashcode32);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("find j={}, bucket={}, code={}, candidate={}",
                        j, bucket.size(), hashcode32, candidate.size());
            }
            for (int key : bucket) {
                int cnt = candidate.get(key);
                if (cnt == '\0') {
                    consumer.accept(key);
                    cnt = 0;
                }
                candidate.put(key, cnt + 1);
                if (limit <= candidate.size()) {
                    return candidate;
                }
            }
        }
        return candidate;
    }

    private IntList getOrEmpty(final Long2ObjectMap<IntList> bucket,
                               final long hashcode32) {
        IntList found = bucket.get(hashcode32);
        if (found == null) {
            return IntLists.EMPTY_LIST;
        } else {
            return found;
        }
    }

    private void swap(final IntList as, final IntList bs,
                      final int aidx, final int bidx) {
        if (aidx != bidx) {
            int temp = as.getInt(aidx);
            as.set(aidx, bs.getInt(bidx));
            bs.set(bidx, temp);
        }
    }

    private IntList getOrInsert(final Long2ObjectMap<IntList> map,
                                final long idx) {
        IntList found = map.get(idx);
        if (found == null) {
            found = new IntArrayList();
            map.put(idx, found);
        }
        return found;
    }

}
