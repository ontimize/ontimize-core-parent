package com.ontimize.gui.button;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApToolBarPopupButton;
import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.Form;
import com.ontimize.gui.SecureElement;
import com.ontimize.gui.field.AccessForm;
import com.ontimize.gui.i18n.Internationalization;
import com.ontimize.gui.images.ImageManager;

/**
 * This class implements a pop-up menu with options and a two-state button.
 * <p>
 *
 * @author Imatia Innovation
 */
public class FormHeaderPopupButton extends ApToolBarPopupButton implements AccessForm {

	private static final Logger logger = LoggerFactory.getLogger(FormHeaderPopupButton.class);

	/**
	 * The name of class. Used by L&F to put UI properties.
	 *
	 * @since 5.2072EN
	 */
	public static final String FORMHEADERPOPUPBUTTON = "FormHeaderPopupButton";

	public static Boolean defaultPaintFocus;

	public static Boolean defaultContentAreaFilled;

	public static Boolean defaultCapable;

	public static boolean createRolloverIcon = false;

	/**
	 * The reference to parent form. By default, false.
	 */
	protected Form parentForm = null;

	/**
	 * The class constructor. Calls to <code>super()</code> with parameters and sets margin. <p>
	 * 
	 * @param parameters
	 *            the <code>Map</code> with parameters. Adds the next parameters. <p> <Table BORDER=1 CELLPADDING=3 CELLSPACING=1 RULES=ROWS FRAME=BOX> <tr> <td><b>attribute</td>
	 *            <td><b>values</td> <td><b>default</td> <td><b>required</td> <td><b>meaning</td> </tr> <tr> <td>keys</td> <td><i>key1;key2;...;keyn</td> <td></td> <td>yes</td>
	 *            <td>The keys for field.</td> </tr> <tr> <td>texts</td> <td><i>text1;text2;...;textn</td> <td></td> <td>no</td> <td>The text showed in each option.</td> </tr> <tr>
	 *            <td>icons</td> <td><i>icon1;icon2;...;iconn</td> <td></td> <td>no</td> <td>The icon showed for each text. If <code>texts</code> and <code>icons</code> are both
	 *            presents should have the same size and it is possible to complete with <code>Null</code> values to indicate empty value. For example:<br>
	 *            <b>texts=query;delete;null;close<B><br> <b>icons=null;icons/delete.gif;null;icons/close.gif<B></td> </tr> </Table>
	 */
	public FormHeaderPopupButton(Map<Object, Object> parameters) {
		super(parameters);
		Object margin = parameters.get("margin");

		if (margin == null) {
			margin = "3;3;3;3";
		}

		try {
			this.setMargin(ApplicationManager.parseInsets((String) margin));
		} catch (Exception e) {
			if (ApplicationManager.DEBUG) {
				FormHeaderPopupButton.logger.error(null, e);
			}
		}

		Object keys = parameters.get("keys");
		if (keys == null) {
			throw new IllegalArgumentException("keys required");
		}
		List<String> vKeys = ApplicationManager.getTokensAt((String) keys, ";");
		// Popup element texts
		Object texts = parameters.get("texts");
		// Icons
		Object icons = parameters.get("icons");

		if ((texts != null) || (icons != null)) {
			List<String> vTexts = null;
			if (texts != null) {
				vTexts = ApplicationManager.getTokensAt(texts.toString(), ";");
			} else {
				vTexts = vKeys;
			}
			List<String> vIcons = null;
			if (icons != null) {
				vIcons = ApplicationManager.getTokensAt(icons.toString(), ";");
			}

			if ((vTexts != null) && (vIcons != null) && (vTexts.size() != vIcons.size())) {
				throw new IllegalArgumentException(
						this.getClass().toString() + " texts and icons must have identical number of elements: "
								+ vTexts.size() + " != " + vIcons.size());
			}

			int nKeys = vKeys.size();
			// Creates the elements
			int nelementsi = vIcons != null ? vIcons.size() : 0;
			int nelementst = vTexts != null ? vTexts.size() : 0;

			if (((nKeys != nelementsi) && (nelementsi > 0)) || (nKeys != nelementst)) {
				throw new IllegalArgumentException(
						this.getClass().toString() + "keys, texts and icons must have identical number of elements: "
								+ vKeys.size() + " != " + vTexts
								.size()
								+ " != " + vIcons.size());
			}

			for (int i = 0; i < nKeys; i++) {
				String sKey = vKeys.get(i);
				String text = vTexts != null ? (String) vTexts.get(i) : null;
				String icon = vIcons != null ? (String) vIcons.get(i) : null;

				Map<Object, Object> params = new HashMap<>();
				params.put("key", sKey);
				if ((text != null) && (text.length() > 0) && !text.equalsIgnoreCase("null")) {
					params.put("text", text);
				}
				if ((icon != null) && (icon.length() > 0) && !icon.equalsIgnoreCase("null")) {
					params.put("icon", icon);
				}

				FormHeaderButton item = this.createFormHeaderButton(params);
				this.addPopupElement(item, this.popup.getComponentCount());
			}
		}
	}

