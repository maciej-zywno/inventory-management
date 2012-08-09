package com.ebayinventory.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class ResourceId {

	private final String resourceId;

	public ResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public String getResourceId() {
		return resourceId;
	}

	public String getResourceIdWithoutPrefix() {
		return resourceId.split(":")[1];
	}

	@Override
	public int hashCode() {
		return resourceId.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return resourceId.equals(obj);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
