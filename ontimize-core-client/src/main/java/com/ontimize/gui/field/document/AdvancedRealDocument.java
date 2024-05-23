package com.ontimize.gui.field.document;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.jee.common.gui.SearchValue;

public class AdvancedRealDocument extends RealDocument {

	private static final Logger logger = LoggerFactory.getLogger(AdvancedRealDocument.class);

	public static final String OR = "|";

	public static final String BETWEEN = ":";

	public static final String NOT = "!";

	public static final String EQUAL = "=";

	public static final String LESS = "<";

	public static final String MORE = ">";

	public static final String LESS_EQUAL = "<=";

	public static final String MORE_EQUAL = ">=";

	protected boolean advancedQueryMode = false;

	public AdvancedRealDocument() {
	}

	public void setAdvancedQueryMode(boolean advancedQueryMode) {
		this.advancedQueryMode = advancedQueryMode;
	}

	@Override
	public boolean isValid() {
		if (!this.advancedQueryMode) {
			return super.isValid();
		} else {
			if (this.getQueryValue() == null) {
				return false;
			} else {
				return true;
			}
		}
	}

	@Override
	public void format() {
		if (!this.advancedQueryMode) {
			super.format();
		} else {
			try {
				if (!this.isSymbolFirst()) {
					int orIndex = this.getText(0, this.getLength()).indexOf(AdvancedRealDocument.OR);
					int betweenIndex = this.getText(0, this.getLength()).indexOf(AdvancedRealDocument.BETWEEN);
					if (orIndex >= 0) {
						if (orIndex == (this.getLength() - 1)) {
							super.removeWithoutCheck(orIndex, 1);
						}
					} else if (betweenIndex >= 0) {
						if (betweenIndex == (this.getLength() - 1)) {
							super.removeWithoutCheck(betweenIndex, 1);
						}
					} else {
						super.format();
					}
				} else {
					// If the first character is a symbol
					String symbol = this.getDocumentFirstSymbol();
					if (this.getLength() <= symbol.length()) {
						if (!symbol.equals(AdvancedRealDocument.NOT)) {
							this.remove(0, this.getLength());
						}
						return;
					}
					String remainder = this.getText(symbol.length(), this.getLength() - symbol.length());
					try {
						Number number = this.formatter.parse(remainder);
						this.remove(0, this.getLength());
						String newText = symbol + this.formatter.format(number).toString();
						this.insertStringWithoutCheck(0, newText, null);
					} catch (Exception e) {
						AdvancedRealDocument.logger.error(null, e);
					}
				}
			} catch (Exception e) {
				AdvancedRealDocument.logger.error(null, e);
				try {
					super.remove(0, this.getLength());
				} catch (Exception ex) {
					AdvancedRealDocument.logger.trace(null, ex);
				}
			}
		}
	}

