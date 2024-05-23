package com.ontimize.gui.field.document;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.jee.common.gui.SearchValue;

public class AdvancedIntegerDocument extends IntegerDocument {

	private static final Logger logger = LoggerFactory.getLogger(AdvancedIntegerDocument.class);

	public static final String OR = "|";

	public static final String BETWEEN = ":";

	public static final String NOT = "!";

	public static final String EQUAL = "=";

	public static final String LESS = "<";

	public static final String MORE = ">";

	public static final String LESS_EQUAL = "<=";

	public static final String MORE_EQUAL = ">=";

	protected boolean advancedQueryMode = false;

	public AdvancedIntegerDocument() {
	}

	@Override
	public boolean isRight() {
		if (!this.advancedQueryMode) {
			return super.isRight();
		} else {
			if (this.getQueryValue() == null) {
				return false;
			} else {
				return true;
			}
		}
	}

	@Override
	protected void updateValue() {
		Number previousValue = this.integerValue;
		try {
			String documentFirstSymbol = this.getDocumentFirstSymbol();
			String currentText = this.getText(documentFirstSymbol == null ? 0 : this.getDocumentFirstSymbol().length(),
					this.getLength());
			if (currentText.length() == 0) {
				this.integerValue = this.getNumericValue("0");
			} else {
				this.integerValue = this.getNumericValue(currentText);
			}
		} catch (Exception e) {
			AdvancedIntegerDocument.logger.trace(null, e);
			this.integerValue = previousValue;
		}
	}

	@Override
	public void format() {
		if (!this.advancedQueryMode) {
			super.format();
		} else {
			try {
				if (!this.isSymbolFirst()) {
					int orIndex = this.getText(0, this.getLength()).indexOf(AdvancedIntegerDocument.OR);
					int betweenIndex = this.getText(0, this.getLength()).indexOf(AdvancedIntegerDocument.BETWEEN);
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
				}
			} catch (Exception e) {
				AdvancedIntegerDocument.logger.trace(null, e);
				try {
					super.remove(0, this.getLength());
				} catch (Exception ex) {
					AdvancedIntegerDocument.logger.trace(null, ex);
				}
			}
		}
	}

	public void setAdvancedQueryMode(boolean advancedQueryMode) {
		this.advancedQueryMode = advancedQueryMode;
	}

	public void setQueryValue(SearchValue value) {
		try {
			switch (value.getCondition()) {
				case SearchValue.BETWEEN:
					List<?> vValues = (List<?>) value.getValue();
					Object d1 = vValues.get(0);
					Object d2 = vValues.get(1);
					String string1 = this.numberFormat.format(d1);
					String string2 = this.numberFormat.format(d2);
					this.remove(0, this.getLength());
					if (ApplicationManager.DEBUG) {
						AdvancedIntegerDocument.logger.debug(this.getClass().toString() + " Inserting: " + string1
								+ AdvancedIntegerDocument.BETWEEN + string2);
					}
					this.insertString(0, string1 + AdvancedIntegerDocument.BETWEEN + string2, null);
					return;
				case SearchValue.OR:
					StringBuilder sb = new StringBuilder();
					vValues = (List<?>) value.getValue();
					for (int i = 0; i < vValues.size(); i++) {
						boolean isGroupingUsed = this.numberFormat.isGroupingUsed();
						this.numberFormat.setGroupingUsed(false);
						String stringValue = this.numberFormat.format(vValues.get(i));
						this.numberFormat.setGroupingUsed(isGroupingUsed);
						sb.append(stringValue);
						if (i < (vValues.size() - 1)) {
							sb.append(AdvancedIntegerDocument.OR);
						}
					}
					this.remove(0, this.getLength());
					if (ApplicationManager.DEBUG) {
						AdvancedIntegerDocument.logger
						.debug(this.getClass().toString() + " Inserting: " + sb.toString());
					}
					this.insertString(0, sb.toString(), null);
					return;
				default:
					sb = new StringBuilder();
					Object current = value.getValue();
					sb.append(value.getStringCondition());
					boolean isGroupingUsed = this.numberFormat.isGroupingUsed();
					this.numberFormat.setGroupingUsed(false);
					String stringValue = this.numberFormat.format(current);
					this.numberFormat.setGroupingUsed(isGroupingUsed);
					sb.append(stringValue);
					this.remove(0, this.getLength());
					if (ApplicationManager.DEBUG) {
						AdvancedIntegerDocument.logger
						.debug(this.getClass().toString() + " Inserting: " + sb.toString());
					}
					this.insertString(0, sb.toString(), null);
					return;
			}
		} catch (Exception e) {
			if (ApplicationManager.DEBUG) {
				AdvancedIntegerDocument.logger.debug(null, e);
			} else {
				AdvancedIntegerDocument.logger.trace(null, e);
			}
		}
	}

