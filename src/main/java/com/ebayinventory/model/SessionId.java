package com.ebayinventory.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class SessionId {

	private final String sessionId;

	public SessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getSessionId() {
		return sessionId;
	}

	@Override
	public int hashCode() {
		return sessionId.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return sessionId.equals(obj);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
