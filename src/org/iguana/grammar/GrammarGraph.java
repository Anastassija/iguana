/*
 * Copyright (c) 2015, Ali Afroozeh and Anastasia Izmaylova, Centrum Wiskunde & Informatica (CWI)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this 
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this 
 *    list of conditions and the following disclaimer in the documentation and/or 
 *    other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
 * OF SUCH DAMAGE.
 *
 */

package org.iguana.grammar;

import static org.iguana.util.CharacterRanges.toNonOverlapping2;
import static org.iguana.util.CharacterRanges.toNonOverlappingSet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.iguana.datadependent.ast.Expression;
import org.iguana.datadependent.ast.Statement;
import org.iguana.grammar.condition.Condition;
import org.iguana.grammar.condition.Conditions;
import org.iguana.grammar.condition.ConditionsFactory;
import org.iguana.grammar.exception.IncorrectNumberOfArgumentsException;
import org.iguana.grammar.operations.FirstFollowSets;
import org.iguana.grammar.slot.BodyGrammarSlot;
import org.iguana.grammar.slot.CodeTransition;
import org.iguana.grammar.slot.ConditionalTransition;
import org.iguana.grammar.slot.EndGrammarSlot;
import org.iguana.grammar.slot.EpsilonGrammarSlot;
import org.iguana.grammar.slot.EpsilonTransition;
import org.iguana.grammar.slot.EpsilonTransition.Type;
import org.iguana.grammar.slot.GrammarSlot;
import org.iguana.grammar.slot.NonterminalGrammarSlot;
import org.iguana.grammar.slot.NonterminalTransition;
import org.iguana.grammar.slot.ReturnTransition;
import org.iguana.grammar.slot.TerminalGrammarSlot;
import org.iguana.grammar.slot.TerminalTransition;
import org.iguana.grammar.slot.lookahead.FollowTest;
import org.iguana.grammar.slot.lookahead.LookAheadTest;
import org.iguana.grammar.slot.lookahead.RangeTreeLookaheadTest;
import org.iguana.grammar.symbol.CharacterRange;
import org.iguana.grammar.symbol.Code;
import org.iguana.grammar.symbol.Conditional;
import org.iguana.grammar.symbol.Epsilon;
import org.iguana.grammar.symbol.Nonterminal;
import org.iguana.grammar.symbol.Position;
import org.iguana.grammar.symbol.Return;
import org.iguana.grammar.symbol.Rule;
import org.iguana.grammar.symbol.Symbol;
import org.iguana.parser.gss.lookup.ArrayNodeLookup;
import org.iguana.parser.gss.lookup.GSSNodeLookup;
import org.iguana.parser.gss.lookup.HashMapNodeLookup;
import org.iguana.regex.RegularExpression;
import org.iguana.regex.matcher.DFAMatcherFactory;
import org.iguana.regex.matcher.JavaRegexMatcherFactory;
import org.iguana.regex.matcher.MatcherFactory;
import org.iguana.util.Configuration;
import org.iguana.util.Configuration.LookupImpl;
import org.iguana.util.Configuration.MatcherType;
import org.iguana.util.Input;
import org.iguana.util.collections.IntRangeTree;
import org.iguana.util.collections.RangeTree;

public class GrammarGraph implements Serializable {

	private static final long serialVersionUID = 1L;

	Map<Nonterminal, NonterminalGrammarSlot> nonterminalsMap;
	
	Map<RegularExpression, TerminalGrammarSlot> terminalsMap;
	
	private final Map<String, GrammarSlot> names;
	
	private List<GrammarSlot> slots;
	
	private final FirstFollowSets firstFollow;
	
	Grammar grammar;

	private Configuration config;

	private Input input;
	
	private int id = 1;
	
	private final MatcherFactory matcherFactory;
	
	private final TerminalGrammarSlot epsilonSlot;
	
