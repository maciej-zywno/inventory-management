package com.ebayinventory.ebay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ebay.sdk.ApiContext;
import com.ebay.soap.eBLBaseComponents.ItemType;
import com.ebay.soap.eBLBaseComponents.TransactionType;
import com.ebay.soap.eBLBaseComponents.VariationType;
import com.ebay.soap.eBLBaseComponents.VariationsType;
import com.ebayinventory.converter.ItemTypeConverter;
import com.ebayinventory.model.ItemId;
import com.ebayinventory.model.ItemRow;
import com.ebayinventory.model.TimeRange;
import com.ebayinventory.model.Token;
import com.ebayinventory.model.VariationKey;
import com.ebayinventory.model.VariationValue;
import com.ebayinventory.util.DateFormatter;

/**
 * needs seller ebay token
 * 
 * apiContext.getApiCredential() .seteBayToken( "AgAAAA**AQAAAA**aAAAAA**gp10Tw**nY+");
 */
@Component
public class EbaySellerFacade {

	private final Logger log = Logger.getLogger(EbaySellerFacade.class);

	private final int millisInDay = 24 * 60 * 60 * 1000;

	private final ApiContextBuilder apiContextBuilder;
	private final ItemTypeConverter itemTypeConverter;
	private final EbayFacade ebayFacade;
	private final DateFormatter dateFormatter;

	@Autowired
	public EbaySellerFacade(ApiContextBuilder apiContextBuilder, ItemTypeConverter itemTypeConverter, EbayFacade ebayFacade,
			DateFormatter dateFormatter) {
		this.apiContextBuilder = apiContextBuilder;
		this.itemTypeConverter = itemTypeConverter;
		this.ebayFacade = ebayFacade;
		this.dateFormatter = dateFormatter;
	}

	public List<ItemType> getActiveItemTypes(Token token) {
		ApiContext apiContext = apiContextBuilder.buildNewWithConcreteApiCredentialEbayToken(token);
		List<ItemType> items = new ArrayList<>();
		Integer entriesPerPage = 100;
		int totalPageCount = ebayFacade.getMyeBaySellingTotalNumberOfPagesAndEntries(entriesPerPage, apiContext).getLeft();
		for (int i = 1; i <= totalPageCount; i++) {
			log.info("fetching items for page " + i + " and ebaySellerToken '" + apiContext.getApiCredential().geteBayToken() + "'");
			ItemType[] onePageItems = ebayFacade.getActiveItemTypes(i, entriesPerPage, apiContext);
			log.info("fetched items count: " + onePageItems.length);
			items.addAll(Arrays.asList(onePageItems));
		}
		return items;
	}

	public List<ItemType> getItems(Set<ItemId> itemIds, Token token) {
		ApiContext apiContext = apiContextBuilder.buildNewWithConcreteApiCredentialEbayToken(token);
		List<ItemType> itemTypes = new ArrayList<>();
		for (ItemId itemId : itemIds) {
			itemTypes.add(ebayFacade.fetchItem(itemId, apiContext));
		}
		return itemTypes;
	}

	public void updateItems(List<ItemRow> delta, Token token) {
		ApiContext apiContext = apiContextBuilder.buildNewWithConcreteApiCredentialEbayToken(token);
		for (ItemRow itemRow : delta) {
			boolean hasVariations = itemRow.getQuantityPerVariations() != null;
			if (hasVariations) {
				ItemType item = ebayFacade.fetchItem(itemRow.getItemId(), apiContext);
				ItemType itemToRevise = new ItemType();
				itemToRevise.setItemID(item.getItemID());
				// itemToRevise.setTitle(itemRow.getTitle());
				// TODO: handle com.ebay.sdk.ApiException: The title or subtitle cannot be changed if an auction-style listing has a bid or
				// ends within 12 hours, or a fixed price listing has a sale or a pending Best Offer.
				itemToRevise.setVariations(updateVariationsQuantities(item.getVariations(), itemRow.getQuantityPerVariations()));
				ebayFacade.reviseItem(itemToRevise, apiContext);
			} else {
				ItemType item = new ItemType();
				item.setItemID(itemRow.getItemId().getItemIdAsString());
				// item.setTitle(itemRow.getTitle());
				item.setQuantity(itemRow.getTotalQuantity());
				ebayFacade.reviseItem(item, apiContext);
			}
		}
	}

