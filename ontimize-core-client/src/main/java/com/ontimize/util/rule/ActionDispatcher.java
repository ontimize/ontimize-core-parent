package com.ontimize.util.rule;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.MapContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.Form;
import com.ontimize.gui.field.DataComponent;
import com.ontimize.gui.field.DataField;
import com.ontimize.gui.field.FormComponent;
import com.ontimize.gui.manager.IFormManager;
import com.ontimize.jee.common.gui.SearchValue;
import com.ontimize.util.ParseUtils;
import com.ontimize.util.math.JexlExpressionParser;

public class ActionDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(ActionDispatcher.class);

    public ActionDispatcher(Form form, IFormManager formManager) {
    }

    public static Object execute(List<?> lActions, Form form) {
        List<Object> lResult = new ArrayList<>();
        for (int i = 0; i < lActions.size(); i++) {
            IAction action = (Action) lActions.get(i);
            lResult.add(ActionDispatcher.executeAction(action, form));
        }
        return lResult;
    }

    public static Object executeAction(IAction action, Form form) {
        ICondition condition = action.getCondition();
        boolean conditionOK = ActionDispatcher.evaluateCondition(condition, form);
        if (conditionOK) {
            String id = action.getId();
            List<?> parameters = action.getParams();
            if (RuleParser.Attributes.SHOW_MESSAGE_ACTION.equalsIgnoreCase(id)) {
                if (parameters.size() != 1) {
                    ActionDispatcher.logger.debug("Incorrect number of parameters for action: " + id);
                }
                IActionParam actionParam = (IActionParam) parameters.get(0);
                String message = actionParam.getParamValue();
                ActionDispatcher.showMessageAction(form, message);
                return null;
            }
            if (RuleParser.Attributes.SET_VALUE_ACTION.equalsIgnoreCase(id)) {
                String attr = null;
                Object value = null;
                for (int i = 0; i < parameters.size(); i++) {
                    // ActionParam with attr and enabled value
                    IActionParam actionParam = (IActionParam) parameters.get(i);
                    if (DataField.ATTR.equalsIgnoreCase(actionParam.getParamName())) {
                        attr = actionParam.getParamValue();
                    }
                    if ("value".equalsIgnoreCase(actionParam.getParamName())) {
                        value = actionParam.getParamValue();
                    }

                }
                if (attr != null) {
                    ActionDispatcher.setValueAction(form, attr, value);
                }
            }
            if (RuleParser.Attributes.SET_ENABLED_ACTION.equalsIgnoreCase(id)) {
                String attr = null;
                String enabled = null;
                for (int i = 0; i < parameters.size(); i++) {
                    // ActionParam with attr and enabled value
                    IActionParam actionParam = (IActionParam) parameters.get(i);
                    if (DataField.ATTR.equalsIgnoreCase(actionParam.getParamName())) {
                        attr = actionParam.getParamValue();
                    }
                    if ("value".equalsIgnoreCase(actionParam.getParamName())) {
                        enabled = actionParam.getParamValue();
                    }

                }
                if (attr != null) {
                    ActionDispatcher.setEnabledAction(form, attr, Boolean.valueOf(enabled).booleanValue());
                }
            }
            if (RuleParser.Attributes.SET_REQUIRED_ACTION.equalsIgnoreCase(id)) {
                String attr = null;
                String enabled = null;
                for (int i = 0; i < parameters.size(); i++) {
                    // ActionParam with attr and enabled value
                    IActionParam actionParam = (IActionParam) parameters.get(i);
                    if (DataField.ATTR.equalsIgnoreCase(actionParam.getParamName())) {
                        attr = actionParam.getParamValue();
                    }
                    if ("value".equalsIgnoreCase(actionParam.getParamName())) {
                        enabled = actionParam.getParamValue();
                    }

                }
                if (attr != null) {
                    ActionDispatcher.setRequiredAction(form, attr, Boolean.valueOf(enabled).booleanValue());
                }
            }
        }
        return null;
    }

    public static void showMessageAction(Form form, String message) {
        form.message(message, Form.INFORMATION_MESSAGE);
    }

    public static void setValueAction(Form form, String attr, Object value) {
        DataComponent comp = form.getDataFieldReference(attr);
        comp.setValue(ParseUtils.getValueForSQLType(value, comp.getSQLDataType()));
    }

    public static void setEnabledAction(Form form, String attr, boolean enabled) {
        FormComponent comp = form.getElementReference(attr);
        comp.setEnabled(enabled);
    }

    public static void setRequiredAction(Form form, String attr, boolean required) {
        DataComponent comp = form.getDataFieldReference(attr);
        comp.setRequired(required);
    }

    public static boolean evaluateCondition(ICondition condition, Form form) {
        if (condition == null) {
            return true;
        }
        String sExpression = condition.getExpression().trim();
        if ((sExpression == null) || (sExpression.length() == 0)) {
            return true;
        }
        Expression e = JexlExpressionParser.getJexlEngine().createExpression(sExpression);

        // populate the context
        JexlContext context = new MapContext();
        for (int j = 0; j < form.getComponentList().size(); j++) {
            Object comp = form.getComponentList().get(j);
            if (comp instanceof DataComponent) {
                Object value = ((DataComponent) comp).getValue();
                if (value instanceof SearchValue) {
                    value = ((SearchValue) value).getValue();
                }
                context.set(((DataComponent) comp).getAttribute().toString(), value);
            }
        }

        Object result = e.evaluate(context);
        if ((result == null) || !(result instanceof Boolean) || !((Boolean) result).booleanValue()) {
            // first condition that returns false provokes false in conditions
            return false;
        }
        return true;

    }

}