	public GrammarGraph(Grammar grammar, Input input, Configuration config) {
		this.grammar = grammar;
		this.input = input;
		this.config = config;
		this.nonterminalsMap = new LinkedHashMap<>();
		this.terminalsMap = new LinkedHashMap<>();
		this.names = new HashMap<>();
		this.slots = new ArrayList<>();
		
		if (config.getMatcherType() == MatcherType.JAVA_REGEX) {
			matcherFactory = new JavaRegexMatcherFactory();
		} else {
			matcherFactory = new DFAMatcherFactory();
		}
		
		this.firstFollow = new FirstFollowSets(grammar);
		
		epsilonSlot = new TerminalGrammarSlot(0, Epsilon.getInstance(), matcherFactory);
		
		terminalsMap.put(Epsilon.getInstance(), epsilonSlot);

		add(epsilonSlot);

		Set<Nonterminal> nonterminals = grammar.getNonterminals();
		nonterminals.forEach(n -> getNonterminalGrammarSlot(n));
		nonterminals.forEach(n -> grammar.getAlternatives(n).forEach(r -> convert(r)));
		nonterminals.forEach(n -> setFirstFollowTests(n));
	}
	
	public NonterminalGrammarSlot getHead(Nonterminal start) {
		return nonterminalsMap.get(start);
	}	
	
	public TerminalGrammarSlot getTerminal(RegularExpression regex) {
		return terminalsMap.get(regex);
	}

	public GrammarSlot getGrammarSlot(String s) {
		return names.get(s);
	}
	
	public Collection<NonterminalGrammarSlot> getNonterminals() {
		return nonterminalsMap.values();
	}
	
	public RegularExpression getRegularExpression(String s) {
		GrammarSlot slot = names.get(s);
		if (!(slot instanceof TerminalGrammarSlot))
			throw new RuntimeException("No regular expression for " + s + " found.");
		return ((TerminalGrammarSlot) names.get(s)).getRegularExpression();
	}
	
	private void convert(Rule rule) {
		Nonterminal nonterminal = rule.getHead();
		NonterminalGrammarSlot nonterminalSlot = getNonterminalGrammarSlot(nonterminal);
		addRule(nonterminalSlot, rule);
	}
	
	private void setFirstFollowTests(Nonterminal nonterminal) {
		NonterminalGrammarSlot nonterminalSlot = getNonterminalGrammarSlot(nonterminal);
		nonterminalSlot.setLookAheadTest(getLookAheadTest(nonterminal, nonterminalSlot));
		nonterminalSlot.setFollowTest(getFollowTest(nonterminal));
	}

	private LookAheadTest getLookAheadTest(Nonterminal nonterminal, NonterminalGrammarSlot nonterminalSlot) {
		
		if (config.getLookAheadCount() == 0)
			return i -> nonterminalSlot.getFirstSlots();
		
		RangeTree<List<BodyGrammarSlot>> rangeTree = new RangeTree<>();
		
		Map<CharacterRange, List<BodyGrammarSlot>> map = new HashMap<>();
		
		List<Rule> alternatives = grammar.getAlternatives(nonterminal);
		
		for (int i = 0; i < alternatives.size(); i++) {
			Rule rule = alternatives.get(i);
			BodyGrammarSlot firstSlot = nonterminalSlot.getFirstSlots().get(i);
			Set<CharacterRange> set = firstFollow.getPredictionSet(rule, 0);
			set.forEach(cr -> map.computeIfAbsent(cr, k -> new ArrayList<>()).add(firstSlot));			
		}
		
		// A map from non-overlapping ranges to a list of original ranges
		Map<CharacterRange, List<CharacterRange>> rangeMap = toNonOverlapping2(map.keySet());
		
		// A map from non-overlapping ranges to a list associated body grammar slots
		Map<CharacterRange, List<BodyGrammarSlot>> nonOverlappingMap = new HashMap<>();
		
		// compute a list of body grammar slots from a non-overlapping range
		Function<CharacterRange, Set<BodyGrammarSlot>> f = r -> rangeMap.get(r).stream().flatMap(range -> map.get(range).stream()).collect(Collectors.toSet());
		
		rangeMap.keySet().forEach(r -> nonOverlappingMap.computeIfAbsent(r, range -> new ArrayList<>()).addAll(f.apply(r))); 
		
		nonOverlappingMap.entrySet().forEach(e -> rangeTree.insert(e.getKey(), e.getValue().isEmpty() ? Collections.emptyList() : e.getValue()));
		
		return new RangeTreeLookaheadTest(nonOverlappingMap);
	}
	
