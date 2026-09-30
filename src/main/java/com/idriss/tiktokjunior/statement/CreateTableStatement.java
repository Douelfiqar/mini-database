package com.idriss.tiktokjunior.statement;

import java.util.ArrayList;
import java.util.List;

public class CreateTableStatement implements Statement {
    private String tableName;
    private List<ColumnDefinition> columns;

    public CreateTableStatement() {
        this.columns = new ArrayList<>();
    }

    public CreateTableStatement(String tableName, List<ColumnDefinition> columns) {
        this.tableName = tableName;
        this.columns = columns;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<ColumnDefinition> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnDefinition> columns) {
        this.columns = columns;
    }

    public void addColumn(ColumnDefinition columnDefinition) {
        columns.add(columnDefinition);
    }
}
