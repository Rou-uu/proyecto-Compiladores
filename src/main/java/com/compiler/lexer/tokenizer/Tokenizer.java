package com.compiler.lexer.tokenizer;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.compiler.utility.FileReader;

import com.compiler.lexer.NfaToDfaConverter;
import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;
import com.compiler.lexer.tokenizer.MultiRuleBuilder.ComplexNFA;

/**
 * A lexical analyzer that converts input text into a sequence of tokens.
 * The tokenizer reads lexical rules from a configuration file, builds a DFA
 * from those rules, and uses it to recognize tokens in input text.
 *
 * @author Rubén Alfaro
 * @version 1.0
 */
public class Tokenizer {
    /**
     * Represents a potential token match found during tokenization.
     * Used internally to track the best match found so far.
     */
    private static class TokenMatch {
        /** The type of token that was matched. */
        final TokenType tokenType;

        /** The actual text value that was matched. */
        final String value;

        /** The starting position of the match in the input. */
        final int startPosition;

        /** The ending position of the match in the input. */
        final int endPosition;

        /**
         * Constructs a new TokenMatch.
         *
         * @param tokenType     the type of the matched token
         * @param value         the matched text value
         * @param startPosition the start position in the input
         * @param endPosition   the end position in the input
         */
        TokenMatch(TokenType tokenType, String value, int startPosition, int endPosition) {
            this.tokenType = tokenType;
            this.value = value;
            this.startPosition = startPosition;
            this.endPosition = endPosition;
        }
    }

    /**
     * The DFA used for token recognition.
     */
    private final DFA dfa;

    /**
     * The list of lexical rules loaded from the configuration file.
     */
    private final List<LexRule> rules;

    /**
     * The alphabet (set of characters) extracted from all rules.
     */
    private final Set<Character> alphabet;

    /**
     * Location of the file containing the lexical rules.
     */
    private final String rulesFile = "resources/testRules.txt";

    /**
     * Constructs a new Tokenizer by loading rules from the default configuration file
     * and building the corresponding DFA for token recognition.
     *
     * @throws IllegalStateException if no lexical rules are found in the configuration file
     */
    public Tokenizer() {
        rules = findRulesFromFile(rulesFile);
        if (rules == null || rules.isEmpty()) {
            throw new IllegalStateException("No lexical rules found in the specified file: " + rulesFile);
        }

        alphabet = extractAlphabet(rules);

        MultiRuleBuilder builder = new MultiRuleBuilder();
        ComplexNFA complexNFA = builder.buildNfa(rules);
        dfa = NfaToDfaConverter.convertComplexNfaToDfa(complexNFA, alphabet);
    }

    /**
     * Tokenizes the given input string into a list of tokens.
     * The input is split by whitespace, and each word is matched against
     * the configured lexical rules using the DFA.
     *
     * @param input the input string to tokenize
     * @return a list of tokens recognized from the input
     * @throws IllegalArgumentException if an unrecognized token is encountered
     */
    public List<Token> tokenize(String input) {
        ArrayList<Token> tokens = new ArrayList<>();

        // Split input by spaces to get individual tokens
        String[] tokenStrings = input.trim().split("\\s+");

        for (String tokenString : tokenStrings) {
            if (tokenString.isEmpty()) {
                continue; // Skip empty strings
            }

            TokenMatch match = longestMatch(tokenString, 0);

            if (match != null) {
                Token token = new Token(match.tokenType, match.value);
                tokens.add(token);
            } else {
                throw new IllegalArgumentException("Unrecognized token: '" + tokenString + "'");
            }
        }

        return tokens;
    }

    /**
     * Finds the longest possible token match starting at the given position
     * in the input string using the DFA.
     *
     * @param input the input string to match against
     * @param pos   the starting position in the input string
     * @return the longest TokenMatch found, or null if no match is possible
     */
    public TokenMatch longestMatch(String input, int pos) {
        DfaState curr = dfa.startState;
        TokenMatch lastMatch = null;
        int currPos = pos;

        while (currPos < input.length() && curr != null) {
            char symbol = input.charAt(currPos);

            if (curr.isFinal()) {
                String matchVal = input.substring(pos, currPos);
                lastMatch = new TokenMatch(curr.getTokenType(), matchVal, pos, currPos);
            } else {
                lastMatch = null;
            }

            curr = curr.getTransition(symbol);
            if (curr != null) {
                currPos++;
            }

        }

        if (curr != null && curr.isFinal()) {
            String matchVal = input.substring(pos, currPos);
            lastMatch = new TokenMatch(curr.getTokenType(), matchVal, pos, currPos);
        } else {
            lastMatch = null;
        }

        return lastMatch;
    }

    /**
     * Returns a copy of the lexical rules used by this tokenizer.
     *
     * @return a new list containing copies of all lexical rules
     */
    public List<LexRule> getRules() {
        return new ArrayList<>(rules);
    }

