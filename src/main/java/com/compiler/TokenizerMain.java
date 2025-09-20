package com.compiler;

import java.util.List;

import com.compiler.lexer.tokenizer.Token;
import com.compiler.lexer.tokenizer.Tokenizer;

/**
 * Main class for testing the Tokenizer with simple input.
 * Change the input string to test different cases with the rules from testRules.txt.
 */
public class TokenizerMain {

    public static void main(String[] args) {
        // CHANGE THIS INPUT TO TEST DIFFERENT STRINGS
        String input = "abba <=> ba $ 967A57646a";

        System.out.println("=== Tokenizer Test ===");
        System.out.println("Input: '" + input + "'");
        System.out.println();

        try {
            // Initialize tokenizer (reads rules from testRules.txt)
            Tokenizer tokenizer = new Tokenizer();

            // Show loaded rules
            System.out.println("Loaded Rules:");
            tokenizer.getRules().forEach(rule -> {
                System.out.println("  '" + rule.getRegex() + "' -> " + rule.getTokenType().getName());
            });
            System.out.println();

            // Tokenize the input
            List<Token> tokens = tokenizer.tokenize(input);

            // Print results
            System.out.println("Tokenization Results:");
            System.out.println("Found " + tokens.size() + " tokens:");
            System.out.println();

            for (int i = 0; i < tokens.size(); i++) {
                Token token = tokens.get(i);
                System.out.println("Token " + (i + 1) + ":");
                System.out.println("  Type: " + (token.getType() != null ? token.getType().getName() : "NULL"));
                System.out.println("  Value: '" + token.getVal() + "'");
                System.out.println();
            }

        } catch (Exception e) {
            System.err.println("Error during tokenization: " + e.getMessage());
            e.printStackTrace();
        }
    }
}