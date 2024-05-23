package com.ontimize.gui;

import java.util.EventObject;
import java.util.Map;

import com.ontimize.jee.common.dto.EntityResult;

public class DataRecordEvent extends EventObject {

    public static final int UPDATE = 0;

    public static final int DELETE = 1;

    public static final int INSERT = 2;

    protected int type = 0;

    protected EntityResult rs = null;

    protected Map<Object, Object> keysValues = null;

    protected Map<Object, Object> attributesValues = null;

    public DataRecordEvent(Object source, int type, Map<Object, Object> kv, Map<Object, Object> av, EntityResult rs) {
        super(source);
        this.type = type;
        this.keysValues = kv;
        this.attributesValues = av;
        this.rs = rs;
    }

    public Map<Object, Object> getKeysValues() {
        return this.keysValues;
    }

    public Map<Object, Object> getAttributesValues() {
        return this.attributesValues;
    }

    public EntityResult getResult() {
        return this.rs;
    }

    public int getType() {
        return this.type;
    }

}
