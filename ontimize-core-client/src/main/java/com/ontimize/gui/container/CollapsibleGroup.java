package com.ontimize.gui.container;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.MediaTracker;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.ColorConstants;
import com.ontimize.gui.Form;
import com.ontimize.gui.Freeable;
import com.ontimize.gui.field.AccessForm;
import com.ontimize.gui.field.DataField;
import com.ontimize.gui.field.FormComponent;
import com.ontimize.gui.field.IdentifiedElement;
import com.ontimize.gui.i18n.Internationalization;
import com.ontimize.gui.images.ImageManager;
import com.ontimize.jee.common.security.FormPermission;
import com.ontimize.security.ClientSecurityManager;
import com.ontimize.util.ParseUtils;
import com.ontimize.util.swing.JCollapsibleGroupPanel.JCollapsibleGroup;
import com.ontimize.util.swing.JCollapsibleGroupPanel.JCollapsibleGroupHeader;

/**
 * This class implements a deployable panel into a <code>CollapsibleGroupPanel</code> container.
 * Each CollapsibleGroup is composed by a Header and a Body. The Header is the visible part when the
 * component is collapsed and the one that contains the title of the group and the responsible of
 * the motion of the component. The body is the motion part of the component and the one that
 * functions as a container of elements.
 * <p>
 *
 * @author Imatia Innovation
 */
