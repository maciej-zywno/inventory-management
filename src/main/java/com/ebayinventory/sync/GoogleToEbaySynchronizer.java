package com.ebayinventory.sync;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ebayinventory.FileStorage;
import com.ebayinventory.ItemRowCalculator;
import com.ebayinventory.Repository;
import com.ebayinventory.converter.SpreadsheetModelConverter;
import com.ebayinventory.ebay.EbaySellerFacade;
import com.ebayinventory.gdocs.GoogleFacade;
import com.ebayinventory.model.EbayLogin;
import com.ebayinventory.model.ItemRow;
import com.ebayinventory.model.VariationKey;
import com.ebayinventory.model.VariationValue;
import com.ebayinventory.util.DateFormatter;

@Component
public class GoogleToEbaySynchronizer {

	private final Logger log = Logger.getLogger(GoogleToEbaySynchronizer.class);

	private final FileStorage fileStorage;
	private final GoogleFacade googleFacade;
	private final Repository repository;
	private final SpreadsheetModelConverter spreadsheetModelConverter;
	private final EbaySellerFacade ebaySellerFacade;
	private final ItemRowCalculator itemRowCalculator;
	private final DateFormatter dateFormatter;
	private final int googleToEbaySynchronizerSchedulePeriodInMillis;

	@Autowired
	public GoogleToEbaySynchronizer(FileStorage fileStorage, GoogleFacade googleFacade, Repository repository,
			SpreadsheetModelConverter spreadsheetModelConverter, EbaySellerFacade ebaySellerFacade, ItemRowCalculator itemRowCalculator,
			DateFormatter dateFormatter,
			@Value("${googleToEbaySynchronizerSchedulePeriodInMillis}") int googleToEbaySynchronizerSchedulePeriodInMillis) {
		this.fileStorage = fileStorage;
		this.googleFacade = googleFacade;
		this.repository = repository;
		this.spreadsheetModelConverter = spreadsheetModelConverter;
		this.ebaySellerFacade = ebaySellerFacade;
		this.itemRowCalculator = itemRowCalculator;
		this.dateFormatter = dateFormatter;
		this.googleToEbaySynchronizerSchedulePeriodInMillis = googleToEbaySynchronizerSchedulePeriodInMillis;
	}

	void executeUpdate(EbayLogin ebayLogin) {

		long currentTime = System.currentTimeMillis();

		// load gdocs items
		log.info("Loading gdocs spreadsheet for seller " + ebayLogin.getEbayLogin());
		long startGoogleItemsArrayDownloadingTime = System.currentTimeMillis();
		String[][] googleItemsArray = googleFacade.readSpreadsheetLines(repository.getResourceId(ebayLogin), "csv");
		long endGoogleItemsArrayDownloadingTime = System.currentTimeMillis() - startGoogleItemsArrayDownloadingTime;
		log.info("Loaded gdocs spreadsheet of size " + googleItemsArray.length + " in millis " + endGoogleItemsArrayDownloadingTime);

		log.info("Converting gdocs spreadsheet for seller " + ebayLogin.getEbayLogin());
		long startGoogleItemsArrayConvertingTime = System.currentTimeMillis();
		Pair<Map<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>, List<ItemRow>> googleItemRows = spreadsheetModelConverter.convert(
				googleItemsArray).getItemRows();
		long endGoogleItemsArrayConvertingTime = System.currentTimeMillis() - startGoogleItemsArrayConvertingTime;
		log.info("Converted googleItemsArray to variationItems=" + googleItemRows.getLeft().size() + " and noVariationItems="
				+ googleItemRows.getRight().size() + " in millis " + endGoogleItemsArrayConvertingTime);

		// load local items
		log.info("Loading local csv for seller " + ebayLogin.getEbayLogin());
		long startLocalCsvLoadingTime = System.currentTimeMillis();
		String[][] localItemsArray = fileStorage.load(ebayLogin);
		long endLocalCsvLoadingTime = System.currentTimeMillis() - startLocalCsvLoadingTime;
		log.info("Loaded local csv for seller " + ebayLogin.getEbayLogin() + " in millis " + endLocalCsvLoadingTime);

		log.info("Converting local csv for seller " + ebayLogin.getEbayLogin());
		long startLocalCsvConvertingTime = System.currentTimeMillis();
		Pair<Map<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>, List<ItemRow>> locaItemRows = spreadsheetModelConverter.convert(
				localItemsArray).getItemRows();
		long endLocalCsvConvertingTime = System.currentTimeMillis() - startLocalCsvConvertingTime;
		log.info("Converted local csv for seller " + ebayLogin.getEbayLogin() + " in millis " + endLocalCsvConvertingTime);

		// find delta
		log.info("Finding delta for " + ebayLogin.getEbayLogin());
		long startFindingDeltaTime = System.currentTimeMillis();
		List<ItemRow> delta = itemRowCalculator.findDelta(googleItemRows, locaItemRows);
		long endFindingDeltaTime = System.currentTimeMillis() - startFindingDeltaTime;
		log.info("Found delta of size " + delta.size() + " for " + ebayLogin.getEbayLogin() + " in millis " + endFindingDeltaTime);

		// ebay update
		log.info("Updating delta for " + ebayLogin.getEbayLogin());
		long startEbayUpdateTime = System.currentTimeMillis();
		ebaySellerFacade.updateItems(delta, repository.getToken(ebayLogin));
		long endEbayUpdateTime = System.currentTimeMillis() - startEbayUpdateTime;
		log.info("Updated delta for " + ebayLogin.getEbayLogin() + " in millis " + endEbayUpdateTime);

		// after update store google items as local items
		log.info("Updating local csv for " + ebayLogin.getEbayLogin());
		long startLocalCsvUpdateTime = System.currentTimeMillis();
		fileStorage.update(ebayLogin, googleItemsArray);
		long endLocalCsvUpdateTime = System.currentTimeMillis() - startLocalCsvUpdateTime;
		log.info("Updated local csv for " + ebayLogin.getEbayLogin() + " in millis " + endLocalCsvUpdateTime);

		// schedule next sync
		long nextSyncTime = currentTime + googleToEbaySynchronizerSchedulePeriodInMillis;
		repository.scheduleNextGoogleToEbaySyncTime(ebayLogin, nextSyncTime);
		log.info("Scheduled next sync google2ebay time for " + ebayLogin.getEbayLogin() + " at " + dateFormatter.format(nextSyncTime));

	}

}
