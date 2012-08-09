package com.ebayinventory.email;

import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.ebayinventory.email.data.EmailAddress;
import com.ebayinventory.email.data.EmailMessage;

@Component
public class EmailSenderSpringImpl implements EmailSender {

	private final Logger log = Logger.getLogger(EmailSenderSpringImpl.class);

	private final JavaMailSender mailSender;
	private final String encoding;

	@Autowired
	public EmailSenderSpringImpl(JavaMailSender mailSender, @Value("${encoding}") String encoding) {
		this.mailSender = mailSender;
		this.encoding = encoding;
	}

	@Override
	public void sendMail(EmailMessage emailMessage) {
		try {
			System.out.println();
			log.info("building mimeMessage for " + emailMessage.getToEmailAddresses().getTo());
			MimeMessage mimeMessage = createMimeMessage(emailMessage, encoding);
			log.info("sending mimeMessage for " + emailMessage.getToEmailAddresses().getTo());
			mailSender.send(mimeMessage);
		} catch (MailAuthenticationException e) {
			throw new RuntimeException(e);
		} catch (MailSendException e) {
			throw new RuntimeException(e);
		} catch (MailException e) {
			throw new RuntimeException(e);
		}
	}

	private MimeMessage createMimeMessage(EmailMessage email, String encoding) {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, encoding);
			helper.setFrom(email.getFromName().getFromName() + " " + "<" + email.getFromName().getHiddenFromEmail() + ">");
			helper.setTo(email.getToEmailAddresses().getTo().getEmail());
			helper.setReplyTo(email.getReplyToAddress().getEmail());
			helper.setBcc(toArray(email));
			helper.setSubject(email.getSubject().getSubject());
			helper.setText(email.getContent().getHtml(), true);
			return mimeMessage;
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

	private String[] toArray(EmailMessage email) {
		List<EmailAddress> bcc = email.getToEmailAddresses().getBcc();
		return stringView(bcc).toArray(new String[0]);
	}

	private List<String> stringView(List<EmailAddress> addresses) {
		List<String> stringList = new ArrayList<String>();
		for (EmailAddress emailAddress : addresses) {
			stringList.add(emailAddress.getEmail());
		}
		return stringList;
	}

}