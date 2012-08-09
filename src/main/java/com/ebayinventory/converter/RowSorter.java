package com.ebayinventory.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.ebayinventory.model.ItemRow;
import com.ebayinventory.model.VariationKey;
import com.ebayinventory.model.VariationValue;

@Component
public class RowSorter {

	private final class GroupedByVariationsItemRowsByPriceComparator implements
			Comparator<Map.Entry<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>> {
		@Override
		public int compare(Entry<Map<VariationKey, Set<VariationValue>>, List<ItemRow>> o1,
				Entry<Map<VariationKey, Set<VariationValue>>, List<ItemRow>> o2) {
			return o1.getValue().get(0).getPrice().compareTo(o2.getValue().get(0).getPrice());
		}
	}

	private final class ItemRowByPriceComparator implements Comparator<ItemRow> {
		@Override
		public int compare(ItemRow o1, ItemRow o2) {
			return o1.getPrice().compareTo(o2.getPrice());
		}
	}

	private final Comparator<Entry<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>> comparator = new GroupedByVariationsItemRowsByPriceComparator();
	private final Comparator<ItemRow> itemRowByPriceComparator = new ItemRowByPriceComparator();

	public List<Entry<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>> sort(
			Set<Entry<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>> entrySet) {
		// 1 sort each entry list
		for (Entry<Map<VariationKey, Set<VariationValue>>, List<ItemRow>> entry : entrySet) {
			Collections.sort(entry.getValue(), itemRowByPriceComparator);
		}
		// 2 only now sort entries by comparing first element price
		List<Entry<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>> entryList = new ArrayList<Entry<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>>(
				entrySet);
		Collections.sort(entryList, comparator);
		return entryList;
	}

	public List<ItemRow> sort(List<ItemRow> value) {
		Collections.sort(value, itemRowByPriceComparator);
		return value;
	}

}
