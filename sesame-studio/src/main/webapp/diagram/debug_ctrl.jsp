<%@page language="java" contentType="text/xml; charset=utf-8"
	pageEncoding="utf-8"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="javax.xml.transform.dom.DOMSource"%>
<%@page import="com.sanxing.studio.*,com.sanxing.studio.utils.*"%>
<%@page import="java.io.*, java.util.*"%>
<%@page import="java.lang.reflect.*"%>
<%@page import="org.slf4j.Logger, org.slf4j.LoggerFactory"%>
<%@page import="org.json.*"%>
<%@page import="org.jdom.input.SAXBuilder"%>
<%@page import="org.jdom.*"%>
<%@page import="org.jdom.input.SAXBuilder"%>
<%@page import="com.sanxing.sesame.engine.*"%>
<%@page import="com.sanxing.sesame.engine.context.*"%>
<%@page import="com.sanxing.sesame.test.DummySequencer"%>
<%!
private Logger logger = LoggerFactory.getLogger(this.getClass());


private File getComponentRoot(File flowFile) throws IOException, JDOMException {
	File unitFolder = flowFile.getParentFile();

	SAXBuilder builder = new SAXBuilder();
	Document doc = builder.build(new File(unitFolder, "jbi.xml"));
	Element rootEl = doc.getRootElement();
	Namespace xmlns_comp = rootEl.getNamespace("comp");
	String component = xmlns_comp!=null ? xmlns_comp.getURI() : null;
	
	if (component != null) {
		return Application.getWarehouseFile(component);
	}
	else {
		return Application.getWarehouseFile("components/process-ec");
	}
}

@SuppressWarnings("unchecked")
public String startDebug(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	JSONObject item = new JSONObject();
	
	String suName = request.getParameter("suName");
	String operaName = request.getParameter("operaName");
	
	// 流程xml文件路径
	String flowXmlPath = request.getParameter("flowXml");
	File flowFile =  Configuration.getWorkspaceFile(flowXmlPath);
	if (!flowFile.exists()) {
	    item.put("flowId", "EOP");
	    item.put("context", "");
	} 
	else {
		// 传入的原始消息xml字符串
		String data = URLDecoder.decode(((String)request.getParameter("data")), "UTF-8");
		
		// 取得EngineDebugger对象和ExecutionContext上下文环境
		HttpSession session = request.getSession();
		EngineDebugger debug = (EngineDebugger)session.getAttribute("debug");
		ExecutionContext ec = (ExecutionContext)session.getAttribute("context");
		
		if (debug == null) {
			File componentRoot = getComponentRoot(flowFile);

			Document flowDef = new SAXBuilder().build(flowFile);
			Engine engine = Engine.getInstance();
			engine.registerFlow(flowFile.getAbsolutePath(), flowDef.getRootElement());
			 
			Reader reader= new StringReader(data);
			Document inputDoc = new SAXBuilder().build(reader);
		    Element requestData = inputDoc.getRootElement();
		    
		    if (ec == null) {
				ec = new ExecutionContext("testFlow");
				ec.put(ExecutionEnv.SERIAL_NUMBER, DummySequencer.getSerial());
				ec.put(com.sanxing.sesame.engine.component.Constants.SERVICE_NAME, suName);
				ec.put(com.sanxing.sesame.engine.component.Constants.OPERATION_NAME, operaName);
				session.setAttribute("context", ec);
		    }
		    ec.getDataContext().addVariable("request", new Variable(requestData, Variable.ELEMENT));
		    
		    debug = new EngineDebugger(ec);
		    session.setAttribute("debug", debug);
		    
		    // 开始流程，在执行第一个action之后，流程就暂停
		    debug.start(componentRoot.getAbsolutePath(), flowFile.getAbsolutePath());			

		    Thread.sleep(300);
		    item.put("actionId", ec.getCurrentAction());
		    item.put("context", new JSONObject(dataContextToString(ec.getDataContext())));
		    if (ec.get("exception") != null)  {
		    	item.put("exception", ((Exception)ec.get("exception")).getMessage());
		    }
		}
	}
	
	response.setContentType("text/json; charset=utf-8");
	return item.toString();
}


