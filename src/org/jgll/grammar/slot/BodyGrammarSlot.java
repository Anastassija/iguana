package org.jgll.grammar.slot;

import java.util.HashMap;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jgll.datadependent.env.Environment;
import org.jgll.grammar.condition.Condition;
import org.jgll.grammar.condition.Conditions;
import org.jgll.grammar.condition.ConditionsFactory;
import org.jgll.grammar.symbol.Position;
import org.jgll.parser.GLLParser;
import org.jgll.parser.gss.GSSNode;
import org.jgll.parser.gss.GSSNodeData;
import org.jgll.parser.gss.lookup.GSSNodeLookup;
import org.jgll.sppf.IntermediateNode;
import org.jgll.sppf.NonPackedNode;
import org.jgll.util.Input;
import org.jgll.util.collections.Key;


public class BodyGrammarSlot extends AbstractGrammarSlot {
	
	protected final Position position;
	
	private HashMap<Key, IntermediateNode> intermediateNodes;
	
	private final GSSNodeLookup nodeLookup;
	
	private final Conditions conditions;

	public BodyGrammarSlot(int id, Position position, GSSNodeLookup nodeLookup, Set<Condition> conditions) {
		super(id);
		this.position = position;
		this.nodeLookup = nodeLookup;
		this.conditions = ConditionsFactory.getConditions(conditions);
		this.intermediateNodes = new HashMap<>();
	}
	
	@Override
	public String getConstructorCode() {
		return new StringBuilder()
    	  .append("new BodyGrammarSlot(")
    	  .append(")").toString();
	}
	
	@Override
	public String toString() {
		return position.toString();
	}
	
	@Override
	public boolean isFirst() {
		return position.isFirst();
	}
	
	public IntermediateNode getIntermediateNode(Key key, Supplier<IntermediateNode> s, Consumer<IntermediateNode> c) {
		return intermediateNodes.computeIfAbsent(key, k -> { IntermediateNode val = s.get();
															 c.accept(val);
															 return val; 
														   });
	}
	
	public IntermediateNode findIntermediateNode(Key key) {
		return intermediateNodes.get(key);
	}
	
	@Override
	public GSSNode getGSSNode(int inputIndex) {
		return nodeLookup.getOrElseCreate(this, inputIndex);
	}
	
	@Override
	public GSSNode hasGSSNode(int inputIndex) { 
		if (nodeLookup.isInitialized()) {
			return nodeLookup.get(inputIndex);
		} else {
			nodeLookup.init();			
			return null;
		}
	}
	
	public Conditions getConditions() {
		return conditions;
	}

	@Override
	public void reset(Input input) {
		intermediateNodes = new HashMap<>();
		nodeLookup.reset(input);
	}
	
	public void execute(GLLParser parser, GSSNode u, int i, NonPackedNode node) {
		getTransitions().forEach(t -> t.execute(parser, u, i, node));
	}
	
	/**
	 * 
	 * Data-dependent GLL parsing
	 * 
	 */
	public void execute(GLLParser parser, GSSNode u, int i, NonPackedNode node, Environment env) {
		getTransitions().forEach(t -> t.execute(parser, u, i, node, env));
	}
	
	/**
	 * 
	 * Data-dependent GLL parsing
	 * 
	 */
	@Override
	public <T> GSSNode getGSSNode(int inputIndex, GSSNodeData<T> data) {
		// FIXME:
		return nodeLookup.getOrElseCreate(this, inputIndex);
	}
	
	@Override
	public <T> GSSNode hasGSSNode(int inputIndex, GSSNodeData<T> data) {
		// FIXME:
		return nodeLookup.get(inputIndex);
	}
	
}
