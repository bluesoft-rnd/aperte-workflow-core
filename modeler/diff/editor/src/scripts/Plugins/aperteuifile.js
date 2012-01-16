if (!ORYX.Plugins) 
    ORYX.Plugins = new Object();

ORYX.Plugins.AperteUiSave = ORYX.Plugins.Save.extend({
	
	
    saveSynchronously: function(forceNew, modelInfo){
		
		if (!modelInfo) {
			return;
		}
		
		var modelMeta = this.facade.getModelMetaData();
		var reqURI = modelMeta.modelHandler;


		// Get the stencilset
		var ss = this.facade.getStencilSets().values()[0]
		
		var typeTitle = ss.title();
		
		// Define Default values
		var name = (modelMeta["new"] && modelMeta.name === "") ? ORYX.I18N.Save.newProcess : modelInfo.name;
		var defaultData = {title:Signavio.Utils.escapeHTML(name||""), summary:Signavio.Utils.escapeHTML(modelInfo.description||""), type:typeTitle, url: reqURI, namespace: modelInfo.model.stencilset.namespace, comment: '' }
		
		// Create a Template
		var dialog = new Ext.XTemplate(		
					// TODO find some nice words here -- copy from above ;)
					'<form class="oryx_repository_edit_model" action="#" id="edit_model" onsubmit="return false;">',
									
						'<fieldset>',
							'<p class="description">' + ORYX.I18N.Save.dialogDesciption + '</p>',
							'<input type="hidden" name="namespace" value="{namespace}" />',
							'<p><label for="edit_model_title">' + ORYX.I18N.Save.dialogLabelTitle + '</label><input type="text" class="text" name="title" value="{title}" id="edit_model_title" onfocus="this.className = \'text activated\'" onblur="this.className = \'text\'"/></p>',
							'<p><label for="edit_model_summary">' + ORYX.I18N.Save.dialogLabelDesc + '</label><textarea rows="5" name="summary" id="edit_model_summary" onfocus="this.className = \'activated\'" onblur="this.className = \'\'">{summary}</textarea></p>',
							(modelMeta.versioning) ? '<p><label for="edit_model_comment">' + ORYX.I18N.Save.dialogLabelComment + '</label><textarea rows="5" name="comment" id="edit_model_comment" onfocus="this.className = \'activated\'" onblur="this.className = \'\'">{comment}</textarea></p>' : '',
							'<p><label for="edit_model_type">' + ORYX.I18N.Save.dialogLabelType + '</label><input type="text" name="type" class="text disabled" value="{type}" disabled="disabled" id="edit_model_type" /></p>',
							
						'</fieldset>',
					
					'</form>')
		
		// Create the callback for the template
		callback = function(form){

			    // raise loading enable event
		        /*this.facade.raiseEvent({
		            type: ORYX.CONFIG.EVENT_LOADING_ENABLE,
					text: ORYX.I18N.Save.saving
		        });*/

				var title 		= form.elements["title"].value.strip();
				title 			= title.length == 0 ? defaultData.title : title;
				
				var summary 	= form.elements["summary"].value.strip();	
				summary 		= summary.length == 0 ? defaultData.summary : summary;
				
				var namespace	= form.elements["namespace"].value.strip();
				namespace		= namespace.length == 0 ? defaultData.namespace : namespace;
				
				var comment 	= form .elements["comment"].value.strip();
				comment			= comment.length == 0 ? defaultData.comment : comment;
				
				modelMeta.name = title;
				modelMeta.description = summary;
				modelMeta.parent = modelInfo.parent;
				modelMeta.namespace = namespace;
	        		
				//added changing title of page after first save, but with the changed flag
				if(!forceNew) window.document.title = this.changeSymbol + title + " | " + ORYX.CONFIG.APPNAME;
					
					
		        // Get json
				var json = this.facade.getJSON();
				
				var glossary = [];
				
				//Support for glossary
				if (this.facade.hasGlossaryExtension) {
					
					Ext.apply(json, ORYX.Core.AbstractShape.JSONHelper);
					var allNodes = json.getChildShapes(true);
					
					var orders = {};
					
					this.facade.getGlossary().each(function(entry){
						if ("undefined" == typeof orders[entry.shape.resourceId+"-"+entry.property.prefix()+"-"+entry.property.id()]){
							orders[entry.shape.resourceId+"-"+entry.property.prefix()+"-"+entry.property.id()] = 0;
						}
						// Add entry
						glossary.push({
							itemId		: entry.glossary,
			            	elementId	: entry.shape.resourceId,
			            	propertyId	: entry.property.prefix()+"-"+entry.property.id(),
				            order		: orders[entry.shape.resourceId+"-"+entry.property.prefix()+"-"+entry.property.id()]++
						});
						
						// Replace the property with the generated glossary url
						/*var rId = entry.shape.resourceId;
						var pKe = entry.property.id();
						for (var i=0, size=allNodes.length; i<size; ++i) {
							var sh = allNodes[i];
							if (sh.resourceId == rId) {
								for (var prop in sh.properties) {
									if (prop === pKe) {
										sh.properties[prop] = this.facade.generateGlossaryURL(entry.glossary, sh.properties[prop]);
										break;
									}
								}
								break;
							}
						}*/
						
						
						// Replace SVG
						if (entry.property.refToView() && entry.property.refToView().length > 0) {
							entry.property.refToView().each(function(ref){
								var node = $(entry.shape.id+""+ref);
								if (node)
									node.setAttribute("oryx:glossaryIds", entry.glossary + ";")
							})
						}
					}.bind(this))
					

					// Set the json as string
					json = json.serialize();

				} else {
					json = Ext.encode(json);
				}
				
				// Set the glossaries as string
				glossary = Ext.encode(glossary);
				
				var selection = this.facade.getSelection();
				this.facade.setSelection([]);

				// Get the serialized svg image source
		        var svgClone 	= this.facade.getCanvas().getSVGRepresentation(true);
				this.facade.setSelection(selection);
		        if (this.facade.getCanvas().properties["oryx-showstripableelements"] === false) {
		        	var stripOutArray = svgClone.getElementsByClassName("stripable-element");
		        	for (var i=stripOutArray.length-1; i>=0; i--) {
		        		stripOutArray[i].parentNode.removeChild(stripOutArray[i]);
		        	}
		        }
				  
				// Remove all forced stripable elements 
	        	var stripOutArray = svgClone.getElementsByClassName("stripable-element-force");
	        	for (var i=stripOutArray.length-1; i>=0; i--) {
	        		stripOutArray[i].parentNode.removeChild(stripOutArray[i]);
	        	}
				          
				// Parse dom to string
		        var svgDOM 	= DataManager.serialize(svgClone);
				
		        var params = {
		        		json_xml: json,
		        		svg_xml: svgDOM,
		        		name: title,
		        		type: defaultData.type,
		        		parent: modelMeta.parent,
		        		description: summary,
		        		comment: comment,
		        		glossary_xml: glossary,
		        		namespace: modelMeta.namespace,
		        		views: Ext.util.JSON.encode(modelMeta.views || [])
		        };
		        
				var success = false;
				
				var successFn = function(transport) {
					var loc = transport.getResponseHeader.location;
					if (!this.processURI && loc) {
						this.processURI = loc;
					}
	
					if( forceNew ){
						var resJSON = transport.responseText.evalJSON();
						
						var modelURL = location.href.substring(0, location.href.indexOf(location.search)) + '?id=' + resJSON.href.substring(7);
						var newURLWin = new Ext.Window({
							title:		ORYX.I18N.Save.savedAs, 
							bodyStyle:	"background:white;padding:10px", 
							width:		'auto', 
							height:		'auto',
							html:"<div style='font-weight:bold;margin-bottom:10px'>"+ORYX.I18N.Save.savedDescription+":</div><span><a href='" + modelURL +"' target='_blank'>" + modelURL + "</a></span>",
							buttons:[{text:'Ok',handler:function(){newURLWin.destroy()}}]
						});
						newURLWin.show();
						
						window.open(modelURL);
					}
	
					//show saved status
					/*this.facade.raiseEvent({
							type:ORYX.CONFIG.EVENT_LOADING_STATUS,
							text:ORYX.I18N.Save.saved
						});*/
						
					success = true;
					
					win.close();
				
					if (success) {
						// Reset changes
						this.changeDifference = 0;
						this.updateTitle();
						
						if(modelMeta["new"]) {
							modelMeta["new"] = false;
						}
					}
					
					
					delete this.saving;
						
				}.bind(this);
				
				var failure = function(transport) {
						// raise loading disable event.
		                this.facade.raiseEvent({
		                    type: ORYX.CONFIG.EVENT_LOADING_DISABLE
		                });
						
						win.close();
						
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
						
						delete this.saving;
						
					}.bind(this);
				
				if(modelMeta["new"]) {	
					// Send the request out
					params.id = modelMeta.modelId;					
					this.sendSaveRequest('POST', reqURI, params, forceNew, successFn, failure);
				} else if(forceNew) {
					this.sendSaveRequest('POST', reqURI, params, forceNew, successFn, failure);
				} else {
					if (!reqURI.include(modelMeta.modelId))
						reqURI += "/" + modelMeta.modelId;
						
					params.id = modelMeta.modelId;
					// Send the request out
					this.sendSaveRequest('PUT', reqURI, params, false, successFn, failure);
				}
		}.bind(this);
			
		// Create a new window				
		win = new Ext.Window({
			id		: 'Propertie_Window',
	        width	: 'auto',
	        height	: 'auto',
		    title	: forceNew ? ORYX.I18N.Save.saveAsTitle : ORYX.I18N.Save.save,
	        modal	: true,
	        resize	: false,
			bodyStyle: 'background:#FFFFFF',
	        html	: dialog.apply( defaultData ),
	        defaultButton: 0,
			buttons:[{
				text: ORYX.I18N.Save.saveBtn,
				handler: function(){
				
					win.body.mask(ORYX.I18N.Save.pleaseWait, "x-waiting-box");
					
					window.setTimeout(function(){
						
						callback($('edit_model'));
						
					}.bind(this), 10);			
				},
				listeners:{
					render:function(){
						this.focus();
					}
				}
			},{
            	text: ORYX.I18N.Save.close,
            	handler: function(){
	               win.close();
            	}.bind(this)
			}],
			listeners: {
				close: function(){					
                	win.destroy();
					delete this.saving;
				}.bind(this)
			}
	    });
				      
		win.show();
    }	
});
