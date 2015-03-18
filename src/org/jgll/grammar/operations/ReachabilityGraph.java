package org.jgll.grammar.operations;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgll.grammar.Grammar;
import org.jgll.grammar.symbol.Align;
import org.jgll.grammar.symbol.Block;
import org.jgll.grammar.symbol.Character;
import org.jgll.grammar.symbol.CharacterRange;
import org.jgll.grammar.symbol.Code;
import org.jgll.grammar.symbol.Conditional;
import org.jgll.grammar.symbol.EOF;
import org.jgll.grammar.symbol.Epsilon;
import org.jgll.grammar.symbol.IfThen;
import org.jgll.grammar.symbol.IfThenElse;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.grammar.symbol.Offside;
import org.jgll.grammar.symbol.Rule;
import org.jgll.grammar.symbol.Symbol;
import org.jgll.grammar.symbol.Terminal;
import org.jgll.grammar.symbol.While;
import org.jgll.regex.Alt;
import org.jgll.regex.Opt;
import org.jgll.regex.Plus;
import org.jgll.regex.Sequence;
import org.jgll.regex.Star;
import org.jgll.traversal.ISymbolVisitor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;

public class ReachabilityGraph {

	private final ListMultimap<Nonterminal, Rule> definitions;
	
	private final SetMultimap<Nonterminal, Nonterminal> reachabilityGraph;
	
	private final Set<String> layouts;
	
	public ReachabilityGraph(Grammar grammar) {
		this.reachabilityGraph = HashMultimap.create();
		this.definitions = grammar.getDefinitions();
		
		this.layouts = new HashSet<>();
		
		if (grammar.getLayout() != null)
			layouts.add(grammar.getLayout().getName());
		
		for (Rule rule : grammar.getDefinitions().values())
			if (rule.getLayout() != null)
				layouts.add(rule.getLayout().getName());
		
		calculateReachabilityGraph();
	}
	
	public SetMultimap<Nonterminal, Nonterminal> getReachabilityGraph() {
		return reachabilityGraph;
	}
	
	public Set<Nonterminal> getReachableNonterminals(Nonterminal nt) {
		return reachabilityGraph.get(nt);
	}
	
	/**
	 * 
	 * Calculate the set of nonterminals that are reachable via the alternates of A.
	 * In other words, if A is a nonterminal, reachable nonterminals are all the B's such as
	 * A =>* alpha B gamma. Note that this method does not calculate direct-nullable reachable
	 * nonterminals.
	 * 
	 */
	public Multimap<Nonterminal, Nonterminal> calculateReachabilityGraph() {
		
		Visitor visitor = new Visitor(reachabilityGraph, layouts);
		
		Set<Nonterminal> nonterminals = definitions.keySet();
		
		boolean changed = true;
		
		while (changed) {
			
			changed = false;
			
			for (Nonterminal head : nonterminals) {
				
				// Skips layout
				if (layouts.contains(head.getName()))
					continue;
				
				visitor.setHead(head);
				
				for (Rule rule : definitions.get(head)) {
					
					List<Symbol> alternate = rule.getBody();
					
					if (alternate == null)
						continue;
					
					for (Symbol symbol : alternate)
						changed |= symbol.accept(visitor);
					
				}
			}
		}
		
		return reachabilityGraph;
	}
	
	private static class Visitor implements ISymbolVisitor<Boolean> {
		
		private final SetMultimap<Nonterminal, Nonterminal> reachabilityGraph;
		private final Set<String> layouts;
		
		private Nonterminal head;
		
		public Visitor(SetMultimap<Nonterminal, Nonterminal> reachabilityGraph, Set<String> layouts) {
			this.reachabilityGraph = reachabilityGraph;
			this.layouts = layouts;
		}
		
		public void setHead(Nonterminal head) {
			this.head = head;
		}

		@Override
		public Boolean visit(Align symbol) {
			return symbol.getSymbol().accept(this);
		}

		@Override
		public Boolean visit(Block symbol) {
			boolean changed = false;
			for (Symbol s : symbol.getSymbols())
				changed |= s.accept(this);
			return changed;
		}

		@Override
		public Boolean visit(Character symbol) {
			return false;
		}

		@Override
		public Boolean visit(CharacterRange symbol) {
			return false;
		}

		@Override
		public Boolean visit(Code symbol) {
			return symbol.getSymbol().accept(this);
		}

		@Override
		public Boolean visit(Conditional symbol) {
			return symbol.getSymbol().accept(this);
		}

		@Override
		public Boolean visit(EOF symbol) {
			return false;
		}

		@Override
		public Boolean visit(Epsilon symbol) {
			return false;
		}

		@Override
		public Boolean visit(IfThen symbol) {
			return symbol.getThenPart().accept(this);
		}

		@Override
		public Boolean visit(IfThenElse symbol) {
			return symbol.getThenPart().accept(this) || symbol.getElsePart().accept(this);
		}

		@Override
		public Boolean visit(Nonterminal symbol) {
			// Skips layout
			if (layouts.contains(symbol.getName()))
				return false;
			return add(head, symbol, reachabilityGraph);
		}

		@Override
		public Boolean visit(Offside symbol) {
			return symbol.getSymbol().accept(this);
		}

		@Override
		public Boolean visit(Terminal symbol) {
			return false;
		}

		@Override
		public Boolean visit(While symbol) {
			return symbol.getBody().accept(this);
		}

		@Override
		public <E extends Symbol> Boolean visit(Alt<E> symbol) {
			boolean changed = false;
			for (Symbol s : symbol.getSymbols())
				changed |= s.accept(this);
			return changed;
		}

		@Override
		public Boolean visit(Opt symbol) {
			return symbol.getSymbol().accept(this);
		}

		@Override
		public Boolean visit(Plus symbol) {
			boolean changed = symbol.getSymbol().accept(this);
			for (Symbol sep : symbol.getSeparators())
				changed |= sep.accept(this);
			return changed;
		}

		@Override
		public <E extends Symbol> Boolean visit(Sequence<E> symbol) {
			boolean changed = false;
			for (Symbol s : symbol.getSymbols())
				changed |= s.accept(this);
			return changed;
		}

		@Override
		public Boolean visit(Star symbol) {
			boolean changed = symbol.getSymbol().accept(this);
			for (Symbol sep : symbol.getSeparators())
				changed |= sep.accept(this);
			return changed;
		}

	}
	
	private static boolean add(Nonterminal a, Nonterminal nonterminal, SetMultimap<Nonterminal, Nonterminal> reachabilityGraph) {
		boolean changed = false;
		changed |= reachabilityGraph.put(a, nonterminal);
		changed |= reachabilityGraph.putAll(a, reachabilityGraph.get(nonterminal));
		return changed;
	}
	
}
