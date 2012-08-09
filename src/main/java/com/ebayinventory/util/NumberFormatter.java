package com.ebayinventory.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.springframework.stereotype.Component;

@Component
public class NumberFormatter {

	private final DecimalFormatSymbols symbols = new DecimalFormatSymbols();
	{
		symbols.setDecimalSeparator('.');
	}

	private final DecimalFormat twoDecimalPlacesFormatter = new DecimalFormat("0.00", symbols);
	private final DecimalFormat fourDecimalPlacesFormatter = new DecimalFormat("0.0000", symbols);

	public String formatTwoDecimal(BigDecimal number) {
		return twoDecimalPlacesFormatter.format(number);
	}

	public String formatTwoDecimal(double number) {
		return twoDecimalPlacesFormatter.format(number);
	}

	public String formatFourDecimal(BigDecimal number) {
		return fourDecimalPlacesFormatter.format(number);
	}

}
