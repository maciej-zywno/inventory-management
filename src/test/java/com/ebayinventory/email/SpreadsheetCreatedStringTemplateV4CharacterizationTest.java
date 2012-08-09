package com.ebayinventory.email;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupDir;

public class SpreadsheetCreatedStringTemplateV4CharacterizationTest {

	public static void main(String[] args) throws IOException {
		STGroup group = new STGroupDir("template", "UTF-8", '$', '$');
		ST template = group.getInstanceOf("spreadsheetCreated");
		String spreadsheetUrl = "https://docs.google.com/spreadsheet/ccc?key=0AuUzafvBi8nRdHZxSWV3djVvQUNBMHRwUVYza3phQlE#gid=0";
		template.add("spreadsheetUrl", spreadsheetUrl);
		String html = template.render();
		File file = new File("temp.html");
		FileUtils.writeStringToFile(file, html, "UTF-8");
		Desktop.getDesktop().open(file);
	}

}
