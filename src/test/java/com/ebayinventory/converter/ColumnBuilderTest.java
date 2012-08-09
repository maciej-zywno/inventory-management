package com.ebayinventory.converter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.ebayinventory.model.VariationKey;
import com.ebayinventory.model.VariationValue;

public class ColumnBuilderTest extends TestCase {

	public void testBuildColumns() {
		// SETUP
		ColumnBuilder columnBuilder = new ColumnBuilder(new ColumnSorter());
		Map<VariationKey, Set<VariationValue>> varSpecMap = new HashMap<>();
		put(1, 3, varSpecMap);
		put(2, 2, varSpecMap);
		put(3, 1, varSpecMap);

		Map<VariationKey, VariationValue> com1 = new HashMap<>();
		com1.put(key(1), value(1, 1));
		com1.put(key(2), value(2, 1));
		com1.put(key(3), value(3, 1));
		Map<VariationKey, VariationValue> com2 = new HashMap<>();
		com2.put(key(1), value(1, 1));
		com2.put(key(2), value(2, 2));
		com2.put(key(3), value(3, 1));
		Map<VariationKey, VariationValue> com3 = new HashMap<>();
		com3.put(key(1), value(1, 2));
		com3.put(key(2), value(2, 1));
		com3.put(key(3), value(3, 1));
		Map<VariationKey, VariationValue> com4 = new HashMap<>();
		com4.put(key(1), value(1, 2));
		com4.put(key(2), value(2, 2));
		com4.put(key(3), value(3, 1));
		Map<VariationKey, VariationValue> com5 = new HashMap<>();
		com5.put(key(1), value(1, 3));
		com5.put(key(2), value(2, 1));
		com5.put(key(3), value(3, 1));
		Map<VariationKey, VariationValue> com6 = new HashMap<>();
		com6.put(key(1), value(1, 3));
		com6.put(key(2), value(2, 2));
		com6.put(key(3), value(3, 1));

		// EXECUTE
		Set<Map<VariationKey, VariationValue>> columns = columnBuilder.buildColumns1(varSpecMap);

		// VERIFY
		print(columns);
		assertEquals(6, columns.size());
		assertTrue(columns.contains(com1));
		assertTrue(columns.contains(com2));
		assertTrue(columns.contains(com3));
		assertTrue(columns.contains(com4));
		assertTrue(columns.contains(com5));
		assertTrue(columns.contains(com6));
	}

	private void print(Set<Map<VariationKey, VariationValue>> columns) {
		ToStringBuilder.setDefaultStyle(ToStringStyle.SIMPLE_STYLE);
		for (Map<VariationKey, VariationValue> map : columns) {
			Set<Entry<VariationKey, VariationValue>> entrySet = map.entrySet();
			for (Entry<VariationKey, VariationValue> entry : entrySet) {
				System.out.print(entry.getKey() + "=" + entry.getValue() + ",");
			}
			System.out.println();
		}
	}

	private void put(int keyIndex, int valuesCount, Map<VariationKey, Set<VariationValue>> varSpecMap) {
		VariationKey key = key(keyIndex);
		Set<VariationValue> values = new HashSet<>();
		for (int i = 1; i <= valuesCount; i++) {
			VariationValue value = value(keyIndex, i);
			values.add(value);
		}
		varSpecMap.put(key, values);
	}

	private VariationValue value(int keyIndex, int i) {
		return new VariationValue("value" + keyIndex + i);
	}

	private VariationKey key(int keyIndex) {
		return new VariationKey("key" + keyIndex);
	}

}
