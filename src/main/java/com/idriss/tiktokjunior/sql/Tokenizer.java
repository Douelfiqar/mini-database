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

            i++;
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

            default ->
                    TokenType.IDENTIFIER;
        };
    }
}