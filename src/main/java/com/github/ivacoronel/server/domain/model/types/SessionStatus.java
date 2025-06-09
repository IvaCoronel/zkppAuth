package com.github.ivacoronel.server.domain.model.types;

import com.github.ivacoronel.server.EnumUtils;
import com.github.ivacoronel.server.IdentifierType;
import java.util.Objects;

public enum SessionStatus implements IdentifierType<Integer> {
    VALIDATED(1), WAITING(2), INVALIDATED(3), INITIATING(4);

    private final int id;

    SessionStatus(int id) {
        this.id = id;
    }

    public static SessionStatus byValue(int value) {
        if (Objects.nonNull(value)) {
            return EnumUtils.getByValue(SessionStatus.class, value);
        }

        return null;
    }

    @Override
    public Integer getValue() {
        return id;
    }
}
