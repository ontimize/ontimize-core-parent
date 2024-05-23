package com.ontimize.gui.field;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.Form;
import com.ontimize.gui.Freeable;
import com.ontimize.jee.common.security.FormPermission;
import com.ontimize.security.ClientSecurityManager;

public class AnalogImage extends JPanel implements DataComponent, AccessForm, Freeable {

	private static final Logger logger = LoggerFactory.getLogger(AnalogImage.class);

	protected boolean required = false;

	protected boolean show = true;

	protected boolean modificable = true;

	protected short active = 0;

	protected Object savedValue = null;

	protected Number value = null;

	protected int fontSize = 12;

	protected Color fontColor = Color.black;

	public int x = 0;

	public int y = 0;

	protected String attribute = null;

	protected int width = -1;

	protected int high = -1;

	protected JLabel attributeLabel = new JLabel();

	protected JPanel panelLabel = new JPanel();

	protected JImageAnalog panelImage = null;

	protected Form parentForm = null;

	protected FormPermission visiblePermission = null;

	protected FormPermission enabledPermission = null;

	class JImageAnalog extends JImage {

		public JImageAnalog(Map<Object, Object> parameters) {
			super(parameters);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (AnalogImage.this.value != null) {
				Color c = g.getColor();
				g.setFont(g.getFont().deriveFont((float) AnalogImage.this.fontSize));
				g.setFont(g.getFont().deriveFont(Font.PLAIN, AffineTransform.getScaleInstance(1.0, 2.0)));
				g.setColor(AnalogImage.this.fontColor);
				String text = Double.toString(AnalogImage.this.value.doubleValue());
				FontMetrics fontMetrics = g.getFontMetrics();
				int stringWidth = fontMetrics.stringWidth(text);
				g.drawString(text, (this.getSize().width - stringWidth) / 2,
						(this.getSize().height / 2) + (fontMetrics.getHeight() / 2 / 2));
				g.setColor(c);
			}
		}

	}

