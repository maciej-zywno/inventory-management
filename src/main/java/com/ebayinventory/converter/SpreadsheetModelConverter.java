package com.ebayinventory.converter;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

import com.ebayinventory.ListUtil;
import com.ebayinventory.StringUtils;
import com.ebayinventory.model.CellPosition;
import com.ebayinventory.model.Currency;
import com.ebayinventory.model.ItemId;
import com.ebayinventory.model.ItemRow;
import com.ebayinventory.model.Model;
import com.ebayinventory.model.Price;
import com.ebayinventory.model.VariationKey;
import com.ebayinventory.model.VariationValue;
import com.ebayinventory.util.NumberFormatter;

@Component
public class SpreadsheetModelConverter {

	private final int nonVariationColumnCount = 3;// id,title,price

	private final ColumnBuilder columnBuilder;
	private final ListUtil listUtil;
	private final RowSorter rowSorter;
	private final StringArrayParser stringArrayParser;
	private final NumberFormatter numberFormatter;

	@Autowired
	public SpreadsheetModelConverter(ColumnBuilder columnBuilder, ListUtil listUtil, RowSorter rowSorter, StringArrayParser stringArrayParser,
			NumberFormatter numberFormatter) {
		this.columnBuilder = columnBuilder;
		this.listUtil = listUtil;
		this.rowSorter = rowSorter;
		this.stringArrayParser = stringArrayParser;
		this.numberFormatter = numberFormatter;
	}

	/*public Pair<Map<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>, List<ItemRow>> buildVarSpecMapToItemRows(List<ItemRow> items) {
		Map<Map<VariationKey, Set<VariationValue>>, List<ItemRow>> varSpecMapToItemRows = new HashMap<>();
		List<ItemRow> itemsWithNoVariations = new ArrayList<>();
		for (ItemRow itemRow : items) {
			boolean noVariations = itemRow.getQuantityPerVariations() == null;
			if (noVariations) {
				itemsWithNoVariations.add(itemRow);
			} else {
				Map<Map<VariationKey, VariationValue>, Integer> quantityPerVariations = itemRow.getQuantityPerVariations();
				Map<VariationKey, Set<VariationValue>> varSpecMap = toTypedMap(quantityPerVariations.keySet());
				getOrCreateAndGet(varSpecMapToItemRows, varSpecMap).add(itemRow);
			}
		}
		return new ImmutablePair<Map<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>, List<ItemRow>>(varSpecMapToItemRows,
				itemsWithNoVariations);
	}*/

	protected List<String[]> buildLinesFromNoVarItems(List<ItemRow> itemsWithNoVariations) {
		List<String[]> lines = new ArrayList<>();
		for (ItemRow itemRow : itemsWithNoVariations) {
			lines.add(toStringArray(itemRow));
		}
		return lines;
	}

	protected List<String[]> buildLinesFromVarItems(List<Entry<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>> items,
			Map<Map<VariationKey, Set<VariationValue>>, Map<Integer, Map<VariationKey, VariationValue>>> columnIndexMap) {
		List<String[]> lines = new ArrayList<>();
		for (Entry<Map<VariationKey, Set<VariationValue>>, List<ItemRow>> entry : items) {
			Map<VariationKey, Set<VariationValue>> varSpecMap = entry.getKey();
			Map<Integer, Map<VariationKey, VariationValue>> columnIndexToFeatureToValue = columnIndexMap.get(varSpecMap);
			lines.add(asSpecLine(columnIndexToFeatureToValue));
			for (ItemRow itemRow : entry.getValue()) {
				Map<Map<VariationKey, VariationValue>, Integer> quantityPerVariations = itemRow.getQuantityPerVariations();
				Integer[] columnValues = new Integer[columnIndexToFeatureToValue.size()];
				for (Entry<Integer, Map<VariationKey, VariationValue>> columnIndexAndColumnSpec : columnIndexToFeatureToValue.entrySet()) {
					Map<VariationKey, VariationValue> value = columnIndexAndColumnSpec.getValue();
					boolean containsKey = quantityPerVariations.containsKey(value);
					Integer integer = quantityPerVariations.get(value);
					columnValues[columnIndexAndColumnSpec.getKey()] = containsKey ? integer : 0;
				}
				String[] itemLine = merge(itemRow.getItemId(), itemRow.getTitle(), itemRow.getPrice(), columnValues);
				lines.add(itemLine);
			}
		}
		return lines;
	}

	private Map<Map<VariationKey, Set<VariationValue>>, Map<Integer, Map<VariationKey, VariationValue>>> buildColumnIndexMap(
			List<Entry<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>> list) {
		Map<Map<VariationKey, Set<VariationValue>>, Map<Integer, Map<VariationKey, VariationValue>>> map = new HashMap<>();
		for (Entry<Map<VariationKey, Set<VariationValue>>, List<ItemRow>> entry : list) {
			Map<Integer, Map<VariationKey, VariationValue>> columnIndexToFeatureToValue = columnBuilder.buildColumns(entry.getKey());
			map.put(entry.getKey(), columnIndexToFeatureToValue);
		}
		return map;
	}

