package com.idriss.tiktokjunior.statement;

import java.util.ArrayList;
import java.util.List;

public class InsertStatement implements Statement {

    private String tableName;
    private List<Object> values = new ArrayList<>();

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void addValue(Object value) {
        values.add(value);
    }

    public String getTableName() {
        return tableName;
    }

    public List<Object> getValues() {
        return values;
    }

    public void setValues(List<Object> values) {
        this.values = values;
    }
}