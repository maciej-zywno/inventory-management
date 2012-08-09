package com.ebayinventory.retry.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ebayinventory.ebay.EbayExceptionUtil;
import com.ebayinventory.retry.RetryPolicy;
import com.ebayinventory.retry.RetryPolicyTemplate;

@Component
public class RetryPolicyTemplateImpl implements RetryPolicyTemplate {

	private final EbayExceptionUtil ebayExceptionUtil;

	@Autowired
	public RetryPolicyTemplateImpl(EbayExceptionUtil ebayExceptionUtil) {
		this.ebayExceptionUtil = ebayExceptionUtil;
	}

	/* For non-last try we do not rethrow any exception */
	/* always rethrow EbayCallException */
	/* always rethrow Exception */
	@Override
	public RetryPolicy createRetryPolicy(boolean isLastTry) {
		if (isLastTry) {
			return new RetryPolicyLastRetryImpl(ebayExceptionUtil);
		} else {
			return new RetryPolicyNonLastRetryImpl(ebayExceptionUtil);
		}
	}

}