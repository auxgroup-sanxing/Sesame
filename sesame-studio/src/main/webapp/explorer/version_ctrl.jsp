<%@page import="com.sanxing.sesame.pwd.PasswordTool"%>
<%@page import="java.io.*"%>
<%@page import="java.lang.reflect.*"%>
<%@page import="java.util.*,java.util.jar.*,java.util.regex.*"%>
<%@page import="java.util.zip.*"%>

<%@page import="com.sanxing.studio.*,com.sanxing.studio.utils.*"%>
<%@page import="com.sanxing.studio.team.*"%>
<%@page import="com.sanxing.studio.team.svn.*"%>

<%@page import="org.slf4j.Logger, org.slf4j.LoggerFactory"%>
<%@page import="org.jdom.*"%>
<%@page import="org.jdom.xpath.*"%>
<%@page import="org.json.*"%>

<%@page language="java" contentType="text/json; charset=utf-8" pageEncoding="utf-8"%>

<%!//private static Namespace XMLNS_ART = Namespace.getNamespace("sn", Namespaces.STATENET);
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private String getProject(String path) {
		int n = path.indexOf("/");
		return n > 0 ? path.substring(0, n) : path;
	}
	
	private void saveComment(String comment) throws IOException {
		File file = new File(Application.getRealPath("temp/comments"));
		Properties properties = new Properties();
		if (file.exists()) {
			InputStream input = new FileInputStream(file);
			try {
				properties.load(input);
			}
			finally {
				input.close();
			}
		}
		for (int i=8; i>=0; i--) {
			String value = properties.getProperty(String.valueOf(i));
			if (value != null) {
				properties.setProperty(String.valueOf(i+1), value);
			}
		}
		properties.setProperty("0", comment);
		OutputStream output = new FileOutputStream(file);
		try {
			properties.store(output, "sesame-studio");
		}
		finally {
			output.close();
		}
	}
	
	//--------------------------------------------------------------------------------------------------------------------
	public String cleanup(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String path = request.getParameter("path");
		String region = request.getHeader("region");
		File wcPath = null;
		File wcRoot = null;
		if (region == null || region.equals("workspace")) {
			String project = getProject(path);
			wcRoot = Application.getWorkspaceFile(project);
			wcPath = Application.getWorkspaceFile(path);
		}
		else if (region.equals("warehouse")) {
			wcRoot = Application.getWarehouseRoot();
			wcPath = path!=null ? new File(wcRoot, path) : wcRoot;
		}

		ThreeWaySynchronizer synchronizer = SCM.getSynchronizer(wcRoot);
		if (synchronizer != null) {
			synchronizer.cleanup(wcPath);
		}
		return "true";
	}

	public String commit(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String path = request.getParameter("path");
		String comment = request.getParameter("comment");
		String selected = request.getParameter("selected");
		String region = request.getHeader("region");
		File wcPath = null;
		File wcRoot = null;
		if (region == null || region.equals("workspace")) {
			String project = getProject(path);
			wcRoot = Application.getWorkspaceFile(project);
			wcPath = Application.getWorkspaceFile(path);
			if (comment!=null || comment.length()>0) {
				saveComment(comment); 
			}
		}
		else if (region.equals("warehouse")) {
			wcRoot = Application.getWarehouseRoot();
			wcPath = path!=null ? new File(wcRoot, path) : wcRoot;
		}
		
		JSONArray array = new JSONArray(selected);
		ThreeWaySynchronizer synchronizer = SCM.getSynchronizer(wcRoot);
		if (SCM.isVersioned(wcPath)) {
			synchronizer.cleanup(wcPath);
		}
		List changelist = new LinkedList();
		List newlist = new ArrayList(100);
		for (int i=0,len=array.length(); i<len; i++) {
			JSONObject item = array.getJSONObject(i);
			File file = new File(wcRoot, item.getString("resource"));
			changelist.add(file);
			if (item.getString("status").equals("?")) {
				newlist.add(file);
			}
		}
		synchronizer.add((File[])newlist.toArray(new File[newlist.size()]), false);
		if (comment==null || comment.length()==0) {
			comment = "Commited by studio"; 
		}
		long revision = synchronizer.commit((File[])changelist.toArray(new File[changelist.size()]), comment);
		Map version = SCM.getVersionInfo(wcPath);
		JSONObject result = new JSONObject();
		result.put("head-rev", revision);
		result.put("success", true);
		if (version != null) {
			result.put("revision", version.get("committed-rev"));
			result.put("author", version.get("author"));
			result.put("url", version.get("url"));
		}
		return result.toString();
	}

	public String getChangeList(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String path = request.getParameter("path");
		String region = request.getHeader("region");
		File wcPath = null;
		File wcRoot = null;
		if (region == null || region.equals("workspace")) {
			String project = getProject(path);
			wcRoot = Application.getWorkspaceFile(project);
			wcPath = Application.getWorkspaceFile(path);
		}
		else if (region.equals("warehouse")) {
			wcRoot = Application.getWarehouseRoot();
			wcPath = path!=null ? new File(wcRoot, path) : wcRoot;
		}
		
		JSONArray items = new JSONArray();
		ThreeWaySynchronizer synchronizer = SCM.getSynchronizer(wcRoot);
		if (synchronizer != null) {
			int beginIndex = wcRoot.toURI().toString().length();
			Collection<?> list = synchronizer.getChangeList(wcPath);
			for (Iterator iter=list.iterator(); iter.hasNext(); ){
				Map map = (Map)iter.next();
				File filepath = (File)map.get("path");
				String resource = filepath.toURI().toString();
				if (resource.length() < beginIndex) {
					continue;
				}
				
				JSONObject item = new JSONObject();
				item.put("resource", resource.substring(beginIndex));
				item.put("status", map.get("status"));
				int lastIndex = filepath.getName().lastIndexOf('.');
				String ext = (lastIndex>-1 ? filepath.getName().substring(lastIndex+1) : "");
				item.put("iconCls", "x-icon-"+(filepath.isDirectory() ? "folder" : ext));
				
				items.put(item);
			}
		}
		JSONObject result = new JSONObject();
		result.put("items", items);
		return result.toString();
	}
	
	public String getComments(HttpServletRequest request, HttpServletResponse response) throws Exception {
		JSONArray items = new JSONArray();
		File file = new File(Application.getRealPath("temp/comments"));
		Properties properties = new Properties();
		if (file.exists()) {
			InputStream input = new FileInputStream(file);
			try {
				properties.load(input);
			}
			finally {
				input.close();
			}
		}
		for (int i=0; i<10; i++) {
			String value = properties.getProperty(String.valueOf(i));
			if (value != null) {
				JSONObject item = new JSONObject();
				item.put("comment", value);
				items.put(item);
			}
			else {
				break;
			}
		}
		
		JSONObject result = new JSONObject();
		result.put("items", items);
		return result.toString();
	}
	
	public String revert(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String path = request.getParameter("path");
		String region = request.getHeader("region");
		File wcPath = null;
		File wcRoot = null;
		if (region == null || region.equals("workspace")) {
			String project = getProject(path);
			wcRoot = Application.getWorkspaceFile(project);
			wcPath = Application.getWorkspaceFile(path);
		}
		else if (region.equals("warehouse")) {
			wcRoot = Application.getWarehouseRoot();
			wcPath = path!=null ? new File(wcRoot, path) : wcRoot;
		}
		
		ThreeWaySynchronizer synchronizer = SCM.getSynchronizer(wcRoot);
		synchronizer.cleanup(wcPath);
		synchronizer.revert(wcPath);
		return "还原成功";
	}

	public String resolve(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String path = request.getParameter("path");
		String choice = request.getParameter("choice");
		String region = request.getHeader("region");
		File wcPath = null;
		File wcRoot = null;
		if (region == null || region.equals("workspace")) {
			String project = getProject(path);
			wcRoot = Application.getWorkspaceFile(project);
			wcPath = Application.getWorkspaceFile(path);
		}
		else if (region.equals("warehouse")) {
			wcRoot = Application.getWarehouseRoot();
			wcPath = path!=null ? new File(wcRoot, path) : wcRoot;
		}
		
		ThreeWaySynchronizer synchronizer = SCM.getSynchronizer(wcRoot);
		synchronizer.resolve(wcPath, choice);
		JSONObject result = new JSONObject();
		result.put("success", true);
		return result.toString();
	}

	public String update(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String path = request.getParameter("path");
		String region = request.getHeader("region");
		File wcPath = null;
		File wcRoot = null;
		if (region == null || region.equals("workspace")) {
			String project = getProject(path);
			wcRoot = Application.getWorkspaceFile(project);
			wcPath = Application.getWorkspaceFile(path);
		}
		else if (region.equals("warehouse")) {
			wcRoot = Application.getWarehouseRoot();
			wcPath = path!=null ? new File(wcRoot, path) : wcRoot;
		}
		
		ThreeWaySynchronizer synchronizer = SCM.getSynchronizer(wcRoot);
		if (synchronizer == null) {
			throw new RuntimeException("Can't get synchronizer");
		}
		synchronizer.cleanup(wcPath);
		long revision = synchronizer.update(wcPath, 0);
		Map version = synchronizer.status(wcPath);
		//System.out.println(version.toString());
		
		JSONObject result = new JSONObject();
		result.put("message", "更新成功, 主版本号: "+revision);
		result.put("head-rev", version.get("revision"));
		result.put("revision", version.get("committed-rev"));
		result.put("author", version.get("author"));
		result.put("status", version.get("status"));
		
		return result.toString();
	}

	public String put(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String url = request.getParameter("url");
		String username = request.getParameter("username");
		String password = request.getParameter("passwd");
		String path = request.getParameter("path");
		
		String region = request.getHeader("region");
		
		if (username == null) {
			Element prefsEl = Configuration.getSCMPrefs();
			Element repoEl = (Element)XPath.selectSingleNode(prefsEl, "repository[@url='"+url+"']");
			if (repoEl != null) {
				username = repoEl.getAttributeValue("username");
				password = PasswordTool.decrypt(repoEl.getAttributeValue("password"));
			}
		}
		
		JSONObject result = new JSONObject();
		File location = null;
		if ("warehouse".equals(region)) {
			location = Application.getWarehouseRoot();
			path = "warehouse";
		}
		else {
			location = Application.getWorkspaceFile(path);
		}
		ThreeWaySynchronizer synchronizer = null;
		try {
			synchronizer = SCM.getSynchronizer(location, url, username, password);
			long revision = synchronizer.put(location, path, "initial version");
			result.put("revision", revision);
			
			Element prefsEl = Configuration.getSCMPrefs();
			
			Element repoEl = (Element)XPath.selectSingleNode(prefsEl, "repository[@url='"+url+"']");
			if (repoEl == null) {
				repoEl = new Element("repository");
				repoEl.setAttribute("url", url);
				prefsEl.addContent(repoEl);

				Configuration.setSCMPrefs(prefsEl);
				Configuration.writePrefsFile();
			}
		}
		finally {
			if (synchronizer != null)
				synchronizer.dispose();
		}
		return result.toString();
	}

	public String disconnect(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String path = request.getParameter("path");
		String region = request.getHeader("region");
		File wcPath = null;
		File wcRoot = null;
		if (region == null || region.equals("workspace")) {
			String project = getProject(path);
			wcRoot = Application.getWorkspaceFile(project);
			wcPath = Application.getWorkspaceFile(path);
		}
		else if (region.equals("warehouse")) {
			wcRoot = Application.getWarehouseRoot();
			wcPath = path!=null ? new File(wcRoot, path) : wcRoot;
		}
		
		if (wcRoot != null && wcPath != null) {
			ThreeWaySynchronizer synchronizer = SCM.getSynchronizer(wcRoot);
			synchronizer.disconnect(wcPath);
			if (wcPath.equals(wcRoot)) {
				SCM.removeSynchronizer(wcRoot);
			}
		}
		return "true";
	}
	
	public String removeRepository(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String url = request.getParameter("url");

		Element prefsEl = Configuration.getSCMPrefs();
		Element repoEl = (Element)XPath.selectSingleNode(prefsEl, "repository[@url='"+url+"']");
		if (repoEl != null) {
			repoEl.detach();

			Configuration.setSCMPrefs(prefsEl);
			Configuration.writePrefsFile();
		}
		return "true";
	}
	
	public String synchronize(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String path = request.getParameter("path");
		File wcPath = null;
		File file = null;
		if (path != null) {
			String project = getProject(path);
			file = Application.getWorkspaceFile(project);
			wcPath = Application.getWorkspaceFile(path);
		} else {
			file = wcPath = Application.getWarehouseRoot();
		}
		
		if (file == null || wcPath == null) 
			return "同步时出错!";
		
		ThreeWaySynchronizer synchronizer = SCM.getSynchronizer(file);
		long revision = synchronizer.synchronize(wcPath);
		String result = revision==-1 ? "自上次提交后没有发生任何变动" : "同步完成, 版本号: "+revision;
		return result;
	}
	
	public String getSCMPrefs(HttpServletRequest request, HttpServletResponse response) throws Exception {
		JSONArray result = new JSONArray();
		Element prefsEl = Configuration.getSCMPrefs();
		List<?> repos = prefsEl.getChildren("repository");
		for (Object repo : repos) {
			Element repoEl = (Element)repo;
			JSONObject data = new JSONObject();
			data.put("text", repoEl.getAttributeValue("url"));
			data.put("leaf", true);
			data.put("iconCls", "x-icon-repo");
			result.put(data);
		}
		return result.toString();
	}
	
	public String getFolders(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String repositoryURL = request.getParameter("repositoryURL");
		String path = request.getParameter("path");
		
		JSONArray result = new JSONArray();
		PathEntry[] entries = SCM.getEntries(repositoryURL, path);
		for (PathEntry entry : entries) {
			if (entry.getKind()==null || !entry.getKind().equals(PathEntry.DIR)) {
				continue;
			}
			
			JSONObject data = new JSONObject();
			data.put("name", entry.getName());
			data.put("text", entry.getName());
			data.put("revision", entry.getRevision());
			data.put("leaf", false);
			result.put(data);
		}

		Element prefsEl = Configuration.getSCMPrefs();
		
		Element repoEl = (Element)XPath.selectSingleNode(prefsEl, "repository[@url='"+repositoryURL+"']");
		if (repoEl == null) {
			repoEl = new Element("repository");
			repoEl.setAttribute("url", repositoryURL);
			prefsEl.addContent(repoEl);
			Configuration.setSCMPrefs(prefsEl);
			Configuration.writePrefsFile();
		}
		
		return result.toString();
	}
	
	
	public String saveSCMPrefs(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String url = request.getParameter("url");
		System.out.println("repository: " + url);
		
		Element prefsEl = new Element("scm");
		prefsEl.setAttribute("url", url);
		prefsEl.setAttribute("username", "liuyl");
		prefsEl.setAttribute("password", "liuyl");
		Configuration.setSCMPrefs(prefsEl);
		/*
		ThreeWaySynchronizer synchronizer = SCM.getSynchronizer();
		File workspaceRoot = Application.getWorkspaceRoot();
		File[] files = workspaceRoot.listFiles();
		for (File file : files) {
			if (SVNWCUtil.isVersionedDirectory(file)) {
				synchronizer.relocate(file, file.getName());
			}
			else if (!synchronizer.isIgnored(file) && file.isDirectory()) {
				synchronizer.put(file, file.getName(), "initial version");
			}
		}
		*/
		Configuration.writePrefsFile();
		
		JSONObject result = new JSONObject();
		result.put("success", true);
		return result.toString();
	}%>

<%
	String operation = request.getParameter("operation");
	WebServletResponse responseWrapper = new WebServletResponse(response);

	if (operation == null)
		operation = request.getMethod().toLowerCase();

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