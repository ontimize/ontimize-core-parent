package com.ontimize.printing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.ColorConstants;

public abstract class AbstractPrintingElement implements PrintingElement {

	private static final Logger logger = LoggerFactory.getLogger(AbstractPrintingElement.class);

	public static boolean DEBUG = false;

	public static int PPI = 72;

	public static double MM_PER_INCH = 25.4;

	protected Color color = Color.black;

	protected Color bgcolor = null;

	protected int fontSize = 12;

	protected boolean bold = false;

	protected boolean italics = false;

	protected String id = null;

	protected Object contain = null;

	protected int aligment = PrintingElement.LEFT;

	protected static Font defaultFont = Font.decode("Arial-plain-12");

	public static Dimension millimeterToPagePixels(Dimension d) {
		double dWidth = d.getWidth();// millimeters
		double dHeight = d.getHeight();
		return new Dimension(AbstractPrintingElement.millimeterToPagePixels((int) dWidth),
				AbstractPrintingElement.millimeterToPagePixels((int) dHeight));
	}

	public static int millimeterToPagePixels(int d) {
		int pix = (int) ((d / AbstractPrintingElement.MM_PER_INCH) * AbstractPrintingElement.PPI);
		return pix;
	}

	public static int pagePixelsToMillimeters(int d) {
		int mm = (int) ((d * AbstractPrintingElement.MM_PER_INCH) / AbstractPrintingElement.PPI);
		return mm;
	}

	protected int getInteger(Object v) {
		try {
			if (v == null) {
				if (ApplicationManager.DEBUG) {
					AbstractPrintingElement.logger.debug(this.getClass().toString() + " : null value");
				}
			}
			return Integer.parseInt(v.toString());
		} catch (Exception e) {
			if (ApplicationManager.DEBUG) {
				AbstractPrintingElement.logger
				.debug(this.getClass().toString() + " : Parameter error. " + e.getMessage(), e);
			}
			return 0;
		}
	}

	protected void setBasicParameters(Map<Object, Object> parameters) {
		Object ident = parameters.get("id");
		if (ident != null) {
			this.id = ident.toString();
		} else {
			if (ApplicationManager.DEBUG) {
				AbstractPrintingElement.logger.debug(this.getClass().toString() + " : Parameter 'id' not found");
			}
		}
		Object fontsize = parameters.get("fontsize");
		if (fontsize != null) {
			try {
				this.fontSize = Integer.parseInt(fontsize.toString());
			} catch (Exception e) {
				if (ApplicationManager.DEBUG) {
					AbstractPrintingElement.logger
					.debug(this.getClass().toString() + " : Error in parameter 'fontsize'", e);
				}
			}
		}

		Object color = parameters.get("color");
		if (color != null) {
			if (color.equals("none")) {
				color = null;
			} else {
				try {
					this.color = ColorConstants.parseColor(color.toString());
				} catch (Exception e) {
					AbstractPrintingElement.logger
					.debug(this.getClass().toString() + " Error in parameter 'color':" + e.getMessage(), e);
				}
			}
		}

		Object bgcolor = parameters.get("bgcolor");
		if (bgcolor != null) {
			try {
				this.bgcolor = ColorConstants.parseColor(bgcolor.toString());
			} catch (Exception e) {
				AbstractPrintingElement.logger
				.debug(this.getClass().toString() + " Error in parameter 'bgcolor':" + e.getMessage(), e);
			}

		}

		Object bold = parameters.get("bold");
		if (bold != null) {
			if (bold.toString().equalsIgnoreCase("yes")) {
				this.bold = true;
			} else {
				this.bold = false;
			}
		}

		Object italic = parameters.get("italic");
		if (italic != null) {
			if (italic.toString().equalsIgnoreCase("yes")) {
				this.italics = true;
			} else {
				this.italics = false;
			}
		}

		Object align = parameters.get("align");
		if (align != null) {
			if (align.equals("center")) {
				this.aligment = PrintingElement.CENTER;
			} else if (align.equals("right")) {
				this.aligment = PrintingElement.RIGHT;
			} else {
				this.aligment = PrintingElement.LEFT;
			}
		}
	}

	@Override
	public void setContent(Object content) {
		this.contain = content;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public void paintInPage(Graphics g, PageFormat f) {
		this.paint(g, AbstractPrintingElement.millimeterToPagePixels(1));
	}

	@Override
	public String toString() {
		return this.getClass() + " " + this.getId();
	}

	@Override
	public abstract int getX();

	@Override
	public abstract int getY();

	@Override
	public abstract int getWidth();

	@Override
	public abstract int getHeight();

	@Override
	public abstract void setX(int x);

	@Override
	public abstract void setY(int y);

	@Override
	public abstract void setWidth(int w);

	@Override
	public abstract void setHeight(int h);

}
