package com.ebayinventory.gdocs;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.gdata.client.spreadsheet.FeedURLFactory;
import com.google.gdata.data.Link;
import com.google.gdata.data.spreadsheet.CellFeed;

public class BatchUpdateUtils {

	public static URL getBatchUpdateFeedUrl(CellFeed cellFeed) throws MalformedURLException {
		return new URL(cellFeed.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM).getHref());
	}

	public static URL getCellFeedUrl(String key) throws MalformedURLException {
		FeedURLFactory urlFactory = FeedURLFactory.getDefault();
		URL cellFeedUrl = urlFactory.getCellFeedUrl(key, "od6", "private", "full");
		return cellFeedUrl;
	}

}
