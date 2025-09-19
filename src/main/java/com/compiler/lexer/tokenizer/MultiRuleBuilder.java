package com.compiler.lexer.tokenizer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.State;
import com.compiler.lexer.nfa.Transition;
import com.compiler.lexer.regex.RegexParser;

public class MultiRuleBuilder {

    public class ComplexNFA {

        public final NFA nfa;

        public final Map<State, TokenType> finalStateToType;

        public final Map<State, Integer> finalStateToPriority;

        public ComplexNFA(NFA nfa, Map<State, TokenType> finalStateToType, Map<State, Integer> finalStateToPriority) {
            this.nfa = nfa;
            this.finalStateToType = finalStateToType;
            this.finalStateToPriority = finalStateToPriority;
        }

        public TokenType getTokenType(State state) {
            return finalStateToType.get(state);
        }

        public Integer getPriority(State state) {
            return finalStateToPriority.get(state);
        }

        public boolean isFinalState(State state) {
            return finalStateToType.containsKey(state);
        }

    }


    private final Map<State, TokenType> nfaFinalStateToType;

    private final Map<State, Integer> nfaFinalStateToPriority;

    private final RegexParser regexParser = new RegexParser();

    public MultiRuleBuilder() {
        this.nfaFinalStateToType = new HashMap<>();
        this.nfaFinalStateToPriority = new HashMap<>();
    }

    public ComplexNFA buildNfa(List<LexRule> rules) {
        if (rules == null || rules.isEmpty()) {
            throw new IllegalArgumentException("Rules list cannot be null or empty");
        }

        // Clear previous mappings
        nfaFinalStateToPriority.clear();
        nfaFinalStateToType.clear();

        // New begin state for combinded NFA
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
