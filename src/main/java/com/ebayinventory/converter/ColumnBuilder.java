package com.ebayinventory.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ebayinventory.model.VariationKey;
import com.ebayinventory.model.VariationValue;

@Component
public class ColumnBuilder {

	private final ColumnSorter columnSorter;

	@Autowired
	public ColumnBuilder(ColumnSorter columnSorter) {
		this.columnSorter = columnSorter;
	}

	public Map<Integer, Map<VariationKey, VariationValue>> buildColumns(Map<VariationKey, Set<VariationValue>> varSpecMap) {
		Set<Map<VariationKey, VariationValue>> maps = buildColumns1(varSpecMap);
		Map<Integer, Map<VariationKey, VariationValue>> map = new HashMap<>();
		int index = 0;
		for (Map<VariationKey, VariationValue> oneCombinationMap : sort(maps, varSpecMap)) {
			map.put(index, oneCombinationMap);
			index++;
		}
		return map;
	}

	private List<Map<VariationKey, VariationValue>> sort(Set<Map<VariationKey, VariationValue>> columns,
			Map<VariationKey, Set<VariationValue>> varSpecMap) {

		// order keys: key1, key2, key2..
		// while comparing two columns we will start comparison from key1 and only if key11!=key12 then we will compare key21 to key22?
		// we use random order of keys
		final List<VariationKey> keysOrder = new ArrayList<VariationKey>(varSpecMap.keySet());

		return columnSorter.sortToList(columns, keysOrder);
	}

	protected Set<Map<VariationKey, VariationValue>> buildColumns1(Map<VariationKey, Set<VariationValue>> varSpecMap) {
		assertNoMoreThan5(varSpecMap);
		List<Pair<VariationKey, List<VariationValue>>> list = convert(varSpecMap);
		Pair<VariationKey, List<VariationValue>> pair1 = list.get(0);
		Pair<VariationKey, List<VariationValue>> pair2 = list.size() > 1 ? list.get(1) : null;
		Pair<VariationKey, List<VariationValue>> pair3 = list.size() > 2 ? list.get(2) : null;
		Pair<VariationKey, List<VariationValue>> pair4 = list.size() > 3 ? list.get(3) : null;
		Pair<VariationKey, List<VariationValue>> pair5 = list.size() > 4 ? list.get(4) : null;
		Set<Map<VariationKey, VariationValue>> maps = new HashSet<>();

		for (VariationValue feature1Value : pair1.getValue()) {
			if (pair2 == null) {
				Map<VariationKey, VariationValue> map1 = new HashMap<>();
				map1.put(pair1.getKey(), feature1Value);
				maps.add(map1);
			} else if (pair3 == null) {
				for (VariationValue feature2Value : pair2.getValue()) {
					Map<VariationKey, VariationValue> map1 = new HashMap<>();
					map1.put(pair1.getKey(), feature1Value);
					map1.put(pair2.getKey(), feature2Value);
					maps.add(map1);
				}
			} else if (pair4 == null) {
				for (VariationValue feature2Value : pair2.getValue()) {
					for (VariationValue feature3Value : pair3.getValue()) {
						Map<VariationKey, VariationValue> map1 = new HashMap<>();
						map1.put(pair1.getKey(), feature1Value);
						map1.put(pair2.getKey(), feature2Value);
						map1.put(pair3.getKey(), feature3Value);
						maps.add(map1);
					}
				}
			} else if (pair5 == null) {
				for (VariationValue feature2Value : pair2.getValue()) {
					for (VariationValue feature3Value : pair3.getValue()) {
						for (VariationValue feature4Value : pair4.getValue()) {
							Map<VariationKey, VariationValue> map1 = new HashMap<>();
							map1.put(pair1.getKey(), feature1Value);
							map1.put(pair2.getKey(), feature2Value);
							map1.put(pair3.getKey(), feature3Value);
							map1.put(pair4.getKey(), feature4Value);
							maps.add(map1);
						}
					}
				}
			} else {
				for (VariationValue feature2Value : pair2.getValue()) {
					for (VariationValue feature3Value : pair3.getValue()) {
						for (VariationValue feature4Value : pair4.getValue()) {
							for (VariationValue feature5Value : pair5.getValue()) {
								Map<VariationKey, VariationValue> map1 = new HashMap<>();
								map1.put(pair1.getKey(), feature1Value);
								map1.put(pair2.getKey(), feature2Value);
								map1.put(pair3.getKey(), feature3Value);
								map1.put(pair4.getKey(), feature4Value);
								map1.put(pair5.getKey(), feature5Value);
								maps.add(map1);
							}
						}
					}
				}
			}
		}
		return maps;
	}

	private void assertNoMoreThan5(Map<VariationKey, Set<VariationValue>> varSpecMap) {
		if (varSpecMap.size() > 5) {
			throw new RuntimeException("only up to 5 variation keys are supported but was " + varSpecMap.size());
		}
	}

	private List<Pair<VariationKey, List<VariationValue>>> convert(Map<VariationKey, Set<VariationValue>> varSpecMap) {
		List<Pair<VariationKey, List<VariationValue>>> list = new ArrayList<>();
		for (Entry<VariationKey, Set<VariationValue>> entry : varSpecMap.entrySet()) {
			list.add(new ImmutablePair<VariationKey, List<VariationValue>>(entry.getKey(), new ArrayList<>(entry.getValue())));
		}
		return list;
	}

}
