package com.compiler.parser.syntax;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.compiler.parser.grammar.Grammar;
import com.compiler.parser.grammar.Production;
import com.compiler.parser.grammar.Symbol;
import com.compiler.parser.grammar.SymbolType;

/**
 * Calculates the FIRST and FOLLOW sets for a given grammar.
 * Main task of Practice 5.
 */
public class StaticAnalyzer {
    private final Grammar grammar;
    private final Map<Symbol, Set<Symbol>> firstSets;
    private final Map<Symbol, Set<Symbol>> followSets;

    public StaticAnalyzer(Grammar grammar) {
        this.grammar = grammar;
        this.firstSets = new HashMap<>();
        this.followSets = new HashMap<>();
    }

    /**
     * Calculates and returns the FIRST sets for all symbols.
     * @return A map from Symbol to its FIRST set.
     */
    public Map<Symbol, Set<Symbol>> getFirstSets() {
        // TODO: Implement the algorithm to calculate FIRST sets.
        /*
         * Pseudocode for FIRST set calculation:
         *
         * 1. For each symbol S in grammar:
         *      - If S is a terminal, FIRST(S) = {S}
         *      - If S is a non-terminal, FIRST(S) = {}
         *
         * 2. Repeat until no changes:
         *      For each production A -> X1 X2 ... Xn:
         *          - For each symbol Xi in the right-hand side:
         *              a. Add FIRST(Xi) - {ε} to FIRST(A)
         *              b. If ε is in FIRST(Xi), continue to next Xi
         *                 Otherwise, break
         *          - If ε is in FIRST(Xi) for all i, add ε to FIRST(A)
         *
         * 3. Return the map of FIRST sets for all symbols.
         */

        // If already calculated, return the existing sets
        if (!firstSets.isEmpty()) {
            return firstSets;
        }

        // Initialize FIRST sets for all symbols
        for (Symbol symbol : grammar.getTerminals()) {
            firstSets.put(symbol, new HashSet<>());
            firstSets.get(symbol).add(symbol);
        }

        for (Symbol symbol : grammar.getNonTerminals()) {
            firstSets.put(symbol, new HashSet<>());
        }


        // Repeat until no changes
        boolean changed = true;
        while (changed) {
            changed = false;

            // For each production A -> X1 X2 ... Xn
            for (Production prod : grammar.getProductions()) {
                Symbol left = prod.getLeft();
                Set<Symbol> first = firstSets.get(left);
                int initialSize = first.size();

                if (prod.getRight().isEmpty()) {
                    Symbol epsilon = new Symbol("ε", SymbolType.TERMINAL);
                    first.add(epsilon);
                } else {
                    boolean allHaveEpsilon = true;

                    // For each symbol Xi in the right-hand side
                    for (Symbol symbol : prod.getRight()) {
                        Set<Symbol> symbolFirst = firstSets.get(symbol);

                        // Handle case where symbol is not in firstSets (this shouldn't happen but it was causing problems)
                        if (symbolFirst == null) {
                            if (symbol.name.equals("ε")) {
                                Symbol epsilon = new Symbol("ε", SymbolType.TERMINAL);
                                first.add(epsilon);
                                continue;
                            } else {
                                allHaveEpsilon = false;
                                break;
                            }
                        }

                        // Add FIRST(symbol) - {ε} to FIRST(leftSymbol)
                        for (Symbol s : symbolFirst) {
                            if (!s.name.equals("ε")) {
                                first.add(s);
                            }
                        }

                        // Check if epsilon is in FIRST(symbol)
                        boolean hasEpsilon = containsEpsilon(first);
                        if (!hasEpsilon) {
                            allHaveEpsilon = false;
                            break;
                        }
                    }

                    // If all symbols have epsilon, add epsilon to FIRST(leftSymbol)
                    if (allHaveEpsilon) {
                        Symbol epsilon = new Symbol("ε", SymbolType.TERMINAL);
                        first.add(epsilon);
                    }
                }

                if (first.size() > initialSize) {
                    changed = true;
                }
            }
        }

        return firstSets;
    }