	public SearchValue getQueryValue() {
		if (!this.advancedQueryMode) {
			return null;
		}
		// If the first character is not a condition symbol then look for OR and
		// BETWEEN
		try {
			if (this.getLength() == 0) {
				return null;
			}
			if (!this.isSymbolFirst()) {
				String sText = this.getText(0, this.getLength());
				int orIndex = sText.indexOf(AdvancedIntegerDocument.OR);
				if (orIndex > 0) {
					List<Number> vIntegers = new ArrayList<>();
					// Now get the values
					StringTokenizer st = new StringTokenizer(sText, AdvancedIntegerDocument.OR);
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						Number t = this.getNumericValue(token);
						if (t == null) {
							return null;
						}
						vIntegers.add(t);
					}
					return new SearchValue(SearchValue.OR, vIntegers);
				} else {
					int betweenIndex = sText.indexOf(AdvancedIntegerDocument.BETWEEN);
					if (betweenIndex > 0) {
						List<Number> vIntegers = new ArrayList<>();
						StringTokenizer st = new StringTokenizer(sText, AdvancedIntegerDocument.BETWEEN);
						while (st.hasMoreTokens()) {
							String token = st.nextToken();
							Number t = this.getNumericValue(token);
							if (t == null) {
								return null;
							}
							vIntegers.add(t);
						}
						return new SearchValue(SearchValue.BETWEEN, vIntegers);
					} else {
						Number t = this.getNumericValue(sText);
						if (t == null) {
							return null;
						} else {
							return new SearchValue(SearchValue.EQUAL, t);
						}
					}
				}
			} else {
				// The first character is a symbol
				String symbol = this.getDocumentFirstSymbol();
				String sText = this.getText(0, this.getLength());
				if (sText.length() > symbol.length()) {
					String textNumber = sText.substring(symbol.length());
					Number numericValue = this.getNumericValue(textNumber);
					if (symbol.equals(AdvancedIntegerDocument.NOT)) {
						if (numericValue == null) {
							return new SearchValue(SearchValue.NULL, null);
						} else {
							return new SearchValue(SearchValue.NOT_EQUAL, numericValue);
						}
					} else {
						if (numericValue == null) {
							return null;
						} else {
							if (symbol.equals(AdvancedIntegerDocument.LESS)) {
								return new SearchValue(SearchValue.LESS, numericValue);
							} else if (symbol.equals(AdvancedIntegerDocument.MORE)) {
								return new SearchValue(SearchValue.MORE, numericValue);
							} else if (symbol.equals(AdvancedIntegerDocument.MORE_EQUAL)) {
								return new SearchValue(SearchValue.MORE_EQUAL, numericValue);
							} else if (symbol.equals(AdvancedIntegerDocument.LESS_EQUAL)) {
								return new SearchValue(SearchValue.LESS_EQUAL, numericValue);
							} else if (symbol.equals(AdvancedIntegerDocument.EQUAL)) {
								return new SearchValue(SearchValue.EQUAL, numericValue);
							} else {
								return null;
							}
						}
					}
				} else {
					if (symbol.equals(AdvancedIntegerDocument.NOT)) {
						return new SearchValue(SearchValue.NULL, null);
					}
					return null;
				}
			}
		} catch (Exception e) {
			AdvancedIntegerDocument.logger.error(null, e);
			return null;
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
		if (!this.advancedQueryMode) {
			super.insertString(offset, s, attributes);
			return;
		}
		// Characters must be inserted one by one
		if ((offset == 0) && (s.length() > 1)) {
			for (int i = 0; i < s.length(); i++) {
				this.insertString(offset + i, s.substring(i, i + 1), attributes);
				if (".".equals(s.substring(i, i + 1))) {
					// When a point is included in advanced query mode it must
					// be removed (To avoid parse errors)
					offset--;
				}
			}
			return;
		}
		if (offset == 0) {
			this.insertStringIfOffsetIsZero(offset, s, attributes);
		} else {
			if (this.isSymbolFirst()) {
				this.insertFirstSymbol(offset, s, attributes);
				return;
			} else {
				// offset is not 1 and the first character is not a symbol.
				// If the text is a number then insert. If it is OR or BETWEEN
				// it depends
				if (Character.isDigit(s.charAt(0))) {
					// If text after insertion is not a valid number then undo.
					// OR and BETWEEN can exists too.
					String currentText = this.getText(0, this.getLength());
					int orIndex = currentText.indexOf(AdvancedIntegerDocument.OR);
					int betweenIndex = currentText.indexOf(AdvancedIntegerDocument.BETWEEN);
					if (orIndex >= 0) {
						// Separate numbers
						StringBuilder sb = new StringBuilder(currentText);
						sb.insert(offset, s);
						StringTokenizer st = new StringTokenizer(sb.toString(), AdvancedIntegerDocument.OR);
						boolean allowed = true;
						while (st.hasMoreTokens()) {
							String token = st.nextToken();
							try {
								this.numberFormat.parse(token);
							} catch (Exception e) {
								AdvancedIntegerDocument.logger.trace(null, e);
								// The new number is not valid, then it is not allowed
								allowed = false;
								break;
							}
						}
						if (allowed) {
							this.insertStringWithoutCheck(offset, s, attributes);
						}
					} else if (betweenIndex >= 0) {
						// Separate the numbers
						StringBuilder sb = new StringBuilder(currentText);
						sb.insert(offset, s);
						StringTokenizer st = new StringTokenizer(sb.toString(), AdvancedIntegerDocument.BETWEEN);
						boolean allowed = true;
						while (st.hasMoreTokens()) {
							String token = st.nextToken();
							try {
								this.numberFormat.parse(token);
							} catch (Exception e) {
								AdvancedIntegerDocument.logger.trace(null, e);
								// The new number is not valid, then it is not allowed
								allowed = false;
								break;
							}
						}
						if (allowed) {
							this.insertStringWithoutCheck(offset, s, attributes);
						}
					} else {// It is only one number
						try {
							StringBuilder sb = new StringBuilder(currentText);
							sb.insert(offset, s);
							this.numberFormat.parse(sb.toString());
							this.insertStringWithoutCheck(offset, s, attributes);
						} catch (Exception e) {
							AdvancedIntegerDocument.logger.trace(null, e);
							// The new number is not valid then does not insert the character
						}
					}
				} else {
					// It is not a number, can be OR or BETWEEN
					this.insertStringIfSymbolIsOROrBETWEEN(offset, s, attributes);
				}
			}
		}
	}

