package com.ebayinventory.gdocs;

public class NoSpeadsheetEntryFoundException extends RuntimeException {

	private final String spreadsheetTitle;

	public NoSpeadsheetEntryFoundException(String spreadsheetTitleSearchedFor) {
		super(spreadsheetTitleSearchedFor);
		spreadsheetTitle = spreadsheetTitleSearchedFor;
	}

	public String getSpreadsheetTitle() {
		return spreadsheetTitle;
	}

}
