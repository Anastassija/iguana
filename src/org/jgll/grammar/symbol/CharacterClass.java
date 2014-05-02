package org.jgll.grammar.symbol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jgll.grammar.condition.Condition;
import org.jgll.regex.RegexAlt;
import org.jgll.regex.automaton.Automaton;
import org.jgll.util.CollectionsUtil;

/**
 * Character class represents a set of {@link Range} instances.
 * For example, [A-Za-z0-9] represents a character which is
 * either [A-Z], [a-z] or [0-9].
 * 
 * @author Ali Afroozeh
 *
 */
public class CharacterClass extends AbstractRegularExpression {
	
	private static final long serialVersionUID = 1L;
	
	private final RegexAlt<Range> alt;
	
	public CharacterClass(Range...ranges) {
		this(Arrays.asList(ranges), Collections.<Condition>emptySet());
	}
	
	public CharacterClass(List<Range> ranges, Set<Condition> conditions) {
		this(new RegexAlt<>(ranges), conditions);
	}
	
	public CharacterClass(RegexAlt<Range> alt, Set<Condition> conditions) {
		super(alt.toString(), conditions);
		this.alt = alt.withConditions(conditions);
	}
	
	public CharacterClass(RegexAlt<Range> alt) {
		this(alt, Collections.<Condition>emptySet());
	}
	
	public CharacterClass(List<Range> ranges) {
		this(ranges, Collections.<Condition>emptySet());
	}
	
	public static CharacterClass fromChars(Character...chars) {
		List<Range> list = new ArrayList<>();
		for(Character c : chars) {
			list.add(Range.in(c.getValue(), c.getValue()));
		}
		return new CharacterClass(list, Collections.<Condition>emptySet());
	}
	
	@Override
	public int hashCode() {
		return alt.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof CharacterClass)) {
			return false;
		}
		
		CharacterClass other = (CharacterClass) obj;

		return alt.equals(other.alt);
	}

	@Override
	protected Automaton createAutomaton() {
		return alt.getAutomaton();
	}

	@Override
	public boolean isNullable() {
		return false;
	}

	public CharacterClass not() {
		List<Range> newRanges = new ArrayList<>();
		
		int i = 0;
		
		Range[] ranges = alt.getRegularExpressions().toArray(new Range[] {});
		Arrays.sort(ranges);
		
		if(ranges[i].getStart() >= 1) {
			newRanges.add(Range.in(1, ranges[i].getStart() - 1));
		}
		
		for(; i < ranges.length - 1; i++) {
			Range r1 = ranges[i];
			Range r2 = ranges[i + 1];
			
			if(r2.getStart() > r1.getEnd() + 1) {
				newRanges.add(Range.in(r1.getEnd() + 1, r2.getStart() - 1));
			}
		}
		
		if(ranges[i].getEnd() < Constants.MAX_UTF32_VAL) {
			newRanges.add(Range.in(ranges[i].getEnd() + 1, Constants.MAX_UTF32_VAL));
		}
		
		return new CharacterClass(newRanges, conditions);
	}

	@Override
	public Set<Range> getFirstSet() {
		return alt.getFirstSet();
	}
	
	public int size() {
		return alt.size();
	}
	
	public Range get(int index) {
		return alt.get(index);
	}

	@Override
	public CharacterClass withConditions(Set<Condition> conditions) {
		return new CharacterClass(alt, CollectionsUtil.union(conditions, this.conditions));
	}
	
	@Override
	public CharacterClass withoutConditions() {
		return new CharacterClass(alt);
	}
	
}
