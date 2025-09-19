package com.compiler.lexer.tokenizer;

/**
 * Represents a lexical rule that defines how to recognize a specific token type.
 * Each rule consists of a regular expression pattern and the corresponding token type.
 * Rules are automatically assigned priorities based on their order of creation.
 *
 * @author Rubén Alfaro
 * @version 1.0
 */
public class LexRule {

    /**
     * The token type that this rule produces when matched.
     */
    public final TokenType token;

    /**
     * The regular expression pattern used to match input text.
     */
    public final String regex;

    /**
     * The priority of this rule, used for disambiguation when multiple rules match.
     * Lower numbers indicate higher priority.
     */
    public final int priority;

    /**
     * Counter to automatically assign priorities to rules based on creation order.
     */
    private static int ruleCount = 0;

    /**
     * Constructs a new lexical rule with the specified token type and regex pattern.
     * The rule is automatically assigned a priority based on its creation order.
     *
     * @param token the token type this rule produces
     * @param regex the regular expression pattern for matching
     */
    public LexRule(TokenType token, String regex) {
        this.token = token;
        this.regex = regex;
        this.priority = ruleCount++;
    }

    /**
     * Returns the token type that this rule produces.
     *
     * @return the token type for this rule
     */
    public TokenType getTokenType() {
        return token;
    }

    /**
     * Returns the regular expression pattern of this rule.
     *
     * @return the regex pattern for this rule
     */
    public String getRegex() {
        return regex;
    }

    /**
     * Returns the priority of this rule.
     *
     * @return the priority value (lower means higher priority)
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Returns a string representation of this lexical rule.
     *
     * @return a string representation in the format "LexRule{token=..., regex='...', priority=...}"
     */
    @Override
    public String toString() {
        return "LexRule{" + "token=" + token + ", regex='" + regex + '\'' + ", priority=" + priority + '}';
    }

    /**
     * Indicates whether some other object is "equal to" this lexical rule.
     * Two rules are considered equal if they have the same token, regex, and priority.
     *
     * @param obj the reference object with which to compare
     * @return true if this rule is the same as the obj argument; false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        LexRule lexRule = (LexRule) obj;
        return priority == lexRule.priority && token.equals(lexRule.token) && (regex != null ? regex.equals(lexRule.regex) : lexRule.regex == null);
    }

    /**
     * Returns a hash code value for this lexical rule.
     *
     * @return a hash code value for this rule
     */
    @Override
    public int hashCode() {
        int result = token.hashCode();
        result = 31 * result + (regex != null ? regex.hashCode() : 0);
        result = 31 * result + priority;
        return result;
    }
}
