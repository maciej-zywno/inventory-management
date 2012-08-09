package com.ebayinventory.ebay;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ebay.sdk.ApiContext;
import com.ebay.sdk.ApiException;
import com.ebay.sdk.SdkException;
import com.ebay.soap.eBLBaseComponents.ItemType;
import com.ebay.soap.eBLBaseComponents.VariationType;
import com.ebay.soap.eBLBaseComponents.VariationsType;
import com.ebayinventory.model.ItemId;
import com.ebayinventory.model.Token;

public class ReviseExistingItemWithVariationsTest {

	public static void main(String[] args) throws ApiException, SdkException, Exception {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("spring/main.xml");
		EbayFacade facade = ctx.getBean(EbayFacade.class);
		Token ebaySellerToken = new Token(
				"AgAAAA**AQAAAA**aAAAAA**9QZ2Tw**nY+sHZ2PrBmdj6wVnY+sEZ2PrA2dj6wFk4CoAJiAoQ6dj6x9nY+seQ**hWwBAA**AAMAAA**mgg8D8S3kbQI+hDsbPO+ZsMrkFsg6WsLO5hSLyTIvpwNpx2uR0WQJ1t4xogTiqMQrCdzCCciFi3u0YYNVDbq84IjBardU6H3sBeEGBwET5JKXvPNg2+uCsv1GPXPBdhf9JNnpXvOsTfLWaE6p90Ap6UC7YiWf9U16rJEYy1NJETtP8cCymccC7mCgCYqisKfjEQO0uHNli4LIdjVO0zchIV/XNI/S3o3Fu58Pouc6sE+d1KoBoKmfPFOTfGOLYsNd1GkxeRdjC8EuFw6G1b/J8anwRt9ZM5KBXyIBTQsIEGlXjJqGemsPtqCsHTB7C0u+Zz8DdmWjZYwuXw93k/hMhjZjQomEeKW4j5dTLlMuLdMfxRQldQ19pXNQ3A+Zbv6TH/M0j0QWjbxop3RbDlqqxPWpitbTi9YntCU/mjP+8RrXTM/xrp+JmgPekXpD4yU/oHgGaTTURs29SIL2B9zEjNAQqHZltFegXPpYH55K/Y/PFy23NW9zd0s6qYXS4rlPM+2rh3grvHktVjPr08jINSsTq+ObB+kqwdGCFuiuUfJVPMAo3wWVKSJEEqf56psiDhnqhrrxllM+nPP2bSjTvDQU+7nwzvZNvfQ6F9a3U1aAvNDaKduFI7pdW6HkoOtP02GmVq+GW9Ga8vn+GWaK3+wTuIbcj56sD07O1YSSOi3WTIttZpiE1f9pPuz1tSUbjz8Tfa/1etN4zTIfPvs9vPVbLfhvDHcsQNGtGUFWX5WB5PBE5ihFdcGbOwvtvmf");
		ApiContextBuilder apiContextBuilder = ctx.getBean(ApiContextBuilder.class);

		Long itemIdAsLong = 110087722140l;
		ItemId itemId = new ItemId(itemIdAsLong);

		ApiContext apiContext = apiContextBuilder.buildNewWithConcreteApiCredentialEbayToken(ebaySellerToken);

		ItemType item = facade.fetchItem(itemId, apiContext);

		ItemType itemToRevise = new ItemType();
		itemToRevise.setItemID(item.getItemID());
		itemToRevise.setVariations(increaseQty(item.getVariations()));

		facade.reviseItem(itemToRevise, apiContext);
	}

	private static VariationsType increaseQty(VariationsType variationsType) {
		for (VariationType variationType : variationsType.getVariation()) {
			variationType.setQuantity(variationType.getQuantity() + 5);
		}
		return variationsType;
	}

}
