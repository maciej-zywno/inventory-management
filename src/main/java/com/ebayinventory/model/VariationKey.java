package com.ebayinventory.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class VariationKey {

	private final String variationKey;

	public VariationKey(String variationValue) {
		this.variationKey = variationValue;
	}

	public String getVariationKey() {
		return variationKey;
	}

	@Override
	public int hashCode() {
		return variationKey.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		String variationKey2 = ((VariationKey) obj).getVariationKey();
		String variationKey1 = variationKey;
		boolean equals = variationKey1.equals(variationKey2);
		return equals;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
