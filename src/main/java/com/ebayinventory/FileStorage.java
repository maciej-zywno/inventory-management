package com.ebayinventory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.Ostermiller.util.CSVParse;
import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.CSVPrint;
import com.Ostermiller.util.CSVPrinter;
import com.ebayinventory.model.EbayLogin;

@Component
public class FileStorage {

	private final File csvDir;
	private final String encoding;

	@Autowired
	public FileStorage(@Value("${csvDir}") File csvDir, @Value("${encoding}") String encoding) {
		this.csvDir = csvDir;
		this.encoding = encoding;
	}

	public String[][] load(EbayLogin ebayLogin) {
		String fileName = ebayLogin.getEbayLogin() + ".csv";
		return readFromFile(fileName);
	}

	public String createNew(EbayLogin ebayLogin, List<String[]> items) {
		String fileName = ebayLogin.getEbayLogin() + ".csv";
		String[][] array = items.toArray(new String[items.size()][]);
		return writeToFile(fileName, array);
	}

	public String update(EbayLogin ebayLogin, List<String[]> items) {
		String[][] array = items.toArray(new String[items.size()][]);
		return update(ebayLogin, array);
	}

	public String update(EbayLogin ebayLogin, String[][] items) {
		String fileName = ebayLogin.getEbayLogin() + ".csv";
		return writeToFile(fileName, items);
	}

	private String[][] readFromFile(String fileName) {
		try {
			InputStream inputStream = new FileInputStream(new File(csvDir, fileName));
			Reader in = new InputStreamReader(inputStream, encoding);
			CSVParse parser = new CSVParser(in);
			String[][] allValues = parser.getAllValues();
			return allValues == null ? new String[0][0] : allValues;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// returns absolute path to a new file
	private String writeToFile(String fileName, String[][] array) {
		try {
			Writer writer = new OutputStreamWriter(new FileOutputStream(new File(csvDir, fileName)), encoding);
			CSVPrint printer = new CSVPrinter(writer);
			printer.println(array);
			printer.close();
			return new File(csvDir, fileName).getAbsolutePath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