	private FollowTest getFollowTest(Nonterminal nonterminal) {
		
		if (config.getLookAheadCount() == 0)
			return FollowTest.DEFAULT;
		
		// TODO: move toNonOverlapping to first follow itself
		Set<CharacterRange> followSet = toNonOverlappingSet(firstFollow.getFollowSet(nonterminal));
		IntRangeTree rangeTree = new IntRangeTree();
		followSet.forEach(cr -> rangeTree.insert(cr, 1));
		
		return i -> rangeTree.get(i) == 1;
	}
	
	private void addRule(NonterminalGrammarSlot head, Rule rule) {
		
		BodyGrammarSlot firstSlot = getFirstGrammarSlot(rule, head);
		head.addFirstSlot(firstSlot);	
		BodyGrammarSlot currentSlot = firstSlot;
		
		GrammarGraphSymbolVisitor rule2graph = new GrammarGraphSymbolVisitor(head, rule, currentSlot);
		
		while (rule2graph.hasNext()) 
			rule2graph.nextSymbol();
	}
	
	private class GrammarGraphSymbolVisitor extends  AbstractGrammarGraphSymbolVisitor<Void> {
		
		private final NonterminalGrammarSlot head;
		private final Rule rule;
		
		private BodyGrammarSlot currentSlot;
		private int i = 0;
		
		private int j = -1;
		
		public GrammarGraphSymbolVisitor(NonterminalGrammarSlot head, Rule rule, BodyGrammarSlot currentSlot) {
			this.head = head;
			this.rule = rule;
			this.currentSlot = currentSlot;
		}
		
		public boolean hasNext() {
			return i < rule.size();
		}
		
		public void nextSymbol() {
			j = -1;
			visitSymbol(rule.symbolAt(i));
			i++;
		}
		
		public Void visit(Nonterminal symbol) {
			
			NonterminalGrammarSlot nonterminalSlot = getNonterminalGrammarSlot(symbol);
			
			BodyGrammarSlot slot;
			if (i == rule.size() - 1 && j == -1)
				slot = getEndGrammarSlot(rule, i + 1, rule.getPosition(i + 1), head, symbol.getLabel(), symbol.getVariable(), symbol.getState());
			else
				slot = getBodyGrammarSlot(rule, i + 1, rule.getPosition(i + 1), head, symbol.getLabel(), symbol.getVariable(), symbol.getState());
			
			Expression[] arguments = symbol.getArguments();
			
			validateNumberOfArguments(nonterminalSlot.getNonterminal(), arguments);
			
			Set<Condition> preConditions = (i == 0 && j == -1)? new HashSet<>() : symbol.getPreConditions();
			currentSlot.addTransition(new NonterminalTransition(nonterminalSlot, currentSlot, slot, arguments, getConditions(preConditions)));
			currentSlot = slot;
			
			return null;
		}
		
		@Override
		public Void visit(Conditional symbol) {
			
			Symbol sym = symbol.getSymbol();
			Expression expression = symbol.getExpression();
			
			visitSymbol(sym);
			
			BodyGrammarSlot thenSlot = getBodyGrammarSlot(rule, i + 1, rule.getPosition(i + 1), head, null, null, null);
			currentSlot.addTransition(new ConditionalTransition(expression, currentSlot, thenSlot));
			currentSlot = thenSlot;
			
			return null;
		}
		
