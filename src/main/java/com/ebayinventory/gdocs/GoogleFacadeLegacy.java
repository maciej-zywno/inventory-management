package com.ebayinventory.gdocs;


public class GoogleFacadeLegacy {

/*	public InputStream getLink(String url1) throws IOException, ServiceException {
		MediaContent mediaContent = new MediaContent();
		mediaContent.setMimeType(new ContentType("text/csv"));
		mediaContent.setUri(url1);
		MediaSource media = docsService.getMedia(mediaContent);
		InputStream inputStream = media.getInputStream();
		return inputStream;
	}
*/
	/*public void insertRows(String docTitle, List<String[]> lines) {
	SpreadsheetEntry spreadsheetEntry = findSpreadsheetEntryByTitle(docTitle);
	WorksheetEntry worksheetEntry = getFirstWorksheetEntry(spreadsheetEntry);
	int nonItemLineCount = 5;// extra lines for headers
	worksheetEntry.setRowCount(lines.size() + nonItemLineCount);
	worksheetEntry.setColCount(maxSize(lines));
	try {
		worksheetEntry.update();
	} catch (IOException e1) {
		throw new RuntimeException(e1);
	} catch (ServiceException e1) {
		throw new RuntimeException(e1);
	}
	CellFeed cellFeed = getFirstWorksheetCellFeed(spreadsheetEntry);
	for (int i = 0; i < lines.size(); i++) {
		String[] cells = lines.get(i);
		for (int j = 0; j < cells.length; j++) {
			try {
				CellEntry cellEntry = new CellEntry(i + 1, j + 1, cells[j]);
				cellFeed.insert(cellEntry);
			} catch (ServiceException | IOException e) {
				throw new RuntimeException(e);
			}
		}

	}
	}*/

	/*private int maxSize(List<String[]> lines) {
		int max = 0;
		for (String[] strings : lines) {
			if (strings.length > max) {
				max = strings.length;
			}
		}
		return max;
	}*/

	/*private WorksheetEntry getFirstWorksheetEntry(SpreadsheetEntry spreadsheetEntry) {
		try {
			return spreadsheetEntry.getWorksheets().get(0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ServiceException e) {
			throw new RuntimeException(e);
		}
	}*/

	/*private CellFeed getFirstWorksheetCellFeed(SpreadsheetEntry spreadsheetEntry) {
		try {
			return spreadsheetService.getFeed(getFirstWorksheetEntry(spreadsheetEntry).getCellFeedUrl(), CellFeed.class);
		} catch (IOException | ServiceException e) {
			throw new RuntimeException(e);
		}
	}*/
	/*public String[][] readLines(SpreadsheetEntry spreadsheetEntry) {
		CellFeed cellFeed = getCellFeed(spreadsheetEntry);
		List<List<String>> arrayList = new ArrayList<>();
		for (int i = 0; i < cellFeed.getRowCount(); i++) {
			arrayList.add(new ArrayList<String>());
		}
		String[][] arrays = new String[cellFeed.getRowCount()][cellFeed.getColCount()];
		for (CellEntry cellEntry : cellFeed.getEntries()) {
			String plainText = cellEntry.getTextContent().getContent().getPlainText();
			arrays[cellEntry.getCell().getRow()][cellEntry.getCell().getCol() - 1] = plainText;
		}

		return arrays;
	}*/

	/*private CellFeed getCellFeed(SpreadsheetEntry spreadsheetEntry) {
		WorksheetEntry worksheetEntry = getFirstWorksheet(spreadsheetEntry);
		CellFeed queryCellFeed = queryCellFeed(buildCellQuery(worksheetEntry.getRowCount(), worksheetEntry.getColCount(), worksheetEntry.getCellFeedUrl()));
		return queryCellFeed;
	}*/

	/*	private CellQuery buildCellQuery(int rowCount, int colCount, URL cellFeedUrl) {
			CellQuery query = new CellQuery(cellFeedUrl);
			query.setMinimumRow(1);
			query.setMaximumRow(rowCount);
			query.setMinimumCol(1);
			query.setMaximumCol(colCount);
			return query;
		}

		private CellFeed queryCellFeed(CellQuery query) {
			try {
				CellFeed feed = spreadsheetService.query(query, CellFeed.class);
				return feed;
			} catch (IOException | ServiceException e) {
				throw new RuntimeException(e);
			}
		}

		private WorksheetEntry getFirstWorksheet(SpreadsheetEntry spreadsheetEntry) {
			try {
				return spreadsheetEntry.getWorksheets().get(0);
			} catch (IOException | ServiceException e) {
				throw new RuntimeException(e);
			}
		}*/

}
