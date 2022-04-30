package io.github.stepping1st.hh.utils;


import com.google.gson.Gson;
import org.apache.commons.lang3.math.NumberUtils;


public class TypeUtils {

    public static Class<?> type(Object o) {
        if (o == null) {
            return Object.class;
        }
        if (NumberUtils.isCreatable(o.toString())) {
            Number number = NumberUtils.createNumber(o.toString());
            if (Integer.class.isAssignableFrom(number.getClass())) {
                return Integer.class;
            }
            if (Long.class.isAssignableFrom(number.getClass())) {
                return Long.class;
            }
            if (Float.class.isAssignableFrom(number.getClass())) {
                return Float.class;
            }
            if (Double.class.isAssignableFrom(number.getClass())) {
                return Double.class;
            }
        }
        return String.class;
    }

    public static String toString(Object o) {
        if (o == null) {
            return "";
        }
        if (o instanceof String) {
            return String.valueOf(o);
        }
        if (o.getClass().isPrimitive() || Number.class.isAssignableFrom(o.getClass())) {
            return String.valueOf(o);
        }
        Gson gson = new Gson();
        return String.format("\"%s\"", gson.toJson(o));
    }

    public static Class<?> combine(Class<?> a, Class<?> b) {
        if (a == b) {
            return a;
        }
        if (a == Integer.class && b == Long.class || a == Long.class && b == Integer.class) {
            return Long.class;
        }
        if (Number.class.isAssignableFrom(a) && b == Double.class || a == Double.class && Number.class.isAssignableFrom(b)) {
            return Double.class;
        }
        if (Number.class.isAssignableFrom(a) && b == Float.class || a == Float.class && Number.class.isAssignableFrom(b)) {
            return Float.class;
        }
        if (Object.class == a) {
            return b;
        }
        if (Object.class == b) {
            return a;
        }
        return String.class;
    }

}
