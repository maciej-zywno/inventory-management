package com.ebayinventory.ebay;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ebay.sdk.ApiContext;
import com.ebay.sdk.TimeFilter;
import com.ebay.sdk.call.GetSellerEventsCall;
import com.ebay.soap.eBLBaseComponents.FeesType;
import com.ebay.soap.eBLBaseComponents.ItemType;
import com.ebay.soap.eBLBaseComponents.TransactionType;
import com.ebayinventory.ebay.exception.EbayException;
import com.ebayinventory.ebay.exception.GeneralEbayException;
import com.ebayinventory.ebay.exception.TokenRevokedEbayException;
import com.ebayinventory.model.ItemId;
import com.ebayinventory.model.SessionId;
import com.ebayinventory.model.TimeRange;
import com.ebayinventory.model.Token;
import com.ebayinventory.retry.RetryPolicyTemplate;
import com.ebayinventory.retry.RetryUtil;

@Component
public class EbayFacade {

	private final EbayCall ebayCall;
	private final RetryUtil retryUtil;
	private final RetryPolicyTemplate rethrowPolicyTemplate;

	@Autowired
	public EbayFacade(EbayCall ebayCall, RetryUtil retryUtil, RetryPolicyTemplate rethrowPolicyTemplate) {
		this.ebayCall = ebayCall;
		this.retryUtil = retryUtil;
		this.rethrowPolicyTemplate = rethrowPolicyTemplate;
	}

	public ItemType fetchItem(final ItemId itemId, final ApiContext apiContext) {
		return retryUtil.retryTemplate(new Callable<ItemType>() {
			@Override
			public ItemType call() throws Exception {
				return ebayCall.fetchItem(itemId, apiContext);
			}
		}, rethrowPolicyTemplate);
	}

	public FeesType reviseItem(final ItemType item, final ApiContext apiContext) {
		return retryUtil.retryTemplate(new Callable<FeesType>() {
			@Override
			public FeesType call() throws Exception {
				return ebayCall.reviseItem(item, apiContext);
			}
		}, rethrowPolicyTemplate);
	}

	public Token fetchToken(final SessionId sessionId, final ApiContext apiContext) {
		return retryUtil.retryTemplate(new Callable<Token>() {
			@Override
			public Token call() throws Exception {
				return new Token(ebayCall.fetchToken(sessionId, apiContext));
			}
		}, rethrowPolicyTemplate);
	}

	public SessionId getSessionId(final String ruName, final ApiContext apiContext) {
		return retryUtil.retryTemplate(new Callable<SessionId>() {
			@Override
			public SessionId call() throws Exception {
				return new SessionId(ebayCall.getSessionId(ruName, apiContext));
			}
		}, rethrowPolicyTemplate);
	}

	public Pair<Integer, Integer> getMyeBaySellingTotalNumberOfPagesAndEntries(final Integer entriesPerPage, final ApiContext apiContext) {
		return retryUtil.retryTemplate(new Callable<Pair<Integer, Integer>>() {
			@Override
			public Pair<Integer, Integer> call() throws Exception {
				return ebayCall.getMyeBaySellingTotalNumberOfPagesAndEntries(entriesPerPage, apiContext);
			}
		}, rethrowPolicyTemplate);
	}

	public ItemType[] getActiveItemTypes(final int pageNumber, final Integer entriesPerPage, final ApiContext apiContext) {
		return retryUtil.retryTemplate(new Callable<ItemType[]>() {
			@Override
			public ItemType[] call() throws Exception {
				return ebayCall.getActiveItemTypes(pageNumber, entriesPerPage, apiContext);
			}
		}, rethrowPolicyTemplate);
	}

	public EbayException mapToRuntimeException(String errorCode) {
		// http://developer.ebay.com/devzone/xml/docs/Reference/ebay/Errors/ErrorMessages.htm
		switch (errorCode) {
		case "16110":
			throw new TokenRevokedEbayException(errorCode);
		default:
			throw new GeneralEbayException(errorCode);
		}
	}

	public Pair<Integer, Integer> getSellerTransactionsTotalNumberOfPagesAndEntries(final TimeRange startTimeRange, final Integer entriesPerPage,
			final ApiContext apiContext) {
		return retryUtil.retryTemplate(new Callable<Pair<Integer, Integer>>() {
			@Override
			public Pair<Integer, Integer> call() throws Exception {
				return ebayCall.getSellerTransactionsTotalNumberOfPagesAndEntries(toTimeFilter(startTimeRange), entriesPerPage, apiContext);
			}
		}, rethrowPolicyTemplate);
	}

	public TransactionType[] getSellerTransactions(final int pageNumber, final Integer entriesPerPage, final TimeRange startTimeRange,
			final ApiContext apiContext) {
		return retryUtil.retryTemplate(new Callable<TransactionType[]>() {
			@Override
			public TransactionType[] call() throws Exception {
				TransactionType[] sellerTransactions = ebayCall.getSellerTransactions(pageNumber, entriesPerPage, toTimeFilter(startTimeRange),
						apiContext);
				return sellerTransactions == null ? new TransactionType[0] : sellerTransactions;
			}
		}, rethrowPolicyTemplate);
	}

	public ItemType[] getSellerEvents(final TimeRange modTimeRange, final ApiContext apiContext) {
		return retryUtil.retryTemplate(new Callable<ItemType[]>() {
			@Override
			public ItemType[] call() throws Exception {
				GetSellerEventsCall call = new GetSellerEventsCall(apiContext);
				call.setModTimeFilter(toTimeFilter(modTimeRange));
				ItemType[] sellerEvents = call.getSellerEvents();
				return sellerEvents == null ? new ItemType[0] : sellerEvents;
			}
		}, rethrowPolicyTemplate);
	}

	private TimeFilter toTimeFilter(final TimeRange modTimeRange) {
		Calendar timeFrom = createCalendar(modTimeRange.getStartTime());
		Calendar timeTo = createCalendar(modTimeRange.getEndTime());
		TimeFilter modTimeFilter = new TimeFilter(timeFrom, timeTo);
		return modTimeFilter;
	}

	private Calendar createCalendar(long time) {
		Calendar timeFrom = new GregorianCalendar();
		timeFrom.setTimeInMillis(time);
		return timeFrom;
	}
}
