package com.ontimize.printing;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.MessageDialog;
import com.ontimize.gui.images.ImageManager;

public class TemplateElement implements PrintingElement {

    private static final Logger logger = LoggerFactory.getLogger(TemplateElement.class);

    public static boolean DEBUG = false;

    double zoom = 1.0;

    String id = null;

    Vector elements = new Vector();

    PrinterJob pj = PrinterJob.getPrinterJob();

    PageFormat pf = this.pj.defaultPage();

    JPanel centralPanel = new JPanel(new GridBagLayout());

    class ListenerMouse extends MouseAdapter implements MouseMotionListener {

        protected PrintingElement el = null;

        protected PagePanel p = null;

        public ListenerMouse(PagePanel p) {
            this.p = p;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            this.el = this.p.getElementAt(e.getX(), e.getY());
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (this.el == null) {
                return;
            } else {

                double dLeftMargin = TemplateElement.this.pf.getImageableX();

                double dTopMargin = TemplateElement.this.pf.getImageableY();

                int pixelsFromXMargin = (int) (e.getX() - dLeftMargin);
                int pixelsFromYMargin = (int) (e.getY() - dTopMargin);

                int x = AbstractPrintingElement.pagePixelsToMillimeters(pixelsFromXMargin);
                int y = AbstractPrintingElement.pagePixelsToMillimeters(pixelsFromYMargin);

                TemplateElement.logger.debug("Setting x = " + x + " , y = " + y + " for " + this.el);

                this.el.setX(x);
                this.el.setY(y);
                this.p.paintImmediately(0, 0, this.p.getWidth(), this.p.getHeight());
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            this.el = null;
        }

        @Override
        public void mouseMoved(MouseEvent e) {

        }

    }

    class PagePanel extends JPanel implements Printable {

        protected Stroke selStroke = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f,
                new float[] { 5f, 5.0f }, 0.0f);

