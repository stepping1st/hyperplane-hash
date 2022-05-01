package io.github.stepping1st.hh;


import io.github.stepping1st.hh.search.Search;
import io.github.stepping1st.hh.utils.IOUtils;
import org.apache.commons.math.random.JDKRandomGenerator;
import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;
import io.github.stepping1st.hh.column.DataFrame;
import io.github.stepping1st.hh.utils.OpUtils;
import io.github.stepping1st.hh.column.Row;
import io.github.stepping1st.hh.column.Series;
import io.github.stepping1st.hh.hash.BHHash;
import io.github.stepping1st.hh.hash.EHHash;
import io.github.stepping1st.hh.hash.FHHash;
import io.github.stepping1st.hh.search.FHSearch;
import io.github.stepping1st.hh.search.HashSearch;
import io.github.stepping1st.hh.hash.MHHash;
import io.github.stepping1st.hh.hash.NHHash;
import io.github.stepping1st.hh.search.NHSearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.stepping1st.hh.utils.OpUtils.doubleArray;
import static io.github.stepping1st.hh.utils.OpUtils.range;
import static io.github.stepping1st.hh.utils.OpUtils.map;


public class Runner {
    private static final Logger LOGGER = LoggerFactory.getLogger(Runner.class.getName());

    public static void main(String[] args) throws Exception {
        run(args);
    }

    public static void run(String[] args) throws Exception {
        SearchProperties prop = new SearchProperties(args);
        long start = System.currentTimeMillis();
        double[][] data = read(prop);

        List<Row<Object>> metas = evaluate(prop, data);
        Row<Object> base = new Row<>();
        base.put("top_k", prop.topK());
        base.put("limit", prop.limit());
        base.put("(m)single_hasher", prop.singleHasher());
        base.put("(M)num_proj_hash", prop.M());
        base.put(String.format("(%s)tables", prop.tablesOpt()), prop.tables());
        base.put("(b)interval_ratio", prop.b());
        base.put("(s)scale_dim", prop.s());
        base.put("(w)bucket_width", prop.w());
        base.put("(l)separation_threshold", prop.separationThreshold());
        base.put("search_output", prop.searchOutput());
        base.put("real_output", prop.realOutput());
        base.put("input", prop.input());
        base.put("extend", prop.extend());
        base.put("norm", prop.norm());
        base.put("seed", prop.seed());
        base.put("name", prop.name());
        base.put("eval_dist", prop.evalDist());
        base.put("ms_time", start);
        base.put("data_size", data.length);
        for (Row<Object> m : metas) {
            m.putAll(base);
        }

        String metaOutput = prop.metaOutput();
        DataFrame df = DataFrame.of(metas);
        IOUtils.mkdir(Paths.get(metaOutput));
        if (Files.exists(Paths.get(metaOutput))) {
            List<Row<Object>> rows = IOUtils.csv(metaOutput);
            DataFrame.of(rows)
                    .concat(df, 0)
                    .csv(metaOutput);
        } else {
            df.csv(metaOutput);
        }
    }

    private static List<Row<Object>> evaluate(SearchProperties prop, double[][] data) throws IOException {
        int dim = data.length == 0 ? 0 : data[0].length;
        String run = prop.run();
        List<double[]> queries = prop.query(data);
        assert dim == (queries.isEmpty() ? 0 : queries.get(0).length);

        JDKRandomGenerator rg = new JDKRandomGenerator();
        rg.setSeed(prop.seed());
        RandomData rd = new RandomDataImpl(rg);

        if (run.equals("BH")) {
            return evaluate(map(queries, new Function<double[], Query>() {
                @Override
                public Query apply(double[] query) {
                    return new Query(query, data, prop.topK(), prop.limit(), Dist.ABS_DOT);
                }
            }), new Supplier<Search<Query>>() {
                @Override
                public Search<Query> get() {
                    BHHash hash = new BHHash(dim, prop.singleHasher(), prop.tables(), rd);
                    HashBucket bucket = new HashBucket(data.length, prop.tables());
                    return new HashSearch(hash, data, bucket);
                }
            }, prop);
        }
        if (run.equals("MH")) {
            return evaluate(map(queries, new Function<double[], Query>() {
                @Override
                public Query apply(double[] query) {
                    return new Query(query, data, prop.topK(), prop.limit(), Dist.ABS_DOT);
                }
            }), new Supplier<Search<Query>>() {
                @Override
                public Search<Query> get() {
                    MHHash hash = new MHHash(dim, prop.singleHasher(), prop.tables(), prop.M(), rd);
                    HashBucket bucket = new HashBucket(data.length, prop.tables());
                    return new HashSearch(hash, data, bucket);
                }
            }, prop);
        }
        if (run.equals("EH")) {
            return evaluate(map(queries, new Function<double[], Query>() {
                @Override
                public Query apply(double[] query) {
                    return new Query(query, data, prop.topK(), prop.limit(), Dist.ABS_DOT);
                }
            }), new Supplier<Search<Query>>() {
                @Override
                public Search<Query> get() {
                    EHHash hash = new EHHash(dim, prop.singleHasher(), prop.tables(), rd);
                    HashBucket bucket = new HashBucket(data.length, prop.tables());
                    return new HashSearch(hash, data, bucket);
                }
            }, prop);
        }
        if (run.equals("NH")) {
            return evaluate(map(queries, new Function<double[], Query>() {
                @Override
                public Query apply(double[] query) {
                    return new Query(query, data, prop.topK(), prop.limit(), Dist.ABS_DOT);
                }
            }), new Supplier<Search<Query>>() {
                @Override
                public Search<Query> get() {
                    NHHash hash = new NHHash(dim, prop.singleHasher(), prop.s(), prop.w(), rd);
                    return new NHSearch(hash, prop.singleHasher(), data);
                }
            }, prop);
        }
        if (run.equals("FH")) {
            return evaluate(map(queries, new Function<double[], FHQuery>() {
                @Override
                public FHQuery apply(double[] query) {
                    return new FHQuery(query, data, prop.topK(), prop.limit(), prop.separationThreshold(), Dist.ABS_DOT);
                }
            }), new Supplier<Search<FHQuery>>() {
                @Override
                public Search<FHQuery> get() {
                    FHHash hash = new FHHash(dim, prop.s(), rd);
                    return new FHSearch(hash, prop.b(), prop.tables(), data, rd);
                }
            }, prop);
        }
        return Collections.emptyList();
    }

