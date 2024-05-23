package com.ontimize.gui.button;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.border.Border;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.Form;
import com.ontimize.gui.Freeable;
import com.ontimize.gui.field.AccessForm;
import com.ontimize.gui.field.FormComponent;
import com.ontimize.gui.field.IdentifiedElement;
import com.ontimize.jee.common.security.FormPermission;
import com.ontimize.security.ClientSecurityManager;
import com.ontimize.util.ParseUtils;
import com.ontimize.util.swing.ButtonSelection;

public abstract class AbstractButtonSelection extends ButtonSelection
implements FormComponent, AccessForm, IdentifiedElement, Freeable {

	private static final Logger logger = LoggerFactory.getLogger(AbstractButtonSelection.class);

	public static final String MODE = "mode";

	/**
	 * Parameter to specify the button alignment in the container with GridBagLayout. The function
	 * getConstraints() in the Component interface uses this property.
	 */
	protected int align = GridBagConstraints.NORTH;

	protected int verticalAlign = GridBagConstraints.NORTH;

	protected String icon = null;

	/**
	 * String to identify the button
	 */
	protected String buttonKey = null;

	protected Form parentForm = null;

	protected ResourceBundle bundle = null;

	protected String text = null;

	protected String tooltip = null;

	protected boolean ownerTooltip = false;

	protected String keyStrokeText = null;

	protected FormPermission visiblePermission = null;

	protected FormPermission enablePermission = null;

	protected MouseAdapter listenerHighlightButtons;

	protected boolean opaque;

	protected Border border;

	/**
	 * @see #init
	 * @param parameter
	 */
	public AbstractButtonSelection(Map<Object, Object> parameter) {
		super(parameter);
		this.init(parameter);
		this.init(this.highlight);
		this.changeButton(this.button);
		this.changeButton(this.menuButton);

	}

	@Override
	protected void init(boolean highlight) {
		super.init(highlight);
		if (this.text != null) {
			this.setText(this.text);
		}
	}

	/**
	 * Initialize the button with the parameters in the xml
	 */
	@Override
	public void init(Map<Object, Object> parameter) {
		Object rollover = parameter.get("highlight");
		if (rollover == null) {
			this.highlight = false;
		} else {
			if (rollover.toString().equalsIgnoreCase("yes")) {
				this.highlight = true;
			} else {
				this.highlight = false;
			}
		}

		Object alignment = parameter.get(Button.ALIGN);
		if (alignment == null) {
			this.align = GridBagConstraints.NORTH;
		} else {
			if (alignment.equals("left")) {
				this.align = GridBagConstraints.NORTHWEST;
			} else {
				if (alignment.equals("right")) {
					this.align = GridBagConstraints.NORTHEAST;
				} else {
					this.align = GridBagConstraints.NORTH;
				}
			}
		}

		Object valign = parameter.get(Button.VALIGN);
		if (valign == null) {
		} else {
			if (valign.equals("center")) {
				this.verticalAlign = GridBagConstraints.CENTER;
			} else {
				if (valign.equals("bottom")) {
					this.verticalAlign = GridBagConstraints.SOUTH;
				} else {
					this.verticalAlign = GridBagConstraints.NORTH;
				}
			}
		}

		// Parameter 'key'
		Object oKey = parameter.get(Button.KEY);
		if (oKey == null) {
			AbstractButtonSelection.logger.debug(this.getClass().toString() + " 'key' attribute is required");
		} else {
			this.buttonKey = oKey.toString();
		}

		Object icon = parameter.get("icon");
		if (icon == null) {
			this.icon = null;

		} else {
			this.icon = icon.toString();
		}

		// Parameter 'text'
		Object oButtonText = parameter.get(Button.TEXT);
		if (oButtonText == null) {
		} else {
			this.text = oButtonText.toString();
		}

		// Parameter 'tip'
		Object tip = parameter.get(Button.TIP);
		if (tip == null) {
			this.tooltip = this.text;
		} else {
			this.tooltip = tip.toString();
			this.ownerTooltip = true;
		}

		this.border = ParseUtils.getBorder((String) parameter.get("border"), null);
		this.opaque = ParseUtils.getBoolean((String) parameter.get("opaque"), true);
		boolean highlightButtons = ParseUtils.getBoolean((String) parameter.get("highlight"), false);
		if (highlightButtons) {
			this.listenerHighlightButtons = new MouseAdapter() {

				@Override
				public void mouseEntered(MouseEvent e) {
					((AbstractButton) e.getSource()).setOpaque(true);
					((AbstractButton) e.getSource()).setContentAreaFilled(true);
				}

				@Override
				public void mouseExited(MouseEvent e) {
					((AbstractButton) e.getSource()).setOpaque(false);
					((AbstractButton) e.getSource()).setContentAreaFilled(false);
				}
			};
		}

	}

	@Override
	public Object getConstraints(LayoutManager parentLayout) {
		if (parentLayout instanceof GridBagLayout) {
			// Determine the component alignment.
			int totalAlignment = this.align;
			switch (this.verticalAlign) {
				case GridBagConstraints.NORTH:
					totalAlignment = this.align;
					break;
				case GridBagConstraints.CENTER:
					switch (this.align) {
						case GridBagConstraints.NORTH:
							totalAlignment = GridBagConstraints.CENTER;
							break;
						case GridBagConstraints.NORTHEAST:
							totalAlignment = GridBagConstraints.EAST;
							break;
						case GridBagConstraints.NORTHWEST:
							totalAlignment = GridBagConstraints.WEST;
							break;
						default:
							break;
					}
					break;
				case GridBagConstraints.SOUTH:
					switch (this.align) {
						case GridBagConstraints.NORTH:
							totalAlignment = GridBagConstraints.SOUTH;

							break;
						case GridBagConstraints.NORTHEAST:
							totalAlignment = GridBagConstraints.SOUTHEAST;
							break;
						case GridBagConstraints.NORTHWEST:
							totalAlignment = GridBagConstraints.SOUTHWEST;
							break;
						default:
							break;
					}
					break;
				default:
					totalAlignment = this.align;
					break;
			}

			return new GridBagConstraints(0, 0, 1, 1, 0.01, 0, totalAlignment, GridBagConstraints.NONE,
					new Insets(1, 1, 1, 1), 0, 0);
		} else {
			return null;
		}
	}

	@Override
	public Vector getTextsToTranslate() {
		return null;
	}

	@Override
	public void setComponentLocale(Locale l) {

	}

	@Override
	public void setResourceBundle(ResourceBundle resources) {
		this.bundle = resources;
		String localeTexts = null;
		try {
			String textKey = this.text;
			if (resources != null) {
				localeTexts = resources.getString(textKey);
			}
			if (localeTexts != null) {
				super.setText(localeTexts);
			}
		} catch (Exception e) {
			if (ApplicationManager.DEBUG) {
				AbstractButtonSelection.logger.error(this.getClass().toString() + " : " + e.getMessage(), e);
			}
		}
		this.updateTip();
	}

	protected void updateTip() {
		String tipKey = this.tooltip;
		if (tipKey == null) {
			return;
		}
		try {
			if (this.bundle != null) {
				this.setToolTipText(this.getTextWithKeyStroke(this.bundle.getString(tipKey)));
			} else {
				this.setToolTipText(this.getTextWithKeyStroke(tipKey));
			}
		} catch (Exception e) {
			this.setToolTipText(this.getTextWithKeyStroke(tipKey));
			if (ApplicationManager.DEBUG) {
				AbstractButtonSelection.logger.error(this.getClass().toString() + " : " + e.getMessage(), e);
			}
		}
	}

	protected String getTextWithKeyStroke(String text) {
		if (this.keyStrokeText == null) {
			return text;
		} else {
			return text + " (" + this.keyStrokeText + ")";
		}
	}

	@Override
	public void setParentForm(Form form) {
		this.parentForm = form;
	}

	@Override
	public void initPermissions() {
		if (ApplicationManager.getClientSecurityManager() != null) {
			ClientSecurityManager.registerSecuredElement(this);
		}
		boolean pVisible = this.checkVisiblePermission();
		if (!pVisible) {
			this.setVisible(false);
		}

		boolean pEnabled = this.checkEnabledPermission();
		if (!pEnabled) {
			this.setEnabled(false);
		}

	}

	protected boolean restricted = false;

	@Override
	public boolean isRestricted() {
		return this.restricted;
	}

	public void setKeyStrokeText(String keyStrokeText) {
		this.keyStrokeText = keyStrokeText;
		this.updateTip();
	}

	public String getKey() {
		return this.buttonKey;
	}

	@Override
	public Object getAttribute() {
		return this.buttonKey;
	}

	protected boolean checkVisiblePermission() {
		ClientSecurityManager manager = ApplicationManager.getClientSecurityManager();
		if (manager != null) {
			if (this.visiblePermission == null) {
				if ((this.buttonKey != null) && (this.parentForm != null)) {
					this.visiblePermission = new FormPermission(this.parentForm.getArchiveName(), "visible",
							this.buttonKey, true);
				}
			}
			try {
				// Check to show
				if (this.visiblePermission != null) {
					manager.checkPermission(this.visiblePermission);
				}
				this.restricted = false;
				return true;
			} catch (Exception e) {
				this.restricted = true;
				if (e instanceof NullPointerException) {
					AbstractButtonSelection.logger.error(null, e);
				}
				if (ApplicationManager.DEBUG_SECURITY) {
					AbstractButtonSelection.logger.error(this.getClass().toString() + ": " + e.getMessage(), e);
				}
				return false;
			}
		} else {
			return true;
		}
	}

	protected boolean checkEnabledPermission() {
		ClientSecurityManager manager = ApplicationManager.getClientSecurityManager();
		if (manager != null) {
			if (this.enablePermission == null) {
				if ((this.buttonKey != null) && (this.parentForm != null)) {
					this.enablePermission = new FormPermission(this.parentForm.getArchiveName(), "enabled",
							this.buttonKey, true);
				}
			}
			try {
				// Check to enable
				if (this.enablePermission != null) {
					manager.checkPermission(this.enablePermission);
				}
				this.restricted = false;
				return true;
			} catch (Exception e) {
				this.restricted = true;
				if (e instanceof NullPointerException) {
					AbstractButtonSelection.logger.error(null, e);
				}
				if (ApplicationManager.DEBUG_SECURITY) {
					AbstractButtonSelection.logger.debug(this.getClass().toString() + ": " + e.getMessage());
				}
				return false;
			}
		} else {
			return true;
		}
	}

	protected void changeButton(JButton button) {
		if (button != null) {
			// button.setFocusPainted(false);
			if (this.border != null) {
				button.setBorder(this.border);
			}
			if (!this.opaque) {
				button.setOpaque(false);
				button.setContentAreaFilled(false);
			}
			if (this.listenerHighlightButtons != null) {
				button.addMouseListener(this.listenerHighlightButtons);
			}
		}
	}

	@Override
	public void free() {
		// TODO Auto-generated method stub

	}

}
