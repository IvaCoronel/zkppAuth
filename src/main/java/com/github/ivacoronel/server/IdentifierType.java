package com.github.ivacoronel.server;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Idan Rozenfeld
 */
@FunctionalInterface
public interface IdentifierType<V> {
    @JsonValue
    V getValue();
}
