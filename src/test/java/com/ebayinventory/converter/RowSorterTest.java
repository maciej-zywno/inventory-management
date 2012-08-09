package com.ebayinventory.converter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Assert;
import org.junit.Test;

import com.ebayinventory.model.Currency;
import com.ebayinventory.model.ItemId;
import com.ebayinventory.model.ItemRow;
import com.ebayinventory.model.Price;
import com.ebayinventory.model.VariationKey;
import com.ebayinventory.model.VariationValue;

public class RowSorterTest {

	@Test
	public void testSortSetOfEntryOfMapOfVariationKeySetOfVariationValueListOfItemRow() {
		// SETUP
		RowSorter sorter = new RowSorter();
		Set<Entry<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>> entrySet = new HashSet<>();

		Entry<Map<VariationKey, Set<VariationValue>>, List<ItemRow>> entry1 = new ImmutablePair<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>(
				map("key1", "value1"), Arrays.asList(new ItemRow[] { item(13), item(11), item(12) }));
		Entry<Map<VariationKey, Set<VariationValue>>, List<ItemRow>> entry2 = new ImmutablePair<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>(
				map("key2", "value2"), Arrays.asList(new ItemRow[] { item(21), item(23), item(22) }));
		Entry<Map<VariationKey, Set<VariationValue>>, List<ItemRow>> entry3 = new ImmutablePair<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>(
				map("key3", "value3"), Arrays.asList(new ItemRow[] { item(33), item(32), item(31) }));
		entrySet.add(entry3);
		entrySet.add(entry1);
		entrySet.add(entry2);

		// EXECUTE
		List<Entry<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>> sorted = sorter.sort(entrySet);

		// VERIFY
		int price11 = sorted.get(0).getValue().get(0).getPrice().getAmount().intValue();
		int price12 = sorted.get(0).getValue().get(1).getPrice().getAmount().intValue();
		int price13 = sorted.get(0).getValue().get(2).getPrice().getAmount().intValue();
		int price21 = sorted.get(1).getValue().get(0).getPrice().getAmount().intValue();
		int price22 = sorted.get(1).getValue().get(1).getPrice().getAmount().intValue();
		int price23 = sorted.get(1).getValue().get(2).getPrice().getAmount().intValue();
		int price31 = sorted.get(2).getValue().get(0).getPrice().getAmount().intValue();
		int price32 = sorted.get(2).getValue().get(1).getPrice().getAmount().intValue();
		int price33 = sorted.get(2).getValue().get(2).getPrice().getAmount().intValue();
		Assert.assertEquals(11, price11);
		Assert.assertEquals(12, price12);
		Assert.assertEquals(13, price13);
		Assert.assertEquals(21, price21);
		Assert.assertEquals(22, price22);
		Assert.assertEquals(23, price23);
		Assert.assertEquals(31, price31);
		Assert.assertEquals(32, price32);
		Assert.assertEquals(33, price33);
	}

	private ItemRow item(int price) {
		long unique = System.nanoTime();
		return new ItemRow(new ItemId(unique), unique + "", new Price(new BigDecimal(price), Currency.EUR), 1);
	}

	private Map<VariationKey, Set<VariationValue>> map(String string, String string2) {
		// TODO Auto-generated method stub
		return null;
	}
}
