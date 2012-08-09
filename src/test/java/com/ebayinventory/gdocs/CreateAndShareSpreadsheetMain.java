package com.ebayinventory.gdocs;

import java.io.IOException;

import com.ebayinventory.email.data.EmailAddress;
import com.ebayinventory.gdocs.copied.DocumentListException;
import com.google.gdata.util.ServiceException;

public class CreateAndShareSpreadsheetMain {

	public static void main(String[] args) throws IOException, ServiceException, DocumentListException {

		String user = "errors@finapi.pl";
		String password = "errors01";
		GoogleUtils googleUtils = new GoogleUtils();
		GoogleCall googleFacade = new GoogleCall(user, password, "UTF-8", googleUtils);

		EmailAddress ebaySellerGoogleEmail = new EmailAddress("m.zywno@gmail.com");
		String ebaySellerLogin = "hanatopshop";

		String title = ebaySellerLogin + "-" + System.currentTimeMillis();
		googleFacade.addRoleWriter(title, ebaySellerGoogleEmail);
	}
}
