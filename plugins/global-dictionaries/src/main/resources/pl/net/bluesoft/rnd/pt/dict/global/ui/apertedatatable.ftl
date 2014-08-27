<#import "/spring.ftl" as spring />

<#assign portlet=JspTaglibs["http://java.sun.com/portlet_2_0"] />

<script type="text/javascript">
	function AperteDataTable(tableId, columnDefs, sortingOrder)
	{
		this.tableId = tableId;
		this.requestUrl = '';
		this.columnDefs = columnDefs;
		this.sortingOrder = sortingOrder;
		this.dataTable;
		this.requestParameters = [];

		this.initialized = false;

		this.setParameters = function(parameters)
		{
           this.requestParameters =  parameters;
		}

		this.addParameter = function(name, value)
		{
			this.requestParameters.push({ "name": name, "value": value });
		}

		this.reloadTable = function(requestUrl)
		{
			$.each(this.requestParameters, function (index, parameter)
			{
				requestUrl += "&<@portlet.namespace/>" + parameter["name"] + "=" + parameter["value"];
			});

			this.requestUrl = requestUrl;
			if(this.initialized == false)
			{
				this.createDataTable();
				this.initialized = true;
			}
			else
			{
				this.dataTable.fnReloadAjax(this.requestUrl);
			}
		}

		this.enableMobileMode = function()
		{
		}

		this.enableTabletMode = function()
		{
		}

		this.disableMobileMode = function()
		{
		}

		this.disableTabletMode = function()
		{
		}

		this.createDataTable = function()
		{
			this.dataTable = $('#'+this.tableId).dataTable({
				"bLengthChange": true,
				"bFilter": true,
				"bProcessing": true,
				"bServerSide": true,
				"bInfo": true,
				"aaSorting": sortingOrder,
				"bSort": true,
				"iDisplayLength": 10,
				"sDom": 'R<"top"t><"bottom"plr>',
				"sAjaxSource": this.requestUrl,
				"fnServerData": function ( sSource, aoData, fnCallback ) {

					$.ajax( {
						"dataType": 'json',
						"type": "POST",
						"url": sSource,
						"data": aoData,
						"success": fnCallback
					} );
				},
				"fnServerParams": function ( aoData ) {
                      aoData.push( this.requestParameters );
                },
				"aoColumns": this.columnDefs,
				"oLanguage": {
					  //todo: uzeleznic tresci od tlumaczen w messages
					  "sInfo": "Wyniki od _START_ do _END_ z _TOTAL_",
					  "sEmptyTable": "<@spring.message 'datatable.empty' />",
					  "sInfoEmpty": "<@spring.message 'datatable.empty' />",
					  "sProcessing": "<@spring.message 'datatable.processing' />",
					  "sLengthMenu": "<@spring.message 'datatable.records' />",
					  "sInfoFiltered": "",
					  "oPaginate": {
						"sFirst": "<@spring.message 'datatable.paginate.firstpage' />",
						"sNext": "<@spring.message 'datatable.paginate.next' />",
						"sPrevious": "<@spring.message 'datatable.paginate.previous' />"
					  }

					}
			});
			if(typeof windowManager != 'undefined')
			{
				if(windowManager.mobileMode == true)
				{
					this.enableMobileMode();
				}

				if(windowManager.tabletMode == true)
				{
					this.enableTabletMode();
				}
			}
		}

		this.toggleColumnButton = function(columnName, active)
		{
			var checkbox = $("#button-"+this.tableId+'-'+columnName);
			checkbox.trigger('click');
		}

		this.toggleColumn = function(columnName)
		{
			var dataTable = this.dataTable;
			$.each(dataTable.fnSettings().aoColumns, function (columnIndex, column)
			{
				if (column.sName == columnName)
				{
					  dataTable.fnSetColumnVis(columnIndex, column.bVisible ? false : true, false);
				}
		    });
		}

		this.addRow = function(data) {
		    this.dataTable.row.add(data);
		}
	}
</script>