		@Override
		public Void visit(Code symbol) {
			
			Symbol sym = symbol.getSymbol();
			Statement[] statements = symbol.getStatements();
			
			visitSymbol(sym);
			
			BodyGrammarSlot done = getBodyGrammarSlot(rule, i + 1, rule.getPosition(i + 1), head, null, null, null);
			currentSlot.addTransition(new CodeTransition(statements, currentSlot, done));
			currentSlot = done;
			
			return null;
		}
				
		public Void visit(Return symbol) {
			BodyGrammarSlot done;
			if (i != rule.size() - 1)
				throw new RuntimeException("Return symbol can only be used at the end of a grammar rule!");
			else
				done = getEndGrammarSlot(rule, i + 1, rule.getPosition(i + 1), head, null, null, null);
			
			currentSlot.addTransition(new ReturnTransition(symbol.getExpression(), currentSlot, done));
			currentSlot = done;
			
			return null;
		}
		
		@Override
		public Void visit(RegularExpression symbol) {
			TerminalGrammarSlot terminalSlot = getTerminalGrammarSlot(symbol);
			
			BodyGrammarSlot slot;
			
			if (i == rule.size() - 1 && j == -1)
				slot = getEndGrammarSlot(rule, i + 1, rule.getPosition(i + 1), head, symbol.getLabel(), null, null);
			else
				slot = getBodyGrammarSlot(rule, i + 1, rule.getPosition(i + 1), head, symbol.getLabel(), null, null);
			
			Set<Condition> preConditions = (i == 0 && j == -1)? new HashSet<>() : symbol.getPreConditions();
			currentSlot.addTransition(getTerminalTransition(rule, i + 1, terminalSlot, currentSlot, slot, preConditions, symbol.getPostConditions()));
			currentSlot = slot;
			
			return null;
		}
		
		/**
		 *  Introduces epsilon transitions to handle labels and preconditions/postconditions
		 */
		private void visitSymbol(Symbol symbol) {
			
			if (symbol instanceof Nonterminal || symbol instanceof RegularExpression || symbol instanceof Return) { // TODO: I think this can be unified
				symbol.accept(this);
				return;
			}
			
			Conditions preconditions = i == 0? ConditionsFactory.DEFAULT : getConditions(symbol.getPreConditions());
			
			if (symbol.getLabel() != null) {
				BodyGrammarSlot declared = getBodyGrammarSlot(rule, i + 1, rule.getPosition(i + 1), head, null, null, null);
				currentSlot.addTransition(new EpsilonTransition(Type.DECLARE_LABEL, symbol.getLabel(), preconditions, currentSlot, declared));
				currentSlot = declared;
			} else {
				BodyGrammarSlot checked = getBodyGrammarSlot(rule, i + 1, rule.getPosition(i + 1), head, null, null, null);
				currentSlot.addTransition(new EpsilonTransition(preconditions, currentSlot, checked));
				currentSlot = checked;
			}
			
			j += 1;
			
			symbol.accept(this);
			
			j -= 1;
			
			if (symbol.getLabel() != null) {
				
				BodyGrammarSlot stored;
				if (i == rule.size() - 1 && j == -1)
					stored = getEndGrammarSlot(rule, i + 1, rule.getPosition(i + 1), head, null, null, null);
				else
					stored = getBodyGrammarSlot(rule, i + 1, rule.getPosition(i + 1), head, null, null, null);
				
				currentSlot.addTransition(new EpsilonTransition(Type.STORE_LABEL, symbol.getLabel(), getConditions(symbol.getPostConditions()), currentSlot, stored));
				currentSlot = stored;
			} else {
				
				BodyGrammarSlot checked;
				if (i == rule.size() - 1 && j == -1)
					checked = getEndGrammarSlot(rule, i + 1, rule.getPosition(i + 1), head, null, null, null);
				else
					checked = getBodyGrammarSlot(rule, i + 1, rule.getPosition(i + 1), head, null, null, null);
				
				currentSlot.addTransition(new EpsilonTransition(getConditions(symbol.getPostConditions()), currentSlot, checked));
				currentSlot = checked;
			}
		}
		
	}
	
