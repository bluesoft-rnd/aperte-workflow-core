AUI().ready(

	/*
	This function gets loaded when all the HTML, not including the portlets, is
	loaded.
	*/

	function() {
		//Dropdown menu:
		sfHover = function() {
		var sfEls = document.getElementById("navbar").getElementsByTagName("li");
		for (var i=0; i<sfEls.length; i++) {
			sfEls[i].onmouseover=function() {
				this.className+=" hover";
		}
			sfEls[i].onmouseout=function() {
				this.className=this.className.replace(new RegExp(" hover\\b"), "");
		}
	}
}
if (window.attachEvent) window.attachEvent("onload", sfHover);
	}
);

Liferay.Portlet.ready(

	/*
	This function gets loaded after each and every portlet on the page.

	portletId: the current portlet's id
	node: the Alloy Node object of the current portlet
	*/

	function(portletId, node) {
	}
);

Liferay.on(
	'allPortletsReady',
	/*
	This function gets loaded when everything, including the portlets, is on
	the page.
	*/

	function() {
	}
);