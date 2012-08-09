package com.ebayinventory.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class DayRange {

	private final Day startDay;
	private final Day endDay;

	public DayRange(Day startDay, Day endDay) {
		this.startDay = startDay;
		this.endDay = endDay;
	}

	public Day getStartDay() {
		return startDay;
	}

	public Day getEndDay() {
		return endDay;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
}
