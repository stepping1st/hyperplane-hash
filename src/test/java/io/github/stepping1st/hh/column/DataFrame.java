package io.github.stepping1st.hh.column;


import io.github.stepping1st.hh.utils.IOUtils;
import io.github.stepping1st.hh.utils.TypeUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.github.stepping1st.hh.utils.OpUtils.append;
import static io.github.stepping1st.hh.utils.OpUtils.json;
import static io.github.stepping1st.hh.utils.IOUtils.write;


public class DataFrame {
    public static final DataFrame EMPTY = new DataFrame(Collections.emptyList());
    private final List<Series> series;
    private final int size;
    private final Row<Integer> index;

    public DataFrame(List<Series> series) {
        this.series = series;
        index = new Row<>();
        int size = -1;
        for (int i = 0; i < series.size(); i++) {
            Series s = series.get(i);
            index.put(s.name(), i);
            assert size == -1 || size == s.size() : String.format("prev size(%d) != size(%d)", size, s.size());
            size = s.size();
        }
        this.size = Math.max(0, size);
    }

    public List<Row<Object>> rows(int limit) {
        List<Row<Object>> list = new ArrayList<>();
        int size = Math.min(this.size, limit);
        for (int i = 0; i < size; i++) {
            list.add(row(i));
        }
        return list;
    }

    public Row<Object> row(int i) {
        Row<Object> row = new Row<>();
        for (Series s : series) {
            row.put(s.name(), s.valueOf(i));
        }
        return row;
    }

    public DataFrame concat(DataFrame other, int axis) {
        return concat(this, other, axis);
    }

    public Stream<Stream<Object>> lines(int limit, boolean head) {
        int size = Math.min(this.size, limit);
        Stream<Stream<Object>> header = head ? Stream.of(series.stream().map(Series::name)) : Stream.empty();
        Stream<Stream<Object>> body = IntStream.range(0, size)
                .mapToObj(i -> series.stream().map(s -> s.valueOf(i)));
        return Stream.concat(header, body);
    }

    public void csv(String path) throws IOException {
        IOUtils.write(path, lines(size, true).map(l -> json(",", l)));
    }

    public void csv(FileWriter writer, boolean head) throws IOException {
        IOUtils.write(writer, lines(size, head).map(l -> json(",", l)));
    }

    @Override
    public String toString() {
        return toString(10);
    }

    public String toString(int limit) {
        return json("\n", lines(limit, true)
                .map(l -> json(",", l)));
    }

    public static <T> DataFrame of(List<Row<T>> rows) {
        DataFrame df = EMPTY;
        for (Row<T> row : rows) {
            DataFrame metaDF = DataFrame.of(row);
            DataFrame next = df == EMPTY ? metaDF : df.concat(metaDF, 0);
            assert next.size == df.size + metaDF.size;
            df = next;
        }
        return df;
    }

    public static <T> DataFrame of(Row<T> row) {
        List<Series> series = new ArrayList<>();
        for (Map.Entry<String, T> e : row) {
            String key = e.getKey();
            Object value = e.getValue();
            series.add(new Series(key, TypeUtils.type(value), new Object[]{value}));
        }
        return new DataFrame(series);
    }

    public static DataFrame of(String prefix, double[][] values) {
        int dim = values.length == 0 ? 0 : values[0].length;
        int size = values.length;
        List<Series> series = new ArrayList<>();
        for (int d = 0; d < dim; d++) {
            Object[] vs = new Object[size];
            for (int i = 0; i < size; i++) {
                vs[i] = values[i][d];
            }
            String name = String.format("%s%05d", prefix, d);
            series.add(new Series(name, Double.class, vs));
        }
        return new DataFrame(series);
    }

    public static DataFrame concat(DataFrame a, DataFrame b, int axis) {
        if (axis == 0) {
            List<Series> result = new ArrayList<>();
            for (Map.Entry<String, Integer> e : a.index) {
                String name = e.getKey();
                Series already = a.series.get(e.getValue());
                Integer found = b.index.get(name);
                Series next = found == null ? new Series(name, already.clazz(), new Object[b.size]) : b.series.get(found);
                result.add(already.concat(next));
            }
            for (Map.Entry<String, Integer> e : b.index) {
                if (!a.index.containsKey(e.getKey())) {
                    Series s = b.series.get(e.getValue());
                    Series next = new Series(e.getKey(), s.clazz(), new Object[a.size]);
                    result.add(next.concat(s));
                }
            }
            return new DataFrame(result);
        } else {
            assert a.size == b.size : String.format("%s(%d)!=%s(%d)", a.index, a.size, b.index, b.size);
            return new DataFrame(append(a.series, b.series));
        }
    }
}
