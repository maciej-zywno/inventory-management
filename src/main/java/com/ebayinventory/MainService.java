package com.ebayinventory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ebay.soap.eBLBaseComponents.ItemType;
import com.ebayinventory.converter.ItemTypeConverter;
import com.ebayinventory.converter.SpreadsheetModelConverter;
import com.ebayinventory.ebay.EbayAuthFacade;
import com.ebayinventory.ebay.EbaySellerFacade;
import com.ebayinventory.email.EmailHtmlBuilder;
import com.ebayinventory.email.EmailSender;
import com.ebayinventory.email.data.EmailAddress;
import com.ebayinventory.email.data.EmailMessage;
import com.ebayinventory.gdocs.GoogleFacade;
import com.ebayinventory.messages.AppAuthorizedMessage;
import com.ebayinventory.messages.AppAuthorizedMessageError;
import com.ebayinventory.model.EbayLogin;
import com.ebayinventory.model.ItemRow;
import com.ebayinventory.model.ResourceId;
import com.ebayinventory.model.Token;
import com.ebayinventory.model.Url;
import com.ebayinventory.model.VariationKey;
import com.ebayinventory.model.VariationValue;
import com.google.gdata.data.docs.DocumentListEntry;

@Component
public class MainService {

	private final Logger log = Logger.getLogger(MainService.class);

	private final EbaySellerFacade ebaySellerFacade;
	private final ItemTypeConverter itemTypeConverter;
	private final SpreadsheetModelConverter spreadsheetModelConverter;
	private final GoogleFacade googleFacade;
	private final EmailSender emailService;
	private final EbayAuthFacade ebayAuthFacade;
	private final AmqpTemplate amqpTemplate;
	private final String appAuthorizedRoutingKey;
	private final String appAuthorizedExceptionRoutingKey;
	private final EmailHtmlBuilder emailBuilder;
	private final FileStorage fileStorage;
	private final Repository repository;
	private final SpreadsheetTitleBuilder spreadsheetTitleBuilder;
	private final GoogleDocsLimitations googleDocsLimitations;

	@Autowired
	public MainService(EbaySellerFacade ebaySellerFacade, ItemTypeConverter itemTypeConverter, SpreadsheetModelConverter spreadsheetModelConverter,
			GoogleFacade googleFacade, EmailSender emailSender, EbayAuthFacade ebayAuthFacade, AmqpTemplate amqpTemplate,
			@Value("${appAuthorizedRoutingKey}") String appAuthorizedRoutingKey,
			@Value("${appAuthorizedExceptionRoutingKey}") String appAuthorizedExceptionRoutingKey, EmailHtmlBuilder emailBuilder,
			FileStorage fileStorage, Repository repository, SpreadsheetTitleBuilder spreadsheetTitleBuilder,
			GoogleDocsLimitations googleDocsLimitations) {
		this.ebaySellerFacade = ebaySellerFacade;
		this.itemTypeConverter = itemTypeConverter;
		this.spreadsheetModelConverter = spreadsheetModelConverter;
		this.googleFacade = googleFacade;
		this.emailService = emailSender;
		this.ebayAuthFacade = ebayAuthFacade;
		this.amqpTemplate = amqpTemplate;
		this.appAuthorizedRoutingKey = appAuthorizedRoutingKey;
		this.appAuthorizedExceptionRoutingKey = appAuthorizedExceptionRoutingKey;
		this.emailBuilder = emailBuilder;
		this.fileStorage = fileStorage;
		this.repository = repository;
		this.spreadsheetTitleBuilder = spreadsheetTitleBuilder;
		this.googleDocsLimitations = googleDocsLimitations;
	}

	// This method is invoked by Spring and if we let any exception slip out of this method then this method will be re-invoked again and
	// again until it successfully returns. As we send a welcome email we cannot
	public void handleAppAuthorizedMessage(AppAuthorizedMessage message) {
		log.info("handling message " + message);
		try {
			EmailMessage welcomeEmail = emailBuilder.buildWelcomeEmail(message.getEbaySellerLogin(), message.getEmailAddress());
			emailService.sendMail(welcomeEmail);
			handleAppAuthorizedMessage_WithExceptionThrown(message);
		} catch (RuntimeException e) {
			log.info("got exception", e);
			enqueueMessage(new AppAuthorizedMessageError(message, e));
		}
	}

