package com.ebayinventory;

import org.springframework.stereotype.Component;

import com.ebayinventory.model.EbayLogin;

@Component
public class SpreadsheetTitleBuilder {

	public String buildSpreadsheetTitle(EbayLogin ebayLogin) {
		return ebayLogin.getEbayLogin() + "-" + System.currentTimeMillis();
	}
}
