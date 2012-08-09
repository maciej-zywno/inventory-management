package com.ebayinventory.ebay;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ebay.sdk.ApiContext;
import com.ebay.sdk.ApiException;
import com.ebay.sdk.SdkException;
import com.ebay.soap.eBLBaseComponents.ItemType;
import com.ebay.soap.eBLBaseComponents.NameValueListArrayType;
import com.ebay.soap.eBLBaseComponents.NameValueListType;
import com.ebayinventory.model.Token;

public class FixItemsWithMissingItemSpecificsMain {

	public static void main(String[] args) throws ApiException, SdkException, Exception {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("spring/main.xml");
		EbaySellerFacade sellerFacade = ctx.getBean(EbaySellerFacade.class);
		ApiContextBuilder apiContextBuilder = ctx.getBean(ApiContextBuilder.class);
		EbayFacade facade = ctx.getBean(EbayFacade.class);
		Token ebaySellerToken = new Token(
				"AgAAAA**AQAAAA**aAAAAA**nYmATw**nY+sHZ2PrBmdj6wVnY+sEZ2PrA2dj6wFk4CoAJiAoQ6dj6x9nY+seQ**hWwBAA**AAMAAA**mgg8D8S3kbQI+hDsbPO+ZsMrkFsg6WsLO5hSLyTIvpwNpx2uR0WQJ1t4xogTiqMQrCdzCCciFi3u0YYNVDbq84IjBardU6H3sBeEGBwET5JKXvPNg2+uCsv1GPXPBdhf9JNnpXvOsTfLWaE6p90Ap6UC7YiWf9U16rJEYy1NJETtP8cCymccC7mCgCYqisKfjEQO0uHNli4LIdjVO0zchIV/XNI/S3o3Fu58Pouc6sE+d1KoBoKmfPFOTfGOLYsNd1GkxeRdjC8EuFw6G1b/J8anwRt9ZM5KBXyIBTQsIEGlXjJqGemsPtqCsHTB7C0u+Zz8DdmWjZYwuXw93k/hMhjZjQomEeKW4j5dTLlMuLdMfxRQldQ19pXNQ3A+Zbv6TH/M0j0QWjbxop3RbDlqqxPWpitbTi9YntCU/mjP+8RrXTM/xrp+JmgPekXpD4yU/oHgGaTTURs29SIL2B9zEjNAQqHZltFegXPpYH55K/Y/PFy23NW9zd0s6qYXS4rlPM+2rh3grvHktVjPr08jINSsTq+ObB+kqwdGCFuiuUfJVPMAo3wWVKSJEEqf56psiDhnqhrrxllM+nPP2bSjTvDQU+7nwzvZNvfQ6F9a3U1aAvNDaKduFI7pdW6HkoOtP02GmVq+GW9Ga8vn+GWaK3+wTuIbcj56sD07O1YSSOi3WTIttZpiE1f9pPuz1tSUbjz8Tfa/1etN4zTIfPvs9vPVbLfhvDHcsQNGtGUFWX5WB5PBE5ihFdcGbOwvtvmf");
		ApiContext apiContext = apiContextBuilder.buildNewWithConcreteApiCredentialEbayToken(ebaySellerToken);
		List<ItemType> itemTypes = sellerFacade.getActiveItemTypes(ebaySellerToken);
		for (ItemType itemType : itemTypes) {
			ItemType newItemType = new ItemType();
			try {
				newItemType.setItemID(itemType.getItemID());
				facade.reviseItem(newItemType, apiContext);
			} catch (RuntimeException e) {
				Throwable cause = e.getCause();
				if (cause.getClass().equals(ApiException.class)) {
					if (itemType.getItemSpecifics() != null) {
						NameValueListType[] oldNameValueList = itemType.getItemSpecifics().getNameValueList();
						NameValueListType[] newNameValueList = new NameValueListType[oldNameValueList.length + 7];
						for (int i = 0; i < oldNameValueList.length; i++) {
							newNameValueList[i] = oldNameValueList[i];
						}
						// Underwear Size
						NameValueListType sizeType = new NameValueListType();
						sizeType.setName("Underwear Size");
						sizeType.setValue(new String[] { "XS", "S", "M", "L", "XL" });
						// Brand
						NameValueListType brandType = new NameValueListType();
						brandType.setName("Brand");
						brandType.setValue(new String[] { "SuperBrand" });
						// Trouser Size
						NameValueListType trouserType = new NameValueListType();
						trouserType.setName("Trouser Size");
						trouserType.setValue(new String[] { "Small" });
						// Hosiery Size
						NameValueListType hosieryType = new NameValueListType();
						hosieryType.setName("Hosiery Size");
						hosieryType.setValue(new String[] { "Small" });
						// Chest Size
						NameValueListType chestType = new NameValueListType();
						chestType.setName("Chest Size");
						chestType.setValue(new String[] { "Small" });
						// Cup Size
						NameValueListType cupType = new NameValueListType();
						cupType.setName("Cup Size");
						cupType.setValue(new String[] { "Small" });
						// Shoe Size
						NameValueListType shoeType = new NameValueListType();
						shoeType.setName("Shoe Size");
						shoeType.setValue(new String[] { "42" });

						newNameValueList[newNameValueList.length - 1] = sizeType;
						newNameValueList[newNameValueList.length - 2] = brandType;
						newNameValueList[newNameValueList.length - 3] = trouserType;
						newNameValueList[newNameValueList.length - 4] = hosieryType;
						newNameValueList[newNameValueList.length - 5] = chestType;
						newNameValueList[newNameValueList.length - 6] = cupType;
						newNameValueList[newNameValueList.length - 7] = shoeType;

						NameValueListArrayType nameValueListArrayType = new NameValueListArrayType();
						nameValueListArrayType.setNameValueList(newNameValueList);
						newItemType.setItemSpecifics(nameValueListArrayType);
					} else {
						NameValueListType[] newNameValueList = new NameValueListType[7];
						// Underwear Size
						NameValueListType sizeType = new NameValueListType();
						sizeType.setName("Underwear Size");
						sizeType.setValue(new String[] { "XS", "S", "M", "L", "XL" });
						// Brand
						NameValueListType brandType = new NameValueListType();
						brandType.setName("Brand");
						brandType.setValue(new String[] { "SuperBrand" });
						// Trouser Size
						NameValueListType trouserType = new NameValueListType();
						trouserType.setName("Trouser Size");
						trouserType.setValue(new String[] { "Small" });
						// Hosiery Size
						NameValueListType hosieryType = new NameValueListType();
						hosieryType.setName("Hosiery Size");
						hosieryType.setValue(new String[] { "Small" });
						// Chest Size
						NameValueListType chestType = new NameValueListType();
						chestType.setName("Chest Size");
						chestType.setValue(new String[] { "Small" });
						// Cup Size
						NameValueListType cupType = new NameValueListType();
						cupType.setName("Cup Size");
						cupType.setValue(new String[] { "Small" });
						// Shoe Size
						NameValueListType shoeType = new NameValueListType();
						shoeType.setName("Shoe Size");
						shoeType.setValue(new String[] { "42" });

						newNameValueList[0] = sizeType;
						newNameValueList[1] = brandType;
						newNameValueList[2] = trouserType;
						newNameValueList[3] = hosieryType;
						newNameValueList[4] = chestType;
						newNameValueList[5] = cupType;
						newNameValueList[6] = shoeType;

						NameValueListArrayType nameValueListArrayType = new NameValueListArrayType();
						nameValueListArrayType.setNameValueList(newNameValueList);
						newItemType.setItemSpecifics(nameValueListArrayType);
					}
					try {
						facade.reviseItem(newItemType, apiContext);
					} catch (RuntimeException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		System.out.println("");
	}

}
