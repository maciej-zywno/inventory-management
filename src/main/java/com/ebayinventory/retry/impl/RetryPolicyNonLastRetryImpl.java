package com.ebayinventory.retry.impl;

import java.io.IOException;
import java.net.ConnectException;

import javax.net.ssl.SSLHandshakeException;

import com.ebay.sdk.SdkException;
import com.ebayinventory.ebay.EbayExceptionUtil;
import com.ebayinventory.retry.RetryPolicy;

public class RetryPolicyNonLastRetryImpl implements RetryPolicy {

	private final EbayExceptionUtil ebayExceptionUtil;

	public RetryPolicyNonLastRetryImpl(EbayExceptionUtil ebayExceptionUtil) {
		this.ebayExceptionUtil = ebayExceptionUtil;
	}

	@Override
	public boolean understand(Exception e) {
		boolean isUnderstandableException = isUnderstandableException(e.getClass());
		boolean isUnderstandableExceptionCause = e.getCause() == null ? false : isUnderstandableException(e.getCause().getClass());
		return isUnderstandableException || isUnderstandableExceptionCause;
	}

	@Override
	public boolean shouldRethrowThisException(Exception exception) {
		if (exception instanceof SdkException) {
			if (ebayExceptionUtil.isNetworkExceptionWeShouldRetry((SdkException) exception)) {
				return false;
			}
		}
		if (exception instanceof IOException) {
			return true;
		}
		if (isUnderstandableException(exception.getClass())) {
			return true;
		}

		return false;
	}

	@Override
	public RuntimeException wrap(Exception e) {
		if (isUnderstandableException(e.getClass())) {
			if (e instanceof RuntimeException) {
				e.printStackTrace();
				System.exit(1);
			}
			throw (RuntimeException) e;
		}
		throw new RuntimeException(e);
	}

	private boolean isUnderstandableException(Class<? extends Throwable> clazz) {
		return clazz.equals(ConnectException.class) || clazz.equals(SdkException.class) || clazz.equals(IOException.class)
				|| clazz.equals(com.ebay.sdk.SdkSoapException.class) || clazz.equals(SSLHandshakeException.class);
	}
}
