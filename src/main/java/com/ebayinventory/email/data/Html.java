package com.ebayinventory.email.data;

import java.io.Serializable;

public class Html implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String html;

	public Html(String html) {
		this.html = html;
	}

	public String getHtml() {
		return html;
	}

}
