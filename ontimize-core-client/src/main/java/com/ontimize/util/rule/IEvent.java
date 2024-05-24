package com.ontimize.util.rule;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public interface IEvent {

    public String getType();

    public void setType(String type);

    public List<Object> getRules();

    public Map<Object,Object> getAttributes();

    public void setAttributes(Map<Object,Object> attributes);

    public void addRule(IRule rule);

    @Override
    public String toString();

}
