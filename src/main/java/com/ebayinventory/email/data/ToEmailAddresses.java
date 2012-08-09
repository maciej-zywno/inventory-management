package com.ebayinventory.email.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class ToEmailAddresses implements Serializable {

	private static final long serialVersionUID = 1L;

	private final EmailAddress to;
	private final List<EmailAddress> bcc;

	public ToEmailAddresses(EmailAddress to, List<EmailAddress> bcc) {
		this.to = to;
		this.bcc = bcc;
	}

	public ToEmailAddresses(EmailAddress to) {
		this.to = to;
		this.bcc = Collections.emptyList();
	}

	public EmailAddress getTo() {
		return to;
	}

	public List<EmailAddress> getBcc() {
		return bcc;
	}

}
