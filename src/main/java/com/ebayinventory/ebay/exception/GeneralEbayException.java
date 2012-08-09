package com.ebayinventory.ebay.exception;

public class GeneralEbayException extends EbayException {

	private final String errorCode;

	public GeneralEbayException(String errorCode) {
		this.errorCode = errorCode;
	}

}
