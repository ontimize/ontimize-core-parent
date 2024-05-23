package com.ontimize.gui.container;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.Map;

import javax.swing.border.Border;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.field.RadioButtonDataField;
import com.ontimize.util.ParseUtils;
import com.ontimize.util.swing.NullableButtonGroup;

/**
 * This class creates a group of radio buttons grouped in a column.
 * <p>
 *
 * @author Imatia Innovation
 */
public class ColumnRadioButtonGroup extends Column {

	private static final Logger logger = LoggerFactory.getLogger(ColumnRadioButtonGroup.class);

	/**
	 * An instance of container button group.
	 */
	protected NullableButtonGroup buttonGroup;

	/**
	 * Boolean to allow null selection.
	 */
	protected boolean allowNullSelection = false;

	/**
	 * The reference to init selection. By default, null.
	 */
	protected String initSelection = null;

    /**
	 * The class constructor. Calls to <code>super()</code> with <code>Map</code> parameters and initializes parameters. <p>
	 *
	 */
	public ColumnRadioButtonGroup(Map<Object, Object> parameters) {
		super(parameters);
		this.init(parameters);
	}

    /**
	 * Initializes parameters. <p>
	 * 
	 * @param parameters
	 *            the <code>Map</code> with parameters. Adds the next parameters:
	 *
	 *            <p>
	 *
	 *
	 *            <Table BORDER=1 CELLPADDING=3 CELLSPACING=1 RULES=ROWS FRAME=BOX> <tr> <td><b>attribute</td> <td><b>values</td> <td><b>default</td> <td><b>required</td>
	 *            <td><b>meaning</td> </tr>
	 *
	 *            <tr> <td>selected</td> <td></td> <td></td> <td>no</td> <td>The initial selection for radio buttons.</td> </tr>
	 *
	 *            <tr> <td>nullselection</td> <td>yes/no</td> <td>no</td> <td>no</td> <td>By default this component always tries to select one component placed inside. With this
	 *            parameter is allowed null selection (a non-visible radio button is selected).</td> </tr>
	 *
	 *            </TABLE>
	 *
	 */
	@Override
	public void init(Map<Object, Object> parameters) {
		super.init(parameters);
		if (this.title != null) {
			Border border = this.getBorder();

			// This color can't be set here
			/*
			 * if ((border != null) && (border instanceof TitledBorder)) { ((TitledBorder)
			 * border).setTitleColor(Color.blue); ((TitledBorder) border).setBorder(new
			 * EtchedBorder(EtchedBorder.LOWERED)); }
			 */

		}

		if (parameters.containsKey("nullselection")) {
			this.allowNullSelection = ParseUtils.getBoolean((String) parameters.get("nullselection"),
					this.allowNullSelection);
		}

		this.buttonGroup = new NullableButtonGroup(this.allowNullSelection);

		Object selected = parameters.get("selected");
		if (selected != null) {
			this.initSelection = selected.toString();
		} else {
			ColumnRadioButtonGroup.logger
			.debug(this.getClass().toString() + " .Info: 'selected' parameter hasn't been specified");
		}
	}

	@Override
	public Object getConstraints(LayoutManager parentLayout) {
		if (parentLayout instanceof GridBagLayout) {
			return new GridBagConstraints(GridBagConstraints.RELATIVE, 0, 1, 1, 0, 0, GridBagConstraints.CENTER,
					GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);
		} else {
			return null;
		}
	}

	/**
	 * Gets the button selected from list.
	 * <p>
	 * @return the selected radio button
	 */
	public RadioButtonDataField getSelected() {
		Component[] comp = this.getComponents();
		for (Component element : comp) {
			if (element instanceof RadioButtonDataField) {
				if (((RadioButtonDataField) element).isSelected()) {
					return (RadioButtonDataField) element;
				}
			}
		}
		return null;
	}

	@Override
	public void add(Component comp, Object constraints) {
		super.add(comp, constraints);
		if (comp instanceof RadioButtonDataField) {
			this.buttonGroup.add(((RadioButtonDataField) comp).getAbstractButton());
			if (this.initSelection != null) {
				if (this.initSelection.equals(((RadioButtonDataField) comp).getAttribute())) {
					((RadioButtonDataField) comp).setValue(new Short((short) 1));
				}
			}
		}
	}

}
