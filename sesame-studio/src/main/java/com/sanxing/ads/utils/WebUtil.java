package com.sanxing.ads.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.servlet.http.HttpServletResponse;

public class WebUtil {
	public static void sendError(HttpServletResponse res, String message)
			throws UnsupportedEncodingException, IOException {
		if (message == null)
			message = "未知错误，请查看服务器日志";
		res.setCharacterEncoding("utf-8");
		res.sendError(500, message);
	}
}