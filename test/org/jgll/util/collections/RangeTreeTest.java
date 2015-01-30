package org.jgll.util.collections;

import static org.junit.Assert.*;

import org.jgll.grammar.symbol.CharacterRange;
import org.junit.Test;

public class RangeTreeTest {

	@Test
	public void test1() {
		RangeTree<String> rangeTree = new RangeTree<>();
		rangeTree.insert(CharacterRange.in(1, 4), "a");
		rangeTree.insert(CharacterRange.in(7, 13), "b");
		rangeTree.insert(CharacterRange.in(18, 101), "c");
		checkAsserts(rangeTree);
	}
	
	@Test
	public void test2() {
		RangeTree<String> rangeTree = new RangeTree<>();
		rangeTree.insert(CharacterRange.in(18, 101), "c");
		rangeTree.insert(CharacterRange.in(7, 13), "b");
		rangeTree.insert(CharacterRange.in(1, 4), "a");
		checkAsserts(rangeTree);
	}
	
	@Test
	public void test3() {
		RangeTree<String> rangeTree = new RangeTree<>();
		rangeTree.insert(CharacterRange.in(18, 101), "c");
		rangeTree.insert(CharacterRange.in(1, 4), "a");
		rangeTree.insert(CharacterRange.in(7, 13), "b");
		checkAsserts(rangeTree);
	}
	
	@Test
	public void test4() {
		RangeTree<String> rangeTree = new RangeTree<>();
		rangeTree.insert(CharacterRange.in(7, 13), "b");
		rangeTree.insert(CharacterRange.in(18, 101), "c");
		rangeTree.insert(CharacterRange.in(1, 4), "a");
		checkAsserts(rangeTree);
	}
	
	@Test
	public void test5() {
		RangeTree<Void> rangeTree = new RangeTree<>();
		rangeTree.insert(44);
		rangeTree.insert(17);
		rangeTree.insert(32);
		rangeTree.insert(78);
		rangeTree.insert(50);
		rangeTree.insert(48);
		rangeTree.insert(62);
		rangeTree.insert(54);
		rangeTree.insert(88);
		assertEquals(9, rangeTree.size());
		assertEquals(3, rangeTree.getRoot().getHeight());
	}
	
	@Test
	public void test6() {
		RangeTree<Void> rangeTree = new RangeTree<>();
		rangeTree.insert(14);
		rangeTree.insert(17);
		rangeTree.insert(11);
		rangeTree.insert(7);
		rangeTree.insert(53);
		rangeTree.insert(4);
		rangeTree.insert(13);
		assertEquals(7, rangeTree.size());
		assertEquals(3, rangeTree.getRoot().getHeight());
	}
	
	private static <T> void checkAsserts(RangeTree<T> rangeTree) {
		assertEquals(null, rangeTree.get(0));
		assertEquals("a",  rangeTree.get(1));
		assertEquals("a",  rangeTree.get(3));
		assertEquals("a",  rangeTree.get(4));
		assertEquals(null, rangeTree.get(5));
		assertEquals("b",  rangeTree.get(7));
		assertEquals("b",  rangeTree.get(12));
		assertEquals("b",  rangeTree.get(13));
		assertEquals("c",  rangeTree.get(18));
		assertEquals("c",  rangeTree.get(101));
		assertEquals(null, rangeTree.get(121));

		assertTrue(rangeTree.isBalanced());
		assertEquals(3, rangeTree.size());
	}
	
}
