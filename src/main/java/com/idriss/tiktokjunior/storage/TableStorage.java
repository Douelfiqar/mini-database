package com.idriss.tiktokjunior.storage;

import com.idriss.tiktokjunior.statement.ColumnDefinition;
import com.idriss.tiktokjunior.statement.CreateTableStatement;
import com.idriss.tiktokjunior.statement.InsertStatement;
import com.idriss.tiktokjunior.statement.SelectStatement;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TableStorage {
    private final Path dataDirectory = Paths.get("data");

    public TableStorage() throws IOException {
        Files.createDirectories(dataDirectory);
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

        Path schemaFile = dataDirectory.resolve(
                statement.getTableName() + ".schema"
        );

        if (!Files.exists(schemaFile)) {
            throw new IllegalArgumentException(
                    "Table does not exist: "
                            + statement.getTableName()
            );
        }

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

        if (!Files.exists(dataFile)) {
            return List.of();
        }

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
                tableName + ".schema"
        );

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
}
