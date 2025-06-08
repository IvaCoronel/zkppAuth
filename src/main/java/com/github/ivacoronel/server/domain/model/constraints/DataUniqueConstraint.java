package com.github.ivacoronel.server.domain.model.constraints;

public interface DataUniqueConstraint {

    String getConstraintName();

    String[] getFieldNames();
}
