
<%@page import="com.sanxing.sesame.logging.lucene.LuceneColumn"%><%@page
	import="org.slf4j.Logger, org.slf4j.LoggerFactory"%>
<%@page import="java.io.*, java.util.*"%>
<%@page import="java.lang.reflect.*"%>
<%@page import="org.json.*"%>
<%@page import="com.sanxing.studio.*"%>
<%@page import="com.sanxing.studio.utils.*"%>
<%@page import="com.sanxing.sesame.logging.monitor.*"%>
<%@page import="com.sanxing.sesame.logging.dao.LogBean"%>
<%@page import="com.sanxing.sesame.dao.data.PageInfo"%>
<%@page import="com.sanxing.sesame.logging.lucene.LuceneSearcher"%>
<%@page import="com.sanxing.sesame.logging.lucene.LuceneRecord"%>
<%@page import="org.dom4j.*, org.dom4j.io.*"%>
<%@page language="java" contentType="text/xml; charset=utf-8"
	pageEncoding="utf-8"%>

<%!
private final Logger logger = LoggerFactory.getLogger(this.getClass());
private SesameMonitor am = new SesameMonitor();

// 获取交易信息
public String getTransactionInfo(HttpServletRequest request,HttpServletResponse response) throws Exception {
	JSONArray result = new JSONArray();
	JSONObject jsoData = new JSONObject();
	
	LogBean data = (LogBean)am.getMessage(0);
	if (data != null) {
		jsoData.put("CHANNEL", data.getChannel());
		jsoData.put("STATE", data.getState());
		jsoData.put("SERIALNUMBER", data.getSerialNumber().toString());
		jsoData.put("SERVICENAME", data.getServiceName());
		jsoData.put("OPERATIONNAME", data.getOperationName());
		jsoData.put("TRANSACTIONCODE", data.getTransactionCode());
		jsoData.put("STARTTIME", data.getStartTime());
		jsoData.put("UPDATETIME", data.getUpdateTime());
	}
	
	JSONObject jso = new JSONObject();
	jso.put("type", "event");
	jso.put("name", "message");
	jso.put("data", jsoData);
	jso.put("total", result.length());
	
	response.setContentType("text/json;charset=utf-8");
	return jso.toString();
}

// 模糊查询
@SuppressWarnings("unchecked")
public String queryTrans(HttpServletRequest request,HttpServletResponse response) throws Exception {
	String keyValue = request.getParameter("keyword");
	String types = request.getParameter("types");
	JSONObject typesJso = new JSONObject(types);
	List<String> fields = new ArrayList<String>(); 
	for (Iterator itr = typesJso.keys(); itr.hasNext();) {
		String key = (String)itr.next();
		if(typesJso.getBoolean(key))
			fields.add(key);
	}
	
	String temp[]=new String[fields.size()];
	String[] fieldsArray = fields.toArray(temp);
	
	JSONArray result = new JSONArray();
	JSONObject jsoData = new JSONObject();
	
	String start = request.getParameter("start");
	String limit = request.getParameter("limit");
	
	LuceneSearcher searcher = LuceneSearcher.getInstance();
	int totalCount = searcher.parse(keyValue, fieldsArray);
	int startNo = Integer.valueOf(start);
	int limitNo = Integer.valueOf(limit);
	int currentPageNo = startNo/limitNo + 1;
	
	Set<LuceneRecord> records = new TreeSet<LuceneRecord>();
	records = searcher.pageSearch(currentPageNo, limitNo);
	if (records != null && !records.isEmpty())
	for (Iterator recItr = records.iterator(); recItr.hasNext();) {
		LuceneRecord record = (LuceneRecord)recItr.next();
		JSONObject jso = new JSONObject();
		String state = record.getColumnByName("state").getValue();
		if (!typesJso.has("content") && "9".equals(state)) 
			continue;
		
		for (String colName : fieldsArray) {
			if ("content".equalsIgnoreCase(colName))
				continue;
			LuceneColumn vl = record.getColumnByName(colName);
			String value = (vl != null) ? vl.getValue() : "";
			jso.put(colName.toUpperCase(), value);
		}
		
		jso.put("STATE", record.getColumnByName("state").getValue());
		jso.put("SERIALNUMBER", record.getColumnByName("serialNumber").getValue());
		result.put(jso);
	}
	jsoData.put("data", result);
	jsoData.put("total", result.length()); 
	response.setContentType("text/json;charset=utf-8");
	return jsoData.toString();
}

