package com.ebayinventory.ebay;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ebay.sdk.ApiContext;
import com.ebay.sdk.ApiCredential;
import com.ebay.sdk.eBayAccount;
import com.ebayinventory.model.Token;

@Component
public class ApiContextBuilder {

	@Autowired
	private ApiContext ebayApiContextTemplate;

	// public ApiContextBuilder(ApiContext ebayApiContextTemplate) {
	// this.ebayApiContextTemplate = ebayApiContextTemplate;
	// }

	public ApiContext buildNewWithConcreteApiCredentialEbayToken(Token ebaySellerToken) {
		ApiContext ctx = new ApiContext();
		ctx.setApiCredential(buildNewBasedOnTemplateAndToken(ebaySellerToken, ebayApiContextTemplate.getApiCredential()));
		ctx.setApiLogging(ebayApiContextTemplate.getApiLogging());
		ctx.setApiServerUrl(ebayApiContextTemplate.getApiServerUrl());
		ctx.setSite(ebayApiContextTemplate.getSite());
		ctx.setErrorLanguage(ebayApiContextTemplate.getErrorLanguage());
		return ctx;
	}

	public ApiContext buildNew() {
		ApiContext ctx = new ApiContext();
		ctx.setApiCredential(buildNewBasedOnTemplate(ebayApiContextTemplate.getApiCredential()));
		ctx.setApiLogging(ebayApiContextTemplate.getApiLogging());
		ctx.setApiServerUrl(ebayApiContextTemplate.getApiServerUrl());
		ctx.setSite(ebayApiContextTemplate.getSite());
		ctx.setErrorLanguage(ebayApiContextTemplate.getErrorLanguage());
		return ctx;
	}

	private ApiCredential buildNewBasedOnTemplateAndToken(Token ebaySellerToken, ApiCredential apiCredentialTemplate) {
		ApiCredential apiCredential = new ApiCredential();
		apiCredential.setApiAccount(apiCredentialTemplate.getApiAccount());
		apiCredential.seteBayToken(ebaySellerToken.getToken());
		return apiCredential;
	}

	private ApiCredential buildNewBasedOnTemplate(ApiCredential apiCredentialTemplate) {
		ApiCredential apiCredential = new ApiCredential();
		apiCredential.setApiAccount(apiCredentialTemplate.getApiAccount());
		apiCredential.seteBayToken(apiCredential.geteBayToken());
		apiCredential.seteBayAccount(new eBayAccount(apiCredential.geteBayAccount().getUsername(), apiCredential.geteBayAccount().getPassword()));
		return apiCredential;
	}

	public ApiContext buildNewWithConcreteEbayAccount(eBayAccount ebayAccount) {
		ApiContext context = buildNew();
		context.getApiCredential().seteBayAccount(ebayAccount);
		return context;
	}

}
