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
		this.validateDataCorrectness = function() {};
		this.getData = function() { return null; };
		this.isEnabled = true;
	}
	
	function WidgetDataBean(widgetId, widgetName, data)
	{
		this.widgetId = widgetId;
		this.data = data;
		this.widgetName = widgetName;
	}
	
	function fullValidate(actionName) 
	{
	    var errors = [];
		var validateAllEnabled = true;

		$.each(widgets, function() {
			if (this.isValidateAllEnabled && !this.isValidateAllEnabled(actionName)) {
				validateAllEnabled = false;
			}
		});
			
        $.each(widgets, function()
        {
            /* Validate technical correctness */
			var errorMessages = validateAllEnabled ? this.validateDataCorrectness() :
								this.partialValidate ? this.partialValidate(actionName) : [];
            if(errorMessages)
            {
                $.each(errorMessages, function() {
                    errors.push(this);
                    addAlert(this);
                });
            }

            /* Validate business correctness */

			errorMessages = validateAllEnabled ? this.validate(actionName) :
								this.partialValidate ? this.partialValidate(actionName) : [];
            if(errorMessages)
            {
                $.each(errorMessages, function() {
                    errors.push(this);
                    addAlert(this);
                });
            }
        });
        return errors;
	}