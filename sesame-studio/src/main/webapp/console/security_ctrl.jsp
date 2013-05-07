<%@page import="com.sanxing.studio.*"%>
<%@page import="org.jdom.input.SAXBuilder"%>
<%@page import="java.io.*, java.util.*"%>
<%@page import="java.lang.reflect.*"%>
<%@page import="java.rmi.*"%>
<%@page import="com.sanxing.studio.utils.*"%>
<%@page import="org.slf4j.Logger, org.slf4j.LoggerFactory"%>
<%@page import="org.json.*"%>
<%@page import="org.jdom.*"%>
<%@page import="com.sanxing.sesame.core.keymanager.*"%>
<%@page language="java" contentType="text/xml; charset=utf-8"
	pageEncoding="utf-8"%>

<%!
private final Logger logger = LoggerFactory.getLogger(this.getClass());

// 读取keystore文件
@SuppressWarnings("unchecked")
public String getKeyStoreInfo(HttpServletRequest request,HttpServletResponse response) throws Exception{
	JSONObject result = new JSONObject();
	JSONArray items = new JSONArray();
	
	KeyStoreManager manager = KeyStoreManager.getInstance();
	List<KeyStoreInfo> list = manager.getAllKeyStore();
	
	if (list != null && !list.isEmpty()) {
		for (Iterator itr = list.iterator(); itr.hasNext();) {
			KeyStoreInfo info = (KeyStoreInfo)itr.next();
			JSONObject item = new JSONObject();
			item.put("name", info.getName());
			item.put("keystorePath", info.getKeystorePath());
			item.put("storePass", info.getStorePass());
			item.put("description", info.getDescription());
			items.put(item);
		}
	}
	
	response.setContentType("text/json;charset=utf-8");
	result.put("items", items);
	
	return result.toString();
}

// 保存keystore文件
public String saveKeyStoreInfo(HttpServletRequest request,HttpServletResponse response) throws Exception{
	String dataStr = request.getParameter("data");
	JSONArray jsoArray = new JSONArray(dataStr);
	
	if (jsoArray.length() > 0)
		for (int i=0; i<jsoArray.length(); i++) {
			JSONObject jso =jsoArray.getJSONObject(i);
			String oldName = jso.getString("oldName");
			String name = jso.getString("name");
			
			KeyStoreInfo info = new KeyStoreInfo();
			info.setName(name);
			info.setKeystorePath(jso.getString("keystorePath"));
			info.setStorePass(jso.getString("storePass"));
			String desc = jso.getString("description");
			if (desc == null)
				desc = "";
			info.setDescription(desc);
			
			KeyStoreManager manager = KeyStoreManager.getInstance();
			if (!oldName.equals(name)) {
				manager.delKeyStore(oldName);
			}
			manager.addKeyStore(info);
		}
	
	response.setContentType("text/plain;charset=utf-8");
	return "true";
}

public String deleteKeyStore(HttpServletRequest request,HttpServletResponse response) throws Exception{ 
	String name = request.getParameter("name");
	
	KeyStoreManager manager = KeyStoreManager.getInstance();
	manager.delKeyStore(name);
	
	SKPManager skpManager = SKPManager.getInstance();
	List<ServiceKeyProvider> list = skpManager.getAllKeyProvider();
	if (list != null && !list.isEmpty()) {
		for (Iterator itr = list.iterator(); itr.hasNext();) {
			ServiceKeyProvider skp = (ServiceKeyProvider)itr.next();
			if (name.equals(skp.getKeystoreName())) {
				skpManager.delSKP(skp.getName());
			}
		}
	}
	
	response.setContentType("text/plain;charset=utf-8");
	return "true";
}

@SuppressWarnings("unchecked")
public String getKeyStoreList(HttpServletRequest request,HttpServletResponse response) throws Exception{ 
	JSONObject result = new JSONObject();
	JSONArray items = new JSONArray();
	
	KeyStoreManager manager = KeyStoreManager.getInstance();
	List<KeyStoreInfo> list = manager.getAllKeyStore();
	
	if (list != null && !list.isEmpty()) {
		for (Iterator itr = list.iterator(); itr.hasNext();) {
			KeyStoreInfo info = (KeyStoreInfo)itr.next();
			JSONObject item = new JSONObject();
			item.put("keystore", info.getName());
			items.put(item);
		}
	}
	
	response.setContentType("text/json;charset=utf-8");
	result.put("items", items);
	
	return result.toString();
}


// 读取keyprovider文件
@SuppressWarnings("unchecked")
public String getKeyProviderInfo(HttpServletRequest request,HttpServletResponse response) throws Exception{
	JSONObject result = new JSONObject();
	JSONArray items = new JSONArray();
	
	SKPManager manager = SKPManager.getInstance();
	List<ServiceKeyProvider> list = manager.getAllKeyProvider();
	
	if (list != null && !list.isEmpty()) {
		for (Iterator itr = list.iterator(); itr.hasNext();) {
			ServiceKeyProvider skp = (ServiceKeyProvider)itr.next();
			JSONObject item = new JSONObject();
			item.put("name", skp.getName());
			item.put("alias", skp.getAlias());
			item.put("keystoreName", skp.getKeystoreName());
			item.put("paired", String.valueOf(skp.isPri()));
			item.put("keyPass", skp.getKeyPass());
			items.put(item);
		}
	}
	
	response.setContentType("text/json;charset=utf-8");
	result.put("items", items);
	
	return result.toString();
}

// 保存keyprovider文件
@SuppressWarnings("unchecked")
public String saveKeyProviderInfo(HttpServletRequest request,HttpServletResponse response) throws Exception{
	String dataStr = request.getParameter("data");
	JSONArray jsoArray = new JSONArray(dataStr);
	
	if (jsoArray.length() > 0)
		for (int i=0; i<jsoArray.length(); i++) {
			JSONObject jso =jsoArray.getJSONObject(i);
			
			ServiceKeyProvider skp = new ServiceKeyProvider();
			skp.setName(jso.getString("name"));
			skp.setAlias(jso.getString("alias"));
			skp.setKeystoreName(jso.getString("keystoreName"));
			skp.setKeyPass(jso.getString("keyPass"));
			skp.setPri(jso.optBoolean("paired"));
			
			SKPManager manager = SKPManager.getInstance();
			manager.addSKP(skp);
		}
	
	response.setContentType("text/plain;charset=utf-8");
	return "true";
}

public String deleteSkp(HttpServletRequest request,HttpServletResponse response) throws Exception{ 
	String name = request.getParameter("name");
	
	SKPManager manager = SKPManager.getInstance();
	manager.delSKP(name);
	
	response.setContentType("text/plain;charset=utf-8");
	return "true";
}
%>

<%
	Logger logger = LoggerFactory.getLogger(this.getClass());
	String operation = request.getParameter("operation");
	WebServletResponse responseWrapper = new WebServletResponse(response);
	
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
		responseWrapper.sendError(t.getMessage());
	}
	catch (Exception e) {
		logger.error("", e);
		responseWrapper.sendError(e.getMessage());
	}
%>