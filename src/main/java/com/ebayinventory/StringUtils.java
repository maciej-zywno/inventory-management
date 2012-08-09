package com.ebayinventory;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.ebayinventory.model.VariationKey;
import com.ebayinventory.model.VariationValue;

public class StringUtils {

	public static String asString(Map<VariationKey, Set<VariationValue>> map) {
		String string = "";
		for (Entry<VariationKey, Set<VariationValue>> entry : map.entrySet()) {
			string += entry.getKey().getVariationKey() + "=[" + StringUtils.variationValues(entry.getValue()) + "]";
		}
		return string;
	}

	public static String variationValues(Set<VariationValue> values) {
		String string = "";
		for (VariationValue variationValue : values) {
			string += variationValue.getVariationValue() + ",";
		}
		return string;
	}

	public static String variationKeyToVariationValue(Map<VariationKey, VariationValue> map) {
		String string = "";
		for (Entry<VariationKey, VariationValue> entry : map.entrySet()) {
			string += entry.getKey().getVariationKey() + "=" + entry.getValue() + ",";
		}
		return string.substring(0, string.length() - 1);
	}

	public static String qtyPerVariations(Map<Map<VariationKey, VariationValue>, Integer> quantityPerVariations) {
		String string = "";
		for (Entry<Map<VariationKey, VariationValue>, Integer> entry : quantityPerVariations.entrySet()) {
			string += "[" + variationKeyToVariationValue(entry.getKey()) + "]=" + entry.getValue() + ",";
		}
		return string.substring(0, string.length() - 1);
	}

	public static boolean isNullOrEmpty(String string) {
		return string == null || string.isEmpty();
	}

	public static String[][] toArrays(List<List<String>> lists) {
		String[][] arrays = new String[lists.size()][];
		for (int i = 0; i < lists.size(); i++) {
			arrays[i] = lists.get(i).toArray(new String[lists.get(i).size()]);
		}
		return arrays;
	}

}
