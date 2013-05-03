
<%@page import="com.sanxing.studio.Configuration"%><%@page language="java" contentType="text/xml; charset=utf-8"%>

<%@page import="com.sanxing.studio.utils.*"%>
<%@page import="java.io.*, java.util.*, java.util.regex.*, java.net.*"%>
<%@page import="org.jdom.*, org.jdom.input.*, org.jdom.output.*, org.jdom.xpath.*, org.jdom.filter.Filter"%>
<%@page import="org.json.*"%>

<%!
private File getWorkareaFile(String filePath)
{
	return Configuration.getWorkspaceFile(filePath);
}

private void generateTarget(Element element, Element targetEl) throws JDOMException, IOException
{
	Element rootEl = element.getDocument().getRootElement();
	targetEl.setText(element.getTextTrim());
	String type = element.getAttributeValue("type");
	int index;
	if (type != null && (index=type.indexOf(":")) > -1) {
		String prefix=type.substring(0, index);
		if (prefix.equals("tns")) {
			Element typeEl = (Element)XPath.selectSingleNode(rootEl, "*[@name='"+type.substring(index+1)+"']");
			if (typeEl != null) {
				if (typeEl.getName().equals("simpleType")) {
				
				}
				else if (typeEl.getName().equals("complexType")) {
					Element el = new Element(typeEl.getName());
					targetEl.addContent(el);
					generateTarget(typeEl, el);
				}
			}
		}
		else {
			Namespace namespace = rootEl.getNamespace(prefix);
			if (namespace==null) return;
			
			XPath xpath = XPath.newInstance("ns:import[@namespace='"+namespace.getURI()+"']");
			xpath.addNamespace("ns", rootEl.getNamespaceURI());
			Element importEl = (Element)xpath.selectSingleNode(rootEl);
			if (importEl==null) return;
			URI uri = URI.create(importEl.getDocument().getBaseURI());
			URI importUri = uri.resolve(importEl.getAttributeValue("schemaLocation"));

			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(importUri.toURL());
			Element typeEl = (Element)XPath.selectSingleNode(document.getRootElement(), "*[@name='"+type.substring(index+1)+"']");
			if (typeEl != null) {
				Element el = new Element(typeEl.getName());
				targetEl.addContent(el);
				generateTarget(typeEl, el);
			}
		}
	}
	String ref = element.getAttributeValue("ref");
	if (ref != null && (index=ref.indexOf(":")) > -1) {
		String prefix=ref.substring(0, index);
		if (prefix.equals("tns")) {
			Element refEl = (Element)XPath.selectSingleNode(rootEl, "*[@name='"+ref.substring(index+1)+"']");
			if (refEl != null) {
				element.removeAttribute("ref");
				generateTarget(refEl, element);
			}
		}
		else {
			Namespace namespace = rootEl.getNamespace(prefix);
			if (namespace==null) return;
			
			XPath xpath = XPath.newInstance("ns:import[@namespace='"+namespace.getURI()+"']");
			xpath.addNamespace("ns", rootEl.getNamespaceURI());
			Element importEl = (Element)xpath.selectSingleNode(rootEl);
			if (importEl==null) return;
			URI uri = URI.create(importEl.getDocument().getBaseURI());
			URI importUri = uri.resolve(importEl.getAttributeValue("schemaLocation"));

			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(importUri.toURL());
			Element refEl = (Element)XPath.selectSingleNode(document.getRootElement(), "*[@name='"+type.substring(index+1)+"']");
			if (refEl != null) {
				element.removeAttribute("ref");
				generateTarget(refEl, element);
			}
		}
	}
	List<?> attributes = element.getAttributes();
	for (Iterator<?> iter=attributes.iterator(); iter.hasNext(); ) {
		Attribute attribute = (Attribute)iter.next();
		targetEl.setAttribute(attribute.getName(), attribute.getValue());
	}
	List<?> children = element.getChildren();
	for (Iterator<?> iter=children.iterator(); iter.hasNext(); ) {
		Element child = (Element)iter.next();
		Element el = new Element(child.getName());
		targetEl.addContent(el);
		generateTarget(child, el);
	}
}
%>

