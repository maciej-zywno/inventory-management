package com.ebayinventory.messages;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.ebayinventory.email.data.EmailAddress;
import com.ebayinventory.model.EbayLogin;

public class AppAuthorizedMessage implements Serializable {

	private final EbayLogin ebaySellerLogin;
	private final EmailAddress emailAddress;

	public AppAuthorizedMessage(EbayLogin ebaySellerLogin, EmailAddress emailAddress) {
		this.ebaySellerLogin = ebaySellerLogin;
		this.emailAddress = emailAddress;
	}

	public EbayLogin getEbaySellerLogin() {
		return ebaySellerLogin;
	}

	public EmailAddress getEmailAddress() {
		return emailAddress;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
