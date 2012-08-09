package com.ebayinventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import com.ebayinventory.model.ItemId;
import com.ebayinventory.model.ItemRow;
import com.ebayinventory.model.VariationKey;
import com.ebayinventory.model.VariationValue;

@Component
public class ItemRowCalculator {

	public List<ItemRow> findDelta(Pair<Map<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>, List<ItemRow>> googleItemRows,
			Pair<Map<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>, List<ItemRow>> localItemRows) {
		Map<ItemId, ItemRow> googleItemsIndexed = indexByItemId(googleItemRows);
		Map<ItemId, ItemRow> localItemsIndexed = indexByItemId(localItemRows);
		Assert.assertEquals(googleItemsIndexed.keySet(), localItemsIndexed.keySet());
		List<ItemRow> delta = new ArrayList<>();
		// compare
		for (Entry<ItemId, ItemRow> entry : googleItemsIndexed.entrySet()) {
			ItemRow googleItem = entry.getValue();
			ItemRow localItem = localItemsIndexed.get(entry.getKey());
			if (hasVariationsAndIsDifferentQuantity(googleItem, localItem) || hasNoVariationsAndHasDifferentTotalQuantity(googleItem, localItem)) {
				delta.add(googleItem);
			}
		}
		return delta;
	}

	private boolean hasNoVariationsAndHasDifferentTotalQuantity(ItemRow googleItem, ItemRow localItem) {
		return googleItem.getQuantityPerVariations() == null && !googleItem.getTotalQuantity().equals(localItem.getTotalQuantity());
	}

	private boolean hasVariationsAndIsDifferentQuantity(ItemRow googleItem, ItemRow localItem) {
		return googleItem.getQuantityPerVariations() != null && !googleItem.getQuantityPerVariations().equals(localItem.getQuantityPerVariations());
	}

	private Map<ItemId, ItemRow> indexByItemId(Pair<Map<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>, List<ItemRow>> pair) {
		Map<ItemId, ItemRow> index = new HashMap<>();

		// 1
		for (Entry<Map<VariationKey, Set<VariationValue>>, List<ItemRow>> entry : pair.getLeft().entrySet()) {
			for (ItemRow itemRow : entry.getValue()) {
				index.put(itemRow.getItemId(), itemRow);
			}
		}
		// 2
		for (ItemRow itemRow : pair.getRight()) {
			index.put(itemRow.getItemId(), itemRow);
		}

		return index;
	}

}
