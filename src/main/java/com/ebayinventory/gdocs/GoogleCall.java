package com.ebayinventory.gdocs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.Ostermiller.util.CSVParse;
import com.Ostermiller.util.CSVParser;
import com.ebayinventory.email.data.EmailAddress;
import com.ebayinventory.gdocs.copied.DocumentList;
import com.ebayinventory.gdocs.copied.DocumentListException;
import com.ebayinventory.model.CellPosition;
import com.ebayinventory.model.ResourceId;
import com.google.gdata.client.batch.BatchInterruptedException;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.acl.AclEntry;
import com.google.gdata.data.acl.AclScope.Type;
import com.google.gdata.data.batch.BatchOperationType;
import com.google.gdata.data.batch.BatchUtils;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.media.MediaFileSource;
import com.google.gdata.data.media.MediaSource;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

@Component
public class GoogleCall {

	private final Logger log = Logger.getLogger(GoogleCall.class);

	private final String user;
	private final String password;
	private final String encoding;
	private final GoogleUtils googleUtils;

	private DocsService docsService;
	private DocumentList documentList;
	private SpreadsheetService spreadsheetService;

	@Autowired
	public GoogleCall(@Value("${gmailUser}") String user, @Value("${gmailPassword}") String password, @Value("${encoding}") String encoding,
			GoogleUtils googleUtils) {
		this.user = user;
		this.password = password;
		this.encoding = encoding;
		this.googleUtils = googleUtils;
	}

	@PostConstruct
	public void init() {
		log.info("gmailUser='" + user + "'");
		try {
			this.docsService = new DocsService("");
			this.docsService.setUserCredentials(user, password);
			this.spreadsheetService = new SpreadsheetService("");
			this.spreadsheetService.setUserCredentials(user, password);
			this.spreadsheetService.setProtocolVersion(SpreadsheetService.Versions.V1);
			this.documentList = new DocumentList("appName");
			this.documentList.login(user, password);
		} catch (AuthenticationException e) {
			throw new RuntimeException(e);
		} catch (DocumentListException e) {
			throw new RuntimeException(e);
		}
	}

	public com.google.gdata.data.docs.SpreadsheetEntry addSpreadsheet(final String title) throws MalformedURLException, IOException, ServiceException {
		final com.google.gdata.data.docs.SpreadsheetEntry newEntry = new com.google.gdata.data.docs.SpreadsheetEntry();
		newEntry.setTitle(new PlainTextConstruct(title));
		URL url = new URL("https://docs.google.com/feeds/default/private/full");
		com.google.gdata.data.docs.SpreadsheetEntry spreadsheetEntry = docsService.insert(url, newEntry);
		return spreadsheetEntry;
	}

	public DocumentListEntry uploadFile(final File file, final String title) throws IOException, ServiceException, MalformedURLException,
			DocumentListException {
		return documentList.uploadFile(file, title);
	}

	public DocumentListEntry updateFile(final File file, final DocumentListEntry entry) throws IOException, ServiceException {
		MediaSource mediaSource = new MediaFileSource(file, "text/csv");
		entry.setMediaSource(mediaSource);
		return entry.updateMedia(true);
	}

	public boolean updateFile(final Map<CellPosition, Integer> changesForGdocs, final String resourceIdWithoutPrefix) throws MalformedURLException,
			IOException, ServiceException, BatchInterruptedException {
		URL cellFeedUrl = BatchUpdateUtils.getCellFeedUrl(resourceIdWithoutPrefix);

		CellFeed batchRequestCellFeed = new CellFeed();
		for (Entry<CellPosition, Integer> entry : changesForGdocs.entrySet()) {
			String cellEntryId = googleUtils.asCellEntryId(entry.getKey());
			CellEntry batchEntry = new CellEntry(entry.getKey().getRow() + 1, entry.getKey().getColumn() + 1, cellEntryId);
			batchEntry.setId(String.format("%s/%s", cellFeedUrl, cellEntryId));
			BatchUtils.setBatchId(batchEntry, cellEntryId);
			BatchUtils.setBatchOperationType(batchEntry, BatchOperationType.QUERY);
			batchRequestCellFeed.getEntries().add(batchEntry);
		}
		CellFeed cellFeed = spreadsheetService.getFeed(cellFeedUrl, CellFeed.class);
		CellFeed queryBatchResponse = spreadsheetService.batch(BatchUpdateUtils.getBatchUpdateFeedUrl(cellFeed), batchRequestCellFeed);
		Map<String, CellEntry> cellEntryByBatchIdMap = new HashMap<>();
		for (CellEntry entry : queryBatchResponse.getEntries()) {
			cellEntryByBatchIdMap.put(BatchUtils.getBatchId(entry), entry);
		}

		CellFeed batchRequest = new CellFeed();
		for (Entry<CellPosition, Integer> entry : changesForGdocs.entrySet()) {
			String idString = googleUtils.asCellEntryId(entry.getKey());
			CellEntry cellEntry = cellEntryByBatchIdMap.get(idString);
			CellEntry batchEntry = new CellEntry(cellEntry);
			batchEntry.changeInputValueLocal(Integer.toString(entry.getValue()));
			BatchUtils.setBatchId(batchEntry, idString);
			BatchUtils.setBatchOperationType(batchEntry, BatchOperationType.UPDATE);
			batchRequest.getEntries().add(batchEntry);
		}

		CellFeed batchResponse = spreadsheetService.batch(
				BatchUpdateUtils.getBatchUpdateFeedUrl(spreadsheetService.getFeed(cellFeedUrl, CellFeed.class)), batchRequest);
		boolean successResponse = googleUtils.isSuccessResponse(batchResponse);
		return successResponse;
	}