	public AnalogImage(Map<Object, Object> parameters) {
		super();
		this.init(parameters);
		this.panelImage = new JImageAnalog(parameters);
		this.setLayout(new GridBagLayout());
		this.add(this.panelImage, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(this.panelLabel, new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.panelLabel.add(this.attributeLabel);
		try {
			this.attributeLabel.setFont(this.attributeLabel.getFont().deriveFont(Font.BOLD));
		} catch (Exception e) {
			AnalogImage.logger.error(
					this.getClass().toString() + ": " + "Error changing attribute text font type: " + e.getMessage(),
					e);
		}
		this.panelImage.setOpaque(false);
		this.panelLabel.setOpaque(false);
		this.attributeLabel.setOpaque(false);
		this.setOpaque(false);
		this.setSize(this.getPreferredSize());
	}

	@Override
	public void init(Map<Object, Object> parameters) {
		// Parameter: Attribute 'attr'
		Object atrib = parameters.get("attr");
		if (atrib == null) {
		} else {
			this.attribute = atrib.toString();
			this.attributeLabel.setText(this.attribute);
		}
		// Parameter Position X: 'x'
		Object posicionX = parameters.get("x");
		if (posicionX == null) {
			AnalogImage.logger.debug(this.getClass().toString() + " : 'x' parameter not specified");
		} else {
			try {
				this.x = Integer.parseInt(posicionX.toString());
			} catch (Exception e) {
				AnalogImage.logger.error(" Error in image position ('x' must be an Integer)", e);
			}
		}
		// Parameter Position Y
		Object posicionY = parameters.get("y");
		if (posicionY == null) {
			AnalogImage.logger.debug(this.getClass().toString() + ": 'y' parameter not specified");
		} else {
			try {
				this.y = Integer.parseInt(posicionY.toString());
			} catch (Exception e) {
				AnalogImage.logger.error("Error in image position ('y' must be an Integer)", e);
			}
		}

		Object visible = parameters.get("visible");
		if (visible != null) {
			if (visible.equals("no")) {
				this.show = false;
			} else {
				this.show = true;
			}
		}

		Object required = parameters.get("required");
		if (required != null) {
			if (required.equals("yes")) {
				this.required = true;
			} else {
				this.required = false;
			}
		}

		// 'fontsize'
		Object fontsize = parameters.get("fontsize");
		if (fontsize != null) {
			try {
				this.fontSize = Integer.parseInt(fontsize.toString());
			} catch (Exception e) {
				AnalogImage.logger
				.error(this.getClass().toString() + " : Error in 'fontsize' paramater ." + e.getMessage(), e);
			}
		}

		// 'fontcolor'
		Object fontcolor = parameters.get("fontcolor");
		if (fontcolor != null) {
			try {
				StringTokenizer st = new StringTokenizer(fontcolor.toString(), ";");
				int tokens = st.countTokens();
				if (tokens != 3) {
					AnalogImage.logger.debug(this.getClass().toString()
							+ " : Error in 'fontcolor' parameter. Set valid RGB values separated by ';'. Example: \"255;30;255 \"");
				} else {
					String r = st.nextToken();
					String g = st.nextToken();
					String b = st.nextToken();
					int rf = Integer.parseInt(r);
					int gf = Integer.parseInt(g);
					int bf = Integer.parseInt(b);
					this.fontColor = new Color(rf, gf, bf);
				}
			} catch (Exception e) {
				AnalogImage.logger
				.error(this.getClass().toString() + " : Error in 'fontcolor' parameter." + e.getMessage(), e);
			}
		}
	}

	@Override
	public void setComponentLocale(Locale l) {
	}

	@Override
	public List<String> getTextsToTranslate() {
		List<String> v = new ArrayList<>();
		v.add(this.attribute);
		return v;
	}

	@Override
	public void setResourceBundle(ResourceBundle resources) {
		try {
			if (resources != null) {
				this.attributeLabel.setText(resources.getString(this.attribute));
			}
			this.attributeLabel.setText(this.attribute);
		} catch (Exception e) {
			this.attributeLabel.setText(this.attribute);
			if (com.ontimize.gui.ApplicationManager.DEBUG) {
				AnalogImage.logger.debug(this.getClass().toString() + " : " + e.getMessage(), e);
			}
		}
	}

	@Override
	public Object getConstraints(LayoutManager layout) {
		// Return the point in the top left corner of the image
		return new Point((int) (this.x - this.getImageOffset().getX()), this.y);
	}

	@Override
	public Object getAttribute() {
		return this.attribute;
	}

	@Override
	public String getLabelComponentText() {
		return null;
	}

	@Override
	public Object getValue() {
		return this.value;
	}

	@Override
	public void setValue(Object value) {
		if (value instanceof Number) {
			this.value = (Number) value;
			this.panelImage.repaint();
		} else {
			this.deleteData();
		}
	}

	@Override
	public void deleteData() {
		this.value = null;
		this.savedValue = this.getValue();
		this.panelImage.repaint();
	}

	@Override
	public boolean isEmpty() {
		return this.value == null ? true : false;
	}

	@Override
	public boolean isModifiable() {
		return this.modificable;
	}

	@Override
	public void setModifiable(boolean modif) {
		this.modificable = modif;
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public int getSQLDataType() {
		return java.sql.Types.DOUBLE;
	}

	@Override
	public boolean isRequired() {
		return this.required;
	}

	@Override
	public void setRequired(boolean required) {
		this.required = required;
	}

	@Override
	public boolean isModified() {
		return false;
	}

	public Point getImageOffset() {
		int xPixels = (this.getSize().width - this.panelImage.getPreferredSize().width) / 2;
		if (xPixels > 0) {
			return new Point(xPixels, 0);
		}
		return new Point(0, 0);
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (enabled) {
			boolean permission = this.checkEnabledPermission();
			if (!permission) {
				return;
			}
		}
		super.setEnabled(enabled);
	}

	@Override
	public void setVisible(boolean vis) {
		if (vis) {
			boolean permission = this.checkVisiblePermission();
			if (!permission) {
				return;
			}
		}
		super.setVisible(vis);
	}

	@Override
	public void initPermissions() {
		boolean pVisible = this.checkVisiblePermission();
		if (!pVisible) {
			this.setVisible(false);
		}

		boolean pEnabled = this.checkEnabledPermission();
		if (!pEnabled) {
			this.setEnabled(false);
		}

	}

	protected boolean checkVisiblePermission() {
		ClientSecurityManager manager = ApplicationManager.getClientSecurityManager();
		if (manager != null) {
			if (this.visiblePermission == null) {
				if ((this.attribute != null) && (this.parentForm != null)) {
					this.visiblePermission = new FormPermission(this.parentForm.getArchiveName(), "visible",
							this.attribute.toString(), true);
				}
			}
			try {
				// Checks to show
				if (this.visiblePermission != null) {
					manager.checkPermission(this.visiblePermission);
				}
				return true;
			} catch (Exception e) {
				if (e instanceof NullPointerException) {
					AnalogImage.logger.error(null, e);
				}
				if (ApplicationManager.DEBUG_SECURITY) {
					AnalogImage.logger.debug(this.getClass().toString() + ": " + e.getMessage(), e);
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
			if (this.enabledPermission == null) {
				if ((this.attribute != null) && (this.parentForm != null)) {
					this.enabledPermission = new FormPermission(this.parentForm.getArchiveName(), "enabled",
							this.attribute.toString(), true);
				}
			}
			try {
				// Check to show
				if (this.enabledPermission != null) {
					manager.checkPermission(this.enabledPermission);
				}
				this.restricted = false;
				return true;
			} catch (Exception e) {
				this.restricted = true;
				if (e instanceof NullPointerException) {
					AnalogImage.logger.error(null, e);
				}
				if (ApplicationManager.DEBUG_SECURITY) {
					AnalogImage.logger.debug(this.getClass().toString() + ": " + e.getMessage(), e);
				}
				return false;
			}
		} else {
			return true;
		}
	}

	@Override
	public void setParentForm(Form f) {
		this.parentForm = f;
	}

	protected boolean restricted = false;

	@Override
	public boolean isRestricted() {
		return this.restricted;
	}

	@Override
	public void free() {
		// TODO Auto-generated method stub

	}

}
