package com.sanxing.ads.matcher;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class StringFilter extends AbstractStringMatcher {
	public StringFilter() {
		init();
	}

	public StringFilter(boolean caseSenitive) {
		init();
		setCaseSenitive(caseSenitive);
	}

	public StringFilter(List list, boolean caseSenitive) {
		init();
		addAll(list);
		setCaseSenitive(caseSenitive);
	}

	public StringFilter(List list) {
		init();
		addAll(list);
	}

	public StringFilter(Set set, boolean caseSenitive) {
		init();
		addAll(set);
		setCaseSenitive(caseSenitive);
	}

	public StringFilter(Set set) {
		init();
		addAll(set);
	}

	public StringFilter(String[] patters, boolean caseSenitive) {
		init();
		addAll(patters);
		setCaseSenitive(caseSenitive);
	}

	public StringFilter(String[] patters) {
		init();
		addAll(patters);
	}

	public StringFilter(String patter, boolean caseSenitive) {
		init();
		addPatten(patter);
		setCaseSenitive(caseSenitive);
	}

	public StringFilter(String patter) {
		init();
		addPatten(patter);
	}

	public boolean matcher(String s, boolean caseSenitive) {
		String[] patters = getPattens();
		for (String str : patters) {
			Pattern p;
			if (caseSenitive) {
				p = Pattern.compile(str);
			} else {
				p = Pattern.compile(str, 66);
			}
			if (p.matcher(s).matches())
				return true;
		}
		return false;
	}

	public boolean match(String s) {
		return matcher(s, isCaseSenitive());
	}
}