package com.ontimize.util.serializer.xml.adapters;

import com.ontimize.db.SQLStatementBuilder.BasicOperator;
import org.apache.commons.lang.StringEscapeUtils;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class XmlBasicOperatorAdapter extends XmlAdapter<String, BasicOperator> {

    @Override
    public BasicOperator unmarshal(String v) throws Exception {
        return new BasicOperator(StringEscapeUtils.unescapeHtml(v));
    }

    @Override
    public String marshal(BasicOperator v) throws Exception {
        return v.toString();
    }

}

