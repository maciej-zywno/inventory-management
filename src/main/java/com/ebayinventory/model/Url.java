package com.ebayinventory.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Url {

	private final String url;

	public Url(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
