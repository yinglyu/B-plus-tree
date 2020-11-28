package com.yinglyu.csce608;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BPlusTreeTest {

	public List<Integer> generateData(int start, int end, int scale) {
		List<Integer> range = IntStream.rangeClosed(start, end).boxed().collect(Collectors.toList());
		Collections.shuffle(range);
		return range.subList(0, scale);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BPlusTreeTest test = new BPlusTreeTest();
		List<Integer> records = test.generateData(100000, 200000, 20000);
		System.out.println(records);

		// BPlusTree<Integer> bpt13 = new BPlusTree<>(5);
		// bpt13.insert(records);
		//
		// System.out.println(bpt13);
		boolean isSparse = false;
		BPlusTree<Integer> bptDense13 = new BPlusTree<>(13, records.subList(0, 10000), isSparse);
		// System.out.println(bptDense13);
		//
		//
		//
		// isSparse = true;
		// BPlusTree<Integer> bptSparse13 = new BPlusTree<>(5, records, isSparse);
		// System.out.println(bptSparse13);
		// System.out.println(bptSparse13.searchRange(100000, 150000));
		Integer ircd = records.get(10001);
		System.out.println(ircd);
		bptDense13.insert(ircd);
		for (Integer record : records.subList(0, 30)) {
			System.out.println(record);
			bptDense13.delete(record);
			// System.out.println(bpt13);
		}

	}

}
