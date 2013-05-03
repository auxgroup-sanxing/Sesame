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

<%@page import="com.ibm.wsdl.extensions.schema.*" %>
<%@page import="com.ibm.wsdl.extensions.soap.*"%>

<%@page import="com.sanxing.studio.*,com.sanxing.studio.utils.*"%>
<%@page import="com.sanxing.studio.emu.*"%>
<%@page import="com.sanxing.studio.team.*"%>

<%@page import="org.apache.commons.fileupload.*"%>
<%@page import="org.apache.commons.fileupload.disk.*"%>
<%@page import="org.apache.commons.fileupload.servlet.*"%>
<%@page import="org.slf4j.Logger, org.slf4j.LoggerFactory"%>
<%@page import="org.jdom.*,org.jdom.input.*,org.jdom.output.*,org.jdom.xpath.*"%>
<%@page import="org.json.*"%>
<%@page import="javax.management.*,javax.management.remote.*" %>
<%@page import="java.util.concurrent.*"%>
<%@page import="com.sanxing.adp.eclipse.ADPServiceProjectBuilder"%>
<%@page import="com.sanxing.sesame.sharelib.ShareLibManager"%>
<%@page import="com.sanxing.sesame.core.Platform"%>
<%@page language="java" contentType="text/json; charset=utf-8" pageEncoding="utf-8"%>

