package com.ebayinventory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ebayinventory.email.data.EmailAddress;
import com.ebayinventory.model.EbayLogin;
import com.ebayinventory.model.ResourceId;
import com.ebayinventory.model.Token;

@Component
public class RepositoryWriter {

	private final String ebayLoginToResourceIdFilePath;
	private final String ebayLoginToEmailAddressFilePath;
	private final String ebayLoginToTokenFilePath;

	@Autowired
	public RepositoryWriter(@Value("${ebayLoginToResourceIdFilePath}") String ebayLoginToResourceIdFilePath,
			@Value("${ebayLoginToEmailAddressFilePath}") String ebayLoginToEmailAddressFilePath,
			@Value("${ebayLoginToTokenFilePath}") String ebayLoginToTokenFilePath) {
		this.ebayLoginToResourceIdFilePath = ebayLoginToResourceIdFilePath;
		this.ebayLoginToEmailAddressFilePath = ebayLoginToEmailAddressFilePath;
		this.ebayLoginToTokenFilePath = ebayLoginToTokenFilePath;
	}

	public void storeEbayLoginToResourceId(Map<EbayLogin, ResourceId> ebayLoginToResourceId) {
		storeProperties(toStringMapFromEbayLoginToResourceIdMap(ebayLoginToResourceId), ebayLoginToResourceIdFilePath);
	}

	public void storeEbayLoginToEmailAddress(Map<EbayLogin, EmailAddress> ebayLoginToEmailAddress) {
		storeProperties(toStringMapFromEbayLoginToEmailAddressMap(ebayLoginToEmailAddress), ebayLoginToEmailAddressFilePath);
	}

	public void storeEbayLoginToToken(Map<EbayLogin, Token> ebayLoginToToken) {
		storeProperties(toStringMapFromEbayLoginToTokenMap(ebayLoginToToken), ebayLoginToTokenFilePath);
	}

	private Map<String, String> toStringMapFromEbayLoginToResourceIdMap(Map<EbayLogin, ResourceId> ebayLoginToResourceId) {
		Map<String, String> stringMap = new HashMap<>();
		for (Entry<EbayLogin, ResourceId> entry : ebayLoginToResourceId.entrySet()) {
			stringMap.put(entry.getKey().getEbayLogin(), entry.getValue().getResourceId());
		}
		return stringMap;
	}

	private Map<String, String> toStringMapFromEbayLoginToEmailAddressMap(Map<EbayLogin, EmailAddress> ebayLoginToEmailAddress) {
		Map<String, String> stringMap = new HashMap<>();
		for (Entry<EbayLogin, EmailAddress> entry : ebayLoginToEmailAddress.entrySet()) {
			stringMap.put(entry.getKey().getEbayLogin(), entry.getValue().getEmail());
		}
		return stringMap;
	}

	private Map<String, String> toStringMapFromEbayLoginToTokenMap(Map<EbayLogin, Token> ebayLoginToToken) {
		Map<String, String> stringMap = new HashMap<>();
		for (Entry<EbayLogin, Token> entry : ebayLoginToToken.entrySet()) {
			stringMap.put(entry.getKey().getEbayLogin(), entry.getValue().getToken());
		}
		return stringMap;
	}

	private void storeProperties(Map<String, String> stringMap, String filePath) {
		Properties props = new Properties();
		props.putAll(stringMap);
		try {
			props.store(new FileOutputStream(filePath), "#");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
