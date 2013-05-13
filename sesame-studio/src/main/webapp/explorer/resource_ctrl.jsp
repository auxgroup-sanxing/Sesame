<%@page import="com.sanxing.sesame.pwd.PasswordTool"%>
<%@page import="com.sanxing.studio.IllegalNameException"%>
<%@page import="java.net.URL"%>
<%@page import="java.io.*"%>
<%@page import="java.lang.reflect.*"%>
<%@page import="java.util.*,java.util.jar.*,java.util.regex.*"%>
<%@page import="java.util.zip.*"%>
<%@page import="javax.wsdl.*"%>
<%@page import="javax.wsdl.factory.WSDLFactory"%>
<%@page import="javax.wsdl.xml.*"%>
<%@page import="javax.wsdl.extensions.*"%>
<%@page import="javax.wsdl.extensions.schema.*"%>
<%@page import="javax.wsdl.extensions.soap.*"%>
<%@page import="javax.xml.namespace.QName"%>
<%@page import="javax.xml.parsers.*"%>

<%@page import="com.ibm.wsdl.extensions.schema.*"%>
<%@page import="com.ibm.wsdl.extensions.soap.*"%>

<%@page import="com.sanxing.studio.*,com.sanxing.studio.utils.*"%>
<%@page import="com.sanxing.studio.emu.*"%>
<%@page import="com.sanxing.studio.team.*"%>

<%@page import="org.apache.commons.fileupload.*"%>
<%@page import="org.apache.commons.fileupload.disk.*"%>
<%@page import="org.apache.commons.fileupload.servlet.*"%>
<%@page import="org.slf4j.Logger, org.slf4j.LoggerFactory"%>
<%@page
	import="org.jdom.*,org.jdom.input.*,org.jdom.output.*,org.jdom.xpath.*"%>
<%@page import="org.json.*"%>
<%@page import="com.sanxing.adp.eclipse.ADPServiceProjectBuilder"%>
<%@page import="com.sanxing.sesame.sharelib.ShareLibManager"%>

<%@page language="java" contentType="text/json; charset=utf-8"
	pageEncoding="utf-8"%>

