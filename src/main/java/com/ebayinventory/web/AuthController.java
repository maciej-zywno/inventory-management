package com.ebayinventory.web;

import javax.servlet.ServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.ebayinventory.MainService;
import com.ebayinventory.ebay.EbayAuthFacade;
import com.ebayinventory.email.data.EmailAddress;
import com.ebayinventory.model.EbayLogin;
import com.ebayinventory.model.Url;
import com.ebayinventory.web.model.EbaySellerForm;

@Controller
public class AuthController {

	private final Logger log = Logger.getLogger(AuthController.class);

	private final EbayAuthFacade ebayAuthFacade;
	private final MainService service;

	@Autowired
	public AuthController(EbayAuthFacade ebayAuthFacade, MainService service) {
		this.ebayAuthFacade = ebayAuthFacade;
		this.service = service;
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String getForm(Model model) {
		model.addAttribute(new EbaySellerForm());
		return "/index";
	}

	@RequestMapping(value = "/", method = RequestMethod.POST)
	public RedirectView processRequestConsentFormRequest() {
		log.info("/ (requestConsentForm) requested");
		Url signinAndConsentUrl = ebayAuthFacade.getSigninAndConsentUrl();
		log.info("redirecting to '" + signinAndConsentUrl.getUrl() + "'");
		return new RedirectView(signinAndConsentUrl.getUrl());
	}

	@RequestMapping(value = "/addAddress", method = RequestMethod.POST)
	public String processGoogleEmailAddressForm(@RequestParam(value = "gmailAddress") String gmailAddress, @RequestParam(value = "ebaySellerLogin") String ebaySellerLogin) {
		log.info("/ (processGoogleEmailAddressForm) requested for ebay seller +'" + ebaySellerLogin + "' and gmail address '" + gmailAddress + "'");

		service.appAuthorizedAndEmailAddressEntered(new EbayLogin(ebaySellerLogin), new EmailAddress(gmailAddress));

		log.info("showing '" + "spreadsheetShared.jsp" + "'");
		return "spreadsheetCreated";
	}

	@RequestMapping(value = "/auth_auth_thanks", method = RequestMethod.GET)
	public ModelAndView processAuthSuccess(@RequestParam(value = "ebaytkn")/* !!!empty!!! */String ebayToken, @RequestParam(value = "tknexp") String tokenExpirationDate,
			@RequestParam(value = "username") String ebaySellerLogin) {
		log.info("/auth_auth_thanks start");

		ModelAndView modelAndView = new ModelAndView("authSuccess");
		modelAndView.addObject("ebaySellerLogin", ebaySellerLogin);// hidden field

		log.info("/auth_auth_thanks end");
		return modelAndView;
	}

	@RequestMapping(value = "/auth_auth_cancel", method = RequestMethod.GET)
	public String processAuthFailure(@RequestParam(value = "username") String username, ServletRequest request) {
		log.info("/auth_auth_cancel start");
		log.info("/auth_auth_cancel end");
		return "authFailure";
	}

}