package com.ebayinventory.email.data;

import java.io.Serializable;

public class EmailSubject implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String subject;

	public EmailSubject(String subject) {
		this.subject = subject;
	}

	public String getSubject() {
		return subject;
	}

}
