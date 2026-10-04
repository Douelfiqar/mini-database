package com.idriss.tiktokjunior.sql;

import com.idriss.tiktokjunior.statement.ColumnDefinition;
import com.idriss.tiktokjunior.statement.CreateTableStatement;
import com.idriss.tiktokjunior.statement.InsertStatement;
import com.idriss.tiktokjunior.statement.SelectStatement;
import com.idriss.tiktokjunior.statement.Statement;

import java.util.List;

public class Parser {

    public Statement parse(List<Token> tokens) {

        if (tokens == null) {
            throw new IllegalArgumentException("Empty SQL command");
        }

        Token firstKeyword = tokens.get(0);
        Token secondKeyword = tokens.get(1);

        if (
                firstKeyword.getType() == TokenType.CREATE &&
                        secondKeyword.getType() == TokenType.TABLE
        ) {

            CreateTableStatement statement = new CreateTableStatement();

            // CREATE TABLE users
            //              ↑
            statement.setTableName(tokens.get(2).getValue());

            ColumnDefinition column = new ColumnDefinition();

            int depth = 0;
            int i = 3;

            while (i < tokens.size()) {

                Token token = tokens.get(i);
                TokenType type = token.getType();

                // (
                if (type == TokenType.LEFT_PAREN) {
                    depth++;
                    i++;
                    continue;
                }

                // )
                if (type == TokenType.RIGHT_PAREN) {
                    depth--;

                    // End of CREATE TABLE (...)
                    if (depth == 0) {
                        statement.addColumn(column);
                        return statement;
                    }

                    i++;
                    continue;
                }

                // End of one column
                if (type == TokenType.COMMA && depth == 1) {

                    statement.addColumn(column);
                    column = new ColumnDefinition();

                } else if (type == TokenType.IDENTIFIER) {

                    column.setValue(token.getValue());

                } else if (type == TokenType.LONG) {

                    column.setType("LONG");

                } else if (type == TokenType.VARCHAR) {

                    column.setType("VARCHAR");

                } else if (type == TokenType.STRING) {

                    column.setType("STRING");

                } else if (type == TokenType.NUMBER) {

                    column.setLength(
                            Long.parseLong(token.getValue())
                    );

                } else if (
                        type == TokenType.PRIMARY &&
                                i + 1 < tokens.size() &&
                                tokens.get(i + 1).getType() == TokenType.KEY
                ) {

                    column.setPrimaryKey(true);

                    // Skip KEY because we already processed PRIMARY KEY
                    i++;
                }

                i++;
            }

            return statement;
        }

        else if (
                firstKeyword.getType() == TokenType.INSERT &&
                        secondKeyword.getType() == TokenType.INTO
        ) {

            InsertStatement statement = new InsertStatement();

            // INSERT INTO users
            //             ↑
            statement.setTableName(
                    tokens.get(2).getValue()
            );

            int i = 3;

            while (i < tokens.size()) {

                Token token = tokens.get(i);
                TokenType type = token.getType();

                if (type == TokenType.EOF) {
                    return statement;
                }

                if (type == TokenType.NUMBER) {

                    statement.addValue(
                            Long.parseLong(token.getValue())
                    );

                } else if (type == TokenType.STRING_LITERAL) {

                    statement.addValue(
                            token.getValue()
                    );
                }

                i++;
            }

            return statement;
        } else if (
                firstKeyword.getType() == TokenType.SELECT
        ) {

            SelectStatement statement =
                    new SelectStatement();

            int i = 1;

            // Read columns until FROM
            while (i < tokens.size()
                    && tokens.get(i).getType() != TokenType.FROM
                    && tokens.get(i).getType() != TokenType.EOF) {

                Token token = tokens.get(i);

                if (token.getType() == TokenType.STAR) {

                    statement.addColumn("*");

                } else if (
                        token.getType() == TokenType.IDENTIFIER
                ) {

                    statement.addColumn(
                            token.getValue()
                    );
                }

                i++;
            }
            if (i >= tokens.size()
                    || tokens.get(i).getType() != TokenType.FROM) {
                throw new IllegalArgumentException("SELECT is missing FROM");
            }
            // currently pointing at FROM
            i++;
            if (i >= tokens.size()
                    || tokens.get(i).getType() != TokenType.IDENTIFIER) {
                throw new IllegalArgumentException("SELECT is missing a table name");
            }
            // next token should be table name
            statement.setTableName(
                    tokens.get(i).getValue()
            );

            return statement;
        }

        throw new IllegalArgumentException(
                "Unsupported SQL statement"
        );
    }

}