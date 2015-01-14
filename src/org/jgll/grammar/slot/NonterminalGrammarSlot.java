package org.jgll.grammar.slot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jgll.grammar.GrammarRegistry;
import org.jgll.grammar.symbol.Nonterminal;
import org.jgll.parser.GLLParser;
import org.jgll.parser.descriptor.Descriptor;
import org.jgll.parser.gss.GSSNode;
import org.jgll.parser.gss.lookup.GSSNodeLookup;
import org.jgll.sppf.DummyNode;
import org.jgll.sppf.NonPackedNode;
import org.jgll.sppf.NonterminalNode;
import org.jgll.util.Input;
import org.jgll.util.collections.Key;


/**
 * 
 * 
 * @author Ali Afroozeh
 *
 */
public class NonterminalGrammarSlot extends AbstractGrammarSlot {
	
	private final Nonterminal nonterminal;
	
	private final List<BodyGrammarSlot> firstSlots;
	
	private final GSSNodeLookup nodeLookup;

	private Map<Key, NonterminalNode> nonterminalNodes;

	public NonterminalGrammarSlot(int id, Nonterminal nonterminal, GSSNodeLookup nodeLookup) {
		super(id);
		this.nonterminal = nonterminal;
		this.nodeLookup = nodeLookup;
		this.firstSlots = new ArrayList<>();
		this.nonterminalNodes = new HashMap<>();
	}
	
	@Override
	public void execute(GLLParser parser, GSSNode u, int i, NonPackedNode node) {
		firstSlots.forEach(s -> parser.scheduleDescriptor(new Descriptor(s, u, i, DummyNode.getInstance())));
	}
	
	public void addFirstSlot(BodyGrammarSlot slot) {
		firstSlots.add(slot);
	}
	
	public List<BodyGrammarSlot> getFirstSlots() {
		return firstSlots;
	}
	
	public boolean test(int v)  {
		return true;
	}
	
	public Nonterminal getNonterminal() {
		return nonterminal;
	}
	
	@Override
	public String toString() {
		return nonterminal.getName();
	}
	
	@Override
	public String getConstructorCode(GrammarRegistry registry) {
		return new StringBuilder()
		           .append("new NonterminalGrammarSlot(")
		           .append(nonterminal.getConstructorCode(registry))
		           .append(")").toString();
	}
	
	@Override
	public GSSNode getGSSNode(int inputIndex) {
		return nodeLookup.getOrElseCreate(this, inputIndex);
	}
	
	@Override
	public GSSNode hasGSSNode(int inputIndex) { 
		return nodeLookup.get(inputIndex);
	}

	@Override
	public boolean isFirst() {
		return true;
	}
	
	public NonterminalNode getNonterminalNode(Key key, Supplier<NonterminalNode> s, Consumer<NonterminalNode> c) {
		return nonterminalNodes.computeIfAbsent(key, k -> { NonterminalNode val = s.get();
															c.accept(val);
															return val; 
														  });
		}
	
	public NonterminalNode findNonterminalNode(Key key) {
		return nonterminalNodes.get(key);
	}
	
	public Iterable<GSSNode> getGSSNodes() {
		return nodeLookup.getNodes();
	}

	@Override
	public void reset(Input input) {
		nodeLookup.reset(input);
		nonterminalNodes = new HashMap<>();
	}

}