	public void handleAppAuthorizedMessageError(AppAuthorizedMessageError message) {
		log.info("no handling for message " + message);
	}

	private void handleAppAuthorizedMessage_WithExceptionThrown(AppAuthorizedMessage message) {
		// 1 fetch a token unique for the seller
		EbayLogin ebayLogin = message.getEbaySellerLogin();
		Token ebaySellerToken = ebayAuthFacade.fetchToken(ebayLogin);
		log.info("fetched token '" + ebaySellerToken + "'");
		repository.putToken(ebayLogin, ebaySellerToken);

		// 2 fetch listings
		log.info("fetching items for ebay seller '" + ebayLogin.getEbayLogin() + "'");
		List<ItemType> itemTypes = ebaySellerFacade.getActiveItemTypes(ebaySellerToken);
		log.info("fetched items count " + itemTypes.size());

		// 3 build lines to insert to a non-existing-yet spreadsheet
		Pair<Map<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>, List<ItemRow>> varSpecMapToItemRowsAndNoVariationItemRows = itemTypeConverter
				.buildVarSpecMapToItemRows(itemTypes);

		// lines with spec header line
		List<String[]> items = spreadsheetModelConverter.buildLines(varSpecMapToItemRowsAndNoVariationItemRows);

		int originalItemsSize = items.size();

		// google docs limitations: "400,000 cells, with a maximum of 256 columns per sheet."
		if (googleDocsLimitations.exceeded(maxLineLength(items), items.size())) {

			// compute trimmed items
			Map<Integer, List<Map<VariationKey, Set<VariationValue>>>> indexByColumnCount = indexAndSortByColumnCount(varSpecMapToItemRowsAndNoVariationItemRows);
			// let's start with all items and remove the longest elements in each iteration until gdocs limitations are not exceeded
			Map<Map<VariationKey, Set<VariationValue>>, List<ItemRow>> trimmed = varSpecMapToItemRowsAndNoVariationItemRows.getLeft();
			List<String[]> trimmedItems = null;
			do {
				removeLongestItemsFromTrimmedAndRemoveLongestItemFromIndexMap(trimmed, indexByColumnCount);
				trimmedItems = spreadsheetModelConverter
						.buildLines(new ImmutablePair<Map<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>, List<ItemRow>>(trimmed,
								varSpecMapToItemRowsAndNoVariationItemRows.getRight()));

			} while (googleDocsLimitations.exceeded(maxLineLength(trimmedItems), trimmedItems.size()));

			// assigned trimmed items to original items reference
			items = trimmedItems;
		}

		int trimmedItemsSize = items.size();
		boolean isTrimmed = trimmedItemsSize != originalItemsSize;

		// 3 create a new local csv
		String filePath = fileStorage.createNew(ebayLogin, items);

		// 4 upload a new google docs spreadsheet
		String spreadsheetTitle = spreadsheetTitleBuilder.buildSpreadsheetTitle(ebayLogin);
		DocumentListEntry entry = googleFacade.uploadFile(new File(filePath), spreadsheetTitle);

		// 5 store mapping between ebay login and google spreadsheet data (title and resource id)
		repository.putResourceId(new ResourceId(entry.getResourceId()), ebayLogin);
		repository.putSellerEmail(message.getEmailAddress(), ebayLogin);

		// 6 store data locally
		fileStorage.createNew(ebayLogin, items);

		// 7 share the spreadsheet
		// sharing implies a "doc has been shared" message being sent by google to the seller
		googleFacade.addWriter(spreadsheetTitle, message.getEmailAddress());

		// schedule sync
		repository.scheduleNextGoogleToEbaySyncTime(ebayLogin, System.currentTimeMillis());
		repository.scheduleNextEbayToGoogleSyncTime(ebayLogin, System.currentTimeMillis());
		repository.putLastEbayToGoogleSyncTime(ebayLogin, System.currentTimeMillis());

		// 8 send a welcome email to the seller
		Url spreadsheetUrl = new Url(entry.getHtmlLink().getHref());
		emailService.sendMail(emailBuilder.buildSpreadsheetCreatedEmail(spreadsheetTitle, spreadsheetUrl, message.getEmailAddress(), isTrimmed,
				trimmedItemsSize, originalItemsSize));
	}

