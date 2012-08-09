package com.ebayinventory.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.springframework.stereotype.Component;

import com.ebayinventory.model.TimeRange;

@Component
public class DateFormatter {

	private final DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.zzz");
	{
		format.setTimeZone(TimeZone.getTimeZone("Europe/Warsaw"));
	}

	public String format(long time) {
		synchronized (format) {
			return format.format(time);
		}
	}

	public String format(TimeRange timeRange) {
		return format(timeRange.getStartTime()) + "-" + format(timeRange.getEndTime());
	}

}
