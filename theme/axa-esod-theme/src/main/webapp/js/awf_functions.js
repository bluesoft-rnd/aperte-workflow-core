var helpChangerServletAddress = "/aperteworkflow/delegate/help_context";

var enableTabOverride = false;

function hideLoadingMessage(elementName) {
    var element = $("div[name='" + elementName + "']").last();
    if (element) {
        element.css("display", "none");
    }
}

$(document.documentElement).keydown(function (event) {
    var navigableTable = $(".navigable-table");
    if (!navigableTable || navigableTable.length == 0) {
        return;
    }

    if (event.keyCode != 9) {
        return;
    }

    var isShift = event.shiftKey ? true : false;

    var focusedElement = document.activeElement;

    if (hasElementClass(focusedElement, "v-filterselect-input")) {
        focusedElement = focusedElement.parentElement;
    }

    var classes = focusedElement.className.split(' ');
    for (var i = 0, j = classes.length; i < j; i++) {
        var className = classes[i];
        var navigableClassPrefixName = "navigable-rowId-";
        var navigableClassRowNumberPrefixName = "-navigable-columnNumber-";

        var substringStartsOn = className.lastIndexOf(navigableClassPrefixName);
        if (substringStartsOn > -1) {
            var rowNumberStartOn = className.lastIndexOf(navigableClassRowNumberPrefixName);
            var itemId = className.substr(substringStartsOn + navigableClassPrefixName.length, rowNumberStartOn - substringStartsOn - navigableClassPrefixName.length);

            var rowNumberString = className.substr(rowNumberStartOn + navigableClassRowNumberPrefixName.length, className.length - rowNumberStartOn);

            var newRowNumber = parseInt(rowNumberString);

            var mod = isShift ? -1 : 1;
            var limit = isShift ? 0 : 25;

            for (var rowNumber = newRowNumber + mod;
            (rowNumber < 25) && (rowNumber >= 0); rowNumber = rowNumber + mod) {

                var classPrefix = navigableClassPrefixName + itemId + navigableClassRowNumberPrefixName;
                var nextElementClass = classPrefix + rowNumber;

                var nextElements = $("div." + nextElementClass + " input, input." + nextElementClass);

                if (!nextElements || nextElements.length == 0 || nextElements.hasClass("v-readonly")) {
                    continue;
                }

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

function registerCloseHandler(func) {
    window.onbeforeunload = func;
}

function unregisterCloseHandler(func) {
    $(window).unbind('beforeunload', func);
}

function clearCloseHandler() {
    window.onbeforeunload = null;
}

function createCookieWithWindowParameters() {
    var winX = (document.all) ? window.screenLeft : window.screenX;
    var winY = (document.all) ? window.screenTop : window.screenY;
    var winH = $(window).height();
    var winW = $(window).width();
    createCookie('apw_img_view', winX + ',' + winY + ',' + winH + ',' + winW, 360);
}

function createCookie(name, value, days) {
    if (days) {
        var date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        var expires = "; expires=" + date.toGMTString();
    } else var expires = "";
    document.cookie = name + "=" + value + expires + "; path=/";
}

function readCookie(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
    }
    return null;
}


function showEditHelpContextPopup(processDefinitionName, dictionaryId, languageCode, dictionaryItemKey, dictionaryItemValue) {

    AUI().ready('aui-dialog', 'aui-overlay-manager', 'dd-constrain', function (A) {
        var textArea = "<textarea id=\"textArea\" rows=\"11\" cols=\"100\">" + dictionaryItemValue + "</textarea>";

        var dialog1 = new A.Dialog({
            bodyContent: textArea,
            buttons: [{
                text: 'Zapisz',
                handler: function () {
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

                }
            }],
            constrain2view: true,
            draggable: true,
            group: 'default',
            height: 250,
            stack: true,
            title: "Zmień wartość słownika [" + dictionaryItemKey + "]",
            width: 500,
            xy: [200, 50]
        });

        dialog1.render();
    });

    return false;
}

function sendChangeHelpContextRequest(processDefinitionName, dictionaryId, languageCode, dictionaryItemKey, dictionaryItemValue) {
    $.ajax({
        type: "POST",
        url: helpChangerServletAddress,
        data: {
            processDefinitionName: processDefinitionName,
            dictionaryId: dictionaryId,
            languageCode: languageCode,
            dictionaryItemKey: dictionaryItemKey,
            dictionaryItemValue: dictionaryItemValue
        },
        success: function (data) {}
    });
}