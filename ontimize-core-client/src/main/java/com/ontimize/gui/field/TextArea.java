package com.ontimize.gui.field;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JTextArea;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.ColorConstants;
import com.ontimize.gui.Freeable;

public class TextArea extends JTextArea implements FormComponent, Freeable {

	private static final Logger logger = LoggerFactory.getLogger(TextArea.class);

	protected String textKey = "NO_TEXT";

	protected ResourceBundle resources = null;

	protected int fontSize = 12;

	protected Color fontColor = Color.black;

	public TextArea(Map<Object, Object> parameters) {
		this.init(parameters);
		this.setBorder(null);
		this.setOpaque(false);
		this.setEditable(false);
		this.setLineWrap(true);
		this.setWrapStyleWord(true);
	}

	@Override
	public Object getConstraints(LayoutManager parentLayout) {
		if (parentLayout instanceof GridBagLayout) {
			return new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH,
					new Insets(5, 5, 5, 5), 0, 0);
		} else {
			return null;
		}
	}

	@Override
	public void init(Map<Object, Object> parameters) {
		Object oText = parameters.get("text");
		if (oText != null) {
			this.textKey = oText.toString();
			try {
				if (this.resources != null) {
					this.setText(this.resources.getString(this.textKey));
				} else {
					this.setText(this.textKey);
				}
			} catch (Exception exc) {
				this.setText(this.textKey);
				if (ApplicationManager.DEBUG) {
					TextArea.logger.debug(this.getClass().toString() + " : " + exc.getMessage(), exc);
				}
			}
		}
		Object fontsize = parameters.get("fontsize");
		if (fontsize != null) {
			try {
				this.fontSize = Integer.parseInt(fontsize.toString());
				this.setFontSize(this.fontSize);
			} catch (Exception exc) {
				TextArea.logger.error(this.getClass().toString() + " : Error in parameter 'fontsize'", exc);
			}
		}

		Object fontcolor = parameters.get("fontcolor");
		if (fontcolor != null) {
			try {
				this.fontColor = ColorConstants.parseColor(fontcolor.toString());
				this.setFontColor(this.fontColor);
			} catch (Exception exc) {
				TextArea.logger.error(this.getClass().toString() + " Error in parameter 'fontcolor':" + exc.getMessage(), exc);
			}
		}
	}

	@Override
	public List<String> getTextsToTranslate() {
		List<String> v = new ArrayList<>();
		v.add(this.textKey);
		return v;
	}

	@Override
	public void setResourceBundle(ResourceBundle resources) {
		this.resources = resources;
		try {
			if (resources != null) {
				this.setText(this.resources.getString(this.textKey));
			} else {
				this.setText(this.textKey);
			}
		} catch (Exception exc) {
			this.setText(this.textKey);
			if (ApplicationManager.DEBUG) {
				TextArea.logger.debug(this.getClass().toString() + " : " + exc.getMessage(), exc);
			}
		}
	}

	@Override
	public void setComponentLocale(Locale l) {
	}

	public void setFontSize(int fontSize) {
		try {
			this.setFont(this.getFont().deriveFont((float) fontSize));
		} catch (Exception exc) {
			TextArea.logger.error(this.getClass().toString() + " : Error setting the font size" + fontSize, exc);
		}
	}

	public void setFontColor(Color fontColor) {
		try {
			this.setForeground(fontColor);
		} catch (Exception exc) {
			TextArea.logger.error(this.getClass().toString() + " : Error setting the font color" + fontColor, exc);
		}
	}

	@Override
	public void updateUI() {
		super.updateUI();
		try {
			this.setFont(UIManager.getFont("Label.font"));
		} catch (Exception exc) {
			TextArea.logger.error(null, exc);
		}
	}

	@Override
	public void free() {
		// TODO Auto-generated method stub

	}

}
