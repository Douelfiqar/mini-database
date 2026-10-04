package com.idriss.tiktokjunior.sql;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {

    public List<Token> tokenize(String input) {

        List<Token> tokens = new ArrayList<>();

        int i = 0;

        while (i < input.length()) {

            char c = input.charAt(i);

            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            if (c == '(') {
                tokens.add(
                        new Token(
                                TokenType.LEFT_PAREN,
                                "(",
                                null
                        )
                );

                i++;
                continue;
            }

            if (c == ')') {
                tokens.add(
                        new Token(
                                TokenType.RIGHT_PAREN,
                                ")",
                                null
                        )
                );

                i++;
                continue;
            }

            if (c == ',') {
                tokens.add(
                        new Token(
                                TokenType.COMMA,
                                ",",
                                null
                        )
                );

                i++;
                continue;
            }
            if (Character.isDigit(c)) {
                int start = i;

                while (i < input.length()
                        && Character.isDigit(input.charAt(i))) {
                    i++;
                }

                String number = input.substring(start, i);
                tokens.add(new Token(TokenType.NUMBER, number, null));
                continue;
            }
            if (c == '\'') {
                int start = ++i;

                while (i < input.length() && input.charAt(i) != '\'') {
                    i++;
                }

                if (i == input.length()) {
                    throw new IllegalArgumentException("Unterminated string literal");
                }

                tokens.add(new Token(
                        TokenType.STRING_LITERAL,
                        input.substring(start, i),
                        null
                ));

                i++; // Move past the closing quote
                continue;
            }
            if (c == '*') {
                tokens.add(new Token(TokenType.STAR, "*", null));
                i++;
                continue;
            }
            if (Character.isLetter(c)) {

                int start = i;

                while (
                        i < input.length()
                                && Character.isLetterOrDigit(
                                input.charAt(i)
                        )
                ) {
                    i++;
                }

                String word =
                        input.substring(start, i);

                TokenType type =
                        getWordType(word);

                tokens.add(
                        new Token(type, word, null)
                );

                continue;
            }

            throw new IllegalArgumentException("Unexpected character: " + c);
        }

        tokens.add(
                new Token(
                        TokenType.EOF,
                        "",
                        null
                )
        );

        return tokens;
    }

    private TokenType getWordType(String word) {

        return switch (word.toUpperCase()) {

            case "CREATE" ->
                    TokenType.CREATE;

            case "TABLE" ->
                    TokenType.TABLE;

            case "PRIMARY" ->
                    TokenType.PRIMARY;

            case "KEY" ->
                    TokenType.KEY;

            case "LONG" ->
                    TokenType.LONG;

            case "VARCHAR" ->
                    TokenType.VARCHAR;
            case "INSERT" -> TokenType.INSERT;
            case "INTO" -> TokenType.INTO;
            case "VALUES" -> TokenType.VALUES;
            case "SELECT" -> TokenType.SELECT;
            case "FROM" -> TokenType.FROM;
            case "STRING" -> TokenType.STRING;
            default ->
                    TokenType.IDENTIFIER;
        };
    }
}