package com.ebayinventory.sync;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ebayinventory.Repository;
import com.ebayinventory.WaitUtil;
import com.ebayinventory.model.EbayLogin;

@Component
public class GoogleToEbaySynchronizerThread {

	private final Logger log = Logger.getLogger(GoogleToEbaySynchronizerThread.class);

	private final SyncLock syncLock;
	private final Repository repository;
	private final WaitUtil waitUtils;
	private final GoogleToEbaySynchronizer googleToEbaySynchronizer;

	@Autowired
	public GoogleToEbaySynchronizerThread(SyncLock syncLock, Repository repository, WaitUtil waitUtils, GoogleToEbaySynchronizer googleToEbaySynchronizer) {
		this.syncLock = syncLock;
		this.repository = repository;
		this.waitUtils = waitUtils;
		this.googleToEbaySynchronizer = googleToEbaySynchronizer;
	}

	@PostConstruct
	public void startSyncThread() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				sync();
			}
		}, "GoogleToEbaySynchronizer").start();
	}

	public void sync() {
		while (repository.hasNothingScheduled()) {
			waitUtils.waitMillis(1000);
		}
		// repeat always
		while (true) {
			// [next update time, seller]
			Pair<Long, EbayLogin> pair = null;
			// wait until soonest scheduled google spreadsheet fetch time is after now
			while (!shouldBeRunNow((pair = repository.getSoonestScheduledGoogleToEbayUpdate()).getLeft())) {
				long timeToWait = pair.getLeft() - System.currentTimeMillis();
				if (timeToWait < 0) {
					break;
				} else {
					waitUtils.waitMillis(timeToWait);
				}
			}
			synchronized (syncLock) {
				googleToEbaySynchronizer.executeUpdate(pair.getRight());
			}
		}
	}

	private boolean shouldBeRunNow(Long scheduledTime) {
		boolean shouldRunNow = System.currentTimeMillis() >= scheduledTime;
		return shouldRunNow;
	}

}
