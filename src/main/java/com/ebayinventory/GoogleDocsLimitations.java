package com.ebayinventory;

import org.springframework.stereotype.Component;

@Component
public class GoogleDocsLimitations {

	public boolean exceeded(int maxItemLength, int howManyItems) {
		return maxItemLength > 256 || maxItemLength * howManyItems > 400000;
	}
}
