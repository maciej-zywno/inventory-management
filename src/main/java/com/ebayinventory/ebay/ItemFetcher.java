/*
 * package com.ebayinventory.ebay;
 * 
 * import java.util.Arrays; import java.util.HashMap; import java.util.Map;
 * 
 * import org.apache.commons.lang3.tuple.ImmutablePair; import org.apache.commons.lang3.tuple.Pair;
 * 
 * import com.ebay.sdk.ApiContext; import com.ebay.sdk.ApiException; import com.ebay.sdk.SdkException; import com.ebay.sdk.call.GetItemCall;
 * import com.ebay.sdk.call.GetSearchResultsCall; import com.ebay.soap.eBLBaseComponents.DetailLevelCodeType; import
 * com.ebay.soap.eBLBaseComponents.ItemType; import com.ebay.soap.eBLBaseComponents.PaginationType; import
 * com.ebay.soap.eBLBaseComponents.SearchResultItemType; import com.ebay.soap.eBLBaseComponents.UserIdFilterType;
 * 
 * public class ItemFetcher {
 * 
 * private final ApiContext apiContext;
 * 
 * public ItemFetcher(ApiContext apiContext) { this.apiContext = apiContext; }
 * 
 * public ItemType fetchItem(ItemType item) throws ApiException, SdkException, Exception { GetItemCall call = new GetItemCall(apiContext);
 * DetailLevelCodeType[] detailLevel = createDetailLevelCodeTypes(); call.setDetailLevel(detailLevel); call.setItemID(item.getItemID());
 * return call.getItem(); }
 * 
 * public Map<String, ItemType> fetchItems(String userId) throws ApiException, SdkException, Exception { int pageNumber = 1; Pair<Integer,
 * SearchResultItemType[]> result = null; Map<String, ItemType> itemIdToItemType = new HashMap<>(); while ((result = getResult(apiContext,
 * pageNumber, userId)).getLeft() == pageNumber) { for (SearchResultItemType searchResultItemType : Arrays.asList(result.getRight())) { if
 * (!itemIdToItemType.containsKey(searchResultItemType.getItem().getItemID())) {
 * itemIdToItemType.put(searchResultItemType.getItem().getItemID(), searchResultItemType.getItem()); } } pageNumber++;
 * 
 * int maxPageCount = 10000; if (pageNumber > maxPageCount) { throw new RuntimeException("page count exceeded max " + maxPageCount); } if
 * (pageNumber > 5) { break; } } return itemIdToItemType; }
 * 
 * private Pair<Integer, SearchResultItemType[]> getResult(ApiContext apiContext, int pageNumber, String userId) throws ApiException,
 * SdkException, Exception { GetSearchResultsCall call = createCall(apiContext, pageNumber, userId); SearchResultItemType[] items =
 * call.getSearchResults(); Integer returnedPageNumber = call.getReturnedPageNumber(); return new ImmutablePair<Integer,
 * SearchResultItemType[]>(returnedPageNumber, items); }
 * 
 * private GetSearchResultsCall createCall(ApiContext apiContext, int pageNumber, String userId) { GetSearchResultsCall call = new
 * GetSearchResultsCall(apiContext); call.setQuery("price -porn"); call.setDetailLevel(createDetailLevelCodeTypes());
 * call.setUserIdFilter(createUserIdFilterType(userId)); call.setPagination(createPaginationType(pageNumber)); return call; }
 * 
 * private DetailLevelCodeType[] createDetailLevelCodeTypes() { return new DetailLevelCodeType[] {
 * DetailLevelCodeType.ITEM_RETURN_ATTRIBUTES, DetailLevelCodeType.RETURN_ALL }; }
 * 
 * private UserIdFilterType createUserIdFilterType(String userId) { UserIdFilterType userIdFilter = new UserIdFilterType();
 * userIdFilter.setIncludeSellers(new String[] { userId }); return userIdFilter; }
 * 
 * private PaginationType createPaginationType(int pageNumber) { PaginationType paginationType = new PaginationType();
 * paginationType.setPageNumber(pageNumber); return paginationType; } }
 */