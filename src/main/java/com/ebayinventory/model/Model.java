package com.ebayinventory.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

public class Model {

	private final Pair<Map<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>, List<ItemRow>> itemRows;
	private final Map<Pair<ItemId, Map<VariationKey, VariationValue>>, CellPosition> spreadsheetMatrix;

	public Model(Pair<Map<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>, List<ItemRow>> itemRows,
			Map<Pair<ItemId, Map<VariationKey, VariationValue>>, CellPosition> spreadsheetMatrix) {
		this.itemRows = itemRows;
		this.spreadsheetMatrix = spreadsheetMatrix;
	}

	public Pair<Map<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>, List<ItemRow>> getItemRows() {
		return itemRows;
	}

	public Map<Pair<ItemId, Map<VariationKey, VariationValue>>, CellPosition> getSpreadsheetMatrix() {
		return spreadsheetMatrix;
	}

}
