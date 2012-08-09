package com.ebayinventory.ebay;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import com.ebay.sdk.ApiContext;
import com.ebay.sdk.ApiException;
import com.ebay.sdk.SdkException;
import com.ebay.sdk.TimeFilter;
import com.ebay.sdk.call.FetchTokenCall;
import com.ebay.sdk.call.GetItemCall;
import com.ebay.sdk.call.GetMyeBaySellingCall;
import com.ebay.sdk.call.GetSellerTransactionsCall;
import com.ebay.sdk.call.GetSessionIDCall;
import com.ebay.sdk.call.ReviseItemCall;
import com.ebay.soap.eBLBaseComponents.DetailLevelCodeType;
import com.ebay.soap.eBLBaseComponents.FeesType;
import com.ebay.soap.eBLBaseComponents.ItemArrayType;
import com.ebay.soap.eBLBaseComponents.ItemListCustomizationType;
import com.ebay.soap.eBLBaseComponents.ItemType;
import com.ebay.soap.eBLBaseComponents.PaginatedItemArrayType;
import com.ebay.soap.eBLBaseComponents.PaginationResultType;
import com.ebay.soap.eBLBaseComponents.PaginationType;
import com.ebay.soap.eBLBaseComponents.TransactionType;
import com.ebayinventory.model.ItemId;
import com.ebayinventory.model.SessionId;

@Component
public class EbayCall {

	public String fetchToken(SessionId sessionId, ApiContext apiContext) throws ApiException, SdkException, Exception {
		FetchTokenCall call = new FetchTokenCall(apiContext);
		call.setSessionID(sessionId.getSessionId());
		return call.fetchToken();
	}

	public ItemType fetchItem(ItemId itemId, ApiContext apiContext) throws ApiException, SdkException, Exception {
		GetItemCall getItemCall = new GetItemCall(apiContext);
		DetailLevelCodeType[] detailLevels = new DetailLevelCodeType[] { DetailLevelCodeType.ITEM_RETURN_ATTRIBUTES };
		getItemCall.setDetailLevel(detailLevels);
		return getItemCall.getItem(itemId.getItemIdAsString());
	}

	public FeesType reviseItem(ItemType item, ApiContext apiContext) throws ApiException, SdkException, Exception {
		ReviseItemCall call = new ReviseItemCall(apiContext);
		call.setItemToBeRevised(item);
		return call.reviseItem();
	}

	public String getSessionId(String ruName, ApiContext apiContext) throws ApiException, SdkException, Exception {
		GetSessionIDCall call = new GetSessionIDCall(apiContext);
		call.setRuName(ruName);
		return call.getSessionID();
	}

	public Pair<Integer, Integer> getMyeBaySellingTotalNumberOfPagesAndEntries(Integer entriesPerPage, ApiContext apiContext) throws ApiException,
			SdkException, Exception {
		GetMyeBaySellingCall getMyeBaySellingCall = new GetMyeBaySellingCall(apiContext);
		ItemListCustomizationType itemListCustomizationType = new ItemListCustomizationType();
		itemListCustomizationType.setInclude(true);
		itemListCustomizationType.setPagination(createPaginationType(entriesPerPage, 1));
		getMyeBaySellingCall.setActiveList(itemListCustomizationType);
		getMyeBaySellingCall.getMyeBaySelling();
		PaginatedItemArrayType returnedActiveList = getMyeBaySellingCall.getReturnedActiveList();
		if (returnedActiveList == null) {
			return new ImmutablePair<Integer, Integer>(0, 0);
		}
		PaginationResultType paginationResult = returnedActiveList.getPaginationResult();
		Integer totalNumberOfPages = paginationResult.getTotalNumberOfPages();
		Integer totalNumberOfEntries = paginationResult.getTotalNumberOfEntries();
		return new ImmutablePair<Integer, Integer>(totalNumberOfPages, totalNumberOfEntries);
	}

	public Pair<Integer, Integer> getSellerTransactionsTotalNumberOfPagesAndEntries(TimeFilter timeFilter, Integer entriesPerPage,
			ApiContext apiContext) throws ApiException, SdkException, Exception {
		GetSellerTransactionsCall call = new GetSellerTransactionsCall(apiContext);
		call.setTimeFilter(timeFilter);
		call.setPagination(createPaginationType(entriesPerPage, 1));
		call.getSellerTransactions();
		PaginationResultType paginationResult = call.getPaginationResult();
		Integer totalNumberOfPages = paginationResult.getTotalNumberOfPages();
		Integer totalNumberOfEntries = paginationResult.getTotalNumberOfEntries();
		return new ImmutablePair<Integer, Integer>(totalNumberOfPages, totalNumberOfEntries);
	}

	public TransactionType[] getSellerTransactions(int pageNumber, int entriesPerPage, TimeFilter timeFilter, ApiContext apiContext)
			throws ApiException, SdkException, Exception {
		GetSellerTransactionsCall call = new GetSellerTransactionsCall(apiContext);
		call.setTimeFilter(timeFilter);
		call.setPagination(createPaginationType(entriesPerPage, pageNumber));
		return call.getReturnedTransactions();
	}

	public ItemType[] getActiveItemTypes(int pageNumber, Integer entriesPerPage, ApiContext apiContext) throws ApiException, SdkException, Exception {
		DetailLevelCodeType[] detailLevelCodeTypes = new DetailLevelCodeType[] { DetailLevelCodeType.ITEM_RETURN_ATTRIBUTES };
		GetMyeBaySellingCall getMyeBaySellingCall = new GetMyeBaySellingCall(apiContext);
		ItemListCustomizationType itemListCustomizationType = new ItemListCustomizationType();
		PaginationType paginationType = new PaginationType();
		paginationType.setEntriesPerPage(entriesPerPage);
		paginationType.setPageNumber(pageNumber);
		itemListCustomizationType.setInclude(true);
		itemListCustomizationType.setPagination(paginationType);
		getMyeBaySellingCall.setActiveList(itemListCustomizationType);
		getMyeBaySellingCall.setDetailLevel(detailLevelCodeTypes);
		getMyeBaySellingCall.getMyeBaySelling();
		PaginatedItemArrayType returnedActiveList = getMyeBaySellingCall.getReturnedActiveList();
		if (returnedActiveList == null) {
			return new ItemType[0];
		}
		ItemArrayType itemArray = returnedActiveList.getItemArray();
		ItemType[] itemTypes = itemArray.getItem();
		return itemTypes;
	}

	private PaginationType createPaginationType(Integer entriesPerPage, int pageNumber) {
		PaginationType paginationType = new PaginationType();
		paginationType.setEntriesPerPage(entriesPerPage);
		paginationType.setPageNumber(pageNumber);
		return paginationType;
	}
}
