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

public class Tokenizer {
    private static class TokenMatch {
        final TokenType tokenType;
        final String value;
        final int startPosition;
        final int endPosition;
        
        TokenMatch(TokenType tokenType, String value, int startPosition, int endPosition) {
            this.tokenType = tokenType;
            this.value = value;
            this.startPosition = startPosition;
            this.endPosition = endPosition;
        }
    }

    private final DFA dfa;

    private final List<LexRule> rules;

    private final Set<Character> alphabet;

    private final String rulesFile = "resources/testRules.txt"; // Location of the file with the rules

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

    public List<LexRule> getRules() {
        return new ArrayList<>(rules);
    }

    public Set<Character> getAlphabet() {
        return alphabet;
    }

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
