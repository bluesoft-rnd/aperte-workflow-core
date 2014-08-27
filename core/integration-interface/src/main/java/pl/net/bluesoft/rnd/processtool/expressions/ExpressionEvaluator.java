package pl.net.bluesoft.rnd.processtool.expressions;

import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;

/**
 * User: POlszewski
 * Date: 2014-06-18
 */
public interface ExpressionEvaluator {
	class Result {
		private final String value;

		public Result(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	Result evaluate(String expression, IAttributesProvider attributesProvider);
}
