package com.ebayinventory;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import com.ebayinventory.converter.ColumnBuilder;
import com.ebayinventory.converter.ColumnSorter;
import com.ebayinventory.converter.RowSorter;
import com.ebayinventory.converter.SpreadsheetModelConverter;
import com.ebayinventory.converter.StringArrayParser;
import com.ebayinventory.model.CellPosition;
import com.ebayinventory.model.Currency;
import com.ebayinventory.model.EbayLogin;
import com.ebayinventory.model.ItemId;
import com.ebayinventory.model.ItemRow;
import com.ebayinventory.model.Model;
import com.ebayinventory.model.VariationKey;
import com.ebayinventory.model.VariationValue;
import com.ebayinventory.util.NumberFormatter;
import com.ebayinventory.util.PriceUtil;

public class SpreadsheetModelConverterTest extends TestCase {

	static {
		ToStringBuilder.setDefaultStyle(ToStringStyle.SIMPLE_STYLE);
	}

	private final SpreadsheetModelConverter converter = new SpreadsheetModelConverter(new ColumnBuilder(new ColumnSorter()), new ListUtil(),
			new RowSorter(), new StringArrayParser(), new NumberFormatter());
	private final PriceUtil priceUtil = new PriceUtil();

	@Test
	public void testModel_OneVarItem() {
		// SETUP
		String[] firstLine = { "", "Colour,Size", "Price", "WHITE,XL", "BLACK,S", "BLACK,L", "WHITE,L", "BLACK,M", "WHITE,M", "BLACK,XL", "WHITE,S" };
		String[] secondLine = { "110097770248", "super bra 6", "EUR 5.99", "5", "10", "1", "1", "1", "1", "1", "1" };
		String[][] array = new String[][] { firstLine, secondLine };
		// expected item
		Map<Map<VariationKey, VariationValue>, Integer> expectedQtyPerVariation = new HashMap<>();
		add(expectedQtyPerVariation, p(var("WHITE", "XL"), 5), p(var("BLACK", "S"), 10), p(var("BLACK", "L"), 1), p(var("WHITE", "L"), 1),
				p(var("BLACK", "M"), 1), p(var("WHITE", "M"), 1), p(var("BLACK", "XL"), 1), p(var("WHITE", "S"), 1));
		ItemRow expectedItemRow = new ItemRow(new ItemId(110097770248l), "super bra 6", priceUtil.createPrice(Currency.EUR, 5.99),
				expectedQtyPerVariation);
		Map<VariationKey, Set<VariationValue>> expextedCombinations = new HashMap<>();
		expextedCombinations.put(key("Colour"), set(value("WHITE"), value("BLACK")));
		expextedCombinations.put(key("Size"), set(value("S"), value("M"), value("L"), value("XL")));

		Map<Pair<ItemId, Map<VariationKey, VariationValue>>, CellPosition> expectedSpreadsheetMatrix = new HashMap<>();
		expectedSpreadsheetMatrix.put(pair(id("110097770248"), var("WHITE", "XL")), pos(1, 3));
		expectedSpreadsheetMatrix.put(pair(id("110097770248"), var("BLACK", "S")), pos(1, 4));
		expectedSpreadsheetMatrix.put(pair(id("110097770248"), var("BLACK", "L")), pos(1, 5));
		expectedSpreadsheetMatrix.put(pair(id("110097770248"), var("WHITE", "L")), pos(1, 6));
		expectedSpreadsheetMatrix.put(pair(id("110097770248"), var("BLACK", "M")), pos(1, 7));
		expectedSpreadsheetMatrix.put(pair(id("110097770248"), var("WHITE", "M")), pos(1, 8));
		expectedSpreadsheetMatrix.put(pair(id("110097770248"), var("BLACK", "XL")), pos(1, 9));
		expectedSpreadsheetMatrix.put(pair(id("110097770248"), var("WHITE", "S")), pos(1, 10));

		// EXECUTE
		Model model = converter.convert(array);

		// VERIFY
		Map<Map<VariationKey, Set<VariationValue>>, List<ItemRow>> variationItems = model.getItemRows().getLeft();
		Assert.assertEquals("should be only one variation item", 1, variationItems.size());
		Entry<Map<VariationKey, Set<VariationValue>>, List<ItemRow>> actualEntry = variationItems.entrySet().iterator().next();
		Assert.assertEquals("wrong combinations", expextedCombinations, actualEntry.getKey());
		Assert.assertEquals("should be only one variation item", 1, actualEntry.getValue().size());
		Assert.assertEquals("wrong item id", expectedItemRow.getItemId(), actualEntry.getValue().get(0).getItemId());
		Assert.assertEquals("wrong item price", expectedItemRow.getPrice(), actualEntry.getValue().get(0).getPrice());
		Assert.assertEquals("wrong item title", expectedItemRow.getTitle(), actualEntry.getValue().get(0).getTitle());
		Assert.assertEquals("wrong item qty/var", expectedItemRow.getQuantityPerVariations(), actualEntry.getValue().get(0)
				.getQuantityPerVariations());
		Assert.assertEquals("wrong item total qty", expectedItemRow.getTotalQuantity(), actualEntry.getValue().get(0).getTotalQuantity());
		Assert.assertEquals("wrong item row", expectedItemRow, actualEntry.getValue().get(0));
		Assert.assertTrue("should be no no variation items", model.getItemRows().getRight().isEmpty());
		Assert.assertEquals("wrong spreadsheet matrix size", expectedSpreadsheetMatrix.size(), model.getSpreadsheetMatrix().size());
		Assert.assertEquals("wrong spreadsheet matrix", expectedSpreadsheetMatrix, model.getSpreadsheetMatrix());

	}

