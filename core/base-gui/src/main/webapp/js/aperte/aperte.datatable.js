	function AperteDataTable(tableId, columnDefs, sortingOrder, options)
	{
	    $.fn.dataTableExt.sErrMode = "throw";
		this.tableId = tableId;
		this.requestUrl = '';
		this.columnDefs = columnDefs;
		this.sortingOrder = sortingOrder;
		this.dataTable;
		this.requestParameters = [];
		this.clearOnStart = false;

		this.initialized = false;

		this.setParameters = function(parameters)
		{
           this.requestParameters =  parameters;
		}

		this.addParameter = function(name, value)
		{
			this.requestParameters.push({ "name": name, "value": value });
		}
		

		this.clearState = function()
		{
		    this.dataTable.state.clear();
		    this.dataTable.page('first');
		}


		this.reloadTable = function(requestUrl)
		{
			$.each(this.requestParameters, function (index, parameter)
			{
				requestUrl += portletNamespace + parameter["name"] + "=" + parameter["value"];
			});

			this.requestUrl = requestUrl;
			if(this.initialized == false)
			{
				this.createDataTable();
				this.initialized = true;
				if(this.clearOnStart) 
				{
					this.clearState();
					this.dataTable.draw();
				}
			}
			else
			{
				this.dataTable.ajax.url(requestUrl).load(null, false);
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

		this.createDataTable = function(tableElementsPlacement)
		{
		    var sDom = (tableElementsPlacement !== undefined) ? tableElementsPlacement : 'R<"top"t><"bottom"plr>';

			var aperteDataTable = this;

			var definition = {
				 serverSide: true,
				 ordering: true,
				 lengthChange: true,
				 stateSave: true,
				 dom: sDom,
				 processing: true,
				 order: sortingOrder,
				ajax: {
						 dataType: 'json',
						 type: "POST",
						 url: aperteDataTable.requestUrl,
						"data": function ( d )
						{

							$.each(aperteDataTable.requestParameters, function (index, parameter)
							{
								var key = parameter["name"];
								var value = parameter["value"];
								d[key] = value;
							});
						 },
						dataSrc: "listData"
					 },
				columns: aperteDataTable.columnDefs,
				language: dataTableLanguage
			 };

			if (options) {
				for (var key in options) {
					definition[key] = options[key];
				}
			}

			this.dataTable = $('#'+this.tableId).DataTable(definition);
				
			var searchWasEnabled = this.dataTable.search() != "";
			if(searchWasEnabled)
				this.dataTable.search('').draw();
			else
			{
				$.each(this.dataTable.columns().search(), function(columnIndex, columnSearch) {
					if(columnSearch && columnSearch != "")
						searchWasEnabled = true;
				});

				if(searchWasEnabled)
					this.dataTable.columns().search('').draw();
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
	}