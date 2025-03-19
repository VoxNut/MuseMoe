package com.javaweb.utils;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MapUtil {

    public static <T> T getObject(Map<String, Object> params, String key, Class<T> tClass) {
        Object value = params.getOrDefault(key, null);
        if (value != null) {
            switch (tClass.getTypeName()) {
                case "java.lang.Long":
                    value = value != "" ? Long.valueOf(value.toString()) : null;
                    break;
                case "java.lang.Integer":
                    value = value != "" ? Integer.valueOf(value.toString()) : null;
                    break;
                case "java.lang.String":
                    value = value != "" ? value.toString() : null;
                    break;
            }
            return tClass.cast(value);
        }
        return null;
    }

    public static <K, V extends Comparable<V>> Map<K, V> sortMapDescendingByValue(Map<K, V> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (ex, replacement) -> ex,
                        LinkedHashMap::new
                ));

    }
}
