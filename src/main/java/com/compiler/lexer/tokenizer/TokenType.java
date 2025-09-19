package com.compiler.lexer.tokenizer;

/**
 * Represents the type or category of a token in the lexical analysis process.
 * Token types are used to classify tokens into different categories such as
 * identifiers, keywords, operators, literals, etc.
 *
 * @author Rubén Alfaro
 * @version 1.0
 */
public class TokenType {

    /**
     * The name that identifies this token type.
     */
    public final String name;

    /**
     * Constructs a new TokenType with the specified name.
     *
     * @param name the name of the token type
     */
    public TokenType(String name) {
        this.name = name;
    }

    /**
     * Returns the name of this token type.
     *
     * @return the name of this token type
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a string representation of this token type.
     *
     * @return a string representation in the format "TokenType{name='...'}"
     */
    @Override
    public String toString() {
        return "TokenType{" + "name='" + name + '\'' + '}';
    }

    /**
     * Indicates whether some other object is "equal to" this token type.
     * Two token types are considered equal if they have the same name.
     *
     * @param obj the reference object with which to compare
     * @return true if this token type is the same as the obj argument; false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        TokenType type = (TokenType) obj;
        return name != null ? name.equals(type.name) : type.name == null;
    }

    /**
     * Returns a hash code value for this token type.
     *
     * @return a hash code value for this token type
     */
    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
