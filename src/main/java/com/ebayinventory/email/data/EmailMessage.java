package com.ebayinventory.email.data;

import java.io.Serializable;

public class EmailMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	private final FromName fromName;
	private final ToEmailAddresses toEmailAddresses;
	private final EmailAddress replyToAddress;
	private final EmailSubject emailSubject;
	private final Html content;

	public EmailMessage(FromName fromName, ToEmailAddresses toEmailAddresses, EmailAddress replyToAddress, EmailSubject emailSubject, Html content) {
		this.fromName = fromName;
		this.toEmailAddresses = toEmailAddresses;
		this.replyToAddress = replyToAddress;
		this.emailSubject = emailSubject;
		this.content = content;
	}

	public FromName getFromName() {
		return fromName;
	}

	public ToEmailAddresses getToEmailAddresses() {
		return toEmailAddresses;
	}

	public EmailAddress getReplyToAddress() {
		return replyToAddress;
	}

	public EmailSubject getSubject() {
		return emailSubject;
	}

	public Html getContent() {
		return content;
	}

}
