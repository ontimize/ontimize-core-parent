package com.ontimize.util.rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.util.rule.RuleParser.Attributes;

public class RuleUtils {

    /**
     * List of events matching <code>type</code>. If type is "" or null returns all events for these
     * rules.
     */
    public static List<Object> findEventsByType(IRules rules, String type) {
    	List<Object> lMatchEvents = new ArrayList<>();
        if ((rules == null) || (rules.getEvents().size() == 0)) {
            return lMatchEvents;
        }
        List<Object> lAllEvents = rules.getEvents();
        if ((type == null) || (type.length() == 0)) {
            return lAllEvents;
        }
        for (int i = 0; i < lAllEvents.size(); i++) {
            IEvent currentEvent = (IEvent) lAllEvents.get(i);
            if (type.equalsIgnoreCase(currentEvent.getType())) {
                lMatchEvents.add(currentEvent);
            }
        }
        return lMatchEvents;
    }

    /**
     * List of events matching <code>type</code> and <code>field</code>. If type is "" or null returns
     * all events for these rules.
     */
    public static List<Object> findEventsByField(IRules rules, String field) {
    	List<Object> lMatchEvents = new ArrayList<>();
        if ((rules == null) || (rules.getEvents().size() == 0)) {
            return lMatchEvents;
        }
        List<Object> lAllEvents = rules.getEvents();
        if ((field == null) || (field.length() == 0)) {
            return lAllEvents;
        }
        for (int i = 0; i < lAllEvents.size(); i++) {
            IEvent currentEvent = (IEvent) lAllEvents.get(i);
            Object oField = currentEvent.getAttributes().get(Attributes.FIELD);
            if ((oField != null) && field.equalsIgnoreCase(oField.toString())) {
                lMatchEvents.add(currentEvent);
            }
        }
        return lMatchEvents;
    }

    /**
     * List of events matching <code>type</code> and <code>field</code>. If type is "" or null returns
     * all events for these rules.
     */
    public static List<Object> findEventsByFieldAndType(IRules rules, String field, String type) {
    	List<Object> lMatchEvents = new ArrayList<>();
        if ((rules == null) || (rules.getEvents().size() == 0)) {
            return lMatchEvents;
        }
        List<Object> lAllEvents = rules.getEvents();
        if ((type == null) || (type.length() == 0)) {
            return lAllEvents;
        }
        for (int i = 0; i < lAllEvents.size(); i++) {
            IEvent currentEvent = (IEvent) lAllEvents.get(i);
            Object oField = currentEvent.getAttributes().get(Attributes.FIELD);
            if (type.equalsIgnoreCase(currentEvent.getType()) && (oField != null)
                    && field.equalsIgnoreCase(oField.toString())) {
                lMatchEvents.add(currentEvent);
            }
        }
        return lMatchEvents;
    }

    /**
     * List of actions matching <code>actionType</code> in Event. If actionType is "" or null returns
     * all actions for this event. This method does not take account conditions.
     */
    public static List<Object> findActionsByEvent(IEvent event, String actionId) {
    	List<Object> lMatchActions = new ArrayList<>();
        if ((event == null) || (event.getRules().size() == 0)) {
            return lMatchActions;
        }
        List<Object> lEventRules = event.getRules();
        for (int i = 0; i < lEventRules.size(); i++) {
            IRule currentRule = (IRule) lEventRules.get(i);
            List<?> lActions = currentRule.getActions();
            if ((actionId == null) || (actionId.length() == 0)) {
                lMatchActions.addAll(lActions);
            } else {
                for (int j = 0; j < lActions.size(); j++) {
                    IAction currentAction = (IAction) lActions.get(j);
                    if (actionId.equalsIgnoreCase(currentAction.getId())) {
                        lMatchActions.add(currentAction);
                    }
                }
            }
        }
        return lMatchActions;
    }

