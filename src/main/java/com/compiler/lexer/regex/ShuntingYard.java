package com.compiler.lexer.regex;

import java.util.Stack;

/**
 * Utility class for regular expression parsing using the Shunting Yard
 * algorithm.
 * <p>
 * Provides methods to preprocess regular expressions by inserting explicit
 * concatenation operators, and to convert infix regular expressions to postfix
 * notation for easier parsing and NFA construction.
 */
/**
 * Utility class for regular expression parsing using the Shunting Yard
 * algorithm.
 */
public class ShuntingYard {

    /**
     * Default constructor for ShuntingYard.
     */
    public ShuntingYard() {
        // TODO: Implement constructor if needed
    }

    /**
     * Inserts the explicit concatenation operator ('·') into the regular
     * expression according to standard rules. This makes implicit
     * concatenations explicit, simplifying later parsing.
     *
     * @param regex Input regular expression (may have implicit concatenation).
     * @return Regular expression with explicit concatenation operators.
     */
    public static String insertConcatenationOperator(String regex) {
        // TODO: Implement insertConcatenationOperator
        /*
            Pseudocode:
            For each character in regex:
                - Append current character to output
                - If not at end of string:
                        - Check if current and next character form an implicit concatenation
                        - If so, append '·' to output
            Return output as string
         */
        String result = "";

        for (int i = 0; i  < regex.length(); i++) {
            char curr = regex.charAt(i);

            if (i + 1 == regex.length()) {
                result += curr;
                break;
            }

            char sig = regex.charAt(i + 1);

            boolean needConc = isConcNeeded(curr, sig);
            String aux = needConc ? "·" : "";

            result += curr + aux;

        }

        return result;
    }

    /**
     * Determines if an explicit concatenation operator is needed between two characters.
     * <p>
     * Concatenation is needed if:
     * <ul>
     *   <li>The left side is an operand, closing parenthesis, or a unary operator (*, +, ?)</li>
     *   <li>The right side is an operand or an opening parenthesis</li>
     * </ul>
     *
     * @param left  Current character in the regex.
     * @param right Next character in the regex.
     * @return true if concatenation should be inserted, false otherwise.
     */
    private static boolean isConcNeeded(char left, char right) {
        boolean primero = false, segundo = false;
        if (isOperand(left) || left == ')' || left == '+' || left == '?' || left == '*')
            primero = true;

        if (isOperand(right) || right == '(')
            segundo = true;

        return primero && segundo;
    }

    /**
     * Determines if the given character is an operand (not an operator or
     * parenthesis).
     *
     * @param c Character to evaluate.
     * @return true if it is an operand, false otherwise.
     */
    public static boolean isOperand(char c) {
        // TODO: Implement isOperand
        /*
        Pseudocode:
        Return true if c is not one of: '|', '*', '?', '+', '(', ')', '·'
         */
        if (c == '|' || c == '*' || c == '?' || c == '+' || c == '(' || c == ')' || c == '·') {
            return false;
        }

        return true;
    }

    /**
     * Converts an infix regular expression to postfix notation using the
     * Shunting Yard algorithm. This is useful for constructing NFAs from
     * regular expressions.
     *
     * @param infixRegex Regular expression in infix notation.
     * @return Regular expression in postfix notation.
     */
    public static String toPostfix(String infixRegex) {
        // TODO: Implement toPostfix
        /*
        Pseudocode:
        1. Define operator precedence map
        2. Preprocess regex to insert explicit concatenation operators
        3. For each character in regex:
            - If operand: append to output
            - If '(': push to stack
            - If ')': pop operators to output until '(' is found
            - If operator: pop operators with higher/equal precedence, then push current operator
        4. After loop, pop remaining operators to output
        5. Return output as string
         */
        String regex = insertConcatenationOperator(infixRegex);
        String res = "";
        Stack<Character> operadores = new Stack<>();

        for (int i = 0; i < regex.length(); i++) {
            char curr = regex.charAt(i);

            if (isOperand(curr)) {
                res += curr;
                continue;
            }

            switch (curr) {
                case '(':
                    operadores.add(curr);
                    break;
                
                case ')':
                    char aux = operadores.pop();
                    while (aux != '(') {
                        res += aux;
                        aux = operadores.pop();
                    }
                    break;
                
                default: // Default means it's neither an operand (variable) nor a parenthesis
                    while (!operadores.isEmpty() && operadores.peek() != '(' && findPrecedence(operadores.peek()) >= findPrecedence(curr)) {
                        res += operadores.pop();
                    }

                    operadores.push(curr);
                        break;
            }
        }

        while (!operadores.isEmpty()) {
            res += operadores.pop();
        }

        return res;
    }

    /**
     * Returns the precedence level of an operator.
     * <p>
     * Higher value → higher precedence.
     *
     * @param a Operator character.
     * @return Precedence value, or -1 if not an operator.
     */
    private static int findPrecedence(char a) {
        switch (a) {
            case '|': return 1;

            case '·': return 2;

            case '*':
            case '+':
            case '?': return 3;

            // Otros casos
            case '(':
            case ')': return -1;
            default: return -1;
        }
    }
}
