package com.ontimize.report.item;

import java.util.List;
import java.util.ResourceBundle;

public class SelectableMultipleItem {

	List<Object> itemList = null;

    public SelectableMultipleItem(List<Object> list, ResourceBundle res) {
        this.itemList = list;
    }

    public List<Object> getItemList() {
        return this.itemList;
    }

    @Override
    public String toString() {
        String sValue = "";
        for (int i = 0; i < this.itemList.size(); i++) {
            Object ite = this.itemList.get(i);
            sValue += sValue.length() == 0 ? ite.toString() : "," + ite.toString();
        }
        return sValue;
    }

    public String getText() {
        String sValue = "";
        for (int i = 0; i < this.itemList.size(); i++) {
            com.ontimize.report.item.SelectableItem ite = (com.ontimize.report.item.SelectableItem) this.itemList
                .get(i);
            sValue += sValue.length() == 0 ? ite.getText() : "," + ite.getText();
        }
        return sValue;

    }

}