	private TerminalTransition getTerminalTransition(Rule rule, int i, TerminalGrammarSlot slot, 
															 BodyGrammarSlot origin, BodyGrammarSlot dest,
															 Set<Condition> preConditions, Set<Condition> postConditions) {
		
		return new TerminalTransition(slot, origin, dest, getConditions(preConditions), getConditions(postConditions));
	}
	
	private TerminalGrammarSlot getTerminalGrammarSlot(RegularExpression regex) {
		TerminalGrammarSlot terminalSlot = new TerminalGrammarSlot(id++, regex, matcherFactory);
		add(terminalSlot);
		return terminalsMap.computeIfAbsent(regex, k -> terminalSlot);
	}
	
	private NonterminalGrammarSlot getNonterminalGrammarSlot(Nonterminal nonterminal) {
		return nonterminalsMap.computeIfAbsent(nonterminal, k -> {
			NonterminalGrammarSlot ntSlot;
			ntSlot = new NonterminalGrammarSlot(id++, nonterminal, getNodeLookup());			
			add(ntSlot);
			return ntSlot;
		});
	}
	
	private BodyGrammarSlot getFirstGrammarSlot(Rule rule,  NonterminalGrammarSlot nonterminal) {
		BodyGrammarSlot slot;
		
		if (rule.size() == 0) {
			slot = new EpsilonGrammarSlot(id++, rule.getPosition(0,0), nonterminal, epsilonSlot, ConditionsFactory.DEFAULT, rule.getAction());
		} else {
			// TODO: This is not a final solution; in particular, 
			//       not any precondition of the first symbol (due to labels) can currently be moved to the first slot.  
			Set<Condition> preConditions = new HashSet<>();
			preConditions.addAll(rule.symbolAt(0).getPreConditions());
			 
			slot = new BodyGrammarSlot(id++, rule.getPosition(0,0), rule.symbolAt(0).getLabel(), null, null, getConditions(preConditions));
		}
		add(slot);
		return slot;
	}
	
	private BodyGrammarSlot getBodyGrammarSlot(Rule rule, int i, Position position, NonterminalGrammarSlot nonterminal, String label, String variable, Set<String> state) {
		assert i < rule.size();
		BodyGrammarSlot slot = new BodyGrammarSlot(id++, position, label, variable, state, getConditions(rule.symbolAt(i - 1).getPostConditions()));
		add(slot);
		return slot;
	}
	
	private BodyGrammarSlot getEndGrammarSlot(Rule rule, int i, Position position, NonterminalGrammarSlot nonterminal, String label, String variable, Set<String> state) {
		assert i == rule.size();
		BodyGrammarSlot slot = new EndGrammarSlot(id++, position, nonterminal, label, variable, state, getConditions(rule.symbolAt(i - 1).getPostConditions()), rule.getAction());				
		add(slot);
		return slot;
	}

	private void add(GrammarSlot slot) {
		names.put(slot.toString(), slot);
		slots.add(slot);
	}
	
	private GSSNodeLookup getNodeLookup() {
		if (config.getGSSLookupImpl() == LookupImpl.HASH_MAP) {
			return new HashMapNodeLookup();
		} else {
			return new ArrayNodeLookup(input);
		}
	}
	
	static private void validateNumberOfArguments(Nonterminal nonterminal, Expression[] arguments) {
		String[] parameters = nonterminal.getParameters();
		if ((parameters == null && arguments == null) 
				|| (parameters.length == arguments.length)) return;
		
		throw new IncorrectNumberOfArgumentsException(nonterminal, arguments);
	}

	public void reset(Input input) {
		slots.forEach(s -> s.reset(input));
	}

	private Conditions getConditions(Set<Condition> conditions) {
		if (conditions.isEmpty())
			return ConditionsFactory.DEFAULT;
		return ConditionsFactory.getConditions(conditions, matcherFactory);
	}
	
}