	private String[] toStringArray(ItemRow itemRow) {
		return new String[] { Long.toString(itemRow.getItemId().getItemId()), itemRow.getTitle(), toString(itemRow.getPrice()),
				Integer.toString(itemRow.getTotalQuantity()) };
	}

	private String toString(Price price) {
		return price.getCurrency() + " " + numberFormatter.formatTwoDecimal(price.getAmount());
	}

	private String[] asSpecLine(Map<Integer, Map<VariationKey, VariationValue>> columnIndexToFeatureToValue) {
		String[] array = new String[nonVariationColumnCount + columnIndexToFeatureToValue.size()];
		array[0] = "";
		array[1] = "";
		array[2] = keysCommaDelimited(columnIndexToFeatureToValue.values().iterator().next().keySet());
		for (Entry<Integer, Map<VariationKey, VariationValue>> entry : columnIndexToFeatureToValue.entrySet()) {
			array[nonVariationColumnCount + entry.getKey()] = valuesCommaDelimited(entry.getValue().values());
		}
		return array;
	}

	private String keysCommaDelimited(Collection<VariationKey> variationKeys) {
		String text = "";
		for (VariationKey variationValue : variationKeys) {
			text += variationValue.getVariationKey() + ",";
		}
		return text.isEmpty() ? text : text.substring(0, text.length() - 1);
	}

	private String valuesCommaDelimited(Collection<VariationValue> variationValues) {
		String text = "";
		for (VariationValue variationValue : variationValues) {
			text += variationValue.getVariationValue() + ",";
		}
		return text.isEmpty() ? text : text.substring(0, text.length() - 1);
	}

	private String[] merge(ItemId itemId, String title, Price price, Integer[] columnValues) {
		String[] array = new String[nonVariationColumnCount + columnValues.length];
		array[0] = Long.toString(itemId.getItemId());
		array[1] = title;
		array[2] = toString(price);
		for (int i = 0; i < columnValues.length; i++) {
			array[i + nonVariationColumnCount] = columnValues[i] + "";

		}
		return array;
	}

	public Model convert(String[][] array) {

		Map<Map<VariationKey, Set<VariationValue>>, List<ItemRow>> headerSpecToItemRows = new HashMap<>();
		List<ItemRow> list = new ArrayList<>();

		boolean stateClear = true;
		boolean expectVariationLineData = false;
		String[] variationHeaderRow = null;
		List<VariationKey> variationKeys = null;

		Map<Pair<ItemId, Map<VariationKey, VariationValue>>, CellPosition> spreadsheetMatrix = new HashMap<>();

		for (int i = 0; i < array.length; i++) {
			String[] line = array[i];
			if (StringArrayUtils.isEmpty(line)) {
				stateClear = true;
				expectVariationLineData = false;
			} else if (isVariationLineHeader(line)) {
				variationHeaderRow = line;
				expectVariationLineData = true;
				variationKeys = stringArrayParser.parseVariationKeys(line[nonVariationColumnCount - 1]);
				stateClear = false;
			} else if (expectVariationLineData) {
				Pair<Map<VariationKey, Set<VariationValue>>, List<Map<VariationKey, VariationValue>>> headerRow = parseVariationsHeader(
						variationHeaderRow, variationKeys);
				if (!headerSpecToItemRows.containsKey(headerRow.getLeft())) {
					headerSpecToItemRows.put(headerRow.getLeft(), new ArrayList<ItemRow>());
				}
				Pair<ItemRow, Map<Map<VariationKey, VariationValue>, Integer>> itemRowWithVariations = parseItemRowWithVariations(line,
						headerRow.getRight());

				for (Entry<Map<VariationKey, VariationValue>, Integer> entry : itemRowWithVariations.getRight().entrySet()) {
					spreadsheetMatrix.put(new ImmutablePair<ItemId, Map<VariationKey, VariationValue>>(itemRowWithVariations.getLeft().getItemId(),
							entry.getKey()), new CellPosition(i, entry.getValue()));
				}

				headerSpecToItemRows.get(headerRow.getLeft()).add(itemRowWithVariations.getLeft());
				expectVariationLineData = true;
			} else if (stateClear) {
				ItemRow itemRow = parseItemRow(line);

				spreadsheetMatrix.put(new ImmutablePair<ItemId, Map<VariationKey, VariationValue>>(itemRow.getItemId(), null), new CellPosition(i,
						nonVariationColumnCount));
				list.add(itemRow);

				expectVariationLineData = false;
			} else {
				throw new RuntimeException();
			}
		}
		Pair<Map<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>, List<ItemRow>> immutablePair = new ImmutablePair<Map<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>, List<ItemRow>>(
				headerSpecToItemRows, list);

		return new Model(immutablePair, spreadsheetMatrix);
	}

