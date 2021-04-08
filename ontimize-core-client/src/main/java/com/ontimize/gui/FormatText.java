package com.ontimize.gui;

import com.ontimize.db.NullValue;

public class FormatText extends MultipleValue {

    protected String textCol = null;

    protected String formatCol = null;

    public FormatText(String textColumn, String formatColumn, String text, String formattedText) {
        super(null);
        this.textCol = textColumn;
        this.formatCol = formatColumn;
        if (text == null) {
            this.put(textColumn, new NullValue(java.sql.Types.VARCHAR));
        } else {
            this.put(textColumn, text);
        }

        if ((formatColumn != null) && (formattedText == null)) {
            this.put(formatColumn, new NullValue(java.sql.Types.VARCHAR));
        } else if (formatColumn != null) {
            this.put(formatColumn, formattedText);
        }
    }

    @Override
    public String toString() {
        if (this.formatCol != null) {
            return (String) this.get(this.formatCol);
        }
        if (this.textCol != null) {
            return (String) this.get(this.textCol);
        }
        return super.toString();
    }

}
