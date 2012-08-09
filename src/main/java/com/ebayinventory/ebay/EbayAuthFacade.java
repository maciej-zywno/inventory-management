package com.ebayinventory.ebay;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ebay.sdk.eBayAccount;
import com.ebayinventory.model.EbayLogin;
import com.ebayinventory.model.SessionId;
import com.ebayinventory.model.Token;
import com.ebayinventory.model.Url;

/**
 * does NOT need seller ebay token
 */
@Component
public class EbayAuthFacade {

	private final Logger log = Logger.getLogger(EbayAuthFacade.class);

	private final ApiContextBuilder apiContextBuilder;
	private final EbayFacade ebayFacade;
	private final String ruName;
	private final String signinAndConsentUrlTemplate;

	private volatile SessionId sessionId;
	private volatile long sessionIdCreationTime;

	private final boolean alwaysGetNewSessionId = true;

	@Autowired
	public EbayAuthFacade(ApiContextBuilder apiContextBuilder, EbayFacade ebayFacade, @Value("${ruName}") String ruName,
			@Value("${signinAndConsentUrlTemplate}") String signinAndConsentUrlTemplate) {
		this.apiContextBuilder = apiContextBuilder;
		this.ebayFacade = ebayFacade;
		this.ruName = ruName;
		this.signinAndConsentUrlTemplate = signinAndConsentUrlTemplate;
	}

	@SuppressWarnings("unused")
	public Url getSigninAndConsentUrl() {
		if (alwaysGetNewSessionId || isSessionIdNullOrOlderThan1Minute()) {
			log.info("sessionId null or too old");
			SessionId sessionIdUrlEncoded = getAndFetchSessionIdIfNeeded();
			String signinAndConsentUrl = signinAndConsentUrlTemplate.replace("${sessionId}", sessionIdUrlEncoded.getSessionId());
			return new Url(signinAndConsentUrl);
		} else {
			log.info("sessionId already available '" + sessionId + "'");
			String signinAndConsentUrl = signinAndConsentUrlTemplate.replace("${sessionId}", sessionId.getSessionId());
			return new Url(signinAndConsentUrl);
		}
	}

	private SessionId getAndFetchSessionIdIfNeeded() {
		if (sessionId == null) {
			this.sessionId = fetchAndSetNewSessionId();
		}
		return this.sessionId;
	}

	private SessionId fetchAndSetNewSessionId() {
		SessionId sessionId = ebayFacade.getSessionId(ruName, apiContextBuilder.buildNew());
		SessionId sessionIdUrlEncoded = utf8Encoded(sessionId);
		log.info("got new session id '" + sessionIdUrlEncoded.getSessionId() + "'");
		return sessionIdUrlEncoded;
	}

	private SessionId utf8Encoded(SessionId sessionId) {
		try {
			return new SessionId(URLEncoder.encode(sessionId.getSessionId(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean isSessionIdNullOrOlderThan1Minute() {
		return sessionId == null && System.currentTimeMillis() - sessionIdCreationTime > 1 * 60 * 1000;
	}

	public Token fetchToken(EbayLogin ebayLogin) {
		SessionId sessionId = getAndFetchSessionIdIfNeeded();
		log.info("executing FetchTokenCall on sessionId '" + sessionId + "' and ebaySellerLogin '" + ebayLogin.getEbayLogin() + "'");
		try {
			return ebayFacade.fetchToken(sessionId, apiContextBuilder.buildNewWithConcreteEbayAccount(createEbayAccount(ebayLogin)));
		} catch (RuntimeException e) {
			if (isExpiredSession(e)) {
				this.sessionId = fetchAndSetNewSessionId();
				return ebayFacade.fetchToken(sessionId, apiContextBuilder.buildNewWithConcreteEbayAccount(createEbayAccount(ebayLogin)));
			} else {
				throw e;
			}
		}
	}

	private boolean isExpiredSession(RuntimeException e) {
		if (e.getCause() != null && e.getCause().getMessage().contains("The SessionID in your request has been expired, please see API doc for the lifetime of a SessionID")) {
			return true;
		} else {
			return false;
		}
	}

	private eBayAccount createEbayAccount(EbayLogin ebayLogin) {
		eBayAccount ebayAccount = new eBayAccount();
		ebayAccount.setUsername(ebayLogin.getEbayLogin());
		return ebayAccount;
	}

}
