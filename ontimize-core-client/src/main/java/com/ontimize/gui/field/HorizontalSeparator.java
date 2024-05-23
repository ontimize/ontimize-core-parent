package com.ontimize.gui.field;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ColorConstants;
import com.ontimize.gui.Freeable;

/**
 * The main classes to put a horizontal separator component into a container.
 * <p>
 *
 * @author Imatia Innovation
 */
public class HorizontalSeparator extends JPanel implements FormComponent, Freeable {

	private static final Logger logger = LoggerFactory.getLogger(HorizontalSeparator.class);

	EtchedBorder border = new EtchedBorder(EtchedBorder.LOWERED);

	public HorizontalSeparator(Map<Object, Object> parameters) {
		this.setBorder(this.border);
		this.setPreferredSize(new Dimension(1, 2));
	}

	/**
	 * Inits parameters. <p>
	 *
	 * @param parameters
	 *            the <code>Map</code> with parameters <p> <Table BORDER=1 CELLPADDING=3 CELLSPACING=1 RULES=ROWS FRAME=BOX> <tr> <td><b>attribute</td> <td><b>values</td>
	 *            <td><b>default</td> <td><b>required</td> <td><b>meaning</td> </tr> <tr> <td>color</td> <td></td> <td></td> <td>no</td> <td>The color for separator.</td> </tr>
	 *            </table>
	 */
	@Override
	public void init(Map<Object, Object> parameters) {
		Object color = parameters.get("color");
		if (color != null) {
			try {
				this.setForeground(ColorConstants.parseColor(color.toString()));
			} catch (Exception exc) {
				HorizontalSeparator.logger
				.error(this.getClass().toString() + " Error in parameter 'color':" + exc.getMessage(), exc);
			}
		}
	}

	@Override
	public Object getConstraints(LayoutManager parentLayout) {
		if (parentLayout instanceof GridBagLayout) {
			return new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
					new Insets(0, 10, 0, 10), 0, 0);
		} else {
			return null;
		}
	}

	@Override
	public List<String> getTextsToTranslate() {
		return new ArrayList<>();
	}

	@Override
	public void setResourceBundle(ResourceBundle resource) {

	}

	@Override
	public void setComponentLocale(Locale l) {
	}

	@Override
	public void free() {
		// TODO Auto-generated method stub

	}

}
