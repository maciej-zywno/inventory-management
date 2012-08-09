<jsp:root version="2.0" xmlns="http://www.w3.org/1999/html" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:c="http://java.sun.com/jsp/jstl/core" xmlns:templates="urn:jsptagdir:/WEB-INF/tags/templates" xmlns:form="http://www.springframework.org/tags/form">
	<jsp:directive.page contentType="text/html" />
	<templates:main>
		<jsp:attribute name="content">

			<div class="container">

				<div id="left">
					<div id="slogan-1">
						<div class="slogan-text-1">Manage the inventory with a Google Docs spreadsheet</div>
						<br style="height: 10px;" />
						<div class="slogan-text-2">Update tens of auctions at once by simply modifying your good old Google Docs spreadsheet.</div>
						<div class="slogan-text-2">Tired with keeping your listings variations inventory through ebay website?</div>
						<div class="slogan-text-2">We believe that when it comes to updating an inventory an old Excel file might be a better option 
than a complex multi-page website.</div>
					</div>
					<div>
						<form:form method="post">
							<p>
								<input type="submit" value="I want to use it for free" />
							</p>
						</form:form>
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