	public List<TransactionType> fetchSellerTransactions(long lastSellerEventsFetchAndSyncTime, Token token) {
		Integer entriesPerPage = 100;
		TimeRange timeRange = createTimeRange(lastSellerEventsFetchAndSyncTime);
		ApiContext apiContext = apiContextBuilder.buildNewWithConcreteApiCredentialEbayToken(token);
		Pair<Integer, Integer> totalNumberOfPagesAndEntries = ebayFacade.getSellerTransactionsTotalNumberOfPagesAndEntries(timeRange, entriesPerPage,
				apiContext);

		log.info("Fetching transactions from range " + dateFormatter.format(timeRange));

		List<TransactionType> sellerTransactions = new ArrayList<>();
		for (int i = 1; i <= totalNumberOfPagesAndEntries.getLeft(); i++) {
			log.info("fetching transactions for page " + i + " and ebaySellerToken '" + apiContext.getApiCredential().geteBayToken() + "'");
			TransactionType[] onePageItems = ebayFacade.getSellerTransactions(i, entriesPerPage, timeRange, apiContext);
			log.info("fetched transactions count: " + onePageItems.length);
			sellerTransactions.addAll(Arrays.asList(onePageItems));
		}

		log.info("Fetched " + sellerTransactions.size() + " transactions");
		return sellerTransactions;
	}

	public ItemType[] fetchSellerEvents(long lastSellerEventsFetchAndSyncTime, Token token) {
		ApiContext apiContext = apiContextBuilder.buildNewWithConcreteApiCredentialEbayToken(token);
		TimeRange modificationTimeRange = createTimeRange(lastSellerEventsFetchAndSyncTime);
		log.info("Fetching events of items with modification time within range " + dateFormatter.format(modificationTimeRange));
		ItemType[] sellerEvents = ebayFacade.getSellerEvents(modificationTimeRange, apiContext);
		log.info("Fetched " + sellerEvents.length + " events");
		return sellerEvents;
	}

	private TimeRange createTimeRange(long lastSellerEventsFetchAndSyncTime) {
		long modStartTime = lastSellerEventsFetchAndSyncTime == 0 ? nowMinus28Days() : lastSellerEventsFetchAndSyncTime;
		TimeRange modificationTimeRange = toNextDayTimeRange(modStartTime);
		return modificationTimeRange;
	}

	private long nowMinus28Days() {
		// com.ebay.sdk.ApiException: You have exceeded the 30 day maximum time window allowed by <LastModifiedFrom> and <LastModifiedTo>.
		// we take 28 days, just to be sure
		long currentTimeMillis = System.currentTimeMillis();
		long millisIn28days = (long) 28 * 24 * 60 * 60 * 1000;
		long nowMinus28Days = currentTimeMillis - millisIn28days;
		return nowMinus28Days;
	}

	private TimeRange toNextDayTimeRange(long startTime) {
		return new TimeRange(startTime, System.currentTimeMillis() + millisInDay);
	}

	private VariationsType updateVariationsQuantities(VariationsType variationsType,
			Map<Map<VariationKey, VariationValue>, Integer> quantityPerVariations) {
		for (VariationType variationType : variationsType.getVariation()) {
			Map<VariationKey, VariationValue> specMap = itemTypeConverter.asStringMap(variationType.getVariationSpecifics());
			Integer integer = quantityPerVariations.get(specMap);
			variationType.setQuantity(integer);
		}
		return variationsType;
	}

}
