package com.ebayinventory.model;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class Day implements Comparable<Day> {

	private final int year;
	private final int month;
	private final int day;

	public Day(int year, int month, int day) {
		this.year = year;
		this.month = month;
		this.day = day;
	}

	public int getYear() {
		return year;
	}

	public int getMonth() {
		return month;
	}

	public Integer getDay() {
		return day;
	}

	@Override
	public int compareTo(Day day) {
		if (this.equals(day)) {
			return 0;
		}
		DateTime dateTime = toDateTime(day);
		DateTime thisDateTime = toDateTime(this);
		return dateTime.isAfter(thisDateTime) ? -1 : +1;
	}

	private DateTime toDateTime(Day day) {
		return new DateTime(day.getYear(), day.getMonth(), day.getDay(), 0, 0, 0, 0, DateTimeZone.UTC);
	}

	@Override
	public String toString() {
		return year + "-" + month + "-" + day;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		Day day1 = (Day) obj;
		return day1.getYear() == year && day1.getMonth() == month && day1.getDay() == day;
	}

}
