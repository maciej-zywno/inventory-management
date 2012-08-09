package com.ebayinventory.email;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupDir;

public class StringTemplateV4CharacterizationTest {

	public static void main1(String[] args) throws IOException {
		STGroup group = new STGroupDir("template", "UTF-8", '$', '$');
		ST template = group.getInstanceOf("welcome");
		// template.add("token", "token");
		String html = template.render();
		File file = new File("temp.html");
		FileUtils.writeStringToFile(file, html, "UTF-8");
		Desktop.getDesktop().open(file);
	}

	public static void main(String[] args) throws IOException {
		STGroup group = new STGroupDir("template", "UTF-8", '$', '$');
		ST template = group.getInstanceOf("tooManyListings");
		template.add("spreadsheetUrl", "spreadsheetUrl");
		template.add("howManyListingsSkipped", 8);
		template.add("originalItemsSize", 1548);
		template.add("trimmedItemsSize", 1540);
		
		String html = template.render();
		File file = new File("temp.html");
		FileUtils.writeStringToFile(file, html, "UTF-8");
		Desktop.getDesktop().open(file);
	}

}
