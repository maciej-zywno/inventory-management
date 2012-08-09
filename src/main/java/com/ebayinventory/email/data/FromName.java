package com.ebayinventory.email.data;

import java.io.Serializable;

public class FromName implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String fromName;
	private final String hiddenFromEmail;

	public FromName(String fromName, String hiddenFromEmail) {
		this.fromName = fromName;
		this.hiddenFromEmail = hiddenFromEmail;
	}

	public String getFromName() {
		return fromName;
	}

	public String getHiddenFromEmail() {
		return hiddenFromEmail;
	}

}
