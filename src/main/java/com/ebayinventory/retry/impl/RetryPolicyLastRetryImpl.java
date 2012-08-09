package com.ebayinventory.retry.impl;

import java.io.IOException;

import javax.net.ssl.SSLHandshakeException;

import com.ebay.sdk.SdkException;
import com.ebayinventory.ebay.EbayExceptionUtil;
import com.ebayinventory.retry.RetryPolicy;

public class RetryPolicyLastRetryImpl implements RetryPolicy {

	private final EbayExceptionUtil ebayExceptionUtil;

	public RetryPolicyLastRetryImpl(EbayExceptionUtil ebayExceptionUtil) {
		this.ebayExceptionUtil = ebayExceptionUtil;
	}

	@Override
	public boolean understand(Exception e) {
		return isUnderstandableException(e.getClass());
	}

	@Override
	public RuntimeException wrap(Exception e) {
		return new RuntimeException(e);
	}

	@Override
	public boolean shouldRethrowThisException(Exception exception) {
		if (exception instanceof SdkException) {
			if (ebayExceptionUtil.isNetworkExceptionWeShouldRetry((SdkException) exception)) {
				return true;
			} else {
				return false;
			}
		}
		if (exception instanceof IOException) {
			return true;
		}
		if (exception instanceof com.google.gdata.util.ServiceException) {
			return false;
		}
		return false;
	}

	private boolean isUnderstandableException(Class<? extends Exception> clazz) {
		return clazz.equals(SdkException.class) || clazz.equals(IOException.class) || clazz.equals(com.ebay.sdk.SdkSoapException.class)
				|| clazz.equals(SSLHandshakeException.class);
	}
}