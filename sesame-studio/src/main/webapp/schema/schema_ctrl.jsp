<%@page language="java" contentType="text/xml; charset=utf-8"%>

<%@page
	import="com.sanxing.studio.*,com.sanxing.studio.utils.*,com.sanxing.studio.team.svn.*,com.sanxing.studio.team.*"%>
<%@page import="java.io.*, java.util.*, java.util.regex.*"%>
<%@page import="java.lang.reflect.*"%>
<%@page import="javax.xml.namespace.QName"%>
<%@page import="org.slf4j.Logger, org.slf4j.LoggerFactory"%>
<%@page import="org.apache.ws.commons.schema.*"%>
<%@page
	import="org.jdom.*, org.jdom.input.*, org.jdom.output.*, org.jdom.xpath.*, org.jdom.filter.Filter"%>
<%@page import="org.json.*"%>

<%!

/**
* 获取 XML Schema 对象的描述信息
*/
private String getDocumentation(XmlSchemaDocumentation docu)
{
	org.w3c.dom.NodeList  list = docu.getMarkup();
	if (list.getLength() > 0) {
		StringBuffer buf = new StringBuffer();
		for (int i=0, len=list.getLength(); i<len; i++) {
			org.w3c.dom.Node node = list.item(i);
			buf.append(node.getNodeValue()!=null ? node.getNodeValue() : "");
		}
		return buf.toString();
	}
	else {
		return "";
	}
}

/**
*获取schema中定义的类型， 结果放到 array 参数中
**/
@SuppressWarnings("unchecked")
private void getSchemaTypes(XmlSchema schema, String prefix, JSONArray array) throws JSONException {
	XmlSchemaObjectTable schemaTypes = schema.getSchemaTypes();
	for (Iterator<XmlSchemaType> iter = schemaTypes.getValues(); iter.hasNext(); ) {
		XmlSchemaType schemaType = iter.next();
		QName qname = schemaType.getQName();

		JSONObject item = new JSONObject();
		item.put("cls", "xsd-icon-"+(schemaType instanceof XmlSchemaSimpleType?"simpletype":"complextype"));
		item.put("type", qname.getLocalPart());
		if (prefix != null) {
			item.put("pre", prefix);
		}
		String desc = null;
		if (schemaType.getAnnotation() != null) {
			XmlSchemaObjectCollection annColl = schemaType.getAnnotation().getItems();
			org.w3c.dom.Element formatEl = null;
			for (Iterator<?> it = annColl.getIterator(); it.hasNext(); ) {
				Object ann = it.next();
				if (ann instanceof XmlSchemaDocumentation) {
					desc = getDocumentation((XmlSchemaDocumentation)ann);
				}
				else if (ann instanceof XmlSchemaAppInfo) {
					//formatEl = getFormat((XmlSchemaAppInfo)ann);
				}
			}
		}
		item.put("desc", desc);
		array.put(item);
	}
	
}

private File getWorkareaFile(String filePath)
{
	return Configuration.getWorkspaceFile(filePath);
}

public String load(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String schemaPath = request.getParameter("schema");
	try
	{
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(getWorkareaFile(schemaPath));
		Element rootEl = document.getRootElement();
		
		List<?> list = rootEl.getChildren("import", rootEl.getNamespace());
		List<?> nsList = rootEl.getAdditionalNamespaces();
		for (Iterator<?> iter=list.iterator(); iter.hasNext(); ) {
			Element importEl=(Element)iter.next();
			for (Iterator<?> it=nsList.iterator(); it.hasNext(); ) {
				Namespace ns = (Namespace)it.next();
				if (ns.getURI().equals(importEl.getAttributeValue("namespace"))) {
					importEl.setAttribute("prefix", ns.getPrefix());
				}
			}
		}
		XMLOutputter outputter = new XMLOutputter();
	    outputter.getFormat().setEncoding(response.getCharacterEncoding());
		return outputter.outputString(document);
		//BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(), "UTF-8")); 
		//new FileReader(getSchemaFile(schemaPath)));
		//for (String line=null; (line=reader.readLine())!=null;) {
		//	out.println(line);
		//}
		//reader.close();
	}
	finally
	{
	}
}

//锁定操作
@SuppressWarnings("unchecked")
public String schemaLock(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String schema = request.getParameter("schema");
	
	File xsdFile = new File(Configuration.getWorkspaceRoot(),schema);
	File projectFolder = xsdFile.getParentFile().getParentFile();
	ThreeWaySynchronizer sync = SCM.getSynchronizer(projectFolder);
	Map props = sync.info(xsdFile);
	String lock = (String)props.get("lock");
	if(null == lock){
		sync.lock(xsdFile,"");
	}else{
		JSONObject user = Authentication.getCurrentUser();
		String userName = user.optString("userid");
		if(userName.equals((String)props.get("lock.owner")))
			return "success";
		else
			throw new Exception("数据字典文件:["+xsdFile.getAbsolutePath()+"],已经被用户：[:"+props.get("lock.owner")+"]锁定!");
	}
	sync.update(xsdFile,0);
	response.setContentType("text/plain;charset=utf-8");
	return "success";
}
//解锁操作
@SuppressWarnings("unchecked")
public String schemaUnlock(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String schema = request.getParameter("schema");
	File xsdFile = new File(Configuration.getWorkspaceRoot(),schema);
	File projectFolder = xsdFile.getParentFile().getParentFile();
	
	ThreeWaySynchronizer sync = SCM.getSynchronizer(projectFolder);
	if(null == sync ||(!sync.isVersioned(xsdFile)))
		return "success";
	Map props = sync.info(xsdFile);
	String lock = (String)props.get("lock");
	if(lock != null){
	    try{
			sync.commit(new File[]{xsdFile}, "Auto commit by studio");
	    }
		catch (Exception e) {
			//e.printStackTrace();
		}
		
		props = sync.info(xsdFile);
		lock = (String)props.get("lock");
		if(lock != null) {
			sync.unlock(xsdFile, false);
		}
	}
	response.setContentType("text/plain;charset=utf-8");
	return "success";
}
//TODO NEW ADD END



