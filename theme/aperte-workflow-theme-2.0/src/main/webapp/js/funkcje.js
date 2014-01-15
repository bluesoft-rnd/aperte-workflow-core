 // Lista aktualnie przechowywanych skanow, ktore beda mozliwe do wyswietlenia
 var scansList = [];
 var helpChangerServletAddress = "/aperteworkflow/delegate/help_context";
 // okno ze skanami
  var scansWindow = null;
  
  var enableTabOverride = false;
 
 // Funkcja zwraca aktualna liste skanow
 function getCurrentScanList()
 {
	return scansList;	
 } 
 
 function closeScanPage()
 {
	if(!scansWindow)
		return;
		
	scansWindow.close();
 }
 
  function hideLoadingMessage(elementName)
 {
	var element = $("div[name='"+elementName+"']").last();
	if(element)
	{
		element.css("display", "none");
	}
 }
 
$(document.documentElement).keydown(function (event) 
{
	var navigableTable = $(".navigable-table");
	if(!navigableTable || navigableTable.length == 0)
	{
		return;
	}

   if (event.keyCode != 9) 
   {
		return;
   }
   
   var isShift = event.shiftKey ? true : false;
   
   var focusedElement = document.activeElement;
   
   if(hasElementClass(focusedElement, "v-filterselect-input"))
   {
	   /* select ma taka wlasciwosc, ze input nie ma przypisanej klasy w vaadinie w comboboxie */
       focusedElement = focusedElement.parentElement;
   }
   
   var classes = focusedElement.className.split(' ');
   for (var i=0, j=classes.length; i < j; i++) 
   {
		var className = classes[i];
		var navigableClassPrefixName = "navigable-rowId-";
		var navigableClassRowNumberPrefixName = "-navigable-columnNumber-";
		
		
		var substringStartsOn = className.lastIndexOf(navigableClassPrefixName);
		if(substringStartsOn > -1)
		{
			
			var rowNumberStartOn = className.lastIndexOf(navigableClassRowNumberPrefixName);
			var itemId = className.substr(substringStartsOn + navigableClassPrefixName.length, rowNumberStartOn - substringStartsOn - navigableClassPrefixName.length );
			
			var rowNumberString = className.substr(rowNumberStartOn + navigableClassRowNumberPrefixName.length, className.length - rowNumberStartOn);
			
			var newRowNumber = parseInt(rowNumberString);
			
			/* Jak wcisniety shift to lecimy do tylu zamiast do przodu */
			var mod = isShift ? -1 : 1;
			var limit = isShift ? 0 : 25;
			
			for (var rowNumber=newRowNumber+mod;(rowNumber<25) && (rowNumber>=0);rowNumber=rowNumber+mod)
			{ 
			
				var classPrefix = navigableClassPrefixName + itemId + navigableClassRowNumberPrefixName;
				var nextElementClass = classPrefix + rowNumber;
				
				var nextElements = $("div."+nextElementClass+" input, input."+nextElementClass);
				
				if(!nextElements || nextElements.length == 0 || nextElements.hasClass("v-readonly"))
				{
					continue;
				}
				
				/* Usun zolta poswiate usuwajac focus ze starego elementu vaadinowi */
				focusedElement.blur();
				
				event.preventDefault();
				
				nextElements.focus();
				
				return;
				
			}
						
		}
   }
});

function hasElementClass(element, cls) {
    var r = new RegExp('\\b' + cls + '\\b');
    return r.test(element.className);
}
 

function openScanPage(urls)
{	
	scansList = urls.split(",");	
	var val = readCookie('apw_img_view');
	var parameters = "resizable=1";
	if(val != null)
	{
		var cookieValue = val.split(',');
		
		var winX = cookieValue[0];
		var winY = cookieValue[1];
		var winH = cookieValue[2];
		var winW = cookieValue[3];
		parameters = parameters+",width="+winW+",height="+winH+",top="+ winY + ",left="+winX+"";
	}
	
	// jezeli okno nie jest juz otworzone, otworz je w odpowiedniej pozycji
	if(scansWindow == null || !scansWindow || scansWindow.closed)
	{		
		scansWindow = window.open("/axa-esod-theme/html/scansViewer.html","_blank", parameters );
		if(scansWindow !=null)
			scansWindow.focus();
	}
	else
	{
		scansWindow.initScansInWindow();
	}
}

function registerCloseHandler(func) {
	window.onbeforeunload = func;
}

function unregisterCloseHandler(func) {
	$(window).unbind('beforeunload', func);
}

