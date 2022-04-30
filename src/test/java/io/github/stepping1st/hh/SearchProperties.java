package io.github.stepping1st.hh;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.github.stepping1st.hh.utils.IOUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class SearchProperties {
    private final CommandLine clArgs;
    private String seed = null;
    private List<double[]> queries = null;

    public SearchProperties(String[] args) throws ParseException {
        Options options = new Options()
                .addOption("i", "input", true, "input data file")
                .addOption(null, "top_k", true, "number of top k")
                .addOption(null, "limit", true, "limit number of search")
                .addOption(null, "search_output", true, "search top k output file")
                .addOption(null, "real_output", true, "real output file")
                .addOption(null, "meta_output", true, "meta output file")
                .addOption(null, "query", true, "index of input or query vector")
                .addOption("r", "run", true, "run algorithm")
                .addOption(null, "single_hasher", true, "single hasher (m:EH,BH,MH,NH)")
                .addOption(null, "tables", true, "tables (l:EH,BH,MH) (m:FH)")
                .addOption("M", "num_proj_hash", true, "proj vectors for a single hasher (MH)")
                .addOption("s", "scale_dim", true, "scale factor of dimension (0 < s) (FH,NH)")
                .addOption(null, "seed", true, "random seed")
                .addOption(null, "extend", true, "object extends")
                .addOption(null, "norm", true, "normalize data")
                .addOption(null, "name", true, "name")
                .addOption("b", "interval_ratio", true, "interval ratio (0 < b < 1) (FH)")
                .addOption("w", "bucket_width", true, "bucket_width (NH)")
                .addOption(null, "separation_threshold", true, "separation threshold (FH)")
                .addOption(null, "eval_dist", true, "distance from data and query for evaluation")
                ;
        clArgs = new DefaultParser().parse(options, args);
    }

    public String input() {
        return clArgs.getOptionValue("input");
    }

    public int topK() {
        return Integer.parseInt(clArgs.getOptionValue("top_k"));
    }

    public List<double[]> query(double[][] data) {
        String query = clArgs.getOptionValue("query");
        if (queries == null) {
            synchronized (this) {
                if (queries == null) {
                    queries = parse(query, data);
                }
            }
        }
        return queries;
    }

    private List<double[]> parse(String query, double[][] data) {
        List<double[]> result = new ArrayList<>();
        try {
            Path path = Paths.get(query);
            if (Files.exists(path)) {
                InputStream inputStream = Files.newInputStream(path);
                return Arrays.asList(IOUtils.read(inputStream));
            } else {
                JsonArray arr = JsonParser.parseString(query).getAsJsonArray();
                for (int i = 0; i < arr.size(); i++) {
                    JsonElement e = arr.get(i);
                    if (e.isJsonArray()) {
                        JsonArray ja = e.getAsJsonArray();
                        double[] vec = new double[ja.size()];
                        for (int j = 0; j < ja.size(); j++) {
                            vec[j] = ja.get(j).getAsDouble();
                        }
                        result.add(vec);
                    } else {
                        result.add(data[e.getAsInt()]);
                    }
                }
                return result;
            }
        } catch (Exception e) {
            int size = Integer.parseInt(query);
            Random random = new Random(seed());
            for (int i = 0; i < size; i++) {
                result.add(data[random.nextInt(data.length)]);
            }
            return result;
        }
    }

    public String run() {
        return clArgs.getOptionValue("run");
    }

    public Integer tables() {
        String value = clArgs.getOptionValue("tables");
        return value == null ? null : Integer.parseInt(value);
    }

    public String tablesOpt() {
        String value = run();
        if (value.equals("FH")) {
            return "m";
        } else {
            return "l";
        }
    }

    public int limit() {
        return Integer.parseInt(clArgs.getOptionValue("limit"));
    }

    public Integer singleHasher() {
        String value = clArgs.getOptionValue("single_hasher");
        return value == null ? null : Integer.parseInt(value);
    }

    public Integer M() {
        String value = clArgs.getOptionValue("num_proj_hash");
        return value == null ? null : Integer.parseInt(value);
    }

    public Double b() {
        String value = clArgs.getOptionValue("interval_ratio");
        return value == null ? null : Double.parseDouble(value);
    }

    public Integer separationThreshold() {
        String value = clArgs.getOptionValue("separation_threshold");
        return value == null ? null : Integer.parseInt(value);
    }

    public boolean norm() {
        return Boolean.parseBoolean(clArgs.getOptionValue("norm"));
    }

    public boolean extend() {
        String extend = clArgs.getOptionValue("extend");
        return extend == null || Boolean.parseBoolean(extend);
    }

    public int seed() {
        if (this.seed == null) {
            synchronized (this) {
                if (this.seed == null) {
                    String seed = clArgs.getOptionValue("seed");
                    if (seed == null) {
                        Random rand = new Random();
                        this.seed = String.valueOf(Math.abs(rand.nextInt()));
                    }
                }
            }
        }
        return Integer.parseInt(seed);
    }

    public String searchOutput() {
        return clArgs.getOptionValue("search_output");
    }

    public String realOutput() {
        return clArgs.getOptionValue("real_output");
    }

    public Integer s() {
        String scaleDim = clArgs.getOptionValue("scale_dim");
        return scaleDim == null ? null : Integer.valueOf(scaleDim);
    }

    public String metaOutput() {
        return clArgs.getOptionValue("meta_output", "meta.csv");
    }

    public String name() {
        return clArgs.getOptionValue("name", "");
    }

    public Double w() {
        String bucketWidth = clArgs.getOptionValue("bucket_width");
        return bucketWidth == null ? null : Double.valueOf(bucketWidth);
    }

    public String evalDist() {
        return clArgs.getOptionValue("eval_dist");
    }
}
