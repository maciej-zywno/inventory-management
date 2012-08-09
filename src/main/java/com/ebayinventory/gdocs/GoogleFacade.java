package com.ebayinventory.gdocs;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ebayinventory.Repository;
import com.ebayinventory.email.data.EmailAddress;
import com.ebayinventory.model.CellPosition;
import com.ebayinventory.model.EbayLogin;
import com.ebayinventory.model.ResourceId;
import com.ebayinventory.retry.RetryPolicyTemplate;
import com.ebayinventory.retry.RetryUtil;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.util.VersionConflictException;

@Component
public class GoogleFacade {

	private final Logger log = Logger.getLogger(GoogleFacade.class);

	private final RetryUtil retryUtil;
	private final RetryPolicyTemplate retryPolicyTemplate;
	private final GoogleCall googleCall;
	private final Repository repository;

	@Autowired
	public GoogleFacade(RetryUtil retryUtil, RetryPolicyTemplate rethrowPolicyTemplate, GoogleCall googleCall, Repository repository) {
		this.retryUtil = retryUtil;
		this.retryPolicyTemplate = rethrowPolicyTemplate;
		this.googleCall = googleCall;
		this.repository = repository;
	}

	public com.google.gdata.data.docs.SpreadsheetEntry addSpreadsheet(final String title) {
		log.info("adding spreadsheet");
		return retryUtil.retryTemplate(new Callable<com.google.gdata.data.docs.SpreadsheetEntry>() {
			@Override
			public com.google.gdata.data.docs.SpreadsheetEntry call() throws Exception {
				return googleCall.addSpreadsheet(title);
			}

		}, retryPolicyTemplate);
	}

	public DocumentListEntry uploadFile(final File file, final String title) {
		Callable<DocumentListEntry> callable = new Callable<DocumentListEntry>() {
			@Override
			public DocumentListEntry call() throws Exception {
				return googleCall.uploadFile(file, title);
			}

		};
		return retryUtil.retryTemplate(callable, retryPolicyTemplate);
	}

	public void updateFile(final File file, final ResourceId resourceId) {
		final DocumentListEntry entry = getEntry(resourceId);
		Callable<DocumentListEntry> callable = new Callable<DocumentListEntry>() {
			@Override
			public DocumentListEntry call() throws Exception {
				return googleCall.updateFile(file, entry);
			}

		};
		retryUtil.retryTemplate(callable, retryPolicyTemplate);
	}

	public void updateFile(final Map<CellPosition, Integer> changesForGdocs, final String resourceIdWithoutPrefix) {
		Callable<Boolean> callable = new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return googleCall.updateFile(changesForGdocs, resourceIdWithoutPrefix);
			}

		};
		retryUtil.retryTemplate(callable, retryPolicyTemplate);
	}

	public void addWriter(final String title, final EmailAddress ebaySellerGoogleEmail) {
		Callable<Boolean> callable = new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				try {
					googleCall.addRoleWriter(title, ebaySellerGoogleEmail);
				} catch (VersionConflictException e) {
					// success
					return true;
				}
				return true;
			}

		};
		retryUtil.retryTemplate(callable, retryPolicyTemplate);
	}

	public void removeWriter(final String title, final EmailAddress ebaySellerGoogleEmail) {
		Callable<Boolean> callable = new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return googleCall.changeRoleToReader(title, ebaySellerGoogleEmail);
			}

		};
		retryUtil.retryTemplate(callable, retryPolicyTemplate);
	}

	public String[][] readSpreadsheetLines(final ResourceId resourceId, final String format) {
		Callable<String[][]> callable = new Callable<String[][]>() {
			@Override
			public String[][] call() throws Exception {
				return googleCall.readSpreadsheetLines(resourceId, format);
			}

		};
		return retryUtil.retryTemplate(callable, retryPolicyTemplate);
	}

	public void deleteSpreadsheet(final ResourceId resourceId) {
		Callable<Boolean> callable = new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				googleCall.deleteSpreadsheet(resourceId);
				return true;
			}
		};
		retryUtil.retryTemplate(callable, retryPolicyTemplate);
	}

	public void blockUI(EbayLogin ebayLogin) {
		EmailAddress emailAddress = repository.getEmailAddress(ebayLogin);
		ResourceId resourceId = repository.getResourceId(ebayLogin);
		blockUI(emailAddress, resourceId);
	}

	public void blockUI(final EmailAddress emailAddress, final ResourceId resourceId) {
		Callable<Boolean> callable = new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				googleCall.changeRoleToReader(emailAddress, resourceId);
				return true;
			}
		};
		retryUtil.retryTemplate(callable, retryPolicyTemplate);
	}

	public void unblockUI(EbayLogin ebayLogin) {
		EmailAddress emailAddress = repository.getEmailAddress(ebayLogin);
		ResourceId resourceId = repository.getResourceId(ebayLogin);
		unblockUI(emailAddress, resourceId);
	}

	private void unblockUI(final EmailAddress emailAddress, final ResourceId resourceId) {
		Callable<Boolean> callable = new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				googleCall.changeRoleToWriter(emailAddress, resourceId);
				return true;
			}
		};
		retryUtil.retryTemplate(callable, retryPolicyTemplate);
	}

	public DocumentListEntry getEntry(final ResourceId resourceId) {
		return retryUtil.retryTemplate(new Callable<DocumentListEntry>() {
			@Override
			public DocumentListEntry call() throws Exception {
				return googleCall.getEntry(resourceId);
			}

		}, retryPolicyTemplate);
	}

}
