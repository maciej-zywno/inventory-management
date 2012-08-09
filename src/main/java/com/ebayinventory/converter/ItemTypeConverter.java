package com.ebayinventory.converter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ebay.soap.eBLBaseComponents.AmountType;
import com.ebay.soap.eBLBaseComponents.ItemType;
import com.ebay.soap.eBLBaseComponents.NameValueListArrayType;
import com.ebay.soap.eBLBaseComponents.NameValueListType;
import com.ebay.soap.eBLBaseComponents.VariationType;
import com.ebay.soap.eBLBaseComponents.VariationsType;
import com.ebayinventory.model.Currency;
import com.ebayinventory.model.ItemId;
import com.ebayinventory.model.ItemRow;
import com.ebayinventory.model.Price;
import com.ebayinventory.model.VariationKey;
import com.ebayinventory.model.VariationValue;

@Component
public class ItemTypeConverter {

	private final Logger log = Logger.getLogger(ItemTypeConverter.class);

	private final int defaultItemPriceIfBuyItNowPriceNull = -1;
	private final Currency defaultItemCurrencyIfBuyItNowPriceNull = Currency.XXX;

	public List<ItemRow> toItemRows(List<ItemType> itemTypes) {
		List<ItemRow> items = new ArrayList<>();
		for (ItemType itemType : itemTypes) {
			String itemId = itemType.getItemID();
			String title = itemType.getTitle();
			boolean noVariations = noVariations(itemType.getVariations());
			Price price = new Price(new BigDecimal(zeroIfNull(itemType.getBuyItNowPrice())), null);
			if (noVariations) {
				log.info(itemType.getTitle());
				ItemRow itemRow = new ItemRow(new ItemId(Long.parseLong(itemId)), title, price, itemType.getQuantity());
				items.add(itemRow);
			} else {
				log.info(asString(itemType));
				Map<Map<VariationKey, VariationValue>, Integer> quantityPerVariations = getVariationsToQuantity(itemType);
				ItemRow itemRow = new ItemRow(new ItemId(Long.parseLong(itemId)), title, price, quantityPerVariations);
				items.add(itemRow);
			}
		}
		return items;
	}

	public Pair<Map<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>, List<ItemRow>> buildVarSpecMapToItemRows(List<ItemType> itemTypes) {
		Map<Map<VariationKey, Set<VariationValue>>, List<ItemRow>> varSpecMapToItemRows = new HashMap<>();
		List<ItemRow> itemsWithNoVariations = new ArrayList<>();
		for (ItemType itemType : itemTypes) {
			String itemId = itemType.getItemID();
			String title = itemType.getTitle();
			boolean noVariations = noVariations(itemType.getVariations());
			AmountType buyItNowPrice = itemType.getBuyItNowPrice();
			Price price = toPrice(buyItNowPrice);
			if (noVariations) {
				log.info(itemType.getTitle());
				ItemRow itemRow = new ItemRow(new ItemId(Long.parseLong(itemId)), title, price, itemType.getQuantity());
				itemsWithNoVariations.add(itemRow);
			} else {
				log.info(asString(itemType));
				Map<Map<VariationKey, VariationValue>, Integer> quantityPerVariations = getVariationsToQuantity(itemType);
				ItemRow itemRow = new ItemRow(new ItemId(Long.parseLong(itemId)), title, price, quantityPerVariations);
				Map<VariationKey, Set<VariationValue>> varSpecMap = toTypedMap(toStringMap(itemType.getVariations().getVariation()));
				getOrCreateAndGet(varSpecMapToItemRows, varSpecMap).add(itemRow);
			}
		}
		return new ImmutablePair<Map<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>, List<ItemRow>>(varSpecMapToItemRows,
				itemsWithNoVariations);
	}

	private Price toPrice(AmountType buyItNowPrice) {
		return new Price(new BigDecimal(zeroIfNull(buyItNowPrice)), eurIfNull(buyItNowPrice));
	}

	private double zeroIfNull(AmountType buyItNowPrice) {
		return buyItNowPrice == null ? defaultItemPriceIfBuyItNowPriceNull : buyItNowPrice.getValue();
	}

	private Currency eurIfNull(AmountType buyItNowPrice) {
		return buyItNowPrice == null ? defaultItemCurrencyIfBuyItNowPriceNull : Currency.fromEbay(buyItNowPrice.getCurrencyID());
	}

	private Map<VariationKey, Set<VariationValue>> toTypedMap(Map<String, Set<String>> stringMap) {
		Map<VariationKey, Set<VariationValue>> map = new HashMap<>();
		for (Entry<String, Set<String>> entry : stringMap.entrySet()) {
			map.put(new VariationKey(entry.getKey()), toTypedValues(entry.getValue()));
		}
		return map;
	}

	private Set<VariationValue> toTypedValues(Set<String> strings) {
		Set<VariationValue> values = new HashSet<>();
		for (String string : strings) {
			values.add(new VariationValue(string));
		}
		return values;
	}

	private Map<String, Set<String>> toStringMap(VariationType[] variationTypes) {
		Map<String, Set<String>> map = new HashMap<>();
		for (VariationType variationType : variationTypes) {
			for (NameValueListType type : variationType.getVariationSpecifics().getNameValueList()) {
				String name = type.getName();
				if (!map.containsKey(name)) {
					map.put(name, new HashSet<String>());
				}
				map.get(name).add(type.getValue()[0]);
			}
		}
		return map;
	}

	private boolean noVariations(VariationsType variations) {
		return variations == null || variations.getVariation() == null || variations.getVariation().length == 0;
	}

	private String asString(ItemType itemType) {
		return "variations: " + itemType.getVariations() + ", variationSpecificsSet: " + itemType.getVariations().getVariationSpecificsSet();
	}

	private List<ItemRow> getOrCreateAndGet(Map<Map<VariationKey, Set<VariationValue>>, List<ItemRow>> varSpecMapToItemRows,
			Map<VariationKey, Set<VariationValue>> varSpecMap) {
		if (!varSpecMapToItemRows.containsKey(varSpecMap)) {
			varSpecMapToItemRows.put(varSpecMap, new ArrayList<ItemRow>());
		}
		return varSpecMapToItemRows.get(varSpecMap);
	}

	private Map<Map<VariationKey, VariationValue>, Integer> getVariationsToQuantity(ItemType item) {
		Map<Map<VariationKey, VariationValue>, Integer> variations = new HashMap<>();
		for (VariationType variationType : getVariationTypes(item.getVariations())) {
			// Integer unitsAvailable = variationType.getUnitsAvailable();
			// Integer quantitySold = variationType.getSellingStatus().getQuantitySold();
			Integer quantity = variationType.getQuantity();
			Integer quantitySold = variationType.getSellingStatus().getQuantitySold();
			int quantityAvailable = quantity - quantitySold;
			variations.put(asStringMap(variationType.getVariationSpecifics()), quantityAvailable);
		}
		return variations;
	}

	private VariationType[] getVariationTypes(VariationsType variations) {
		if (variations == null) {
			return new VariationType[0];
		} else {
			return variations.getVariation();
		}
	}

	public Map<VariationKey, VariationValue> asStringMap(NameValueListArrayType variationSpecifics) {
		Map<VariationKey, VariationValue> map = new HashMap<>();
		for (NameValueListType type : variationSpecifics.getNameValueList()) {
			map.put(new VariationKey(type.getName()), new VariationValue(type.getValue(0)));
		}
		return map;
	}

}