public String save(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String schemaPath = request.getParameter("schema");
	String data = request.getParameter("data");
	try
	{
		SAXBuilder builder = new SAXBuilder();
		Reader reader= new StringReader(data);
		Document document = builder.build(reader);
		Element rootEl = document.getRootElement();
		Namespace xs=Namespace.getNamespace(Namespaces.XSD);
		rootEl.setNamespace(xs);
		rootEl.addNamespaceDeclaration(Namespace.getNamespace("tns", rootEl.getAttributeValue("targetNamespace")));
		for (Iterator<?> iter=rootEl.getDescendants(); iter.hasNext(); ) {
			Object obj = iter.next();
			if (obj instanceof Element) ((Element)obj).setNamespace(xs);
		}
		List<?> list = rootEl.getChildren("import", xs);
		for (Iterator<?> iter=list.iterator(); iter.hasNext(); ) {
			Element el = (Element)iter.next();
			String prefix = el.getAttributeValue("prefix");
			el.removeAttribute("prefix");
			rootEl.addNamespaceDeclaration(Namespace.getNamespace(prefix, el.getAttributeValue("namespace")));
		}
        
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat(Format.getPrettyFormat().setEncoding("utf-8").setIndent("  "));
        FileOutputStream outStream = new FileOutputStream(this.getWorkareaFile(schemaPath));
        outputter.output(document, outStream);
        outStream.close();

		response.setContentType("text/json; charset=utf-8");
		return "true";
	}
	finally
	{
	}
}

public String loadSchemaTypes(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String schemaPath = request.getParameter("schema");
	File schemaFile = this.getWorkareaFile(schemaPath);
	String xmlns = request.getParameter("xmlns");
	String location = request.getParameter("location");
	
	
	XmlSchemaCollection schemaColl = new XmlSchemaCollection();

	JSONArray items = new JSONArray();
	XmlSchema[] schemas = schemaColl.getXmlSchemas();
	for (XmlSchema schema : schemas) {
		getSchemaTypes(schema, null, items);
	}
	
	JSONObject loc = new JSONObject(location!=null ? location: "{}");
	for (Iterator<?> keys=loc.keys(); keys.hasNext(); ) {
		String pref = (String)keys.next();
		String path = loc.getString(pref);
        	
		File file = new File(path);
        if (!file.isAbsolute()) {
			file = new File(schemaFile.getParent()+"/"+path);
		}
        if (!file.exists()) {
			//System.out.println(file.getCanonicalPath());
			break;
        }
        XmlSchema schema = schemaColl.read(new FileReader(file), null);
		getSchemaTypes(schema, pref, items);
	}
	
	JSONObject result = new JSONObject();
	result.put("items", items);
	response.setContentType("text/json; charset=utf-8");
	return result.toString();
}

public String listFiles(HttpServletRequest request, HttpServletResponse response) throws Exception
{
	String path = request.getParameter("path");
	final String regex = request.getParameter("filter");
	try
	{
        JSONArray items = new JSONArray();
		File folder = this.getWorkareaFile(path);
		File[] list = folder.listFiles(new FilenameFilter(){
			private Pattern pattern = regex!=null && regex.length()>0 ? Pattern.compile(regex, Pattern.CASE_INSENSITIVE) : null;
			public boolean accept(File parent, String name){
				File file = new File(parent.getPath()+"/"+name);
				if (file.isFile() && regex!=null && regex.length()>0) {
					return pattern.matcher(name).matches();
				}
				else {
        			return !name.startsWith(".");
				}
			}
		});
		if (list != null) {
			SAXBuilder builder = new SAXBuilder();
            for (int i=0,len=list.length; i<len; i++)
            {
            	File f = list[i];
            	JSONObject item = new JSONObject();
            	item.put("cls", f.isDirectory()?"x-dialog-folder":"x-dialog-file");
            	item.put("name", f.getName());
            	if (f.isFile()) {
					Document document = builder.build(f);
					Element rootEl = document.getRootElement();
					item.put("content", rootEl.getAttributeValue("targetNamespace"));
				}
            	items.put(item);
            }
        }
        JSONObject data = new JSONObject();
        data.put("items", items);
		response.setContentType("text/json; charset=utf-8");
		return data.toString();
	}
	finally
	{
	}
}

%>

<%
Logger logger = LoggerFactory.getLogger(this.getClass());

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