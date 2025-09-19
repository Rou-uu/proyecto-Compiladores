package com.compiler.lexer.tokenizer;

public class LexRule {
    
    public final TokenType token;

    public final String regex;

    public final int priority;

    private static int ruleCount = 0;

    public LexRule(TokenType token, String regex) {
        this.token = token;
        this.regex = regex;
        this.priority = ruleCount++;
    }

    public TokenType getTokenType() {
        return token;
    }

    public String getRegex() {
        return regex;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public String toString() {
        return "LexRule{" + "token=" + token + ", regex='" + regex + '\'' + ", priority=" + priority + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        LexRule lexRule = (LexRule) obj;
        return priority == lexRule.priority && token.equals(lexRule.token) && (regex != null ? regex.equals(lexRule.regex) : lexRule.regex == null);
    }

    @Override
    public int hashCode() {
        int result = token.hashCode();
        result = 31 * result + (regex != null ? regex.hashCode() : 0);
        result = 31 * result + priority;
        return result;
    }
}
