package com.sanxing.studio.action;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.json.JSONObject;

import com.sanxing.studio.Authentication;
import com.sanxing.studio.ConnDispenser;
import com.sanxing.studio.utils.CommonUtil;

public class TableEditOparation
    extends HttpServlet
{
    private static final long serialVersionUID = 7492901959300218619L;

    public String useridname = "";

    @Override
    public void destroy()
    {
        super.destroy();
    }

    @Override
    public void doGet( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        response.setContentType( "text/json; charset=utf-8" );

        PrintWriter out = response.getWriter();
    }

    public String getValue( String kindid, String value, Connection conn )
    {
        String resultvalue = "";
        Statement stmt = null;
        ResultSet rs = null;
        if ( kindid.indexOf( ":" ) >= 0 )
        {
            String[] kindids = kindid.split( "," );
            for ( int i = 0; i < kindids.length; ++i )
            {
                if ( !( kindids[i].substring( 0, kindids[i].indexOf( ":" ) ).equals( value ) ) )
                {
                    continue;
                }
                resultvalue = kindids[i].substring( kindids[i].indexOf( ":" ) + 1, kindids[i].length() );

                break;
            }
        }
        else
        {
            try
            {
                stmt = conn.createStatement();
                rs = stmt.executeQuery( kindid );
                while ( rs.next() )
                {
                    if ( rs.getString( 1 ).equals( value ) )
                    {
                        ;
                    }
                    resultvalue = rs.getString( 2 );
                }
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
            }
            finally
            {
                try
                {
                    if ( rs != null )
                    {
                        rs.close();
                    }
                    if ( stmt != null )
                    {
                        stmt.close();
                    }
                }
                catch ( Exception ex1 )
                {
                }
            }
        }
        return resultvalue;
    }

    public void insertLog( Connection con, String userid, String table_name, String field_name, String main_content,
                           String before_modify, String after_modify )
    {
        Statement stmt = null;
        ResultSet rs = null;
        try
        {
            stmt = con.createStatement();
            String sql = "select max(logno)+1 from oper_log";
            rs = stmt.executeQuery( sql );
            String act_date = new SimpleDateFormat( "yyyyMMdd" ).format( new Date() );

            String act_time = new SimpleDateFormat( "HH:mm:ss" ).format( new Date() );

            String count = "";
            if ( rs.next() )
            {
                count = rs.getString( 1 );
            }
            if ( ( count == null ) || ( count.equals( "" ) ) || ( count.equals( "null" ) ) )
            {
                count = "1";
            }
            sql =
                "insert into oper_log (logno,userid,act_date,act_time,table_name,field_name,main_content,before_modify,after_modify) values ("
                    + count
                    + ",'"
                    + userid
                    + "','"
                    + act_date
                    + "','"
                    + act_time
                    + "','"
                    + table_name
                    + "',"
                    + "'"
                    + field_name + "','" + main_content + "','" + before_modify + "','" + after_modify + "')";

            stmt.execute( sql );
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
        finally
        {
            try
            {
                if ( rs != null )
                {
                    rs.close();
                }
                if ( stmt != null )
                {
                    stmt.close();
                }
            }
            catch ( Exception ex1 )
            {
            }
        }
    }

    public String getTablelocalname( String tablename, HttpServletResponse response )
        throws IOException
    {
        try
        {
            File file = new File( getServletContext().getRealPath( "tablegroup/table.xml" ) );

            SAXBuilder builder = CommonUtil.newSAXBuilder();
            Document document = builder.build( file );
            Element root = document.getRootElement();
            Element userEl = (Element) XPath.selectSingleNode( root, "table[@id='" + tablename + "']" );

            return userEl.getAttributeValue( "localname" );
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
            response.sendError( 500, ex.getMessage() );
        }
        return null;
    }

    @Override
    public void doPost( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        response.setContentType( "text/json; charset=utf-8" );
        request.setCharacterEncoding( "utf-8" );
        PrintWriter out = response.getWriter();
        String action = request.getParameter( "action" );
        String tablename = request.getParameter( "tablename" );
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        String returnstring = "";
        try
        {
            JSONObject user1 = Authentication.getCurrentUser();
            useridname = ( (String) user1.get( "userid" ) );

            conn = ConnDispenser.getConnection();
            stmt = conn.createStatement();

            JSONObject user = Authentication.getCurrentUser();
            if ( action.equals( "delete" ) )
            {
                String shouquan = request.getParameter( "shouquan" );
                if ( shouquan.equals( "001" ) )
                {
                    String cuserid = request.getParameter( "cuserid" );
                    String cpassword = request.getParameter( "cpassword" );

                    if ( !( checkUser( cuserid, cpassword, "C" ) ) )
                    {
                        out.println( "[shouquanerror]" );
                        return;
                    }
                }
                else if ( shouquan.equals( "002" ) )
                {
                    String buserid = request.getParameter( "buserid" );
                    String bpassword = request.getParameter( "bpassword" );
                    if ( !( checkUser( buserid, bpassword, "B" ) ) )
                    {
                        out.println( "[shouquanerror]" );
                        return;
                    }
                }
                else if ( shouquan.equals( "003" ) )
                {
                    String auserid = request.getParameter( "auserid" );
                    String apassword = request.getParameter( "apassword" );
                    if ( !( checkUser( auserid, apassword, "A" ) ) )
                    {
                        out.println( "[shouquanerror]" );
                        return;
                    }
                }
                else if ( shouquan.equals( "004" ) )
                {
                    String cuserid = request.getParameter( "cuserid" );
                    String cpassword = request.getParameter( "cpassword" );
                    String buserid = request.getParameter( "buserid" );
                    String bpassword = request.getParameter( "bpassword" );
                    if ( ( !( checkUser( cuserid, cpassword, "C" ) ) ) || ( !( checkUser( buserid, bpassword, "B" ) ) ) )
                    {
                        out.println( "[shouquanerror]" );
                        return;
                    }
                }
                else if ( shouquan.equals( "005" ) )
                {
                    String cuserid = request.getParameter( "cuserid" );
                    String cpassword = request.getParameter( "cpassword" );
                    String buserid = request.getParameter( "buserid" );
                    String bpassword = request.getParameter( "bpassword" );
                    if ( ( !( checkUser( cuserid, cpassword, "C" ) ) ) || ( !( checkUser( buserid, bpassword, "A" ) ) ) )
                    {
                        out.println( "[shouquanerror]" );
                        return;
                    }
                }
                else if ( shouquan.equals( "006" ) )
                {
                    String cuserid = request.getParameter( "cuserid" );
                    String cpassword = request.getParameter( "cpassword" );
                    String buserid = request.getParameter( "buserid" );
                    String bpassword = request.getParameter( "bpassword" );
                    if ( ( !( checkUser( cuserid, cpassword, "C" ) ) ) || ( !( checkUser( buserid, bpassword, "C" ) ) ) )
                    {
                        out.println( "[shouquanerror]" );
                        return;
                    }
                }
                else if ( shouquan.equals( "007" ) )
                {
                    String cuserid = request.getParameter( "cuserid" );
                    String cpassword = request.getParameter( "cpassword" );
                    String buserid = request.getParameter( "buserid" );
                    String bpassword = request.getParameter( "bpassword" );
                    if ( ( !( checkUser( cuserid, cpassword, "B" ) ) ) || ( !( checkUser( buserid, bpassword, "A" ) ) ) )
                    {
                        out.println( "[shouquanerror]" );
                        return;
                    }
                }
                String deletedata = request.getParameter( "deletedata" );
                String sql = "delete from " + tablename + " where " + deletedata;

                stmt.execute( sql );
                out.println( "[ok]" );
            }
            else if ( action.equals( "deleteall" ) )
            {
                String deletedata = request.getParameter( "deletedata" );
                String sql = "delete from " + tablename;
                stmt.execute( sql );
            }
            else if ( action.equals( "sqlcheck" ) )
            {
                String sql = request.getParameter( "sql" );
                String space = request.getParameter( "space" );
                String errorinfo = request.getParameter( "errorinfo" );

                if ( ( space == null ) || ( space.equals( "" ) ) || ( space.equals( "null" ) ) )
                {
                    space = ",";
                }
                if ( ( errorinfo == null ) || ( errorinfo.equals( "" ) ) || ( errorinfo.equals( "null" ) ) )
                {
                    errorinfo = "no found";
                }
                rs = stmt.executeQuery( sql );
                ResultSetMetaData metadata = rs.getMetaData();
                if ( rs.next() )
                {
                    String result = "";
                    for ( int i = 1; i <= metadata.getColumnCount(); ++i )
                    {
                        if ( i == 1 )
                        {
                            result = rs.getString( i );
                        }
                        else
                        {
                            result = result + space + rs.getString( i );
                        }
                    }
                    out.println( "[" + result + "]" );
                }
                else
                {
                    out.println( "[" + errorinfo + "]" );
                }
            }
            else if ( action.equals( "update" ) )
            {
                String where = request.getParameter( "where" );
                String index = request.getParameter( "index" );
                String[] indexs = index.split( "," );
                String column = "";
                String wherecheck = "";
                String updatebefore = "";
                String updateafter = "";
                String shouquan = request.getParameter( "shouquan" );
                if ( shouquan.equals( "001" ) )
                {
                    String cuserid = request.getParameter( "cuserid" );
                    String cpassword = request.getParameter( "cpassword" );
                    if ( !( checkUser( cuserid, cpassword, "C" ) ) )
                    {
                        out.println( "[shouquanerror]" );
                        return;
                    }
                }
                else if ( shouquan.equals( "002" ) )
                {
                    String buserid = request.getParameter( "buserid" );
                    String bpassword = request.getParameter( "bpassword" );
                    if ( !( checkUser( buserid, bpassword, "B" ) ) )
                    {
                        out.println( "[shouquanerror]" );
                        return;
                    }
                }
                else if ( shouquan.equals( "003" ) )
                {
                    String auserid = request.getParameter( "auserid" );
                    String apassword = request.getParameter( "apassword" );
                    if ( !( checkUser( auserid, apassword, "A" ) ) )
                    {
                        out.println( "[shouquanerror]" );
                        return;
                    }
                }
                else if ( shouquan.equals( "004" ) )
                {
                    String cuserid = request.getParameter( "cuserid" );
                    String cpassword = request.getParameter( "cpassword" );
                    String buserid = request.getParameter( "buserid" );
                    String bpassword = request.getParameter( "bpassword" );
                    if ( ( !( checkUser( cuserid, cpassword, "C" ) ) ) || ( !( checkUser( buserid, bpassword, "B" ) ) ) )
                    {
                        out.println( "[shouquanerror]" );
                        return;
                    }
                }
                else if ( shouquan.equals( "005" ) )
                {
                    String cuserid = request.getParameter( "cuserid" );
                    String cpassword = request.getParameter( "cpassword" );
                    String buserid = request.getParameter( "buserid" );
                    String bpassword = request.getParameter( "bpassword" );
                    if ( ( !( checkUser( cuserid, cpassword, "C" ) ) ) || ( !( checkUser( buserid, bpassword, "A" ) ) ) )
                    {
                        out.println( "[shouquanerror]" );
                        return;
                    }
                }
                else if ( shouquan.equals( "006" ) )
                {
                    String cuserid = request.getParameter( "cuserid" );
                    String cpassword = request.getParameter( "cpassword" );
                    String buserid = request.getParameter( "buserid" );
                    String bpassword = request.getParameter( "bpassword" );
                    if ( ( !( checkUser( cuserid, cpassword, "C" ) ) ) || ( !( checkUser( buserid, bpassword, "C" ) ) ) )
                    {
                        out.println( "[shouquanerror]" );
                        return;
                    }
                }
                else if ( shouquan.equals( "007" ) )
                {
                    String cuserid = request.getParameter( "cuserid" );
                    String cpassword = request.getParameter( "cpassword" );
                    String buserid = request.getParameter( "buserid" );
                    String bpassword = request.getParameter( "bpassword" );
                    if ( ( !( checkUser( cuserid, cpassword, "B" ) ) ) || ( !( checkUser( buserid, bpassword, "A" ) ) ) )
                    {
                        out.println( "[shouquanerror]" );
                        return;
                    }
                }
                for ( int i = 0; i < indexs.length; ++i )
                {
                    if ( i == 0 )
                    {
                        column = indexs[i].substring( 0, indexs[i].indexOf( ":" ) );
                        if ( indexs[i].substring( indexs[i].indexOf( ":" ) + 1, indexs[i].lastIndexOf( ":" ) ).equals(
                            "string" ) )
                        {
                            wherecheck =
                                indexs[i].substring( 0, indexs[i].indexOf( ":" ) ) + "='"
                                    + request.getParameter( indexs[i].substring( 0, indexs[i].indexOf( ":" ) ) ) + "'";
                        }
                        else
                        {
                            wherecheck =
                                indexs[i].substring( 0, indexs[i].indexOf( ":" ) ) + "="
                                    + request.getParameter( indexs[i].substring( 0, indexs[i].indexOf( ":" ) ) );
                        }

                    }
                    else
                    {
                        column = column + "," + indexs[i].substring( 0, indexs[i].indexOf( ":" ) );

                        if ( indexs[i].substring( indexs[i].indexOf( ":" ) + 1, indexs[i].lastIndexOf( ":" ) ).equals(
                            "string" ) )
                        {
                            wherecheck =
                                wherecheck + " and " + indexs[i].substring( 0, indexs[i].indexOf( ":" ) ) + "='"
                                    + request.getParameter( indexs[i].substring( 0, indexs[i].indexOf( ":" ) ) ) + "'";
                        }
                        else
                        {
                            wherecheck =
                                wherecheck + " and " + indexs[i].substring( 0, indexs[i].indexOf( ":" ) ) + "="
                                    + request.getParameter( indexs[i].substring( 0, indexs[i].indexOf( ":" ) ) );
                        }

                    }

                }

                String sqlcheck = "select " + column + " from " + tablename + " where " + where;

                rs = stmt.executeQuery( sqlcheck );
                boolean checkflag = false;
                if ( rs.next() )
                {
                    String line = "";
                    for ( int i = 0; i < indexs.length; ++i )
                    {
                        line = indexs[i].substring( 0, indexs[i].indexOf( ":" ) );
                        if ( rs.getString( line ).equals( request.getParameter( line ) ) )
                        {
                            continue;
                        }
                        checkflag = true;
                        break;
                    }
                }

                if ( checkflag )
                {
                    sqlcheck = "select count(*) from " + tablename + " where " + wherecheck;

                    rs = stmt.executeQuery( sqlcheck );
                    if ( ( rs.next() ) && ( rs.getInt( 1 ) != 0 ) )
                    {
                        out.println( "[no]" );
                        return;
                    }
                }

                Element tableEl = null;
                List list = getElement( getServletContext().getRealPath( "table/" + tablename + ".xml" ) );

                int row = 0;
                String sql = "";
                for ( Iterator iter = list.iterator(); iter.hasNext(); )
                {
                    tableEl = (Element) iter.next();
                    List attributes = tableEl.getAttributes();
                    String name = "";
                    String formitem = "";
                    String type = "";
                    String kindid = "";
                    Iterator at = attributes.iterator();
                    while ( at.hasNext() )
                    {
                        Attribute attr = (Attribute) at.next();
                        if ( attr.getName().equals( "name" ) )
                        {
                            name = attr.getValue();
                        }
                        if ( attr.getName().equals( "formitem" ) )
                        {
                            formitem = attr.getValue();
                        }
                        if ( attr.getName().equals( "type" ) )
                        {
                            type = attr.getValue();
                        }
                        if ( attr.getName().equals( "kindid" ) )
                        {
                            kindid = attr.getValue();
                        }
                    }
                    if ( !( formitem.equals( "checkbox" ) ) )
                    {
                        String canshu = request.getParameter( name );
                        String canshuold = request.getParameter( name + "old" );
                        String canshu1 = "";
                        if ( !( formitem.equals( "text" ) ) )
                        {
                            canshu1 = canshu + "--" + getValue( kindid, canshu, conn );
                        }
                        else
                        {
                            canshu1 = canshu;
                        }
                        if ( ( canshuold != null )
                            && ( !( canshuold.substring( canshuold.indexOf( ":" ) + 1, canshuold.length() ).equals( canshu ) ) ) )
                        {
                            updatebefore = updatebefore + canshuold;
                            updateafter =
                                updateafter + canshuold.substring( 0, canshuold.indexOf( ":" ) ) + ":" + canshu;
                        }

                        if ( row == 0 )
                        {
                            returnstring = canshu1 + " ";
                            if ( ( canshu != null ) && ( !( canshu.equals( "" ) ) ) && ( !( canshu.equals( "null" ) ) ) )
                            {
                                if ( canshu.indexOf( "'" ) >= 0 )
                                {
                                    canshu = canshu.replaceAll( "'", "''" );
                                }
                                if ( ( type.equals( "string" ) ) || ( type.equals( "date" ) ) )
                                {
                                    sql = sql + name + "='" + canshu + "'";
                                }
                                else
                                {
                                    sql = sql + name + "=" + canshu + "";
                                }
                            }
                        }
                        else
                        {
                            returnstring = returnstring + "," + canshu1 + " ";
                            if ( ( canshu != null ) && ( !( canshu.equals( "" ) ) ) && ( !( canshu.equals( "null" ) ) ) )
                            {
                                if ( canshu.indexOf( "'" ) >= 0 )
                                {
                                    canshu = canshu.replaceAll( "'", "''" );
                                }
                                if ( ( type.equals( "string" ) ) || ( type.equals( "date" ) ) )
                                {
                                    sql = sql + "," + name + "='" + canshu + "'";
                                }
                                else
                                {
                                    sql = sql + "," + name + "=" + canshu + "";
                                }
                            }
                        }
                    }
                    else
                    {
                        String canshuold = request.getParameter( name + "old" );
                        int k = 0;
                        if ( kindid.indexOf( ":" ) < 0 )
                        {
                            String sql1 =
                                "select count(*) " + kindid.substring( kindid.indexOf( "from" ), kindid.length() );

                            rs = stmt.executeQuery( sql1 );
                            if ( rs.next() )
                            {
                                k = rs.getInt( 1 );
                            }
                        }
                        String canshu = getcheckbox( kindid, k, request, name );
                        if ( ( canshuold != null )
                            && ( !( canshuold.substring( canshuold.indexOf( ":" ) + 1, canshuold.length() ).equals( canshu ) ) ) )
                        {
                            updatebefore = updatebefore + canshuold;
                            updateafter =
                                updateafter + canshuold.substring( 0, canshuold.indexOf( ":" ) ) + ":" + canshu;
                        }

                        if ( row == 0 )
                        {
                            returnstring = canshu;
                            if ( ( type.equals( "string" ) ) || ( type.equals( "date" ) ) )
                            {
                                sql = sql + "" + name + "='" + canshu + "'";
                            }
                            else
                            {
                                sql = sql + "" + name + "=" + canshu + "";
                            }
                        }
                        else
                        {
                            returnstring = returnstring + "," + canshu;
                            if ( ( type.equals( "string" ) ) || ( type.equals( "date" ) ) )
                            {
                                sql = sql + "," + name + "='" + canshu + "'";
                            }
                            else
                            {
                                sql = sql + "," + name + "=" + canshu + "";
                            }
                        }
                    }
                    ++row;
                }
                sql = "update " + tablename + " set " + sql + " where " + where;
                stmt.execute( sql );
                out.println( "[" + returnstring + "]" );
            }
            else if ( action.equals( "geturl" ) )
            {
                String url = request.getParameter( "url" );
                url = URLEncoder.encode( url, "utf-8" );
                String index1 = request.getParameter( "index" );
                if ( ( index1 != null ) && ( !( index1.equals( "" ) ) ) && ( !( index1.equals( "null" ) ) ) )
                {
                    index1 = URLEncoder.encode( index1, "utf-8" );
                    out.println( "[" + url + "!=!" + index1 + "]" );
                }
                else
                {
                    out.println( "[" + url + "]" );
                }
            }
            else if ( action.equals( "insert" ) )
            {
                String index = request.getParameter( "index" );
                String sql = "select count(*) from " + tablename + " where ";
                String[] where = index.split( "," );
                String shouquan = request.getParameter( "shouquan" );
                if ( shouquan.equals( "001" ) )
                {
                    String cuserid = request.getParameter( "cuserid" );
                    String cpassword = request.getParameter( "cpassword" );
                    if ( !( checkUser( cuserid, cpassword, "C" ) ) )
                    {
                        out.println( "[shouquanerror]" );
                        return;
                    }
                }
                else if ( shouquan.equals( "002" ) )
                {
                    String buserid = request.getParameter( "buserid" );
                    String bpassword = request.getParameter( "bpassword" );
                    if ( !( checkUser( buserid, bpassword, "B" ) ) )
                    {
                        out.println( "[shouquanerror]" );
                        return;
                    }
                }
                else if ( shouquan.equals( "003" ) )
                {
                    String auserid = request.getParameter( "auserid" );
                    String apassword = request.getParameter( "apassword" );
                    if ( !( checkUser( auserid, apassword, "A" ) ) )
                    {
                        out.println( "[shouquanerror]" );
                        return;
                    }
                }
                else if ( shouquan.equals( "004" ) )
                {
                    String cuserid = request.getParameter( "cuserid" );
                    String cpassword = request.getParameter( "cpassword" );
                    String buserid = request.getParameter( "buserid" );
                    String bpassword = request.getParameter( "bpassword" );
                    if ( ( !( checkUser( cuserid, cpassword, "C" ) ) ) || ( !( checkUser( buserid, bpassword, "B" ) ) ) )
                    {
                        out.println( "[shouquanerror]" );
                        return;
                    }
                }
                else if ( shouquan.equals( "005" ) )
                {
                    String cuserid = request.getParameter( "cuserid" );
                    String cpassword = request.getParameter( "cpassword" );
                    String buserid = request.getParameter( "buserid" );
                    String bpassword = request.getParameter( "bpassword" );
                    if ( ( !( checkUser( cuserid, cpassword, "C" ) ) ) || ( !( checkUser( buserid, bpassword, "A" ) ) ) )
                    {
                        out.println( "[shouquanerror]" );
                        return;
                    }
                }
                else if ( shouquan.equals( "006" ) )
                {
                    String cuserid = request.getParameter( "cuserid" );
                    String cpassword = request.getParameter( "cpassword" );
                    String buserid = request.getParameter( "buserid" );
                    String bpassword = request.getParameter( "bpassword" );
                    if ( ( !( checkUser( cuserid, cpassword, "C" ) ) ) || ( !( checkUser( buserid, bpassword, "C" ) ) ) )
                    {
                        out.println( "[shouquanerror]" );
                        return;
                    }
                }
                else if ( shouquan.equals( "007" ) )
                {
                    String cuserid = request.getParameter( "cuserid" );
                    String cpassword = request.getParameter( "cpassword" );
                    String buserid = request.getParameter( "buserid" );
                    String bpassword = request.getParameter( "bpassword" );
                    if ( ( !( checkUser( cuserid, cpassword, "B" ) ) ) || ( !( checkUser( buserid, bpassword, "A" ) ) ) )
                    {
                        out.println( "[shouquanerror]" );
                        return;
                    }
                }
                for ( int i = 0; i < where.length; ++i )
                {
                    String id = where[i].substring( 0, where[i].indexOf( ":" ) );
                    String type = where[i].substring( where[i].indexOf( ":" ) + 1, where[i].lastIndexOf( ":" ) );

                    if ( i == 0 )
                    {
                        if ( type.equals( "string" ) )
                        {
                            sql = sql + id + "='" + request.getParameter( id ) + "'";
                        }
                        else
                        {
                            sql = sql + id + "=" + request.getParameter( id ) + "";
                        }

                    }
                    else if ( type.equals( "string" ) )
                    {
                        sql = sql + " and " + id + "='" + request.getParameter( id ) + "'";
                    }
                    else
                    {
                        sql = sql + " and " + id + "=" + request.getParameter( id ) + "";
                    }

                }

                rs = stmt.executeQuery( sql );
                int count = 0;
                if ( rs.next() )
                {
                    count = rs.getInt( 1 );
                }
                if ( count != 0 )
                {
                    out.println( "[no]" );
                    return;
                }
                String sql1 = "";
                String sql2 = "";
                Element tableEl = null;
                List list = getElement( getServletContext().getRealPath( "table/" + tablename + ".xml" ) );

                int row = 0;
                for ( Iterator iter = list.iterator(); iter.hasNext(); )
                {
                    tableEl = (Element) iter.next();
                    List attributes = tableEl.getAttributes();
                    String name = "";
                    String formitem = "";
                    String type = "";
                    String kindid = "";
                    Iterator at = attributes.iterator();
                    while ( at.hasNext() )
                    {
                        Attribute attr = (Attribute) at.next();
                        if ( attr.getName().equals( "name" ) )
                        {
                            name = attr.getValue();
                        }
                        if ( attr.getName().equals( "formitem" ) )
                        {
                            formitem = attr.getValue();
                        }
                        if ( attr.getName().equals( "type" ) )
                        {
                            type = attr.getValue();
                        }
                        if ( attr.getName().equals( "kindid" ) )
                        {
                            kindid = attr.getValue();
                        }
                    }
                    if ( !( formitem.equals( "checkbox" ) ) )
                    {
                        String canshu = request.getParameter( name );
                        String canshu1 = "";
                        if ( !( formitem.equals( "text" ) ) )
                        {
                            canshu1 = canshu + "--" + getValue( kindid, canshu, conn );
                        }
                        else
                        {
                            canshu1 = canshu;
                        }
                        if ( row == 0 )
                        {
                            returnstring = canshu1 + " ";
                            if ( ( canshu != null ) && ( !( canshu.equals( "" ) ) ) && ( !( canshu.equals( "null" ) ) ) )
                            {
                                sql1 = sql1 + name;
                                if ( ( type.equals( "string" ) ) || ( type.equals( "date" ) ) )
                                {
                                    if ( canshu.indexOf( "'" ) >= 0 )
                                    {
                                        canshu = canshu.replaceAll( "'", "''" );
                                    }
                                    sql2 = sql2 + "'" + canshu + "'";
                                }
                                else
                                {
                                    sql2 = sql2 + "" + canshu + "";
                                }
                            }
                        }
                        else
                        {
                            returnstring = returnstring + "," + canshu1 + " ";
                            if ( ( canshu != null ) && ( !( canshu.equals( "" ) ) ) && ( !( canshu.equals( "null" ) ) ) )
                            {
                                sql1 = sql1 + "," + name;
                                if ( ( type.equals( "string" ) ) || ( type.equals( "date" ) ) )
                                {
                                    if ( canshu.indexOf( "'" ) >= 0 )
                                    {
                                        canshu = canshu.replaceAll( "'", "''" );
                                    }
                                    sql2 = sql2 + ",'" + canshu + "'";
                                }
                                else
                                {
                                    sql2 = sql2 + "," + canshu + "";
                                }
                            }
                        }
                    }
                    else
                    {
                        int k = 0;
                        if ( kindid.indexOf( ":" ) < 0 )
                        {
                            sql = "select count(*) " + kindid.substring( kindid.indexOf( "from" ), kindid.length() );

                            rs = stmt.executeQuery( sql );
                            if ( rs.next() )
                            {
                                k = rs.getInt( 1 );
                            }
                        }
                        String canshu = getcheckbox( kindid, k, request, name );
                        if ( row == 0 )
                        {
                            returnstring = canshu;
                            sql1 = sql1 + name;
                            if ( ( type.equals( "string" ) ) || ( type.equals( "date" ) ) )
                            {
                                sql2 = sql2 + "'" + canshu + "'";
                            }
                            else
                            {
                                sql2 = sql2 + "" + canshu + "";
                            }
                        }
                        else
                        {
                            returnstring = returnstring + "," + canshu;
                            sql1 = sql1 + "," + name;
                            if ( ( type.equals( "string" ) ) || ( type.equals( "date" ) ) )
                            {
                                sql2 = sql2 + ",'" + canshu + "'";
                            }
                            else
                            {
                                sql2 = sql2 + "," + canshu + "";
                            }
                        }
                    }
                    ++row;
                }
                sql = "insert into " + tablename + " (" + sql1 + ") values (" + sql2 + ")";

                stmt.execute( sql );
                out.println( "[" + returnstring + "]" );
            }
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
            String message = new String( ex.getMessage().getBytes( "UTF-8" ), "ISO-8859-1" );

            response.sendError( 500, message );
        }
        finally
        {
            try
            {
                if ( rs != null )
                {
                    rs.close();
                }
                if ( stmt != null )
                {
                    stmt.close();
                }
                if ( ( conn != null ) && ( !( conn.isClosed() ) ) )
                {
                    conn.close();
                }
            }
            catch ( Exception e )
            {
            }
        }
    }

    public boolean checkUser( String userid, String password, String userlevel )
    {
        try
        {
            File file = new File( getServletContext().getRealPath( "WEB-INF/user.xml" ) );

            SAXBuilder builder = CommonUtil.newSAXBuilder();
            Document document = builder.build( file );
            Element root = document.getRootElement();
            Element userEl = (Element) XPath.selectSingleNode( root, "user[@userid='" + userid + "']" );

            if ( ( userEl != null ) && ( password.equals( userEl.getAttributeValue( "passwd" ) ) ) )
            {
                if ( userlevel.equals( "C" ) )
                {
                    return ( !( userid.equals( useridname ) ) );
                }

                if ( userlevel.equals( "B" ) )
                {
                    return ( ( !( userid.equals( useridname ) ) ) && ( !( userEl.getAttributeValue( "userlevel" ).equals( "C" ) ) ) );
                }

                if ( userlevel.equals( "A" ) )
                {
                    return ( ( !( userid.equals( useridname ) ) )
                        && ( !( userEl.getAttributeValue( "userlevel" ).equals( "C" ) ) ) && ( !( userEl.getAttributeValue( "userlevel" ).equals( "B" ) ) ) );
                }

            }
            else
            {
                return false;
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public String getcheckbox( String kindid, int k, HttpServletRequest request, String name )
    {
        String value = "";
        try
        {
            if ( kindid.indexOf( ":" ) < 0 )
            {
                for ( int i = 0; i < k; ++i )
                {
                    String canshu = request.getParameter( name + i );
                    if ( canshu.equals( "true" ) )
                    {
                        value = value + "1";
                    }
                    else
                    {
                        value = value + "0";
                    }
                }
            }
            else
            {
                String[] line = kindid.split( "," );
                for ( int i = 0; i < line.length; ++i )
                {
                    String canshu = request.getParameter( name + i );
                    if ( canshu.equals( "true" ) )
                    {
                        value = value + "1";
                    }
                    else
                    {
                        value = value + "0";
                    }
                }
            }
        }
        catch ( Exception ex )
        {
        }
        return value;
    }

    public static List getElement( String path )
    {
        SAXBuilder builder = CommonUtil.newSAXBuilder();
        try
        {
            File file = new File( path );
            Document pluginDoc = builder.build( file );
            List menus = XPath.selectNodes( pluginDoc, "/table/columns/column" );

            return menus;
        }
        catch ( Exception ex )
        {
        }
        return null;
    }

    @Override
    public void init()
        throws ServletException
    {
        super.init();
    }
}