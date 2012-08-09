package com.ebayinventory.ebay;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.stereotype.Component;

import com.ebay.sdk.ApiException;
import com.ebay.sdk.SdkException;

@Component
public class EbayExceptionUtil {

	public boolean isGalleryDurationException(ApiException e) {
		boolean equals = "com.ebay.sdk.ApiException: Input data for tag <Item.PictureDetails.GalleryDuration> is invalid or missing. Please check API documentation.".equals(e
				.toString());
		return equals;
	}

	public boolean isPictureUrlInvalidException(SdkException e) {
		Throwable cause = e.getCause();
		String exceptionToString = e.toString();
		if (exceptionToString.contains("Input data for tag <Item.PictureDetails.")) {
			return true;
		}
		if (cause != null && cause.getMessage() != null) {
			boolean contains = cause.getMessage().contains("Input data for tag <Item.PictureDetails.GalleryURL> is invalid or missing. Please check API documentation.");
			return contains;
		} else {
			return false;
		}
	}

	public boolean isNotAllowedToReviseEndedAuctionsException(com.ebay.sdk.ApiException e) {
		return e.getMessage().contains("You are not allowed to revise ended auctions.");
	}

	public boolean isAuctionAlreadyClosedException(com.ebay.sdk.ApiException e) {
		return e.getMessage().equals("The auction has already been closed.");
	}

	public boolean isAuctionDeletedOrHalfSiteOrNotSellerException(com.ebay.sdk.ApiException e) {
		return e.getMessage().equals("This item cannot be accessed because the listing has been deleted, is a Half.com listing, or you are not the seller.");
	}

	public boolean isVariationCannotBeDeletedDuringRestrictedReviseException(SdkException e) {
		return e.getMessage().contains("Variation cannot be deleted during restricted revise.");
	}

	public boolean isAuctionTitleOrDescriptionInViolationOfEBayPolicyException(ApiException e) {
		String message = e.getMessage();
		return message.contains("The title and/or description may contain improper words, or the listing or seller may be in violation");
	}

	public boolean isTitleOrSubtitleCannotBeChangedException(ApiException e) {
		return e.getMessage().contains(
				"The title or subtitle cannot be changed if an auction-style listing has a bid or ends within 12 hours, or a fixed price listing has a pending Best Offer.");
	}

	public boolean isVariationSpecificsNotMatchVariationSpecificsOfVariationsOnItemException(ApiException e) {
		return e.toString().contains("Variation Specifics provided does not match with the variation specifics of the variations on the item.");
	}

	public boolean isNetworkExceptionWeShouldRetry(SdkException exception) {
		boolean isClientTransportationException = isClientTransportationException(exception);
		boolean isSocketTimeoutException = isSocketTimeoutException(exception);
		boolean isOtherNetworkExceptionWeShouldRetry = isOtherNetworkExceptionWeShouldRetry(exception);
		return isClientTransportationException || isSocketTimeoutException || isOtherNetworkExceptionWeShouldRetry;
	}

	private boolean isSocketTimeoutException(SdkException e) {
		return (e.getInnerThrowable() instanceof javax.xml.ws.soap.SOAPFaultException) && e.getInnerThrowable().getMessage().startsWith("java.net.SocketTimeoutException:");
	}

	private boolean isClientTransportationException(SdkException e) {
		return e.getInnerThrowable() instanceof com.sun.xml.ws.client.ClientTransportException;
	}

	private boolean isOtherNetworkExceptionWeShouldRetry(SdkException e) {
		boolean isSdkSoapException = e instanceof com.ebay.sdk.SdkSoapException;
		if (isSdkSoapException) {
			String innerThrowableMessage = e.getInnerThrowable().getMessage();
			if (innerThrowableMessage.equals("java.net.SocketTimeoutException: Read timed out")) {
				return true;
			}
		}
		boolean isWebServiceException = e.getInnerThrowable() instanceof javax.xml.ws.WebServiceException;
		boolean isInnerThrowableMessageNotNull = e.getInnerThrowable().getMessage() != null;
		if (isWebServiceException && isInnerThrowableMessageNotNull) {
			String innerThrowableMessage = e.getInnerThrowable().getMessage();

			String sandboxUnknownHost = "java.net.UnknownHostException: api.sandbox.ebay.com";
			String productionUnknownHost = "java.net.UnknownHostException: api.ebay.com";
			String noRouteToHostHost = "java.net.NoRouteToHostException: No route to host: connect";
			String errorWritingToServer = "java.io.IOException: Error writing to server";
			String gatewayTimeout = "The server sent HTTP status code 504: Gateway Timeout";
			Collection<String> exceptionMessagesWeShouldRetry = Arrays.asList(new String[] { sandboxUnknownHost, productionUnknownHost, noRouteToHostHost, errorWritingToServer,
					gatewayTimeout, "HTTP transport error: java.net.UnknownHostException: api.sandbox.ebay.com",
					"HTTP transport error: java.net.UnknownHostException: api.ebay.com", "The server sent HTTP status code 500: Internal Server Error",
					"HTTP transport error: java.net.ConnectException: Connection timed out: connect" });
			boolean isNetworkException = exceptionMessagesWeShouldRetry.contains(innerThrowableMessage);
			if (!isNetworkException) {
				System.out.println(innerThrowableMessage);
			}
			return isNetworkException;
		} else {
			return false;
		}
	}

	public boolean isVariationsNumberExceedsLimit120Exception(ApiException e) {
		String message = e.getMessage();
		if (message != null) {
			boolean b = message.contains("Variations number exceeds limit 120");
			return b;
		} else {
			return false;
		}
	}
}
