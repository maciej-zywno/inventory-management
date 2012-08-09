package com.ebayinventory.retry;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ebayinventory.WaitUtil;

@Component
public class RetryUtil {

	private final WaitUtil waitUtil;
	private final int maxTryCount = 10;
	private final int basePeriodInSecondsToWaitBeforeNextTry = 30;

	@Autowired
	public RetryUtil(WaitUtil waitUtil) {
		this.waitUtil = waitUtil;
	}

	public <T> T retryTemplate(Callable<T> callable, RetryPolicyTemplate retryPolicyTemplate) {
		T result = null;
		int tryCount = 0;
		// keep on trying until we get no io exception or we exceeded max number of retries
		while ((result == null || result.equals(false)) && tryCount < maxTryCount) {
			tryCount++;
			// time btw consecutive retries is longer and longer
			int numberOfSecondsToWaitBeforeNextTry = basePeriodInSecondsToWaitBeforeNextTry * (tryCount - 1);
			// for first try we wait 0 seconds
			waitUtil.waitSeconds(numberOfSecondsToWaitBeforeNextTry);
			boolean isLastTry = tryCount == maxTryCount;
			// for last try do not "catch" exception (I mean do rethrow caught io exception)
			result = executeButCatch(callable, retryPolicyTemplate.createRetryPolicy(isLastTry));
		}
		return result;
	}

	// returning null means "can do retry"
	// throwing exception means "cannot do retry"
	// otherwise means "success"
	private <T> T executeButCatch(Callable<T> callable, RetryPolicy retryPolicy) {
		try {
			return callable.call();
		} catch (Exception e) {
			// if retry policy can do anything with an exception then ask it whether the exception should be rethrown or a null should be
			// returned which means that we can trigger a retry (outside this method)
			if (retryPolicy.understand(e)) {
				if (retryPolicy.shouldRethrowThisException(e)) {
					throw retryPolicy.wrap(e);
				} else {
					return null;
				}
			}
			// we rethrow any uncaught exception as runtime exception
			else {
				throw new RuntimeException(e);
			}
		}
	}

}
