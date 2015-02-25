package org.jgll.util.collections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.jgll.grammar.symbol.Character;
import org.jgll.grammar.symbol.CharacterRange;
import org.jgll.util.collections.IntRangeTree.IntNode;

/**
 * 
 * An implementaion of AVL trees for fast range search
 * that used integer keys.
 * 
 * @author Ali Afroozeh
 *
 */
public class IntRangeTree implements Iterable<IntNode>{	

	private IntNode root;
	
	private int countNodes;
	
	public IntNode getRoot() {
		return root;
	}
	
	public int size() {
		return countNodes;
	}
	
	public boolean contains(int key) {
		return get(key) != -1;
	}
	
	public boolean contains(CharacterRange range) {
		return getNode(range, root) != null;
	}
	
	private IntNode getNode(CharacterRange range, IntNode node) {
		if (node == null)
			return null;
		
		if (range.getStart() == node.start && range.getEnd() == node.end)
			return node;
		
		if (range.getStart() < node.start)
			return getNode(range, node.left);
		
		if (range.getEnd() > node.end)
			return getNode(range, node.right);
		
		throw new RuntimeException("Should not reach here!");
	}
	
	public int get(int key) {
		return get(key, root);
	}
	
	public int get(int key, IntNode node) {
		if (node == null)
			return -1;
		
		if (key < node.start) 
			return get(key, node.left);
		
		if (key > node.end)
			return get(key, node.right);
		
		return node.val;
	}
	
	public void insert(int key, int val) {
		insert(CharacterRange.in(key, key), val);
	}
	
	public void insert(CharacterRange range, int val) {
		insert(range, val, root, new IntNode(range.getStart(), range.getEnd(), val));
	}
	
	private void insert(CharacterRange range, int val, IntNode parent, IntNode newNode) {		
		
		if (root == null) {
			root = newNode;
			countNodes++;
			return;
		}
		
		if (range.getStart() < parent.start) {
			if (parent.left == null) {
				addToLeft(parent, newNode);
				balance(parent);
			} else {
				insert(range, val, parent.left, newNode);
			}
		} 
		else if (range.getEnd() > parent.end) {
			if (parent.right == null) {
				addToRight(parent, newNode);
				balance(parent);
			} else {
				insert(range, val, parent.right, newNode);					
			}
		}
	}
	
	private void addToLeft(IntNode parent, IntNode newNode) {
		countNodes++;
		parent.left = newNode;
		newNode.parent = parent;
	}
	
	private void addToRight(IntNode parent, IntNode newNode) {
		countNodes++;
		parent.right = newNode;
		newNode.parent = parent;
	}
	 
	private void balance(IntNode node) {
		node.updateHeight();
		
		if (!node.isBalanced()) {
			if (node.right != null) {
				if (node.right.isRightHeavy()) {
					leftRotate(node);
				} 
				else if (node.right.isLeftHeavy()) {
					rightRotate(node.right);
					leftRotate(node);
				}
			}
			if (node.left != null) {
				if (node.left.isLeftHeavy()) {
					rightRotate(node);					
				} 
				else if (node.left.isRightHeavy()) {
					leftRotate(node.left);
					rightRotate(node);
				}
			}
		}
		
		if (node.parent != null) {
			balance(node.parent);
		}
	}

	/**
	 *      x                y
	 *     / \              / \
	 *    A   y     =>     x   C 
	 *       / \          / \
	 *      B   C        A   B
	 */
	private void leftRotate(IntNode x) {
		IntNode parent = x.parent;
		
		IntNode y = x.right;
		IntNode B = y.left;
		y.left = x;
		x.parent = y;
		x.right = B;
		if (B != null)
			B.parent = x;
		
		y.parent = parent;
		if (parent != null) {
			parent.replaceChild(x, y);
		} else {
			root = y;
		}
		
		x.updateHeight();
		y.updateHeight();
	}
	
	/**
	 *      x                y
	 *     / \              / \
	 *    y   C     =>     A   x 
	 *   / \                  / \
	 *  A   B                B   C
	 *      
	 */
	private void rightRotate(IntNode x) {
		IntNode parent = x.parent;
		
		IntNode y = x.left;
		IntNode B = y.right;
		y.right = x;
		x.parent = y;
		x.left = B;
		
		if (B != null)
			B.parent = x;

		y.parent = parent;
		if (parent != null) {
			parent.replaceChild(x, y);
		} else {
			root = y;
		}
		
		x.updateHeight();
		y.updateHeight();
	}
	
	public boolean isBalanced() {
		return inOrder(root).stream().allMatch(n -> n.isBalanced());
	}

	private List<IntNode> inOrder(IntNode node) {
		List<IntNode> list = new ArrayList<>();
		inOrder(node, (n) -> list.add(n));
		return list;
	}
	
	private void inOrder(IntNode node, Consumer<IntNode> consumer) {
		if (node.left != null)
			inOrder(node.left, consumer);
		
		consumer.accept(node);
		
		if(node.right != null) 
			inOrder(node.right, consumer);
	}
	
	@Override
	public Iterator<IntNode> iterator() {
		return inOrder(root).iterator();
	}
	
	public static class IntNode {
		final int start;
		final int end;
		final int val;
		IntNode left;
		IntNode right;
		int height;
		
		IntNode parent;
		
		public IntNode(int start, int end, int val) {
			this.start = start;
			this.end = end;
			this.val = val;
			updateHeight();
		}
		
		public void replaceChild(IntNode child, IntNode replacement) {
			// Fix the pointers of the parents
			if (left == child) {
				left = replacement;
			} else {
				right = replacement;
			}		
		}
		
		public void updateHeight() {
			this.height = height();
		}
		
		public boolean isBalanced() {
			return Math.abs(heightLeft() - heightRight()) <= 1;
		}
		
		public boolean isRightHeavy() {
			return heightRight() > heightLeft();
		}
		
		public boolean isLeftHeavy() {
			return heightLeft() > heightRight();
		}
		
		public int getHeight() {
			return height;
		}
		
		private int height() {
			int max;
			if (heightLeft() > heightRight())
				max = heightLeft();
			else
				max = heightRight();
			
			return max + 1;
		}
		
		private int heightLeft() {
			return left == null ? -1 : left.height;
		}
		
		private int heightRight() {
			return right == null ? -1 : right.height;
		}
		
		public IntNode getLeft() {
			return left;
		}
		
		public IntNode getRight() {
			return right;
		}
		
		@Override
		public String toString() {
			return String.format("%s-%s", Character.getName(start), Character.getName(end));
		}
	}

}