// 精确查询
public String inquiryTrans(HttpServletRequest request,HttpServletResponse response) throws Exception {
	
	String conditionStr = request.getParameter("condition");
	JSONArray conditionArray = new JSONArray(conditionStr);
	
	JSONArray result = new JSONArray();
	JSONObject jsoData = new JSONObject();
	
	String start = request.getParameter("start");
	String limit = request.getParameter("limit");
	
	SQLCondition condition = new SQLCondition();
	for (int i=0; i<conditionArray.length(); i++) {
		JSONObject jso = conditionArray.getJSONObject(i);
		String colName = jso.getString("name");
		String op = jso.getString("op");
		String value = jso.getString("value");
		if ("serialNumber".equals(colName)) {
			condition.addNum(colName, op, value);
		} else if (colName.contains("Time")) {
			condition.addTimeStamp(colName, op, value);
		} else {
			condition.add(colName, op, value);
		}
	}
	condition.add("state", "<>", "9");
	
	long totalCount = am.queryCount(condition, false);
	int startNo = Integer.valueOf(start);
	int limitNo = Integer.valueOf(limit);
	
	PageInfo pageInfo = new PageInfo();
	pageInfo.setCurrentPageNo(startNo/limitNo + 1);
	pageInfo.setPageSize(limitNo);
	
	List<LogBean> dataList = am.queryForRecordSet(condition, pageInfo, false);
	if (dataList != null && !dataList.isEmpty()) {
		for (LogBean data : dataList) {
			JSONObject jso = new JSONObject();
			jso.put("CHANNEL", data.getChannel());
			jso.put("STATE", data.getState());
			jso.put("SERIALNUMBER", data.getSerialNumber().toString());
			jso.put("SERVICENAME", data.getServiceName());
			jso.put("OPERATIONNAME", data.getOperationName());
			jso.put("TRANSACTIONCODE", data.getTransactionCode());
			jso.put("STARTTIME", data.getStartTime());
			jso.put("UPDATETIME", data.getUpdateTime());
			result.put(jso);
		}
	}
	
	jsoData.put("data", result);
	jsoData.put("total", totalCount); 
	
	response.setContentType("text/json;charset=utf-8");
	return jsoData.toString();
	
}

// 详细信息查询
public String queryMsg(HttpServletRequest request, HttpServletResponse response) throws Exception { 
	String serailNumber = request.getParameter("snumber");
	String stateStr = request.getParameter("state");
	int state = Integer.valueOf(stateStr);
	JSONObject jso = new JSONObject();
	
	String start = request.getParameter("start");
	String limit = request.getParameter("limit");
	
	SQLCondition condition = new SQLCondition();
	condition.addNum("serialNumber", "=", serailNumber);
	LogBean data = am.queryForRecord(condition, true);
	
	long totalCount = am.queryCount(condition, true);
	int startNo = Integer.valueOf(start);
	int limitNo = Integer.valueOf(limit);
	
	PageInfo pageInfo = new PageInfo();
	pageInfo.setCurrentPageNo(startNo/limitNo + 1);
	pageInfo.setPageSize(limitNo);
	
	JSONArray items = new JSONArray();
	List<LogBean> dataList = am.queryForRecordSet(condition, pageInfo, true);
	if (dataList != null && !dataList.isEmpty()) {
		for (LogBean log : dataList) {
			JSONObject item = new JSONObject();
			item.put("startTime", log.getStartTime());
			item.put("updateTime", log.getUpdateTime());
			item.put("stage", log.getStage());
			item.put("value", FormatXml(log.getContent()));
			items.put(item);
		}
	}
	
	jso.put("items", items);
	jso.put("count", items.length());
	response.setContentType("text/json;charset=utf-8");
	return jso.toString();
}

private String FormatXml(String source) {
	if (source == null) source = "";
	String rs = source;
	try {
		org.dom4j.Document document = null;  
		document = DocumentHelper.parseText(source); 
		//格式化输出格式  
		OutputFormat format = OutputFormat.createPrettyPrint(); 
		format.setIndent("##");
		format.setEncoding(document.getXMLEncoding());
		StringWriter writer = new StringWriter();  
		//格式化输出流  
		XMLWriter xmlWriter = new XMLWriter(writer,format);
		//将document写入到输出流  
		xmlWriter.write(document);  
		xmlWriter.close();  
		//输出到控制台  
		return writer.toString();  
	} catch(Exception e) {
		return rs;
	}
}
%>

<%
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
		logger.error(t.getMessage(), t);
		responseWrapper.sendError(t.getMessage());
	}
	catch (Exception e) {
		logger.error(e.getMessage(), e);
		responseWrapper.sendError(e.getMessage());
	}
%>