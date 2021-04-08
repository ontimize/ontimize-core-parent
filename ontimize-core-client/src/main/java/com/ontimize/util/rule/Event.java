package com.ontimize.util.rule;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.ontimize.util.rule.RuleParser.Attributes;

public class Event implements IEvent {

    protected String type;

    protected Hashtable attributes;

    protected List rules;

    public Event() {
        this.attributes = new Hashtable();
        this.rules = new Vector();
    }

    public Event(Hashtable attributes, List rules) {
        this.setAttributes(attributes);
        this.setRules(rules);
    }

    /**
     * @param rules the rules to set
     */
    public void setRules(List rules) {
        this.rules = rules;
    }

    @Override
    public void addRule(IRule rule) {
        this.rules.add(rule);
    }

    /**
     * @return the rules
     */
    @Override
    public List getRules() {
        return this.rules;
    }

    /**
     * @param attributes the attributes to set
     */
    @Override
    public void setAttributes(Hashtable attributes) {
        this.attributes = attributes;
    }

    /**
     * @return the attributes
     */
    @Override
    public Hashtable getAttributes() {
        return this.attributes;
    }

    /**
     * @param type the type to set
     */
    @Override
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the type
     */
    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(RuleParser.openTagWithAttributes(Attributes.EVENT, this.getAttributes()));
        for (int i = 0; i < this.rules.size(); i++) {
            sb.append(this.rules.get(i).toString());
        }
        sb.append(RuleParser.closeTag(Attributes.EVENT));
        return sb.toString();
    }

}
