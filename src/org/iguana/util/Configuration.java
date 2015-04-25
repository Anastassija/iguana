package org.jgll.util;

/**
 * 
 * @author Ali Afroozeh
 *
 */
public class Configuration {
	
	public static final Configuration DEFAULT = builder().build();
	
	private final GSSType gssType;
	
	private final LookupImpl gssLookupImpl;
	
	private final LookupImpl sppfLookupImpl;
	
	private final LookupImpl descriptorLookupImpl;
	
	private final LookupStrategy gssLookupStrategy;
	
	private final LookupStrategy sppfLookupStrategy;
	
	private final LookupStrategy descriptorLookupStrategy;
	
	private final int lookAheadCount;
	
	private Configuration(Builder builder) {
		this.gssType = builder.gssType;
		this.gssLookupImpl = builder.gssLookupImpl;
		this.sppfLookupImpl = builder.sppfLookupImpl;
		this.descriptorLookupImpl = builder.descriptorLookupImpl;
		this.gssLookupStrategy = builder.gssLookupStrategy;
		this.sppfLookupStrategy = builder.sppfLookupStrategy;
		this.descriptorLookupStrategy = builder.descriptorLookupStrategy;
		this.lookAheadCount = builder.lookaheadCount;
	}
	
	public GSSType getGSSType() {
		return gssType;
	}
	
	public LookupImpl getGSSLookupImpl() {
		return gssLookupImpl;
	}
	
	public LookupImpl getSPPFLookupImpl() {
		return sppfLookupImpl;
	}

	public LookupImpl getDescriptorLookupImpl() {
		return descriptorLookupImpl;
	}
	
	public LookupStrategy getGSSLookupStrategy() {
		return gssLookupStrategy;
	}
	
	public LookupStrategy getSPPFLookupStrategy() {
		return sppfLookupStrategy;
	}
	
	public LookupStrategy getDescriptorLookupStrategy() {
		return descriptorLookupStrategy;
	}
	
	public int getLookAheadCount() {
		return lookAheadCount;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static enum MatcherType {
		DFA,
		JAVA_REGEX
	}
	
	public static enum GSSType {
		NEW,
		ORIGINAL
	}
	
	public static enum LookupImpl {
		ARRAY,
		HASH_MAP
	}
	
	public static enum LookupStrategy {
		GLOBAL,
		DISTRIBUTED
	}
	
	@Override
	public String toString() {
		return "GSS Type: " + gssType + "\n" +
			   "GSS Lookup Strategy: " + gssLookupStrategy + "\n" +
			   "GSS Lookup Impl: " + gssLookupImpl + "\n" +
			   "Descriptor Lookup Strategy: " + descriptorLookupStrategy + "\n" +
			   "Descriptor Lookup Impl: " + descriptorLookupImpl + "\n" +
			   "SPPF Lookup Strategy: " + sppfLookupStrategy + "\n" +
			   "SPPF Lookup Impl: " + sppfLookupImpl;
	}
	
	public static class Builder {
		
		private GSSType gssType;
		private LookupImpl gssLookupImpl;
		private LookupImpl sppfLookupImpl;
		private LookupImpl descriptorLookupImpl;
		private LookupStrategy gssLookupStrategy;
		private LookupStrategy sppfLookupStrategy;
		private LookupStrategy descriptorLookupStrategy;
		private int lookaheadCount;
		
		public Builder() {
			this.gssType = GSSType.NEW;
			this.gssLookupImpl = LookupImpl.ARRAY;
			this.sppfLookupImpl = LookupImpl.HASH_MAP;
			this.descriptorLookupImpl = LookupImpl.HASH_MAP;
			this.gssLookupStrategy = LookupStrategy.DISTRIBUTED;
			this.sppfLookupStrategy = LookupStrategy.DISTRIBUTED;
			this.descriptorLookupStrategy = LookupStrategy.DISTRIBUTED;
			this.lookaheadCount = 0;
		}
		
		public Configuration build() {
			return new Configuration(this);
		}
		
		public Builder setGSSType(GSSType gssType) {
			this.gssType = gssType;
			return this;
		}
		
		public Builder setGSSLookupImpl(LookupImpl gssLookupImpl) {
			this.gssLookupImpl = gssLookupImpl;
			return this;
		}
		
		public Builder setSPPFLookupImpl(LookupImpl sppfLookupImpl) {
			this.sppfLookupImpl = sppfLookupImpl;
			return this;
		}
		
		public Builder setDescriptorLookupImpl(LookupImpl descriptorLookupImpl) {
			this.descriptorLookupImpl = descriptorLookupImpl;
			return this;
		}
		
		public Builder setGSSLookupStrategy(LookupStrategy gssLookupStrategy) {
			this.gssLookupStrategy = gssLookupStrategy;
			return this;
		}
		
		public Builder setSPPFLookupStrategy(LookupStrategy sppfLookupStrategy) {
			this.sppfLookupStrategy = sppfLookupStrategy;
			return this;
		}
		
		public Builder setDescriptorLookupStrategy(LookupStrategy descriptorLookupStrategy) {
			this.descriptorLookupStrategy = descriptorLookupStrategy;
			return this;
		}
		
		public Builder setLookaheadCount(int lookaheadCount) {
			this.lookaheadCount = lookaheadCount;
			return this;
		}
		
	}
}
