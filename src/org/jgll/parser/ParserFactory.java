package org.jgll.parser;

import org.jgll.grammar.Grammar;
import org.jgll.parser.gss.lookup.DistributedGSSLookupImpl;
import org.jgll.parser.gss.lookup.GSSLookup;
import org.jgll.parser.gss.lookup.GlobalHashGSSLookupImpl;
import org.jgll.parser.lookup.DescriptorLookup;
import org.jgll.parser.lookup.DistributedDescriptorLookupImpl;
import org.jgll.parser.lookup.GlobalDescriptorLookupImpl;
import org.jgll.sppf.lookup.DistributedSPPFLookupImpl;
import org.jgll.sppf.lookup.GlobalSPPFLookupImpl;
import org.jgll.sppf.lookup.OriginalDistributedSPPFLookupImpl;
import org.jgll.sppf.lookup.OriginalGlobalSPPFLookupImpl;
import org.jgll.sppf.lookup.SPPFLookup;
import org.jgll.util.Configuration;
import org.jgll.util.Configuration.GSSType;
import org.jgll.util.Configuration.LookupStrategy;
import org.jgll.util.Input;
import org.jgll.util.hashing.hashfunction.HashFunction;

/**
 * 
 * @author Ali Afroozeh
 *
 */
public class ParserFactory {
	
	public static GLLParser getParser(Configuration config, Input input, Grammar grammar) {
		if (config.getGSSType() == GSSType.NEW) {
			return newParser(config, input, grammar);
		} else {
			return originalParser(config, input, grammar);
		}
	}
	
	private static GLLParser newParser(Configuration config, Input input, Grammar grammar) {
		return new NewGLLParserImpl(getGSSLookup(config, input, grammar), 
								    getSPPFLookup(config, input, grammar), 
								    getDescriptorLookup(config, input, grammar));		
	}
	
	private static GLLParser originalParser(Configuration config, Input input, Grammar grammar) {
		return new OriginalGLLParserImpl(getGSSLookup(config, input, grammar), 
				 					     getSPPFLookup(config, input, grammar), 
				 					     getDescriptorLookup(config, input, grammar));
	}

	private static GSSLookup getGSSLookup(Configuration config, Input input, Grammar grammar) {
		if (config.getGSSLookupStrategy() == LookupStrategy.DISTRIBUTED) {
			return new DistributedGSSLookupImpl();
		} else {
			return new GlobalHashGSSLookupImpl();
		}
	}
	
	private static SPPFLookup getSPPFLookup(Configuration config, Input input, Grammar grammar) {
		int inputSize =  getSize(input.length());
		int grammarSize = getSize(grammar.size());

		HashFunction hash; 
		
		if (config.getSPPFLookupStrategy() == LookupStrategy.DISTRIBUTED) {
			hash = HashFunctions.coefficientHash(inputSize);
			if (config.getGSSType() == GSSType.NEW) {
				return new DistributedSPPFLookupImpl(hash);
			} else {
				return new OriginalDistributedSPPFLookupImpl(hash);
			}
		} else {
			hash = HashFunctions.coefficientHash(grammarSize, inputSize);
			if (config.getGSSType() == GSSType.NEW) {
				return new GlobalSPPFLookupImpl(hash);				
			} else {
				return new OriginalGlobalSPPFLookupImpl(hash);
			}			
		}		
	}
	
	private static DescriptorLookup getDescriptorLookup(Configuration config, Input input, Grammar grammar) {
		int inputSize =   getSize(input.length());
		int grammarSize = getSize(grammar.size());
		
		HashFunction hash;
		if (config.getDescriptorLookupStrategy() == LookupStrategy.DISTRIBUTED) {
			hash = HashFunctions.coefficientHash(grammarSize);
			return new DistributedDescriptorLookupImpl(hash);			
		} else {
			hash = HashFunctions.coefficientHash(grammarSize, inputSize, grammarSize);
			return new GlobalDescriptorLookupImpl(hash);
		}
	}
	
	private static int getSize(int n) {
		return n + 1;
//		return (int) Math.pow(2, Math.ceil(Math.log(n) / Math.log(2)));
	}

}
