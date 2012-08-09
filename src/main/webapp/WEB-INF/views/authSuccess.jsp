<jsp:root version="2.0" xmlns="http://www.w3.org/1999/html" 
xmlns:jsp="http://java.sun.com/JSP/Page" 
xmlns:c="http://java.sun.com/jsp/jstl/core" 
xmlns:templates="urn:jsptagdir:/WEB-INF/tags/templates" 
xmlns:form="http://www.springframework.org/tags/form">

	<jsp:directive.page contentType="text/html" />

	<templates:main>
		<jsp:attribute name="content">

			<div class="container">

				<div id="left">
					<div id="slogan-1">
						<div class="slogan-text-2">Thank you for your interest in our ebay inventory management software.

We are already fetching your active listings and putting the data into a Google Docs spreadsheet. 
This should not take longer than a few minutes depending on how many items you sell. 
Once we're done we will share the spreadsheet with you so that you can actually start using the software. To do this we need your gmail address:</div>
						<br style="height: 10px;" />
					</div>
					<div>
						<form action="/addAddress" method="post">
							<p>
								Your gmail address
								<br />
								<input name="gmailAddress" type="text" value="m.zywno@gmail.com" />
							</p>
							<input type="hidden" name="ebaySellerLogin" value="${ebaySellerLogin}" />
							<p>
								<input type="submit" value="Go" />
							</p>
						</form>
					</div>
				</div>
				<div id="right">
					<div id="youtubeMovie">
						<!--iframe width="470" height="315" src="http://www.youtube.com/embed/us8Vq7cEOtA" frameborder="0" allowfullscreen="false">
						</iframe-->
					</div>
				</div>

			</div>
		</jsp:attribute>
	</templates:main>
</jsp:root>