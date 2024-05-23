package com.ontimize.db.query;

import java.util.ResourceBundle;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.jee.common.db.ContainsExtendedSQLConditionValuesProcessor;
import com.ontimize.jee.common.db.SQLStatementBuilder.Expression;
import com.ontimize.jee.common.db.SQLStatementBuilder.Field;
import com.ontimize.jee.common.db.query.ParameterField;

public class ContainsSQLConditionValuesProcessorHelper {

	public static String renderQueryConditionsExpressBundle(Expression expression, ResourceBundle bundle) {
		StringBuilder sb = new StringBuilder();
		ContainsSQLConditionValuesProcessorHelper.renderQueryConditionsExpress(expression, sb, bundle);
		return sb.toString();
	}

	public static void renderQueryConditionsExpress(Expression expression, StringBuilder sb, ResourceBundle bundle) {
		if (expression == null) {
			return;
		}

		if (expression.getLeftOperand() instanceof Expression) {
			sb.append("(" + ContainsSQLConditionValuesProcessorHelper.renderQueryConditionsExpressBundle((Expression) expression.getLeftOperand(), bundle));
			sb.append(" " + expression.getOperator().toString() + " ");
			sb.append(ContainsSQLConditionValuesProcessorHelper.renderQueryConditionsExpressBundle((Expression) expression.getRightOperand(), bundle) + ")");
		} else {
			sb.append("(");
			if (expression.getLeftOperand() != null) {
				// Simple expressions
				sb.append(ApplicationManager.getTranslation(expression.getLeftOperand().toString(), bundle));
				if (expression.getOperator() != null) {
					sb.append(" " + expression.getOperator().toString());
				} else {
					sb.append(" null ");
				}
				if (expression.getRightOperand() != null) {
					if (expression.getRightOperand() instanceof ParameterField) {
						if (((ParameterField) expression.getRightOperand()).getValue() == null) {
							sb.append(" {Parameter=?} ");
						} else {
							sb.append(" {Parameter='" + ((ParameterField) expression.getRightOperand()).getValue() + "'} ");
						}
					} else if (expression.getRightOperand() instanceof Field) {
						sb.append(ApplicationManager.getTranslation(expression.getRightOperand().toString(), bundle));
					} else if (expression.getRightOperand() instanceof String) {
						sb.append(" '" + ApplicationManager.getTranslation(expression.getRightOperand().toString(), bundle) + "'");
					} else if (expression.getRightOperand() instanceof Expression) {
						sb.append(" " + ContainsExtendedSQLConditionValuesProcessor.createQueryConditionsExpress((Expression) expression.getRightOperand()));
					}
				}
			}
			sb.append(")");
		}
	}

}
