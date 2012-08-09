package com.ebayinventory.email;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupDir;

import com.ebayinventory.email.data.EmailAddress;
import com.ebayinventory.email.data.EmailMessage;
import com.ebayinventory.email.data.EmailSubject;
import com.ebayinventory.email.data.FromName;
import com.ebayinventory.email.data.Html;
import com.ebayinventory.email.data.ToEmailAddresses;
import com.ebayinventory.model.EbayLogin;
import com.ebayinventory.model.Url;

@Component
public class EmailHtmlBuilder {

	private final Logger log = Logger.getLogger(EmailHtmlBuilder.class);

	private final String templateDirName;// dir inside root of classpath
	private final String encoding;
	private final FromName fromName;
	private final EmailAddress replyToAddress;

	@Autowired
	public EmailHtmlBuilder(@Value("${templateDirPath}") String templateDirPath, @Value("${encoding}") String encoding, @Value("${fromName_Name}") String fromName_Name,
			@Value("${fromName_HiddenEmail}") String fromName_HiddenEmail, @Value("${replyToAddress}") String replyToAddress) {
		this.templateDirName = templateDirPath;
		this.encoding = encoding;
		this.fromName = new FromName(fromName_Name, fromName_HiddenEmail);
		this.replyToAddress = new EmailAddress(replyToAddress);
	}

	public EmailMessage buildWelcomeEmail(EbayLogin ebayLogin, EmailAddress emailAddress) {
		log.info("building email for " + ebayLogin + " to " + emailAddress);
		STGroup group = new STGroupDir(templateDirName, encoding, '$', '$');
		ST template = group.getInstanceOf("welcome");
		String html = template.render();
		Html content = new Html(html);
		ToEmailAddresses toEmailAddresses = new ToEmailAddresses(emailAddress, Arrays.asList(new EmailAddress[] { new EmailAddress("m.zywno@gmail.com") }));
		EmailSubject emailSubject = new EmailSubject("Hello " + ebayLogin.getEbayLogin() + "!");
		return new EmailMessage(fromName, toEmailAddresses, replyToAddress, emailSubject, content);
	}

	public EmailMessage buildSpreadsheetCreatedEmail(String spreadsheetTitle, Url spreadsheetUrl, EmailAddress emailAddress, boolean isTrimmed, int trimmedItemsSize,
			int originalItemsSize) {
		if (isTrimmed) {
			STGroup group = new STGroupDir(templateDirName, encoding, '$', '$');
			ST template = group.getInstanceOf("tooManyListings");
			template.add("spreadsheetUrl", spreadsheetUrl.getUrl());
			template.add("howManyListingsSkipped", spreadsheetUrl.getUrl());
			template.add("originalItemsSize", originalItemsSize);
			template.add("trimmedItemsSize", trimmedItemsSize);
			String html = template.render();
			Html content = new Html(html);
			ToEmailAddresses toEmailAddresses = new ToEmailAddresses(emailAddress, Arrays.asList(new EmailAddress[] { new EmailAddress("m.zywno@gmail.com") }));
			EmailSubject emailSubject = new EmailSubject("Spreadsheet created - Ebay@GoogleDocs!");
			return new EmailMessage(fromName, toEmailAddresses, replyToAddress, emailSubject, content);
		} else {
			STGroup group = new STGroupDir(templateDirName, encoding, '$', '$');
			ST template = group.getInstanceOf("spreadsheetCreated");
			template.add("spreadsheetUrl", spreadsheetUrl.getUrl());
			String html = template.render();
			Html content = new Html(html);
			ToEmailAddresses toEmailAddresses = new ToEmailAddresses(emailAddress, Arrays.asList(new EmailAddress[] { new EmailAddress("m.zywno@gmail.com") }));
			EmailSubject emailSubject = new EmailSubject("Spreadsheet created - Ebay@GoogleDocs!");
			return new EmailMessage(fromName, toEmailAddresses, replyToAddress, emailSubject, content);
		}
	}

}