    private static <T extends Query> List<Row<Object>> evaluate(List<T> queries,
                                                                Supplier<Search<T>> supplier,
                                                                SearchProperties prop) throws IOException {
        System.gc();
        long indexStart = System.currentTimeMillis();
        long prevIndexMemory = usedMemory();
        Search<T> searcher = supplier.get();
        long indexDuration = System.currentTimeMillis() - indexStart;
        long indexUsedMemory = usedMemory() - prevIndexMemory;

        List<Row<Object>> metas = new ArrayList<>();
        for (int i = 0; i < queries.size(); i++) {
            T query = queries.get(i);
            long searchStart = System.currentTimeMillis();
            long prevSearchMemory = usedMemory();
            List<IdxVal> found = searcher.nns(query);
            long searchDuration = System.currentTimeMillis() - searchStart;
            long searchUsedMemory = usedMemory() - prevSearchMemory;

            TrueSet trueSet = TrueSet.of(query.copy(Dist.valueOf(prop.evalDist())));
            Row<Object> meta = new Row<>();
            meta.put("run", prop.run());
            meta.put("no", i);
            meta.putAll(metric(trueSet, found));
            meta.put("search_duration", searchDuration);
            meta.put("index_duration", indexDuration);
            meta.put("index_used_memory", indexUsedMemory);
            meta.put("search_used_memory", searchUsedMemory);
            LOGGER.info(String.valueOf(meta));
            meta.put("query", query.query());
            metas.add(meta);
            write(i, trueSet.slice(prop.topK()), prop, found);
        }
        return metas;
    }

    public static Row<Object> metric(TrueSet trueSet, List<IdxVal> result) {
        Query query = trueSet.query();
        IdxVal[] idxes = trueSet.idxes();
        double matched = 0;
        double searchSum = 0;
        double trueSum = 0;
        double[][] data = query.data();
        Dist dist = query.dist();
        int last = Math.min(idxes.length - 1, query.top() - 1);
        double distMax = -1 < last && last < idxes.length ?
                dist.distance(query.query(), data[idxes[last].idx()]) : Double.NaN;
        for (int order = 0; order < result.size(); order++) {
            IdxVal pred = result.get(order);
            IdxVal real = idxes[order];
            double searchDist = dist.distance(query.query(), data[pred.idx()]);
            searchSum += searchDist;
            trueSum += real.value();
            if (searchDist <= distMax) {
                matched = matched + 1;
            }
        }
        Row<Object> metric = new Row<>();
        metric.put("matched", (int) matched);
        metric.put("recall", matched / query.top());
        metric.put("precision", matched / query.limit());
        metric.put("true_dist_mean", trueSum / result.size());
        metric.put("search_dist_mean", searchSum / result.size());
        return metric;
    }

    private static void write(int idx,
                              TrueSet trueSet,
                              SearchProperties prop,
                              List<IdxVal> found) throws IOException {
        double[][] data = trueSet.query().data();

        // write found data
        String searchOutput = prop.searchOutput();
        if (searchOutput != null && !searchOutput.isEmpty()) {
            if (idx == 0) {
                Files.deleteIfExists(Paths.get(searchOutput));
            }
            boolean head = !Files.exists(Paths.get(searchOutput));
            IOUtils.mkdir(Paths.get(searchOutput));
            FileWriter searchWriter = new FileWriter(searchOutput, true);
            new Series("no", Integer.class, OpUtils.fill(found.size(), idx)).toFrame()
                    .concat(Series.of("data_idx", Integer.class, map(found, IdxVal::idx)).toFrame(), 1)
                    .concat(Series.of("idx", Integer.class, range(0, found.size())).toFrame(), 1)
                    .concat(DataFrame.of("dim_", doubleArray(found, i -> data[i.idx()])), 1)
                    .csv(searchWriter, head);
        }

        // write real data
        String realOutput = prop.realOutput();
        if (realOutput != null && !realOutput.isEmpty()) {
            if (idx == 0) {
                Files.deleteIfExists(Paths.get(realOutput));
            }
            boolean head = !Files.exists(Paths.get(realOutput));
            IOUtils.mkdir(Paths.get(realOutput));
            FileWriter realWriter = new FileWriter(realOutput, true);
            int size = trueSet.idxes().length;
            new Series("no", Integer.class, OpUtils.fill(size, idx)).toFrame()
                    .concat(Series.of("data_idx", Integer.class, map(trueSet.idxes(), IdxVal::idx)).toFrame(), 1)
                    .concat(Series.of("idx", Integer.class, range(0, size)).toFrame(), 1)
                    .concat(DataFrame.of("dim_", trueSet.data()), 1)
                    .csv(realWriter, head);
        }
    }

    private static double[][] read(SearchProperties prop) throws IOException {
        Path input = Paths.get(prop.input());
        double[][] data = IOUtils.read(input);
        data = prop.norm() ? Op.normalize(data) : data;
        data = prop.extend() ? Op.concat(data, Op.fill(data.length, 1)) : data;
        return data;
    }

    private static long usedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

}
