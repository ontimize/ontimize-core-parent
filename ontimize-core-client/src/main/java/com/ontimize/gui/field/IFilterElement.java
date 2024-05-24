package com.ontimize.gui.field;

import java.util.List;
import java.util.Map;

public interface IFilterElement {

    public boolean hasParentKeys();

    public List<Object> getParentKeyList();

    public Map<Object, Object> getParentKeyValues();

}
