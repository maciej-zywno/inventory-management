/*package com.ebayinventory.ebay;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.Ostermiller.util.CSVPrint;
import com.Ostermiller.util.CSVPrinter;
import com.ebay.sdk.ApiContext;
import com.ebay.sdk.ApiException;
import com.ebay.sdk.SdkException;
import com.ebay.soap.eBLBaseComponents.ItemType;
import com.ebay.soap.eBLBaseComponents.NameValueListArrayType;
import com.ebay.soap.eBLBaseComponents.NameValueListType;
import com.ebayinventory.converter.ItemTypeConverter;
import com.ebayinventory.converter.SpreadsheetModelConverter;
import com.ebayinventory.model.ItemRow;

public class EbayFetchItemsBySellerLoginTest {

	public static void main(String[] args) throws ApiException, SdkException, Exception {
		String ebayLogin = "infantfashionshop";
		ItemTypeConverter itemTypeConverter = new ItemTypeConverter();
		SpreadsheetModelConverter spreadsheetModelConverter = new SpreadsheetModelConverter();

		ApiContext apiContext = createApiContext();
		apiContext
				.getApiCredential()
				.seteBayToken(
						"AgAAAA**AQAAAA**aAAAAA**gp10Tw**nY+sHZ2PrBmdj6wVnY+sEZ2PrA2dj6wFk4CoAJiAoQ6dj6x9nY+seQ**hWwBAA**AAMAAA**qHwemvmihhQTgg5Op+1wsm5Gt5zJvG26jx9P4a3TPJeF2VKWkABFT4vF7dBoFwwLzizJnCJCUIxMZhOQlkGM+jVK/gGlmp0cYr0finFtWZg9HbCtUiDM+Ibh7SlkRYvw5xhc+DA0KzvQQRaQzIDlaNyjYkK/JCiMIWpsgt8J++oxjKjprKN15qmq3jy1gxfbwaiQxzuHQovlUK0vUBunZxT0Yv13yW3OzRz4fzC8Xgy9tADPm5eqi6O5g3HoKTsVXkqjiHRH1x4XJjtqqDLT062A7LSJIu8MNec9I/kzwHahsG0sLoxH+9MiRm7jWzGPgTb/rwI+NVh5D3I8dVi4GMhuKkd5rGj6EK4KYqKD3T19i8tEHc62Q5QS68PeVlc4UD6xYdYqgUv7fdb6e6Oeie9q36mvSZzZhGzeeSRT332W/4yXl8ilubN96lr64VPBZ1g9of3JpPMJxK/uDTTtXIpyKpZMhYAE396DUMLGEd5ZEakJEr/fd46cC5chQR5TBO7pI4/8W6+3chMMRaSv7PLAOUGwGOAIxOx202AHG8gr3haVgProImyDH25xgV3vl2bs6zyYhUkjkg+BKK0l3CVwUYAj18gQuSn4l47xPsr3y2vz3e9vgaKfRGwRf76gEEbnpuhyqOYKZlJ2RSNKXHdvI/GN4yM/k7szDkdrwe9goa77xAAYVh1LUXdkDxr27emqkqPJr0NIVOmnsY6R7s0vS1qgW2RYdt0Zl3H7aAAYNLpnkA0aW3dCVnxi87jl");

		List<ItemType> itemTypeList = fetchSellerItems(ebayLogin, apiContext);

		Map<Map<String, Set<String>>, List<ItemRow>> varSpecMapToItemRows = itemTypeConverter.buildVarSpecMapToItemRows(itemTypeList);

		List<String[]> lines = spreadsheetModelConverter.buildLines(varSpecMapToItemRows);

		FileWriter writer = new FileWriter("E:/dev/ebay-inventory/output.csv");
		CSVPrint csvPrint = new CSVPrinter(writer, '#', '"', ',', true, true);
		for (String[] cells : lines) {
			csvPrint.writeln(cells);
		}

	}

	private static List<ItemType> fetchSellerItems(String userId, ApiContext apiContext) throws ApiException, SdkException, Exception {
		ItemFetcher itemsFetcher = new ItemFetcher(apiContext);
		Map<String, ItemType> itemIdToItemType = itemsFetcher.fetchItems(userId);
		List<ItemType> itemTypeList = new ArrayList<>();
		for (ItemType itemType : itemIdToItemType.values()) {
			itemTypeList.add(itemsFetcher.fetchItem(itemType));
		}
		return itemTypeList;
	}

	private static String asString(NameValueListArrayType variationSpecifics) {
		String text = "";
		for (NameValueListType type : variationSpecifics.getNameValueList()) {
			text += type.getName() + ":" + type.getValue(0) + ";";
		}
		return text.isEmpty() ? text : text.substring(0, text.length() - 1);
	}

	private static ApiContext createApiContext() {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("spring/ebay-api-context.xml");
		ApiContext apiContext = ctx.getBean(ApiContext.class);
		return apiContext;
	}

}
*/