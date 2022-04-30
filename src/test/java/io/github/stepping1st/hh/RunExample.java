package io.github.stepping1st.hh;


import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static io.github.stepping1st.hh.utils.OpUtils.map;
import static io.github.stepping1st.hh.utils.OpUtils.permutation;
import static io.github.stepping1st.hh.utils.OpUtils.wrap;


public class RunExample {

    public static void main(String[] args) {
        // data file name
        String input = "data/yelp.gz";
        // query file name
        String query = "data/yelp_query.csv";
        // top n
        String[] top = new String[]{"500"};
        // search limit
        String[] limit = new String[]{"5000", "10000", "20000", "30000",
                "40000", "50000", "60000", "70000", "75000"};

        // create params
        Stream.of(
                hash("BH", input, query, top, limit)
                , hash("EH", input, query, top, limit)
                , mh(input, query, top, limit)
                , nh(input, query, top, limit)
                , fh(input, query, top, limit)
        ).flatMap(Collection::stream)
                .forEach(
                        wrap(param -> {
                            System.out.println(Arrays.toString(param));
                            Runner.run(param);
                        })
                );
    }

    public static List<String[]> hash(String run, String input,
                                      String query, String[] top,
                                      String[] limit) {
        String[] m = new String[]{"2", "4", "6", "8", "10"};
        String[] l = new String[]{"8", "16", "32", "64", "128", "256"};
        List<String[]> permutation = permutation(new String[][]{top, limit, m, l});

        return map(permutation, new Function<String[], String[]>() {
            @Override
            public String[] apply(String[] base) {
                String key = String.format("%s_%s", run, String.join("_", base));
                return new String[]{
                        "--run", run
                        , "--query", query
                        , "--top", base[0]
                        , "--input", input
                        , "--limit", base[1]
                        , "--single_hasher", base[2]
                        , "--tables", base[3]
                        , "--norm", "true"
                        , "--meta_output", String.format("output/eval_%s.csv", run.toLowerCase())
                        , "--name", key
                };
            }
        });
    }

    public static List<String[]> mh(String input, String query,
                                    String[] top, String[] limit) {
        String[] m = new String[]{"2", "4", "6", "8", "10"};
        String[] l = new String[]{"8", "16", "32", "64", "128", "256"};
        String[] M = new String[]{"4", "8", "16"};
        List<String[]> permutation = permutation(new String[][]{top, limit, m, l, M});

        return map(permutation, new Function<String[], String[]>() {
            @Override
            public String[] apply(String[] base) {
                String key = String.format("MH_%s", String.join("_", base));
                return new String[]{
                        "--run", "MH"
                        , "--query", query
                        , "--top", base[0]
                        , "--input", input
                        , "--limit", base[1]
                        , "--single_hasher", base[2]
                        , "--tables", base[3]
                        , "--num_proj_hash", base[4]
                        , "--norm", "true"
                        , "--meta_output", "output/eval_mh.csv"
                        , "--name", key
                };
            }
        });
    }

    public static List<String[]> nh(String input, String query,
                                    String[] top, String[] limit) {
        String[] m = new String[]{"8", "16", "32", "64", "128", "256"};
        String[] s = new String[]{"1", "2", "4", "8"};
        String[] w = new String[]{"0.1"};
        List<String[]> permutation = permutation(new String[][]{top, limit, m, s, w});

        return map(permutation, new Function<String[], String[]>() {
            @Override
            public String[] apply(String[] base) {
                String key = String.format("NH_%s", String.join("_", base));
                return new String[]{
                        "--run", "NH"
                        , "--query", query
                        , "--top", base[0]
                        , "--input", input
                        , "--limit", base[1]
                        , "--single_hasher", base[2]
                        , "--scale_dim", base[3]
                        , "--bucket_width", base[4]
                        , "--norm", "true"
                        , "--meta_output", "output/eval_nh.csv"
                        , "--name", key
                };
            }
        });
    }

    public static List<String[]> fh(String input, String query,
                                    String[] top, String[] limit) {
        String[] m = new String[]{"8", "16", "32", "64", "128", "256"};
        String[] s = new String[]{"1", "2", "4", "8"};
        String[] l = new String[]{"2", "4", "6", "8", "10"};
        String[] b = new String[]{"0.9"};
        List<String[]> permutation = permutation(new String[][]{top, limit, m, s, l, b});

        return map(permutation, new Function<String[], String[]>() {
            @Override
            public String[] apply(String[] base) {
                String key = String.format("FH_%s", String.join("_", base));
                return new String[]{
                        "--run", "FH"
                        , "--query", query
                        , "--top", base[0]
                        , "--input", input
                        , "--limit", base[1]
                        , "--tables", base[2]
                        , "--scale_dim", base[3]
                        , "--separation_threshold", base[4]
                        , "--interval_ratio", base[5]
                        , "--norm", "true"
                        , "--meta_output", "output/eval_fh.csv"
                        , "--name", key
                };
            }
        });
    }

}
