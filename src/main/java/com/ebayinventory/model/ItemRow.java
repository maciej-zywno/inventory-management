package com.ebayinventory.model;

import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class ItemRow {

	private final ItemId itemId;
	private final String title;
	private final Price price;
	private final Map<Map<VariationKey, VariationValue>, Integer> quantityPerVariations;
	private final Integer totalQuantity;

	private ItemRow(ItemId itemId, String title, Price price, Integer totalQuantity,
			Map<Map<VariationKey, VariationValue>, Integer> quantityPerVariations) {
		this.itemId = itemId;
		this.title = title;
		this.price = price;
		this.totalQuantity = totalQuantity;
		this.quantityPerVariations = quantityPerVariations;
	}

	public ItemRow(ItemId itemId, String title, Price price, Integer quantity) {
		this(itemId, title, price, quantity, null);
	}

	public ItemRow(ItemId itemId, String title, Price price, Map<Map<VariationKey, VariationValue>, Integer> quantityPerVariations) {
		this(itemId, title, price, null, quantityPerVariations);
	}

	public ItemId getItemId() {
		return itemId;
	}

	public String getTitle() {
		return title;
	}

	public Price getPrice() {
		return price;
	}

	public Integer getTotalQuantity() {
		return totalQuantity;
	}

	public Map<Map<VariationKey, VariationValue>, Integer> getQuantityPerVariations() {
		return quantityPerVariations;
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
