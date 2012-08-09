package com.ebayinventory;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ebayinventory.email.data.EmailAddress;
import com.ebayinventory.model.EbayLogin;
import com.ebayinventory.model.ResourceId;
import com.ebayinventory.model.Token;
import com.ebayinventory.util.DateFormatter;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

@Component
public class Repository {

	private final Logger log = Logger.getLogger(Repository.class);

	private final BiMap<Long, EbayLogin> googleToEbayScheduledUpdateTimeToEbayLogin = HashBiMap.create();
	private final BiMap<Long, EbayLogin> ebayToGoogleScheduledUpdateTimeToEbayLogin = HashBiMap.create();
	private final Map<EbayLogin, Long> lastEbayToGoogleSyncTime = new HashMap<>();

	private final DateFormatter formatter;
	private final RepositoryWriter repositoryWriter;
	private final Map<EbayLogin, Token> ebayLoginToToken;
	private final Map<EbayLogin, ResourceId> ebayLoginToResourceId;
	private final Map<EbayLogin, EmailAddress> ebayLoginToEmailAddress;

	@Autowired
	public Repository(DateFormatter formatter, RepositoryWriter repositoryWriter,
			@Value("#{ebayLoginToToken}") Map<EbayLogin, Token> ebayLoginToToken,
			@Value("#{ebayLoginToResourceId}") Map<EbayLogin, ResourceId> ebayLoginToResourceId,
			@Value("#{ebayLoginToEmailAddress}") Map<EbayLogin, EmailAddress> ebayLoginToEmailAddress) {
		this.formatter = formatter;
		this.repositoryWriter = repositoryWriter;
		this.ebayLoginToToken = ebayLoginToToken;
		this.ebayLoginToResourceId = ebayLoginToResourceId;
		this.ebayLoginToEmailAddress = ebayLoginToEmailAddress;
	}

	// schedule all existing clients update immediately
	@SuppressWarnings("unused")
	@PostConstruct
	private void initScheduledTimesMaps() {
		long now = System.currentTimeMillis();
		for (EbayLogin ebayLogin : ebayLoginToToken.keySet()) {
			googleToEbayScheduledUpdateTimeToEbayLogin.put(now, ebayLogin);
			ebayToGoogleScheduledUpdateTimeToEbayLogin.put(now, ebayLogin);
			// assume last sync was a long time ago (time=0)
			lastEbayToGoogleSyncTime.put(ebayLogin, 0l);
		}

	}

	public Pair<Long, EbayLogin> getSoonestScheduledGoogleToEbayUpdate() {
		Entry<Long, EbayLogin> firstEntry = googleToEbayScheduledUpdateTimeToEbayLogin.entrySet().iterator().next();
		Pair<Long, EbayLogin> pair = new ImmutablePair<Long, EbayLogin>(firstEntry.getKey(), firstEntry.getValue());
		return pair;
	}

	public Pair<Long, EbayLogin> getSoonestScheduledEbayToGoogleUpdate() {
		Entry<Long, EbayLogin> firstEntry = ebayToGoogleScheduledUpdateTimeToEbayLogin.entrySet().iterator().next();
		Pair<Long, EbayLogin> pair = new ImmutablePair<Long, EbayLogin>(firstEntry.getKey(), firstEntry.getValue());
		return pair;
	}

	public void scheduleNextGoogleToEbaySyncTime(EbayLogin ebayLogin, long nextSyncTime) {
		log.info("scheduled for " + ebayLogin.getEbayLogin() + " = " + nextSyncTime + " (" + formatter.format(nextSyncTime) + ")");
		synchronized (googleToEbayScheduledUpdateTimeToEbayLogin) {
			googleToEbayScheduledUpdateTimeToEbayLogin.inverse().remove(ebayLogin);
			googleToEbayScheduledUpdateTimeToEbayLogin.put(nextSyncTime, ebayLogin);
		}
	}

	public void scheduleNextEbayToGoogleSyncTime(EbayLogin ebayLogin, long nextSyncTime) {
		log.info("scheduled ebay to google sync for " + ebayLogin.getEbayLogin() + " = " + nextSyncTime + " (" + formatter.format(nextSyncTime) + ")");
		synchronized (ebayToGoogleScheduledUpdateTimeToEbayLogin) {
			ebayToGoogleScheduledUpdateTimeToEbayLogin.inverse().remove(ebayLogin);
			ebayToGoogleScheduledUpdateTimeToEbayLogin.put(nextSyncTime, ebayLogin);
		}
	}

	public boolean hasNothingScheduled() {
		return googleToEbayScheduledUpdateTimeToEbayLogin.isEmpty();
	}

	public Token getToken(EbayLogin ebayLogin) {
		synchronized (ebayLoginToToken) {
			return ebayLoginToToken.get(ebayLogin);
		}
	}

	public void putToken(EbayLogin ebayLogin, Token ebaySellerToken) {
		synchronized (ebayLoginToToken) {
			ebayLoginToToken.put(ebayLogin, ebaySellerToken);
			repositoryWriter.storeEbayLoginToToken(ebayLoginToToken);
		}
	}

	public void putResourceId(ResourceId resourceId, EbayLogin ebayLogin) {
		synchronized (ebayLoginToResourceId) {
			ebayLoginToResourceId.put(ebayLogin, resourceId);
			repositoryWriter.storeEbayLoginToResourceId(ebayLoginToResourceId);
		}
	}

	public void putSellerEmail(EmailAddress emailAddress, EbayLogin ebayLogin) {
		synchronized (ebayLoginToEmailAddress) {
			ebayLoginToEmailAddress.put(ebayLogin, emailAddress);
			repositoryWriter.storeEbayLoginToEmailAddress(ebayLoginToEmailAddress);
		}
	}

	public ResourceId getResourceId(EbayLogin ebayLogin) {
		synchronized (ebayLoginToResourceId) {
			return ebayLoginToResourceId.get(ebayLogin);
		}
	}

	public EmailAddress getEmailAddress(EbayLogin ebayLogin) {
		synchronized (ebayLoginToEmailAddress) {
			return ebayLoginToEmailAddress.get(ebayLogin);
		}
	}

	public void putLastEbayToGoogleSyncTime(EbayLogin ebayLogin, long time) {
		lastEbayToGoogleSyncTime.put(ebayLogin, time);
	}

	public long getLastEbayToGoogleSyncTime(EbayLogin ebayLogin) {
		return lastEbayToGoogleSyncTime.get(ebayLogin);
	}

}
