package com.ontimize.gui.field.document;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.jee.common.gui.SearchValue;

public class AdvancedDateDocument extends DateDocument {

	private static final Logger logger = LoggerFactory.getLogger(AdvancedDateDocument.class);

	public static final String OR = "|";

	public static final String BETWEEN = ":";

	public static final String NOT = "!";

	public static final String EQUAL = "=";

	public static final String LESS = "<";

	public static final String MORE = ">";

	public static final String LESS_EQUAL = "<=";

	public static final String MORE_EQUAL = ">=";

	protected boolean advancedQueryMode = false;

	public AdvancedDateDocument() {
		super();
		this.currentTimestamp = new Timestamp(System.currentTimeMillis());
	}

	public void setAdvancedQueryMode(boolean advancedQueryMode) {
		this.advancedQueryMode = advancedQueryMode;
	}

	public void setValue(SearchValue value) {
		try {
			switch (value.getCondition()) {
				case SearchValue.BETWEEN:
					this.processBetweenSearchValue(value);
					return;
				case SearchValue.OR:
					this.processOrSearchValue(value);
					return;
				default:
					StringBuilder sb = new StringBuilder();
					Date v = (Date) value.getValue();
					sb.append(value.getStringCondition());
					String stringDate = this.dateFormat.format(v);
					sb.append(stringDate);
					this.remove(0, this.getLength());
					if (ApplicationManager.DEBUG) {
						AdvancedDateDocument.logger.debug(this.getClass().toString() + " Inserting: " + sb.toString());
					}
					this.insertString(0, sb.toString(), null);
					return;
			}
		} catch (Exception e) {
			if (ApplicationManager.DEBUG) {
				AdvancedDateDocument.logger.debug(this.getClass().toString() + ": " + e.getMessage(), e);
			}
		}
	}

