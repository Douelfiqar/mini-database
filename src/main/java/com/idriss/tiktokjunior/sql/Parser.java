package com.idriss.tiktokjunior.sql;

import com.idriss.tiktokjunior.statement.ColumnDefinition;
import com.idriss.tiktokjunior.statement.CreateTableStatement;
import com.idriss.tiktokjunior.statement.InsertStatement;
import com.idriss.tiktokjunior.statement.SelectStatement;
import com.idriss.tiktokjunior.statement.Statement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Parser {

    public Statement parse(List<Token> tokens) {
        if (tokens == null || tokens.isEmpty() || is(tokens, 0, TokenType.EOF)) {
            throw new IllegalArgumentException("Empty SQL command");
        }

        return switch (tokens.get(0).getType()) {
            case CREATE -> parseCreate(tokens);
            case INSERT -> parseInsert(tokens);
            case SELECT -> parseSelect(tokens);
            default -> throw new IllegalArgumentException(
                    "Unsupported SQL statement: " + tokens.get(0).getValue());
        };
    }

    private CreateTableStatement parseCreate(List<Token> tokens) {
        require(tokens, 1, TokenType.TABLE, "Expected TABLE after CREATE");
        String tableName = require(tokens, 2, TokenType.IDENTIFIER,
                "CREATE TABLE needs a table name").getValue();
        require(tokens, 3, TokenType.LEFT_PAREN,
                "Expected '(' after table name");

        CreateTableStatement statement = new CreateTableStatement();
        statement.setTableName(tableName);
        Set<String> columnNames = new HashSet<>();
        int i = 4;

        if (is(tokens, i, TokenType.RIGHT_PAREN)) {
            throw new IllegalArgumentException("CREATE TABLE needs at least one column");
        }

        while (true) {
            String name = require(tokens, i++, TokenType.IDENTIFIER,
                    "Expected column name").getValue();
            if (!columnNames.add(name)) {
                throw new IllegalArgumentException("Duplicate column: " + name);
            }

            ColumnDefinition column = new ColumnDefinition();
            column.setValue(name);
            TokenType type = i < tokens.size()
                    ? tokens.get(i).getType() : TokenType.EOF;
            if (type != TokenType.LONG && type != TokenType.STRING
                    && type != TokenType.VARCHAR) {
                throw new IllegalArgumentException("Expected LONG, STRING, or VARCHAR for column " + name);
            }
            column.setType(type.name());
            i++;

            if (type == TokenType.VARCHAR && is(tokens, i, TokenType.LEFT_PAREN)) {
                i++;
                String lengthText = require(tokens, i++, TokenType.NUMBER,
                        "VARCHAR needs a numeric length").getValue();
                long length = parseNumber(lengthText);
                if (length == 0) {
                    throw new IllegalArgumentException("VARCHAR length must be positive");
                }
                column.setLength(length);
                require(tokens, i++, TokenType.RIGHT_PAREN,
                        "Expected ')' after VARCHAR length");
            }

            if (is(tokens, i, TokenType.PRIMARY)) {
                i++;
                require(tokens, i++, TokenType.KEY, "Expected KEY after PRIMARY");
                column.setPrimaryKey(true);
            }
            statement.addColumn(column);

            if (is(tokens, i, TokenType.COMMA)) {
                i++;
                continue;
            }
            require(tokens, i++, TokenType.RIGHT_PAREN,
                    "Expected ',' or ')' after column definition");
            require(tokens, i, TokenType.EOF,
                    "Unexpected token after CREATE TABLE");
            return statement;
        }
    }

    private InsertStatement parseInsert(List<Token> tokens) {
        require(tokens, 1, TokenType.INTO, "Expected INTO after INSERT");
        String tableName = require(tokens, 2, TokenType.IDENTIFIER,
                "INSERT INTO needs a table name").getValue();
        require(tokens, 3, TokenType.VALUES, "Expected VALUES after table name");
        require(tokens, 4, TokenType.LEFT_PAREN, "Expected '(' after VALUES");

        InsertStatement statement = new InsertStatement();
        statement.setTableName(tableName);
        int i = 5;
        if (is(tokens, i, TokenType.RIGHT_PAREN)) {
            throw new IllegalArgumentException("VALUES needs at least one value");
        }

        while (true) {
            Token value = i < tokens.size() ? tokens.get(i) : null;
            if (value != null && value.getType() == TokenType.NUMBER) {
                statement.addValue(parseNumber(value.getValue()));
            } else if (value != null && value.getType() == TokenType.STRING_LITERAL) {
                statement.addValue(value.getValue());
            } else {
                throw new IllegalArgumentException("Expected a number or quoted string in VALUES");
            }
            i++;

            if (is(tokens, i, TokenType.COMMA)) {
                i++;
                continue;
            }
            require(tokens, i++, TokenType.RIGHT_PAREN,
                    "Expected ',' or ')' after value");
            require(tokens, i, TokenType.EOF,
                    "Unexpected token after INSERT");
            return statement;
        }
    }

    private SelectStatement parseSelect(List<Token> tokens) {
        SelectStatement statement = new SelectStatement();
        int i = 1;

        if (is(tokens, i, TokenType.STAR)) {
            statement.addColumn("*");
            i++;
        } else {
            statement.addColumn(require(tokens, i++, TokenType.IDENTIFIER,
                    "SELECT needs a column or '*'").getValue());
            while (is(tokens, i, TokenType.COMMA)) {
                i++;
                statement.addColumn(require(tokens, i++, TokenType.IDENTIFIER,
                        "Expected column name after ','").getValue());
            }
        }

        require(tokens, i++, TokenType.FROM,
                "Expected FROM after SELECT columns");
        statement.setTableName(require(tokens, i++, TokenType.IDENTIFIER,
                "SELECT is missing a table name").getValue());
        require(tokens, i, TokenType.EOF,
                "Unexpected token after SELECT table name");
        return statement;
    }

    private static boolean is(List<Token> tokens, int index, TokenType type) {
        return index < tokens.size() && tokens.get(index).getType() == type;
    }

    private static Token require(List<Token> tokens, int index,
                                 TokenType type, String message) {
        if (!is(tokens, index, type)) {
            throw new IllegalArgumentException(message);
        }
        return tokens.get(index);
    }

    private static long parseNumber(String text) {
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Number out of range: " + text, e);
        }
    }
}
