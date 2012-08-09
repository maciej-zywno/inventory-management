package com.ebayinventory.sync;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ebay.soap.eBLBaseComponents.ItemType;
import com.ebay.soap.eBLBaseComponents.TransactionType;
import com.ebayinventory.FileStorage;
import com.ebayinventory.ListUtil;
import com.ebayinventory.Repository;
import com.ebayinventory.converter.ItemTypeConverter;
import com.ebayinventory.converter.SpreadsheetModelConverter;
import com.ebayinventory.ebay.EbaySellerFacade;
import com.ebayinventory.gdocs.GoogleFacade;
import com.ebayinventory.model.CellPosition;
import com.ebayinventory.model.EbayLogin;
import com.ebayinventory.model.ItemId;
import com.ebayinventory.model.ItemRow;
import com.ebayinventory.model.Model;
import com.ebayinventory.model.VariationKey;
import com.ebayinventory.model.VariationValue;
import com.ebayinventory.util.DateFormatter;

@Component
public class EbayToGoogleSynchronizer {

	private final Logger log = Logger.getLogger(EbayToGoogleSynchronizer.class);

	private final FileStorage fileStorage;
	private final GoogleFacade googleFacade;
	private final Repository repository;
	private final SpreadsheetModelConverter spreadsheetModelConverter;
	private final EbaySellerFacade ebaySellerFacade;
	private final DateFormatter dateFormatter;
	private final GoogleToEbaySynchronizer googleToEbaySynchronizer;
	private final ItemTypeConverter itemTypeConverter;
	private final ListUtil listUtil;
	private final int ebayToGoogleSynchronizerSchedulePeriodInMillis;

	@Autowired
	public EbayToGoogleSynchronizer(FileStorage fileStorage, GoogleFacade googleFacade, Repository repository,
			SpreadsheetModelConverter spreadsheetModelConverter, EbaySellerFacade ebaySellerFacade, DateFormatter dateFormatter,
			GoogleToEbaySynchronizer googleToEbaySynchronizer, ItemTypeConverter itemTypeConverter, ListUtil listUtil,
			@Value("${ebayToGoogleSynchronizerSchedulePeriodInMillis}") int ebayToGoogleSynchronizerSchedulePeriodInMillis) {
		this.fileStorage = fileStorage;
		this.googleFacade = googleFacade;
		this.repository = repository;
		this.spreadsheetModelConverter = spreadsheetModelConverter;
		this.ebaySellerFacade = ebaySellerFacade;
		this.dateFormatter = dateFormatter;
		this.googleToEbaySynchronizer = googleToEbaySynchronizer;
		this.itemTypeConverter = itemTypeConverter;
		this.listUtil = listUtil;
		this.ebayToGoogleSynchronizerSchedulePeriodInMillis = ebayToGoogleSynchronizerSchedulePeriodInMillis;
	}

