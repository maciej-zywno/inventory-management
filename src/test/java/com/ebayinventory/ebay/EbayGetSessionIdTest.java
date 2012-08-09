package com.ebayinventory.ebay;

import java.net.URLEncoder;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ebay.sdk.ApiContext;
import com.ebay.sdk.ApiException;
import com.ebay.sdk.SdkException;
import com.ebay.sdk.call.GetSessionIDCall;

public class EbayGetSessionIdTest {

	public static void main(String[] args) throws ApiException, SdkException, Exception {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("spring/ebay-api-context.xml");
		ApiContext apiContext = ctx.getBean(ApiContext.class);

		GetSessionIDCall call1 = new GetSessionIDCall(apiContext);
		call1.setRuName("ostpreis-ostpreis-7137-4-hpoxg");
		System.out.println(URLEncoder.encode(call1.getSessionID(), "UTF-8"));

		GetSessionIDCall call2 = new GetSessionIDCall(apiContext);
		call2.setRuName("ostpreis-ostpreis-7137-4-hpoxg");
		System.out.println(URLEncoder.encode(call2.getSessionID(), "UTF-8"));
		
	}

}
