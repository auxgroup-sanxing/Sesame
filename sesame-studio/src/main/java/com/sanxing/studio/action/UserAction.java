package com.sanxing.studio.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.KeyPair;
import java.util.Enumeration;
import java.util.Iterator;

import javax.crypto.Cipher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.pwd.PasswordTool;
import com.sanxing.studio.Application;
import com.sanxing.studio.Authentication;
import com.sanxing.studio.utils.HexBinary;
import com.sanxing.studio.utils.MessageException;
import com.sanxing.studio.utils.WebUtil;

public class UserAction
    extends HttpServlet
    implements Servlet
{
    private static final long serialVersionUID = 3L;

    private static final String RSA_ALGORITHM = "RSA/ECB/NoPadding";

    private static final Logger LOG = LoggerFactory.getLogger( UserAction.class );

    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        String action = request.getParameter( "action" );
        String userId = request.getParameter( "userid" );
        userId = new String( userId.getBytes( "ISO-8859-1" ), "UTF-8" );
        response.setContentType( "text/json; charset=utf-8" );
        PrintWriter out = response.getWriter();

        File file = getUserFile( getServletContext() );
        SAXBuilder builder = new SAXBuilder();
        try
        {
            if ( action != null )
            {
                if ( action.equals( "getAuth" ) )
                {
                    String type = request.getParameter( "type" );
                    String result = "";
                    Document document = builder.build( file );
                    Element root = document.getRootElement();
                    Element accredit =
                        (Element) XPath.selectSingleNode( root, "user[@userid='" + userId + "']/accredit[@type='"
                            + type + "']" );
                    if ( accredit != null )
                    {
                        result = accredit.getText();
                    }
                    out.write( result );
                }
                else
                {
                    LOG.info( getServletName() + " GET::" + action );
                    LOG.info( request.getQueryString() );
                }
            }
        }
        catch ( Exception e )
        {
            if ( !( e instanceof MessageException ) )
            {
                LOG.error( e.getMessage(), e );
            }
            WebUtil.sendError( response, e.getMessage() );
        }
        finally
        {
        }
    }

    public static File getUserFile( ServletContext servletContext )
    {
        return new File( servletContext.getRealPath( "WEB-INF/user.xml" ) );
    }

    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        request.setCharacterEncoding( "UTF-8" );
        String action = request.getParameter( "action" );
        String userId = request.getParameter( "userid" );

        response.setCharacterEncoding( "UTF-8" );
        File file = getUserFile( getServletContext() );
        SAXBuilder builder = new SAXBuilder();
        try
        {
            JSONObject currUser = Authentication.getCurrentUser();
            if ( action == null )
            {
                if ( ( currUser == null ) || ( !( currUser.get( "userlevel" ).equals( "S" ) ) ) )
                {
                    throw new ServletException( "您没有此操作权限！" );
                }
                String fullName = request.getParameter( "fullname" );
                String password = request.getParameter( "password" );
                String kind = request.getParameter( "kind" );

                KeyPair keyPair = Application.getKeyPair();
                Cipher cipher = Cipher.getInstance( "RSA/ECB/NoPadding" );

                cipher.init( Cipher.DECRYPT_MODE, keyPair.getPrivate() );

                byte[] bytes = HexBinary.decode( password );
                bytes = cipher.doFinal( bytes );
                password = new StringBuffer( new String( bytes ).trim() ).reverse().toString();
                password = PasswordTool.encrypt( password );

                Document document = builder.build( file );
                Element root = document.getRootElement();
                validateUser( userId, root );
                Element userEl = new Element( "user" );
                userEl.setAttribute( "userid", userId );
                userEl.setAttribute( "fullname", fullName );
                userEl.setAttribute( "passwd", password );
                userEl.setAttribute( "userlevel", kind );
                root.addContent( userEl );
                writeUserFile( document, file );
                response.sendRedirect( "user/UserHome.jsp" );
            }
            else if ( action.equals( "removeUser" ) )
            {
                if ( !( currUser.get( "userlevel" ).equals( "S" ) ) )
                {
                    WebUtil.sendError( response, "您没有删除权限！" );
                    return;
                }
                Document document = builder.build( file );
                Element root = document.getRootElement();
                Element userEl = (Element) XPath.selectSingleNode( root, "user[@userid='" + userId + "']" );
                if ( userEl != null )
                {
                    userEl.detach();
                    writeUserFile( document, file );
                }
                response.sendRedirect( "user/UserHome.jsp" );
            }
            else if ( action.equals( "changeName" ) )
            {
                if ( !( currUser.get( "userlevel" ).equals( "S" ) ) )
                {
                    WebUtil.sendError( response, "您没有修改权限！" );
                    return;
                }
                String fullName = request.getParameter( "fullname" );
                String oldid = request.getParameter( "oldid" );
                Document document = builder.build( file );
                Element root = document.getRootElement();
                if ( !( userId.equals( oldid ) ) )
                {
                    validateUser( userId, root );
                }
                Element userEl = (Element) XPath.selectSingleNode( root, "user[@userid='" + oldid + "']" );
                if ( userEl != null )
                {
                    userEl.setAttribute( "userid", userId );
                    userEl.setAttribute( "fullname", fullName );
                    writeUserFile( document, file );
                }
                response.sendRedirect( "user/UserMgr.jsp?userid=" + userId );
            }
            else if ( action.equals( "changePwd" ) )
            {
                if ( ( !( currUser.get( "userlevel" ).equals( "S" ) ) )
                    && ( !( currUser.get( "userid" ).equals( userId ) ) ) )
                {
                    WebUtil.sendError( response, "您没有操作权限！" );
                    return;
                }
                String currPwd = request.getParameter( "currpwd" );
                String newPwd = request.getParameter( "newpwd" );

                KeyPair keyPair = Application.getKeyPair();
                Cipher cipher = Cipher.getInstance( "RSA/ECB/NoPadding" );
                cipher.init( Cipher.DECRYPT_MODE, keyPair.getPrivate() );

                if ( currPwd != null )
                {
                    byte[] bytes = HexBinary.decode( currPwd );
                    bytes = cipher.doFinal( bytes );
                    currPwd = new StringBuffer( new String( bytes ).trim() ).reverse().toString();
                }
                byte[] bytes = HexBinary.decode( newPwd );
                bytes = cipher.doFinal( bytes );
                newPwd = new StringBuffer( new String( bytes ).trim() ).reverse().toString();
                newPwd = PasswordTool.encrypt( newPwd );

                Document document = builder.build( file );
                Element root = document.getRootElement();
                Element userEl = (Element) XPath.selectSingleNode( root, "user[@userid='" + userId + "']" );
                if ( userEl == null )
                {
                    WebUtil.sendError( response, "没有找到用户" );
                    return;
                }
                String passwd = userEl.getAttributeValue( "passwd" );
                passwd = PasswordTool.decrypt( passwd );
                if ( ( currPwd != null ) && ( !( currPwd.equals( passwd ) ) ) )
                {
                    WebUtil.sendError( response, "密码错误, 请重新输入" );
                    return;
                }
                userEl.setAttribute( "passwd", newPwd );
                writeUserFile( document, file );
                response.sendRedirect( "user/UserMgr.jsp?userid=" + userId );
            }
            else if ( action.equals( "changeKind" ) )
            {
                if ( !( currUser.get( "userlevel" ).equals( "S" ) ) )
                {
                    WebUtil.sendError( response, "您没有操作权限！" );
                    return;
                }
                String kind = request.getParameter( "kind" );
                Document document = builder.build( file );
                Element root = document.getRootElement();
                Element userEl = (Element) XPath.selectSingleNode( root, "user[@userid='" + userId + "']" );
                if ( userEl != null )
                {
                    userEl.setAttribute( "userlevel", kind );
                    writeUserFile( document, file );
                }
                response.sendRedirect( "user/UserMgr.jsp?userid=" + userId );
            }
            else if ( action.equals( "changeLogo" ) )
            {
                String userDsr = request.getParameter( "userdsr" );
                Document document = builder.build( file );
                Element root = document.getRootElement();
                Element userEl = (Element) XPath.selectSingleNode( root, "user[@userid='" + userId + "']" );
                if ( userEl != null )
                {
                    userEl.setAttribute( "userdsr", userDsr );
                    writeUserFile( document, file );
                }
                response.sendRedirect( "user/UserMgr.jsp?userid=" + userId );
            }
            else if ( action.equals( "changeRole" ) )
            {
                if ( !( currUser.get( "userlevel" ).equals( "S" ) ) )
                {
                    WebUtil.sendError( response, "您没有操作权限！" );
                    return;
                }
                JSONArray roleArray = new JSONArray( request.getParameter( "roles" ) );
                Document document = builder.build( file );
                Element root = document.getRootElement();
                Element userEl = (Element) XPath.selectSingleNode( root, "user[@userid='" + userId + "']" );
                if ( userEl != null )
                {
                    userEl.removeChildren( "role" );
                    int i = 0;
                    for ( int len = roleArray.length(); i < len; ++i )
                    {
                        String roleId = roleArray.getString( i );
                        userEl.addContent( new Element( "role" ).setAttribute( "id", roleId ) );
                    }
                    writeUserFile( document, file );
                }
                response.sendRedirect( "user/UserMgr.jsp?userid=" + userId );
            }
            else if ( action.equals( "createRole" ) )
            {
                if ( !( currUser.get( "userlevel" ).equals( "S" ) ) )
                {
                    WebUtil.sendError( response, "您没有操作权限！" );
                    return;
                }
                String roleId = request.getParameter( "roleid" );
                String roleName = request.getParameter( "rolename" );
                String desc = request.getParameter( "desc" );
                Document document = builder.build( file );
                Element root = document.getRootElement();

                validateRole( roleId, root );
                Element roles = root.getChild( "roles", root.getNamespace() );
                if ( roles == null )
                {
                    root.addContent( roles = new Element( "roles" ) );
                }
                Element roleEl = new Element( "role" );
                roles.addContent( roleEl );
                roleEl.setAttribute( "id", roleId );
                roleEl.setAttribute( "name", roleName );
                roleEl.setAttribute( "desc", desc );
                putAccredit( roleEl, request.getParameter( "accredit" ) );
                writeUserFile( document, file );
                response.sendRedirect( "user/RoleManager.jsp" );
            }
            else if ( action.equals( "modifyRole" ) )
            {
                if ( !( currUser.get( "userlevel" ).equals( "S" ) ) )
                {
                    WebUtil.sendError( response, "您没有操作权限！" );
                    return;
                }
                String roleId = request.getParameter( "roleid" );
                Document document = builder.build( file );
                Element root = document.getRootElement();
                Element roleEl = (Element) XPath.selectSingleNode( root, "roles/role[@id='" + roleId + "']" );
                if ( roleEl != null )
                {
                    roleEl.setAttribute( "name", request.getParameter( "rolename" ) );
                    roleEl.setAttribute( "desc", request.getParameter( "desc" ) );
                    putAccredit( roleEl, request.getParameter( "accredit" ) );
                    writeUserFile( document, file );
                }
                response.sendRedirect( "user/RoleManager.jsp" );
            }
            else if ( action.equals( "removeRole" ) )
            {
                if ( !( currUser.get( "userlevel" ).equals( "S" ) ) )
                {
                    WebUtil.sendError( response, "您没有操作权限！" );
                    return;
                }
                String roleId = request.getParameter( "roleid" );
                Document document = builder.build( file );
                Element root = document.getRootElement();
                Element roleEl = (Element) XPath.selectSingleNode( root, "roles/role[@id='" + roleId + "']" );
                if ( roleEl != null )
                {
                    roleEl.detach();
                    writeUserFile( document, file );
                }
                response.sendRedirect( "user/RoleManager.jsp" );
            }
            else
            {
                LOG.info( getServletName() + " POST::" + action );
                Enumeration params = request.getParameterNames();
                while ( params.hasMoreElements() )
                {
                    String param = (String) params.nextElement();
                    LOG.info( param + "=" + request.getParameter( param ) );
                }
                WebUtil.sendError( response, "未知操作 " + action );
            }
        }
        catch ( Exception e )
        {
            if ( !( e instanceof MessageException ) )
            {
                LOG.error( e.getMessage(), e );
            }
            WebUtil.sendError( response, e.getMessage() );
        }
        finally
        {
        }
    }

    private void putAccredit( Element roleEl, String accredit )
        throws JSONException, JDOMException
    {
        JSONObject accreditObj = new JSONObject( accredit );
        JSONObject subObj;
        Element accreditEl;
        Iterator keys;
        if ( accreditObj.has( "menu" ) )
        {
            subObj = accreditObj.getJSONObject( "menu" );
            accreditEl = (Element) XPath.selectSingleNode( roleEl, "accredit[@type='menu']" );
            if ( accreditEl == null )
            {
                roleEl.addContent( accreditEl = new Element( "accredit" ).setAttribute( "type", "menu" ) );
            }
            accreditEl.removeContent();
            for ( keys = subObj.keys(); keys.hasNext(); )
            {
                String key = (String) keys.next();
                accreditEl.addContent( new Element( key ).setText( subObj.getString( key ) ) );
            }
        }
        if ( accreditObj.has( "view" ) )
        {
            subObj = accreditObj.getJSONObject( "view" );
            accreditEl = (Element) XPath.selectSingleNode( roleEl, "accredit[@type='view']" );
            if ( accreditEl == null )
            {
                roleEl.addContent( accreditEl = new Element( "accredit" ).setAttribute( "type", "view" ) );
            }
            accreditEl.removeContent();
            for ( keys = subObj.keys(); keys.hasNext(); )
            {
                String key = (String) keys.next();
                accreditEl.addContent( new Element( key ).setText( subObj.getString( key ) ) );
            }
        }
        if ( !( accreditObj.has( "edit" ) ) )
        {
            return;
        }
        subObj = accreditObj.getJSONObject( "edit" );
        accreditEl = (Element) XPath.selectSingleNode( roleEl, "accredit[@type='edit']" );
        if ( accreditEl == null )
        {
            roleEl.addContent( accreditEl = new Element( "accredit" ).setAttribute( "type", "edit" ) );
        }
        accreditEl.removeContent();
        for ( keys = subObj.keys(); keys.hasNext(); )
        {
            String key = (String) keys.next();
            accreditEl.addContent( new Element( key ).setText( subObj.getString( key ) ) );
        }
    }

    private void validateUser( String userId, Element root )
        throws Exception
    {
        Element userEl = (Element) XPath.selectSingleNode( root, "user[@userid='" + userId + "']" );
        if ( userEl == null )
        {
            return;
        }
        throw new MessageException( "输入的帐户名称已存在，请重新输入" );
    }

    private void validateRole( String roleId, Element root )
        throws Exception
    {
        Element roleEl = (Element) XPath.selectSingleNode( root, "roles/role[@id='" + roleId + "']" );
        if ( roleEl == null )
        {
            return;
        }
        throw new MessageException( "输入的角色标识已存在，请重新输入" );
    }

    private void writeUserFile( Document document, File file )
        throws IOException
    {
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat( Format.getPrettyFormat().setEncoding( "GBK" ).setIndent( "  " ) );
        FileOutputStream outStream = new FileOutputStream( file );
        outputter.output( document, outStream );
        outStream.close();
    }
}