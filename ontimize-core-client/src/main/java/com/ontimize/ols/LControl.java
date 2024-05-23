package com.ontimize.ols;

import java.util.Map;

public interface LControl {

    public Map<Object, Object> getParameters() throws Exception;

    public Map<Object, Object> updateL(Map<Object, Object> h) throws Exception;

}