	private Pair<Map<VariationKey, Set<VariationValue>>, List<Map<VariationKey, VariationValue>>> parseVariationsHeader(String[] line,
			List<VariationKey> variationKeys) {

		Map<VariationKey, Set<VariationValue>> map = new HashMap<>();
		List<Map<VariationKey, VariationValue>> concreteSpecs = new ArrayList<>();

		for (int i = nonVariationColumnCount; i < line.length; i++) {
			String cell = line[i];
			if (StringUtils.isNullOrEmpty(cell)) {
				break;
			}
			String[] variationValues = cell.split(",");
			Map<VariationKey, VariationValue> concreteSpec = new HashMap<>();
			for (int j = 0; j < variationValues.length; j++) {
				VariationValue variationValue = new VariationValue(variationValues[j]);
				VariationKey variationKey = variationKeys.get(j);
				if (!map.containsKey(variationKey)) {
					map.put(variationKey, new HashSet<VariationValue>());
				}
				map.get(variationKey).add(variationValue);
				concreteSpec.put(variationKey, variationValue);
			}
			concreteSpecs.add(i - nonVariationColumnCount, concreteSpec);
		}
		return new ImmutablePair<Map<VariationKey, Set<VariationValue>>, List<Map<VariationKey, VariationValue>>>(map, concreteSpecs);
	}

	private ItemRow parseItemRow(String[] line) {
		ItemId itemId = new ItemId(Long.parseLong(line[0]));
		String title = line[1];
		Price price = parsePrice(line[2]);
		Integer totalQuantity = Integer.parseInt(line[3]);
		return new ItemRow(itemId, title, price, totalQuantity);
	}

	private Price parsePrice(String priceAsString) {
		String[] tokens = priceAsString.split(" ");
		BigDecimal amount = new BigDecimal(Double.parseDouble(tokens[1]), MathContext.DECIMAL32);
		Currency currency = Currency.valueOf(tokens[0]);
		Price price = new Price(amount, currency);
		return price;
	}

	private Pair<ItemRow, Map<Map<VariationKey, VariationValue>, Integer>> parseItemRowWithVariations(String[] line,
			List<Map<VariationKey, VariationValue>> headerCells) {
		Map<Map<VariationKey, VariationValue>, Integer> quantityPerVariation = new HashMap<>();
		ItemId itemId = new ItemId(Long.parseLong(line[0]));
		String title = line[1];
		Map<Map<VariationKey, VariationValue>, Integer> map = new HashMap<>();
		for (int i = nonVariationColumnCount; i < line.length; i++) {
			String cell = line[i];
			if (StringUtils.isNullOrEmpty(cell)) {
				break;
			}
			Map<VariationKey, VariationValue> concreteSpec = headerCells.get(i - nonVariationColumnCount);
			quantityPerVariation.put(concreteSpec, Integer.parseInt(cell));
			map.put(concreteSpec, i);
		}
		Price price = parsePrice(line[2]);
		return new ImmutablePair<ItemRow, Map<Map<VariationKey, VariationValue>, Integer>>(new ItemRow(itemId, title, price, quantityPerVariation),
				map);
	}

	private boolean isVariationLineHeader(String[] line) {
		boolean isHeader = (line[0] == null || line[0].isEmpty() || line[1] == null || line[1].isEmpty()) && !line[2].isEmpty();
		return isHeader;
	}

	/*	private List<ItemRow> getOrCreateAndGet(Map<Map<VariationKey, Set<VariationValue>>, List<ItemRow>> varSpecMapToItemRows,
				Map<VariationKey, Set<VariationValue>> varSpecMap) {
			if (!varSpecMapToItemRows.containsKey(varSpecMap)) {
				varSpecMapToItemRows.put(varSpecMap, new ArrayList<ItemRow>());
			}
			return varSpecMapToItemRows.get(varSpecMap);
		}

		private Map<VariationKey, Set<VariationValue>> toTypedMap(Set<Map<VariationKey, VariationValue>> set) {
			Map<VariationKey, Set<VariationValue>> allMap = new HashMap<>();
			for (Map<VariationKey, VariationValue> map : set) {
				for (Entry<VariationKey, VariationValue> entry : map.entrySet()) {
					VariationKey key = entry.getKey();
					if (!allMap.containsKey(key)) {
						allMap.put(key, new HashSet<VariationValue>());
					}
					allMap.get(key).add(entry.getValue());
				}
			}
			return allMap;
		}
	*/
	public List<String[]> buildLines(
			Pair<Map<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>, List<ItemRow>> varSpecMapToItemRowsAndNoVariationItemRows) {
		List<Entry<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>> items1 = rowSorter.sort(varSpecMapToItemRowsAndNoVariationItemRows
				.getLeft().entrySet());
		List<String[]> itemWithVariationsLines = buildLinesFromVarItems(items1, buildColumnIndexMap(items1));
		List<String[]> emptyLine = Arrays.asList(new String[0][]);
		List<String[]> itemWithNoVariationsLines = buildLinesFromNoVarItems(rowSorter.sort(varSpecMapToItemRowsAndNoVariationItemRows.getRight()));
		List<String[]> items = listUtil.mergeListsOfArrays(itemWithNoVariationsLines, emptyLine, itemWithVariationsLines);
		return items;
	}

}
