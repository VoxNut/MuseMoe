package com.javaweb.utils;

import com.javaweb.exception.EnumNotFoundException;

import java.util.Arrays;

public class EnumUtil {
    public static <E extends Enum<E> & BaseEnum> E fromValue(Class<E> enumClass, String value) {
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> e.getValue().equals(value))
                .findFirst()
                .orElseThrow(() -> new EnumNotFoundException(enumClass.getSimpleName() + " Not Found for value: " + value));
    }
}
