package com.compiler.lexer.regex;

import java.util.Stack;

import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.State;
import com.compiler.lexer.nfa.Transition;

/**
 * RegexParser
 * -----------
 * This class provides functionality to convert infix regular expressions into nondeterministic finite automata (NFA)
 * using Thompson's construction algorithm. It supports standard regex operators: concatenation (·), union (|),
 * Kleene star (*), optional (?), and plus (+). The conversion process uses the Shunting Yard algorithm to transform
 * infix regex into postfix notation, then builds the corresponding NFA.
 *
 * Features:
 * - Parses infix regular expressions and converts them to NFA.
 * - Supports regex operators: concatenation, union, Kleene star, optional, plus.
 * - Implements Thompson's construction rules for NFA generation.
 *
 * Example usage:
 * <pre>
 *     RegexParser parser = new RegexParser();
 *     NFA nfa = parser.parse("a(b|c)*");
 * </pre>
 */
/**
 * Parses regular expressions and constructs NFAs using Thompson's construction.
 */
public class RegexParser {
    /**
     * Default constructor for RegexParser.
     */
    public RegexParser() {
        // TODO: Implement constructor if needed
    }

    /**
     * Converts an infix regular expression to an NFA.
     *
     * @param infixRegex The regular expression in infix notation.
     * @return The constructed NFA.
     */
    public NFA parse(String infixRegex) {
        // TODO: Implement parse
        // Pseudocode: Convert infix to postfix, then build NFA from postfix
        String regex = ShuntingYard.toPostfix(infixRegex);
        NFA nfa = buildNfaFromPostfix(regex);
        nfa.endState.isFinal = true;
        return nfa;
    }

    /**
     * Builds an NFA from a postfix regular expression.
     *
     * @param postfixRegex The regular expression in postfix notation.
     * @return The constructed NFA.
     */
    private NFA buildNfaFromPostfix(String postfixRegex) {
        Stack<NFA> stack = new Stack<>();
        
        for (int i = 0; i < postfixRegex.length(); i++) {
            char c = postfixRegex.charAt(i);
            
            if (isOperand(c)) {
                stack.push(createNfaForCharacter(c));
            } else {
                switch (c) {
                    case '·':
                        handleConcatenation(stack);
                        break;
                    case '|':
                        handleUnion(stack);
                        break;
                    case '*':
                        handleKleeneStar(stack);
                        break;
                    case '?':
                        handleOptional(stack);
                        break;
                    case '+':
                        handlePlus(stack);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown operator: " + c);
                }
            }
        }
        
        return stack.pop();
    }

    /**
     * Handles the '?' operator (zero or one occurrence).
     * Pops an NFA from the stack and creates a new NFA that accepts zero or one
     * occurrence.
     * 
     * @param stack The NFA stack.
     */
    private void handleOptional(Stack<NFA> stack) {
        NFA nfa = stack.pop();
        
        State newStart = new State();
        State newEnd = new State();
        
        newStart.transitions.add(new Transition(null, nfa.startState));
        newStart.transitions.add(new Transition(null, newEnd));
        
        nfa.endState.transitions.add(new Transition(null, newEnd));
        
        NFA result = new NFA(newStart, newEnd);
        stack.push(result);
    }

    /**
     * Handles the '+' operator (one or more occurrences).
     * Pops an NFA from the stack and creates a new NFA that accepts one or more
     * occurrences.
     * 
     * @param stack The NFA stack.
     */
    private void handlePlus(Stack<NFA> stack) {
        NFA nfa = stack.pop();
        
        State newStart = new State();
        State newEnd = new State();
        
        newStart.transitions.add(new Transition(null, nfa.startState));
        
        nfa.endState.transitions.add(new Transition(null, nfa.startState));
        nfa.endState.transitions.add(new Transition(null, newEnd));
        
        NFA result = new NFA(newStart, newEnd);
        stack.push(result);
    }

    /**
     * Creates an NFA for a single character.
     * 
     * @param c The character to create an NFA for.
     * @return The constructed NFA.
     */
    private NFA createNfaForCharacter(char c) {
        State startState = new State();
        State endState = new State();
        startState.transitions.add(new Transition(c, endState));
        return new NFA(startState, endState);
    }

    /**
     * Handles the concatenation operator (·).
     * Pops two NFAs from the stack and connects them in sequence.
     * 
     * @param stack The NFA stack.
     */
    private void handleConcatenation(Stack<NFA> stack) {
        NFA second = stack.pop();
        NFA first = stack.pop();
        
        first.endState.transitions.add(new Transition(null, second.startState));
        
        NFA result = new NFA(first.startState, second.endState);
        stack.push(result);
    }

    /**
     * Handles the union operator (|).
     * Pops two NFAs from the stack and creates a new NFA that accepts either.
     * 
     * @param stack The NFA stack.
     */
    private void handleUnion(Stack<NFA> stack) {
        NFA second = stack.pop();
        NFA first = stack.pop();
        
        State newStart = new State();
        State newEnd = new State();
        
        newStart.transitions.add(new Transition(null, first.startState));
        newStart.transitions.add(new Transition(null, second.startState));
        
        first.endState.transitions.add(new Transition(null, newEnd));
        second.endState.transitions.add(new Transition(null, newEnd));
        
        NFA result = new NFA(newStart, newEnd);
        stack.push(result);
    }

    /**
     * Handles the Kleene star operator (*).
     * Pops an NFA from the stack and creates a new NFA that accepts zero or more
     * repetitions.
     * 
     * @param stack The NFA stack.
     */
    private void handleKleeneStar(Stack<NFA> stack) {
        NFA nfa = stack.pop();
        
        State newStart = new State();
        State newEnd = new State();
        
        newStart.transitions.add(new Transition(null, nfa.startState));
        newStart.transitions.add(new Transition(null, newEnd));
        
        nfa.endState.transitions.add(new Transition(null, nfa.startState));
        nfa.endState.transitions.add(new Transition(null, newEnd));
        
        NFA result = new NFA(newStart, newEnd);
        stack.push(result);
    }

    /**
     * Checks if a character is an operand (not an operator).
     * 
     * @param c The character to check.
     * @return True if the character is an operand, false if it is an operator.
     */
    private boolean isOperand(char c) {
        return ShuntingYard.isOperand(c);
    }
}