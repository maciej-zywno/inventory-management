package com.ebayinventory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class ListUtil {

	@SafeVarargs
	public final <T> List<T[]> mergeListsOfArrays(final List<T[]>... lists) {
		List<T[]> all = new ArrayList<>();
		for (List<T[]> list : lists) {
			all.addAll(list);
		}
		return all;
	}

	@SafeVarargs
	public final <T> List<T> mergeLists(List<T>... lists) {
		List<T> all = new ArrayList<>();
		for (List<T> list : lists) {
			all.addAll(list);
		}
		return all;
	}

	@SafeVarargs
	public final <T> Set<T> mergeSets(Set<T>... sets) {
		Set<T> all = new HashSet<>();
		for (Set<T> set : sets) {
			all.addAll(set);
		}
		return all;
	}
}
