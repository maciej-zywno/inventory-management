package com.ebayinventory.ebay;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ebay.sdk.ApiContext;
import com.ebay.sdk.ApiException;
import com.ebay.sdk.SdkException;
import com.ebay.sdk.eBayAccount;
import com.ebay.sdk.call.FetchTokenCall;

public class EbayFetchTokenTest {

	public static void main(String[] args) throws ApiException, SdkException, Exception {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("spring/ebay-api-context.xml");
		ApiContext apiContext = ctx.getBean(ApiContext.class);

		eBayAccount ebayAccount = new eBayAccount();
		ebayAccount.setUsername("testuser_ostpreis");
		apiContext.getApiCredential().seteBayAccount(ebayAccount);

		FetchTokenCall call = new FetchTokenCall(apiContext);
		call.setSessionID("hWwBAA**5f86ff821360a471da21fcb1ffffffa6");
		// call.setSecretID("rklewjrbczxnbkewifbdse809352hjnfsd");
		String fetchToken = call.fetchToken();

		System.out.println(call.getReturnedToken());
	}

}