public class CollapsibleGroup extends JCollapsibleGroup
implements FormComponent, IdentifiedElement, AccessForm, Freeable {

	private static final Logger logger = LoggerFactory.getLogger(CollapsibleGroup.class);

	/**
	 * Attribute to define the icon on collapsed state of the Collapsible Group Header.
	 */
	public static String ICON = "icon";

	/**
	 * Attribute to define the icon on deployed state of the Collapsible Group Header.
	 */
	public static String OPEN_ICON = "openicon";

	/**
	 * Attribute to define the icon on mouse rollover state of the Collapsible Group Header.
	 */
	public static String ROLLOVER_ICON = "rollovericon";

	/**
	 * Attribute to define background color of the Collapsible Group Header on collapsed state.
	 */
	public static String HEADER_BG_COLOR = "headerbgcolor";

	/**
	 * Attribute to define background color of the Collapsible Group Header on mouse rollover state.
	 */
	public static String HEADER_BG_ROLLOVER_COLOR = "headerbgrollovercolor";

	/**
	 * Attribute to define background color of the Collapsible Group Header on deployed state.
	 */
	public static String HEADER_BG_OPEN_COLOR = "headerbgopencolor";

	/**
	 * Attribute to define background image of the Collapsible Group Header.
	 */
	public static String HEADER_BG_IMAGE = "headerbgimage";

	/**
	 * Attribute to define background image of the Collapsible Group Header on mouse rollover state.
	 */
	public static String HEADER_BG_ROLLOVER_IMAGE = "headerbgrolloverimage";

	/**
	 * Attribute to define background color of the Collapsible Group Header on deployed state.
	 */
	public static String HEADER_BG_OPEN_IMAGE = "headerbgopenimage";

	/**
	 * Attribute to define foreground color of the Collapsible Group Header on collapsed state.
	 */
	public static String FG_COLOR = "fontcolor";

	/**
	 * Attribute to define foreground color of the Collapsible Group Header on mouse rollover state.
	 */
	public static String FG_ROLLOVER_COLOR = "fontrollovercolor";

	/**
	 * Attribute to define foreground color of the Collapsible Group Header on mouse pressed state.
	 */
	public static String FG_PRESSED_COLOR = "fontpressedcolor";

	/**
	 * Attribute to define foreground color of the Collapsible Group Header on deployed state.
	 */
	public static String FG_OPEN_COLOR = "fontopencolor";

	/**
	 * Attribute to define foreground shadow color of the Collapsible Group Header on collapsed state.
	 */
	public static String FG_SHADOW_COLOR = "fontshadowcolor";

	/**
	 * Attribute to define foreground shadow color of the Collapsible Group Header on mouse rollover
	 * state.
	 */
	public static String FG_SHADOW_ROLLOVER_COLOR = "fontshadowrollovercolor";

	/**
	 * Attribute to define foreground shadow color of the Collapsible Group Header on mouse pressed
	 * state.
	 */
	public static String FG_SHADOW_PRESSED_COLOR = "fontshadowpressedcolor";

	/**
	 * Attribute to define foreground shadow color of the Collapsible Group Header on deployed state.
	 */
	public static String FG_SHADOW_OPEN_COLOR = "fontshadowopencolor";

	/**
	 * A reference to background paint. By default, null.
	 */
	protected Paint backgroundPaint = null;

	/**
	 * A reference to specify a background image in the column.
	 */
	protected java.awt.Image backgroundImage = null;

	/**
	 * A reference to specify a texture paint.
	 */
	protected TexturePaint texturePaint;

	/**
	 * The Collapsible Group title. By default, "Group"
	 */
	protected String title = new String("Group");

	/**
	 * The attribute reference. By default, null.
	 */
	protected Object attribute = null;

	/**
	 * The visible permission reference. By default, null.
	 */
	protected FormPermission visiblePermission = null;

	/**
	 * The restricted condition. By default, false.
	 */
	protected boolean restricted = false;

	/**
	 * A reference to parent Form.
	 */
	protected Form parentForm;

	@Override
	public void setParentForm(Form form) {
		this.parentForm = form;
	}

	/**
	 * Init parameters and sets scroll and margins. <p>
	 *
	 * @param parameters
	 *            the map with parameters
	 */
	public CollapsibleGroup(Map<Object, Object> parameters) {
		super(parameters);

		this.addPropertyChangeListener("collapsed", evt -> {
			if (CollapsibleGroup.this.header != null) {
				((CollapsibleGroupHeader) CollapsibleGroup.this.header)
				.setCollapsed(CollapsibleGroup.this.collapsed);
			}
		});

		this.init(parameters);
	}

	@Override
	public LayoutManager getLayout() {
		return super.getLayout();
	}

	/**
	 * Initializes parameters.
	 * <p>
	 * <Table BORDER=1 CELLPADDING=3 CELLSPACING=1 RULES=ROWS FRAME=BOX>
	 * <tr>
	 * <td><b>attribute</td>
	 * <td><b>values</td>
	 * <td><b>default</td>
	 * <td><b>required</td>
	 * <td><b>meaning</td>
	 * </tr>
	 * <tr>
	 * <td>attr</td>
	 * <td></td>
	 * <td></td>
	 * <td>no (only required when title is not specified)</td>
	 * <td>Indicates the component attribute.</td>
	 * </tr>
	 * <tr>
	 * <td>title</td>
	 * <td></td>
	 * <td></td>
	 * <td>no (only required when attr is not specified)</td>
	 * <td>The title for component.</td>
	 * </tr>
	 * <tr>
	 * <td>opaque</td>
	 * <td>yes/no</td>
	 * <td>yes</td>
	 * <td>no</td>
	 * <td>Specifies if the component must be opaque or not.</td>
	 * </tr>
	 * <tr>
	 * <td>startshowed</td>
	 * <td>yes/no</td>
	 * <td>no</td>
	 * <td>no</td>
	 * <td>Specifies whether the Collapsible Group starts collpased or not.</td>
	 * </tr>
	 * <tr>
	 * <td>bgcolor</td>
	 * <td>A color</td>
	 * <td></td>
	 * <td>no</td>
	 * <td>Specifies the Collapsible Group background color.</td>
	 * </tr>
	 * <tr>
	 * <td>bgimage</td>
	 * <td></td>
	 * <td></td>
	 * <td>no</td>
	 * <td>The path to background image.</td>
	 * </tr>
	 * <tr>
	 * <td>bgpaint</td>
	 * <td></td>
	 * <td></td>
	 * <td>no</td>
	 * <td>Paint value to use in this container. See
	 * {@link ColorConstants#paintNameToPaint(String)}</td>
	 * </tr>
	 * <tr>
	 * <td>textureimage</td>
	 * <td></td>
	 * <td></td>
	 * <td>no</td>
	 * <td>Path to source icon to extract a image and create a <code>TexturePaint</code></td>
	 * </tr>
	 * <tr>
	 * <td>icon</td>
	 * <td></td>
	 * <td></td>
	 * <td>no</td>
	 * <td>The icon for Collapsible Group Header when is collapsed. If 'openicon' and 'rollovericon' are
	 * not specified this icon is the default icon.</td>
	 * </tr>
	 * <tr>
	 * <td>openicon</td>
	 * <td></td>
	 * <td></td>
	 * <td>no</td>
	 * <td>The icon for Collapsible Group Header when is deployed.</td>
	 * </tr>
	 * <tr>
	 * <td>rollovericon</td>
	 * <td></td>
	 * <td></td>
	 * <td>no</td>
	 * <td>The icon for Collapsible Group Header when is on mouse rollover event.</td>
	 * </tr>
	 * <tr>
	 * <td>headerbgcolor</td>
	 * <td>A color</td>
	 * <td></td>
	 * <td>no</td>
	 * <td>Specifies the Collapsible Group Header background color when is collapsed.</td>
	 * </tr>
	 * <tr>
	 * <td>headerbgrollovercolor</td>
	 * <td>A color</td>
	 * <td></td>
	 * <td>no</td>
	 * <td>Specifies the Collapsible Group Header background color when is on mouse rollover event.</td>
	 * </tr>
	 * <tr>
	 * <td>headerbgopencolor</td>
	 * <td>A color</td>
	 * <td></td>
	 * <td>no</td>
	 * <td>Specifies the Collapsible Group Header background color when is deployed.</td>
	 * </tr>
	 * <tr>
	 * <td>headerbgimage</td>
	 * <td>A color</td>
	 * <td></td>
	 * <td>no</td>
	 * <td>Specifies the path to background image of Collapsible Group Header when is collapsed.</td>
	 * </tr>
	 * <tr>
	 * <td>headerbgrolloverimage</td>
	 * <td>A color</td>
	 * <td></td>
	 * <td>no</td>
	 * <td>Specifies the path to background image of Collapsible Group Header background when is on
	 * mouse rollover event.</td>
	 * </tr>
	 * <tr>
	 * <td>headerbgopenimage</td>
	 * <td>A color</td>
	 * <td></td>
	 * <td>no</td>
	 * <td>Specifies the path to background image of Collapsible Group Header background when is
	 * deployed.</td>
	 * </tr>
	 * <tr>
	 * <td>font</td>
	 * <td>A string like : 'Arial-BOLD-18' (See #Font.decode())</td>
	 * <td>The default font for system</td>
	 * <td>no</td>
	 * <td>Font for Collapsible Group Header text.</td>
	 * </tr>
	 * <tr>
	 * <td>fontcolor</td>
	 * <td></td>
	 * <td>black</td>
	 * <td>no</td>
	 * <td>The font color of Collapsible Group Header text when is collapsed.</td>
	 * </tr>
	 * <tr>
	 * <td>fontrollovercolor</td>
	 * <td></td>
	 * <td>black</td>
	 * <td>no</td>
	 * <td>The font color of Collapsible Group Header text when is on mouse rollover event.</td>
	 * </tr>
	 * <tr>
	 * <td>fontpressedcolor</td>
	 * <td></td>
	 * <td>black</td>
	 * <td>no</td>
	 * <td>The font color of Collapsible Group Header text when is on mouse pressed event.</td>
	 * </tr>
	 * <tr>
	 * <td>fontopencolor</td>
	 * <td></td>
	 * <td>black</td>
	 * <td>no</td>
	 * <td>The font color of Collapsible Group Header text when is deployed.</td>
	 * </tr>
	 * <tr>
	 * <td>fontshadowcolor</td>
	 * <td></td>
	 * <td>white</td>
	 * <td>no</td>
	 * <td>The font shadow color of Collapsible Group Header text when is collapsed.</td>
	 * </tr>
	 * <tr>
	 * <td>fontshadowrollovercolor</td>
	 * <td></td>
	 * <td>white</td>
	 * <td>no</td>
	 * <td>The font shadow color of Collapsible Group Header text when is on mouse rollover event.</td>
	 * </tr>
	 * <tr>
	 * <td>fontshadowpressedcolor</td>
	 * <td></td>
	 * <td>white</td>
	 * <td>no</td>
	 * <td>The font shadow color of Collapsible Group Header text when is on mouse pressed event.</td>
	 * </tr>
	 * <tr>
	 * <td>fontshadowopencolor</td>
	 * <td></td>
	 * <td>white</td>
	 * <td>no</td>
	 * <td>The font shadow color of Collapsible Group Header text when is deployed.</td>
	 * </tr>
	 * </TABLE>
	 */
	@Override
	public void init(Map<Object, Object> parameters) {

		this.setAttributeAndTitle(parameters);
		this.setFont(parameters);
		this.setOpaque(parameters);

		boolean b = !ParseUtils.getBoolean((String) parameters.get("startshowed"), true);
		this.setCollapsed(b);

		this.setBackgroundColor(parameters);
		this.setBackgroundImage(parameters);
		this.setBackgroundPaint(parameters);
		this.setTexturePaint(parameters);

		// **********************************************************
		// Header Icon Parameters.
		// **********************************************************
		Object oIcon = parameters.get(CollapsibleGroup.ICON);
		ImageIcon icon = this.setHeaderIcon(oIcon);
		this.setHeaderOpenIcon(parameters, oIcon, icon);
		this.setHeaderRollOverIcon(parameters, oIcon, icon);

		// **********************************************************
		// Background Header Parameters.
		// **********************************************************
		this.setHeaderBackgroundColor(parameters);
		this.setHeaderBackgroundRollOverColor(parameters);
		this.setHeaderBackgroundOpenColor(parameters);
		this.setHeaderBackgroundImage(parameters);
		this.setHeaderBackgroundRollOverImage(parameters);
		this.setHeaderBackgroundOpenImage(parameters);

		// **********************************************************
		// Foreground Header Parameters.
		// **********************************************************
		this.setForegroundColor(parameters);
		this.setForegroundRollOverIcon(parameters);
		this.setForegroundPressedColor(parameters);
		this.setForegroundOpenColor(parameters);
		this.setForegroundShadowColor(parameters);
		this.setForegroundShadowRollOverColor(parameters);
		this.setForegroundShadowPressedColor(parameters);
		this.setForegroundShadowOpenCOlor(parameters);
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setForegroundShadowOpenCOlor(Map<Object, Object> parameters) {
		Object ofgshadowpencolor = parameters.get(CollapsibleGroup.FG_SHADOW_OPEN_COLOR);
		if (ofgshadowpencolor != null) {
			String fgopen = ofgshadowpencolor.toString();
			Color fgShadowOpenColor = this.getColor(fgopen, CollapsibleGroup.FG_SHADOW_OPEN_COLOR);
			if (fgShadowOpenColor != null) {
				((CollapsibleGroupHeader) this.header).setForegroundShadowOpenColor(fgShadowOpenColor);
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setForegroundShadowPressedColor(Map<Object, Object> parameters) {
		Object ofgshadowpressedcolor = parameters.get(CollapsibleGroup.FG_SHADOW_PRESSED_COLOR);
		if (ofgshadowpressedcolor != null) {
			String fgpressed = ofgshadowpressedcolor.toString();
			Color fgShadowPressedColor = this.getColor(fgpressed, CollapsibleGroup.FG_SHADOW_PRESSED_COLOR);
			if (fgShadowPressedColor != null) {
				((CollapsibleGroupHeader) this.header).setForegroundShadowPressedColor(fgShadowPressedColor);
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setForegroundShadowRollOverColor(Map<Object, Object> parameters) {
		Object ofgshadowrollovercolor = parameters.get(CollapsibleGroup.FG_SHADOW_ROLLOVER_COLOR);
		if (ofgshadowrollovercolor != null) {
			String fgrollover = ofgshadowrollovercolor.toString();
			Color fgShadowRollOverColor = this.getColor(fgrollover, CollapsibleGroup.FG_SHADOW_ROLLOVER_COLOR);
			if (fgShadowRollOverColor != null) {
				((CollapsibleGroupHeader) this.header).setForegroundShadowRollOverColor(fgShadowRollOverColor);
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setForegroundShadowColor(Map<Object, Object> parameters) {
		Object ofgshadowcolor = parameters.get(CollapsibleGroup.FG_SHADOW_COLOR);
		if (ofgshadowcolor != null) {
			String fg = ofgshadowcolor.toString();
			Color fgShadowColor = this.getColor(fg, CollapsibleGroup.FG_SHADOW_COLOR);
			if (fgShadowColor != null) {
				((CollapsibleGroupHeader) this.header).setForegroundShadowColor(fgShadowColor);
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setForegroundOpenColor(Map<Object, Object> parameters) {
		Object ofgpencolor = parameters.get(CollapsibleGroup.FG_OPEN_COLOR);
		if (ofgpencolor != null) {
			String fgopen = ofgpencolor.toString();
			Color fgOpenColor = this.getColor(fgopen, CollapsibleGroup.FG_OPEN_COLOR);
			if (fgOpenColor != null) {
				((CollapsibleGroupHeader) this.header).setForegroundOpenColor(fgOpenColor);
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setForegroundPressedColor(Map<Object, Object> parameters) {
		Object ofgpressedcolor = parameters.get(CollapsibleGroup.FG_PRESSED_COLOR);
		if (ofgpressedcolor != null) {
			String fgpressed = ofgpressedcolor.toString();
			Color fgPressedColor = this.getColor(fgpressed, CollapsibleGroup.FG_PRESSED_COLOR);
			if (fgPressedColor != null) {
				((CollapsibleGroupHeader) this.header).setForegroundPressedColor(fgPressedColor);
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setForegroundRollOverIcon(Map<Object, Object> parameters) {
		Object ofgrollovercolor = parameters.get(CollapsibleGroup.FG_ROLLOVER_COLOR);
		if (ofgrollovercolor != null) {
			String fgrollover = ofgrollovercolor.toString();
			Color fgRollOverColor = this.getColor(fgrollover, CollapsibleGroup.FG_ROLLOVER_COLOR);
			if (fgRollOverColor != null) {
				((CollapsibleGroupHeader) this.header).setForegroundRollOverColor(fgRollOverColor);
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setForegroundColor(Map<Object, Object> parameters) {
		Object ofgcolor = parameters.get(CollapsibleGroup.FG_COLOR);
		if (ofgcolor != null) {
			String fg = ofgcolor.toString();
			Color fgcolor = this.getColor(fg, CollapsibleGroup.FG_COLOR);
			if (fgcolor != null) {
				((CollapsibleGroupHeader) this.header).setForegroundColor(fgcolor);
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setHeaderBackgroundOpenImage(Map<Object, Object> parameters) {
		Object oheaderbgopenimage = parameters.get(CollapsibleGroup.HEADER_BG_OPEN_IMAGE);
		if (oheaderbgopenimage != null) {
			String openimg = oheaderbgopenimage.toString();
			Image headerbgOpenImage = this.getImage(openimg);
			if (headerbgOpenImage != null) {
				((CollapsibleGroupHeader) this.header).setBackgroundOpenImage(headerbgOpenImage);
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setHeaderBackgroundRollOverImage(Map<Object, Object> parameters) {
		Object oheaderbgrolloverimage = parameters.get(CollapsibleGroup.HEADER_BG_ROLLOVER_IMAGE);
		if (oheaderbgrolloverimage != null) {
			String rolloverimg = oheaderbgrolloverimage.toString();
			Image headerbgRollOverImage = this.getImage(rolloverimg);
			if (headerbgRollOverImage != null) {
				((CollapsibleGroupHeader) this.header).setBackgroundRollOverImage(headerbgRollOverImage);
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setHeaderBackgroundImage(Map<Object, Object> parameters) {
		Object oheaderbgimage = parameters.get(CollapsibleGroup.HEADER_BG_IMAGE);
		if (oheaderbgimage != null) {
			String imgbg = oheaderbgimage.toString();
			Image headerbgimg = this.getImage(imgbg);
			if (headerbgimg != null) {
				((CollapsibleGroupHeader) this.header).setBackgroundImage(headerbgimg);
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setHeaderBackgroundOpenColor(Map<Object, Object> parameters) {
		Object oheaderbgopencolor = parameters.get(CollapsibleGroup.HEADER_BG_OPEN_COLOR);
		if (oheaderbgopencolor != null) {
			String opencolor = oheaderbgopencolor.toString();
			Color headerbgOpenColor = this.getColor(opencolor, CollapsibleGroup.HEADER_BG_OPEN_COLOR);
			if (headerbgOpenColor != null) {
				((CollapsibleGroupHeader) this.header).setBackgroundOpenColor(headerbgOpenColor);
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setHeaderBackgroundRollOverColor(Map<Object, Object> parameters) {
		Object oheaderbgrollovercolor = parameters.get(CollapsibleGroup.HEADER_BG_ROLLOVER_COLOR);
		if (oheaderbgrollovercolor != null) {
			String rollover = oheaderbgrollovercolor.toString();
			Color headerbgRollOverColor = this.getColor(rollover, CollapsibleGroup.HEADER_BG_ROLLOVER_COLOR);
			if (headerbgRollOverColor != null) {
				((CollapsibleGroupHeader) this.header).setBackgroundRollOverColor(headerbgRollOverColor);
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setHeaderBackgroundColor(Map<Object, Object> parameters) {
		Object oheaderbgcolor = parameters.get(CollapsibleGroup.HEADER_BG_COLOR);
		if (oheaderbgcolor != null) {
			String bg = oheaderbgcolor.toString();
			Color headerbgcolor = this.getColor(bg, CollapsibleGroup.HEADER_BG_COLOR);
			if (headerbgcolor != null) {
				((CollapsibleGroupHeader) this.header).setBackgroundColor(headerbgcolor);
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 * @param oIcon
	 * @param icon
	 */
	protected void setHeaderRollOverIcon(Map<Object, Object> parameters, Object oIcon, ImageIcon icon) {
		Object oRollOverIcon = parameters.get(CollapsibleGroup.ROLLOVER_ICON);
		if ((oRollOverIcon == null) && (oIcon != null)) {
			((CollapsibleGroupHeader) this.header).setRollOverIcon(icon);
		} else if (oRollOverIcon != null) {
			String sIconFile = oRollOverIcon.toString();
			ImageIcon rollOverIcon = ImageManager.getIcon(sIconFile);
			if (rollOverIcon != null) {
				((CollapsibleGroupHeader) this.header).setRollOverIcon(rollOverIcon);
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 * @param oIcon
	 * @param icon
	 */
	protected void setHeaderOpenIcon(Map<Object, Object> parameters, Object oIcon, ImageIcon icon) {
		Object oOpenIcon = parameters.get(CollapsibleGroup.OPEN_ICON);
		if ((oOpenIcon == null) && (oIcon != null)) {
			this.header.setOpenIcon(icon);
		} else if (oOpenIcon != null) {
			String sIconFile = oOpenIcon.toString();
			ImageIcon openIcon = ImageManager.getIcon(sIconFile);
			if (openIcon != null) {
				this.header.setOpenIcon(openIcon);
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param oIcon
	 * @param icon
	 * @return
	 */
	protected ImageIcon setHeaderIcon(Object oIcon) {
		ImageIcon icon = null;
		if (oIcon != null) {
			String sIconFile = oIcon.toString();
			icon = ImageManager.getIcon(sIconFile);
			if (icon != null) {
				this.header.setCloseIcon(icon);
			}
		}
		return icon;
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setTexturePaint(Map<Object, Object> parameters) {
		Image im = ParseUtils.getImage((String) parameters.get(CollapsibleGroupPanel.TEXTUREIMAGE), null);
		if (im != null) {
			BufferedImage bi = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			bi.getGraphics().drawImage(im, 0, 0, null);
			this.texturePaint = new TexturePaint(bi, new Rectangle(bi.getWidth(null), bi.getHeight(null)));
		} else {
			this.texturePaint = null;
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setBackgroundPaint(Map<Object, Object> parameters) {
		Object bgpaint = parameters.get(CollapsibleGroupPanel.BGPAINT);
		if (bgpaint != null) {
			String bgp = bgpaint.toString();
			// Only defined colors in ColorConstants
			try {
				this.setBackgroundPaint(ColorConstants.paintNameToPaint(bgp));
			} catch (Exception exc) {
				CollapsibleGroup.logger.error(exc.getMessage(), exc);
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setBackgroundImage(Map<Object, Object> parameters) {
		Object obgimage = parameters.get(CollapsibleGroupPanel.BGIMAGE);
		if (obgimage != null) {
			String imgbg = obgimage.toString();
			Image bgimg = this.getImage(imgbg);
			if (bgimg != null) {
				this.setBackgroundImage(bgimg);
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setBackgroundColor(Map<Object, Object> parameters) {
		Object obgcolor = parameters.get(CollapsibleGroupPanel.BGCOLOR);
		if (obgcolor != null) {
			String bg = obgcolor.toString();
			Color bgcolor = this.getColor(bg, CollapsibleGroupPanel.BGCOLOR);
			if (bgcolor != null) {
				this.setBackground(bgcolor);
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setOpaque(Map<Object, Object> parameters) {
		Object oOpaque = parameters.get("opaque");
		if ((oOpaque != null) && !ApplicationManager.parseStringValue(oOpaque.toString())) {
			this.setOpaque(false);
		} else {
			this.setOpaque(true);
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setFont(Map<Object, Object> parameters) {
		Object oFont = parameters.get("font");
		if (oFont != null) {
			Font f = ParseUtils.getFont(oFont.toString(), this.getFont());
			((CollapsibleGroupHeader) this.header).setFont(f);
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setAttributeAndTitle(Map<Object, Object> parameters) {
		Object attr = parameters.get(DataField.ATTR);
		if (attr == null) {
			if (ApplicationManager.DEBUG) {
				CollapsibleGroup.logger
				.debug(this.getClass().toString() + DataField.ATTR + " parameter hasn't been found");
			}
		} else {
			this.attribute = attr;
			this.title = attr.toString();
		}

		if (attr == null) {
			Object tit = parameters.get("title");
			if (tit != null) {
				this.title = tit.toString();
			}
		}
	}

	/**
	 * This method returns a Color from the specified String with the color.
	 * @param strColor String with the value of the color.
	 * @param parameterName Parameter name for which is the specified color. In case of bad parsing, an
	 *        error is printed with the parameter name.
	 * @return a <code>Color</code>.
	 */
	protected Color getColor(String strColor, String parameterName) {
		if (strColor.indexOf(";") > 0) {
			try {
				return ColorConstants.colorRGBToColor(strColor);
			} catch (Exception e) {
				CollapsibleGroup.logger.error(
						this.getClass().toString() + ": Error in parameter '" + parameterName + "': " + e.getMessage(),
						e);
			}
		} else {
			try {
				return ColorConstants.parseColor(strColor);
			} catch (Exception e) {
				CollapsibleGroup.logger.error(
						this.getClass().toString() + ": Error in parameter '" + parameterName + "': " + e.getMessage(),
						e);
			}
		}
		return null;
	}

	/**
	 * This method returns an Image from the specified path of the image.
	 * @param imageURL The path of the image.
	 * @return an <code>Image</code>.
	 */
	protected Image getImage(String imageURL) {
		try {
			URL url = this.getClass().getClassLoader().getResource(imageURL);
			if (url == null) {
				CollapsibleGroup.logger.debug(this.getClass().toString() + ": Image not found -> " + imageURL);
			} else {
				java.awt.Image im = Toolkit.getDefaultToolkit().getImage(url);
				MediaTracker mt = new MediaTracker(this);
				mt.addImage(im, 0);
				mt.waitForID(0);
				return im;
			}
		} catch (Exception e) {
			CollapsibleGroup.logger.error(e.getMessage(), e);
		}
		return null;
	}

	@Override
	protected void paintComponent(Graphics g) {

		if (this.texturePaint != null) {
			Graphics2D g2 = (Graphics2D) g;
			Rectangle2D r = new Rectangle2D.Float(0, 0, this.getSize().width, this.getSize().height);
			// Now fill the round rectangle.
			g2.setPaint(this.texturePaint);
			g2.fill(r);

		} else if (this.backgroundPaint != null) {
			((Graphics2D) g).setPaint(this.backgroundPaint);
			Rectangle r = new Rectangle();
			r.width = this.getWidth();
			r.height = this.getHeight();
			((Graphics2D) g).fill(r);
		} else if (this.backgroundImage != null) {
			g.drawImage(this.backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
		} else {
			super.paintComponent(g);
		}
	}

	/**
	 * Sets the background paint parameter and repaints the Collapsible Group.
	 * <p>
	 * @param p The background paint configuration
	 */
	public void setBackgroundPaint(Paint p) {
		this.backgroundPaint = p;
		this.repaint();
	}

	/**
	 * Sets a background image in Collapsible Group.
	 * <p>
	 *
	 * @see Image
	 * @param im The awt image to set in background image
	 */
	public void setBackgroundImage(java.awt.Image im) {
		this.backgroundImage = im;
		this.repaint();
	}

	@Override
	public JCollapsibleGroupHeader createCollapsibleGroupHeader(String title) {
		CollapsibleGroupHeader header = new CollapsibleGroupHeader(title);
		header.setClose(true);
		return header;
	}

	@Override
	public List<String> getTextsToTranslate() {
		List<String> v = new ArrayList<>();
		return v;
	}

	@Override
	public void setResourceBundle(ResourceBundle resources) {
		if (this.header instanceof CollapsibleGroupHeader) {
			((CollapsibleGroupHeader) this.header).setResourceBundle(resources);
		}
	}

	@Override
	public void setComponentLocale(Locale l) {
		this.setLocale(l);
	}

	@Override
	public void add(Component c, Object constraints, int index) {
		this.add(c, constraints, index);
	}

	@Override
	public void add(Component c, Object constraints) {
		super.add(c, constraints);
	}

	@Override
	public Component add(Component c) {
		return super.add(c);
	}

	@Override
	public Component add(Component c, int index) {
		return this.add(c, index);
	}

	@Override
	public Object getConstraints(LayoutManager parentLayout) {
		if (parentLayout instanceof GridBagLayout) {
			return new GridBagConstraints(GridBagConstraints.RELATIVE, 0, 1, 1, 1, 1, GridBagConstraints.CENTER,
					GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
		} else {
			return null;
		}
	}

	@Override
	public Object getAttribute() {
		return this.attribute;
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
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
	}

	@Override
	public synchronized void addMouseListener(MouseListener mouseListener) {
		this.header.addMouseListener(mouseListener);
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
	}

	/**
	 * Checks the visible permissions.
	 * <p>
	 * @return the visibility condition
	 */
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
				// Check to show
				if (this.visiblePermission != null) {
					manager.checkPermission(this.visiblePermission);
				}
				this.restricted = false;
				return true;
			} catch (Exception e) {
				this.restricted = true;
				if (e instanceof NullPointerException) {
					CollapsibleGroup.logger.error(null, e);
				}
				if (ApplicationManager.DEBUG_SECURITY) {
					CollapsibleGroup.logger.debug(this.getClass().toString() + ": " + e.getMessage());
				}
				return false;
			}
		} else {
			return true;
		}
	}

	@Override
	public boolean isRestricted() {
		return this.restricted;
	}

	public static class CollapsibleGroupHeader extends JCollapsibleGroupHeader implements Internationalization {

		public static int LEFT_TO_ICON_MARGIN = 5;

		public static int UP_TO_ICON_MARGIN = 5;

		public static int ICON_TO_TEXT_MARGIN = 5;

		// Foreground colors.
		public Color fgColor = null;

		public Color fgRollOverColor = null;

		public Color fgPressedColor = null;

		public Color fgOpenColor = null;

		public Color fgShadowColor = null;

		public Color fgShadowRollOverColor = null;

		public Color fgShadowPressedColor = null;

		public Color fgShadowOpenColor = null;

		public Color fontShadowColor = new Color(228, 228, 228);

		public Color fontColor = new Color(0, 0, 0);

		public Color bgColor = null;

		public Color bgRollOverColor = null;

		public Color bgOpenColor = null;

		public Image bgImage = null;

		public Image bgRollOverImage = null;

		public Image bgOpenImage = null;

		public Color separatorLineColor = new Color(240, 240, 240);

		protected ImageIcon rolloverIcon = ImageManager
				.getIcon("com/ontimize/designer/gui/images/component/folder_close.png");

		protected ResourceBundle bundle;

		protected Locale locale;

		protected Font font = null;

		protected Icon icon = null;

		/**
		 * Variable that indicates if the mouse is over the Collpasible Group Header.
		 */
		protected boolean rollOver = false;

		/**
		 * Variable that indicates if the mouse is pressed or not into the Collapsible Group Header.
		 */
		protected boolean clicked = false;

		/**
		 * Variable that indicates if the CollpasibleGroup is collapsed or not.
		 */
		protected boolean collapsed = true;

		public CollapsibleGroupHeader(String name) {
			super(name);
			this.setBorder(null);
			this.installMouseHandler();
		}

		public void setCollapsed(boolean collapsed) {
			this.collapsed = collapsed;
		}

		public void setRollOverIcon(ImageIcon rollover) {
			this.rolloverIcon = rollover;
		}

		public ImageIcon getRollOverIcon() {
			return this.rolloverIcon;
		}

		protected void installMouseHandler() {

			this.addMouseListener(new MouseAdapter() {

				@Override
				public void mousePressed(MouseEvent e) {
					CollapsibleGroupHeader.this.clicked = true;
					CollapsibleGroupHeader.this.repaint();
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					CollapsibleGroupHeader.this.clicked = false;
					CollapsibleGroupHeader.this.repaint();
				}

				@Override
				public void mouseExited(MouseEvent e) {
					CollapsibleGroupHeader.this.rollOver = false;
					CollapsibleGroupHeader.this.repaint();
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					CollapsibleGroupHeader.this.rollOver = true;
					CollapsibleGroupHeader.this.repaint();
				}

			});
		}

		@Override
		protected void paintComponent(Graphics g) {
			this.paintOptions(g);
		}

		/**
		 * This method establishes the paint options to paint the header of the CollapsibleGroup
		 * @param g The Graphics of the CollapsibleGroup.
		 */
		protected void paintOptions(Graphics g) {
			if (this.font != null) {
				g.setFont(this.font);
			} else {
				g.setFont(new Font("Verdana", Font.BOLD, 14));
			}

			if ((this.getParent() != null) && (this != null)) {
				// Selection of the icon depending on the state of the
				// collapsible
				// group.
				if (this.collapsed) {
					if (this.rollOver) {
						this.icon = this.getRollOverIcon();
					} else {
						this.icon = this.getCloseIcon();
					}
				} else {
					this.icon = this.getOpenIcon();
				}

				Rectangle iconBound = new Rectangle(CollapsibleGroupHeader.LEFT_TO_ICON_MARGIN,
						(this.getHeight() / 2) - this.icon.getIconHeight(), this.icon.getIconWidth(),
						this.icon.getIconHeight());

				String title = ApplicationManager.getTranslation(this.getTitle(), this.bundle);
				int markWidth = g.getFontMetrics().stringWidth(title);
				markWidth += CollapsibleGroupHeader.LEFT_TO_ICON_MARGIN + this.icon.getIconWidth()
				+ CollapsibleGroupHeader.ICON_TO_TEXT_MARGIN + 8;
				Rectangle headerBound = new Rectangle(0, 0, markWidth, this.getHeight());

				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
						RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

				// Paint the background.It is checked the parameters bgcolor and
				// bgimage and the state of the collapsible group. If the
				// parameters
				// are not specified
				// it is taken default value.
				if (this.collapsed) {
					if (this.rollOver) {
						// Paint rollOver state.
						if (this.bgRollOverColor != null) {
							g.setColor(this.bgRollOverColor);
							g.fillRect(headerBound.x, headerBound.y, this.getWidth(), this.getHeight());
						} else if (this.bgRollOverImage != null) {
							g.drawImage(this.bgRollOverImage, 0, 0, this.getWidth(), this.getHeight(), this);
						} else {
							// Default rollOver color.
							g.setColor(new Color(219, 219, 219));
							g.fillRect(headerBound.x, headerBound.y, this.getWidth(), this.getHeight());
						}
					} else {
						// Paint close state.
						if (this.bgColor != null) {
							g.setColor(this.bgColor);
							g.fillRect(headerBound.x, headerBound.y, this.getWidth(), this.getHeight());
						} else if (this.bgImage != null) {
							g.drawImage(this.bgImage, 0, 0, this.getWidth(), this.getHeight(), this);
						} else {
							// Default background color.
							g.setColor(new Color(219, 219, 219));
							g.fillRect(headerBound.x, headerBound.y, this.getWidth(), this.getHeight());
						}
					}
				} else {
					// Paint open state.
					if (this.bgOpenColor != null) {
						g.setColor(this.bgOpenColor);
						g.fillRect(headerBound.x, headerBound.y, this.getWidth(), this.getHeight());
					} else if (this.bgOpenImage != null) {
						g.drawImage(this.bgOpenImage, 0, 0, this.getWidth(), this.getHeight(), this);
					} else {
						// Default open color.
						g.setColor(new Color(219, 219, 219));
						g.fillRect(headerBound.x, headerBound.y, this.getWidth(), this.getHeight());
					}
				}

				this.icon.paintIcon(this, g, iconBound.x, iconBound.y + (this.icon.getIconHeight() / 2));
				this.drawTitle(g, headerBound.x, headerBound.y, markWidth, this.getHeight());
				if (this.separatorLineColor != null) {
					g.setColor(this.separatorLineColor);
					g.drawLine(0, this.getHeight() - 1, this.getWidth(), this.getHeight() - 1);
				}
			}
		}

		/**
		 * This method paints the text of the Collapsible Group Header.
		 * @param g Graphics
		 * @param x X position where starts the text.
		 * @param y Y position where starts the text.
		 * @param width Width of the text.
		 * @param height Height of the text.
		 */
		protected void drawTitle(Graphics g, int x, int y, int width, int height) {
			int textLocx = CollapsibleGroupHeader.LEFT_TO_ICON_MARGIN + this.icon.getIconWidth()
			+ CollapsibleGroupHeader.ICON_TO_TEXT_MARGIN;
			int textLocy = ((height - g.getFontMetrics().getHeight()) / 2) + g.getFontMetrics().getAscent();
			// Selection of the foreground shadow color depending on the state
			// of
			// the collapsible group.
			g.setColor(Color.white); // Default value for the font shadow.
			if (((CollapsibleGroup) this.getParent()).isCollapsed()) {
				if (this.rollOver && !this.clicked) {
					if (this.fgShadowRollOverColor != null) {
						g.setColor(this.fgShadowRollOverColor);
					}
				} else if (this.rollOver && this.clicked) {
					if (this.fgShadowPressedColor != null) {
						g.setColor(this.fgShadowPressedColor);
					}
				} else if (this.fgShadowColor != null) {
					g.setColor(this.fgShadowColor);
				}
			} else {
				// Open foreground shadow color
				if (this.fgShadowOpenColor != null) {
					g.setColor(this.fgShadowOpenColor);
				}
			}
			String title = ApplicationManager.getTranslation(this.getTitle(), this.bundle);
			g.drawString(title, textLocx + 1, textLocy + 1);

			// Selection of the foreground color depending on the state of the
			// collapsible group.
			g.setColor(Color.black); // Default value for the font.
			if (((CollapsibleGroup) this.getParent()).isCollapsed()) {
				if (this.rollOver && !this.clicked) {
					if (this.fgRollOverColor != null) {
						g.setColor(this.fgRollOverColor);
					}
				} else if (this.rollOver && this.clicked) {
					if (this.fgPressedColor != null) {
						g.setColor(this.fgPressedColor);
					}
				} else if (this.fgColor != null) {
					g.setColor(this.fgColor);
				}
			} else {
				// Open foreground shadow color
				if (this.fgOpenColor != null) {
					g.setColor(this.fgOpenColor);
				}
			}
			g.drawString(title, textLocx, textLocy);
		}

		@Override
		public Font getFont() {
			return this.font;
		}

		@Override
		public void setFont(Font font) {
			this.font = font;
		}

		public Color getForegroundColor() {
			return this.fgColor;
		}

		public void setForegroundColor(Color fgColor) {
			this.fgColor = fgColor;
		}

		public Color getForegroundRollOverColor() {
			return this.fgRollOverColor;
		}

		public void setForegroundRollOverColor(Color fgRollOverColor) {
			this.fgRollOverColor = fgRollOverColor;
		}

		public Color getForegroundPressedColor() {
			return this.fgPressedColor;
		}

		public void setForegroundPressedColor(Color fgPressedColor) {
			this.fgPressedColor = fgPressedColor;
		}

		public Color getForegroundOpenColor() {
			return this.fgOpenColor;
		}

		public void setForegroundOpenColor(Color fgOpenColor) {
			this.fgOpenColor = fgOpenColor;
		}

		public Color getForegroundShadowColor() {
			return this.fgShadowColor;
		}

		public void setForegroundShadowColor(Color fgShadowColor) {
			this.fgShadowColor = fgShadowColor;
		}

		public Color getForegroundShadowRollOverColor() {
			return this.fgShadowRollOverColor;
		}

		public void setForegroundShadowRollOverColor(Color fgShadowRollOverColor) {
			this.fgShadowRollOverColor = fgShadowRollOverColor;
		}

		public Color getForegroundShadowPressedColor() {
			return this.fgShadowPressedColor;
		}

		public void setForegroundShadowPressedColor(Color fgShadowPressedColor) {
			this.fgShadowPressedColor = fgShadowPressedColor;
		}

		public Color getForegroundShadowOpenColor() {
			return this.fgShadowOpenColor;
		}

		public void setForegroundShadowOpenColor(Color fgShadowOpenColor) {
			this.fgShadowOpenColor = fgShadowOpenColor;
		}

		public Color getBackgroundColor() {
			return this.bgColor;
		}

		public void setBackgroundColor(Color backgroundColor) {
			this.bgColor = backgroundColor;
		}

		public Color getBackgroundRollOverColor() {
			return this.bgRollOverColor;
		}

		public void setBackgroundRollOverColor(Color bgRollOverColor) {
			this.bgRollOverColor = bgRollOverColor;
		}

		public Color getBackgroundOpenColor() {
			return this.bgOpenColor;
		}

		public void setBackgroundOpenColor(Color bgOpenColor) {
			this.bgOpenColor = bgOpenColor;
		}

		public void setBackgrounRollOverColor(Color bgRollOverColor) {
			this.bgRollOverColor = bgRollOverColor;
		}

		public Image getBackgroundImage() {
			return this.bgImage;
		}

		public void setBackgroundImage(Image bgImage) {
			this.bgImage = bgImage;
		}

		public Image getBackgroundRollOverImage() {
			return this.bgRollOverImage;
		}

		public void setBackgroundRollOverImage(Image bgRollOverImage) {
			this.bgRollOverImage = bgRollOverImage;
		}

		public Image getBackgroundOpenImage() {
			return this.bgOpenImage;
		}

		public void setBackgroundOpenImage(Image bgOpenImage) {
			this.bgOpenImage = bgOpenImage;
		}

		public Color getSeparatorLineColor() {
			return this.separatorLineColor;
		}

		public void setSeparatorLineColor(Color separatorLineColor) {
			this.separatorLineColor = separatorLineColor;
		}

		@Override
		public List<String> getTextsToTranslate() {
			return null;
		}

		@Override
		public void setComponentLocale(Locale l) {
			this.locale = l;
		}

		@Override
		public void setResourceBundle(ResourceBundle resourceBundle) {
			this.bundle = resourceBundle;
		}

	}

	@Override
	public void free() {
		// TODO Auto-generated method stub

	}

}