private String dataContextToString(DataContext dc){
        JSONObject jsonObject = new JSONObject();
        JSONArray items = new JSONArray();  
        try {
	        Set varNames = dc.getVariables().keySet();
	        Iterator iter = varNames.iterator();
	        while (iter.hasNext()) {
	            String name = (String) iter.next();
	            JSONObject item = new JSONObject();
	            item.put("key", name);
	            item.put("value", dc.getVariables().get(name));
	            items.put(item);
	        }
	        jsonObject.put("count", varNames.size());
	        jsonObject.put("items", items);
	        return jsonObject.toString();
	    } 
	    catch (JSONException e) {
	        e.printStackTrace();
	        return null;
	    }
}

@SuppressWarnings("unchecked")
public String stepDebug(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	// 返回值
	String flowId = null;
	String contextStr = null;
	JSONObject item = new JSONObject();
	
	// 取得EngineDebugger对象和ExecutionContext上下文环境
	HttpSession session = request.getSession();
	EngineDebugger debug = (EngineDebugger)session.getAttribute("debug");
	ExecutionContext ec = (ExecutionContext)session.getAttribute("context");
	
	if (debug != null) {
		debug.nextStep();
	    // 返回消息
	    Thread.sleep(80);
	    flowId = ec.getCurrentAction();
	    
	   contextStr = dataContextToString(ec.getDataContext());
	   JSONObject result = new JSONObject(contextStr);
	    result.put("flowId", flowId);
	    result.put("done", !ec.isDebugging());
	    
	    Throwable t = (Throwable)ec.get("exception");
	    if (t != null) {
	    	if (t.getCause() != null) {
	    		t = t.getCause();
	    	}
	    	result.put("exception", t.getMessage()!=null ? t.getMessage() : t.toString());
	    }
	    item = result;
	    
	    if (!ec.isDebugging()) {
	    	ec.close();
	    }
	}
	
	response.setContentType("text/json; charset=utf-8");
	return item.toString();
}

public String resume(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	HttpSession session = request.getSession();
	EngineDebugger debug = (EngineDebugger)session.getAttribute("debug");
	// 跳出调试，执行完剩下的流程
	if (debug != null) {
		debug.resume();
	}
	
	// 清空session中的调试信息
	session.setAttribute("debug", null);
	session.setAttribute("context", null);
	response.setContentType("text/plain; charset=utf-8");
	return "";
}

public String terminate(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	HttpSession session = request.getSession();
	EngineDebugger debug = (EngineDebugger)session.getAttribute("debug");
	if (debug != null) {
		debug.terminate();		// 终止调试
	}
	
	// 清空session中的调试信息
	session.setAttribute("debug", null);
	session.setAttribute("context", null);
	response.setContentType("text/plain; charset=utf-8");
	return "";
}
%>

<%
	String operation = request.getParameter("operation");
	
	WebServletResponse responseWrapper = new WebServletResponse(response);
	
	if (operation == null) operation = request.getMethod().toLowerCase();

	try {
		Class<?> clazz = this.getClass();
		Method method = clazz.getMethod(operation, new Class[]{HttpServletRequest.class, HttpServletResponse.class});
		String result = (String)method.invoke(this, new Object[]{new WebServletRequest(request), responseWrapper});
		out.clear();
		out.println(result);
	}
	catch (NoSuchMethodException e) {
		responseWrapper.sendError("["+request.getMethod()+"]没有方法处理指定的 operation: "+operation);
	}
	catch (InvocationTargetException e) {
		Throwable t = e.getTargetException();
		logger.error(t.getMessage(), t);
		responseWrapper.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage());
	}
	catch (Exception e) {
		logger.error(e.getMessage(), e);
		responseWrapper.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
	}

%>