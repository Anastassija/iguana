package org.jgll.grammar.symbol;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.jgll.regex.Automaton;
import org.jgll.regex.RegularExpression;
import org.jgll.regex.State;
import org.jgll.regex.Transition;

/**
 * 
 * @author Ali Afroozeh
 *
 */
public class Character extends AbstractSymbol implements RegularExpression {
	
	private static final long serialVersionUID = 1L;
	
	private final int c;
	
	private final BitSet bitSet;
	
	public Character(int c) {
		super("[" + getString(c) + "]");
		this.c = c;
		this.bitSet = new BitSet();
		bitSet.set(c);
	}
	
	public static Character from(int c) {
		return new Character(c);
	}
	
	public int getValue() {
		return c;
	}

	@Override
	public int hashCode() {
		return c;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Character)) {
			return false;
		}
		Character other = (Character) obj;
		
		return c == other.c;
	}

	public static String getString(int c) {
		if(c >= Constants.FIRST_PRINTABLE_CHAR && c <= java.lang.Character.MAX_VALUE) {
			return (char)c + "";			
		} else {
			return "\\u" + Integer.toHexString(c);
		}
	}

	@Override
	public BitSet asBitSet() {
		return bitSet;
	}
	
	@Override
	public Automaton toNFA() {
		return createNFA();
	}
	
	private Automaton createNFA() {
		State startState = new State();
		State finalState = new State(true);
		startState.addTransition(new Transition(c, finalState));
		return new Automaton(startState);
	}

	@Override
	public boolean isNullable() {
		return false;
	}

	@Override
	public Character copy() {
		return new Character(c);
	}
	
	public CharacterClass not() {
		List<Range> ranges = new ArrayList<>();
		if(c > 0) {
			ranges.add(Range.in(0, c - 1));
		}
		if(c < Constants.MAX_UTF32_VAL) {
			ranges.add(Range.in(c + 1, Constants.MAX_UTF32_VAL));
		}
		CharacterClass c = new CharacterClass(ranges);
		return c;
	}

}
