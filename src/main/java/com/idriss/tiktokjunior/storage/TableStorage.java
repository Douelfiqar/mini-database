package com.idriss.tiktokjunior.storage;

import com.idriss.tiktokjunior.statement.ColumnDefinition;
import com.idriss.tiktokjunior.statement.CreateTableStatement;
import com.idriss.tiktokjunior.statement.InsertStatement;
import com.idriss.tiktokjunior.statement.SelectStatement;
import com.idriss.tiktokjunior.statement.Statement;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableStorage {
    private final Path dataDirectory = Paths.get("data");

    public TableStorage() throws IOException {
        Files.createDirectories(dataDirectory);
    }

    public void validateTransaction(List<Statement> statements) throws IOException {
        Map<String, Integer> newTables = new HashMap<>();
        for (Statement statement : statements) {
            if (statement instanceof CreateTableStatement create) {
                String name = create.getTableName();
                if (Files.exists(dataDirectory.resolve(name + ".tbl"))
                        || newTables.containsKey(name)) {
                    throw new IllegalStateException("Table already exists: " + name);
                }
                newTables.put(name, create.getColumns().size());
            } else if (statement instanceof InsertStatement insert) {
                String name = insert.getTableName();
                int columnCount = newTables.containsKey(name)
                        ? newTables.get(name) : loadColumnNames(name).size();
                checkValueCount(insert, columnCount);
            } else {
                throw new IllegalArgumentException("Unsupported statement in transaction");
            }
        }
    }

    public void createTable(CreateTableStatement statement) throws IOException {
        Path tableFile = dataDirectory.resolve(
                statement.getTableName()+ ".tbl"
        );

        if (Files.exists(tableFile)) {
            throw new IllegalStateException(
                    "Table already exists: "
                            + statement.getTableName()
            );
        }

        try (BufferedWriter writer =
                     Files.newBufferedWriter(tableFile)) {

            writer.write(
                    "TABLE|" + statement.getTableName()
            );

            writer.newLine();

            for (ColumnDefinition column
                    : statement.getColumns()) {

                writer.write(
                        "COLUMN|"
                                + column.getValue()
                                + "|"
                                + column.getType()
                                + "|"
                                + (column.getLength() == null
                                ? ""
                                : column.getLength())
                                + "|"
                                + column.isPrimaryKey()
                );

                writer.newLine();
            }
        }
    }

    public void insert(InsertStatement statement)
            throws IOException {
        checkValueCount(statement, loadColumnNames(statement.getTableName()).size());

        Path dataFile = dataDirectory.resolve(
                statement.getTableName() + ".data"
        );

        String row = statement.getValues()
                .stream()
                .map(String::valueOf)
                .collect(
                        java.util.stream.Collectors.joining("|")
                );

        Files.writeString(
                dataFile,
                row + System.lineSeparator(),
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.APPEND
        );
    }

    public List<List<String>> select(
            SelectStatement statement
    ) throws IOException {

        List<String> tableColumns =
                loadColumnNames(statement.getTableName());

        Path dataFile = dataDirectory.resolve(
                statement.getTableName() + ".data"
        );

        List<Integer> indexes = new ArrayList<>();

        if (
                statement.getColumns().size() == 1 &&
                        statement.getColumns().get(0).equals("*")
        ) {

            for (int i = 0; i < tableColumns.size(); i++) {
                indexes.add(i);
            }

        } else {

            for (String requestedColumn
                    : statement.getColumns()) {

                int index =
                        tableColumns.indexOf(requestedColumn);

                if (index == -1) {
                    throw new IllegalArgumentException(
                            "Unknown column: "
                                    + requestedColumn
                    );
                }

                indexes.add(index);
            }
        }

        if (!Files.exists(dataFile)) {
            return List.of();
        }

        List<List<String>> results =
                new ArrayList<>();

        for (String line : Files.readAllLines(dataFile)) {

            String[] values =
                    line.split("\\|", -1);

            List<String> selectedRow =
                    new ArrayList<>();

            for (Integer index : indexes) {
                selectedRow.add(values[index]);
            }

            results.add(selectedRow);
        }

        return results;
    }

    private List<String> loadColumnNames(
            String tableName
    ) throws IOException {

        Path schemaFile = dataDirectory.resolve(
                tableName + ".tbl"
        );

        if (!Files.exists(schemaFile)) {
            throw new IllegalArgumentException("Table does not exist: " + tableName);
        }

        List<String> columns = new ArrayList<>();

        for (String line : Files.readAllLines(schemaFile)) {

            if (line.startsWith("COLUMN|")) {

                String[] parts =
                        line.split("\\|", -1);

                columns.add(parts[1]);
            }
        }

        return columns;
    }

    private void checkValueCount(InsertStatement statement, int columnCount) {
        if (statement.getValues().size() != columnCount) {
            throw new IllegalArgumentException(
                    "Expected " + columnCount + " values for table "
                            + statement.getTableName() + ", got "
                            + statement.getValues().size());
        }
    }
}