	@Test
	public void testModel_TwoNonVarItem() {
		// SETUP
		String[] firstLine = { "110097770248", "super 1", "EUR 2.49", "0" };
		String[] secondLine = { "110097770249", "super 2", "EUR 4.99", "1" };
		String[][] array = new String[][] { firstLine, secondLine };
		// expected items
		ItemRow expectedItem1 = new ItemRow(new ItemId(110097770248l), "super 1", priceUtil.createPrice(Currency.EUR, 2.49), 0);
		ItemRow expectedItem2 = new ItemRow(new ItemId(110097770249l), "super 2", priceUtil.createPrice(Currency.EUR, 4.99), 1);
		// expected matrix
		Map<Pair<ItemId, Map<VariationKey, VariationValue>>, CellPosition> expectedSpreadsheetMatrix = new HashMap<>();
		expectedSpreadsheetMatrix.put(pair(id("110097770248"), null), pos(0, 3));
		expectedSpreadsheetMatrix.put(pair(id("110097770249"), null), pos(1, 3));

		// EXECUTE
		Model model = converter.convert(array);

		// VERIFY
		Map<Map<VariationKey, Set<VariationValue>>, List<ItemRow>> variationItems = model.getItemRows().getLeft();
		Assert.assertTrue("should be no variation item", variationItems.isEmpty());
		Assert.assertEquals("should be 2 non-variation items", 2, model.getItemRows().getRight().size());
		Assert.assertEquals("wrong 1st item", expectedItem1, model.getItemRows().getRight().get(0));
		Assert.assertEquals("wrong 2nd item", expectedItem2, model.getItemRows().getRight().get(1));

		Assert.assertEquals("spreadsheet matrix should have 2 items", 2, model.getSpreadsheetMatrix().size());
		Assert.assertNotNull("no pos for 110097770248", model.getSpreadsheetMatrix().get(pair(id("110097770248"), null)));
		Assert.assertNotNull("no pos for 110097770249", model.getSpreadsheetMatrix().get(pair(id("110097770249"), null)));
		Assert.assertEquals("wrong pos for 110097770248", pos(0, 3), model.getSpreadsheetMatrix().get(pair(id("110097770248"), null)));
		Assert.assertEquals("wrong pos for 110097770249", pos(1, 3), model.getSpreadsheetMatrix().get(pair(id("110097770249"), null)));
		Assert.assertEquals("wrong matrix", expectedSpreadsheetMatrix, model.getSpreadsheetMatrix());

	}

