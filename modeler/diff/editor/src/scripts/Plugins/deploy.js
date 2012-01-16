if (!ORYX.Plugins) 
    ORYX.Plugins = new Object();

ORYX.Plugins.Deploy = Clazz.extend({
	
    facade: undefined,
	
	construct: function(facade){
		this.facade = facade;
		
		this.facade.offer({
			'name': ORYX.I18N.Deploy.deploy,
			'functionality': this.deploy.bind(this),
			'group': ORYX.I18N.Deploy.group,
			'icon': ORYX.PATH + "images/disk.png",
			'description': ORYX.I18N.Deploy.deployDesc,
			'index': 1,
			'minShape': 0,
			'maxShape': 0
		});
		
	},
	
    processDeploy: function(modelInfo){
		
		if (!modelInfo) {
			return;
		}
		var value = window.document.title || document.getElementsByTagName("title")[0].childNodes[0].nodeValue;
		
		if (value.startsWith("*")){
		   Ext.Msg.alert("TITLE", "Save your model before deploying.").setIcon(Ext.Msg.WARNING).getDialog().setWidth(260).center().syncSize();
		   delete this.deploying;
		   return;
		}
		
        var params = {
       		name: modelInfo.name,
			parent: modelInfo.parent
        };
		  	
		var successFn = function(transport) {
			
  			Ext.Msg.alert(ORYX.I18N.Oryx.title, "Deploy succeeded.").setIcon(Ext.Msg.INFO).getDialog().setWidth(260).center().syncSize();
			delete this.deploying;
						
		}.bind(this);
				
		var failure = function(transport) {
						
			if(transport.status && transport.status === 401) {
				Ext.Msg.alert(ORYX.I18N.Oryx.title, ORYX.I18N.Save.notAuthorized).setIcon(Ext.Msg.WARNING).getDialog().setWidth(260).center().syncSize();
			} else if(transport.status && transport.status === 403) {
					Ext.Msg.alert(ORYX.I18N.Oryx.title, ORYX.I18N.Save.noRights).setIcon(Ext.Msg.WARNING).getDialog().setWidth(260).center().syncSize();
			} else if(transport.statusText === "transaction aborted") {
					Ext.Msg.alert(ORYX.I18N.Oryx.title, ORYX.I18N.Save.transAborted).setIcon(Ext.Msg.WARNING).getDialog().setWidth(260).center().syncSize();
			} else if(transport.statusText === "communication failure") {
					Ext.Msg.alert(ORYX.I18N.Oryx.title, ORYX.I18N.Save.comFailed).setIcon(Ext.Msg.WARNING).getDialog().setWidth(260).center().syncSize();
			} else {
					var msg = transport.responseText;
					if (msg != null) {
					  msg = Ext.decode(msg);
					  msg = msg == null ? ORYX.I18N.Save.failed : msg.message;
					}
					Ext.Msg.alert(ORYX.I18N.Oryx.title, msg).setIcon(Ext.Msg.WARNING).getDialog().setWidth(260).center().syncSize();
			}
						
			delete this.deploying;
						
		}.bind(this);
				
		this.sendDeployRequest('POST', "/p/deploy", params, successFn, failure);
		
    },
	
	
	
	sendDeployRequest: function(method, url, params, success, failure){
		
		// Send the request to the server.
		Ext.Ajax.request({
			url				: url,
			method			: method,
			timeout			: 1800000,
			disableCaching	: true,
			headers			: {'Accept':"application/json", 'Content-Type':'charset=UTF-8'},
			params			: params,
			success			: success,
			failure			: failure
		});
	},
    
   
    deploy: function(){
        
		// Check if currently is deploying
		if (this.deploying){
			return;
		}
		
		this.deploying = true;
		
        window.setTimeout((function(){
    		var meta = this.facade.getModelMetaData();
	     	this.processDeploy(meta);
	    }).bind(this), 10);
    
        return true;
    }	
});
