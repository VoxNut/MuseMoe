package com.javaweb.utils;

import com.javaweb.enums.BaseEnum;

import java.util.Arrays;

public class EnumUtil {
    public static <E extends Enum<E> & BaseEnum> E fromValue(Class<E> enumClass, String value) {
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> e.getValue().equals(value))
                .findFirst()
                .orElse(null);
    }
}
