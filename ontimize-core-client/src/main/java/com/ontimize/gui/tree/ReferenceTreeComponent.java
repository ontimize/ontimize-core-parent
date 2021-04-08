package com.ontimize.gui.tree;

import java.util.Hashtable;

import com.ontimize.gui.Form;
import com.ontimize.gui.field.ReferenceComboDataField;
import com.ontimize.locator.EntityReferenceLocator;

public class ReferenceTreeComponent {

    protected ReferenceComboDataField comboReferenceDataField = null;

    public void setReferenceLocator(EntityReferenceLocator locator) {
        this.comboReferenceDataField.setReferenceLocator(locator);
        this.comboReferenceDataField.initCache();
    }

    public void setParentForm(Form form) {
        this.comboReferenceDataField.setParentForm(form);
    }

    public String getAttribute() {
        return this.comboReferenceDataField.getAttribute().toString();
    }

    public String getDescriptionForCode(Object code) {
        return this.comboReferenceDataField.getCodeDescription(code);
    }

    public ReferenceTreeComponent(Hashtable parameters) {
        parameters.remove("cachetime");
        this.comboReferenceDataField = new ReferenceComboDataField(parameters);
        this.comboReferenceDataField.setUseCacheManager(false);
    }

}