        public PagePanel() {
            ListenerMouse l = new ListenerMouse(this);
            if (TemplateElement.DEBUG) {
                this.addMouseListener(l);
            }
            if (TemplateElement.DEBUG) {
                this.addMouseMotionListener(l);
            }
            this.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0));
        }

        protected PrintingElement getElementAt(int x, int y) {
            // Search the element in the specified point
            double dLeftMargin = TemplateElement.this.pf.getImageableX();

            double dTopMargin = TemplateElement.this.pf.getImageableY();

            int pixelsFromXMargin = (int) (x - dLeftMargin);
            int pixelsFromYMargin = (int) (y - dTopMargin);

            int mmFromXMargin = AbstractPrintingElement.pagePixelsToMillimeters(pixelsFromXMargin);
            int mmFromYMargin = AbstractPrintingElement.pagePixelsToMillimeters(pixelsFromYMargin);

            for (int i = 0; i < TemplateElement.this.elements.size(); i++) {
                // The elements are painted using the margin
                PrintingElement e = (PrintingElement) TemplateElement.this.elements.get(i);
                if ((e instanceof TemplateElement) || (e == null)) {
                    continue;
                }

                Rectangle r = new Rectangle(e.getX(), e.getY(), e.getWidth(), e.getHeight());
                if (r.contains(mmFromXMargin, mmFromYMargin)) {
                    return e;
                }
            }
            return null;
        }

        @Override
        public int print(Graphics g, PageFormat pf, int pageIndex) {
            if ((TemplateElement.this.pageData != null) && (TemplateElement.this.pageData.size() > 0)) {
                if (pageIndex >= TemplateElement.this.pageData.size()) {
                    return Printable.NO_SUCH_PAGE;
                } else {
                    TemplateElement.this.setContent(TemplateElement.this.pageData.get(pageIndex));

                    this.paintComponent(g, 1.0D, false);
                    if (TemplateElement.DEBUG) {
                        TemplateElement.logger.debug("Page printed " + pageIndex);
                    }
                    try {
                        Thread.sleep(10);
                    } catch (Exception e) {
                        TemplateElement.logger.trace(null, e);
                    }
                    return Printable.PAGE_EXISTS;
                }
            } else {
                if (pageIndex > 0) {
                    return Printable.NO_SUCH_PAGE;
                } else {
                    this.paintComponent(g, 1.0D, false);
                    return Printable.PAGE_EXISTS;
                }
            }
        }

        @Override
        public void paintComponent(Graphics g) {
            this.paintComponent(g, TemplateElement.this.zoom);
        }

        protected void paintMargins(Graphics g, double zoom) {
            Color c = g.getColor();
            g.setColor(Color.lightGray);

            Stroke s = ((Graphics2D) g).getStroke();
            ((Graphics2D) g).setStroke(this.selStroke);
            double dLeftMargin = TemplateElement.this.pf.getImageableX() * zoom;
            double dRightMargin = (TemplateElement.this.pf.getImageableWidth() * zoom)
                    + (TemplateElement.this.pf.getImageableX() * zoom);
            double dTopMargin = TemplateElement.this.pf.getImageableY() * zoom;
            double dBottomMargin = (TemplateElement.this.pf.getImageableHeight() * zoom)
                    + (TemplateElement.this.pf.getImageableY() * zoom);

            g.drawLine(0, (int) dTopMargin, (int) (TemplateElement.this.pf.getWidth() * zoom), (int) dTopMargin);
            g.drawLine(0, (int) dBottomMargin, (int) (TemplateElement.this.pf.getWidth() * zoom), (int) dBottomMargin);

            g.drawLine((int) dLeftMargin, 0, (int) dLeftMargin, (int) (TemplateElement.this.pf.getHeight() * zoom));
            g.drawLine((int) dRightMargin, 0, (int) dRightMargin, (int) (TemplateElement.this.pf.getHeight() * zoom));

            ((Graphics2D) g).setStroke(s);
            g.setColor(c);
        }

        protected synchronized void paintComponent(Graphics g, double zoom, boolean paintMargins) {
            super.paintComponent(g);
            if (paintMargins) {
                this.paintMargins(g, zoom);
            }
            ((Graphics2D) g).translate(zoom * TemplateElement.this.pf.getImageableX(),
                    zoom * TemplateElement.this.pf.getImageableY());

            TemplateElement.this.paint(g, zoom * TemplateElement.this.fitToPageZoom);
            for (int i = 0; i < TemplateElement.this.elements.size(); i++) {
                PrintingElement e = (PrintingElement) TemplateElement.this.elements.get(i);
                if (e != null) {
                    e.paint(g, zoom * TemplateElement.this.fitToPageZoom);
                }
            }
            ((Graphics2D) g).translate(-(zoom * TemplateElement.this.pf.getImageableX()),
                    -(zoom * TemplateElement.this.pf.getImageableY()));

        }

        protected void paintComponent(Graphics g, double zoom) {
            this.paintComponent(g, zoom, true);
        }

    };

    PagePanel view = new PagePanel();

    JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

    JComboBox comboZoom = null;

    JButton printingButton = new JButton();

    JButton pageSetupButton = new JButton();

    JButton closeButton = new JButton();

    ImageIcon backgroundImage = null;

    boolean fitImageToPage = false;

    JToggleButton fitToPage = new JToggleButton();

    double fitToPageZoom = 1.0;

    JDialog dialog = null;

    String page = "A4";

    JPanel componentPanel = null;

    Vector pageData = null;

    public TemplateElement(Hashtable parameters) {

        Object ident = parameters.get("id");
        if (ident != null) {
            this.id = ident.toString();
        } else {
            TemplateElement.logger.debug(this.getClass().toString() + " : Parameter 'id' not found");
        }

        Object page = parameters.get("page");
        if (page != null) {
            this.page = page.toString();
            Paper p = new Paper();
            int width = 0;
            int height = 0;
            if (this.page.equals("A2")) {
                width = 420;
                height = 594;
                p.setImageableArea(72, 72, (width * 72) / 2.54, (height * 72) / 2.54);
            } else if (this.page.equals("A3")) {
                width = 297;
                height = 420;
                p.setImageableArea(72, 72, (width * 72) / 2.54, (height * 72) / 2.54);
            } else if (this.page.equals("A4")) {
                width = 210;
                height = 297;
                p.setImageableArea(72, 72, (width * 72) / 2.54, (height * 72) / 2.54);
            } else if (this.page.equals("A5")) {
                width = 148;
                height = 210;
                p.setImageableArea(72, 72, (width * 72) / 2.54, (height * 72) / 2.54);
            } else if (this.page.equals("A6")) {
                width = 105;
                height = 148;
                p.setImageableArea(72, 72, (width * 72) / 2.54, (height * 72) / 2.54);
            } else if (this.page.equals("A7")) {
                width = 74;
                height = 105;
                p.setImageableArea(72, 72, (width * 72) / 2.54, (height * 72) / 2.54);
            } else if (this.page.equals("LETTER")) {
            }
            this.pf.setPaper(p);

        } else {
        }

        Object landscape = parameters.get("landscape");
        if (landscape != null) {
            if (landscape.equals("yes")) {
                this.pf.setOrientation(PageFormat.LANDSCAPE);
            } else {
                this.pf.setOrientation(PageFormat.PORTRAIT);
            }
        }

        int iLeftMargin = -1;
        Object oLeftmargin = parameters.get("leftmargin");
        if (oLeftmargin != null) {
            try {
                iLeftMargin = Integer.parseInt(oLeftmargin.toString());
            } catch (Exception e) {
                TemplateElement.logger.error("Error in parameter 'leftmargin' ", e);
            }
        }
        int iRightMargin = -1;
        Object rightmargin = parameters.get("rightmargin");
        if (rightmargin != null) {
            try {
                iRightMargin = Integer.parseInt(rightmargin.toString());
            } catch (Exception e) {
                TemplateElement.logger.error("Error in parameter 'rightmargin' ", e);
            }
        }
        int iTopMargin = -1;
        Object topmargin = parameters.get("topmargin");
        if (topmargin != null) {
            try {
                iTopMargin = Integer.parseInt(topmargin.toString());
            } catch (Exception e) {
                TemplateElement.logger.error("Error in parameter 'topmargin' ", e);
            }
        }
        int iBottomMargin = -1;
        Object bottommargin = parameters.get("bottommargin");
        if (bottommargin != null) {
            try {
                iBottomMargin = Integer.parseInt(bottommargin.toString());
            } catch (Exception e) {
                TemplateElement.logger.error("Error in parameter 'bottommargin' ", e);
            }
        }

        if ((iBottomMargin != -1) || (iTopMargin != -1) || (iLeftMargin != -1) || (iRightMargin != -1)) {
            Paper p = this.pf.getPaper();

            double dWidth = p.getWidth();
            double dHeight = p.getHeight();
            double mi = p.getImageableX();
            double ms = p.getImageableY();
            double minf = dHeight - (p.getImageableY() + p.getImageableHeight());
            double mder = dWidth - (p.getImageableX() + p.getImageableWidth());
            if (iBottomMargin != -1) {
                minf = AbstractPrintingElement.millimeterToPagePixels(iBottomMargin);
            }

            if (iTopMargin != -1) {
                ms = AbstractPrintingElement.millimeterToPagePixels(iTopMargin);
            }

            if (iLeftMargin != -1) {
                mi = AbstractPrintingElement.millimeterToPagePixels(iLeftMargin);
            }

            if (iRightMargin != -1) {
                mder = AbstractPrintingElement.millimeterToPagePixels(iRightMargin);
            }
            if (ApplicationManager.DEBUG) {
                TemplateElement.logger.debug("Template margins: " + mi + " , " + ms + " , " + (dWidth - mder - mi)
                        + " , " + (dHeight - minf - ms));
            }

            p.setImageableArea(mi, ms, dWidth - mder - mi, dHeight - minf - ms);

            this.pf.setPaper(p);
        }

        Object bgImage = parameters.get("bgimage");
        if (bgImage != null) {
            URL url = this.getClass().getClassLoader().getResource(bgImage.toString());
            if (url != null) {
                this.backgroundImage = new ImageIcon(url);
            } else {
                TemplateElement.logger.debug(this.getClass().toString() + " : Image not found " + bgImage);
            }
        }

        Object stretch = parameters.get("stretch");
        if ((stretch != null) && stretch.equals("yes")) {
            this.fitImageToPage = true;
        }

        this.componentPanel = this.initComponent();

    }

    protected JPanel initComponent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(this.controlPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(this.centralPanel));
        this.centralPanel.add(this.view, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.centralPanel.setOpaque(false);
        this.view.setBackground(Color.white);
        Vector zooms = new Vector();
        zooms.add("25%");
        zooms.add("50%");
        zooms.add("75%");
        zooms.add("100%");
        zooms.add("200%");
        this.comboZoom = new JComboBox(zooms);
        this.comboZoom.setSelectedIndex(3);
        this.controlPanel.add(this.comboZoom);
        this.printingButton.setIcon(ImageManager.getIcon(ImageManager.PRINT_HELP_UI));
        this.controlPanel.add(this.printingButton);
        this.pageSetupButton.setIcon(ImageManager.getIcon(ImageManager.PAGE));
        this.controlPanel.add(this.pageSetupButton);
        this.closeButton.setIcon(ImageManager.getIcon(ImageManager.EXIT));
        this.controlPanel.add(this.closeButton);
        this.fitToPage.setIcon(ImageManager.getIcon(ImageManager.PAGE_SETUP_HELP_UI));
        this.controlPanel.add(this.fitToPage);
        this.closeButton.setMargin(new Insets(1, 1, 1, 1));
        this.pageSetupButton.setMargin(new Insets(1, 1, 1, 1));
        this.printingButton.setMargin(new Insets(1, 1, 1, 1));
        this.fitToPage.setMargin(new Insets(1, 1, 1, 1));
        this.closeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Window w = SwingUtilities.getWindowAncestor(TemplateElement.this.closeButton);
                w.dispose();
            }
        });
        this.pageSetupButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                TemplateElement.this.setupPage();
            }
        });

        this.printingButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                TemplateElement.this.print();
            }
        });

        this.fitToPage.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                TemplateElement.this.setFitToPage(TemplateElement.this.fitToPage.isSelected());
            }
        });

        this.comboZoom.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (e.getItem().equals("25%")) {
                        TemplateElement.this.changeZoom(0.25);
                    } else if (e.getItem().equals("50%")) {
                        TemplateElement.this.changeZoom(0.5);
                    } else if (e.getItem().equals("75%")) {
                        TemplateElement.this.changeZoom(0.75);
                    } else if (e.getItem().equals("100%")) {
                        TemplateElement.this.changeZoom(1.0);
                    } else if (e.getItem().equals("200%")) {
                        TemplateElement.this.changeZoom(2.0);
                    } else if (e.getItem().equals("500%")) {
                        TemplateElement.this.changeZoom(5.0);
                    }
                }
            }
        });
        return panel;
    }

    public void addPrintingElement(PrintingElement e) {
        this.elements.add(this.elements.size(), e);
    }

    public void preview() {
        this.preview((Frame) null);
    }

    public void preview(Frame f) {
        this.preview(f, 600, 400);
    }

    public void preview(Frame f, int width, int height) {
        double dLeftMargin = this.pf.getImageableX();
        double dRigthMargin = this.pf.getWidth() - this.pf.getImageableWidth() - this.pf.getImageableX();
        double dTopMargin = this.pf.getImageableY();
        double dBottomMargin = this.pf.getHeight() - this.pf.getImageableHeight() - this.pf.getImageableY();
        this.view.setPreferredSize(
                new Dimension((int) (dLeftMargin + dRigthMargin + this.pf.getImageableWidth()),
                        (int) (dTopMargin + dBottomMargin + this.pf.getImageableHeight())));
        this.dialog = new JDialog(f, ApplicationManager.getTranslation("preview"), true);
        this.dialog.setSize(width, height);
        this.dialog.setContentPane(this.componentPanel);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        this.dialog.setLocation((d.width / 2) - (width / 2), (d.height / 2) - (height / 2));
        this.view.repaint();
        this.dialog.setVisible(true);
    }

    public void preview(Dialog f) {
        this.preview(f, 600, 400);
    }

    public void preview(Dialog f, int width, int height) {
        double dLeftMargin = this.pf.getImageableX();
        double dRigthMargin = this.pf.getWidth() - this.pf.getImageableWidth() - this.pf.getImageableX();
        double dTopMargin = this.pf.getImageableY();
        double dBottomMargin = this.pf.getHeight() - this.pf.getImageableHeight() - this.pf.getImageableY();
        this.view.setPreferredSize(
                new Dimension((int) (dLeftMargin + dRigthMargin + this.pf.getImageableWidth()),
                        (int) (dTopMargin + dBottomMargin + this.pf.getImageableHeight())));
        this.dialog = new JDialog(f, ApplicationManager.getTranslation("preview"), true);
        this.dialog.setSize(width, height);
        this.dialog.setContentPane(this.componentPanel);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        this.dialog.setLocation((d.width / 2) - (width / 2), (d.height / 2) - (height / 2));
        this.view.repaint();
        this.dialog.setVisible(true);
    }

    public void print() {
        this.print(null);
    }

    public void print(String name) {
        this.pj.setPrintable(this.view, this.pf);
        if (name != null) {
            this.pj.setJobName(name);
        }
        if (this.pj.printDialog()) {
            try {
                this.pj.print();
            } catch (Exception e) {
                TemplateElement.logger.error(null, e);
                MessageDialog.showMessage(this.dialog, e.getMessage(), JOptionPane.ERROR_MESSAGE, null);
            }
        } else {
        }
    }

    public void print(PrinterJob pjExt, PageFormat pfExt) {
        pjExt.setPrintable(this.view, pfExt);
        try {
            pjExt.print();
        } catch (Exception e) {
            TemplateElement.logger.error(null, e);
            MessageDialog.showMessage(this.dialog, e.getMessage(), JOptionPane.ERROR_MESSAGE, null);
        }
    }

    public void setupPage() {
        this.pf = this.pj.pageDialog(this.pf);
        this.changeZoom(this.zoom);
    }

    public PrintingElement getPrintingElement(String id) {
        for (int i = 0; i < this.elements.size(); i++) {
            PrintingElement e = (PrintingElement) this.elements.get(i);
            if (e != null) {
                Object idE = e.getId();
                if ((idE != null) && idE.equals(id)) {
                    return e;
                }
            }
        }
        return null;
    }

    private void changeZoom(double z) {
        this.zoom = z;
        double dLeftMargin = this.pf.getImageableX();
        double dRightMargin = this.pf.getWidth() - this.pf.getImageableWidth() - this.pf.getImageableX();
        double dTopMargin = this.pf.getImageableY();
        double dBottomMargin = this.pf.getHeight() - this.pf.getImageableHeight() - this.pf.getImageableY();
        this.view.setPreferredSize(
                new Dimension((int) (this.zoom * (dLeftMargin + dRightMargin + this.pf.getImageableWidth())),
                        (int) (this.zoom * (dTopMargin + dBottomMargin + this.pf.getImageableHeight()))));
        this.view.setSize(new Dimension((int) (this.zoom * (dLeftMargin + dRightMargin + this.pf.getImageableWidth())),
                (int) (this.zoom * (dTopMargin + dBottomMargin + this.pf.getImageableHeight()))));
        this.centralPanel
            .setSize(new Dimension((int) (this.zoom * (dLeftMargin + dRightMargin + this.pf.getImageableWidth())),
                    (int) (this.zoom * (dTopMargin + dBottomMargin + this.pf.getImageableHeight()))));

        this.view.invalidate();

        this.view.repaint();

        if (this.dialog != null) {
            this.dialog.doLayout();
            this.dialog.validate();
            this.dialog.repaint();
        }
    }

    /**
     * If the parameter is a Hashtable object then set the values for all the template elements.<br>
     * If the parameter is a vector with hashtables then each hashtable will be a new page
     */
    @Override
    public void setContent(Object c) {
        if (c instanceof Hashtable) {
            Hashtable contents = (Hashtable) c;
            Enumeration enumKeys = contents.keys();
            while (enumKeys.hasMoreElements()) {
                Object oKey = enumKeys.nextElement();
                for (int i = 0; i < this.elements.size(); i++) {
                    PrintingElement element = (PrintingElement) this.elements.get(i);
                    if (element != null) {
                        Object idElemento = element.getId();
                        if ((idElemento != null) && idElemento.equals(oKey)) {
                            element.setContent(contents.get(oKey));
                        }
                    }
                }
            }
        } else if ((c instanceof Vector) && !((Vector) c).isEmpty()) {
            Vector aux = (Vector) c;
            this.pageData = new Vector();
            for (int i = 0; i < aux.size(); i++) {
                if (aux.get(i) instanceof Hashtable) {
                    this.pageData.add(aux.get(i));

                }
            }
            if (this.pageData.size() > 0) {
                this.setContent(this.pageData.get(0));
            }
        }

    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void paint(Graphics g, double scale) {
        if (this.backgroundImage != null) {
            g.drawImage(this.backgroundImage.getImage(), 0, 0, (int) (scale * this.backgroundImage.getIconWidth()) - 1,
                    (int) (scale * this.backgroundImage.getIconHeight()) - 1,
                    this.view);
        }
    }

    public void paintStretch(Graphics g, PageFormat pf) {
        if (this.backgroundImage != null) {
            g.drawImage(this.backgroundImage.getImage(), 0, 0, (int) pf.getImageableWidth() - 1,
                    (int) pf.getImageableHeight() - 1, this.view);
        }
    }

    @Override
    public void paintInPage(Graphics g, PageFormat f) {
        if (!this.fitImageToPage) {
            this.paint(g, AbstractPrintingElement.millimeterToPagePixels(1));
        } else {
            this.paintStretch(g, f);
        }
    }

    public void setFitToPage(boolean fit) {
        if (fit) {
            int maxWidth = 0;
            int maxHeight = 0;
            for (int i = 0; i < this.elements.size(); i++) {
                PrintingElement e = (PrintingElement) this.elements.get(i);
                if (e == null) {
                    continue;
                }
                int x = e.getX();
                int width = e.getWidth();
                int y = e.getY();
                int height = e.getHeight();
                maxWidth = Math.max(maxWidth, x + width);
                maxHeight = Math.max(maxHeight, y + height);
            }
            maxHeight = maxHeight + 10;
            maxWidth = maxWidth + 10;
            maxHeight = AbstractPrintingElement.millimeterToPagePixels(maxHeight);
            maxWidth = AbstractPrintingElement.millimeterToPagePixels(maxWidth);
            double scaleX = this.pf.getImageableWidth() / maxWidth;
            double scaleY = this.pf.getImageableHeight() / maxHeight;

            this.fitToPageZoom = Math.max(scaleX, scaleY);
            TemplateElement.logger.debug("Fitting the page with scale: " + this.fitToPageZoom);
        } else {
            this.fitToPageZoom = 1;
        }
        this.view.repaint();
    }

    @Override
    public int getX() {
        return 0;
    }

    @Override
    public int getY() {
        return 0;
    }

    @Override
    public int getWidth() {
        return AbstractPrintingElement.pagePixelsToMillimeters((int) this.pf.getImageableWidth());
    }

    @Override
    public int getHeight() {
        return AbstractPrintingElement.pagePixelsToMillimeters((int) this.pf.getImageableHeight());
    }

    @Override
    public void setX(int x) {

    }

    @Override
    public void setY(int y) {

    }

    @Override
    public void setWidth(int w) {

    }

    @Override
    public void setHeight(int h) {

    }

    public static void printGroup(final TemplateElement[] templates) throws PrinterException {
        Printable printable = new Printable() {

            @Override
            public int print(Graphics g, PageFormat pf, int pageIndex) {
                if (pageIndex > (templates.length - 1)) {
                    return Printable.NO_SUCH_PAGE;
                } else {
                    templates[pageIndex].view.paintComponent(g, 1, false);
                    return Printable.PAGE_EXISTS;
                }
            }
        };
        PrinterJob pj = PrinterJob.getPrinterJob();
        PageFormat pf = pj.pageDialog(pj.defaultPage());
        pj.setPrintable(printable, pf);
        boolean res = pj.printDialog();
        if (res) {
            pj.print();
        }
    }

}