	protected void insertFirstSymbol(int offset, String s, AttributeSet attributes) throws BadLocationException {
		String symbol = this.getDocumentFirstSymbol();
		// If offset is 1 and the first character is a symbol < or >
		// then character '=' is allowed
		if (offset == 1) {
			if (this.checkIfSymbolIsLessMoreEqual(s, symbol)) {
				if ((this.getLength() > 1) && this.getText(1, 1).equals(AdvancedIntegerDocument.EQUAL)) {
					return;
				}
				this.insertStringWithoutCheck(offset, s, attributes);
			} else { // Only digits allowed
				if (Character.isDigit(s.charAt(0)) || (s.charAt(0) == '-')) {
					if ((this.getLength() > 1) && this.getText(1, 1).equals(AdvancedIntegerDocument.EQUAL)) {
						return;
					}
					if (s.charAt(0) == '-') {
						this.insertStringWithoutCheck(offset, s, attributes);
						return;
					}
					// We must check if after the insertion in the
					// correct place
					// the numbers are valid.
					// Offset is 1, then the first character is a symbol
					// and only digits are allowed
					String currentText = this.getText(0, this.getLength());
					try {
						StringBuilder sb = new StringBuilder(currentText);
						sb.insert(offset, s);
						this.numberFormat.parse(sb.toString().substring(symbol.length()));
						this.insertStringWithoutCheck(offset, s, attributes);
					} catch (Exception e) {
						AdvancedIntegerDocument.logger.trace(null, e);
						// The new number is not valid. This is not allowed
					}
				} else {
					// Nothing
				}
			}
		} else {
			// If offset is the last character and there are no more
			// characters and it is a '-' character
			// then insert
			if ((offset == symbol.length()) && (this.getLength() == symbol.length()) && (s.charAt(0) == '-')) {
				super.insertStringWithoutCheck(offset, s, attributes);
				return;
			}
			if (Character.isDigit(s.charAt(0))) {
				// Offset is not 1 and the first character is a symbol.
				// If the text after the insertion is not a valid number
				// then no insertion is done
				symbol = this.getDocumentFirstSymbol();
				String currentText = this.getText(0, this.getLength());
				try {
					StringBuilder sb = new StringBuilder(currentText);
					sb.insert(offset, s);
					this.numberFormat.parse(sb.toString().substring(symbol.length()));
					this.insertStringWithoutCheck(offset, s, attributes);
				} catch (Exception e) {
					AdvancedIntegerDocument.logger.trace(null, e);
					// The new number is not valid. This is not allowed
				}
			}
		}
	}

