package org.jgll.parser;


import org.jgll.datadependent.env.Environment;
import org.jgll.grammar.condition.Condition;
import org.jgll.grammar.slot.BodyGrammarSlot;
import org.jgll.grammar.slot.DummySlot;
import org.jgll.grammar.slot.GrammarSlot;
import org.jgll.grammar.slot.NonterminalGrammarSlot;
import org.jgll.parser.gss.GSSEdge;
import org.jgll.parser.gss.GSSNode;
import org.jgll.parser.gss.OriginalGSSEdgeImpl;
import org.jgll.parser.gss.lookup.GSSLookup;
import org.jgll.parser.lookup.DescriptorLookup;
import org.jgll.sppf.DummyNode;
import org.jgll.sppf.NonPackedNode;
import org.jgll.sppf.lookup.SPPFLookup;

/**
 *
 * @author Ali Afroozeh
 * 
 */
public class OriginalGLLParserImpl extends AbstractGLLParserImpl {
	
	protected final GSSNode u0 = gssLookup.getGSSNode(DummySlot.getInstance(), 0);
		
	public OriginalGLLParserImpl(GSSLookup gssLookup, SPPFLookup sppfLookup, DescriptorLookup descriptorLookup) {
		super(gssLookup, sppfLookup, descriptorLookup);
	}
	
	@Override
	protected void initParserState(NonterminalGrammarSlot startSymbol) {
		u0.clearDescriptors();
		cu = u0;
		cn = DummyNode.getInstance();
		ci = 0;
		errorSlot = null;
		errorIndex = 0;
		errorGSSNode = null;
	}
	
	@Override
	public final void pop(GSSNode gssNode, int inputIndex, NonPackedNode node) {
		
		if (gssNode != u0) {

			if (!gssLookup.addToPoppedElements(gssNode, node))
				return;

			BodyGrammarSlot returnSlot = (BodyGrammarSlot) gssNode.getGrammarSlot();

			for(Condition c : returnSlot.getConditions()) {
				// FIXME: Data-dependent GLL
				if (c.getSlotAction().execute(input, gssNode, inputIndex, ctx.getEmptyEnvironment())) 
					break;
			}
			
			log.debug("Pop %s, %d, %s", gssNode, inputIndex, node);
						
			for (GSSEdge edge : gssNode.getGSSEdges()) {
				NonPackedNode y = sppfLookup.getNode(returnSlot, edge.getNode(), node);
				// FIXME: Data-dependent GLL
				addDescriptor(returnSlot, edge.getDestination(), inputIndex, y, ctx.getEmptyEnvironment());
			}
		}
	}
	
	@Override
	public final GSSNode createGSSNode(GrammarSlot returnSlot, NonterminalGrammarSlot nonterminal, int i) {
		return gssLookup.getGSSNode(returnSlot, ci);
	}
	
	@Override
	public final GSSNode hasGSSNode(GrammarSlot returnSlot, NonterminalGrammarSlot nonterminal, int i) {
		return gssLookup.hasGSSNode(returnSlot, ci);
	}

	@Override
	public void createGSSEdge(BodyGrammarSlot slot, GSSNode destination, NonPackedNode w, GSSNode source) {		
		GSSEdge edge = new OriginalGSSEdgeImpl(w, destination);
		
		if(gssLookup.getGSSEdge(source, edge)) {
			
			BodyGrammarSlot returnSlot = (BodyGrammarSlot) source.getGrammarSlot();
			
			log.trace("GSS Edge created from %s to %s", source, destination);
			
			for (NonPackedNode z : source.getPoppedElements()) {
				
				// Execute pop actions for continuations, when the GSS node already
				// exits. The input index will be the right extend of the node
				// stored in the popped elements.
				for (Condition c : returnSlot.getConditions()) {
					if (c.getSlotAction().execute(input, destination, z.getRightExtent())) 
						break;
				}
				
				NonPackedNode x = sppfLookup.getNode(returnSlot, w, z);
				addDescriptor(returnSlot, destination, z.getRightExtent(), x);
			}
		}
	}

	/**
	 * 
	 * Data-dependent GLL parsing
	 * 
	 */
	@Override
	public void createGSSEdge(BodyGrammarSlot slot, GSSNode destination, NonPackedNode w, GSSNode source, Environment env) {
		
		GSSEdge edge = new OriginalGSSEdgeImpl(w, destination);
		
		if(gssLookup.getGSSEdge(source, edge)) {
			
			BodyGrammarSlot returnSlot = (BodyGrammarSlot) source.getGrammarSlot();
			
			log.trace("GSS Edge created from %s to %s", source, destination);
			
			for (NonPackedNode z : source.getPoppedElements()) {
				
				// Execute pop actions for continuations, when the GSS node already
				// exits. The input index will be the right extend of the node
				// stored in the popped elements.
				for (Condition c : returnSlot.getConditions()) {
					// FIXME: Data-dependent GLL
					if (c.getSlotAction().execute(input, destination, z.getRightExtent(), env)) 
						break;
				}
				
				NonPackedNode x = sppfLookup.getNode(returnSlot, w, z);
				// TODO: Data-dependent GLL, check here
				addDescriptor(returnSlot, destination, z.getRightExtent(), x, env);
			}
		}
	}

}