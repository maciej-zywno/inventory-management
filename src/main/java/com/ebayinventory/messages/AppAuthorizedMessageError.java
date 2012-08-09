package com.ebayinventory.messages;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class AppAuthorizedMessageError implements Serializable {

	private final AppAuthorizedMessage message;
	private final RuntimeException e;

	public AppAuthorizedMessageError(AppAuthorizedMessage message, RuntimeException e) {
		this.message = message;
		this.e = e;
	}

	public AppAuthorizedMessage getMessage() {
		return message;
	}

	public RuntimeException getE() {
		return e;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
