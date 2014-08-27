package pl.net.bluesoft.rnd.processtool.expressions;

import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * User: POlszewski
 * Date: 2014-06-18
 */
public class ExpressionEvaluators {
	private static final Logger logger = Logger.getLogger(ExpressionEvaluators.class.getName());

	private final List<ExpressionEvaluator> evaluators = new ArrayList<ExpressionEvaluator>();

	public void add(ExpressionEvaluator evaluator) {
		evaluators.add(evaluator);
	}

	public void remove(ExpressionEvaluator evaluator) {
		evaluators.remove(evaluator);
	}

	public String evaluate(String expression, IAttributesProvider attributesProvider) {
		for (ExpressionEvaluator evaluator : evaluators) {
			ExpressionEvaluator.Result result = evaluator.evaluate(expression, attributesProvider);

			if (result != null) {
				return result.getValue();
			}
		}
		logger.severe("Unable to evaluate expression: " + expression);
		return null;
	}
}
