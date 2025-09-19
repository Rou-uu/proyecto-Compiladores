package com.compiler.lexer.tokenizer;

/**
 * Represents a token in the lexical analysis process.
 * A token consists of a type and a value, representing a meaningful unit
 * in the source code that has been recognized by the tokenizer.
 *
 * @author Rubén Alfaro
 * @version 1.0
 */
public class Token {

    /**
     * The type of this token, indicating the category it belongs to
     * (e.g., identifier, keyword, operator, etc.).
     */
    public final TokenType type;

    /**
     * The actual string value of the token as it appears in the source code.
     */
    public final String val;

    /**
     * Constructs a new Token with the specified type and value.
     *
     * @param type the type of the token
     * @param val  the string value of the token
     */
    public Token(TokenType type, String val) {
        this.type = type;
        this.val = val;
    }

    /**
     * Returns the type of this token.
     *
     * @return the TokenType of this token
     */
    public TokenType getType() {
        return type;
    }

    /**
     * Returns the string value of this token.
     *
     * @return the string value of this token
     */
    public String getVal() {
        return val;
    }

    /**
     * Returns a string representation of this token.
     *
     * @return a string representation in the format "Token{type=..., val='...'}"
     */
    @Override
    public String toString() {
        return "Token{" + "type=" + type + ", val='" + val + '\'' +'}';
    }

    /**
     * Indicates whether some other object is "equal to" this token.
     * Two tokens are considered equal if they have the same type and value.
     *
     * @param obj the reference object with which to compare
     * @return true if this token is the same as the obj argument; false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Token token = (Token) obj;
        return type.equals(token.type) && (val != null ? val.equals(token.val) : token.val == null);
    }

    /**
     * Returns a hash code value for this token.
     *
     * @return a hash code value for this token
     */
    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (val != null ? val.hashCode() : 0);
        return result;
    }
}
