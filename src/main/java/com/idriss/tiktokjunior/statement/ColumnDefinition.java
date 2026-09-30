package com.idriss.tiktokjunior.statement;

public class ColumnDefinition {
    private String value;
    private String type;
    private Long length;
    // default value nulll
    private boolean primaryKey;

    public ColumnDefinition() {
    }

    public ColumnDefinition(String value, String type, Long length, boolean primaryKey) {
        this.value = value;
        this.type = type;
        this.length = length;
        this.primaryKey = primaryKey;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }
}
