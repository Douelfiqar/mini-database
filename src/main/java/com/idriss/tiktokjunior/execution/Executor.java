package com.idriss.tiktokjunior.execution;

import com.idriss.tiktokjunior.statement.CreateTableStatement;
import com.idriss.tiktokjunior.statement.InsertStatement;
import com.idriss.tiktokjunior.statement.SelectStatement;
import com.idriss.tiktokjunior.statement.Statement;
import com.idriss.tiktokjunior.storage.TableStorage;

import java.io.IOException;
import java.util.List;

public class Executor {

    private final TableStorage storage;

    public Executor(TableStorage storage) {
        this.storage = storage;
    }

    public void executeTransaction(List<Statement> statements) throws IOException {
        storage.validateTransaction(statements);
        for (Statement statement : statements) {
            execute(statement);
        }
    }

    public Object execute(Statement statement)
            throws IOException {

        if (statement instanceof CreateTableStatement create) {

            storage.createTable(create);

            return "Table created";
        }

        if (statement instanceof InsertStatement insert) {

            storage.insert(insert);

            return "Row inserted";
        }

        if (statement instanceof SelectStatement select) {

            return storage.select(select);
        }

        throw new IllegalArgumentException(
                "Unsupported statement"
        );
    }
}