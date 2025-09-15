package com.compiler.lexer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;
import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.State;

/**
 * NfaToDfaConverter
 * -----------------
 * This class provides a static method to convert a Non-deterministic Finite Automaton (NFA)
 * into a Deterministic Finite Automaton (DFA) using the standard subset construction algorithm.
 */
/**
 * Utility class for converting NFAs to DFAs using the subset construction
 * algorithm.
 */
public class NfaToDfaConverter {
	/**
	 * Default constructor for NfaToDfaConverter.
	 */
	public NfaToDfaConverter() {
		// TODO: Implement constructor if needed
	}

	/**
	 * Converts an NFA to a DFA using the subset construction algorithm.
	 * Each DFA state represents a set of NFA states. Final states are marked if any
	 * NFA state in the set is final.
	 *
	 * @param nfa      The input NFA
	 * @param alphabet The input alphabet (set of characters)
	 * @return The resulting DFA
	 */
	public static DFA convertNfaToDfa(NFA nfa, Set<Character> alphabet) {
		List<DfaState> dfaStates = new ArrayList<>();
		Queue<DfaState> unprocessed = new LinkedList<>();

		// Create initial DFA state from epsilon-closure of NFA start state
		Set<State> initClosure = epsilonClosure(Set.of(nfa.startState));
		DfaState startState = new DfaState(initClosure);
		dfaStates.add(startState);
		unprocessed.add(startState);

		// Process all unprocessed DFA states
		while (!unprocessed.isEmpty()) {
			DfaState curr = unprocessed.poll();

			// For each symbol in alphabet
			for (char symbol : alphabet) {
				// Compute move and epsilon-closure for current DFA state
				Set<State> moved = move(curr.nfaStates, symbol);
				Set<State> closure = epsilonClosure(moved);

				if (!closure.isEmpty()) {
					// Check if this set of NFA states already exists as a DFA state
					DfaState target = findDfaState(dfaStates, closure);
					if (target == null) {
						// Create new DFA state
						target = new DfaState(closure);
						dfaStates.add(target);
						unprocessed.add(target);
					}

					// Add transition from current to target DFA state
					curr.addTransition(symbol, target);
				}
			}
		}

		// Mark DFA states as final if any NFA state in their set is final
		for (DfaState dfaState : dfaStates) {
			for (State nfaState : dfaState.nfaStates) {
				if (nfaState.isFinal()) {
					dfaState.setFinal(true);
					break;
				}
			}
		}

		return new DFA(startState, dfaStates);
	}

	/**
	 * Computes the epsilon-closure of a set of NFA states.
	 * The epsilon-closure is the set of states reachable by epsilon (null)
	 * transitions.
	 *
	 * @param states The set of NFA states.
	 * @return The epsilon-closure of the input states.
	 */
	private static Set<State> epsilonClosure(Set<State> states) {
		Set<State> closure = new HashSet<>(states);
		Stack<State> stack = new Stack<>();

		// Add all input states to the stack
		for (State state : states) {
			stack.push(state);
		}

		// Process states until stack is empty
		while (!stack.isEmpty()) {
			State curr = stack.pop();

			// Add all states reachable via epsilon transitions
			for (State epsilonState : curr.getEpsilonTransitions()) {
				if (!closure.contains(epsilonState)) {
					closure.add(epsilonState);
					stack.push(epsilonState);
				}
			}
		}

		return closure;
	}

	/**
	 * Returns the set of states reachable from a set of NFA states by a given
	 * symbol.
	 *
	 * @param states The set of NFA states.
	 * @param symbol The input symbol.
	 * @return The set of reachable states.
	 */
	private static Set<State> move(Set<State> states, char symbol) {
		Set<State> result = new HashSet<>();

		// For each state in input set
		for (State state : states) {
			// Add all states reachable by the given symbol
			result.addAll(state.getTransitions(symbol));
		}

		return result;
	}

	/**
	 * Finds an existing DFA state representing a given set of NFA states.
	 *
	 * @param dfaStates       The list of DFA states.
	 * @param targetNfaStates The set of NFA states to search for.
	 * @return The matching DFA state, or null if not found.
	 */
	private static DfaState findDfaState(List<DfaState> dfaStates, Set<State> targetNfaStates) {
		for (DfaState dfaState : dfaStates) {
			if (dfaState.nfaStates.equals(targetNfaStates)) {
				return dfaState;
			}
		}
		return null;
	}
}
