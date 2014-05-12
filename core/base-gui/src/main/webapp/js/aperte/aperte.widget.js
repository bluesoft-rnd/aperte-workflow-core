	<!-- Create widgets -->
	var widgets = [];

	<!-- Widget class  -->
	function Widget (name, widgetId, taskId)
	{
		this.name = name;
		this.widgetId = widgetId;
		this.taskId = taskId;
		this.formId = 'test';
		this.validate = function() {};
		this.getData = function() { return null; };
		this.isEnabled = true;
	}

	function WidgetDataBean(widgetId, widgetName, data)
	{
		this.widgetId = widgetId;
		this.data = data;
		this.widgetName = widgetName;
	}