	void executeUpdate(EbayLogin ebayLogin) {

		long currentTime = System.currentTimeMillis();

		// 1) block google docs UI - temporarily remove ebay seller from writer acl
		googleFacade.blockUI(ebayLogin);

		// 2) show "update in progress" message inside a spreadsheet

		// 3) sync google docs to ebay
		googleToEbaySynchronizer.executeUpdate(ebayLogin);

		// 4) load seller events from ebay to find out what listings in google docs are stale
		List<TransactionType> transactionTypes = ebaySellerFacade.fetchSellerTransactions(repository.getLastEbayToGoogleSyncTime(ebayLogin),
				repository.getToken(ebayLogin));
		ItemType[] sellerEventsItemTypes = new ItemType[0];// ebaySellerFacade.fetchSellerEvents(repository.getLastEbayToGoogleSyncTime(ebayLogin),
															// repository.getToken(ebayLogin));

		if (bothEmpty(transactionTypes, sellerEventsItemTypes)) {
			// nothing
			log.info("skipped ebay2google sync as there was no ebay transactions or events");
		} else {
			Set<ItemId> sellerAndTransactionTypeItemIds = listUtil.mergeSets(extractItemIds(transactionTypes), extractItemIds(sellerEventsItemTypes));
			Map<ItemId, ItemRow> changedRowList = indexByItemId(itemTypeConverter.toItemRows(ebaySellerFacade.getItems(
					sellerAndTransactionTypeItemIds, repository.getToken(ebayLogin))));

			// 5) merge local csv with seller events and transactions to build current data that will be uploaded to google and stored as
			// local file
			// Do not change the structure (move rows) in google file. Why? If we move rows then we need to update the google docs file by
			// uploading a
			// new version of the file. Such an update will
			// a) override the columns width that could be changed by an ebay seller
			// b) change the order of rows.
			log.info("loading local items for seller " + ebayLogin.getEbayLogin());
			String[][] localLines = fileStorage.load(ebayLogin);
			Model model = spreadsheetModelConverter.convert(localLines);
			Map<Pair<ItemId, Map<VariationKey, VariationValue>>, CellPosition> spreadsheetMatrix = model.getSpreadsheetMatrix();

			// We will find out what item ids need to be updated. Having spreadsheet matrix we will know what cells to update in google.
			Map<CellPosition, Integer> changesForGdocs = new HashMap<>();
			for (Entry<ItemId, ItemRow> entry : changedRowList.entrySet()) {
				for (Entry<Map<VariationKey, VariationValue>, Integer> variationEntry : entry.getValue().getQuantityPerVariations().entrySet()) {
					CellPosition key = spreadsheetMatrix.get(new ImmutablePair<ItemId, Map<VariationKey, VariationValue>>(entry.getKey(),
							variationEntry.getKey()));
					changesForGdocs.put(key, variationEntry.getValue());
				}
			}

			// 6) store

			// modify local items array in-place
			for (Entry<CellPosition, Integer> entry : changesForGdocs.entrySet()) {
				int row = entry.getKey().getRow();
				int column = entry.getKey().getColumn();
				localLines[row][column] = Integer.toString(entry.getValue());
			}

			String filePath = fileStorage.update(ebayLogin, toArrayList(localLines));
			log.info("Updated local file for seller " + ebayLogin + ": " + filePath);

			// 7) upload updated local csv to google docs
			log.info("Updating google docs file for seller " + ebayLogin + ". Google resource id=" + repository.getResourceId(ebayLogin));
			// googleFacade.updateFile(new File(filePath), repository.getResourceId(ebayLogin));
			googleFacade.updateFile(changesForGdocs, repository.getResourceId(ebayLogin).getResourceIdWithoutPrefix());
			log.info("Updated google docs file for seller " + ebayLogin + ". Google resource id=" + repository.getResourceId(ebayLogin));

			// remove "update in progress" message inside a spreadsheet

		}
		// unblock UI
		googleFacade.unblockUI(ebayLogin);

		// schedule next sync
		long nextSyncTime = currentTime + ebayToGoogleSynchronizerSchedulePeriodInMillis;
		repository.putLastEbayToGoogleSyncTime(ebayLogin, currentTime);
		repository.scheduleNextEbayToGoogleSyncTime(ebayLogin, nextSyncTime);
		log.info("Scheduled next ebay2google sync time for " + ebayLogin.getEbayLogin() + " at " + dateFormatter.format(nextSyncTime));

	}

	private List<String[]> toArrayList(String[][] array) {
		List<String[]> list = new ArrayList<>();
		for (String[] row : array) {
			list.add(row);
		}
		return list;
	}

	private boolean bothEmpty(List<TransactionType> transactionTypes, ItemType[] sellerEventsItemTypes) {
		return transactionTypes.isEmpty() && sellerEventsItemTypes.length == 0;
	}

	private Map<ItemId, ItemRow> indexByItemId(List<ItemRow> itemRows) {
		Map<ItemId, ItemRow> all = new HashMap<>();
		for (ItemRow itemRow : itemRows) {
			all.put(itemRow.getItemId(), itemRow);
		}
		return all;
	}

	private Set<ItemId> extractItemIds(ItemType[] itemTypes) {
		Set<ItemId> itemIds = new HashSet<>();
		for (ItemType itemType : itemTypes) {
			ItemId e = new ItemId(Long.parseLong(itemType.getItemID()));
			itemIds.add(e);
		}
		return itemIds;
	}

	private Set<ItemId> extractItemIds(List<TransactionType> transactionTypes) {
		Set<ItemId> itemIds = new HashSet<>();
		for (TransactionType transactionType : transactionTypes) {
			ItemId e = new ItemId(Long.parseLong(transactionType.getItem().getItemID()));
			itemIds.add(e);
		}
		return itemIds;
	}

}
