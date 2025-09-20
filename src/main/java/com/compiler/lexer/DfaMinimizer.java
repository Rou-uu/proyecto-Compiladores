
/**
 * DfaMinimizer
 * -------------
 * This class provides an implementation of DFA minimization using the table-filling algorithm.
 * It identifies and merges equivalent states in a deterministic finite automaton (DFA),
 * resulting in a minimized DFA with the smallest number of states that recognizes the same language.
 *
 * Main steps:
 *   1. Initialization: Mark pairs of states as distinguishable if one is final and the other is not.
 *   2. Iterative marking: Mark pairs as distinguishable if their transitions lead to distinguishable states,
 *      or if only one state has a transition for a given symbol.
 *   3. Partitioning: Group equivalent states and build the minimized DFA.
 *
 * Helper methods are provided for partitioning, union-find operations, and pair representation.
 */
package com.compiler.lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;
import com.compiler.lexer.nfa.State;
import com.compiler.lexer.tokenizer.TokenType;

/**
 * Implements DFA minimization using the table-filling algorithm.
 */
/**
 * Utility class for minimizing DFAs using the table-filling algorithm.
 */
public class DfaMinimizer {
    /**
     * Default constructor for DfaMinimizer.
     */
    public DfaMinimizer() {
        // TODO: Implement constructor if needed
    }

