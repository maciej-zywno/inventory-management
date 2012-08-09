package com.ebayinventory.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class ItemId {

	private final Long itemId;

	public ItemId(Long itemId) {
		this.itemId = itemId;
	}

	public Long getItemId() {
		return itemId;
	}

	public String getItemIdAsString() {
		return Long.toString(itemId);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
