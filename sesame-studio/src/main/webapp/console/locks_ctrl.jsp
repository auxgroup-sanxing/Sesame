
<%@page import="org.tmatesoft.svn.core.SVNLock"%>
<%@page import="com.sanxing.ads.team.svn.SVNSynchronizer"%>
<%@page import="com.sanxing.ads.team.SCM"%>
<%@page import="com.sanxing.ads.team.ThreeWaySynchronizer"%><%@page import="com.sanxing.ads.*"%>
<%@page import="com.sanxing.ads.utils.*"%>
<%@page import="com.sanxing.statenet.transport.*"%>
<%@page import="java.io.*, java.util.*"%>
<%@page import="java.lang.reflect.*"%>
<%@page import="java.net.*"%>
<%@page import="org.apache.log4j.Logger"%>
<%@page import="org.json.*" %>
<%@page import="org.dom4j.*, org.dom4j.io.*"%>

<%@page language="java" contentType="text/xml; charset=utf-8" pageEncoding="utf-8"%>

<%!
	private final Logger logger = Logger.getLogger(this.getClass());

	public String getLocks(HttpServletRequest request, HttpServletResponse response) throws Exception {
		JSONObject result = new JSONObject();
		JSONArray locks = new JSONArray();
		String projectName = request.getParameter("projectName");
		File projectFolder = Configuration.getWorkspaceFile(projectName);
		SVNSynchronizer sync = (SVNSynchronizer)SCM.getSynchronizer(projectFolder);
		SVNLock[] svnLocks = null;
		try{
			svnLocks = sync.getLocks(projectName);
		}catch(Exception e){
			//e.printStackTrace();
		}
		if(null != svnLocks)
		for(SVNLock l:svnLocks){
			String filePath = l.getPath();
			
			
			
			String[] temp = filePath.split("/");
			String filename = temp[temp.length - 1];
			String parent = temp[temp.length -2];
			String unitname = "";
			
			if(filename.endsWith(".xsd") || filename.endsWith(".wsdl")){
				JSONObject lock = new JSONObject();
				lock.put("owner",l.getOwner());
				lock.put("createDate",l.getCreationDate());
				
				if(filename.endsWith(".xsd") && (parent.equals("schema"))){
					unitname = temp[temp.length -3];
					lock.put("type","数据字典");
					lock.put("path",unitname+"/"+parent+"/"+filename);
				}else if(filename.endsWith(".xsd") && (!parent.equals("schema"))){
					String serverType = temp[temp.length -3];
					unitname = temp[temp.length -4];
					lock.put("path",unitname+"/"+serverType+"/"+parent+"/"+filename);
					lock.put("type","操作");
				}else if(filename.endsWith(".wsdl")){
					String serverType = temp[temp.length -3];
					unitname = temp[temp.length -4];
					lock.put("path",unitname+"/"+serverType+"/"+parent+"/"+filename);
					lock.put("type","服务");
				}else{
					//TODO
					//lock.put("path",filePath);
					//lock.put("type","未知");
					
				}
				locks.put(lock);
			}
		}
		
		
		result.put("locks",locks);
		response.setContentType("text/json;charset=utf-8");
		return result.toString();
	}
	public String unLock(HttpServletRequest request, HttpServletResponse response) throws Exception{
		try{
		String unit = request.getParameter("unit");
		File file = Configuration.getWorkspaceFile(unit);
		ThreeWaySynchronizer sync = SCM.getSynchronizer(file.getParentFile());
		sync.unlock(file,true);
		response.setContentType("text/plain;charset=utf-8");
		}catch(Exception e){
			//e.printStackTrace();
		}
		return "success";
	}
	
	public String getProject(HttpServletRequest request, HttpServletResponse response) throws Exception {
		JSONObject result = new JSONObject();
		JSONArray projects = new JSONArray();
		File workspaceRoot = Configuration.getWorkspaceRoot();
		String[] files = workspaceRoot.list(new FilenameFilter(){
			public boolean accept(File dir,String name){
				File file = new File(dir,name);
				if(file.isDirectory() && (!name.startsWith("."))){
					return true;
				}else{
					return false;
				}
			}
		});
		for(String file:files){
			JSONObject project = new JSONObject();
			project.put("text",file);
			project.put("value",file);
			projects.put(project);
		}
		result.put("projects",projects);
		return result.toString();
	}
	%>

<%
	Logger logger = Logger.getLogger(this.getClass());
	String operation = request.getParameter("operation");
	WebServletResponse responseWrapper = new WebServletResponse(response);
	
	try {
		response.setContentType("text/json; charset=utf-8");
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
		responseWrapper.sendError(t.getMessage());
	}
	catch (Exception e) {
		e.printStackTrace();
		logger.error("", e);
		responseWrapper.sendError(e.getMessage());
	}
%>