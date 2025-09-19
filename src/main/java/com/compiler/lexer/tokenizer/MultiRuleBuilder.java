package com.compiler.lexer.tokenizer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.State;
import com.compiler.lexer.nfa.Transition;
import com.compiler.lexer.regex.RegexParser;

/**
 * Builds a combined NFA from multiple lexical rules for tokenization.
 * This class takes a list of lexical rules and creates a single NFA that
 * can recognize all the patterns, maintaining information about which
 * token type and priority each final state represents.
 *
 * @author Rubén Alfaro
 * @version 1.0
 */
public class MultiRuleBuilder {

    /**
     * Represents a complex NFA that combines multiple lexical rules.
     * This class wraps a standard NFA with additional mappings that track
     * which token types and priorities are associated with final states.
     */
    public class ComplexNFA {

        /**
         * The underlying NFA that recognizes all the combined patterns.
         */
        public final NFA nfa;

        /**
         * Maps final states to their corresponding token types.
         */
        public final Map<State, TokenType> finalStateToType;

        /**
         * Maps final states to their rule priorities.
         */
        public final Map<State, Integer> finalStateToPriority;

        /**
         * Constructs a ComplexNFA with the given NFA and state mappings.
         *
         * @param nfa                    the underlying NFA
         * @param finalStateToType       mapping from final states to token types
         * @param finalStateToPriority   mapping from final states to priorities
         */
        public ComplexNFA(NFA nfa, Map<State, TokenType> finalStateToType, Map<State, Integer> finalStateToPriority) {
            this.nfa = nfa;
            this.finalStateToType = finalStateToType;
            this.finalStateToPriority = finalStateToPriority;
        }

        /**
         * Returns the token type associated with a final state.
         *
         * @param state the state to query
         * @return the token type for the state, or null if not a final state
         */
        public TokenType getTokenType(State state) {
            return finalStateToType.get(state);
        }

        /**
         * Returns the priority associated with a final state.
         *
         * @param state the state to query
         * @return the priority for the state, or null if not a final state
         */
        public Integer getPriority(State state) {
            return finalStateToPriority.get(state);
        }

        /**
         * Checks if a state is a final state in this complex NFA.
         *
         * @param state the state to check
         * @return true if the state is final, false otherwise
         */
        public boolean isFinalState(State state) {
            return finalStateToType.containsKey(state);
        }

    }

    /**
     * Maps NFA final states to their corresponding token types.
     */
    private final Map<State, TokenType> nfaFinalStateToType;

    /**
     * Maps NFA final states to their rule priorities.
     */
    private final Map<State, Integer> nfaFinalStateToPriority;

    /**
     * Parser used to convert regex patterns into NFAs.
     */
    private final RegexParser regexParser = new RegexParser();

    /**
     * Constructs a new MultiRuleBuilder with empty state mappings.
     */
    public MultiRuleBuilder() {
        this.nfaFinalStateToType = new HashMap<>();
        this.nfaFinalStateToPriority = new HashMap<>();
    }

    /**
     * Builds a combined NFA from a list of lexical rules.
     * Each rule is converted to an individual NFA, and all NFAs are combined
     * into a single NFA with a common start state that has epsilon transitions
     * to each individual rule's start state.
     *
     * @param rules the list of lexical rules to combine
     * @return a ComplexNFA containing the combined automaton with state mappings
     * @throws IllegalArgumentException if rules is null or empty
     */
    public ComplexNFA buildNfa(List<LexRule> rules) {
        if (rules == null || rules.isEmpty()) {
            throw new IllegalArgumentException("Rules list cannot be null or empty");
        }

        // Clear previous mappings
        nfaFinalStateToPriority.clear();
        nfaFinalStateToType.clear();

        // New begin state for combined NFA
        State beginState = new State();

        for (LexRule rule : rules) {
            // Aux NFA for each rule
            NFA nfa = regexParser.parse(rule.getRegex());

            // Mark the final state and map it to the token type and priority
            nfa.endState.isFinal = true;
            nfaFinalStateToType.put(nfa.endState, rule.getTokenType());
            nfaFinalStateToPriority.put(nfa.endState, rule.getPriority());

            // Add transition from new beginning to each rule NFA
            beginState.transitions.add(new Transition(null, nfa.startState));
        }

        // Create the combined NFA. Since there's no single end state, we pass null.
        NFA combinedNFA = new NFA(beginState, null);

        // Wrap in ComplexNFA to include mappings and return
        return new ComplexNFA(combinedNFA, nfaFinalStateToType, nfaFinalStateToPriority);
    }
}
