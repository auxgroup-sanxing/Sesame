package com.sanxing.ads.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class WebServletResponse implements HttpServletResponse {
	private HttpServletResponse response = null;

	public WebServletResponse(HttpServletResponse response) {
		this.response = response;
	}

	public void addCookie(Cookie arg0) {
		this.response.addCookie(arg0);
	}

	public void addDateHeader(String arg0, long arg1) {
		this.response.addDateHeader(arg0, arg1);
	}

	public void addHeader(String arg0, String arg1) {
		this.response.addHeader(arg0, arg1);
	}

	public void addIntHeader(String arg0, int arg1) {
		this.response.addIntHeader(arg0, arg1);
	}

	public boolean containsHeader(String arg0) {
		return this.response.containsHeader(arg0);
	}

	/** @deprecated */
	public String encodeRedirectUrl(String arg0) {
		return this.response.encodeRedirectUrl(arg0);
	}

	public String encodeRedirectURL(String arg0) {
		return this.response.encodeRedirectURL(arg0);
	}

	/** @deprecated */
	public String encodeUrl(String arg0) {
		return this.response.encodeUrl(arg0);
	}

	public String encodeURL(String arg0) {
		return this.response.encodeURL(arg0);
	}

	public void flushBuffer() throws IOException {
		this.response.flushBuffer();
	}

	public int getBufferSize() {
		return this.response.getBufferSize();
	}

	public String getCharacterEncoding() {
		return this.response.getCharacterEncoding();
	}

	public String getContentType() {
		return this.response.getContentType();
	}

	public Locale getLocale() {
		return this.response.getLocale();
	}

	public ServletOutputStream getOutputStream() throws IOException {
		return this.response.getOutputStream();
	}

	public PrintWriter getWriter() throws IOException {
		return this.response.getWriter();
	}

	public boolean isCommitted() {
		return this.response.isCommitted();
	}

	public void reset() {
		this.response.reset();
	}

	public void resetBuffer() {
		this.response.resetBuffer();
	}

	public void sendError(int code, String message) throws IOException {
		if (message == null)
			message = "未知错误，请查看服务器日志";
		this.response.setCharacterEncoding("utf-8");
		this.response.sendError(code, message);
	}

	public void sendError(int arg0) throws IOException {
		this.response.sendError(arg0);
	}

	public void sendError(String message) throws IOException {
		if (message == null)
			message = "未知错误，请查看服务器日志";
		this.response.setCharacterEncoding("utf-8");
		this.response.sendError(500, message);
	}

	public void sendRedirect(String arg0) throws IOException {
		this.response.sendRedirect(arg0);
	}

	public void setBufferSize(int arg0) {
		this.response.setBufferSize(arg0);
	}

	public void setCharacterEncoding(String arg0) {
		this.response.setCharacterEncoding(arg0);
	}

	public void setContentLength(int arg0) {
		this.response.setContentLength(arg0);
	}

	public void setContentType(String arg0) {
		this.response.setContentType(arg0);
	}

	public void setDateHeader(String arg0, long arg1) {
		this.response.setDateHeader(arg0, arg1);
	}

	public void setHeader(String arg0, String arg1) {
		this.response.setHeader(arg0, arg1);
	}

	public void setIntHeader(String arg0, int arg1) {
		this.response.setIntHeader(arg0, arg1);
	}

	public void setLocale(Locale arg0) {
		this.response.setLocale(arg0);
	}

	/** @deprecated */
	public void setStatus(int arg0, String arg1) {
		this.response.setStatus(arg0, arg1);
	}

	public void setStatus(int arg0) {
		this.response.setStatus(arg0);
	}
}