    /**
     * List of actions matching event type and event field <code>actionType</code> in Event. If
     * actioType is "" or null returns all actions matching this eventType and eventField. This method
     * does not take account conditions.
     */
    public static List<Object> findActionsByTypeAndField(IRules rules, String eventType, Map<?,?> filterEvents,
            String eventField, String actionType) {
    	List<Object> lMatchActions = new ArrayList<>();
    	List<?> lEvents = rules.getEvents();
        if ((lEvents == null) || (lEvents.size() == 0)) {
            return lMatchActions;
        }
        for (int i = 0; i < lEvents.size(); i++) {
            IEvent event = (IEvent) lEvents.get(i);
            if (RuleUtils.matchAttributes(event.getAttributes(), filterEvents)) {
                Object currentEventField = event.getAttributes().get(Attributes.FIELD);
                if (eventType.equalsIgnoreCase(event.getType()) && (eventField != null)
                        && eventField.equalsIgnoreCase(currentEventField.toString())) {
                    List<Object> lEventRules = event.getRules();
                    for (int j = 0; j < lEventRules.size(); j++) {
                        IRule rule = (IRule) lEventRules.get(j);
                        List<Object> lActions = rule.getActions();
                        if ((actionType == null) || (actionType.length() == 0)) {
                            lMatchActions.addAll(lActions);
                        } else {
                            for (int k = 0; k < lActions.size(); k++) {
                                IAction action = (IAction) lActions.get(k);
                                if (actionType.equalsIgnoreCase(action.getId())) {
                                    lMatchActions.add(action);
                                }
                            }
                        }
                    }
                }
            }
        }
        return lMatchActions;
    }

    public static EntityResult paramActionsToEntityResult(List paramActions) {
        EntityResult res = new EntityResultMapImpl(
                Arrays.asList(new String[] { Attributes.PARAM_NAME, Attributes.PARAM_VALUE }));
        for (int i = 0; i < paramActions.size(); i++) {
            IActionParam param = (IActionParam) paramActions.get(i);
            Map<Object, Object> hParam = new HashMap<>();
            hParam.put(Attributes.PARAM_VALUE, param.getParamValue());
            hParam.put(Attributes.PARAM_NAME, param.getParamName());
            res.addRecord(hParam);
        }
        return res;
    }

    public static List<Object> entityResultToParamActions(EntityResult erActions) {
    	List<Object> paramActions = new ArrayList<>();
        for (int i = 0; i < erActions.calculateRecordNumber(); i++) {
            Map<?,?> hParam = erActions.getRecordValues(i);
            IActionParam param = new ActionParam();
            Object paramName = hParam.get(Attributes.PARAM_NAME);
            if (paramName != null) {
                param.setParamName(paramName.toString());
            }
            Object paramValue = hParam.get(Attributes.PARAM_VALUE);
            if (paramValue != null) {
                param.setParamValue(paramValue.toString());
            }
            paramActions.add(param);
        }
        return paramActions;
    }

    public static boolean matchAttributes(Map<?,?> attributeList, Map<?,?> attrsToMatch) {
        if ((attrsToMatch == null) || attrsToMatch.isEmpty()) {
            return true;
        }
        if (attrsToMatch != null) {
        	for(Entry<?, ?> entry:attrsToMatch.entrySet()) {
        		 Object key = entry.getKey();
                 if (attributeList.containsKey(key)) {
                     Object oColumn = entry.getValue();
                     if (!oColumn.equals(attributeList.get(key))) {
                         return false;
                     }
                 } else {
                     return false;
                 }
        	}
        }
        return true;
    }

    // /**
    // * List of actions matching <code>actionType</code>. If actionType is ""
    // or null
    // * returns all actions for this event. This method does not take account
    // * conditions.
    // */
    // public static List findConditionsByEvent(IEvent event, String actionId) {
    // List lMatchActions = new Vector();
    // if (event == null || event.getRules().size() == 0) {
    // return lMatchActions;
    // }
    // List lEventRules = event.getRules();
    // for (int i=0; i < lEventRules.size(); i++) {
    // IRule currentRule = (IRule) lEventRules.get(i);
    // List lActions = currentRule.getActions();
    // if (actionId == null || actionId.length() == 0) {
    // lMatchActions.addAll(lActions);
    // }
    // else {
    // for (int j=0; j<lActions.size(); j++) {
    // IAction currentAction = (IAction) lActions.get(j);
    // if (actionId.equalsIgnoreCase(currentAction.getId())) {
    // lMatchActions.add(currentAction);
    // }
    // }
    // }
    // }
    // return lMatchActions;
    // }

}
