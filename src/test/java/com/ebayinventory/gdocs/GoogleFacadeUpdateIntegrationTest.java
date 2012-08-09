package com.ebayinventory.gdocs;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ebayinventory.model.CellPosition;
import com.ebayinventory.model.ResourceId;
import com.google.gdata.data.docs.SpreadsheetEntry;

public class GoogleFacadeUpdateIntegrationTest {

	@Test
	public void testCreateAndUpdateIndividualCells() {
		// SETUP
		ApplicationContext ctx = new ClassPathXmlApplicationContext("spring/main.xml");
		GoogleFacade facade = ctx.getBean(GoogleFacade.class);
		SpreadsheetEntry entry = facade.addSpreadsheet("foo");
		ResourceId resourceId = new ResourceId(entry.getResourceId());

		// EXECUTE
		Map<CellPosition, Integer> changesForGdocs = new HashMap<>();
		changesForGdocs.put(new CellPosition(1, 2), 2);
		changesForGdocs.put(new CellPosition(3, 3), 5);
		String resourceIdWithoutPrefix = resourceId.getResourceIdWithoutPrefix();
		facade.updateFile(changesForGdocs, resourceIdWithoutPrefix);
		String format = "csv";
		String[][] lines = facade.readSpreadsheetLines(resourceId, format);

		// VERIFY
		Assert.assertEquals("wrong cell value", "2", lines[0][1]);
		Assert.assertEquals("wrong cell value", "5", lines[2][2]);

		// CLEANUP
		// facade.deleteSpreadsheet(resourceId);

	}
}
