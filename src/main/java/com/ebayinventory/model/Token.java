package com.ebayinventory.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Token {

	private final String token;

	public Token(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	@Override
	public int hashCode() {
		return token.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return token.equals(obj);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