function clearCloseHandler() {
	window.onbeforeunload = null;
}

 // Metoda tworzy cookie z zapisanymi wlasciwosciami okna, zwiazanymi z polozeniem
 function createCookieWithWindowParameters()
 {
	var winX = (document.all)?window.screenLeft:window.screenX;
	var winY = (document.all)?window.screenTop:window.screenY;
	var winH = $(window).height();
	var winW = $(window).width();
	createCookie('apw_img_view', winX+','+winY+','+winH+','+winW,360);		
 }
 
 function createCookie(name,value,days) {
	if (days) {
		var date = new Date();
		date.setTime(date.getTime()+(days*24*60*60*1000));
		var expires = "; expires="+date.toGMTString();
	}
	else var expires = "";
	document.cookie = name+"="+value+expires+"; path=/";
}

function readCookie(name) {
	var nameEQ = name + "=";
	var ca = document.cookie.split(';');
	for(var i=0;i < ca.length;i++) {
		var c = ca[i];
		while (c.charAt(0)==' ') c = c.substring(1,c.length);
		if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
	}
	return null;
}

 // Funkcja laduje obrazek ze wskazanego adresu i zeruje aktualny zoom
 function loadImage(url)
 {
	// zaladuj nowego source do wyswietalnego obrazka

	// usun stary obrazek
	var imageView = $("#imgeview");
	$("#imgeview > img").remove();
	
	// dodaj nowy, z odswiezonym width i height
	var newImg = new Image();
	newImg.id = "scan_view";
	
	// dodaj ten obrazek do widoku
	newImg.onload = function() 
	{
		imageView.append(newImg);
		 if ($("#imgeview > img").height() > 0) {
		   // resetuj ustawienia obrazka
			base_w = 0;
			base_h = 0;
			zoom = 1.0;
			changeZoomToFit();
		} else {
			$("#imgeview > img").load(function() {  
				// resetuj ustawienia obrazka
				base_w = 0;
				base_h = 0;		
				// wyzeruj zoom
				zoom = 1.0;
				changeZoomToFit();
			});  
		}
	};
	
	newImg.src = url;
	

 }

  function registerFile()
 {
	// pierwszy argument to nazwa pod jaka ma sie otworzyc okienko
	var name = arguments[0];
	document.title = name;
	
	// otworz pierwszy plik z listy
	currentImage = 0;
	
	var scans = arguments[1];

	var firstScan = scans[0];
	if(firstScan)
		loadImage(firstScan);
	
 } 
 
 function changeZoom(zoom, img, base_w, base_h)
 {
	if(base_w == 0)
		base_w = img.width();
		
	if(base_h == 0)
		base_h = img.height();
		
	var  w =  base_w * zoom;
	var  h =  base_h * zoom;
	img.width(w);
	img.height(h);
		
 }

 
 function showEditHelpContextPopup(processDefinitionName, dictionaryId, languageCode, dictionaryItemKey, dictionaryItemValue)
 {	
 
    AUI().ready('aui-dialog', 'aui-overlay-manager', 'dd-constrain', function(A) 
	{
	
	var textArea = "<textarea id=\"textArea\" rows=\"11\" cols=\"100\">"+dictionaryItemValue+"</textarea>";
	

	
	var dialog1 = new A.Dialog({
		bodyContent: textArea, 
		buttons: [ { 
			text: 'Zapisz', handler: function() 
			{ 
			    var textAreaObject = document.getElementById("textArea");
				var newValue = textAreaObject.value;
				sendChangeHelpContextRequest(processDefinitionName, dictionaryId, languageCode, dictionaryItemKey, newValue);
				
				var dialog = new A.Dialog({
					title: 'DISPLAY CONTENT',
					centered: true,
					modal: true,
					width: 350,
					height: 50,
					bodyContent: "Polecenie zmiany słownika zostało zakolejkowane..."
					}).render();
				
			} } ], 
		constrain2view: true, 
		draggable: true, 
		group: 'default', 
		height: 250, 
		stack: true, 
		title: "Zmień wartość słownika ["+dictionaryItemKey+"]", 
		width: 500 ,
		xy: [200, 50]
	});
		
	dialog1.render();
	});
	
	return false;
 }
 
function sendChangeHelpContextRequest(processDefinitionName, dictionaryId, languageCode, dictionaryItemKey, dictionaryItemValue)
{
	$.ajax({
	  type: "POST",
	  url: helpChangerServletAddress,
	  data: 
	  {	
		processDefinitionName: processDefinitionName,
		dictionaryId: dictionaryId,
		languageCode: languageCode,
		dictionaryItemKey: dictionaryItemKey,
		dictionaryItemValue: dictionaryItemValue
	  },
	  success: function(data)
	  {
	  }
	});
}

