package com.ebayinventory.gdocs;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.batch.BatchOperationType;
import com.google.gdata.data.batch.BatchStatus;
import com.google.gdata.data.batch.BatchUtils;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

/**
 * A sample application showing how to efficiently use batch updates with the Spreadsheets API to create new cells.
 * 
 * The specified spreadsheet key will be filled in with 'RnCn' identifier of each cell, up to the {@code MAX_ROWS} and {@code MAX_COLS}
 * constants defined in this class.
 * 
 * Usage: java BatchCellUpdater --username [user] --password [pass] --key [spreadsheet-key]
 * 
 * @author Josh Danziger
 */
public class BatchCellUpdater {

	/** The number of rows to fill in the destination workbook */
	private static final int MAX_ROWS = 75;

	/** The number of columns to fill in the destination workbook */
	private static final int MAX_COLS = 5;

	/**
	 * A basic struct to store cell row/column information and the associated RnCn identifier.
	 */
	private static class CellAddress {
		public final int row;
		public final int col;
		public final String idString;

		/**
		 * Constructs a CellAddress representing the specified {@code row} and {@code col}. The idString will be set in 'RnCn' notation.
		 */
		public CellAddress(int row, int col) {
			this.row = row;
			this.col = col;
			this.idString = String.format("R%sC%s", row, col);
		}
	}

	public static void main(String[] args) throws AuthenticationException, MalformedURLException, IOException, ServiceException {
		args = new String[] { "--username", "errors@finapi.pl", "--password", "errors01", "--key", "0Au6Tq9uDF2nKdFhiVHl5QXAxRnJaYkNqY1Etd1FTQUE" };
		// "spreadsheet:0Au6Tq9uDF2nKdDFjTm1yejJWYVM5TllJZkVuS01sOWc"
		// Command line parsing
		SimpleCommandLineParser parser = new SimpleCommandLineParser(args);
		String username = parser.getValue("username", "user", "u");
		String password = parser.getValue("password", "pass", "p");
		String key = parser.getValue("key");
		boolean help = parser.containsKey("help", "h");

		if (help || username == null || password == null) {
			System.err.print("Usage: java BatchCellUpdater --username [user] --password [pass] --key [spreadsheet-key]\n\n");
			System.exit(1);
		}

		long startTime = System.currentTimeMillis();

		// Prepare Spreadsheet Service
		SpreadsheetService service = new SpreadsheetService("Batch Cell Demo");
		service.setUserCredentials(username, password);
		service.setProtocolVersion(SpreadsheetService.Versions.V1);

		// Build list of cell addresses to be filled in
		List<CellAddress> cellAddresses = new ArrayList<CellAddress>();
		for (int row = 1; row <= MAX_ROWS; ++row) {
			for (int col = 1; col <= MAX_COLS; ++col) {
				cellAddresses.add(new CellAddress(row, col));
			}
		}

		// Prepare the update getCellEntryMap is what makes the update fast.
		URL cellFeedUrl = BatchUpdateUtils.getCellFeedUrl(key);
		Map<String, CellEntry> cellEntryByBatchIdMap = getCellEntryMap(service, cellFeedUrl, cellAddresses);

		CellFeed batchRequest = new CellFeed();
		for (CellAddress cellAddr : cellAddresses) {
			String idString = cellAddr.idString;
			CellEntry cellEntry = cellEntryByBatchIdMap.get(idString);
			CellEntry batchEntry = new CellEntry(cellEntry);
			batchEntry.changeInputValueLocal(idString);
			BatchUtils.setBatchId(batchEntry, idString);
			BatchUtils.setBatchOperationType(batchEntry, BatchOperationType.UPDATE);
			batchRequest.getEntries().add(batchEntry);
		}

		// Submit the update
		URL batchUpdateFeedUrl = BatchUpdateUtils.getBatchUpdateFeedUrl(service.getFeed(cellFeedUrl, CellFeed.class));
		CellFeed batchResponse = service.batch(batchUpdateFeedUrl, batchRequest);

		// Check the results
		boolean isSuccess = true;
		for (CellEntry entry : batchResponse.getEntries()) {
			String batchId = BatchUtils.getBatchId(entry);
			if (!BatchUtils.isSuccess(entry)) {
				isSuccess = false;
				BatchStatus status = BatchUtils.getBatchStatus(entry);
				System.out.printf("%s failed (%s) %s", batchId, status.getReason(), status.getContent());
			}
		}

		System.out.println(isSuccess ? "\nBatch operations successful." : "\nBatch operations failed");
		System.out.printf("\n%s ms elapsed\n", System.currentTimeMillis() - startTime);
	}

	/**
	 * Connects to the specified {@link SpreadsheetService} and uses a batch request to retrieve a {@link CellEntry} for each cell
	 * enumerated in {@code cellAddrs}. Each cell entry is placed into a map keyed by its RnCn identifier.
	 * 
	 * @param ssSvc
	 *            the spreadsheet service to use.
	 * @param cellFeedUrl
	 *            url of the cell feed.
	 * @param cellAddrs
	 *            list of cell addresses to be retrieved.
	 * @return a map consisting of one {@link CellEntry} for each address in {@code cellAddrs}
	 */
	public static Map<String, CellEntry> getCellEntryMap(SpreadsheetService ssSvc, URL cellFeedUrl, List<CellAddress> cellAddrs) throws IOException,
			ServiceException {
		CellFeed batchRequest = new CellFeed();
		for (CellAddress cellId : cellAddrs) {
			CellEntry batchEntry = new CellEntry(cellId.row, cellId.col, cellId.idString);
			batchEntry.setId(String.format("%s/%s", cellFeedUrl.toString(), cellId.idString));
			BatchUtils.setBatchId(batchEntry, cellId.idString);
			BatchUtils.setBatchOperationType(batchEntry, BatchOperationType.QUERY);
			batchRequest.getEntries().add(batchEntry);
		}

		CellFeed cellFeed = ssSvc.getFeed(cellFeedUrl, CellFeed.class);
		CellFeed queryBatchResponse = ssSvc.batch(BatchUpdateUtils.getBatchUpdateFeedUrl(cellFeed), batchRequest);

		Map<String, CellEntry> cellEntryByBatchIdMap = new HashMap<>(cellAddrs.size());
		for (CellEntry entry : queryBatchResponse.getEntries()) {
			cellEntryByBatchIdMap.put(BatchUtils.getBatchId(entry), entry);
			System.out.printf("batch %s {CellEntry: id=%s editLink=%s inputValue=%s\n", BatchUtils.getBatchId(entry), entry.getId(), entry
					.getEditLink().getHref(), entry.getCell().getInputValue());
		}

		return cellEntryByBatchIdMap;
	}
}