    /**
     * Minimizes a given DFA using the table-filling algorithm.
     *
     * @param originalDfa The original DFA to be minimized.
     * @param alphabet    The set of input symbols.
     * @return A minimized DFA equivalent to the original.
     */
    public static DFA minimizeDfa(DFA originalDfa, Set<Character> alphabet) {
        // TODO: Implement minimizeDfa
        /*
         * Pseudocode:
         * 1. Collect and sort all DFA states
         * 2. Initialize table of state pairs; mark pairs as distinguishable if one is
         * final and the other is not
         * 3. Iteratively mark pairs as distinguishable if their transitions lead to
         * distinguishable states or only one has a transition
         * 4. Partition states into equivalence classes (using union-find)
         * 5. Create new minimized states for each partition
         * 6. Reconstruct transitions for minimized states
         * 7. Set start state and return minimized DFA
         */
        List<DfaState> allStates = new ArrayList<>(originalDfa.allStates);

        // Initialize table of state pairs
        Map<Pair, Boolean> table = new HashMap<>();

        // Mark pairs as distinguishable if one is final and the other is not
        for (int i = 0; i < allStates.size(); i++) {
            for (int j = i + 1; j < allStates.size(); j++) {
                DfaState s1 = allStates.get(i);
                DfaState s2 = allStates.get(j);
                Pair pair = new Pair(s1, s2);

                if (s1.isFinal() != s2.isFinal()) {
                    table.put(pair, true);
                } else {
                    table.put(pair, false);
                }
            }
        }

        // Iteratively mark pairs as distinguishable
        boolean changed = true;
        while (changed) {
            changed = false;

            for (int i = 0; i < allStates.size(); i++) {
                for (int j = i + 1; j < allStates.size(); j++) {
                    DfaState s1 = allStates.get(i);
                    DfaState s2 = allStates.get(j);
                    Pair pair = new Pair(s1, s2);

                    if (!table.get(pair)) { // If not yet marked as distinguishable
                        for (char symbol : alphabet) {
                            DfaState t1 = s1.getTransition(symbol);
                            DfaState t2 = s2.getTransition(symbol);

                            // If only one state has a transition for this symbol
                            if ((t1 == null) != (t2 == null)) {
                                table.put(pair, true);
                                changed = true;
                                break;
                            }

                            // If both have transitions, check if they lead to distinguishable states
                            if (t1 != null && t2 != null && !t1.equals(t2)) {
                                Pair tarPair = new Pair(t1, t2);
                                if (table.get(tarPair)) {
                                    table.put(pair, true);
                                    changed = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        // Partition states into equivalence classes
        List<Set<DfaState>> partitions = createPartitions(allStates, table);

        // Create new minimized states for each partition
        List<DfaState> minStates = new ArrayList<>();
        Map<DfaState, DfaState> stMapping = new HashMap<>();

        for (Set<DfaState> partition : partitions) {
            Set<State> combinedNfaStates = new HashSet<>();
            boolean isFinal = false;

            for (DfaState state : partition) {
                combinedNfaStates.addAll(state.nfaStates);
                if (state.isFinal()) {
                    isFinal = true;
                }
            }

            DfaState newState = new DfaState(combinedNfaStates);
            newState.setFinal(isFinal);
            minStates.add(newState);

            // Map all states in this partition to the new state
            for (DfaState state : partition) {
                stMapping.put(state, newState);
            }
        }

        // Reconstruct transitions for minimized states
        for (DfaState newState : minStates) {
            // Find a representative from the original states
            DfaState rep = null;
            for (Map.Entry<DfaState, DfaState> entry : stMapping.entrySet()) {
                if (entry.getValue().equals(newState)) {
                    rep = entry.getKey();
                    break;
                }
            }

            if (rep != null) {
                for (char symbol : alphabet) {
                    DfaState target = rep.getTransition(symbol);
                    if (target != null) {
                        DfaState newTarget = stMapping.get(target);
                        if (newTarget != null) {
                            newState.addTransition(symbol, newTarget);
                        }
                    }
                }
            }
        }

        // Set start state
        DfaState newStartState = stMapping.get(originalDfa.startState);

        return new DFA(newStartState, minStates);
    }

    /**
     * Minimizes a given DFA with token types using the table-filling algorithm.
     * This version preserves token types during minimization.
     *
     * @param originalDfa The original DFA to be minimized.
     * @param alphabet    The set of input symbols.
     * @return A minimized DFA equivalent to the original with preserved token types.
     */
    public static DFA minimizeComplexDfa(DFA originalDfa, Set<Character> alphabet) {
        List<DfaState> allStates = new ArrayList<>(originalDfa.allStates);

        // Initialize table of state pairs
        Map<Pair, Boolean> table = new HashMap<>();

        // Mark pairs as distinguishable if one is final and the other is not
        // or if they have different token types
        for (int i = 0; i < allStates.size(); i++) {
            for (int j = i + 1; j < allStates.size(); j++) {
                DfaState s1 = allStates.get(i);
                DfaState s2 = allStates.get(j);
                Pair pair = new Pair(s1, s2);

                if (s1.isFinal() != s2.isFinal()) {
                    table.put(pair, true);
                } else if (s1.isFinal() && s2.isFinal() &&
                          !Objects.equals(s1.getTokenType(), s2.getTokenType())) {
                    // Different token types make states distinguishable
                    table.put(pair, true);
                } else {
                    table.put(pair, false);
                }
            }
        }

        // Iteratively mark pairs as distinguishable
        boolean changed = true;
        while (changed) {
            changed = false;

            for (int i = 0; i < allStates.size(); i++) {
                for (int j = i + 1; j < allStates.size(); j++) {
                    DfaState s1 = allStates.get(i);
                    DfaState s2 = allStates.get(j);
                    Pair pair = new Pair(s1, s2);

                    if (!table.get(pair)) { // If not yet marked as distinguishable
                        for (char symbol : alphabet) {
                            DfaState t1 = s1.getTransition(symbol);
                            DfaState t2 = s2.getTransition(symbol);

                            // If only one state has a transition for this symbol
                            if ((t1 == null) != (t2 == null)) {
                                table.put(pair, true);
                                changed = true;
                                break;
                            }

                            // If both have transitions, check if they lead to distinguishable states
                            if (t1 != null && t2 != null && !t1.equals(t2)) {
                                Pair tarPair = new Pair(t1, t2);
                                if (table.get(tarPair)) {
                                    table.put(pair, true);
                                    changed = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        // Partition states into equivalence classes
        List<Set<DfaState>> partitions = createPartitions(allStates, table);

        // Create new minimized states for each partition
        List<DfaState> minStates = new ArrayList<>();
        Map<DfaState, DfaState> stMapping = new HashMap<>();

        for (Set<DfaState> partition : partitions) {
            Set<State> combinedNfaStates = new HashSet<>();
            boolean isFinal = false;
            TokenType tokenType = null;

            for (DfaState state : partition) {
                combinedNfaStates.addAll(state.nfaStates);
                if (state.isFinal()) {
                    isFinal = true;
                    if (tokenType == null) {
                        tokenType = state.getTokenType();
                    }
                }
            }

            DfaState newState = new DfaState(combinedNfaStates);
            if (isFinal) {
                newState.setFinal(true, tokenType);
            }
            minStates.add(newState);

            // Map all states in this partition to the new state
            for (DfaState state : partition) {
                stMapping.put(state, newState);
            }
        }

        // Reconstruct transitions for minimized states
        for (DfaState newState : minStates) {
            // Find a representative from the original states
            DfaState rep = null;
            for (Map.Entry<DfaState, DfaState> entry : stMapping.entrySet()) {
                if (entry.getValue().equals(newState)) {
                    rep = entry.getKey();
                    break;
                }
            }

            if (rep != null) {
                for (char symbol : alphabet) {
                    DfaState target = rep.getTransition(symbol);
                    if (target != null) {
                        DfaState newTarget = stMapping.get(target);
                        if (newTarget != null) {
                            newState.addTransition(symbol, newTarget);
                        }
                    }
                }
            }
        }

        // Set start state
        DfaState newStartState = stMapping.get(originalDfa.startState);

        return new DFA(newStartState, minStates);
    }

    /**
     * Groups equivalent states into partitions using union-find.
     *
     * @param allStates List of all DFA states.
     * @param table     Table indicating which pairs are distinguishable.
     * @return List of partitions, each containing equivalent states.
     */
    private static List<Set<DfaState>> createPartitions(List<DfaState> allStates, Map<Pair, Boolean> table) {
        // TODO: Implement createPartitions
        /*
        Pseudocode:
        1. Initialize each state as its own parent
        2. For each pair not marked as distinguishable, union the states
        3. Group states by their root parent
        4. Return list of partitions
        */
        Map<DfaState, DfaState> parent = new HashMap<>();

        // Initialize each state as its own parent
        for (DfaState state : allStates) {
            parent.put(state, state);
        }

        // For each pair not marked as distinguishable, union the states
        for (int i = 0; i < allStates.size(); i++) {
            for (int j = i + 1; j < allStates.size(); j++) {
                DfaState s1 = allStates.get(i);
                DfaState s2 = allStates.get(j);
                Pair pair = new Pair(s1, s2);

                if (!table.get(pair)) { // If not distinguishable
                    union(parent, s1, s2);
                }
            }
        }

        // Group states by their root parent
        Map<DfaState, Set<DfaState>> groups = new HashMap<>();
        for (DfaState state : allStates) {
            DfaState root = find(parent, state);

            if (!groups.containsKey(root)) {
                groups.put(root, new HashSet<>());
            }

            groups.get(root).add(state);
        }

        return new ArrayList<>(groups.values());
    }

    /**
     * Finds the root parent of a state in the union-find structure.
     * Implements path compression for efficiency.
     *
     * @param parent Parent map.
     * @param state  State to find.
     * @return Root parent of the state.
     */
    private static DfaState find(Map<DfaState, DfaState> parent, DfaState state) {
        if (parent.get(state).equals(state)) {
            return state;
        }
        // Path compression
        DfaState root = find(parent, parent.get(state));
        parent.put(state, root);
        return root;
    }

    /**
     * Unites two states in the union-find structure.
     *
     * @param parent Parent map.
     * @param s1     First state.
     * @param s2     Second state.
     */
    private static void union(Map<DfaState, DfaState> parent, DfaState s1, DfaState s2) {
        DfaState root1 = find(parent, s1);
        DfaState root2 = find(parent, s2);

        if (!root1.equals(root2)) {
            parent.put(root2, root1);
        }
    }

    /**
     * Helper class to represent a pair of DFA states in canonical order.
     * Used for table indexing and comparison.
     */
    private static class Pair {
        final DfaState s1;
        final DfaState s2;

        /**
         * Constructs a pair in canonical order (lowest id first).
         * 
         * @param s1 First state.
         * @param s2 Second state.
         */
        public Pair(DfaState s1, DfaState s2) {
            // Ensure canonical order (smaller id first)
            if (s1.id <= s2.id) {
                this.s1 = s1;
                this.s2 = s2;
            } else {
                this.s1 = s2;
                this.s2 = s1;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Pair pair = (Pair) o;
            return Objects.equals(s1, pair.s1) && Objects.equals(s2, pair.s2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(s1.id, s2.id);
        }
    }
}
