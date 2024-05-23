package com.ontimize.util;

import java.util.List;

public class ListUtil {

	private ListUtil() {
		super();
	}

	public static int indexOf(List<?> list, Object toFind, int startIndex) {
		if (startIndex < list.size()) {
			int result = list.subList(startIndex, list.size()).indexOf(toFind);
			return result == -1 ? -1 : result + startIndex;
		}
		return -1;
	}

}