    /**
     * Calculates and returns the FOLLOW sets for non-terminals.
     * @return A map from Symbol to its FOLLOW set.
     */
    public Map<Symbol, Set<Symbol>> getFollowSets() {
        // TODO: Implement the algorithm to calculate FOLLOW sets.
        /*
         * Pseudocode for FOLLOW set calculation:
         *
         * 1. For each non-terminal A, FOLLOW(A) = {}
         * 2. Add $ (end of input) to FOLLOW(S), where S is the start symbol
         *
         * 3. Repeat until no changes:
         *      For each production B -> X1 X2 ... Xn:
         *          For each Xi (where Xi is a non-terminal):
         *              a. For each symbol Xj after Xi (i < j <= n):
         *                  - Add FIRST(Xj) - {ε} to FOLLOW(Xi)
         *                  - If ε is in FIRST(Xj), continue to next Xj
         *                    Otherwise, break
         *              b. If ε is in FIRST(Xj) for all j > i, add FOLLOW(B) to FOLLOW(Xi)
         *
         * 4. Return the map of FOLLOW sets for all non-terminals.
         *
         * Note: This method should call getFirstSets() first to obtain FIRST sets.
         */
        if (!followSets.isEmpty()) {
            return followSets;
        }

        // Ensure FIRST sets are calculated
        getFirstSets();

        // Initialize FOLLOW sets for all non-terminals
        for (Symbol symbol : grammar.getNonTerminals()) {
            followSets.put(symbol, new HashSet<>());
        }

        // Add $ to FOLLOW(start symbol)
        Symbol eoi = new Symbol("$", SymbolType.TERMINAL);
        followSets.get(grammar.getStartSymbol()).add(eoi);

        // Repeat until no changes
        boolean changed = true;
        while (changed) {
            changed = false;

            // For each production B -> X1 X2 ... Xn
            for (Production prod : grammar.getProductions()) {
                Symbol left = prod.getLeft();

                // For each symbol Xi in the right-hand side
                for (int i = 0; i < prod.getRight().size(); i++) {
                    Symbol currS = prod.getRight().get(i);

                    // Only process non-terminals
                    if (currS.type == SymbolType.NON_TERMINAL) {
                        Set<Symbol> followSet = followSets.get(currS);
                        int initialSize = followSet.size();

                        // Look at symbols after current symbol
                        boolean allFollowingHaveEpsilon = true;

                        // For each symbol Xj after Xi
                        for (int j = i + 1; j < prod.getRight().size(); j++) {
                            Symbol next = prod.getRight().get(j);
                            Set<Symbol> nextFirst = firstSets.get(next);

                            // Add FIRST(following) - {ε} to FOLLOW(current)
                            for (Symbol s : nextFirst) {
                                if (!s.name.equals("ε")) {
                                    followSet.add(s);
                                }
                            }

                            // Check if epsilon is in FIRST(following)
                            boolean hasEpsilon = containsEpsilon(followSet);
                            if (!hasEpsilon) {
                                allFollowingHaveEpsilon = false;
                                break;
                            }
                        }

                        // If all following symbols have epsilon or no following symbols,
                        // add FOLLOW(leftSymbol) to FOLLOW(currentSymbol)
                        if (allFollowingHaveEpsilon) {
                            followSet.addAll(followSets.get(left));
                        }

                        if (followSet.size() > initialSize) {
                            changed = true;
                        }
                    }
                }
            }
        }

        return followSets;
    }

    /**
     * Checks if a set of symbols contains the epsilon (ε) symbol.
     *
     * @param set the set of symbols to check for epsilon
     * @return true if the set contains epsilon, false otherwise or if set is null
     */
    private static boolean containsEpsilon(Set<Symbol> set) {
        if (set == null) return false;
        for (Symbol s : set) {
            if ("ε".equals(s.name)) {
                return true;
            }
        }
        return false;
    }
}