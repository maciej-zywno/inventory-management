package com.ebayinventory.email;

import com.ebayinventory.email.data.EmailMessage;

public interface EmailSender {

	void sendMail(EmailMessage emailMessage);

}