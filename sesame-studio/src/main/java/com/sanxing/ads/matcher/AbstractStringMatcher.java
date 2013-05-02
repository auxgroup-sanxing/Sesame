package com.sanxing.ads.matcher;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class AbstractStringMatcher implements StringMatcher {
	private Set pattens;
	private boolean isUpperCase;

	public AbstractStringMatcher() {
		this.isUpperCase = true;
	}

	public void init() {
		this.pattens = new HashSet();
	}

	public void addAll(List patters) {
		for (int i = 0; i < patters.size(); ++i) {
			this.pattens.add(patters.get(i));
		}
	}

	public void addPatten(String patter) {
		this.pattens.add(patter);
	}

	public void addAll(Set patters) {
		Iterator it = patters.iterator();
		while (it.hasNext())
			this.pattens.add(it.next());
	}

	public void addAll(String[] patters) {
		for (int i = 0; i < patters.length; ++i)
			this.pattens.add(patters[i]);
	}

	public boolean removePatten(String patter) {
		if (this.pattens.contains(patter)) {
			this.pattens.remove(patter);
			return true;
		}
		return false;
	}

	public void reset() {
		init();
	}

	public int getPattenCount() {
		return this.pattens.size();
	}

	public String getPatten(int index) {
		Object[] obj = this.pattens.toArray();
		return obj[index].toString();
	}

	public void clearPattens() {
		init();
	}

	public String[] getPattens() {
		Object[] temp = this.pattens.toArray();
		String[] tempPatters = new String[temp.length];
		for (int i = 0; i < temp.length; ++i) {
			tempPatters[i] = temp[i].toString();
		}
		return tempPatters;
	}

	public void setCaseSenitive(boolean b) {
		this.isUpperCase = b;
	}

	public boolean isCaseSenitive() {
		return this.isUpperCase;
	}

	public abstract boolean match(String paramString);

	public abstract boolean matcher(String paramString, boolean paramBoolean);
}