	/**
	 * @param s
	 * @param symbol
	 * @return
	 */
	protected boolean checkIfSymbolIsLessMoreEqual(String s, String symbol) {
		return (symbol.equals(AdvancedIntegerDocument.LESS) || symbol.equals(AdvancedIntegerDocument.MORE))
				&& s.equals(AdvancedIntegerDocument.EQUAL);
	}

	/**
	 * @param offset
	 * @param s
	 * @param attributes
	 * @throws BadLocationException
	 */
	protected void insertStringIfSymbolIsOROrBETWEEN(int offset, String s, AttributeSet attributes)
			throws BadLocationException {
		if (s.equals(AdvancedIntegerDocument.OR)) {
			// OR is valid if there is a number first
			if (offset > 0) {
				if ((this.getLength() > offset) && this.getText(offset, 1).equals(AdvancedIntegerDocument.OR)) {
					this.remove(offset, 1);
				}
				this.insertStringWithoutCheck(offset, s, attributes);
			}
		} else {
			this.insertStringIfSymbolIsBetween(offset, s, attributes);
		}
	}

	/**
	 * @param offset
	 * @param s
	 * @param attributes
	 * @throws BadLocationException
	 */
	protected void insertStringIfSymbolIsBetween(int offset, String s, AttributeSet attributes)
			throws BadLocationException {
		if (s.equals(AdvancedIntegerDocument.BETWEEN)) {
			// BETWEEN is valid if there is no other BETWEEN
			if (this.getText(0, this.getLength()).indexOf(AdvancedIntegerDocument.BETWEEN) < 0) {
				if (offset > 0) {
					if ((this.getLength() > offset)
							&& this.getText(offset, 1).equals(AdvancedIntegerDocument.BETWEEN)) {
						this.remove(offset, 1);
					}
					this.insertStringWithoutCheck(offset, s, attributes);
				}
			}
		} else {
			// This is not allowed
		}
	}

	/**
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
					if (s.equals(AdvancedIntegerDocument.LESS) || s.equals(AdvancedIntegerDocument.MORE)) {
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
			if (this.isSymbolFirst()) {
			} else {
				super.insertString(offset, s, attributes);
			}
		}
	}

	protected boolean isStartSymbol(String s) {
		if (s.equals(AdvancedIntegerDocument.LESS) || s.equals(AdvancedIntegerDocument.MORE)
				|| s.equals(AdvancedIntegerDocument.NOT) || s
				.equals(AdvancedIntegerDocument.LESS_EQUAL)
				|| s.equals(AdvancedIntegerDocument.MORE_EQUAL) || s.equals(AdvancedIntegerDocument.EQUAL)) {
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
				&& (this.getText(0, 1).equals(AdvancedIntegerDocument.LESS)
						|| this.getText(0, 1).equals(AdvancedIntegerDocument.MORE) || this.getText(0, 1)
						.equals(AdvancedIntegerDocument.NOT)
						|| this.getText(0, 1).equals(AdvancedIntegerDocument.EQUAL))) {
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
			if (this.getText(0, 1).equals(AdvancedIntegerDocument.LESS)) {
				if (this.getLength() > 1) {
					if (this.getText(1, 1).equals(AdvancedIntegerDocument.EQUAL)) {
						return AdvancedIntegerDocument.LESS_EQUAL;
					} else {
						return AdvancedIntegerDocument.LESS;
					}
				} else {
					return AdvancedIntegerDocument.LESS;
				}
			} else if (this.getText(0, 1).equals(AdvancedIntegerDocument.MORE)) {
				if (this.getLength() > 1) {
					if (this.getText(1, 1).equals(AdvancedIntegerDocument.EQUAL)) {
						return AdvancedIntegerDocument.MORE_EQUAL;
					} else {
						return AdvancedIntegerDocument.MORE;
					}
				} else {
					return AdvancedIntegerDocument.MORE;
				}
			} else if (this.getText(0, 1).equals(AdvancedIntegerDocument.NOT)) {
				return AdvancedIntegerDocument.NOT;
			} else if (this.getText(0, 1).equals(AdvancedIntegerDocument.EQUAL)) {
				return AdvancedIntegerDocument.EQUAL;
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

}