<%
String operation = request.getParameter("operation");
String schemaPath = request.getParameter("schema");

String method = request.getMethod();
if (method.equals("GET"))
{
	//String dc = request.getParameter("_dc");
	schemaPath = schemaPath != null ? new String(schemaPath.getBytes("iso8859-1"), "utf-8") : null;
	if (operation==null)
	{
	}
	else if (operation.equals("load"))
	{
		try
		{
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(getWorkareaFile(schemaPath));
			Element rootEl = document.getRootElement();
			
			Element element = rootEl.getChild("element", rootEl.getNamespace());
			if (element == null) {
				WebUtil.sendError(response, "非法的文档模型，没有根元素！");
				return;
			}
			Element rootElement = new Element("element");
			this.generateTarget(element, rootElement);
			Document doc = new Document(rootElement);
			XMLOutputter outputter = new XMLOutputter();
		    outputter.getFormat().setEncoding(response.getCharacterEncoding());
			out.clear();
			out.print(outputter.outputString(doc));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			WebUtil.sendError(response, e.getMessage());
		}
		finally
		{
		}
	}
}
else
{
	if (operation==null)
	{
	}
	else if (operation.equals("save"))
	{
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
			out.clear();
			out.print(true);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			WebUtil.sendError(response, e.getMessage());
		}
	}
	else if (operation.equals("loadSchemaTypes"))
	{
		File schemaFile = this.getWorkareaFile(schemaPath);
		String xmlns = request.getParameter("xmlns");
		String location = request.getParameter("location");
		File file = new File(this.getServletContext().getRealPath(xmlns.replaceFirst("http://", "cache/")+".xsd"));
		try
		{
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(file);
            List<?> list = XPath.selectNodes(document, "xs:schema/xs:simpleType[@id]");
            JSONArray items = new JSONArray();
            for (Iterator<?> iter=list.iterator();iter.hasNext();)
            {
            	Element el = (Element)iter.next();
            	JSONObject item = new JSONObject();
            	item.put("cls", "xsd-icon-simpletype");
            	item.put("type", el.getAttributeValue("name"));
            	items.put(item);
            }
            JSONObject loc = new JSONObject(location!=null ? location: "{}");
            for (Iterator<?> keys=loc.keys(); keys.hasNext(); ) {
            	String pref = (String)keys.next();
            	String path = loc.getString(pref);
            	
				file = new File(path);
            	if (!file.isAbsolute()) {
					file = new File(schemaFile.getParent()+"/"+path);
				}
            	if (!file.exists()) {
            		//System.out.println(file.getCanonicalPath());
					break;
            	}
				document = builder.build(file);
				Element rootEl = document.getRootElement();
				Namespace ns = rootEl.getNamespace();
				Element annEl, docuEl;
				list = rootEl.getChildren();
				for (Iterator<?> iter=list.iterator();iter.hasNext();)
				{
					Element el = (Element)iter.next();
					String tagName = el.getName();
					if (!tagName.equals("complexType") && !tagName.equals("simpletype")) continue;
					String docu = (annEl=el.getChild("annotation", ns))!=null && (docuEl=annEl.getChild("documentation", ns))!=null ? docuEl.getText() : "";
					JSONObject item = new JSONObject();
					item.put("cls", tagName.equals("complexType") ? "xsd-icon-complextype" : "xsd-icon-simpletype");
					item.put("pre", pref);
					item.put("type", el.getAttributeValue("name"));
					item.put("desc", docu);
					items.put(item);
				}
            }
            JSONObject data = new JSONObject();
            data.put("items", items);
			response.setContentType("text/json; charset=utf-8");
			out.clear();
			out.print(data);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			WebUtil.sendError(response, e.getMessage());
		}
	}
	else if (operation.equals("listFiles"))
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
					if (file.isFile() && regex!=null && regex.length()>0)
						return pattern.matcher(name).matches();
        	    	else 
            			return true;
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
			out.clear();
			out.print(data);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			WebUtil.sendError(response, "获取 Schema 文件列表 - "+e.getMessage());
		}
	}
	else {
		WebUtil.sendError(response, "不能识别的操作 operation:"+operation+"[POST]");
	}
}
%>