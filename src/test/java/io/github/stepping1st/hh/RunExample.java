package io.github.stepping1st.hh;


import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static io.github.stepping1st.hh.utils.OpUtils.concat;
import static io.github.stepping1st.hh.utils.OpUtils.map;
import static io.github.stepping1st.hh.utils.OpUtils.permutation;
import static io.github.stepping1st.hh.utils.OpUtils.wrap;


public class RunExample {

    public static void main(String[] args) {
        // base parameters
        String[] base = {
                "--input", "data/yelp.gz"
                , "--query", "data/yelp_query.csv"
                , "--norm", "true"
        };
        // top n
        String[] top = new String[]{"500"};
        // search limit
        String[] limit = new String[]{"5000", "10000", "20000", "30000",
                "40000", "50000", "60000", "70000", "75000"};

        // create params
        Stream.of(
                hash("BH", "output/eval_bh.csv", top, limit)
                , hash("EH", "output/eval_eh.csv", top, limit)
                , mh("output/eval_mh.csv", top, limit)
                , nh("output/eval_nh.csv", top, limit)
                , fh("output/eval_fh.csv", top, limit)
        ).flatMap(Collection::stream)
                .forEach(
                        wrap(custom -> {
                            String[] param = concat(base, custom);
                            System.out.println(Arrays.toString(param));
                            Runner.run(param);
                        })
                );
    }

    public static List<String[]> hash(String run, String meta, String[] top, String[] limit) {
        String[] m = new String[]{"2", "4", "6", "8", "10"};
        String[] l = new String[]{"8", "16", "32", "64", "128", "256"};
        List<String[]> permutation = permutation(new String[][]{top, limit, m, l});

        return map(permutation, new Function<String[], String[]>() {
            @Override
            public String[] apply(String[] base) {
                String key = String.format("%s_%s", run, String.join("_", base));
                return new String[]{
                        "--run", run
                        , "--top", base[0]
                        , "--limit", base[1]
                        , "--single_hasher", base[2]
                        , "--tables", base[3]
                        , "--meta_output", meta
                        , "--name", key
                };
            }
        });
    }

    public static List<String[]> mh(String meta, String[] top, String[] limit) {
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
                        , "--top", base[0]
                        , "--limit", base[1]
                        , "--single_hasher", base[2]
                        , "--tables", base[3]
                        , "--num_proj_hash", base[4]
                        , "--meta_output", meta
                        , "--name", key
                };
            }
        });
    }

    public static List<String[]> nh(String meta, String[] top, String[] limit) {
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
                        , "--top", base[0]
                        , "--limit", base[1]
                        , "--single_hasher", base[2]
                        , "--scale_dim", base[3]
                        , "--bucket_width", base[4]
                        , "--meta_output", meta
                        , "--name", key
                };
            }
        });
    }

    public static List<String[]> fh(String meta, String[] top, String[] limit) {
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
                        , "--top", base[0]
                        , "--limit", base[1]
                        , "--tables", base[2]
                        , "--scale_dim", base[3]
                        , "--separation_threshold", base[4]
                        , "--interval_ratio", base[5]
                        , "--meta_output", meta
                        , "--name", key
                };
            }
        });
    }

}
