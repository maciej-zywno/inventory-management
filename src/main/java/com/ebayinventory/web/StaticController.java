package com.ebayinventory.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class StaticController {

	@RequestMapping(value = "/")
	public String getWelcome() {
		return "index";
	}

	@RequestMapping(value = "/error")
	public String getError() {
		return "error";
	}

	@RequestMapping(value = "/features")
	public String getFeatures() {
		return "features";
	}

	@RequestMapping(value = "/privacyPolicy")
	public String getPrivacyPolicy() {
		return "privacyPolicy";
	}

}