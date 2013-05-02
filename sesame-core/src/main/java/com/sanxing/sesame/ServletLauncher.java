package com.sanxing.sesame;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sanxing.sesame.core.Platform;

public class ServletLauncher extends HttpServlet {
	private static final long serialVersionUID = 8506882580943054740L;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		super.doGet(req, resp);
	}

	public void init() throws ServletException {
		super.init();
		Platform.startup();
	}
}