package org.jgll.parser.gss.lookup;

import java.util.HashMap;
import java.util.Map;

import org.jgll.grammar.slot.GrammarSlot;
import org.jgll.parser.gss.GSSEdge;
import org.jgll.parser.gss.GSSNode;
import org.jgll.parser.gss.GSSNodeData;
import org.jgll.sppf.NonPackedNode;

/**
 * 
 * 
 * @author Ali Afroozeh
 * 
 */
public class GlobalHashGSSLookupImpl extends AbstractGSSLookup {

	private Map<GSSNode, GSSNode> gssNodes;
	
	public GlobalHashGSSLookupImpl() {
		gssNodes = new HashMap<>();
	}

	@Override
	public GSSNode getGSSNode(GrammarSlot head, int inputIndex) {
		countGSSNodes++;
		GSSNode gssNode = new GSSNode(head, inputIndex);
		gssNodes.put(gssNode, gssNode);		
		return gssNode;
	}
	
	@Override
	public GSSNode hasGSSNode(GrammarSlot head, int inputIndex) {
		return gssNodes.get(new GSSNode(head, inputIndex));
	}

	@Override
	public Iterable<GSSNode> getGSSNodes() {
		return gssNodes.values();
	}

	@Override
	public boolean addToPoppedElements(GSSNode gssNode, NonPackedNode sppfNode) {
		return gssNode.addToPoppedElements(sppfNode);
	}

	@Override
	public boolean getGSSEdge(GSSNode gssNode, GSSEdge edge) {
		countGSSEdges++;
		return gssNode.getGSSEdge(edge);
	}
	
	@Override
	public void reset() {
		gssNodes = new HashMap<>();
	}

	/**
	 * 
	 * Data-dependent GLL parsing
	 * 
	 */
	@Override
	public <T> GSSNode getGSSNode(GrammarSlot slot, int inputIndex, GSSNodeData<T> data) {
		
		return null;
	}

	@Override
	public <T> GSSNode hasGSSNode(GrammarSlot slot, int inputIndex, GSSNodeData<T> data) {
		
		return null;
	}

	@Override
	public <T> boolean getGSSEdge(GSSNode gssNode, GSSEdge edge, GSSNodeData<T> data) {
		
		return false;
	}

}