<%!private static final Namespace XMLNS_ART = Namespace.getNamespace("sn", Namespaces.SESAME);

	private static final String REV_WORKING	= "working";
	private static final String REV_HEAD	= "head";
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private File getFile(String filePath) {
		File file = Application.getSystemFile(filePath);
		return file;
	}

	private void deleteFiles(File folder) throws Exception {
		File[] files = folder.listFiles();
		for (int i = files.length - 1; i >= 0; i--) {
			File entry = files[i];
			if (entry.isDirectory()) {
				deleteFiles(entry);
			}
			if (!entry.delete())
				throw new Exception("删除 \"" + entry + "\" 失败");
		}
	}
	
	private String getRelativePath(String path) {
		int n = path.indexOf("/");
		return n > 0 ? path.substring(0, n) : path;
	}
	
	//从版本库取出项目
	public String checkout(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String url = request.getParameter("url");
		String username = request.getParameter("username");
		String password = request.getParameter("passwd");
		String path = url.substring(url.lastIndexOf('/')+1);
		long revision = Long.parseLong(request.getParameter("revision"));
		File wcPath = Application.getSystemFile(path);
		
		if (username == null) {
			Element prefsEl = Configuration.getSCMPrefs();
			Element repoEl = (Element)XPath.selectSingleNode(prefsEl, "repository[@url='"+url+"']");
			if (repoEl != null) {
				username = repoEl.getAttributeValue("username");
				password = PasswordTool.decrypt(repoEl.getAttributeValue("password"));
			}
		}
		
		ThreeWaySynchronizer synchronizer = SCM.getSynchronizer(wcPath, url, username, password);
		revision = synchronizer.checkout("", wcPath, revision);
		
		JSONObject result = new JSONObject();
		
		result.put("name", wcPath.getName());
		result.put("iconCls", "x-icon-folder");
		result.put("allowRemove", true);
		result.put("revision", revision);
		result.put("status", " ");
		
		return result.toString();
	}

	public String createFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String path = request.getParameter("path");
		String name = request.getParameter("file-name");
		File file = Application.getSystemFile(path + "/" + name);
		if (file.exists()) {
			throw new Exception("文件已存在，请重新指定名称");
		}
		if (!file.createNewFile()) {
			throw new Exception("创建文件失败");
		}
		
		ThreeWaySynchronizer synchronizer = SCM.getSynchronizer(this.getRelativePath(path));
		JSONObject result = new JSONObject();
		if (synchronizer != null) {
			Map prop = synchronizer.status(file);
			if (prop != null) {
				result.put("revision", prop.get("revision"));
				result.put("author", prop.get("author"));
				result.put("status", prop.get("status"));
			}
			else {
				result.put("status", "?");
			}
		}
		result.put("success", true);
		return result.toString();
	}

	public String createFolder(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String path = request.getParameter("path");
		String name = request.getParameter("file-name");
		File folder = Application.getSystemFile(path + "/" + name);
		if (folder.exists()) {
			throw new Exception("目录已存在，请重新指定");
		}
		if (!folder.mkdirs()) {
			throw new Exception("创建目录失败");
		}
		
		ThreeWaySynchronizer synchronizer = SCM.getSynchronizer(this.getRelativePath(path));
		JSONObject result = new JSONObject();
		if (synchronizer != null) {
			Map prop = synchronizer.status(folder);
			if (prop != null) {
				result.put("revision", prop.get("revision"));
				result.put("author", prop.get("author"));
				result.put("status", prop.get("status"));
			}
			else {
				result.put("status", "?");
			}
		}
		result.put("success", true);
		return result.toString();
	}
	
	//资源浏览器获取资源文件
	public String getResourceFiles(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String node = request.getParameter("node");
		String path = request.getParameter("path");
		String regex = request.getParameter("filter");
		
		JSONArray result = new JSONArray();
		if (path == null) {
			return result.toString();
		}
		
		ThreeWaySynchronizer synchronizer = null;
		File entry = path.equals("/") ? Application.getSystemRoot() : Application.getSystemFile(path.substring(2));
		if (entry.exists() && entry.isDirectory()) {
			Pattern pattern = regex != null && regex.length() > 0 ? Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
					: null;

			SAXBuilder builder = new SAXBuilder();
			File[] files = entry.listFiles();
			for (int j = 0; j < files.length; j++) {
				File file = files[j];
				if (file.isHidden()) continue;
				if (file.getName().startsWith(".")) continue;
				
				if (file.isDirectory()) {
					JSONObject item = new JSONObject();
					item.put("iconCls", "x-icon-folder");
					item.put("text", file.getName());
					item.put("name", file.getName());
					
					if (path.equals("/")) {
						synchronizer = SCM.getSynchronizer(file.getName());
						item.put("iconCls", "x-icon-folder");
						item.put("allowRemove", true);
						
						if (synchronizer != null) {
							Map version = synchronizer.status(file);
							//System.out.println(version.toString());
							item.put("revision", version.get("revision"));
							item.put("author", version.get("author"));
							item.put("status", version.get("status"));
							item.put("reposUrl", version.get("url"));
						}
					}
					else {
						item.put("allowRemove", file.canWrite());
						
						String directory = this.getRelativePath(path.substring(2));
						synchronizer = SCM.getSynchronizer(directory);
						Map properties;
						if (synchronizer != null && (properties=synchronizer.status(file))!=null) {
							item.put("revision", properties.get("committed-rev"));
							item.put("author", properties.get("author"));
							item.put("status", properties.get("status"));
						}
					}
					
					result.put(item);
				}
				else if (file.isFile() && (pattern == null || pattern.matcher(file.getName()).matches())) {
					JSONObject item = new JSONObject();
					item.put("text", file.getName());
					String filename = file.getName();
					int pos = filename.lastIndexOf(".");
					item.put("name", file.getName());
					item.put("iconCls", "x-icon-" + (pos >= 0 ? filename.substring(pos + 1) : ""));
					item.put("leaf", true);
					item.put("allowRemove", file.canWrite());
					if (!path.equals("/")) {
						synchronizer = SCM.getSynchronizer(this.getRelativePath(path.substring(2)));
						Map properties;
						if (synchronizer != null && (properties=synchronizer.status(file))!=null) {
							item.put("revision", properties.get("committed-rev"));
							item.put("author", properties.get("author"));
							item.put("status", properties.get("status"));
						}
					}
					result.put(item);
				}
			}
		}
		return result.toString();
	}
	
	public String info(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String path = request.getParameter("path");
		String resource = request.getParameter("resource");
		
		String result = "<b>位置</b>: "+path;
		File location = Application.getSystemFile(path);
		ThreeWaySynchronizer synchronizer = SCM.getSynchronizer(this.getRelativePath(path));
		Map info;
		if (synchronizer!=null && (info=synchronizer.status(location)) != null) {
	        result += "<br/><br/><b>版本库地址</b>: " + info.get("url");
	        result += "<br/><b>版本号</b>: "+ info.get("committed-rev");
	        result += "<br/><b>提交者</b>: "+ info.get("author");
		}
		
		return "<p style='background-color: white; padding:5px;'>"+result+"</p>";
	}

	public String remove(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String files = request.getParameter("files");
		if (files != null) {
			//project explorer remove request
			JSONArray jsonFiles = new JSONArray(files);
			JSONArray effected = new JSONArray();
			JSONObject result = new JSONObject();
			try {
				for (int i = 0, len = jsonFiles.length(); i < len; i++) {
					String filepath = jsonFiles.getString(i);
					String project = this.getRelativePath(filepath.substring(2));
					ThreeWaySynchronizer synchronizer = SCM.getSynchronizer(project);
					File file = Application.getSystemFile(filepath.substring(2));
					//从版本库中删除
					if (synchronizer!=null && SCM.isVersioned(file) && SCM.isVersioned(file.getParentFile())) {
						synchronizer.delete(file);
					}
					//删除本地物理文件
					if (file.isDirectory()) {
						deleteFiles(file);
					}
					file.delete();
					if (file.exists()) {
						throw new Exception("删除失败: \"" + file + "\"");
					}
					effected.put(filepath);
				}
				result.put("success", true);
			}
			catch (Exception e) {
				result.put("success", false);
				result.put("message", e.getMessage());
				result.put("effected", effected);
			}
			return result.toString();
		}

		String filePath = request.getParameter("file");
		File file = this.getFile(filePath);
		String project = this.getRelativePath(filePath);
		//从版本库中删除
		ThreeWaySynchronizer synchronizer = SCM.getSynchronizer(project);
		if (synchronizer!=null && synchronizer.isVersioned(file)) {
			synchronizer.delete(file);
		}
		//删除本地物理文件
		if (file.isDirectory()) {
			deleteFiles(file);
		}
		file.delete();
		if (file.exists()) {
			throw new Exception("删除失败: \"" + file + "\" ");
		}
		return "true";
	}

	public String rename(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String f = request.getParameter("file");
		String path = request.getParameter("path");
		String newName = request.getParameter("newName");
		
		String resource;
		File newFile, file;
		if (path != null) {
			file = Application.getSystemFile(path.substring(2));
			newFile = new File(file.getParent(), newName);
			resource = this.getRelativePath(path.substring(2));
		}
		else {
			file = this.getFile(f);
			String fileName = file.getName();
			int idx = fileName.lastIndexOf(".");
			String ext = idx>-1 ? fileName.substring(idx) : "";
			newFile = new File(file.getParent(), newName + ext);
			resource = this.getRelativePath(f);
		}

		ThreeWaySynchronizer synchronizer = SCM.getSynchronizer(resource);
		if (synchronizer != null && synchronizer.isVersioned(file)) {
			synchronizer.move(file, newFile);
			return "true";
		}
		else {
			if (file.renameTo(newFile)) {
				if (file.isFile()) {
					//尝试更改对象的名称
					
				}
				return "true";
			}
			else {
				throw new Exception("重命名文件失败，文件可能已被删除");
			}
		}
	}

	public String post(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String contentType = request.getContentType();
		if (contentType.indexOf("multipart/form-data;") > -1) {
			response.setContentType("text/html; charset=utf-8");
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
								value = new String(value.getBytes("iso8859-1"), "utf-8");
						}
						catch (UnsupportedEncodingException e) {
						}
						params.put(item.getFieldName(), value);
					}
					else {
						params.put(item.getFieldName(), item);
					}
				}

				//处理项目管理器中简单的文件上传
				String opera = (String) params.get("operation");
				if (opera == null) {
				}
				else if (opera.equals("uploadFile")) {
					String path = (String) params.get("path");
					FileItem uploadFile = (FileItem) params.get("file-name");
					String filename = uploadFile.getName();
					int index = filename.lastIndexOf(File.separator);
					if (index > -1)
						filename = filename.substring(index + 1);
					File file = new File(Application.getSystemRoot(), path.substring(1) + File.separator + filename);
					uploadFile.write(file);
					return "true";
				}
				return "{success:false, error:\"未知操作\"}";
			}
			catch (Throwable e) {
				return "{success:false, error:\"" + e.getMessage() + "\"}";
			}
		}
		else {
			throw new Exception("[" + request.getMethod() + "]没有指定 operation 参数时，只接受文件上传");
		}
	}
%>

<%
	String operation = request.getParameter("operation");
	WebServletResponse responseWrapper = new WebServletResponse(response);

	if (operation == null) {
		operation = request.getMethod().toLowerCase();
	}
	
	try {
		Class<?> clazz = this.getClass();
		Method method = clazz.getMethod(operation, new Class[] { HttpServletRequest.class,
				HttpServletResponse.class });
		String result = (String) method.invoke(this, new Object[] { new WebServletRequest(request),
				responseWrapper });
		out.clear();
		out.println(result);
	}
	catch (NoSuchMethodException e) {
		responseWrapper.sendError("["+request.getMethod()+"]找不到相应的方法来处理指定的 operation: "+operation);
	}
	catch (InvocationTargetException e) {
		Throwable t = e.getTargetException();
		responseWrapper.sendError(t.getMessage());
	}
	catch (Exception e) {
		responseWrapper.sendError(e.getMessage());
	}
%>