	@Test
	public void testModel_OneNonVarItemAndOneVarItem() {
		// SETUP
		String[] firstLine = { "", "Colour,Size", "Price", "WHITE,XL", "BLACK,S", "BLACK,L", "WHITE,L", "BLACK,M", "WHITE,M", "BLACK,XL", "WHITE,S" };
		String[] secondLine = { "110097770248", "super bra 6", "EUR 2.99", "5", "10", "1", "1", "1", "1", "1", "1" };
		String[] thirdLine = {};
		String[] fourthLine = { "110097770249", "super 1", "EUR 12.49", "1" };
		String[][] array = new String[][] { firstLine, secondLine, thirdLine, fourthLine };

		// expected var item
		Map<Map<VariationKey, VariationValue>, Integer> expectedQtyPerVariation = new HashMap<>();
		add(expectedQtyPerVariation, p(var("WHITE", "XL"), 5), p(var("BLACK", "S"), 10), p(var("BLACK", "L"), 1), p(var("WHITE", "L"), 1),
				p(var("BLACK", "M"), 1), p(var("WHITE", "M"), 1), p(var("BLACK", "XL"), 1), p(var("WHITE", "S"), 1));
		ItemRow expectedItemRow = new ItemRow(new ItemId(110097770248l), "super bra 6", priceUtil.createPrice(Currency.EUR, 2.99),
				expectedQtyPerVariation);
		Map<VariationKey, Set<VariationValue>> expextedCombinations = new HashMap<>();
		expextedCombinations.put(key("Colour"), set(value("WHITE"), value("BLACK")));
		expextedCombinations.put(key("Size"), set(value("S"), value("M"), value("L"), value("XL")));

		Map<Pair<ItemId, Map<VariationKey, VariationValue>>, CellPosition> expectedSpreadsheetMatrix = new HashMap<>();
		expectedSpreadsheetMatrix.put(pair(id("110097770248"), var("WHITE", "XL")), pos(1, 3));
		expectedSpreadsheetMatrix.put(pair(id("110097770248"), var("BLACK", "S")), pos(1, 4));
		expectedSpreadsheetMatrix.put(pair(id("110097770248"), var("BLACK", "L")), pos(1, 5));
		expectedSpreadsheetMatrix.put(pair(id("110097770248"), var("WHITE", "L")), pos(1, 6));
		expectedSpreadsheetMatrix.put(pair(id("110097770248"), var("BLACK", "M")), pos(1, 7));
		expectedSpreadsheetMatrix.put(pair(id("110097770248"), var("WHITE", "M")), pos(1, 8));
		expectedSpreadsheetMatrix.put(pair(id("110097770248"), var("BLACK", "XL")), pos(1, 9));
		expectedSpreadsheetMatrix.put(pair(id("110097770248"), var("WHITE", "S")), pos(1, 10));

		// expected non-var item
		ItemRow expectedItem2 = new ItemRow(new ItemId(110097770249l), "super 1", priceUtil.createPrice(Currency.EUR, 12.49), 1);
		expectedSpreadsheetMatrix.put(pair(id("110097770249"), null), pos(3, 3));

		// EXECUTE
		Model model = converter.convert(array);

		// VERIFY
		Map<Map<VariationKey, Set<VariationValue>>, List<ItemRow>> variationItems = model.getItemRows().getLeft();
		Assert.assertEquals("should be only one variation item", 1, variationItems.size());
		Entry<Map<VariationKey, Set<VariationValue>>, List<ItemRow>> actualEntry = variationItems.entrySet().iterator().next();
		Assert.assertEquals("wrong combinations", expextedCombinations, actualEntry.getKey());
		Assert.assertEquals("should be only one variation item", 1, actualEntry.getValue().size());
		Assert.assertEquals("wrong item id", expectedItemRow.getItemId(), actualEntry.getValue().get(0).getItemId());
		Assert.assertEquals("wrong item price", expectedItemRow.getPrice(), actualEntry.getValue().get(0).getPrice());
		Assert.assertEquals("wrong item title", expectedItemRow.getTitle(), actualEntry.getValue().get(0).getTitle());
		Assert.assertEquals("wrong item qty/var", expectedItemRow.getQuantityPerVariations(), actualEntry.getValue().get(0)
				.getQuantityPerVariations());
		Assert.assertEquals("wrong item total qty", expectedItemRow.getTotalQuantity(), actualEntry.getValue().get(0).getTotalQuantity());
		Assert.assertEquals("wrong item row", expectedItemRow, actualEntry.getValue().get(0));

		Assert.assertEquals("should be 1 non-variation item", 1, model.getItemRows().getRight().size());
		Assert.assertEquals("wrong non-variation item", expectedItem2, model.getItemRows().getRight().get(0));

		Assert.assertEquals("spreadsheet matrix should have 9 (1+8) items", 9, model.getSpreadsheetMatrix().size());
		Assert.assertEquals("wrong matrix", expectedSpreadsheetMatrix, model.getSpreadsheetMatrix());

	}