	private void removeLongestItemsFromTrimmedAndRemoveLongestItemFromIndexMap(Map<Map<VariationKey, Set<VariationValue>>, List<ItemRow>> map,
			Map<Integer, List<Map<VariationKey, Set<VariationValue>>>> indexByColumnCount) {

		Iterator<Entry<Integer, List<Map<VariationKey, Set<VariationValue>>>>> iterator = indexByColumnCount.entrySet().iterator();

		// get longest item (items's variations map)
		List<Map<VariationKey, Set<VariationValue>>> longestVariations = iterator.next().getValue();

		// remove from index
		iterator.remove();

		// remove from main map
		for (Map<VariationKey, Set<VariationValue>> map2 : longestVariations) {
			List<ItemRow> removed = map.remove(map2);
			assertNotNull(removed);
		}

	}

	private void assertNotNull(List<ItemRow> removed) {
		if (removed == null) {
			throw new RuntimeException();
		}
	}

	private int maxLineLength(List<String[]> items) {
		int maxLineLength = 0;
		for (String[] line : items) {
			maxLineLength = line.length > maxLineLength ? line.length : maxLineLength;
		}
		return maxLineLength;
	}

	private final Comparator<Integer> inverseNaturalComparator = new Comparator<Integer>() {
		@Override
		public int compare(Integer o1, Integer o2) {
			if (o1.equals(o2)) {
				return 0;
			}
			return o1 > o2 ? -1 : +1;
		}
	};

	private Map<Integer, List<Map<VariationKey, Set<VariationValue>>>> indexAndSortByColumnCount(
			Pair<Map<Map<VariationKey, Set<VariationValue>>, List<ItemRow>>, List<ItemRow>> varSpecMapToItemRowsAndNoVariationItemRows) {
		Map<Integer, List<Map<VariationKey, Set<VariationValue>>>> index = new TreeMap<>(inverseNaturalComparator);
		for (Map<VariationKey, Set<VariationValue>> entry : varSpecMapToItemRowsAndNoVariationItemRows.getLeft().keySet()) {
			Integer howManyColumns = countColumns(entry.values());
			if (!index.containsKey(howManyColumns)) {
				index.put(howManyColumns, new ArrayList<Map<VariationKey, Set<VariationValue>>>());
			}
			List<Map<VariationKey, Set<VariationValue>>> list = index.get(howManyColumns);
			list.add(entry);
		}
		for (Entry<Integer, List<Map<VariationKey, Set<VariationValue>>>> entry : index.entrySet()) {
			System.out.println(entry.getKey());
		}
		return index;
	}

	private Integer countColumns(Collection<Set<VariationValue>> values) {
		Integer totalCombinations = 1;
		for (Set<VariationValue> set : values) {
			totalCombinations *= set.size();
		}
		return totalCombinations;
	}

	public void appAuthorizedAndEmailAddressEntered(EbayLogin ebaySellerLogin, EmailAddress emailAddress) {
		enqueueMessage(new AppAuthorizedMessage(ebaySellerLogin, emailAddress));
	}

	public void enqueueMessage(AppAuthorizedMessage appAuthorizedMessage) {
		log.info("enqueueing message " + appAuthorizedRoutingKey + ": " + appAuthorizedMessage);
		amqpTemplate.convertAndSend(appAuthorizedRoutingKey, appAuthorizedMessage);
	}

	private void enqueueMessage(AppAuthorizedMessageError appAuthorizedMessageError) {
		log.info("enqueueing message " + appAuthorizedExceptionRoutingKey + ": " + appAuthorizedMessageError);
		amqpTemplate.convertAndSend(appAuthorizedExceptionRoutingKey, appAuthorizedMessageError);
	}
}
