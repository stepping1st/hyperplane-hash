package io.github.stepping1st.hh.utils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;


public class OpUtils {

    public static <A, B> List<B> map(A[] as, Function<A, B> f) {
        List<B> list = new ArrayList<>();
        for (A a : as) {
            list.add(f.apply(a));
        }
        return list;
    }

    public static <T> List<T> map(int[] as, Function<Integer, T> f) {
        List<T> list = new ArrayList<>();
        for (int a : as) {
            list.add(f.apply(a));
        }
        return list;
    }

    public static <T> double[][] doubleArray(List<T> values, Function<T, double[]> f) {
        double[][] arr = new double[values.size()][];
        for (int i = 0; i < values.size(); i++) {
            arr[i] = f.apply(values.get(i));
        }
        return arr;
    }

    public static <A, B> List<B> map(List<A> values, Function<A, B> f) {
        List<B> list = new ArrayList<>();
        for (A v : values) {
            list.add(f.apply(v));
        }
        return list;
    }

    public static List<String[]> permutation(String[][] vs) {
        return permutation(Collections.singletonList(new String[vs.length]), vs, 0);
    }

    private static List<String[]> permutation(List<String[]> result, String[][] vs, int i) {
        if (i < vs.length) {
            List<String[]> next = new ArrayList<>();
            for (String value : vs[i]) {
                for (String[] prev : result) {
                    String[] copy = Arrays.copyOf(prev, vs.length);
                    copy[i] = value;
                    next.add(copy);
                }
            }
            return permutation(next, vs, i + 1);
        } else {
            return result;
        }
    }

    public static List<Integer> range(int start, int end) {
        List<Integer> list = new ArrayList<>();
        for (int i = start; i < end; i++) {
            list.add(i);
        }
        return list;
    }

    public static StringJoiner json(CharSequence delimiter, Object[] values) {
        StringJoiner joiner = new StringJoiner(delimiter);
        for (Object o : values) {
            if (o == null) {
                joiner.add("");
            } else {
                joiner.add(TypeUtils.toString(o));
            }
        }
        return joiner;
    }

    public static <T> String join(CharSequence delimiter, Iterable<T> values) {
        StringJoiner joiner = new StringJoiner(delimiter);
        for (Object o : values) {
            if (o == null) {
                joiner.add("");
            } else {
                joiner.add(String.valueOf(o));
            }
        }
        return joiner.toString();
    }

    public static <T> String json(CharSequence delimiter, Iterable<T> values) {
        StringJoiner joiner = new StringJoiner(delimiter);
        for (Object o : values) {
            if (o == null) {
                joiner.add("");
            } else {
                joiner.add(TypeUtils.toString(o));
            }
        }
        return joiner.toString();
    }

    public static <T> String json(CharSequence delimiter, Stream<T> values) {
        StringJoiner joiner = new StringJoiner(delimiter);
        values.forEach(
                o -> {
                    if (o == null) {
                        joiner.add("");
                    } else {
                        joiner.add(TypeUtils.toString(o));
                    }
                }
        );
        return joiner.toString();
    }

    public static <T> Consumer<T> wrap(ExceptionConsumer<T> f) {
        return (T r) -> {
            try {
                f.accept(r);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static <T> List<T> append(List<T> a, List<T> b) {
        List<T> list = new ArrayList<>();
        list.addAll(a);
        list.addAll(b);
        return list;
    }

    public static String[] split(String line) {
        String otherThanQuote = " [^\"] ";
        String quotedString = String.format(" \" %s* \" ", otherThanQuote);
        String regex = String.format("(?x) " + // enable comments, ignore white spaces
                        ",                         " + // match a comma
                        "(?=                       " + // start positive look ahead
                        "  (?:                     " + //   start non-capturing group 1
                        "    %s*                   " + //     match 'otherThanQuote' zero or more times
                        "    %s                    " + //     match 'quotedString'
                        "  )*                      " + //   end group 1 and repeat it zero or more times
                        "  %s*                     " + //   match 'otherThanQuote'
                        "  $                       " + // match the end of the string
                        ")                         ", // stop positive look ahead
                otherThanQuote, quotedString, otherThanQuote);
        return line.split(regex, -1);
    }

    public static Object[] fill(int size, Object value) {
        Object[] objects = new Object[size];
        Arrays.fill(objects, value);
        return objects;
    }

    public static <T> T[] concat(T[] arr1, T[] arr2) {
        T[] result = Arrays.copyOf(arr1, arr1.length + arr2.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }

    public static Stream<String> floatToString(Stream<float[]> values) {
        return values.map(arr -> {
            StringJoiner joiner = new StringJoiner(",");
            for (float v : arr) {
                joiner.add(String.valueOf(v));
            }
            return joiner.toString();
        });
    }

    public interface ExceptionConsumer<T> {
        void accept(T t) throws Exception;
    }

}
