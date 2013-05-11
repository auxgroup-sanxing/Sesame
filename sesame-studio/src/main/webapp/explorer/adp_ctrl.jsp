<%@page import="java.net.URLDecoder"%>
<%@page import="com.sanxing.studio.*,com.sanxing.studio.utils.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.*, java.util.jar.*"%>
<%@page import="java.lang.reflect.*"%>
<%@page import="java.sql.Timestamp"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="org.slf4j.Logger, org.slf4j.LoggerFactory"%>
<%@page import="org.json.*"%>
<%@page import="org.apache.commons.fileupload.*"%>
<%@page import="org.apache.commons.fileupload.disk.*"%>
<%@page import="org.apache.commons.fileupload.servlet.*"%>
<%@page language="java" contentType="text/xml; charset=utf-8"
	pageEncoding="utf-8"%>
<%!
private final Logger logger = LoggerFactory.getLogger(this.getClass());

private static final long SIZE_BT=1024L;
private static final long SIZE_KB=SIZE_BT*1024L;
private static final long SIZE_MB=SIZE_KB*1024L;
private static final int SACLE=2;

private String getFileSize(long size) {
	String fileSize = "";
	if(size>=0 && size<SIZE_BT) {
		fileSize = size+"B";
    }else if(size >= SIZE_BT && size<SIZE_KB) {
    	fileSize =  size/SIZE_BT+"KB";
    }else if(size >= SIZE_KB && size<SIZE_MB) {
    	fileSize = size/SIZE_KB+"MB";
    }
	return fileSize;
}

//读取已经上传的jar文件列表
@SuppressWarnings("unchecked")
public String loadJarFileList(HttpServletRequest request, HttpServletResponse response) throws Exception {
	JSONArray items = new JSONArray();
	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	try {
		File libPath = Application.getSystemLib();
		if (libPath.exists() && libPath.isDirectory()) {
			File[] jarFiles = libPath.listFiles();
			if (jarFiles != null && jarFiles.length > 0)
				for (File jarFile : jarFiles) {
					if (jarFile.isFile() && jarFile.getName().endsWith(".jar")) {
						JSONObject jso = new JSONObject();
						String fullName = jarFile.getName();
						jso.put("displayName", (fullName.length() > 14) ? fullName.substring(0, 9) + "..." : fullName);
						jso.put("fullName", fullName);
						jso.put("size", getFileSize(jarFile.length()));
						jso.put("lastModify", formatter.format( new Date(jarFile.lastModified())));
						items.put(jso);
					}
				}
		}
		
		JSONObject result = new JSONObject();
		result.put("items", items);
		result.put("totalCount", items.length());
		response.setContentType("text/json; charset=utf-8");
		return result.toString();
	} finally {}
}

// 上传文件
@SuppressWarnings("unchecked")
public String uploadJarFiles(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String contentType = request.getContentType();
	if (contentType.indexOf("multipart/form-data;") > -1) {
		JSONArray jsoArray = new JSONArray();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		try {
			DiskFileItemFactory factory = new DiskFileItemFactory();
			factory.setSizeThreshold(4096);
			ServletFileUpload upload = new ServletFileUpload(factory);
			upload.setHeaderEncoding("UTF-8");
			Map<String, Object> params = new HashMap<String, Object>();
			
			List<?> items = upload.parseRequest(request);
			for (Iterator<?> iter = items.iterator(); iter.hasNext();) {
				FileItem item = (FileItem) iter.next();
				if (item.isFormField()) {
					String value = item.getString();
					try {
						if (value != null)
							value = new String(value.getBytes("utf-8"), "utf-8");
					} catch (UnsupportedEncodingException e) {}
					params.put(item.getFieldName(), value);
				} else {
					params.put(item.getFieldName(), item);
				}
			}
			
			File lib = Application.getSystemLib();
			
			FileItem fileItem = (FileItem) params.get("file");
			if (fileItem != null) {
				String fileName = fileItem.getName();
				if (fileName != null) {
					JSONObject jso = new JSONObject();
					
					String size = getFileSize(fileItem.get().length);
					Timestamp now = new Timestamp((new java.util.Date()).getTime());
					String  uploadTime=   formatter.format(now).toString().trim();
					
					jso.put("displayName", (fileName.length() > 14) ? fileName.substring(0, 9) + "..." : fileName);
					jso.put("fullName", fileName);
					jso.put("size", size);
					jso.put("lastModify", uploadTime);
					jsoArray.put(jso);
					
					fileItem.write(new File(lib, fileName));
				}
			}
					
			response.setContentType("text/html;charset=utf-8");
			JSONObject result = new JSONObject();
			result.put("success", true);
			result.put("items", jsoArray);
			result.put("totalCount", jsoArray.length());
			return result.toString();
		}catch(Exception e) {
			response.setContentType("text/html;charset=utf-8");
			JSONObject result = new JSONObject();
			result.put("success", false);
			return result.toString();
		}finally{}
	} else {
		throw new Exception("[" + request.getMethod() + "]没有指定 operation 参数时，只接受文件上传");
	}
}

// 删除文件
public String deletJarFiles(HttpServletRequest request, HttpServletResponse response) throws Exception {
	String dataStr = request.getParameter("data");
	
	try {
		File lib = Application.getSystemLib();
		
		JSONArray data = new JSONArray(dataStr);
		for (int i=0; i< data.length(); i++) {
			JSONObject jso = data.getJSONObject(i);
			String fileName = jso.optString("fullName");
			File jarFile = new File(lib, fileName);
			if (jarFile.exists()) {
				jarFile.delete();
			}
		}
		
		response.setContentType("text/plain; charset=utf-8");
		return "true";
	} finally {}
}
%>

<%
String operation = request.getParameter("operation");
WebServletResponse responseWrapper = new WebServletResponse(response);

if (operation == null)
	operation = "uploadJarFiles";

try {
	Class<?> clazz = this.getClass();
	Method method = clazz.getMethod(operation, new Class[]{HttpServletRequest.class, HttpServletResponse.class});
	String result = (String)method.invoke(this, new Object[]{new WebServletRequest(request), responseWrapper});
	out.clear();
	out.println(result);
}
catch (NoSuchMethodException e) {
	responseWrapper.sendError("["+request.getMethod()+"]找不到相应的方法来处理指定的 operation: "+operation);
}
catch (InvocationTargetException e) {
	Throwable t = e.getTargetException();
	if (!(t instanceof FileNotFoundException))
		logger.error(t.getMessage(), t);
	responseWrapper.sendError(t.getMessage());
}
catch (Exception e) {
	logger.error(e.getMessage(), e);
	responseWrapper.sendError(e.getMessage());
}
%>