	protected FormHeaderButton createFormHeaderButton(Map<Object, Object> params) {
		FormHeaderButton item = new FormHeaderButton(params);
		item.setBorderPainted(false);
		item.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		return item;
	}


	@Override
	public void setParentForm(Form form) {
		this.parentForm = form;
		for (int i = 0; i < this.popup.getComponentCount(); i++) {
			if (this.popup.getComponent(i) instanceof AccessForm) {
				((AccessForm) this.popup.getComponent(i)).setParentForm(form);
			}
		}
	}

	@Override
	public void setResourceBundle(ResourceBundle resources) {
		for (int i = 0; i < this.popup.getComponentCount(); i++) {
			if (this.popup.getComponent(i) instanceof Internationalization) {
				((Internationalization) this.popup.getComponent(i)).setResourceBundle(resources);
			}
		}
	}

	@Override
	public void initPermissions() {
		// since 5.2076EN-0.3, permission for child buttons are checked
		super.initPermissions();
		if (this.popup != null) {
			for (int i = 0; i < this.popup.getComponentCount(); i++) {
				if (this.popup.getComponent(i) instanceof SecureElement) {
					((SecureElement) this.popup.getComponent(i)).initPermissions();
				}
			}
		}
	}

	@Override
	public Dimension getPreferredSize() {
		if (ApplicationManager.useOntimizePlaf) {
			return super.getPreferredSize();
		}
		Dimension d = super.getSwingPreferredSize();
		d.width += 6;
		return d;
	}

	@Override
	public String getName() {
		return FormHeaderPopupButton.FORMHEADERPOPUPBUTTON;
	}

	@Override
	public void setContentAreaFilled(boolean b) {
		if (FormHeaderPopupButton.defaultContentAreaFilled != null) {
			super.setContentAreaFilled(FormHeaderPopupButton.defaultContentAreaFilled.booleanValue());
			return;
		}
		super.setContentAreaFilled(b);
	}

	public boolean isDefaultCapable() {
		if (FormHeaderPopupButton.defaultCapable != null) {
			return FormHeaderPopupButton.defaultCapable.booleanValue();
		}
		return false;
	}

	@Override
	public void setFocusPainted(boolean b) {
		if (FormHeaderPopupButton.defaultPaintFocus != null) {
			super.setFocusPainted(FormHeaderPopupButton.defaultPaintFocus.booleanValue());
			return;
		}
		super.setFocusPainted(b);
	}

	@Override
	public Icon getRolloverIcon() {
		if (FormHeaderPopupButton.createRolloverIcon && (this.getIcon() instanceof ImageIcon)) {
			ImageIcon rollOverIcon = ImageManager.transparent((ImageIcon) this.getIcon(), 0.5f);
			this.setRolloverIcon(rollOverIcon);
			return rollOverIcon;
		}
		return super.getRolloverIcon();
	}

}