    /**
     * Returns the alphabet (set of characters) extracted from the lexical rules.
     *
     * @return the set of characters that can appear in tokens
     */
    public Set<Character> getAlphabet() {
        return alphabet;
    }

    /**
     * Reads and parses lexical rules from the specified file.
     * Each line in the file should be in the format: "regex;tokenName"
     * Empty lines and lines starting with '#' are ignored as comments.
     *
     * @param filePath the path to the rules file
     * @return a list of LexRule objects parsed from the file
     */
    private ArrayList<LexRule> findRulesFromFile(String filePath) {
        ArrayList<LexRule> rules = new ArrayList<>();
        ArrayList<String> lines = FileReader.readLines(filePath);

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String[] parts = line.split(";", 2);
            if (parts.length == 2) {
                String regex = parts[0].trim();
                String tokenName = parts[1].trim();

                // Expand character classes like [a-zA-Z0-9]
                String expandedRegex = expandCharacterClasses(regex);

                TokenType tokenType = new TokenType(tokenName);
                LexRule rule = new LexRule(tokenType, expandedRegex);
                rules.add(rule);
            }
        }

        return rules;
    }

    /**
     * Expands character classes in regex patterns (e.g., [a-z] becomes (a|b|c|...)).
     * Character classes are enclosed in square brackets and may contain ranges.
     *
     * @param regex the regex pattern that may contain character classes
     * @return the regex with character classes expanded to union patterns
     */
    private String expandCharacterClasses(String regex) {
        StringBuilder result = new StringBuilder();
        int i = 0;

        while (i < regex.length()) {
            if (regex.charAt(i) == '[') {
                // Find the matching closing bracket
                int endBracket = regex.indexOf(']', i);
                if (endBracket == -1) {
                    // No matching bracket, treat as literal
                    result.append(regex.charAt(i));
                    i++;
                    continue;
                }

                // Extract the content inside brackets
                String content = regex.substring(i + 1, endBracket);
                String expanded = expandCharacterClass(content);
                result.append("(").append(expanded).append(")");
                i = endBracket + 1; // Skip past the closing bracket
            } else {
                result.append(regex.charAt(i));
                i++;
            }
        }

        return result.toString();
    }

    /**
     * Expands the content of a single character class into a union pattern.
     * Handles both individual characters and character ranges (e.g., "a-z").
     *
     * @param content the content inside square brackets (e.g., "a-zA-Z0-9")
     * @return a union pattern string (e.g., "a|b|c|...|A|B|C|...|0|1|2|...")
     */
    private String expandCharacterClass(String content) {
        ArrayList<Character> characters = new ArrayList<>();
        int i = 0;

        while (i < content.length()) {
            char c = content.charAt(i);

            // Check if this is a range (x-y)
            if (i + 2 < content.length() && content.charAt(i + 1) == '-') {
                char start = c;
                char end = content.charAt(i + 2);

                // Add all characters in the range
                for (char rangeChar = start; rangeChar <= end; rangeChar++) {
                    characters.add(rangeChar);
                }
                i += 3; // Skip the range (start, -, end)
            } else {
                // Single character
                characters.add(c);
                i++;
            }
        }

        // Convert to union pattern: a|b|c (no trailing |)
        if (characters.isEmpty()) {
            return "";
        } else if (characters.size() == 1) {
            return String.valueOf(characters.get(0));
        } else {
            StringBuilder result = new StringBuilder();
            for (int j = 0; j < characters.size(); j++) {
                if (j > 0) {
                    result.append("|");
                }
                result.append(characters.get(j));
            }
            return result.toString();
        }
    }

    /**
     * Extracts the alphabet (set of input characters) from all lexical rules.
     * Regex operators are excluded from the alphabet, only literal characters are included.
     *
     * @param rules the list of lexical rules to extract characters from
     * @return a set containing all literal characters that can appear in tokens
     */
    private Set<Character> extractAlphabet(List<LexRule> rules) {
        Set<Character> alphabet = new HashSet<>();

        // Define regex operators that should NOT be included in the alphabet
        Set<Character> regexSymbols = new HashSet<>();
        regexSymbols.add('|');  // Union
        regexSymbols.add('*');  // Kleene star
        regexSymbols.add('?');  // Zero or one
        regexSymbols.add('+');  // One or more
        regexSymbols.add('(');  // Group start
        regexSymbols.add(')');  // Group end
        regexSymbols.add('·');  // Concatenation symbol

        for (LexRule rule : rules) {
            String regex = rule.getRegex();

            for (int i = 0; i < regex.length(); i++) {
                char c = regex.charAt(i);

                // Add non-regex symbols to alphabet
                if (!regexSymbols.contains(c)) {
                    alphabet.add(c);
                }
            }
        }

        return alphabet;
    }
}
