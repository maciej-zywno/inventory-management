package com.ebayinventory.util;

import java.math.BigDecimal;
import java.math.MathContext;

import com.ebayinventory.model.Currency;
import com.ebayinventory.model.Price;

public class PriceUtil {

	public Price createPrice(Currency eur, double price) {
		return new Price(new BigDecimal(price, MathContext.DECIMAL32), eur);
	}

}
