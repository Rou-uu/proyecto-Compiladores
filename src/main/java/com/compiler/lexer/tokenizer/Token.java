package com.compiler.lexer.tokenizer;

public class Token {

    public final TokenType type;

    public final String val;

    public Token(TokenType type, String val) {
        this.type = type;
        this.val = val;
    }

    public TokenType getType() {
        return type;
    }

    public String getVal() {
        return val;
    }

    @Override
    public String toString() {
        return "Token{" + "type=" + type + ", val='" + val + '\'' +'}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Token token = (Token) obj;
        return type.equals(token.type) && (val != null ? val.equals(token.val) : token.val == null);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (val != null ? val.hashCode() : 0);
        return result;
    }
}
