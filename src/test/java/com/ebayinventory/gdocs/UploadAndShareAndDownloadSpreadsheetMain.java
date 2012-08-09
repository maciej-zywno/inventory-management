package com.ebayinventory.gdocs;

import java.io.File;
import java.io.IOException;

import com.ebayinventory.email.data.EmailAddress;
import com.ebayinventory.gdocs.copied.DocumentListException;
import com.ebayinventory.model.ResourceId;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.media.MediaFileSource;
import com.google.gdata.data.media.MediaSource;
import com.google.gdata.util.ServiceException;

public class UploadAndShareAndDownloadSpreadsheetMain {

	public static void main(String[] args) throws IOException, ServiceException, DocumentListException {

		String user = "errors@finapi.pl";
		String password = "errors01";
		GoogleUtils googleUtils = new GoogleUtils();
		GoogleCall googleFacade = new GoogleCall(user, password, "UTF-8", googleUtils);
		googleFacade.init();

		EmailAddress ebaySellerGoogleEmail = new EmailAddress("m.zywno@gmail.com");
		String ebaySellerLogin = "hanatopshop";

		String title = ebaySellerLogin + "-" + System.currentTimeMillis();

		File file = new File("src/test/resources/sample/test.csv");
		DocumentListEntry uploadedEntry = googleFacade.uploadFile(file, title);
		googleFacade.addRoleWriter(title, ebaySellerGoogleEmail);
		ResourceId resourceId = new ResourceId(uploadedEntry.getResourceId());
		String format = "csv";
		String[][] spreadsheetLines = googleFacade.readSpreadsheetLines(resourceId, format);
		for (String[] strings : spreadsheetLines) {
			System.out.println(strings[0]);
		}

		DocumentListEntry downloadedEntry = googleFacade.getEntry(resourceId);
		MediaSource mediaSource = new MediaFileSource(new File("E:/download/2011.01.csv"), "text/csv");
		downloadedEntry.setMediaSource(mediaSource);
		downloadedEntry.updateMedia(true);

		System.out.println(spreadsheetLines);
	}
}
