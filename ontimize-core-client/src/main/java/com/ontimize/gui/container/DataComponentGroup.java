package com.ontimize.gui.container;

/**
 * This interface represents a basic group of data components in a panel.
 */
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.ontimize.gui.field.FormComponent;
import com.ontimize.gui.field.IdentifiedElement;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Company:
 * </p>
 */
public interface DataComponentGroup extends FormComponent, IdentifiedElement {

    /**
     * Gets a data set with field values.<br>
     * @return The key is the attribute of the data field and the value is the data field value
     */
    public Map<Object, Object> getGroupValue();

    public void setGroupValue(Map<?, ?> value);

    public String getLabel();

    public List<Object> getAttributes();

    public void setAllEnabled(boolean en);

    public void setAllModificable(boolean modif);

}
