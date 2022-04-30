package io.github.stepping1st.hh.column;


import io.github.stepping1st.hh.utils.TypeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;


public class Series {
    private final String name;
    private final Class<?> clazz;
    private final Object[] values;

    public Series(String name, Class<?> clazz, Object[] values) {
        this.name = name;
        this.clazz = clazz;
        this.values = values;
    }

    public static <A> Series of(String name, Class<A> clazz, List<A> values) {
        int size = values.size();
        Object[] vs = new Object[size];
        for (int i = 0; i < size; i++) {
            vs[i] = values.get(i);
        }
        return new Series(name, clazz, vs);
    }

    public static <A, B> Series of(String name, Class<B> clazz, List<A> values, Function<A, B> f) {
        int size = values.size();
        Object[] vs = new Object[size];
        for (int i = 0; i < size; i++) {
            vs[i] = f.apply(values.get(i));
        }
        return new Series(name, clazz, vs);
    }

    public DataFrame toFrame() {
        List<Series> series = new ArrayList<>();
        series.add(this);
        return new DataFrame(series);
    }

    public Object valueOf(int idx) {
        assert idx < size() : String.format("name(%s) idx(%d)<size(%d)", name, idx, size());
        return values[idx];
    }

    public Series concat(Series other) {
        Object[] values = Arrays.copyOf(this.values, this.values.length + other.values.length);
        System.arraycopy(other.values, 0, values, this.values.length, other.size());
        return new Series(name, TypeUtils.combine(clazz, other.clazz), values);
    }

    public DataFrame concat(DataFrame other, int axis) {
        return this.toFrame().concat(other, axis);
    }

    public String name() {
        return name;
    }

    public Class<?> clazz() {
        return clazz;
    }

    public Object[] values() {
        return values;
    }

    public int size() {
        return values.length;
    }
}
