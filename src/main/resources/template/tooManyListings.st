tooManyListings(spreadsheetUrl,howManyListingsSkipped,originalItemsSize,trimmedItemsSize) ::= <<
<div style="font-family:Arial, Helvetica, sans-serif;width:580px;margin:0 10px 15px 10px;color:#3C6995;font-size:13px">
	<p>Finally you can start managing your ebay inventory through the Google Docs spreadsheet:</p>
	<p>
		<a href="$spreadsheetUrl$">$spreadsheetUrl$</a>
	</p>
	<p> 
To make it work, please modify only cells with inventory and. 
<ul>
			<li>do not change any other cells like variation titles or listing numbers</li>
			<li>do not move/copy/delete cells and rows.</li>
</ul>
<p>
Please note that due to Google Docs limitations (<a href="https://support.google.com/docs/bin/answer.py?hl=en&answer=37603">see the limitations details</a>) 
we needed to limit the number of listings inside the spreadsheet. We put $trimmedItemsSize$ listings into the spreadsheet out of $originalItemsSize$
listings that you have. You might also see a "This spreadsheet is about to reach the following limits: Number of Cells" message in Google Docs - please dismiss 
it as we will make sure that this limit will not be exceeded.  
</p>
	<p>
We are working on full inventory management feature set that will also include adding, deleting and modifying listings. 
If you need any specific feature, please do let us know and we will put it on top of our priority list. 
	</p>
	<p>
Thank you,<br />
Maciej Zywno,<br /> 
the founder
	</p>
</div>
>>