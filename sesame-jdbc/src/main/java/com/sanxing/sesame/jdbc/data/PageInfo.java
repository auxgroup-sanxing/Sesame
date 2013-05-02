package com.sanxing.sesame.jdbc.data;

import com.sanxing.sesame.jdbc.DataAccessException;

public class PageInfo {
	private int pageSize = 0;

	private int currentPageNo = 1;

	public static void vlidatePageInfo(int pageNo, int pageSize) {
		if (pageNo < 1) {
			throw new DataAccessException("pageNo[" + String.valueOf(pageNo)
					+ "] is invalid");
		}
		if (pageSize < 1)
			throw new DataAccessException("pageSize["
					+ String.valueOf(pageSize) + "] is invalid");
	}

	public int getPageSize() {
		return this.pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getCurrentPageNo() {
		return this.currentPageNo;
	}

	public void setCurrentPageNo(int currentPageNo) {
		this.currentPageNo = currentPageNo;
	}

	public String toString() {
		String pageInfoStr = "PageInfo:pageSize = " + this.pageSize
				+ ", currentPageNo = " + this.currentPageNo;
		return pageInfoStr;
	}
}