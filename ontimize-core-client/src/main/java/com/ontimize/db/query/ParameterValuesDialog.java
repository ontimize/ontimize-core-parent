package com.ontimize.db.query;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.ontimize.db.SQLStatementBuilder.BasicExpression;
import com.ontimize.db.SQLStatementBuilder.BasicField;
import com.ontimize.db.SQLStatementBuilder.Expression;
import com.ontimize.db.SQLStatementBuilder.Field;
import com.ontimize.db.SQLStatementBuilder.Operator;
import com.ontimize.db.query.QueryBuilder.pvTable;
import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.container.EJDialog;
import com.ontimize.gui.images.ImageManager;

public class ParameterValuesDialog extends EJDialog {

    private ResourceBundle bundle = null;

    private JButton bAccept = null;

    private final String[] cols;

    private final int[] types;

    private pvTable t;

    boolean allOk = true;

    Expression expression = null;

    public boolean isParameterListEmpty() {
        if (this.t.getRowCount() == 0) {
            return true;
        }
        return false;
    }

    protected boolean getAllOk() {
        return this.allOk;
    }

    public ParameterValuesDialog(Frame o, Expression e, ResourceBundle bundle, String[] cols, int[] types) {
        super(o, ApplicationManager.getTranslation("QueryBuilderParameterValuesTable", bundle), true);
        this.bundle = bundle;
        this.cols = cols;
        this.types = types;
        this.init();
    }

    public ParameterValuesDialog(Dialog o, Expression e, ResourceBundle bundle, String[] cols, int[] types) {
        super(o, ApplicationManager.getTranslation("QueryBuilderParameterValuesTable", bundle), true);
        this.bundle = bundle;
        this.cols = cols;
        this.types = types;
        this.init();
    }

    protected void setExpression(Expression e) {
        // Clone the basic expression to allow modifier it without any problem
        // Expression eNew=cloneExpression(e);
        // this.expression=eNew;
        this.expression = e;
        List l = this.getParameterField(this.expression);
        this.t.setListParameter(l);
    }

    public Expression getExpression() {
        return this.expression;
    }

    protected List getParameterField(Expression e) {
        List listParameter = new ArrayList();
        listParameter = this.checkExpression(e, listParameter);
        return listParameter;
    }

    protected List checkExpression(Expression e, List l) {
        Object o = e.getRightOperand();
        if (o instanceof ParameterField) {
            l.add(e);
            return l;
        }

        if (o instanceof Expression) {
            l = this.checkExpression((Expression) o, l);
        }

        o = e.getLeftOperand();
        if (o instanceof Expression) {
            l = this.checkExpression((Expression) o, l);
        }

        return l;
    }

    protected Expression cloneExpression(Expression e) {
        Object o = e.getLeftOperand();
        Object oNewLeft = null;
        if (o instanceof Expression) {
            oNewLeft = this.cloneExpression((Expression) o);
        } else if (o instanceof Field) {
            oNewLeft = new BasicField(((Field) o).toString());
        }

        o = e.getRightOperand();
        Object oNewRight = null;
        if (o instanceof Expression) {
            oNewRight = this.cloneExpression((Expression) o);
        } else if (o instanceof ParameterField) {
            oNewRight = new ParameterField();
        } else {
            oNewRight = o;
        }
        Operator op = e.getOperator();

        return new BasicExpression(oNewLeft, op, oNewRight);
    }

    public void init() {
        this.t = new pvTable(this.bundle, new ArrayList(), this.cols, this.types) {

            @Override
            public Dimension getPreferredScrollableViewportSize() {
                Dimension d = super.getPreferredScrollableViewportSize();
                d.height = this.getRowHeight() * 12;
                return d;
            }
        };

        this.buildView();
    }

    public void setColsAndTypes(String[] cols, int[] types) {
        this.t.setColsAndTypes(cols, types);
    }

    private void buildView() {
        this.bAccept = new com.ontimize.report.ReportDesignerButton(ImageManager.getIcon(ImageManager.OK));
        this.bAccept.setToolTipText(ApplicationManager.getTranslation("QueryBuilderAceptar", this.bundle));
        this.bAccept.setText(ApplicationManager.getTranslation("QueryBuilderAceptar", this.bundle));

        this.getContentPane().setLayout(new GridBagLayout());
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonsPanel.add(this.bAccept);
        this.bAccept.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ParameterValuesDialog.this.setVisible(false);
            }
        });

        this.getContentPane()
            .add(new JLabel(ApplicationManager.getTranslation("QueryBuilderParameterValuesTable", this.bundle)),
                    new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                            new Insets(2, 2, 2, 2), 0, 0));

        this.getContentPane()
            .add(new JScrollPane(this.t),
                    new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                            new Insets(2, 2, 2, 2), 0, 0));

        this.getContentPane()
            .add(buttonsPanel, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.WEST,
                    GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
    }

    protected static ParameterValuesDialog pvd = null;

    public static Expression showParameterValuesTable(Component c, ResourceBundle b, Expression e, String entity) {
        ResourceBundle bundle = b;
        Hashtable ht = QueryBuilder.getColumnTypes(entity, b);
        String[] co = (String[]) ht.get("cols");
        int[] ty = (int[]) ht.get("types");
        if ((co == null) || (ty == null)) {
            return e;
        }

        Window w = SwingUtilities.getWindowAncestor(c);
        if (c instanceof Frame) {
            w = (Frame) c;
        }
        if ((ParameterValuesDialog.pvd == null) || (ParameterValuesDialog.pvd.getOwner() != w)) {
            if (ParameterValuesDialog.pvd != null) {
                ParameterValuesDialog.pvd.dispose();
            }
            if (w instanceof Frame) {
                ParameterValuesDialog.pvd = new ParameterValuesDialog((Frame) w, e, bundle, co, ty);
            } else {
                ParameterValuesDialog.pvd = new ParameterValuesDialog((Dialog) w, e, bundle, co, ty);
            }
            ParameterValuesDialog.pvd.pack();
            ApplicationManager.center(ParameterValuesDialog.pvd);
        }
        ParameterValuesDialog.pvd.setColsAndTypes(co, ty);
        ParameterValuesDialog.pvd.setExpression(e);
        ParameterValuesDialog.pvd.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        ParameterValuesDialog.pvd.setVisible(true);
        return ParameterValuesDialog.pvd.getExpression();
    }

}
