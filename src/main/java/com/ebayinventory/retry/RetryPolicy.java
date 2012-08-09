package com.ebayinventory.retry;

public interface RetryPolicy {

	public abstract boolean shouldRethrowThisException(Exception exception);

	public abstract RuntimeException wrap(Exception e);

	public abstract boolean understand(Exception e);

}