package com.ontimize.util;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.util.math.MathExpressionParser;
import com.ontimize.util.math.MathExpressionParserFactory;

public abstract class JEPUtils {

	private static final Logger			logger			= LoggerFactory.getLogger(JEPUtils.class);

	private static Map<Object, Object>	customFunctions	= new HashMap<>();

	public static class Round extends org.nfunk.jep.function.PostfixMathCommand {

		NumberFormat nf = NumberFormat.getInstance();

		public Round() {
			this.numberOfParameters = 2;
		}

		@Override
		public void run(Stack stack) throws org.nfunk.jep.ParseException {
			this.checkStack(stack);

			Object oDecimals = stack.pop();
			Object oValue = stack.pop();
			Object oNewValue = this.round(oValue, oDecimals);
			stack.push(oNewValue);

			return;
		}

		public Object round(Object param1, Object param2) throws org.nfunk.jep.ParseException {
			if (param1 instanceof Number) {
				int nDec = 0;
				if (param2 instanceof Number) {
					nDec = ((Number) param2).intValue();
				} else if (param2 instanceof String) {
					nDec = Integer.parseInt((String) param2);
				} else {
					throw new org.nfunk.jep.ParseException("Invalid parameter type " + param2);
				}
				this.nf.setMinimumFractionDigits(nDec);
				this.nf.setMaximumFractionDigits(nDec);
				try {
					String s = this.nf.format(((Number) param1).doubleValue());
					Number n = this.nf.parse(s);
					return n;
				} catch (Exception e) {
					JEPUtils.logger.trace(null, e);
					throw new org.nfunk.jep.ParseException("round Error " + e.getMessage());
				}
			}

			throw new org.nfunk.jep.ParseException("Invalid parameter type");
		}

	}

	public static void registerCustomFunction(String name, Object function) {
		String property = System.getProperty(MathExpressionParserFactory.MATH_EXPRESSION_PARSER_PROPERTY);
		if (MathExpressionParser.JEP.equalsIgnoreCase(property)) {
			try {
				if ((function != null) && (function instanceof org.nfunk.jep.function.PostfixMathCommandI)) {
					JEPUtils.customFunctions.put(name, function);
				}
			} catch (Exception exc) {
				JEPUtils.logger.error(null, exc);
			}
		} else {
			try {
				if (function != null) {
					JEPUtils.customFunctions.put(name, function);
				}
			} catch (Exception exc) {
				JEPUtils.logger.error(null, exc);
			}
		}
	}

	public static Map<Object, Object> getCustomFunctions() {
		return (Map<Object, Object>) ((HashMap<Object, Object>) JEPUtils.customFunctions).clone();
	}

	static {
		String property = System.getProperty(MathExpressionParserFactory.MATH_EXPRESSION_PARSER_PROPERTY);
		if (!MathExpressionParser.JEP3x.equalsIgnoreCase(property)) {
			try {
				JEPUtils.registerCustomFunction("round", new Round());
			} catch (Throwable ex) {
				JEPUtils.logger.error(null, ex);
			}
		}
	}

}
