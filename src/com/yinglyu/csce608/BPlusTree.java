package com.yinglyu.csce608;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BPlusTree<Integer extends Comparable<? super Integer>> {
	private static final int DEFAULT_ORDER = 3;

	private int order;

	private Node root;

	private StringBuilder beforeNodes;

	private StringBuilder afterNodes;

	public String toString() {
		Queue<Node> q = new LinkedList<>();
		q.add(root);
		StringBuilder sb = new StringBuilder();
		while (!q.isEmpty()) {
			int len = q.size();
			for (int i = 0; i < len; i++) {
				Node node = q.poll();
				sb.append(node);
				sb.append(";");
				if (node instanceof BPlusTree.NonLeafNode) {
					NonLeafNode nonLeafNode = (BPlusTree.NonLeafNode) node;
					for (Node child : nonLeafNode.children) {
						q.offer(child);
					}
				}
			}
			sb.append("\n");

		}
		return sb.toString();
	}

	public List<Integer> searchRange(Integer key1, Integer key2) {
		return root.searchRange(key1, key2);
	}

	public BPlusTree() {
		this(DEFAULT_ORDER);
	}

	public BPlusTree(int order) {
		this.order = order;
		root = new LeafNode();
	}

	public BPlusTree(int order, List<Integer> records, boolean isSparse) {
		this.order = order;
		root = new LeafNode();
		List<Integer> data = new ArrayList<>();
		data.addAll(records);
		Collections.sort(data);
		root.keys = data;
		while (root.isOverflow()) {

			Node newRoot = root.spread(isSparse);

			root = newRoot;
		}
	}

	public boolean insert(Integer key) {
		beforeNodes = new StringBuilder();
		afterNodes = new StringBuilder();

		String before = root.toString();
		boolean res = root.insert(key);
		String after = root.toString();
		String rtMark = "{Root}";

		if (!before.equals(after)) {
			beforeNodes.insert(0, rtMark + before + rtMark + "\n");
			afterNodes.insert(0, rtMark + after + rtMark + "\n");
		}

		System.out.print("before: " + beforeNodes.toString() + "\n");
		System.out.print("after : " + afterNodes.toString() + "\n");

		return res;
	}

	public void insert(List<Integer> records) {
		for (Integer record : records) {
			root.insert(record);
		}
	}

	public boolean delete(Integer key) {
		beforeNodes = new StringBuilder();
		afterNodes = new StringBuilder();

		String before = root.toString();
		boolean res = root.delete(key);
		String after = root.toString();
		String rtMark = "{Root}";

		if (!before.equals(after)) {
			beforeNodes.insert(0, rtMark + before + rtMark + ";\n");
			afterNodes.insert(0, rtMark + after + rtMark + ";\n");
		}
		System.out.print("before: " + beforeNodes.toString() + "\n");
		System.out.print("after : " + afterNodes.toString() + "\n");
		return res;
	}

	private abstract class Node {
		List<Integer> keys;

		public int getKeySize() {
			return keys.size();
		}

		abstract boolean search(Integer key);

		abstract List<Integer> searchRange(Integer key1, Integer key2);

		abstract boolean insert(Integer key);

		abstract boolean delete(Integer key);

		abstract Integer getFirstLeafKey();

		abstract void coalesce(Node sibling);

		abstract Node split();

		abstract Node spread(boolean isSparse);

		abstract boolean isOverflow();

		abstract boolean isUnderflow();

		public String toString() {
			return keys.toString();
		}

	}

	private class NonLeafNode extends Node {
		List<Node> children;

		NonLeafNode() {
			this.keys = new ArrayList<Integer>();
			this.children = new ArrayList<Node>();
		}

		@Override
		boolean search(Integer key) {
			return getChild(key).search(key);
		}

		@Override
		boolean delete(Integer key) {
			Node child = getChild(key);

			StringBuilder before = new StringBuilder();
			StringBuilder after = new StringBuilder();

			before.append(child);
			boolean res = child.delete(key);
			if (!res) {
				return res;
			}
			Node childLeftSibling = getChildLeftSibling(key);
			Node childRightSibling = getChildRightSibling(key);
			int loc = getIndex(key);
			if (loc >= 0) {
				keys.set(loc, child.getFirstLeafKey());
			}

			if (child.isUnderflow()) {

				Node left = childLeftSibling != null ? childLeftSibling : child;
				Node right = childLeftSibling != null ? child : childRightSibling;

				if (childLeftSibling != null) {
					before.insert(0, childLeftSibling);
				} else if (childRightSibling != null) {
					before.append(childRightSibling);
				}

				left.coalesce(right);
				deleteChild(right.getFirstLeafKey());
				if (left.isOverflow()) {
					Node sibling = left.split();
					insertChild(sibling.getFirstLeafKey(), sibling);
					after.append(sibling);
				}

				if (root.getKeySize() == 0) {
					root = left;
					// System.out.println("level down");
				} else {
					after.insert(0, left);
				}

			} else {
				after.append(child);
			}
			if (!before.toString().equals(after.toString())) {
				beforeNodes.insert(0, ";\n");
				afterNodes.insert(0, ";\n");
				beforeNodes.insert(0, before);
				afterNodes.insert(0, after);

			}
			return res;
		}

		@Override
		boolean insert(Integer key) {
			Node child = getChild(key);

			StringBuilder before = new StringBuilder();
			StringBuilder after = new StringBuilder();
			before.append(child);

			boolean res = child.insert(key);

			if (!res) {
				return res;
			}

			if (child.isOverflow()) {
				Node sibling = child.split();
				insertChild(sibling.getFirstLeafKey(), sibling);
				after.append(sibling);
			}
			after.insert(0, child);
			if (root.isOverflow()) {
				Node sibling = split();
				NonLeafNode newRoot = new NonLeafNode();
				newRoot.keys.add(sibling.getFirstLeafKey());
				newRoot.children.add(this);
				newRoot.children.add(sibling);
				root = newRoot;
				after.insert(0, sibling + ";\n");
				after.insert(0, this);
			}

			if (!before.toString().equals(after.toString())) {
				beforeNodes.insert(0, before + ";\n");
				afterNodes.insert(0, after + ";\n");
			}

			return res;
		}

		@Override
		Integer getFirstLeafKey() {
			return children.get(0).getFirstLeafKey();
		}

		@Override
		List<Integer> searchRange(Integer key1, Integer key2) {
			return getChild(key1).searchRange(key1, key2);
		}

		@Override
		void coalesce(Node sibling) {
			NonLeafNode node = (NonLeafNode) sibling;
			keys.add(node.getFirstLeafKey());// add a key before the first pointer in the sibling.
			keys.addAll(node.keys);
			children.addAll(node.children);
		}

		@Override
		Node split() {
			int fromIndex = getKeySize() / 2 + 1, toIndex = getKeySize();
			NonLeafNode sibling = new NonLeafNode();
			sibling.keys.addAll(keys.subList(fromIndex, toIndex));
			sibling.children.addAll(children.subList(fromIndex, toIndex + 1));

			keys.subList(fromIndex - 1, toIndex).clear();// keys[fromIndex-1] is gone, since it is less than the first
															// key in the sibling node
			children.subList(fromIndex, toIndex + 1).clear();

			return sibling;

		}

		Node spread(boolean isSparse) {
			NonLeafNode parent = new NonLeafNode();
			int limit;
			if (isSparse) {
				limit = order / 2 + 1;
			} else {
				limit = order + 1;
			}
			int i;
			NonLeafNode node = new NonLeafNode();
			node.keys.addAll(keys.subList(0, limit - 1));
			node.children.addAll(children.subList(0, limit));
			parent.children.add(node);
			for (i = limit; i + limit < children.size() - 1; i += limit) {
				node = new NonLeafNode();
				node.keys.addAll(keys.subList(i, i + limit - 1));
				node.children.addAll(children.subList(i, i + limit));
				parent.keys.add(node.getFirstLeafKey());
				parent.children.add(node);
			}
			if (i < children.size()) {// what if i == children.size() - 1
				node.keys.addAll(keys.subList(i - 1, getKeySize()));
				node.children.addAll(children.subList(i, getKeySize() + 1));
			}

			if (node.isOverflow()) {
				Node sibling = node.split();
				parent.insertChild(sibling.getFirstLeafKey(), sibling);
			}

			return parent;
		}

		@Override
		boolean isOverflow() {
			return children.size() > order + 1;
		}

		@Override
		boolean isUnderflow() {
			return children.size() < order / 2 + 1; // roof of (n + 1) / 2
			// even order e.g. 24, roof of (n + 1) / 2 = 13,
			// split when children < 13 = 12 + 1
			// odd order e.g. 13, roof of (n + 1) / 2 = 7,
			// split when children < 7 = 6 + 1
		}

		int getIndex(Integer key) {
			int loc = Collections.binarySearch(keys, key);
			return loc;
		}

		Node getChild(Integer key) {
			int loc = Collections.binarySearch(keys, key);
			int childIndex = loc >= 0 ? loc + 1 : -loc - 1;//
			return children.get(childIndex);
		}

		void deleteChild(Integer key) {
			int loc = Collections.binarySearch(keys, key);
			if (loc >= 0) {
				keys.remove(loc);
				children.remove(loc + 1);
			}
		}

		void insertChild(Integer key, Node child) {
			int loc = Collections.binarySearch(keys, key);
			int childIndex = loc >= 0 ? loc + 1 : -loc - 1;
			if (loc >= 0) {
				children.set(childIndex, child);
			} else {
				keys.add(childIndex, key);
				children.add(childIndex + 1, child);
			}
		}

		Node getChildLeftSibling(Integer key) {
			int loc = Collections.binarySearch(keys, key);
			int childIndex = loc >= 0 ? loc + 1 : -loc - 1;
			if (childIndex > 0) {
				return children.get(childIndex - 1);
			}
			return null;
		}

		Node getChildRightSibling(Integer key) {
			int loc = Collections.binarySearch(keys, key);
			int childIndex = loc >= 0 ? loc + 1 : -loc - 1;
			if (childIndex < getKeySize()) {
				return children.get(childIndex + 1);
			}
			return null;
		}

	}

	private class LeafNode extends Node {
		LeafNode next;

		LeafNode() {
			this.keys = new ArrayList<Integer>();
		}

		@Override
		boolean search(Integer key) {
			int loc = Collections.binarySearch(keys, key);
			return loc >= 0 ? true : false;
		}

		@Override
		boolean delete(Integer key) {
			int loc = Collections.binarySearch(keys, key);
			if (loc >= 0) {
				keys.remove(loc);
			} else {
				return false;
			}
			return true;
		}

		@Override
		boolean insert(Integer key) {
			int loc = Collections.binarySearch(keys, key);
			int valueIndex = loc >= 0 ? loc : -loc - 1;
			if (loc < 0) {
				keys.add(valueIndex, key);
			} else {
				return false;
			}
			if (root.isOverflow()) {
				Node sibling = split();
				NonLeafNode newRoot = new NonLeafNode();
				newRoot.keys.add(sibling.getFirstLeafKey());
				newRoot.children.add(this);
				newRoot.children.add(sibling);
				root = newRoot;
			}
			return true;
		}

		@Override
		Integer getFirstLeafKey() {
			return keys.get(0);
		}

		@Override
		List<Integer> searchRange(Integer key1, Integer key2) {
			List<Integer> result = new ArrayList<>();
			LeafNode node = this;
			while (node != null) {
				Iterator<Integer> iter = node.keys.iterator();
				while (iter.hasNext()) {
					Integer key = iter.next();
					int cmp1 = key.compareTo(key1);
					int cmp2 = key.compareTo(key2);
					if (cmp1 >= 0 && cmp2 <= 0) {
						result.add(key);
					} else if (cmp2 > 0) {
						return result;
					}
				}
				node = node.next;
			}
			return result;
		}

		@Override
		void coalesce(Node sibling) {
			// System.out.println("Leaf coalesce");
			LeafNode node = (LeafNode) sibling;
			keys.addAll(node.keys);
			next = node.next;
		}

		@Override
		Node split() {
			int fromIndex = getKeySize() / 2, toIndex = getKeySize();
			LeafNode sibling = new LeafNode();

			sibling.keys.addAll(keys.subList(fromIndex, toIndex));
			keys.subList(fromIndex, toIndex).clear();

			sibling.next = next;
			next = sibling;
			return sibling;

		}

		Node spread(boolean isSparse) {
			NonLeafNode parent = new NonLeafNode();
			int limit;
			if (isSparse) {
				limit = (order + 1) / 2;
			} else {
				limit = order;
			}
			int i;
			LeafNode last;
			LeafNode node;
			node = new LeafNode();
			node.keys.addAll(keys.subList(0, limit));
			last = node;
			parent.children.add(node);
			for (i = limit; i + limit < keys.size() - 1; i += limit) {
				node = new LeafNode();
				node.keys.addAll(keys.subList(i, i + limit));
				last.next = node;
				last = node;
				parent.keys.add(node.getFirstLeafKey());
				parent.children.add(node);
			}
			if (i < keys.size()) {// what if i == children.size() - 1
				node.keys.addAll(keys.subList(i, getKeySize()));
			}

			if (node.isOverflow()) {
				Node sibling = node.split();
				parent.keys.add(sibling.getFirstLeafKey());
				parent.children.add(sibling);
			}

			return parent;
		}

		@Override
		boolean isOverflow() {
			return keys.size() > order;
		}

		@Override
		boolean isUnderflow() {
			return keys.size() < (order + 1) / 2;
		}

	}

}
