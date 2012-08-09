package com.ebayinventory.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class VariationValue implements Comparable<VariationValue> {

	private final String variationValue;

	public VariationValue(String variationValue) {
		this.variationValue = variationValue;
	}

	public String getVariationValue() {
		return variationValue;
	}

	@Override
	public int hashCode() {
		return variationValue.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return variationValue.equals(((VariationValue) obj).getVariationValue());
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@Override
	public int compareTo(VariationValue o) {
		return variationValue.compareTo(o.variationValue);
	}

}
