package com.ebayinventory.gdocs;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import com.ebayinventory.gdocs.copied.DocumentListException;
import com.google.gdata.client.Service;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.ServiceException;

public class InsertCellsMain {

	public static void main(String[] args) throws IOException, ServiceException, DocumentListException {

		String user = "m.zywno";
		String password = "";

		SpreadsheetService spreadsheetService = new SpreadsheetService("Document List Demo");
		spreadsheetService.setUserCredentials(user, password);

		SpreadsheetEntry spreadsheetEntry = findSpreadsheetEntryByTitle("ebay-inventory", spreadsheetService);
		String id = spreadsheetEntry.getId();
		System.out.println(id);
		WorksheetEntry worksheetEntry = spreadsheetEntry.getWorksheets().get(0);

		CellFeed firstWorksheetCellFeed = spreadsheetService.getFeed(worksheetEntry.getCellFeedUrl(), CellFeed.class);
		CellFeed cellFeed = firstWorksheetCellFeed;
		for (CellEntry cell : cellFeed.getEntries()) {
			int row = cell.getCell().getRow();
			int column = cell.getCell().getCol();
			System.out.println("[" + row + "," + column + "]");
			CellEntry newCellEntry = new CellEntry(1, 3, "dupa");
			cellFeed.insert(newCellEntry);
			// cell.setCanEdit(true);
			// cellFeed.setCanPost(true);
			// cell.getCell().withNewInputValue("bar1");
			// cell.changeInputValueLocal("R1C3");
			break;
		}
	}

	public static SpreadsheetEntry findSpreadsheetEntryByTitle(String spreadsheetTitleSearchedFor, Service spreadsheetService) throws IOException,
			ServiceException {
		URL metafeedUrl = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full");
		SpreadsheetFeed feed = spreadsheetService.getFeed(metafeedUrl, SpreadsheetFeed.class);
		List<SpreadsheetEntry> spreadsheets = feed.getEntries();

		for (SpreadsheetEntry spreadsheetEntry : spreadsheets) {
			String spreadsheetTitle = spreadsheetEntry.getTitle().getPlainText();
			if (spreadsheetTitle.equals(spreadsheetTitleSearchedFor)) {
				return spreadsheetEntry;
			}
		}
		throw new RuntimeException("has not found spreadsheed by title '" + spreadsheetTitleSearchedFor + "'");
	}

}