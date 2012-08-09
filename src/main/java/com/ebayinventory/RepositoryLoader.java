package com.ebayinventory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ebayinventory.email.data.EmailAddress;
import com.ebayinventory.model.EbayLogin;
import com.ebayinventory.model.ResourceId;
import com.ebayinventory.model.Token;

@Configuration
public class RepositoryLoader {

	@Bean
	public Map<EbayLogin, Token> ebayLoginToToken(@Value("${ebayLoginToTokenFilePath}") String ebayLoginToTokenFilePath) {
		return toEbayLoginToTokenMap(loadProperties(ebayLoginToTokenFilePath));
	}

	@Bean
	public Map<EbayLogin, ResourceId> ebayLoginToResourceId(@Value("${ebayLoginToResourceIdFilePath}") String ebayLoginToResourceIdFilePath) {
		return toEbayLoginToResourceIdMap(loadProperties(ebayLoginToResourceIdFilePath));
	}

	@Bean
	public Map<EbayLogin, EmailAddress> ebayLoginToEmailAddress(@Value("${ebayLoginToEmailAddressFilePath}") String ebayLoginToEmailAddressFilePath) {
		return toEbayLoginToEmailAddressMap(loadProperties(ebayLoginToEmailAddressFilePath));
	}

	private Map<EbayLogin, ResourceId> toEbayLoginToResourceIdMap(Properties props) {
		Map<EbayLogin, ResourceId> map = new HashMap<>();
		for (Object object : props.keySet()) {
			String key = (String) object;
			String value = (String) props.get(key);
			map.put(new EbayLogin(key), new ResourceId(value));
		}
		return map;
	}

	private Map<EbayLogin, EmailAddress> toEbayLoginToEmailAddressMap(Properties props) {
		Map<EbayLogin, EmailAddress> map = new HashMap<>();
		for (Object object : props.keySet()) {
			String key = (String) object;
			String value = (String) props.get(key);
			map.put(new EbayLogin(key), new EmailAddress(value));
		}
		return map;
	}

	private Map<EbayLogin, Token> toEbayLoginToTokenMap(Properties props) {
		Map<EbayLogin, Token> map = new HashMap<>();
		for (Object object : props.keySet()) {
			String key = (String) object;
			String value = (String) props.get(key);
			map.put(new EbayLogin(key), new Token(value));
		}
		return map;
	}

	private Properties loadProperties(String filePath) {
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(filePath));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return props;
	}

}
