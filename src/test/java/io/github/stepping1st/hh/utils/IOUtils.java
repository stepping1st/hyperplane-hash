package io.github.stepping1st.hh.utils;


import io.github.stepping1st.hh.column.Row;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public class IOUtils {

    public static List<Row<Object>> csv(String path) throws IOException {
        FileReader fileReader = new FileReader(path);
        BufferedReader reader = new BufferedReader(fileReader);
        List<Row<Object>> lines = new ArrayList<>();
        try {
            String headLine = reader.readLine();
            String[] head = OpUtils.split(headLine);
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                String[] split = OpUtils.split(line);
                Row<Object> row = new Row<>();
                for (int i = 0; i < split.length; i++) {
                    row.put(head[i], split[i]);
                }
                lines.add(row);
            }
        } finally {
            reader.close();
            fileReader.close();
        }
        return lines;
    }

    public static double[][] readGzip(InputStream input) throws IOException {
        GZIPInputStream stream = new GZIPInputStream(input);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        double[][] result = new double[0][];
        try {
            result = parse(reader);
        } finally {
            reader.close();
            stream.close();
            input.close();
        }
        return result;
    }

    public static double[][] read(Path path) throws IOException {
        InputStream inputStream = Files.newInputStream(path);
        if (path.getFileName().toString().endsWith("gz")) {
            return readGzip(inputStream);
        } else {
            return read(inputStream);
        }
    }

    public static double[][] read(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        double[][] result = new double[0][];
        try {
            result = parse(reader);
        } finally {
            reader.close();
            input.close();
        }
        return result;
    }

    private static double[][] parse(BufferedReader reader) throws IOException {
        List<double[]> list = new ArrayList<>();
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            String[] split = line.split(",");
            double[] ds = new double[split.length];
            for (int i = 0; i < split.length; i++) {
                ds[i] = Double.parseDouble(split[i]);
            }
            list.add(ds);
        }
        double[][] result = new double[list.size()][];
        for (int i = 0; i < result.length; i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    public static void mkdir(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
    }

    public static Stream<float[]> binToStream(String path, int n, int dim) throws Exception {
        FileInputStream fis = new FileInputStream(path);
        DataInputStream dis = new DataInputStream(fis);
        return IntStream.range(0, n).mapToObj(
                num -> {
                    float[] values = new float[dim];
                    try {
                        for (int i = 0; i < dim; i++) {
                            int intValue = dis.readInt();
                            int reversed = Integer.reverseBytes(intValue);
                            float v = Float.intBitsToFloat(reversed);
                            values[i] = v;
                        }
                        return values;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    public static void writeGzip(String path, Stream<String> values)
            throws IOException {
        try (FileOutputStream output = new FileOutputStream(path)) {
            Writer writer = new OutputStreamWriter(new GZIPOutputStream(output), StandardCharsets.UTF_8);
            write(writer, values);
        }
    }

    public static void write(String path, Stream<String> stream) throws IOException {
        FileWriter writer = new FileWriter(path);
        write(writer, stream);
    }

    public static void write(Writer writer, Stream<String> stream) throws IOException {
        try {
            stream.forEach(OpUtils.wrap(l -> {
                writer.append(l);
                writer.append("\n");
            }));
        } finally {
            writer.close();
        }
    }
}
