package org.jgll.grammar.symbol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgll.parser.HashFunctions;
import org.jgll.regex.Matcher;
import org.jgll.regex.automaton.Automaton;
import org.jgll.regex.automaton.State;
import org.jgll.regex.automaton.StateType;
import org.jgll.regex.automaton.Transition;


/**
 * 
 * @author Ali Afroozeh
 *
 */
public class CharacterRange extends AbstractRegularExpression implements Comparable<CharacterRange> {
	
	private static final long serialVersionUID = 1L;
	
	private final int start;
	
	private final int end;
	
	public static CharacterRange in(int start, int end) {
		return new Builder(start, end).build();
	}
		
	private CharacterRange(Builder builder) {
		super(builder);
		
		if (builder.end < builder.start) 
			throw new IllegalArgumentException("Start cannot be less than end.");
		
		this.start = builder.start;
		this.end = builder.end;
	}

	public static String getName(int start, int end) {
		if (start == end) {
			return Character.getName(start);
		} else {
			return Character.getName(start) + "-" + Character.getName(end);			
		}
	}
	
	public int getStart() {
		return start;
	}
	
	public int getEnd() {
		return end;
	}
	
	public boolean contains(int c) {
		return start <= c && c <= end;
	}
	
	public boolean contains(CharacterRange other) {
		return start <= other.start && end >= other.end;
	}
	
	public boolean overlaps(CharacterRange other) {
		return !(end < other.start || other.end < start);
	}

	@Override
	public int hashCode() {
		return HashFunctions.defaulFunction.hash(start, end);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		
		if (!(obj instanceof CharacterRange))
			return false;
		
		CharacterRange other = (CharacterRange) obj;
		
		return start == other.start && end == other.end;
	}

	@Override
	protected Automaton createAutomaton() {
		State startState = new State();
		State finalState = new State(StateType.FINAL);
		startState.addTransition(new Transition(start, end, finalState));
		return new Automaton(startState, name);
	}
	
	@Override
	public Matcher getMatcher() {
		return (input, i) -> input.charAt(i) >= start && input.charAt(i) <= end ? 1 : -1;
	}
	
	@Override
	public Matcher getBackwardsMatcher() {
		return getMatcher();
	}

	@Override
	public boolean isNullable() {
		return false;
	}
	
	@Override
	public boolean isSingleChar() {
		return start == end;
	}
	
	@Override
	public Character asSingleChar() {
		return Character.from(start);
	}
	
	@Override
	public boolean isTerminal() {
		return true;
	}

	@Override
	public int compareTo(CharacterRange o) {
		return start == o.start ? end - o.end : start - o.start;
	}

	@Override
	public Set<CharacterRange> getFirstSet() {
		Set<CharacterRange> firstSet = new HashSet<>();
		firstSet.add(this);
		return firstSet;
	}
	
	@Override
	public Set<CharacterRange> getNotFollowSet() {
		return Collections.emptySet();
	}
	
	public static List<CharacterRange> toNonOverlapping(CharacterRange...ranges) {
		return toNonOverlapping(Arrays.asList(ranges));
	}
	
	public static List<CharacterRange> toNonOverlapping(List<CharacterRange> ranges) {
		
		if (ranges.size() < 2)
			return ranges;
		
		Collections.sort(ranges);

		List<CharacterRange> result = new ArrayList<>();
		
		Set<CharacterRange> overlapping = new HashSet<>();
		
		for (int i = 0; i < ranges.size(); i++) {
			
			CharacterRange current = ranges.get(i);
			overlapping.add(current);

			if (i + 1 < ranges.size()) {
				CharacterRange next = ranges.get(i + 1);
				if (!current.overlaps(next)) {
					result.addAll(convertOverlapping(overlapping));
					overlapping.clear();
				}
			}
		}
		
		result.addAll(convertOverlapping(overlapping));
		
		return result;
	}
	
	private static List<CharacterRange> convertOverlapping(Set<CharacterRange> ranges) {
		
		if (ranges.isEmpty())
			return Collections.emptyList();
		
		Set<Integer> set = new HashSet<>();
		for (CharacterRange r : ranges) {
			set.add(r.start - 1);
			set.add(r.end);
		}
		List<Integer> l = new ArrayList<>(set);
		Collections.sort(l);
		
		List<CharacterRange> result = new ArrayList<>();
		
		int start = l.get(0) + 1;
		for (int i = 1; i < l.size(); i++) {
			result.add(CharacterRange.in(start, l.get(i)));
			start = l.get(i) + 1;
		}
		
		return result;
	}

	public static Builder builder(int start, int end) {
		return new Builder(start, end);
	}
	
    @Override
    public SymbolBuilder<? extends Symbol> copyBuilder() {
        return new Builder(this);
    }
    
	@Override
	public String getConstructorCode() {
		return CharacterRange.class.getSimpleName() + ".builder(" + start + ", " + end + ")" + super.getConstructorCode() + ".build()";
	}
	
	public static class Builder extends SymbolBuilder<CharacterRange> {

		private int start;
		private int end;

		public Builder(int start, int end) {
			super(getName(start, end));
			this.start = start;
			this.end = end;
		}
		
		public Builder(CharacterRange range) {
			this(range.start, range.end);
		}
		
		@Override
		public CharacterRange build() {
			return new CharacterRange(this);
		}
	}

	@Override
	public String getPattern() {
		return "[" + getName() + "]";
	}
	
}
