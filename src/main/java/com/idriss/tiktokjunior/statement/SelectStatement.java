package com.idriss.tiktokjunior.statement;

import java.util.ArrayList;
import java.util.List;

public class SelectStatement implements Statement {

    private String tableName;

    private final List<String> columns =
            new ArrayList<>();

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void addColumn(String column) {
        columns.add(column);
    }

    public String getTableName() {
        return tableName;
    }

    public List<String> getColumns() {
        return columns;
    }
}