package com.sanxing.studio.console;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONException;
import org.json.JSONObject;

import com.sanxing.sesame.core.Console;

public class ConsoleServlet extends HttpServlet {
	private static final long serialVersionUID = 8966394714998937124L;
	private static Logger LOG = LoggerFactory.getLogger(ConsoleServlet.class);
	private VirtualOutput output;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/json;charset=utf-8");
		PrintWriter out = response.getWriter();

		JSONObject result = new JSONObject();
		try {
			String content = this.output.poll(10, TimeUnit.SECONDS);

			result.put("type", "event");
			result.put("name", "message");
			result.put("data", content);
		} catch (JSONException e) {
			LOG.error(e.getMessage(), e);
		} catch (InterruptedException e) {
			LOG.error(e.getMessage(), e);
		}

		out.print(result);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

	public void init() throws ServletException {
		super.init();

		Console.addOutput(this.output = new VirtualOutput(5000));
	}

	public void destroy() {
		Console.removeOutput(this.output);
		try {
			this.output.close();
		} catch (IOException e) {
			LOG.debug(e.getMessage(), e);
		}
		super.destroy();
	}
}