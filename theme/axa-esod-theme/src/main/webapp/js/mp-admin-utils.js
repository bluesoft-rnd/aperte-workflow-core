function showPleaseWaitPopup() {
	var popup = $('#pleaseWaitPopup');
	popup.jqm({});
	popup.jqmShow();
}

function hidePleaseWaitPopup() {
	var popup = $('#pleaseWaitPopup');
	popup.jqmHide(); 
}

function showServerErrorPopup() {
	var popup = $('#serverErrorPopup');
	popup.jqm({});
	popup.jqmShow();
}

function createDataTable(tableId,url,columns){
    createDataTable(tableId,url,columns,null);
}

function createDataTable(tableId,url,columns,dataFormId){
    $('#'+tableId).dataTable({
        "bLengthChange": false,
        "bFilter": false,
        "bProcessing": true,
        "bServerSide": true,
        "bInfo": true,
        "sAjaxSource": url,
        "fnServerData": function ( sSource, aoData, fnCallback ) {

            if(dataFormId != null){
                var bean = $('#'+dataFormId).serializeObject();
                for(var key in bean){
                    aoData.push({"name":key,"value":bean[key]});
                }
            }

            $.ajax( {
                "dataType": 'json',
                "type": "POST",
                "url": sSource,
                "data": aoData,
                "success": fnCallback
            } );
        },
        "aoColumns": columns,
        "oLanguage": {
              //todo: uzeleznic tresci od tlumaczen w messages
              "sInfo": "Wyniki od _START_ do _END_ z _TOTAL_",
              "sEmptyTable": "Brak wyników",
              "sInfoEmpty": "Brak wyników",
              "sInfoFiltered": ""

            }
    });
}

function cleanDataTable(tableId) {
	var oSettings = $(tableId).dataTable().fnSettings();
	var iTotalRecords = oSettings.fnRecordsTotal();
	for (i=0; i<=iTotalRecords; i++) {
		$(tableId).dataTable().fnDeleteRow(0, null, true);
	}
}

function showUserBlockButton(blocked, deleted) {
	if ((deleted == null || !deleted) && (blocked == null || (blocked != null && !blocked))) {
		return true;
	}
	return false;
}

function showUserUnblockButton(blocked, deleted) {
	if ((deleted == null || !deleted) && (blocked != null && blocked)) {
		return true;
	}
	return false;
}

function showUserDeleteButton(blocked, deleted) {
	if (deleted == null || (deleted != null && !deleted)) {
		return true;
	}
	return false;
}

function getUserStatusString(blocked, deleted, msgActive, msgBlocked, msgDeleted) {
	if (deleted == null || (deleted != null && !deleted)) {
		if (blocked == null || (blocked != null && !blocked)) {
			return msgActive;
		}
		if (blocked != null && blocked) {
			return msgBlocked;
		}
	}
	if (deleted != null && deleted) {
		return msgDeleted;
	}
	return '!?!';
}

function getUserSexAsString(sex, msgMale, msgFemale) {
	if (sex != null) {
		if ('M' == sex) {
			return msgMale;
		}
		if ('F' == sex) {
			return msgFemale;
		}
	}
	
	return '';
}

function getValueAsString(value) {
	if (value != null) {
		return value;
	}
	
	return '';
}

function millisecondsToDateString(milliseconds) {
	if (milliseconds != null) {
		try {
			return $.format.date(milliseconds, 'yyyy-MM-dd HH:mm:ss')
		} catch (e) {
			return '!?!';
		}
	}
	
	return '';
}

function millisecondsToDateWithoutTimeString(milliseconds) {
	if (milliseconds != null) {
		try {
			return $.format.date(milliseconds, 'yyyy-MM-dd')
		} catch (e) {
			return '!?!';
		}
	}
	
	return '';
}

function getTransactionTypeAsString(type, msgBlock, msgCorrection, msgInnertransfer, msgPayoff, msgPurchase, msgRecharge, msgReturn, msgVoucher) {
	if ('BLOCK' == type) return msgBlock;
	if ('CORRECTION' == type) return msgCorrection;
	if ('INNERTRANSFER' == type) return msgInnertransfer;
	if ('PAYOFF' == type) return msgPayoff;
	if ('PURCHASE' == type) return msgPurchase;
	if ('RECHARGE' == type) return msgRecharge;
	if ('RETURN' == type) return msgReturn;
	if ('VOUCHER' == type) return msgVoucher;
	
	return '!?!'; 
}

function isEmptyString(value) {
	if(value == null || $.trim(value) == '') {
		return true;
	}
	return false;
}

function validateNonEmptyMaxLengthString(value, maxLength) {
	if (isEmptyString(value)) {
		return false;
	}
	if (value.length > maxLength) {
		return false;
	}
	return true;
}

function validateEmptyMaxLengthString(value, maxLength) {
	if (!isEmptyString(value) && value.length > maxLength) {
		return false;
	}
	return true;
}

function validateNonEmptyMaxLengthEmailString(value, maxLength) {
	var pattern = new RegExp(/^((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?$/i);
	if(pattern.test(value) && value.length <= maxLength) {
		return true;
	}
	return false;
}

function validateTransactionAmountValue(value) {
	var pattern = new RegExp(/^[0-9]{0,19}\.?[0-9]{0,4}$/i);
    return value != null && value.length > 0 && pattern.test(value);
}

function validateButtonPriceValue(value) {
	var pattern = new RegExp(/^[0-9]{0,19}\.?[0-9]{0,4}$/i);
    return value != null && value.length > 0 && pattern.test(value);
}

function isLongValue(value) {
	var pattern = new RegExp(/^\d{0,18}$/i);
    return value != null && value.length > 0 && pattern.test(value);
}

function secureInputFields() {
	$('.mp_bigdecimal').keypress(function(event) {
		if(event.which == 0 || event.which == 8) {
		} else if(event.which == 46 && $(this).val().indexOf('.') != -1) {
	        event.preventDefault();
	    } else if(event.which == 44) {
			if ($(this).val().indexOf('.') == -1) {
				$(this).val($(this).val() + '.');
			}
			event.preventDefault();
	    } else if(event.which < 46 || event.which > 59) {
	        event.preventDefault();
	    }
	});
	$('.mp_long').keypress(function(event) {
		if(event.which == 0 || event.which == 8) {
		} else if(event.which < 47 || event.which > 59) {
	        event.preventDefault();
	    }
	});
}

function clearFormValidation(form) {
	$('#' + form + ' td').each(function()
	{
		$(this).removeClass('error');
	});
}

function createNoMessageEffect(form, name) {
	$.tools.validator.addEffect(name, function(errors, event) 
	{
		clearFormValidation(form);
		$.each(errors, function(index, error) 
		{
			error.input.parent('td').addClass('error');
		});
	 
	}, function(inputs) 
	{ 

	});
}

function submitForm(form) {
	$(form).submit();
}
