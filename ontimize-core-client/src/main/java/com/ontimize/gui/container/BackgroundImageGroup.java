package com.ontimize.gui.container;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.field.DataComponent;

public class BackgroundImageGroup extends JImageContainer implements DataComponentGroup {

    private static final Logger logger = LoggerFactory.getLogger(BackgroundImageGroup.class);

    protected Map<Object, DataComponent> dataComponent = new HashMap<>();

    protected Object attribute = null;

    public BackgroundImageGroup(Map<Object, Object> parameters) {
        // Parent constructor execute the initialization
        super(parameters);
        Object attr = parameters.get("attr");
        if (attr == null) {
            BackgroundImageGroup.logger.debug(this.getClass().toString() + " 'attr' parameter is required");
        } else {
            this.attribute = attr;
        }

    }

    @Override
    public void add(Component comp, Object constraints) {
        if (comp instanceof DataComponent) {
            this.dataComponent.put(((DataComponent) comp).getAttribute(), (DataComponent)comp);
        }
        super.add(comp, constraints);
    }

    @Override
    public Object getAttribute() {
        return this.attribute;
    }

    @Override
    public Map<Object, Object> getGroupValue() {
    	Map<Object, Object> hValue = new HashMap<>();
    	for(Entry<Object, DataComponent> entry:this.dataComponent.entrySet()) {
            hValue.put(entry.getKey(), entry.getValue().getValue());
    	}
        return hValue;
    }

    @Override
    public void setAllModificable(boolean modif) {
    	for(Entry<Object, DataComponent> entry:this.dataComponent.entrySet()) {
    		entry.getValue().setModifiable(modif);
    	}
    }

    @Override
    public void setAllEnabled(boolean en) {
    	for(Entry<Object, DataComponent> entry:this.dataComponent.entrySet()) {
    		entry.getValue().setEnabled(en);
    	}
    }

    @Override
    public void setGroupValue(Map<?, ?> value) {
    	for(Entry<Object, DataComponent> entry:this.dataComponent.entrySet()) {
    		entry.getValue().setValue(value.get(entry.getValue().getAttribute()));
    	}
    }

    @Override
    public String getLabel() {
        return "";
    }

    @Override
    public void initPermissions() {
    }

    @Override
    public boolean isRestricted() {
        return false;
    }

    @Override
    public List<Object> getAttributes() {
    	List<Object> v = new ArrayList<>();
    	for(Entry<Object, DataComponent> entry:this.dataComponent.entrySet()) {
    		v.add(entry.getKey());
    	}
        return v;
    }

}
