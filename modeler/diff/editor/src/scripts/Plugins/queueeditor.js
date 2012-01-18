if (!ORYX.Plugins) 
    ORYX.Plugins = new Object();

var queueWin;
	
function editorSetQueueData(newQueueConf){
    if (newQueueConf == null || newQueueConf == "")
	  return;
	  
    newQueueConf = newQueueConf.replace(/&quot;/g,'"'); 
	var oldQueueConf = faccade.getCanvas().properties["oryx-queue-conf"];
	
	var commandClass = ORYX.Core.Command.extend({
			construct: function(){
				this.oldQC 	  = oldQueueConf;
				this.newQC    = newQueueConf;
			},			
			execute: function(){
				faccade.getCanvas().properties["oryx-queue-conf"] = this.newQC;
			},
			rollback: function(){
				faccade.getCanvas().properties['oryx-queue-conf'] = this.oldQC;
			}
		});
	
	// Instanciated the class
	var command = new commandClass();
		
	// Execute the command
	faccade.executeCommands([command]);
	
	queueWin.close(); 
}
	
ORYX.Plugins.QueueEditor = Clazz.extend({
	
    facade: undefined,
	
	construct: function(facade){
		this.facade = facade;
		
		this.facade.offer({
			'name': ORYX.I18N.QueueEditor.queueEditor,
			'functionality': this.runQueueEditor.bind(this),
			'group': ORYX.I18N.QueueEditor.group,
			'icon': ORYX.PATH + "images/queue_editor.png",
			'description': ORYX.I18N.QueueEditor.desc,
			'index': 1,
			'minShape': 0,
			'maxShape': 0
		});
		
	},
	
    
   
    runQueueEditor: function(){
        var queueConf = this.facade.getCanvas().properties["oryx-queue-conf"];
		
		faccade = this.facade;
        this.editorOpenQueueWindow(queueConf);
        
        return true;
    },
	
	editorOpenQueueWindow: function (queueConf){
         
        var iframeName = "ifname";
		
		var rurl = window.location.href;
		var base_editor_url = rurl.substr(0,rurl.indexOf('editor'));
		var back_url = base_editor_url+"aperte_post";
		
		queueWin = new Ext.Window({
			width:750,
			height:500,
			autoScroll:false,
			html:'',
			modal:true,
			maximizable:true,
			cls:'x-window-body-report',
			title: 'Aperte Queue Editor'
		});
		queueWin.on('close', function() {
			if(Ext.isIE) {
				queueWin.body.dom.firstChild.src = "javascript:false";
			}
		}, queueWin);
			
		var id = Ext.id();
		var frame = document.createElement('iframe');
 
		frame.id = id;
		frame.name = id;
		frame.frameBorder = '0';
		frame.width = '100%';
		frame.height = '100%';
		frame.src = '';//Ext.isIE ? Ext.SSL_SECURE_URL : "javascript:;";
 
		queueWin.show();
		queueWin.body.appendChild(frame);
 
		// Seems to be workaround for IE having name readonly.
		if(Ext.isIE) {
			document.frames[id].name = id;
		}
		
		var form = new Ext.FormPanel({
			url: APERTE_QUEUE_EDITOR_URL,
			renderTo:Ext.getBody(),
			standardSubmit:true,
			method:'POST',
			defaultType:'hidden',
			items:[	new Ext.form.TextField({id:'fdata',
						name:'queue_config',
						inputType:'text',
						fieldLabel:'queue_config',
						value: queueConf
					}),
					new Ext.form.TextField({id:'cbe',
						name:'callback_url',
						inputType:'text',
						fieldLabel:'callback_url',
						value: back_url
					}),
					new Ext.form.TextField({id:'fre',
						name:'restartApplication',
						inputType:'text',
						fieldLabel:'restartApplication',
						value: "1"
					}),
					new Ext.form.Hidden({id:'aperteToken',
					    name:'aperteToken',
					    value: aperteToken
					})
		    ]
		});
 
		form.getForm().el.dom.action = form.url;
		form.getForm().el.dom.target = id;
			
		if(!Ext.isGecko) {
			var mask = new Ext.LoadMask(queueWin.id, {msg:"Loading..."});
			mask.show();
		}
 
		Ext.EventManager.on(frame, 'load', function() {
			if(mask !== undefined) { mask.hide(); }
			form.destroy();
		});
 
		Ext.emptyFn.defer(200); // frame on ready?
		form.getForm().submit();
    }
});
