package com.ebayinventory.email.data;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class EmailAddress implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String email;

	public EmailAddress(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
