<%@page language="java" contentType="text/xml; charset=utf-8" pageEncoding="utf-8"%>
<%@page import="com.sanxing.ads.Configuration"%>
<%@page import="java.io.*,java.util.*"%>
<%@page import="java.lang.reflect.*"%>
<%@page import="java.rmi.*"%>
<%@page import="javax.management.*,javax.management.remote.*" %>
<%@page import="java.util.concurrent.*"%>
<%@page import="com.sanxing.ads.utils.*"%>
<%@page import="org.apache.log4j.Logger"%>
<%@page import="org.json.*" %>
<%@page import="org.dom4j.io.*,org.dom4j.*"%>

<%!
	private MBeanServer localServer = null;

	public   void   jspInit()   {
		localServer = (MBeanServer)getServletContext().getAttribute("MBeanServer");
	} 
	
	private BlockingQueue<ObjectName> queue = new ArrayBlockingQueue<ObjectName>(100);

	private Map<String,ObjectInstance> objectInstanceCache = new ConcurrentHashMap<String,ObjectInstance>();
	
	private String jmxServiceURL = null;
	
	private String domain = "*:";

	private ObjectInstance getServerManagerObjectInstance(String servername) throws Exception{
		ObjectInstance obj = objectInstanceCache.get(servername);
		
		if(null == obj){
			ObjectName tempName = ObjectName.getInstance(":ServerName=" + servername+ ",Type=Platform,Name=server-manager");
			if(null != localServer){
				Set<ObjectInstance> set = localServer.queryMBeans(tempName, null);
				if(set.size() > 0){
					obj = set.iterator().next();
					objectInstanceCache.put(servername,obj);
				}
			}else{
				throw new Exception("can not get local MBeanServer!");
			}
		}
		return obj;
	}

	public String listenNotification(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		JSONObject result = new JSONObject();
		ObjectName objectName = (ObjectName) queue.poll(60, TimeUnit.SECONDS);
		if (objectName != null) {
			result.put("domain", objectName.getDomain());
		}
		response.setContentType("text/json;charset=utf-8");
		return result.toString();
	}

	public String invoke(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		if(null == localServer)
			throw new Exception("can not get local MBeanServer!");
		String result = "true";
		String record = request.getParameter("record");
		JSONObject rc = new JSONObject(record);
		String name = rc.optString("objectName");
		String op_name = rc.optString("op_name");
		String returnType = rc.optString("returnType");
		JSONArray params = (JSONArray) rc.get("parameters");
		
		int params_len = params.length();
		Object[] parameters = null;
		String[] signature = null;
		if (params_len > 0) {
			parameters = new Object[params.length()];
			signature = new String[params.length()];

		}else{
			parameters =new Object[0];
			signature = new String[0];
		}
		for (int i = 0; i < params.length(); i++) {
			JSONObject obj = (JSONObject) params.get(i);
			signature[i] = (String) obj.get("param_type");
		
			if (((String) obj.get("param_type")).equals("int")) {
				parameters[i] = obj.getInt("param_value");
			} else if (((String) obj.get("param_type")).equals("boolean")) {
				parameters[i] = obj.getBoolean("param_value");
			} else if (((String) obj.get("param_type")).equals("long")) {
				parameters[i] = obj.getLong("param_value");
			} else if (((String) obj.get("param_type")).equals("double")) {
				parameters[i] = obj.getDouble("param_value");
			} else {
				parameters[i] = obj.get("param_value");
			}
		}

		ObjectName objectName = new ObjectName(name);
		if(!returnType.equals("void")){
			Object value = localServer.invoke(objectName, op_name, parameters, signature);
			result = new String(value.toString().getBytes("gbk"),"utf-8");
			response.setContentType("text/xml;charset=gbk");
		}
		else{
			localServer.invoke(objectName, op_name, parameters, signature);
		}
		
		return result;
	}

	public String invokeServer(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		if(null == localServer)
			throw new Exception("can not get local MBeanServer!");
		try {
			String servername = request.getParameter("servername");
			String op = request.getParameter("op");

			Object[] parameters = null;
			String[] signature = null;
			ObjectName objectName = new ObjectName(":ServerName=" + servername
					+ ",Type=Platform,Name=server-manager");
			
			localServer.invoke(objectName, op, parameters, signature);
		} catch (InstanceNotFoundException e) {
			e.printStackTrace();
			throw new Exception("该服务器没有启动!");
		}
		return "true";
	}

	public String startCollection(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String objectName = request.getParameter("objectName");
		
		if (null == objectName || objectName.length() < 1) {
			throw new Exception("can not get the collector objectName!");
		}
		if(null == localServer)
			throw new Exception("can not get local MBeanServer!");
		String id = request.getParameter("id");

		String senSorName = "";
		if (id.equals("system")) {
			senSorName = objectName.replace("Type=Collector", "Type=Sensor") + ",*";
		} else {

			senSorName = objectName.split(":")[0] + ":Type=Sensor,*";
		}
		ObjectName objName = ObjectName.getInstance(senSorName);

		JSONObject view = getView(objName);

		if (id.equals("system")) {
			view.put("name", "system");
			objName = ObjectName.getInstance(objectName);
		} else {
			view.put("name", "group");
			objName = ObjectName.getInstance(objectName.split(":")[0]
					+ ":Type=Collector,*");
		}

		Set<ObjectInstance> set = localServer.queryMBeans(objName, null);
		for (Iterator<ObjectInstance> it = set.iterator(); it.hasNext();) {
			ObjectInstance obj = it.next();
			localServer.invoke(obj.getObjectName(), "activate", null, null);
		}

		return view.toString();
	}

	public String endCollection(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String objectName = request.getParameter("objectName");
		
		if (null == objectName || objectName.length() < 1) {
			throw new Exception("can not get the collector objectName!");
		}
		if(null == localServer)
			throw new Exception("can not get local MBeanServer!");
		ObjectName objName = ObjectName.getInstance(objectName.split(":")[0]
				+ ":Type=Collector,*");

		Set<ObjectInstance> set = localServer.queryMBeans(objName, null);
		for (Iterator<ObjectInstance> it = set.iterator(); it.hasNext();) {
			ObjectInstance obj = it.next();
			localServer.invoke(obj.getObjectName(), "deactivate", null, null);
		}
		return "true";
	}

	private JSONObject getView(ObjectName objName) throws Exception {
		JSONObject temp = new JSONObject();

		JSONObject view = new JSONObject();
		JSONArray columns = new JSONArray();
		view.put("columns", columns);
		JSONObject tradeType = new JSONObject();
		tradeType.put("header", "交易类型");
		tradeType.put("dataIndex", "tradeType");
		columns.put(tradeType);
		
		

		Set<ObjectInstance> set = localServer.queryMBeans(objName, null);
		for (Iterator<ObjectInstance> it = set.iterator(); it.hasNext();) {
			ObjectInstance obj = it.next();
			String[] array = obj.getObjectName().toString().split(",");
			JSONObject column = new JSONObject();
			column.put("header", array[array.length - 1].replace("Name=", ""));
			column.put("dataIndex", array[array.length - 1]
					.replace("Name=", ""));
			temp.put(array[array.length - 1].replace("Name=", ""), column);
		}
		for (Iterator<String> it = temp.keys(); it.hasNext();) {
			String key = it.next();
			columns.put(temp.getJSONObject(key));
		}

		return view;
	}
	
	private void addChild(JSONArray children,String text,String iconCls,String state,boolean isLeaf,String id, String name, String type)
		throws Exception
	{
		JSONObject child = new JSONObject();
		child.put("text", text);
		child.put("leaf", isLeaf);
		child.put("key", id);
		child.put("name", name);
		child.put("state", state);
		child.put("type", type);
		if(iconCls.length() > 0) {
			child.put("iconCls", iconCls);
		}
		children.put(child);
	}
	
	private void setMangedServerInfo(File file, JSONArray items) throws Exception{
		if (file.exists()) {
			SAXReader reader = new SAXReader();
			Document doc = reader.read(file);
			Element root = doc.getRootElement();
			
			Iterator it = root.elements("server").iterator();
			while(it.hasNext()){
				JSONObject item = new JSONObject();
				Element server = (Element) it.next();
				Iterator temp = server.elements().iterator();
				while(temp.hasNext()){
					Element el = (Element)temp.next();
					String name = el.getName();
					if(name.equals("jms")){
						Element app_info = el.element("app-info");
						if(null != app_info){
							Element port = app_info.element("activemq-broker-port");
							if(port != null){
								item.put("jms-port",port.getTextTrim());
							}
						}
					}else if(name.equals("jdbc")){
						Element datasource = el.element("datasource");
						if(null != datasource){
							Element jndi_name = datasource.element("jndi-name");
							if(null != jndi_name){
								item.put("jndi-name",jndi_name.getTextTrim());
							}
						}
					}else {
						item.put(name,el.getTextTrim());
					}
				}
				
				if(item.length() > 0){
					String servername = item.optString("server-name");
					JSONObject sysInfo = getCpuAndMemory(servername);
					if(null != sysInfo){
						item.put("sysInfo",sysInfo);
						item.put("started",true);
					}else{
						item.put("started",false);
					}
					items.put(item);
				}
			}
		}
	}
	
	private void setAdminServerInfo(File file, JSONArray items) throws Exception{ 
		if (file.exists()) {
			SAXReader reader = new SAXReader();
			Document doc = reader.read(file);
			Element root = doc.getRootElement();
			
			JSONObject item = new JSONObject();
			
			Iterator temp = root.elements().iterator();
			while(temp.hasNext()){
				Element el = (Element)temp.next();
				String name = el.getName();
				if(name.equals("jms")){
					Element app_info = el.element("app-info");
					if(null != app_info){
						Element port = app_info.element("activemq-broker-port");
						if(port != null){
							item.put("jms-port",port.getTextTrim());
						}
					}
				}else if(name.equals("jdbc")){
					Element datasource = el.element("datasource");
					if(null != datasource){
						Element jndi_name = datasource.element("jndi-name");
						if(null != jndi_name){
							item.put("jndi-name",jndi_name.getTextTrim());
						}
					}
				}else {
					item.put(name,el.getTextTrim());
				}
			}
			
			if(item.length() > 0){
				String servername = item.optString("server-name");
				JSONObject sysInfo = getCpuAndMemory(servername);
				if(null != sysInfo){
					item.put("sysInfo",sysInfo);
					item.put("started",true);
				}else{
					item.put("started",false);
				}
				items.put(item);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public String loadServers(HttpServletRequest request,HttpServletResponse response) throws Exception{
		JSONObject result = new JSONObject();
		JSONArray items = new JSONArray();
		result.put("items",items);
		
		// 管理服务器信息
		File adminFile = Configuration.getServerFile();	
		setAdminServerInfo(adminFile, items);
		
		// 受管服务员信息
		File clusterFile = Configuration.getClusterFile();
		setMangedServerInfo(clusterFile, items);
		
		response.setContentType("text/json;charset=utf-8");
		return result.toString();
	}
	
	@SuppressWarnings("unchecked")
	private void addChildren(JSONArray result, ObjectName queryName, String type, boolean isLeaf, String childType) 
		throws Exception
	{
		
		if(null == localServer)
			throw new Exception("can not get local MBeanServer!");
		Set<ObjectInstance> set = localServer.queryMBeans(queryName, null);
		for (Iterator<ObjectInstance> it = set.iterator(); it.hasNext();) {
			ObjectInstance obj = it.next();
			ObjectName objName = obj.getObjectName();
			String name = objName.getKeyProperty("Name");
			
			String desc = getMBeanAttribute(localServer,obj,"description");
			desc = desc.length() > 0 ? name +"("+desc+")" : name;
			String iconCls = "";
			if (type.equals("Component")) {
				iconCls = "x-icon-com";
			}
			else if (type.equals("Endpoint")) {
				iconCls = "x-icon-endpoint";
			}
			String state =  getMBeanAttribute(localServer,obj,"currentState");
			
			addChild(result, desc, iconCls,state,isLeaf, "Name="+name, objName.toString(), childType);
		}
	}
	
	public String getSysInfo(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		if(null == localServer)
			throw new Exception("can not get local MBeanServer!");
		JSONArray result = new JSONArray();
		String servers = request.getParameter("servers");
		JSONArray arrays = new JSONArray(servers);
		for(int i = 0;i<arrays.length();i++){
			String servername = arrays.optJSONObject(i).optString("server");
			JSONObject server = getCpuAndMemory(servername);
			if(null != server)
				result.put(server);
		}
		
		JSONObject jso = new JSONObject();
		jso.put("type", "event");
		jso.put("name", "message");
		jso.put("data", result);
		jso.put("total", result.length());
		
		response.setContentType("text/json;charset=utf-8");
		return jso.toString();
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject getCpuAndMemory(String servername) throws Exception{
		JSONObject result = null;
		ObjectInstance serverManager = objectInstanceCache.get(servername);
		
		if(null == serverManager)
			serverManager = getServerManagerObjectInstance(servername);
		if(null != serverManager){
			String state = getMBeanAttribute(localServer,serverManager,"State");
			if(state.equals("running")){
				result = new JSONObject();
				result.put("name",servername);
				String cpu = getMBeanAttribute(localServer,serverManager,"SystemCpu");
				String memory = getMBeanAttribute(localServer,serverManager,"JVMMemory");
				if(cpu.length() > 0)
					result.put("cpu",Float.parseFloat(cpu) * 100);
				if(memory.length() > 0)
					result.put("memory",Float.parseFloat(memory) * 100);
				
				JSONArray threadPools = getThreadPools(servername);
				if (threadPools.length() >0)
					result.put("threadPool", threadPools);
			}
		}
		return result;
	}
	
	private JSONArray getThreadPools(String servername) throws Exception{
		
		if(null == localServer)
			throw new Exception("can not get local MBeanServer!");
		JSONArray jsoArray = new JSONArray();
		ObjectName tempName = ObjectName.getInstance(":ServerName=" + servername+ ",Type=ThreadPool,*");
		Set<ObjectInstance> set = localServer.queryMBeans(tempName, null);
		if(set.size() == 0){
		}else{
			for (Iterator<ObjectInstance> it = set.iterator(); it.hasNext();) {
				JSONObject jso = new JSONObject();
				ObjectInstance obj = it.next();
				ObjectName objName = obj.getObjectName();			
				
				jso.put("poolId", getMBeanAttribute(localServer,obj, "ID"));
				jso.put("activeCount", localServer.getAttribute(objName, "ActiveCount"));
				jso.put("completedTaskCount",localServer.getAttribute(objName, "CompletedTaskCount"));
				jso.put("corePoolSize", localServer.getAttribute(objName, "CorePoolSize"));
				jso.put("largestPoolSize", localServer.getAttribute(objName, "LargestPoolSize"));
				jso.put("poolSize", localServer.getAttribute(objName, "PoolSize"));
				jso.put("taskCount", localServer.getAttribute(objName, "TaskCount"));
				jsoArray.put(jso);
			}
		}
		return jsoArray;
	}
	
	public String getServerMBean(HttpServletRequest request,HttpServletResponse response) throws Exception {
		
		if(null == localServer)
			throw new Exception("can not get local MBeanServer!");
		JSONArray result = new JSONArray();
		String servername = request.getParameter("servername");
		String type = request.getParameter("type");
		String nodeId = request.getParameter("node");
		String nodePath = request.getParameter("path");
		
		if(nodePath.equals("")){
			ObjectName queryName = ObjectName.getInstance(domain+"Type=Platform,Name=server-manager,*");
			Set<ObjectInstance> set = localServer.queryMBeans(queryName, null);
			for (Iterator<ObjectInstance> it = set.iterator(); it.hasNext();) {
				ObjectInstance obj = it.next();
				ObjectName objName = obj.getObjectName();
				String serverName = objName.getKeyProperty("ServerName");
				addChild(result, serverName,"x-icon-server","",false, "ServerName="+serverName, "", "Server");
			}
		}
		else if(type.equals("Server")){
			ObjectName objectName = ObjectName.getInstance(domain+nodePath.replace("/", ","));
			addChild(result,"组件","","",false, "Type=Component,SubType=LifeCycle", "", "Component");
			addChild(result,"部署包","","",false, "Type=ServiceAssembly", "", "ServiceAssembly");
			addChild(result,"端点","","",false, "Type=Endpoint", "", "Endpoint");
		}
		else if(type.equals("ServiceAssembly")){
			ObjectName queryName = ObjectName.getInstance(domain+nodePath.replace("/", ",")+",*");
			Set<ObjectInstance> set = localServer.queryMBeans(queryName, null);
			for (Iterator<ObjectInstance> it = set.iterator(); it.hasNext();) {
				ObjectInstance obj = it.next();
				ObjectName objName = obj.getObjectName();
				String name = objName.getKeyProperty("Name");
				
				String desc = getMBeanAttribute(localServer,obj,"description");
				desc = desc.length() > 0 ? name +"("+desc+")" : name;
				
				String state =  getMBeanAttribute(localServer,obj,"currentState");
				
				addChild(result, desc, "x-icon-sa", state, false, "Name="+name, objName.toString(), "ServiceUnitAdaptor");
			}
			
		}
		else if(type.equals("ServiceUnitAdaptor")){
			ObjectName parentName = ObjectName.getInstance(domain+nodePath.replace("/", ","));
			String saName = parentName.getKeyProperty("Name");
			ObjectName queryName = ObjectName.getInstance(domain+"ServerName="+
					parentName.getKeyProperty("ServerName")+",Type="+type+",*");
			Set<ObjectInstance> set = localServer.queryMBeans(queryName, null);
			for (Iterator<ObjectInstance> it = set.iterator(); it.hasNext();) {
				ObjectInstance obj = it.next();
				ObjectName objName = obj.getObjectName();
				String serviceAssembly = getMBeanAttribute(localServer,obj,"serviceAssembly");
				if(serviceAssembly.endsWith(saName)){
					String name = objName.getKeyProperty("Name");
					String desc = getMBeanAttribute(localServer,obj,"description");
					desc = (desc.length() > 0) ? name +"("+desc+")" : name;
					
					String state =  getMBeanAttribute(localServer,obj,"currentState");
					
					addChild(result, desc, "x-icon-su",state,true, "Name="+name, objName.toString(), "");
				}
			}
		}
		else {
			ObjectName queryName = ObjectName.getInstance(domain+nodePath.replace("/", ",")+",*");
			addChildren(result, queryName, type, true, "");
		}
		response.setContentType("text/json;charset=utf-8");
		return result.toString();
	}

	public String refreshMBean(HttpServletRequest request,HttpServletResponse response) throws Exception {
		
		if(null == localServer)
			throw new Exception("can not get local MBeanServer!");
		JSONObject mbeanInfo = new JSONObject();

		String objectName = request.getParameter("objectName");
		if (null == objectName || objectName.length() < 1) {
			throw new Exception("can not get the collector objectName!");
		}
		ObjectName objName = ObjectName.getInstance(objectName);
		Set<ObjectInstance> set = localServer.queryMBeans(objName, null);

		//deal with each MBean Object
		for (Iterator<ObjectInstance> it = set.iterator(); it.hasNext();) {
			ObjectInstance obj = it.next();
			MBeanInfo info = localServer.getMBeanInfo(obj.getObjectName());
			JSONObject attrs = getMBeanAttrs(info, obj);
			JSONArray ops = getMBeanOps(info, obj);
			mbeanInfo.put("attris", attrs);
			mbeanInfo.put("ops", ops);
		}

		return mbeanInfo.toString();
	}
	
	public String getMBeanAttribute(MBeanServerConnection serverConn,ObjectInstance obj,String name) throws Exception{
		String desc = "";
		try{
			desc = (String)serverConn.getAttribute(obj.getObjectName(),name);
		}catch(Exception e){
			
		}
		return desc;
	}
	
	public JSONObject getMBeanAttrs(MBeanInfo info, ObjectInstance obj)
			throws Exception {
		
		if(null == localServer)
			throw new Exception("can not get local MBeanServer!");
		JSONObject attrs = new JSONObject();
		for (MBeanAttributeInfo attrInfo : info.getAttributes()) {
			String attrName = attrInfo.getName();
			attrs.put(attrName, localServer.getAttribute(obj.getObjectName(),
					attrName));
		}
		return attrs;
	}

	public JSONArray getMBeanOps(MBeanInfo info, ObjectInstance obj)
			throws Exception {
		
		JSONArray ops = new JSONArray();
		for (MBeanOperationInfo op : info.getOperations()) {
			JSONObject mbop = new JSONObject();
			ops.put(mbop);
			mbop.put("op_name", op.getName());
			mbop.put("returnType", op.getReturnType());
			JSONArray paramArray = new JSONArray();
			mbop.put("parameters", paramArray);
			MBeanParameterInfo[] mbpi = op.getSignature();
			for (MBeanParameterInfo i : mbpi) {
				JSONObject eachParam = new JSONObject();
				eachParam.put("param_name", i.getName());
				eachParam.put("param_type", i.getType());
				paramArray.put(eachParam);
			}
		}
		return ops;
	}

	public String pollMessage(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		if(null == localServer)
			throw new Exception("can not get local MBeanServer!");
		JSONObject result = new JSONObject();
		try {
			String model = request.getParameter("model");
			String id = request.getParameter("id");
			ObjectName objName = ObjectName
					.getInstance(":Type=SystemService,Name=PresentationService,*");
			Set<ObjectInstance> set = localServer.queryMBeans(objName, null);
			for (Iterator<ObjectInstance> it = set.iterator(); it.hasNext();) {
				ObjectInstance obj = it.next();
				Object[] params = new Object[2];
				params[0] = model;
				params[1] = id;
				String[] signature = new String[2];
				signature[0] = String.class.getName();
				signature[1] = String.class.getName();

				String temp = (String) localServer.invoke(obj.getObjectName(),
						"pollMessage", params, signature);

				result.put("type", "event");
				result.put("name", "message");
				result.put("data", new JSONArray(temp));
				response.setContentType("text/json; charset=utf-8");
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result.toString();
	}%>

<%
	Logger logger = Logger.getLogger(this.getClass());

	String operation = request.getParameter("operation");
	WebServletResponse responseWrapper = new WebServletResponse(
			response);

	if (operation == null)
		operation = request.getMethod().toLowerCase();
	
	try {
		Class<?> clazz = this.getClass();
		Method method = clazz.getMethod(operation, new Class[] {
				HttpServletRequest.class, HttpServletResponse.class });
		String result = (String) method.invoke(this, new Object[] {
				new WebServletRequest(request), responseWrapper });
		out.clear();
		out.println(result);
	} catch (NoSuchMethodException e) {
		responseWrapper.sendError("[" + request.getMethod()
				+ "]找不到相应的方法来处理指定的 operation: " + operation);
	} catch (InvocationTargetException e) {
		Throwable t = e.getTargetException();
		/*if (t instanceof ConnectException) {
			try {
				retryConnect();
				responseWrapper.sendError("JMX 连接断开，已重新建立连接");
				return;
			} catch (Exception ex) {
				responseWrapper.sendError("JMX 连接断开，重新建立连接失败");
				return;
			}
		} else if (!(t instanceof FileNotFoundException))
			logger.error("", t);*/
		logger.error("", e);
		responseWrapper.sendError(t.getMessage());
	} catch (Exception e) {
		logger.error("", e);
		responseWrapper.sendError(e.getMessage());
	}
%>