package com.github.ivacoronel.server;

import lombok.experimental.UtilityClass;

@UtilityClass
public class EnumUtils {

    public static <T, E extends Enum<E> & IdentifierType<T>> E getByValue(final Class<E> enumClass, T value) {
        for (final E e : enumClass.getEnumConstants()) {
            if (e.getValue().equals(value)) {
                return e;
            }
        }

        throw new IllegalArgumentException("Wrong value " + value.toString() + " for type " + enumClass.getName());
    }

}