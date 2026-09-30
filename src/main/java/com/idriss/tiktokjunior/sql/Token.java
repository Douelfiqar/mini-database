package com.idriss.tiktokjunior.sql;

public class Token {

    private final TokenType type;
    private final String value;
    private final Long length;

    public Token(TokenType type, String value, Long length) {
        this.type = type;
        this.value = value;
        this.length = length;
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public Long getLength() {
        return length;
    }

    @Override
    public String toString() {
        return type + "(" + value + ")";
    }
}