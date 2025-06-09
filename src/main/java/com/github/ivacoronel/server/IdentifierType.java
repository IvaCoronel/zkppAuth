package com.github.ivacoronel.server;

import com.fasterxml.jackson.annotation.JsonValue;


@FunctionalInterface
public interface IdentifierType<V> {
    @JsonValue
    V getValue();
}
