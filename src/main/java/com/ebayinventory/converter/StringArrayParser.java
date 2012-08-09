package com.ebayinventory.converter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.ebayinventory.model.VariationKey;

@Component
public class StringArrayParser {

	public List<VariationKey> parseVariationKeys(String variationKeysCell) {
		List<VariationKey> list = new ArrayList<>();
		for (String variationKey : variationKeysCell.split(",")) {
			list.add(new VariationKey(variationKey));
		}
		return list;
	}

}
