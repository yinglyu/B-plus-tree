package com.yinglyu.csce608;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BPlusTreeTest {

	private List<Integer> range;
	private int scale;

	public List<Integer> forBuild() {
		return range.subList(0, scale);
	}

	public int forInsert(int i) {
		return range.get(scale + i);
	}

	public int forDelete(int i) {
		return range.get(i);
	}

	public int forSearch(int i) {
		return range.get(scale - i - 1);
	}

	public BPlusTreeTest(int start, int end, int scale) {
		this.range = IntStream.rangeClosed(start, end).boxed().collect(Collectors.toList());
		Collections.shuffle(range);
		this.scale = scale;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		int scale = 10000;
		BPlusTreeTest test = new BPlusTreeTest(100000, 200000, scale);
		System.out.println("Generate a collection of 10,000 records");
		// System.out.println(test.forBuild());

		boolean isSparse = false;
		System.out.println("Build Dense B+ Tree Order 13");
		BPlusTree<Integer> bptDense13 = new BPlusTree<>(13, test.forBuild(), isSparse);
		// System.out.println(bptDense13);
		System.out.println("Build Dense B+ Tree Order 24");
		BPlusTree<Integer> bptDense24 = new BPlusTree<>(24, test.forBuild(), isSparse);
		// System.out.println(bptDense24);
		isSparse = true;
		System.out.println("Build Sparse B+ Tree Order 13");
		BPlusTree<Integer> bptSparse13 = new BPlusTree<>(13, test.forBuild(), isSparse);
		// System.out.println(bptSparse13);
		System.out.println("Build Sparse B+ Tree Order 24");
		BPlusTree<Integer> bptSparse24 = new BPlusTree<>(24, test.forBuild(), isSparse);
		// System.out.println(bptSparse24);

		for (int i = 0; i < 2; i++) {
			System.out.println("Dense B+ Tree Order 13 insert " + test.forInsert(i));
			bptDense13.insert(test.forInsert(i));
			System.out.println("Dense B+ Tree Order 24 insert " + test.forInsert(i));
			bptDense24.insert(test.forInsert(i));
		}

		for (int i = 0; i < 2; i++) {
			System.out.println("Sparse B+ Tree Order 13 delete " + test.forDelete(i));
			bptSparse13.delete(test.forDelete(i));
			System.out.println("Sparse B+ Tree Order 24 delete " + test.forDelete(i));
			bptSparse24.delete(test.forDelete(i));
		}

		for (int i = 10; i < 13; i++) {
			System.out.println("Dense B+ Tree Order 13 insert " + test.forInsert(i));
			bptDense13.insert(test.forInsert(i));
			System.out.println("Dense B+ Tree Order 24 insert " + test.forInsert(i));
			bptDense24.insert(test.forInsert(i));
			System.out.println("Sparse B+ Tree Order 13 insert " + test.forInsert(i));
			bptSparse13.insert(test.forInsert(i));
			System.out.println("Sparse B+ Tree Order 24 insert " + test.forInsert(i));
			bptSparse24.insert(test.forInsert(i));
		}

		for (int i = 10; i < 13; i++) {
			System.out.println("Dense B+ Tree Order 13 delete " + test.forDelete(i));
			bptDense13.delete(test.forDelete(i));
			System.out.println("Dense B+ Tree Order 24 delete " + test.forDelete(i));
			bptDense24.delete(test.forDelete(i));
			System.out.println("Sparse B+ Tree Order 13 delete " + test.forDelete(i));
			bptSparse13.delete(test.forDelete(i));
			System.out.println("Sparse B+ Tree Order 24 delete " + test.forDelete(i));
			bptSparse24.delete(test.forDelete(i));
		}

		for (int i = 0; i < 5; i++) {
			System.out.println("Dense B+ Tree Order 13 search " + test.forSearch(i));
			System.out.println(bptDense13.search(test.forSearch(i)));
			System.out.println("Dense B+ Tree Order 24 search " + test.forSearch(i));
			System.out.println(bptDense24.search(test.forSearch(i)));
			System.out.println("Sparse B+ Tree Order 13 search " + test.forSearch(i));
			System.out.println(bptSparse13.search(test.forSearch(i)));
			System.out.println("Sparse B+ Tree Order 24 search " + test.forSearch(i));
			System.out.println(bptSparse24.search(test.forSearch(i)));
		}

		System.out.println("Dense B+ Tree Order 13 range search (100000, 100500)");
		System.out.println(bptDense13.searchRange(100000, 100500));
		System.out.println("Dense B+ Tree Order 24 range search (100000, 100500)");
		System.out.println(bptDense24.searchRange(100000, 100500));
		System.out.println("Sparse B+ Tree Order 13 range search (100000, 100500)");
		System.out.println(bptSparse13.searchRange(100000, 100500));
		System.out.println("Sparse B+ Tree Order 24 range search (100000, 100500)");
		System.out.println(bptSparse24.searchRange(100000, 100500));

	}

}
