package com.ebayinventory.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.ebayinventory.model.VariationKey;
import com.ebayinventory.model.VariationValue;

@Component
public class ColumnSorter {

	private static class VariationColumnComparator implements Comparator<Map<VariationKey, VariationValue>> {

		private final List<VariationKey> keysOrder;

		public VariationColumnComparator(List<VariationKey> keysOrder) {
			this.keysOrder = keysOrder;
		}

		@Override
		public int compare(Map<VariationKey, VariationValue> o1, Map<VariationKey, VariationValue> o2) {
			// iterate through all keys until variation value is different
			for (VariationKey key : keysOrder) {
				int comparisonResult = Integer.MIN_VALUE;
				if ((comparisonResult = o1.get(key).compareTo(o2.get(key))) != 0) {
					return comparisonResult;
				}
			}
			throw new RuntimeException("two columns are equal according to comparator, map1=" + o1 + ", map2=" + o2);
		}
	}

	public List<Map<VariationKey, VariationValue>> sortToList(Set<Map<VariationKey, VariationValue>> columns, final List<VariationKey> keysOrder) {
		Comparator<Map<VariationKey, VariationValue>> comparator = new VariationColumnComparator(keysOrder);
		List<Map<VariationKey, VariationValue>> list = new ArrayList<Map<VariationKey, VariationValue>>(columns);
		Collections.sort(list, comparator);
		return list;
	}
}