	protected void processOrSearchValue(SearchValue value) throws BadLocationException {
		List<?> vValues = (List<?>) value.getValue();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < vValues.size(); i++) {
			String stringDate = this.dateFormat.format((Date) vValues.get(i));
			sb.append(stringDate);
			if (i < (vValues.size() - 1)) {
				sb.append(AdvancedDateDocument.OR);
			}
		}
		this.remove(0, this.getLength());
		AdvancedDateDocument.logger.debug(this.getClass().toString() + " Inserting: " + sb.toString());
		this.insertString(0, sb.toString(), null);
	}

	protected void processBetweenSearchValue(SearchValue value) throws BadLocationException {
		List<?> vValues = (List<?>) value.getValue();
		Date d1 = (Date) vValues.get(0);
		Date d2 = (Date) vValues.get(1);
		String stringDate1 = this.dateFormat.format(d1);
		String stringDate2 = this.dateFormat.format(d2);
		this.remove(0, this.getLength());
		AdvancedDateDocument.logger.debug(this.getClass().toString() + "  Inserting: " + stringDate1
				+ AdvancedDateDocument.BETWEEN + stringDate2);
		this.insertString(0, stringDate1 + AdvancedDateDocument.BETWEEN + stringDate2, null);
	}

	@Override
	public boolean isValid() {
		if (!this.advancedQueryMode) {
			return super.isValid();
		}
		try {
			// If the first character is a symbol like <,>,=,!, then gets the
			// text after the symbol
			if (this.isSymbolFirst()) {
				String symbol = this.getDocumentFirstSymbol();
				String sText = this.getText(0, this.getLength());
				if (sText.length() > symbol.length()) {
					return super.isValid(sText.substring(symbol.length()));
				} else {
					if (symbol.equals(AdvancedDateDocument.NOT)) {
						if (sText.length() == symbol.length()) {
							return true;
						} else {
							return false;
						}
					}
					return false;
				}
			} else { // Some values can exist. Search for OR or BETWEEN
				String sText = this.getText(0, this.getLength());
				int orIndex = sText.indexOf(AdvancedDateDocument.OR);
				if (orIndex > 0) {
					// Now get the values
					String token0 = null;
					StringTokenizer st = new StringTokenizer(sText, AdvancedDateDocument.OR);
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						if (token0 == null) {
							token0 = token;
						}
						boolean valid = this.isValid(token);
						if (!valid) {
							return false;
						}
					}
					try {
						Date dateAux = this.dateFormat.parse(token0);
						this.currentTimestamp = new Timestamp(dateAux.getTime());
						return true;
					} catch (Exception e) {
						AdvancedDateDocument.logger.trace(null, e);
						return false;
					}

				} else {
					int betweenIndex = sText.indexOf(AdvancedDateDocument.BETWEEN);
					if (betweenIndex > 0) {
						// Now get the values
						String token0 = null;
						StringTokenizer st = new StringTokenizer(sText, AdvancedDateDocument.BETWEEN);
						while (st.hasMoreTokens()) {
							String token = st.nextToken();
							if (token0 == null) {
								token0 = token;
							}
							boolean valid = this.isValid(token);
							if (!valid) {
								return false;
							}
						}
						try {
							Date fechaAux = this.dateFormat.parse(token0);
							this.currentTimestamp = new Timestamp(fechaAux.getTime());
							return true;
						} catch (Exception e) {
							AdvancedDateDocument.logger.trace(null, e);
							return false;
						}
					} else {
						return super.isValid();
					}
				}
			}
		} catch (Exception e) {
			AdvancedDateDocument.logger.error(null, e);
			return false;
		}
	}

	public SearchValue getQueryValue() {
		if (!this.advancedQueryMode) {
			return null;
		}
		try {
			if (this.getLength() == 0) {
				return null;
			}
			// If the first character is a symbol like <,>,=,!, then gets the
			// text after the symbol
			if (this.isSymbolFirst()) {
				String symbol = this.getDocumentFirstSymbol();
				String sText = this.getText(0, this.getLength());
				if (sText.length() > symbol.length()) {
					String dateString = sText.substring(symbol.length());
					Timestamp t = this.getTimestampValue(dateString);
					if (symbol.equals(AdvancedDateDocument.NOT)) {
						if (t == null) {
							return new SearchValue(SearchValue.NULL, null);
						} else {
							return new SearchValue(SearchValue.NOT_EQUAL, t);
						}
					} else {
						if (t == null) {
							return null;
						} else {
							if (symbol.equals(AdvancedDateDocument.LESS)) {
								return new SearchValue(SearchValue.LESS, t);
							} else if (symbol.equals(AdvancedDateDocument.MORE)) {
								return new SearchValue(SearchValue.MORE, t);
							} else if (symbol.equals(AdvancedDateDocument.MORE_EQUAL)) {
								return new SearchValue(SearchValue.MORE_EQUAL, t);
							} else if (symbol.equals(AdvancedDateDocument.LESS_EQUAL)) {
								return new SearchValue(SearchValue.LESS_EQUAL, t);
							} else if (symbol.equals(AdvancedDateDocument.EQUAL)) {
								return new SearchValue(SearchValue.EQUAL, t);
							} else {
								return null;
							}
						}
					}
				} else {
					if (symbol.equals(AdvancedDateDocument.NOT)) {
						return new SearchValue(SearchValue.NULL, null);
					}
					return null;
				}
			} else { // Search for OR
				String sText = this.getText(0, this.getLength());
				int orIndex = sText.indexOf(AdvancedDateDocument.OR);
				if (orIndex > 0) {
					List<Object> timestamps = new ArrayList<>();
					// Get the values
					StringTokenizer st = new StringTokenizer(sText, AdvancedDateDocument.OR);
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						Timestamp t = this.getTimestampValue(token);
						if (t == null) {
							return null;
						}
						timestamps.add(t);
					}
					return new SearchValue(SearchValue.OR, timestamps);
				} else {
					int betweenIndex = sText.indexOf(AdvancedDateDocument.BETWEEN);
					if (betweenIndex > 0) {
						List<Object> timestamps = new ArrayList<>();
						StringTokenizer st = new StringTokenizer(sText, AdvancedDateDocument.BETWEEN);
						while (st.hasMoreTokens()) {
							String token = st.nextToken();
							Timestamp t = this.getTimestampValue(token);
							if (t == null) {
								return null;
							}
							timestamps.add(t);
						}
						return new SearchValue(SearchValue.BETWEEN, timestamps);
					} else {
						Timestamp t = this.getTimestampValue(sText);
						if (t == null) {
							return null;
						} else {
							return new SearchValue(SearchValue.EQUAL, t);
						}
					}
				}
			}
		} catch (Exception exc) {
			AdvancedDateDocument.logger.error(null, exc);
			return null;
		}
	}

	@Override
	public void setComponentLocale(Locale l) {
		if (!this.advancedQueryMode) {
			super.setComponentLocale(l);
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
		// It is only possible insert characters one by one
		this.insertStringCharacterOneByOne(offset, s, attributes);
		// If offset is zero
		if (offset == 0) {
			this.insertStringIfOffsetIsZero(offset, s, attributes);
		} else { // Offset is different than 0, then look the first character
			if (this.isSymbolFirst()) {
				String symbol = this.getDocumentFirstSymbol();
				// If offset is 1 and the first character is < or > then = is
				// accepted
				if (!this.insertFirstSymbol(offset, s, attributes, symbol)) {
					return;
				}
			} else {
				if (!this.insertSymbol(offset, s, attributes)) {
					return;
				}
			}
		}
	}

	protected boolean insertSymbol(int offset, String s, AttributeSet attributes) throws BadLocationException {
		// Offset is not 1 and the first character is not a symbol.
		// If offset is less than pattern length then use the pattern to
		// insert
		if (offset < this.datePattern.length()) {
			if (this.datePattern.charAt(offset) == '/') {
				if ((this.getLength() > offset) && this.getText(offset, 1).equals("/")) {
					return false;
				}
				this.insertStringWithoutCheck(offset, "/", attributes);
				this.insertStringIfIsDigitAtFIrst(offset, s, attributes);
			} else {
				this.insertStringIfIsOneDigit(offset, s, attributes);
			}
		} else {
			// If it is greater or equal
			// If it is equal then OR and BETTWEN are allowed
			if ((offset == 10) && s.equals(AdvancedDateDocument.BETWEEN)) {
				this.insertStringWithoutCheck(offset, s, attributes);
			} else {
				if ((offset >= 21) && (this.getText(0, this.getLength()).indexOf(AdvancedDateDocument.BETWEEN) > 0)) {
					return false;
				}
				// int orCharacterNumber = (offset /
				// (this.datePattern.length() + 1));
				if (this.isOROffset(offset)) {
					if (s.equals(AdvancedDateDocument.OR)) {
						if ((this.getLength() > offset) && this.getText(offset, 1).equals(AdvancedDateDocument.OR)) {
							return false;
						}
						this.insertStringWithoutCheck(offset, s, attributes);
					} else {
						// Nothing
					}
				} else {
					int patternOffset = this.getPatternOffset(offset);
					if (this.datePattern.charAt(patternOffset) == '/') {
						if ((this.getLength() > offset) && this.getText(offset, 1).equals("/")) {
							return false;
						}
						this.insertStringWithoutCheck(offset, "/", attributes);
						this.insertStringIfIsDigitAtFIrst(offset, s, attributes);
					} else {
						this.insertStringIfIsOneDigit(offset, s, attributes);
					}
				}
			}
		}

		return true;
	}

	protected boolean insertFirstSymbol(int offset, String s, AttributeSet attributes, String symbol)
			throws BadLocationException {
		if (offset == 1) {
			if (this.checkSymbolIsLessMoreEqual(s, symbol)) {
				if ((this.getLength() > 1) && this.getText(1, 1).equals(AdvancedDateDocument.EQUAL)) {
					return false;
				}
				this.insertStringWithoutCheck(offset, s, attributes);
			} else { // The digits to insert must follow the pattern
				this.insertStringFollowingThePattern(offset, s, attributes);
			}
		} else {
			// Offset is different than 1 and the first character is a
			// symbol.
			// If offset is greater than pattern length plus symbol
			// length then insertion is not executed
			int symbolLength = symbol.length();
			if (offset >= (symbolLength + this.datePattern.length())) {
				return false;
			}
			int patternOffset = offset - symbolLength;
			if (patternOffset < 0) {
				return false;
			}
			if (this.datePattern.charAt(patternOffset) == '/') {
				if ((this.getLength() > offset) && this.getText(offset, 1).equals("/")) {
					return false;
				}
				this.insertStringWithoutCheck(offset, "/", attributes);
				this.insertStringIfIsDigitAtFIrst(offset, s, attributes);
			} else {
				this.insertStringIfIsOneDigit(offset, s, attributes);
			}
		}
		return true;
	}

	/**
	 * Method used to reduce the complexity of {@link #insertString(int, String, AttributeSet)}
	 * @param s
	 * @param symbol
	 * @return
	 */
	protected boolean checkSymbolIsLessMoreEqual(String s, String symbol) {
		return (symbol.equals(AdvancedDateDocument.LESS) || symbol.equals(AdvancedDateDocument.MORE))
				&& s.equals(AdvancedDateDocument.EQUAL);
	}

	/**
	 * Method used to reduce the complexity of {@link #insertString(int, String, AttributeSet)}
	 * @param offset
	 * @param s
	 * @param attributes
	 * @throws BadLocationException
	 */
	protected void insertStringFollowingThePattern(int offset, String s, AttributeSet attributes)
			throws BadLocationException {
		if (Character.isDigit(s.charAt(0))) {
			if (this.getLength() > 1) {
				this.remove(1, 1);
			}
			this.insertStringWithoutCheck(offset, s, attributes);
		} else {
			// Character is not inserted
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #insertString(int, String, AttributeSet)}
	 * @param offset
	 * @param s
	 * @param attributes
	 * @throws BadLocationException
	 */
	protected void insertStringIfIsDigitAtFIrst(int offset, String s, AttributeSet attributes)
			throws BadLocationException {
		if (Character.isDigit(s.charAt(0))) {
			this.insertStringWithoutCheck(offset + 1, s, attributes);
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #insertString(int, String, AttributeSet)}
	 * @param offset
	 * @param s
	 * @param attributes
	 * @throws BadLocationException
	 */
	protected void insertStringIfIsOneDigit(int offset, String s, AttributeSet attributes) throws BadLocationException {
		// One digit
		if (Character.isDigit(s.charAt(0))) {
			if (this.getLength() > offset) {
				this.remove(offset, 1);
			}
			this.insertStringWithoutCheck(offset, s, attributes);
		} else {
			// Character is not inserted
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
			// If starts with a symbol then overwrite it
			if (this.isSymbolFirst()) {
				String symbol = this.getDocumentFirstSymbol();
				if (symbol.length() == 1) {
					super.remove(0, 1);
					this.insertStringWithoutCheck(offset, s, attributes);
				} else {
					if (s.equals(AdvancedDateDocument.LESS) || s.equals(AdvancedDateDocument.MORE)) {
						super.remove(0, 1);
						this.insertStringWithoutCheck(offset, s, attributes);
					} else {
						super.remove(0, 2);
						this.insertStringWithoutCheck(offset, s, attributes);
					}
				}
			} else {
				// If the first character is not a symbol then insert
				this.insertStringWithoutCheck(offset, s, attributes);
			}
		} else {
			// Inserting character is 0, not a symbol. If a symbol exists
			// left it
			if (this.isSymbolFirst()) {
				// Nothing is done
			} else {
				// No symbol exists, then insert the character
				super.insertString(offset, s, attributes);
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
	protected void insertStringCharacterOneByOne(int offset, String s, AttributeSet attributes)
			throws BadLocationException {
		if (s.length() > 1) {
			for (int i = 0; i < s.length(); i++) {
				this.insertString(offset + i, s.substring(i, i + 1), attributes);
			}
		}
	}

	protected boolean isOROffset(int offset) {
		// An OR exists if it is 10,21,32,43
		// This is 10+x*11
		if (offset < 10) {
			return false;
		} else {
			if (offset == 10) {
				return true;
			} else {
				if (((offset - 10) % 11) == 0) {
					return true;
				} else {
					return false;
				}
			}
		}
	}

	protected int getPatternOffset(int offset) {
		if (offset < 10) {
			return offset;
		} else {
			if (offset == 10) {
				return -1;
			} else {
				// If offset > 10, then get the rest
				if ((offset - 10) < 11) {
					return offset - 11;
				}
				return (offset - 10) % 11;
			}
		}
	}

	protected boolean isStartSymbol(String s) {
		if (s.equals(AdvancedDateDocument.LESS) || s.equals(AdvancedDateDocument.MORE)
				|| s.equals(AdvancedDateDocument.NOT) || s.equals(AdvancedDateDocument.LESS_EQUAL) || s
				.equals(AdvancedDateDocument.MORE_EQUAL)
				|| s.equals(AdvancedDateDocument.EQUAL)) {
			return true;
		} else {
			return false;
		}
	}

	protected boolean isORSymbolAllowed(int offset) throws BadLocationException {
		if (this.isSymbolFirst()) {
			return false;
		}
		if ((offset % 10) == 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks if the first character is a condition symbol
	 * @return true if the first character is a condition symbol
	 * @throws BadLocationException
	 */
	protected boolean isSymbolFirst() throws BadLocationException {
		if ((this.getLength() > 0)
				&& (this.getText(0, 1).equals(AdvancedDateDocument.LESS)
						|| this.getText(0, 1).equals(AdvancedDateDocument.MORE) || this.getText(0, 1)
						.equals(AdvancedDateDocument.NOT)
						|| this.getText(0, 1).equals(AdvancedDateDocument.EQUAL))) {
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
			if (this.getText(0, 1).equals(AdvancedDateDocument.LESS)) {
				if (this.getLength() > 1) {
					if (this.getText(1, 1).equals(AdvancedDateDocument.EQUAL)) {
						return AdvancedDateDocument.LESS_EQUAL;
					} else {
						return AdvancedDateDocument.LESS;
					}
				} else {
					return AdvancedDateDocument.LESS;
				}
			} else if (this.getText(0, 1).equals(AdvancedDateDocument.MORE)) {
				if (this.getLength() > 1) {
					if (this.getText(1, 1).equals(AdvancedDateDocument.EQUAL)) {
						return AdvancedDateDocument.MORE_EQUAL;
					} else {
						return AdvancedDateDocument.MORE;
					}
				} else {
					return AdvancedDateDocument.MORE;
				}
			} else if (this.getText(0, 1).equals(AdvancedDateDocument.NOT)) {
				return AdvancedDateDocument.NOT;
			} else if (this.getText(0, 1).equals(AdvancedDateDocument.EQUAL)) {
				return AdvancedDateDocument.EQUAL;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

}