	public void addRoleWriter(final String title, final EmailAddress ebaySellerGoogleEmail) throws IOException, MalformedURLException, ServiceException,
			DocumentListException {
		ResourceId resourceId = new ResourceId(documentList.findResourceIdByTitle(title));
		addRoleWriter(ebaySellerGoogleEmail, resourceId);
	}

	public void addRoleWriter(final EmailAddress ebaySellerGoogleEmail, ResourceId resourceId) throws IOException, MalformedURLException,
			ServiceException, DocumentListException {
		AclEntry aclEntry = googleUtils.createAclEntry("writer", Type.USER, ebaySellerGoogleEmail.getEmail());
		documentList.addAclRole(aclEntry.getRole(), aclEntry.getScope(), resourceId.getResourceId());
	}

	public void changeRoleToWriter(final EmailAddress ebaySellerGoogleEmail, ResourceId resourceId) throws IOException, MalformedURLException,
			ServiceException, DocumentListException {
		String aclRole = "writer";
		changeAclRole(ebaySellerGoogleEmail, resourceId, aclRole);
	}

	public Boolean changeRoleToReader(final String title, final EmailAddress ebaySellerGoogleEmail) throws IOException, MalformedURLException,
			ServiceException, DocumentListException {
		ResourceId resourceId = new ResourceId(documentList.findResourceIdByTitle(title));
		return changeRoleToReader(ebaySellerGoogleEmail, resourceId);
	}

	public Boolean changeRoleToReader(final EmailAddress ebaySellerGoogleEmail, ResourceId resourceId) throws IOException, ServiceException,
			DocumentListException {
		String aclRole = "reader";
		changeAclRole(ebaySellerGoogleEmail, resourceId, aclRole);
		return true;
	}

	private void changeAclRole(final EmailAddress ebaySellerGoogleEmail, ResourceId resourceId, String aclRole) throws IOException, ServiceException,
			DocumentListException {
		AclEntry aclEntry = googleUtils.createAclEntry(aclRole, Type.USER, ebaySellerGoogleEmail.getEmail());
		documentList.changeAclRole(aclEntry.getRole(), aclEntry.getScope(), resourceId.getResourceId());
	}

	public String[][] readSpreadsheetLines(final ResourceId resourceId, final String format) throws IOException, MalformedURLException,
			ServiceException, DocumentListException, UnsupportedEncodingException {
		CSVParse parser = null;
		try {
			InputStream inputStream = documentList.getInputStream(resourceId.getResourceId(), format);
			// TODO: handle io exception
			Reader spreadsheetInputStream = new InputStreamReader(inputStream, encoding);
			parser = new CSVParser(spreadsheetInputStream);
			String[][] allValues = parser.getAllValues();
			return allValues == null ? new String[0][0] : allValues;
		} finally {
			if (parser != null) {
				try {
					parser.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public void deleteSpreadsheet(ResourceId resourceId) {
		try {
			DocumentListEntry entry = documentList.getDocsListEntry(resourceId.getResourceId());
			String href1 = entry.getEditLink().getHref();
			spreadsheetService.delete(new URL(href1));
			// TODO: com.google.gdata.util.InvalidEntryException: Bad Request
			// Invalid request URI
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ServiceException e) {
			throw new RuntimeException(e);
		} catch (DocumentListException e) {
			throw new RuntimeException(e);
		}
	}

	public DocumentListEntry getEntry(final ResourceId resourceId) throws IOException, MalformedURLException, ServiceException, DocumentListException {
		return documentList.getDocsListEntry(resourceId.getResourceId());
	}
}
