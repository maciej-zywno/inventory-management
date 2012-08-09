package com.ebayinventory.ebay.exception;

public class TokenRevokedEbayException extends EbayException {

	private final String errorCode;

	public TokenRevokedEbayException(String errorCode) {
		this.errorCode = errorCode;
	}

}
