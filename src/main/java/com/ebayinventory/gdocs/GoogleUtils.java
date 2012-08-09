package com.ebayinventory.gdocs;

import org.springframework.stereotype.Component;

import com.ebayinventory.model.CellPosition;
import com.google.gdata.data.acl.AclEntry;
import com.google.gdata.data.acl.AclRole;
import com.google.gdata.data.acl.AclScope;
import com.google.gdata.data.acl.AclScope.Type;
import com.google.gdata.data.batch.BatchUtils;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;

@Component
public class GoogleUtils {

	public boolean isSuccessResponse(CellFeed batchResponse) {
		// Check the results
		boolean isSuccess = true;
		for (CellEntry entry : batchResponse.getEntries()) {
			// String batchId = BatchUtils.getBatchId(entry);
			if (!BatchUtils.isSuccess(entry)) {
				isSuccess = false;
				// BatchStatus status = BatchUtils.getBatchStatus(entry);
			}
		}
		return isSuccess;
	}

	public AclEntry createAclEntry(String role, Type scope, String email) {
		AclRole aclRole = new AclRole(role);
		AclScope aclScope = new AclScope(scope, email);
		AclEntry entry = new AclEntry();
		entry.setRole(aclRole);
		entry.setScope(aclScope);
		return entry;
	}

	public String asCellEntryId(CellPosition pos) {
		return String.format("R%sC%s", pos.getRow() + 1, pos.getColumn() + 1);
	}

}