	public SearchValue getQueryValue() {
		if (!this.advancedQueryMode) {
			return null;
		}

		// Check for OR and BETWEEN
		try {
			if (this.getLength() == 0) {
				return null;
			}
			if (!this.isSymbolFirst()) {
				String text = this.getText(0, this.getLength());
				int orIndex = text.indexOf(AdvancedRealDocument.OR);
				if (orIndex > 0) {
					List<Object> vDoubles = new ArrayList<>();
					StringTokenizer st = new StringTokenizer(text, AdvancedRealDocument.OR);
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						Double t = this.getDoubleValue(token);
						if (t == null) {
							return null;
						}
						vDoubles.add(t);
					}
					return new SearchValue(SearchValue.OR, vDoubles);
				} else {
					int betweenIndex = text.indexOf(AdvancedRealDocument.BETWEEN);
					if (betweenIndex > 0) {
						List<Object> vDoubles = new ArrayList<>();
						StringTokenizer st = new StringTokenizer(text, AdvancedRealDocument.BETWEEN);
						while (st.hasMoreTokens()) {
							String token = st.nextToken();
							Double t = this.getDoubleValue(token);
							if (t == null) {
								return null;
							}
							vDoubles.add(t);
						}
						return new SearchValue(SearchValue.BETWEEN, vDoubles);
					} else {
						Double t = this.getDoubleValue(text);
						if (t == null) {
							return null;
						} else {
							return new SearchValue(SearchValue.EQUAL, t);
						}
					}
				}
			} else {
				// If the first character is a symbol
				String symbol = this.getDocumentFirstSymbol();
				String text = this.getText(0, this.getLength());
				if (text.length() > symbol.length()) {
					String textNumber = text.substring(symbol.length());
					Double t = this.getDoubleValue(textNumber);
					if (symbol.equals(AdvancedRealDocument.NOT)) {
						if (t == null) {
							return new SearchValue(SearchValue.NULL, null);
						} else {
							return new SearchValue(SearchValue.NOT_EQUAL, t);
						}
					} else {
						if (t == null) {
							return null;
						} else {
							if (symbol.equals(AdvancedRealDocument.LESS)) {
								return new SearchValue(SearchValue.LESS, t);
							} else if (symbol.equals(AdvancedRealDocument.MORE)) {
								return new SearchValue(SearchValue.MORE, t);
							} else if (symbol.equals(AdvancedRealDocument.MORE_EQUAL)) {
								return new SearchValue(SearchValue.MORE_EQUAL, t);
							} else if (symbol.equals(AdvancedRealDocument.LESS_EQUAL)) {
								return new SearchValue(SearchValue.LESS_EQUAL, t);
							} else if (symbol.equals(AdvancedRealDocument.EQUAL)) {
								return new SearchValue(SearchValue.EQUAL, t);
							} else {
								return null;
							}
						}
					}
				} else {
					if (symbol.equals(AdvancedRealDocument.NOT)) {
						return new SearchValue(SearchValue.NULL, null);
					}
					return null;
				}
			}
		} catch (Exception e) {
			AdvancedRealDocument.logger.error(null, e);
			return null;
		}
	}

	protected boolean isStartSymbol(String s) {
		if (s.equals(AdvancedRealDocument.LESS) || s.equals(AdvancedRealDocument.MORE)
				|| s.equals(AdvancedRealDocument.NOT) || s.equals(AdvancedRealDocument.LESS_EQUAL) || s
				.equals(AdvancedRealDocument.MORE_EQUAL)
				|| s.equals(AdvancedRealDocument.EQUAL)) {
			return true;
		} else {
			return false;
		}
	}

	protected boolean isORSymbolAllowed(int offset) throws BadLocationException {
		return this.isOROffset(offset);
	}

	/**
	 * Checks if the first character is a condition symbol
	 * @return true if the first character is a condition symbol
	 * @throws BadLocationException
	 */
	protected boolean isSymbolFirst() throws BadLocationException {
		if ((this.getLength() > 0)
				&& (this.getText(0, 1).equals(AdvancedRealDocument.LESS)
						|| this.getText(0, 1).equals(AdvancedRealDocument.MORE) || this.getText(0, 1)
						.equals(AdvancedRealDocument.NOT)
						|| this.getText(0, 1).equals(AdvancedRealDocument.EQUAL))) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @return The symbol at the beginning of the document if it exists
	 * @throws BadLocationException
	 */
	protected String getDocumentFirstSymbol() throws BadLocationException {
		if (this.getLength() > 0) {
			if (this.getText(0, 1).equals(AdvancedRealDocument.LESS)) {
				if (this.getLength() > 1) {
					if (this.getText(1, 1).equals(AdvancedRealDocument.EQUAL)) {
						return AdvancedRealDocument.LESS_EQUAL;
					} else {
						return AdvancedRealDocument.LESS;
					}
				} else {
					return AdvancedRealDocument.LESS;
				}
			} else if (this.getText(0, 1).equals(AdvancedRealDocument.MORE)) {
				if (this.getLength() > 1) {
					if (this.getText(1, 1).equals(AdvancedRealDocument.EQUAL)) {
						return AdvancedRealDocument.MORE_EQUAL;
					} else {
						return AdvancedRealDocument.MORE;
					}
				} else {
					return AdvancedRealDocument.MORE;
				}
			} else if (this.getText(0, 1).equals(AdvancedRealDocument.NOT)) {
				return AdvancedRealDocument.NOT;
			} else if (this.getText(0, 1).equals(AdvancedRealDocument.EQUAL)) {
				return AdvancedRealDocument.EQUAL;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	protected boolean isOROffset(int offset) throws BadLocationException {
		if ((offset > 1) && this.isORSymbolAllowed(offset)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void remove(int offset, int length) throws BadLocationException {
		if (!this.advancedQueryMode) {
			super.remove(offset, length);
			return;
		} else {
			super.removeWithoutCheck(offset, length);
		}
	}

	@Override
	public void insertString(int offset, String s, AttributeSet attributes) throws BadLocationException {
		if (s.length() == 0) {
			return;
		}
		char decimalSeparator = this.symbology.getDecimalSeparator();
		if (!this.advancedQueryMode) {
			super.insertString(offset, s, attributes);
			return;
		}
		// The characters must be inserted one by one
		if (s.length() > 1) {
			for (int i = 0; i < s.length(); i++) {
				this.insertString(offset + i, s.substring(i, i), attributes);
			}
		}
		if (offset == 0) {
			this.insertStringIfOffsetIsZero(offset, s, attributes);
		} else { // Offset is > 0
			if (this.isSymbolFirst()) {
				this.insertFirstSymbol(offset, s, attributes, decimalSeparator);
			} else {
				// offset is not 1 and the first character is not a symbol.
				if (Character.isDigit(s.charAt(0)) || (s.charAt(0) == decimalSeparator)) {
					this.insertStringIfOffsetIsNot1AndFirstCharacterIsNotASymbol(offset, s, attributes);
				} else {
					this.insertStringIfSymbolISOROrBETWEEN(offset, s, attributes);
				}
			}
		}
	}

	protected void insertFirstSymbol(int offset, String s, AttributeSet attributes, char decimalSeparator)
			throws BadLocationException {
		String symbol = this.getDocumentFirstSymbol();
		// If offset is 1 and the first character is < or > then symbol
		// '=' is allowed
		if (offset == 1) {
			if (this.checkIfSymbolIsLessMoreEqual(s, symbol)) {
				if ((this.getLength() > 1) && this.getText(1, 1).equals(AdvancedRealDocument.EQUAL)) {
					return;
				}
				this.insertStringWithoutCheck(offset, s, attributes);
			} else { // Only digits can be inserted or the symbol '-'
				if (Character.isDigit(s.charAt(0)) || (s.charAt(0) == '-')) {
					if ((this.getLength() > 1) && this.getText(1, 1).equals(AdvancedRealDocument.EQUAL)) {
						return;
					}
					if (s.charAt(0) == '-') {
						this.insertStringWithoutCheck(offset, s, attributes);
						return;
					}
					String currentText = this.getText(0, this.getLength());
					try {
						StringBuilder sb = new StringBuilder(currentText);
						sb.insert(offset, s);
						this.formatter.parse(sb.toString().substring(symbol.length()));
						this.insertStringWithoutCheck(offset, s, attributes);
					} catch (Exception e) {
						AdvancedRealDocument.logger.trace(null, e);
						// New number is invalid.
					}
				} else {
					// Nothing
				}
			}
		} else {
			if ((offset == symbol.length()) && (this.getLength() == symbol.length()) && (s.charAt(0) == '-')) {
				super.insertStringWithoutCheck(offset, s, attributes);
				return;
			}
			if (Character.isDigit(s.charAt(0)) || (s.charAt(0) == decimalSeparator)) {
				// Offset is not 1 and there is a symbol.
				symbol = this.getDocumentFirstSymbol();
				String currentText = this.getText(0, this.getLength());
				try {
					StringBuilder sb = new StringBuilder(currentText);
					sb.insert(offset, s);
					this.formatter.parse(sb.toString().substring(symbol.length()));
					this.insertStringWithoutCheck(offset, s, attributes);
				} catch (Exception e) {
					AdvancedRealDocument.logger.trace(null, e);
					// New number is not valid.
				}
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #insertString(int, String, AttributeSet)}
	 * @param offset
	 * @param s
	 * @param attributes
	 * @throws BadLocationException
	 */
	protected void insertStringIfOffsetIsNot1AndFirstCharacterIsNotASymbol(int offset, String s,
			AttributeSet attributes) throws BadLocationException {
		String currentText = this.getText(0, this.getLength());
		int orIndex = currentText.indexOf(AdvancedRealDocument.OR);
		int betweenIndex = currentText.indexOf(AdvancedRealDocument.BETWEEN);
		if (orIndex >= 0) {
			// Separate the number
			StringBuilder sb = new StringBuilder(currentText);
			sb.insert(offset, s);
			StringTokenizer st = new StringTokenizer(sb.toString(), AdvancedRealDocument.OR);
			boolean allowed = true;
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				try {
					this.formatter.parse(token);
				} catch (Exception e) {
					AdvancedRealDocument.logger.trace(null, e);
					allowed = false;
					break;
				}
			}
			if (allowed) {
				this.insertStringWithoutCheck(offset, s, attributes);
			}
		} else if (betweenIndex >= 0) {
			StringBuilder sb = new StringBuilder(currentText);
			sb.insert(offset, s);
			StringTokenizer st = new StringTokenizer(sb.toString(), AdvancedRealDocument.BETWEEN);
			boolean allowed = true;
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				try {
					this.formatter.parse(token);
				} catch (Exception e) {
					AdvancedRealDocument.logger.trace(null, e);
					allowed = false;
					break;
				}
			}
			if (allowed) {
				this.insertStringWithoutCheck(offset, s, attributes);
			}
		} else {
			try {
				StringBuilder sb = new StringBuilder(currentText);
				sb.insert(offset, s);
				this.formatter.parse(sb.toString());
				this.insertStringWithoutCheck(offset, s, attributes);
			} catch (Exception e) {
				AdvancedRealDocument.logger.trace(null, e);
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #insertString(int, String, AttributeSet)}
	 * @param s
	 * @param symbol
	 * @return
	 */
	protected boolean checkIfSymbolIsLessMoreEqual(String s, String symbol) {
		return (symbol.equals(AdvancedRealDocument.LESS) || symbol.equals(AdvancedRealDocument.MORE))
				&& s.equals(AdvancedRealDocument.EQUAL);
	}

	/**
	 * Method used to reduce the complexity of {@link #insertString(int, String, AttributeSet)}
	 * @param offset
	 * @param s
	 * @param attributes
	 * @throws BadLocationException
	 */
	protected void insertStringIfSymbolISOROrBETWEEN(int offset, String s, AttributeSet attributes)
			throws BadLocationException {
		// It is not a number, can be OR or BETWEEN
		if (s.equals(AdvancedRealDocument.OR)) {
			// It is possible to insert if threre is a previous
			// number
			if (offset > 0) {
				if ((this.getLength() > offset) && this.getText(offset, 1).equals(AdvancedRealDocument.OR)) {
					this.remove(offset, 1);
				}
				this.insertStringWithoutCheck(offset, s, attributes);
			}
		} else {
			this.insertStringIfSymbolisBetween(offset, s, attributes);
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #insertString(int, String, AttributeSet)}
	 * @param offset
	 * @param s
	 * @param attributes
	 * @throws BadLocationException
	 */
	protected void insertStringIfSymbolisBetween(int offset, String s, AttributeSet attributes)
			throws BadLocationException {
		if (s.equals(AdvancedRealDocument.BETWEEN)) {
			if (this.getText(0, this.getLength()).indexOf(AdvancedRealDocument.BETWEEN) < 0) {
				if (offset > 0) {
					if ((this.getLength() > offset) && this.getText(offset, 1).equals(AdvancedRealDocument.BETWEEN)) {
						this.remove(offset, 1);
					}
					this.insertStringWithoutCheck(offset, s, attributes);
				}
			}
		} else {
			// Nothing
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #insertString(int, String, AttributeSet)}
	 * @param offset
	 * @param s
	 * @param attributes
	 * @throws BadLocationException
	 */
	protected void insertStringIfOffsetIsZero(int offset, String s, AttributeSet attributes)
			throws BadLocationException {
		if (this.isStartSymbol(s)) {
			if (this.isSymbolFirst()) {
				String symbol = this.getDocumentFirstSymbol();
				if (symbol.length() == 1) {
					super.remove(0, 1);
					this.insertStringWithoutCheck(offset, s, attributes);
				} else {
					if (s.equals(AdvancedRealDocument.LESS) || s.equals(AdvancedRealDocument.MORE)) {
						super.remove(0, 1);
						this.insertStringWithoutCheck(offset, s, attributes);
					} else {
						super.remove(0, 2);
						this.insertStringWithoutCheck(offset, s, attributes);
					}
				}
			} else {
				this.insertStringWithoutCheck(offset, s, attributes);
			}
		} else {
			// It is not possible to insert a number in offset zero if a
			// symbol exists
			if (this.isSymbolFirst()) {
				// Nothing is done
			} else {
				// There is no symbol, then insert
				super.insertString(offset, s, attributes);
			}
		}
	}

}