<%!
	private static final Namespace XMLNS_ART = Namespace.getNamespace("sn", Namespaces.SESAME);

	private static final String REV_WORKING	= "working";
	private static final String REV_HEAD	= "head";
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private MBeanServer mserver = null;
	
	private Map<String,ObjectInstance> objectInstanceCache = new ConcurrentHashMap<String,ObjectInstance>();
	
	public   void   jspInit()   {
		mserver = (MBeanServer)getServletContext().getAttribute("MBeanServer");
	} 
	
	 private Boolean checkStatus(String type, String name) throws Exception {
		if (mserver == null) {
			return false;
		}
		
	    ObjectName objName = ObjectName.getInstance(":Type="+type+",Name=" + name + ",*");
	    Set<ObjectInstance> set = mserver.queryMBeans(objName, null);
	    if (type.equals("component")) {
		    String state = "";
		    for (Iterator<ObjectInstance> it = set.iterator(); it.hasNext();) {
				ObjectInstance obj = it.next();
				state = (String) mserver.invoke(obj.getObjectName(), "getRunningStateFromStore", new Object[0], new String[0]);
		    }
		    return state.equals("Started");
	    }
	    else {
	    	return !set.isEmpty();
	    }
	}

	private void createUnitDescriptor(File unitFolder, String component, boolean binding) 
			throws FileNotFoundException,  IOException
	{
		Namespace xmlns = Namespace.getNamespace(Namespaces.JBI);
		Namespace xmlns_comp = Namespace.getNamespace("comp", component);
		Element rootEl = new Element("jbi", xmlns);
		rootEl.setAttribute("version", "1.0");
		rootEl.addNamespaceDeclaration(xmlns_comp);
		Element servicesEl = new Element("services", xmlns);
		rootEl.addContent(servicesEl);
		servicesEl.setAttribute("binding-component", String.valueOf(binding));
		OutputStream outStream = new FileOutputStream(new File(unitFolder, "jbi.xml"));
		try {
			JdomUtil.getPrettyOutputter().output(new Document(rootEl), outStream);
		}
		finally {
			outStream.close();
		}
	}

	private org.w3c.dom.Element createDocumentationEl(org.w3c.dom.Document doc, String documentation) {
		org.w3c.dom.Element element = doc.createElementNS(Namespaces.WSDL1_NAMESPACE, "documentation");
		//element.setPrefix("");
		org.w3c.dom.Text textNode = doc.createTextNode(documentation);
		element.appendChild(textNode);
		return element;
	}

	private File getFile(String filePath) {
		File file = Application.getWarehouseFile(filePath);
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

	private String getDocumentation(Document document) {
		try {
			Element rootEl = document.getRootElement();
			for (Iterator<?> iter = rootEl.getContent().iterator(); iter.hasNext();) {
				Content content = (Content) iter.next();
				if (content instanceof Comment)
					return ((Comment) content).getText();
				if (content instanceof Text)
					continue;
				break;
			}

			Element annoEl = rootEl.getChild("annotation", rootEl.getNamespace());
			if (annoEl != null) {
				return annoEl.getChildText("documentation", rootEl.getNamespace());
			}
			else {
				return rootEl.getChildText("documentation", rootEl.getNamespace());
			}
		}
		catch (Exception e) {
			return null;
		}
	}

	private String getJbiDescription(Document jbiDoc) {
		try {
			Element rootEl = jbiDoc.getRootElement();
			List<?> list = rootEl.getChildren();
			if (list.size() > 0) {
				Element idenEl, firstEl = (Element)list.get(0);
				if ((idenEl = firstEl.getChild("identification", rootEl.getNamespace())) != null) {
					return idenEl.getChildText("description", rootEl.getNamespace());
				}
			}
		}
		catch (Exception e) {
		}
		return null;
	}

	private String getDescription(JarFile jarFile) throws IOException {
		JarEntry entry = jarFile.getJarEntry("jbi.xml");
		if (entry != null) {
			try {
				Document document = new SAXBuilder().build(jarFile.getInputStream(entry));
				Element rootEl = document.getRootElement();
				Element idenEl, compEl = rootEl.getChild("component", rootEl.getNamespace());

				if (compEl != null && (idenEl = compEl.getChild("identification", rootEl.getNamespace())) != null) {
					return idenEl.getChildText("description", rootEl.getNamespace());
				}
			}
			catch (Exception e) {
			}
		}
		Attributes attrs = jarFile.getManifest().getMainAttributes();
		return attrs.getValue("Specification-Title");
	}
	

	public String loadComponents(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String project = request.getParameter("project");
		String type = request.getParameter("type");
		String role = request.getParameter("role");
		JSONArray result = new JSONArray();
		File compFolder = this.getFile(project + "/component");

		if (compFolder.exists()) {
			SAXBuilder builder = new SAXBuilder();
			File[] files = compFolder.listFiles();
			for (File entry : files) {
				Document document;
				File jbiFile;
				if (entry.isDirectory() && (jbiFile=new File(entry, "jbi.xml")).exists()) {
					document = builder.build(jbiFile);
				}else if (entry.getName().toLowerCase().endsWith(".jar")) {
					JarFile compJar = new JarFile(entry);
					JarEntry jbiXml = compJar.getJarEntry("jbi.xml");
					InputStream input = compJar.getInputStream(jbiXml);
					try {
						document = builder.build(input);
					}finally {
						input.close();
						compJar.close();
					}
				}else {
					continue;
				}
				
				Element rootEl = document.getRootElement();
				String label = null, name = null;
				Namespace ns = rootEl.getNamespace();
				Element compEl = rootEl.getChild("component", ns);
				if (compEl == null) continue;
				if (!compEl.getAttributeValue("type", "").equals(type)) continue;
				
				Element roleEl = compEl.getChild("role", rootEl.getNamespace("sn"));
				if (roleEl != null) {
					String roleStr = roleEl.getText();
					if (!role.equals(roleStr)) continue;
				}

				Element idenEl;
				if ((idenEl = compEl.getChild("identification", ns)) != null) {
					name = idenEl.getChildText("name", ns);
					label = idenEl.getChildText("description", ns);
				}
				
				JSONObject item = new JSONObject();
				item.put("component-name", name);
				item.put("component", "../component/" + entry.getName());
				item.put("label", label+"("+name+")");
				result.put(item);
			}
		}
		return new JSONObject().put("items", result).toString();
	}

	// 创建共享类库
	public String createPlugin(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String libName = request.getParameter("file");
		String version = request.getParameter("version");
		String desc = request.getParameter("desc");
		
		File shareLib = Application.getWarehouseFile("lib/" + libName);
		if (!shareLib.exists()) 
			shareLib.mkdirs();
		ShareLibManager slm = new ShareLibManager(shareLib.getAbsolutePath(), libName, version);
		slm.setDescription(desc);
		slm.addPathElement("");
		slm.persistence();
		
		return "true";
	}


	//资源库浏览器获取资源
	public String getResources(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String group = request.getParameter("group");
		JSONArray result = new JSONArray();
		File folder = Application.getWarehouseFile(group);
		if (folder.exists()) {
			String regex = request.getParameter("filter");
			Pattern pattern = regex != null && regex.length() > 0 ? Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
					: null;
			Pattern xmlPattern = Pattern.compile("^(.*)\\.(xml|xsd|wsdl)$", Pattern.CASE_INSENSITIVE);
			Pattern jarPattern = Pattern.compile("^(.*)\\.(jar|zip)$", Pattern.CASE_INSENSITIVE);

			SAXBuilder builder = new SAXBuilder();
			File[] entries = folder.listFiles();
			for (int j = 0; j < entries.length; j++) {
				File entry = entries[j];
				if (entry.getName().startsWith(".")) {
					continue;
				}
				
				if (pattern == null || pattern.matcher(entry.getName()).matches()) {
					JSONObject item = new JSONObject();
					String desc = "", filename = entry.getName();
					int lastIndex = filename.lastIndexOf(".");
					item.put("text", lastIndex > 0 ? filename.substring(0, lastIndex) : filename);
					try {
						if (entry.isDirectory()) {
							File file;
							if ((file=new File(entry, "jbi.xml")).exists()) {
								desc = this.getJbiDescription(builder.build(file));
							}
						}
						else if (entry.length() > 0) {
							try {
								if (xmlPattern.matcher(filename).matches())
									desc = this.getDocumentation(builder.build(entry));
								else if (jarPattern.matcher(filename).matches()) {
									JarFile jar = new JarFile(entry);
									try {	desc = this.getDescription(jar); } finally { jar.close();}
								}
							}
							catch (Exception e) {
							}
						}
					}
					catch (Exception e) {
					}
					
					//判断组件/共享库是否安装 
					String type = group.equals("components") ? "Component" : "SharedLibrary";
					item.put("checked", checkStatus(type, entry.getName()));
					
					item.put("name", entry.getName());
					item.put("desc", desc != null ? desc : "");
					item.put("uiProvider", "resource");
					item.put("leaf", true);
					Map version = SCM.getVersionInfo(entry);
					if (version != null) {
						item.put("url", version.get("url"));
						item.put("revision", version.get("committed-rev"));
						item.put("author", version.get("author"));
					}
					result.put(item);
				}
			}
		}
		return result.toString();
	}

	public String info(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String path = request.getParameter("path");
		boolean isAdp = path.indexOf("adp-ec") > -1;
		
		String result = "<b>位置</b>: "+path;
		File location = Application.getWarehouseFile(path);

		Map info = SCM.getVersionInfo(location);
		
		if (isAdp) {
			result += "<br><br><b>adp_base工程位置</b>: " + path + "/adp_base";
			if (info != null) {
		        result += "<b>版本库地址</b>: " + String.valueOf(info.get("url")).replaceAll("adp-ec", "adp-ec/adp_base");
		        result += "<br/><b>版本号</b>: 当前版本: "+ info.get("revision")+", 提交版本: "+info.get("committed-rev");
		        result += "<br/><b>提交者</b>: "+ info.get("author");
			}
		}
		else {
			if (info != null) {
		        result += "<br/><br/><b>版本库地址</b>: " + info.get("url");
		        result += "<br/><b>版本号</b>: 当前版本: "+ info.get("revision")+", 提交版本: "+info.get("committed-rev");
		        result += "<br/><b>提交者</b>: "+ info.get("author");
			}
		}
		
		return "<p style='background-color: white; padding:5px;'>"+result+"</p>";
	}
	
	
	public String remove(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String filePath = request.getParameter("file");
		File file = Application.getWarehouseFile(filePath);
		
		//从版本库中删除
		ThreeWaySynchronizer synchronizer = SCM.getSynchronizer(Application.getWarehouseRoot());
		if (synchronizer!=null && synchronizer.isVersioned(file)) {
			try {
				synchronizer.delete(file);
			}
			catch (Exception e) {
				logger.warn(e.getMessage());
			}
		}
		//删除本地物理文件
		if (file.isDirectory()) {
			deleteFiles(file);
		}
		if (!file.delete()) {
			throw new Exception("删除失败: \"" + file + "\" ");
		}
		return "true";
	}

	public String rename(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String f = request.getParameter("file");
		String newName = request.getParameter("newName");
		
		File file = this.getFile(f);
		String fileName = file.getName();
		int idx = fileName.lastIndexOf(".");
		String ext = idx>-1 ? fileName.substring(idx) : "";
		File newFile = new File(file.getParent(), newName + ext);

		//synchronizer
		ThreeWaySynchronizer synchronizer = SCM.getSynchronizer(Application.getWarehouseRoot());
		if (synchronizer != null) {
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

				String opera = (String) params.get("operation");
				if (opera == null) {
				}
				else if (opera.equals("uploadCom")) {
					String project = (String) params.get("project");
					String component = (String) params.get("component");
					FileItem uploadItem = (FileItem) params.get("comp-jar");
					if (uploadItem == null) {
						throw new RuntimeException("组件包上传失败，文件可能已被删除");
					}
					String filename = uploadItem.getName();
					
					int index = filename.lastIndexOf(File.separator);
					if (index > -1)
						filename = filename.substring(index + 1);
					if (!filename.endsWith(".jar")) {
						throw new RuntimeException("组件包必须是扩展名为 .jar 的文件");
					}
					
					ZipInputStream zipInput = new ZipInputStream(uploadItem.getInputStream());
					try {
						File compBundle = Application.getWarehouseFile("components/" + filename.replaceFirst("\\.jar$", ""));
						if (compBundle.exists()) {
							throw new Exception("组件已经存在，请选择其它组件");
						}
						FileUtil.buildDirectory(compBundle);
						
						FileUtil.unzip(zipInput, compBundle);
						
						// 读取组件描述
						File jbiFile = new File(compBundle,"jbi.xml");
						JSONArray jsoArray = new JSONArray();
						JSONObject jso = new JSONObject();
						jso.put("success", true);
						jso.put("text", compBundle.getName());
						if (jbiFile.exists()) {
							Document document = new SAXBuilder().build(jbiFile);
							Element identEl = (Element)XPath.selectSingleNode(document, "//*[name()='component']/*[name()='identification']");
							if (identEl == null) {
								FileUtil.deleteFile(compBundle);
								throw new RuntimeException("非法的组件描述符");
							}
							String name = identEl.getChildText("name", identEl.getNamespace());
							if (name != null) {
								jso.put("text", name);
								jso.put("file", name);
								File destPath = new File(compBundle.getParentFile(), name);
								if (!destPath.equals(compBundle)) {
									if (destPath.exists()) {
										FileUtil.deleteFile(compBundle);
										throw new RuntimeException("组件 "+name+" 已经存在");
									}
									else {
										if (!compBundle.renameTo(destPath)) {
											FileUtil.deleteFile(compBundle);
											throw new RuntimeException("组件保存失败");
										}
									}
								}
							}
							else {
								FileUtil.deleteFile(compBundle);
								throw new RuntimeException("没有设置组件名称");
							}
							String desc = identEl.getChildText("description", identEl.getNamespace());
							jso.put("desc", desc);
							jso.put("checked", false);
							jso.put("leaf", true);
							jso.put("uiProvider", "resource");
						}
						
						return jso.toString();
					}
					finally {
						zipInput.close();
					}
					//uploadItem.write(compFile);
					
				}
				else if (opera.equals("uploadLib")) {
					String project = (String) params.get("project");
					String component = (String) params.get("component");
					FileItem uploadItem = (FileItem) params.get("comp-jar");
					String filename = uploadItem.getName();
					
					int index = filename.lastIndexOf(File.separator);
					if (index > -1)
						filename = filename.substring(index + 1);
					if (!filename.endsWith(".jar")) {
						throw new Exception("组件包必须是扩展名为 .jar 的文件");
					}
					
					ZipInputStream zipInput = new ZipInputStream(uploadItem.getInputStream());
					try {
						File compBundle = Application.getWarehouseFile("lib/" + filename.replaceFirst("\\.jar$", ""));
						if (compBundle.exists()) {
							throw new Exception("共享库已经存在，请另行选择");
						}
						FileUtil.buildDirectory(compBundle);
						
						FileUtil.unzip(zipInput, compBundle);
						
						// 读取组件描述
						File jbiFile = new File(compBundle,"jbi.xml");
						JSONArray jsoArray = new JSONArray();
						JSONObject jso = new JSONObject();
						jso.put("success", true);
						jso.put("text", compBundle.getName());
						if (jbiFile.exists()) {
							Dom4jUtil.initDocument(jbiFile);
							org.dom4j.Element root = Dom4jUtil.getRootEl();
							org.dom4j.Element nameEl = 
									(org.dom4j.Element)root.selectSingleNode("//*[name()='shared-library']/*[name()='identification']/*[name()='name']");
							if (nameEl != null) {
								String name = nameEl.getText();
								jso.put("text", name);
								jso.put("file", name);
								File realComp = new File(compBundle.getParentFile().getAbsoluteFile() + File.separator + name);
								if (!realComp.exists())
									compBundle.renameTo(realComp);
							}
							org.dom4j.Element descEl = 
								(org.dom4j.Element)root.selectSingleNode("//*[name()='shared-library']/*[name()='identification']/*[name()='description']");
							String desc = descEl.getText();
							jso.put("desc", desc);
							jso.put("checked", false);
							jso.put("leaf", true);
							jso.put("uiProvider", "resource");
						}
						
						return jso.toString();
					}
					finally {
						zipInput.close();
					}
				}
				else if (opera.equals("uploadTransport")) {
					String project = (String) params.get("project");
					String component = (String) params.get("component");
					FileItem uploadItem = (FileItem) params.get("comp-jar");
					String filename = uploadItem.getName();
					
					int index = filename.lastIndexOf(File.separator);
					if (index > -1)
						filename = filename.substring(index + 1);
					if (!filename.endsWith(".jar")) {
						throw new Exception("类库必须是扩展名为 .jar 的文件");
					}
					File bundle = Configuration.getWorkspaceFile(project + "/transport/" + filename);
					if (bundle.exists()) {
						throw new Exception("类库已经存在，请重新选择");
					}
					uploadItem.write(bundle);
					
					// 读取组件描述
					JSONArray jsoArray = new JSONArray();
					JSONObject jso = new JSONObject();
					jso.put("success", true);
					jso.put("text", bundle.getName().replaceAll("\\.jar$", ""));
					String desc = "";
					JarFile jar = new JarFile(bundle);
					try {	desc = this.getDescription(jar); } finally { jar.close();}
					jso.put("desc", desc);
					jso.put("checked", false);
					jso.put("file", filename);
					jso.put("leaf", true);
					jso.put("uiProvider", "resource");
					
					return jso.toString();
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