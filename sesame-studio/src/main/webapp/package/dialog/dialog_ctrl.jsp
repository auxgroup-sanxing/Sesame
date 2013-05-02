<%@page import="com.sanxing.ads.utils.*"%>
<%@page import="java.io.*, java.util.*, java.util.regex.*"%>
<%@page import="org.jdom.*, org.jdom.input.*, org.jdom.output.*, org.jdom.xpath.*, org.jdom.filter.Filter"%>
<%@page import="org.json.*"%>
<%@page import="com.sanxing.ads.*" %>
<%@page language="java" contentType="text/json; charset=utf-8"%>
<%!
private File getWorkareaFile(String path)
{
	return Configuration.getWorkspaceFile(path);
}
%>

<%
String operation = request.getParameter("operation");

String method = request.getMethod();
if (method.equals("GET"))
{
	if (operation==null)
	{
	}
}
else
{
	String path = request.getParameter("path");
	if (operation==null)
	{
		WebUtil.sendError(response, "必须指定 operation 参数");
	}
	else if (operation.equals("createFolder"))
	{
		String folderName = request.getParameter("name");
		File folder = this.getWorkareaFile(path);
		if (folder.exists()){
			boolean success = new File(folder.getAbsolutePath()+"/"+folderName).mkdir();
			if (success) 
				out.print(success);
			else
				WebUtil.sendError(response, "创建文件夹失败");
		}
		else {
			WebUtil.sendError(response, "指定的位置不存在");
		}
	}
	else if (operation.equals("listFiles"))
	{
		final String regex = request.getParameter("filter");
		try
		{
            JSONArray items = new JSONArray();
			File folder = this.getWorkareaFile(path);
			File[] list = folder.listFiles(new FilenameFilter(){
				private Pattern pattern = regex!=null && regex.length()>0 ? Pattern.compile(regex, Pattern.CASE_INSENSITIVE) : null;
				public boolean accept(File parent, String name){
					File file = new File(parent.getPath()+"/"+name);
					if (file.isFile() && pattern!=null)
						return pattern.matcher(name).matches();
        	    	else 
            			return true;
				}
			});
			//System.out.println(file);
			if (list != null) {
	            for (int i=0,len=list.length; i<len; i++)
	            {
	            	File f = list[i];
	            	JSONObject item = new JSONObject();
	            	item.put("cls", f.isDirectory()?"x-dialog-folder":"x-dialog-file");
	            	item.put("name", f.getName());
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
	else {
		WebUtil.sendError(response, "POST - 不能识别的操作 operation:"+operation);
	}
}
%>