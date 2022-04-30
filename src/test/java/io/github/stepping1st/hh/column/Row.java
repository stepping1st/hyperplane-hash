package io.github.stepping1st.hh.column;


import io.github.stepping1st.hh.utils.OpUtils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Row<T> implements Iterable<Map.Entry<String, T>> {
    private final List<T> values = new ArrayList<>();
    private final Map<String, Integer> idxes = new HashMap<>();

    public void put(String key, T value) {
        Integer idx = idxes.get(key);
        if (idx == null) {
            idx = idxes.size();
            idxes.put(key, idx);
            values.add(value);
        } else {
            values.set(idx, value);
        }
    }

    @Override
    public java.util.Iterator<Map.Entry<String, T>> iterator() {
        return new Iterator();
    }

    public int size() {
        return values.size();
    }

    @Override
    public String toString() {
        return OpUtils.join(", ", this);
    }

    public void putAll(Row<T> other) {
        for (Map.Entry<String, T> e : other) {
            put(e.getKey(), e.getValue());
        }
    }

    public T get(String key) {
        Integer idx = idxes.get(key);
        return idx == null ? null : values.get(idx);
    }

    public boolean containsKey(String key) {
        return idxes.containsKey(key);
    }

    private class Iterator implements java.util.Iterator<Map.Entry<String, T>> {
        private final String[] keys;
        private int idx = 0;

        private Iterator() {
            keys = new String[idxes.size()];
            for (Map.Entry<String, Integer> e : idxes.entrySet()) {
                keys[e.getValue()] = e.getKey();
            }
        }

        @Override
        public boolean hasNext() {
            return idx < values.size();
        }

        @Override
        public Map.Entry<String, T> next() {
            Map.Entry<String, T> e = new AbstractMap.SimpleEntry<>(keys[idx], values.get(idx));
            idx = idx + 1;
            return e;
        }
    }

}
