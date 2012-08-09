package com.ebayinventory;

import org.springframework.stereotype.Component;

@Component
public class WaitUtil {

	public void waitMillis(long millis) {
		if (millis == 0) {
			return;
		}
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public void waitSeconds(int seconds) {
		waitMillis(seconds * 1000);
	}

}
