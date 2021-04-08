package com.ontimize.gui.table;

import java.util.List;
import java.util.Map;

import javax.swing.JMenuItem;

public class AvgGroupOperation implements GroupOperation {

    public static String headerText = "AVG";

    @Override
    public Number getOperationValue(List list, List rowIndexes, Map requiredColsValues) {
        double d = 0.0;
        for (int i = 0; i < list.size(); i++) {
            Object v = list.get(i);
            if ((v != null) && (v instanceof Number)) {
                d = d + ((Number) v).doubleValue();
            }
        }
        return new Double(d / list.size());
    }

    /**
     * Not implemented
     */
    @Override
    public JMenuItem getItem() {
        return null;
    }

    /**
     * Not used
     */
    @Override
    public int getOperationId() {
        return TableSorter.AVG;
    }

    /**
     * Not used
     */
    @Override
    public String getOperationText() {
        return null;
    }

    @Override
    public String getHeaderText() {
        return AvgGroupOperation.headerText;
    }

    @Override
    public List<String> getRequiredColumns() {
        return null;
    }

}