	private Pair<ItemId, Map<VariationKey, VariationValue>> pair(ItemId id, Map<VariationKey, VariationValue> var) {
		return new ImmutablePair<ItemId, Map<VariationKey, VariationValue>>(id, var);
	}

	private ItemId id(String itemId) {
		return new ItemId(Long.parseLong(itemId));
	}

	private CellPosition pos(int row, int column) {
		return new CellPosition(row, column);
	}

	private Set<VariationValue> set(VariationValue... values) {
		return new HashSet<>(Arrays.asList(values));
	}

	private VariationKey key(String key) {
		return new VariationKey(key);
	}

	private VariationValue value(String value) {
		return new VariationValue(value);
	}

	@SafeVarargs
	private final void add(Map<Map<VariationKey, VariationValue>, Integer> expectedQtyPerVariation,
			Pair<Map<VariationKey, VariationValue>, Integer>... pairs) {
		for (Pair<Map<VariationKey, VariationValue>, Integer> pair : pairs) {
			expectedQtyPerVariation.put(pair.getKey(), pair.getValue());
		}
	}

	private Pair<Map<VariationKey, VariationValue>, Integer> p(Map<VariationKey, VariationValue> var, int qty) {
		return new ImmutablePair<Map<VariationKey, VariationValue>, Integer>(var, qty);
	}

	private Map<VariationKey, VariationValue> var(String color, String size) {
		Map<VariationKey, VariationValue> map = new HashMap<>();
		map.put(key("Colour"), new VariationValue(color));
		map.put(new VariationKey("Size"), new VariationValue(size));
		return map;
	}

	public static void main(String[] args) {
		ToStringBuilder.setDefaultStyle(ToStringStyle.SIMPLE_STYLE);

		String[][] array = new FileStorage(new File("C:/dev/ebay/csv"), "UTF-8").load(new EbayLogin("testuser_ostpreis"));
		SpreadsheetModelConverter converter = new SpreadsheetModelConverter(new ColumnBuilder(new ColumnSorter()), new ListUtil(), new RowSorter(),
				new StringArrayParser(), new NumberFormatter());
		Model model = converter.convert(array);
		Pair<Map<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>, List<ItemRow>> pair = model.getItemRows();

		System.out.println("NO VARIATION ITEMS");
		for (ItemRow itemRow : pair.getRight()) {
			System.out.println(itemRow);
		}
		System.out.println();
		System.out.println("VARIATION ITEMS");
		for (Entry<Map<VariationKey, Set<VariationValue>>, List<ItemRow>> entry : pair.getLeft().entrySet()) {
			System.out.println();
			System.out.println(StringUtils.asString(entry.getKey()));
			System.out.println();
			for (ItemRow itemRow : entry.getValue()) {
				String line = itemRow.getItemId().getItemId() + "," + itemRow.getTitle() + ","
						+ StringUtils.qtyPerVariations(itemRow.getQuantityPerVariations());
				System.out.println(line);
			}
		}

		for (@SuppressWarnings("unused")
		Entry<Pair<ItemId, Map<VariationKey, VariationValue>>, CellPosition> entry : model.getSpreadsheetMatrix().entrySet()) {

		}
	}
}
