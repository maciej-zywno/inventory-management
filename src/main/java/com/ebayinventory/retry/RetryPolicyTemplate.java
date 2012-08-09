package com.ebayinventory.retry;

import org.springframework.stereotype.Component;

@Component
public interface RetryPolicyTemplate {

	RetryPolicy createRetryPolicy(boolean isLastTry);

}