package com.sanxing.studio.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.studio.Configuration;
import com.sanxing.studio.ConnDispenser;
import com.sanxing.studio.utils.MessageException;
import com.sanxing.studio.utils.WebServletRequest;
import com.sanxing.studio.utils.WebUtil;

public class SQLExplorer
    extends HttpServlet
    implements Servlet
{
    private static final long serialVersionUID = 1L;

    private static final String TAG_TABLES = "tables";

    private static final String TAG_VIEWS = "views";

    private static final String TAG_PROCEDURES = "procedures";

    private static final String TAG_COLUMNS = "columns";

    private static final String TAG_INDICES = "indices";

    private static final String MESSAGE_TABLENOTFOUND = "表不存在，可能已被删除";

    private static final Logger LOG = LoggerFactory.getLogger( SQLExplorer.class );

    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        request = new WebServletRequest( request );
        String action = request.getParameter( "action" );
        String data = request.getParameter( "data" );
        response.setContentType( "text/json; charset=utf-8" );
        PrintWriter out = response.getWriter();
        try
        {
            File schemaFolder = getSchemaFolder();
            if ( action != null )
            {
                if ( action.equals( "getDBInfo" ) )
                {
                    response.setContentType( "text/plain; charset=utf-8" );
                    Connection conn = null;
                    conn = ConnDispenser.getConnection();
                    try
                    {
                        DatabaseMetaData meta = conn.getMetaData();
                        String info =
                            "数据库类型: " + meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion() + "\n";

                        info = info + "连接: " + meta.getURL();
                        out.write( info );
                    }
                    finally
                    {
                        if ( !( conn.isClosed() ) )
                        {
                            conn.close();
                        }
                    }
                }
                else if ( action.equals( "getTableMeta" ) )
                {
                    String tableName = request.getParameter( "table" );

                    JSONObject table = getTableMeta( getServletContext(), tableName );
                    if ( ( table == null ) || ( table.getJSONArray( "columns" ).length() == 0 ) )
                    {
                        WebUtil.sendError( response, "没有取到表字段，请在数据库管理器中查看表是否存在" );
                        return;
                    }
                    out.print( table );
                }
                else if ( action.equals( "loadColumns" ) )
                {
                    String objectName = request.getParameter( "object" );
                    int idx = objectName.indexOf( 95 );
                    String object = objectName.substring( 0, idx );
                    String name = objectName.substring( idx + 1 );

                    response.setContentType( "text/xml; charset=utf-8" );
                    File file = new File( schemaFolder, object + "s/" + name );
                    SAXBuilder builder = new SAXBuilder();
                    Document document = builder.build( file );
                    Element root = document.getRootElement();
                    Element columns = root.getChild( "columns", root.getNamespace() );
                    document.detachRootElement();
                    if ( columns != null )
                    {
                        columns.detach();
                        document.setRootElement( columns );
                    }
                    else
                    {
                        document.setRootElement( new Element( "columns" ) );
                    }
                    XMLOutputter outputter = new XMLOutputter();
                    outputter.getFormat().setEncoding( response.getCharacterEncoding() );
                    out.print( outputter.outputString( document ) );
                }
                else if ( action.equals( "loadIndex" ) )
                {
                    String tableName = request.getParameter( "table" );
                    String indexName = request.getParameter( "index" );

                    response.setContentType( "text/xml; charset=utf-8" );
                    File file = new File( schemaFolder, "tables/" + tableName );
                    SAXBuilder builder = new SAXBuilder();
                    Document document = builder.build( file );
                    Element root = document.getRootElement();
                    Element index = null;
                    if ( indexName != null )
                    {
                        index = (Element) XPath.selectSingleNode( root, "indices/index[@name='" + indexName + "']" );
                    }
                    Element columns = (Element) XPath.selectSingleNode( root, "columns" );
                    document.detachRootElement();
                    if ( columns != null )
                    {
                        if ( index != null )
                        {
                            List list = index.getChildren( "column" );
                            Iterator iter = list.iterator();
                            while ( iter.hasNext() )
                            {
                                Element indexCol = (Element) iter.next();
                                Element column =
                                    (Element) XPath.selectSingleNode( columns,
                                        "column[column_name='" + indexCol.getAttributeValue( "name" ) + "']" );

                                if ( column != null )
                                {
                                    column.addContent( new Element( "is_checked" ).setText( "true" ) );
                                    column.addContent( new Element( "sort" ).setText( indexCol.getAttributeValue( "sort" ) ) );
                                }
                            }
                        }
                        columns.detach();
                        document.setRootElement( columns );
                    }
                    else
                    {
                        document.setRootElement( new Element( "columns" ) );
                    }
                    XMLOutputter outputter = new XMLOutputter();
                    outputter.getFormat().setEncoding( response.getCharacterEncoding() );

                    out.print( outputter.outputString( document ) );
                }
                else
                {
                    LOG.info( action + "->" + data );
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
    }

    private void appendChild( String nodeId, HttpServletRequest request )
        throws Exception
    {
        File schemaFolder = getSchemaFolder();
        if ( nodeId.equals( "TABLE" ) )
        {
            String name = request.getParameter( "name" );
            String validator = request.getParameter( "validator" );
            File file = new File( schemaFolder, "tables/" + name );
            validateTable( request, file.getParentFile(), null );
            Element tableEl = new Element( "table" );
            tableEl.setAttribute( "remarks", request.getParameter( "remarks" ) );
            if ( ( validator != null ) && ( validator.length() > 0 ) )
            {
                Element validEl = new Element( "restriction" );
                validEl.addContent( new CDATA( validator ) );
                tableEl.addContent( validEl );
            }
            Document document = new Document( tableEl );
            Connection conn = ConnDispenser.getConnection();
            try
            {
                DatabaseMetaData dbMeta = conn.getMetaData();
                reverseColumn( dbMeta, tableEl, name );
                reverseIndices( dbMeta, tableEl, name );
            }
            catch ( SQLException e )
            {
                LOG.debug( e.getMessage() );
            }
            finally
            {
                conn.close();
            }
            writeSchemaFile( document, file );
        }
        else if ( nodeId.equals( "VIEW" ) )
        {
            String name = request.getParameter( "name" );
            File file = new File( schemaFolder, "views/" + name );
            validateTable( request, file.getParentFile(), null );
            Element viewEl = new Element( "view" );
            viewEl.setAttribute( "remarks", request.getParameter( "remarks" ) );
            Document document = new Document( viewEl );
            writeSchemaFile( document, file );
        }
        else if ( nodeId.startsWith( "indices_" ) )
        {
            SAXBuilder builder = new SAXBuilder();
            String table = request.getParameter( "table" );
            File file = new File( schemaFolder, "tables/" + table );
            if ( !( file.exists() ) )
            {
                throwTableNotFound();
            }
            Document document = builder.build( file );
            validateIndex( request, document, null );
            Element tableEl = document.getRootElement();

            Element indices = tableEl.getChild( "indices", tableEl.getNamespace() );
            if ( indices == null )
            {
                tableEl.addContent( indices = new Element( "indices" ) );
            }
            Element index = new Element( "index" );
            index.setAttribute( "name", request.getParameter( "name" ) );
            index.setAttribute( "type", request.getParameter( "type" ) );
            indices.addContent( index );
            JSONArray colArray = new JSONArray( request.getParameter( "data" ) );
            for ( int i = 0; i < colArray.length(); ++i )
            {
                JSONObject col = colArray.getJSONObject( i );
                Element column = new Element( "column" );
                column.setAttribute( "name", col.getString( "column_name" ) );
                column.setAttribute( "sort", col.getString( "sort" ) );
                index.addContent( column );
            }
            writeSchemaFile( document, file );
        }
        else
        {
            if ( nodeId.startsWith( "columns_" ) )
            {
                return;
            }
            LOG.info( "appendChild->" + nodeId );
        }
    }

    private boolean removeNode( JSONObject node, String drop )
        throws Exception
    {
        String objectId = node.getString( "id" );

        File schemaFolder = getSchemaFolder();
        if ( objectId.startsWith( "table_" ) )
        {
            String id = objectId.replaceAll( "table_", "" );
            File file = new File( schemaFolder, "tables/" + id );
            if ( file.exists() )
            {
                file.delete();
                if ( ( drop != null ) && ( drop.equals( "yes" ) ) )
                {
                    dropTable( id );
                }
            }
            return true;
        }
        if ( objectId.startsWith( "view_" ) )
        {
            String id = objectId.replaceAll( "view_", "" );
            File file = new File( schemaFolder, "views/" + id );
            if ( file.exists() )
            {
                file.delete();
            }
            return true;
        }
        if ( objectId.startsWith( "procedure_" ) )
        {
            String id = objectId.replaceAll( "procedure_", "" );
            File file = new File( schemaFolder, "procedures/" + id );
            if ( file.exists() )
            {
                file.delete();
            }
            return true;
        }
        String meta = node.optString( "meta" );
        if ( ( meta != null ) && ( meta.equals( "INDEX" ) ) )
        {
            File file = new File( schemaFolder, "tables/" + node.getString( "table" ) );

            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build( file );
            Element index =
                (Element) XPath.selectSingleNode( document, "/table/indices/index[@name='" + node.getString( "name" )
                    + "']" );

            if ( index != null )
            {
                index.detach();
                writeSchemaFile( document, file );
            }
            return true;
        }
        return false;
    }

    private JSONObject loadTable( String table )
        throws Exception
    {
        JSONObject data = new JSONObject();
        File folder = getSchemaFolder();
        File file = new File( folder, "tables/" + table );
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build( file );

        Element root = document.getRootElement();
        data.put( "name", file.getName() );
        data.put( "remarks", root.getAttributeValue( "remarks" ) );
        Element restEl = root.getChild( "restriction", root.getNamespace() );
        if ( restEl != null )
        {
            List contents = restEl.getContent( new Filter()
            {
                public static final long serialVersionUID = 0L;

                @Override
                public boolean matches( Object arg )
                {
                    return arg instanceof CDATA;
                }
            } );
            if ( contents.size() > 0 )
            {
                CDATA validator = (CDATA) contents.get( 0 );
                data.put( "validator", validator.getValue() );
            }
        }
        return data;
    }

    private void dropTable( String name )
        throws Exception
    {
        Connection conn = ConnDispenser.getConnection();
        try
        {
            String sqlText = "DROP TABLE " + name;
            ConnDispenser.executeDDL( conn, sqlText );
        }
        finally
        {
            conn.close();
        }
    }

    public String getChildren( String nodeId )
        throws Exception
    {
        JSONArray result = new JSONArray();
        String objectId = nodeId;
        String parentId = objectId.replaceFirst( "[a-z]+_", "" );
        File schemaFolder = getSchemaFolder();
        if ( objectId.equals( "ROOT" ) )
        {
            String[][] list = { { "TABLE", "表" }, { "VIEW", "视图" }, { "PROCEDURE", "存储过程" } };

            for ( int i = 0; i < list.length; ++i )
            {
                String[] item = list[i];
                JSONObject treeNode = new JSONObject();
                treeNode.put( "text", item[1] );
                treeNode.put( "id", item[0] );
                treeNode.put( "icon", "sqlexplorer_viewer/images/closedFolder.gif" );
                treeNode.put( "allowAppend", true );
                result.put( treeNode );
            }
        }
        else if ( objectId.equals( "TABLE" ) )
        {
            SAXBuilder builder = new SAXBuilder();
            File[] list = new File( schemaFolder, "tables" ).listFiles();
            for ( int i = 0; i < list.length; ++i )
            {
                File f = list[i];
                if ( !( f.isFile() ) )
                {
                    continue;
                }
                Document document = builder.build( f );
                Element root = document.getRootElement();
                JSONObject treeNode = new JSONObject();
                treeNode.put( "text", f.getName() + ":" + root.getAttributeValue( "remarks" ) );
                treeNode.put( "id", "table_" + f.getName() );
                treeNode.put( "icon", "sqlexplorer_viewer/images/table.gif" );
                treeNode.put( "allowDelete", true );
                treeNode.put( "allowModify", true );
                treeNode.put( "allowSet", true );
                treeNode.put( "allowAlter", true );
                treeNode.put( "allowReverse", true );
                treeNode.put( "allowBrowse", true );
                treeNode.put( "name", f.getName() );
                treeNode.put( "remarks", root.getAttributeValue( "remarks" ) );
                result.put( treeNode );
            }
        }
        else if ( objectId.equals( "VIEW" ) )
        {
            SAXBuilder builder = new SAXBuilder();
            File viewFolder = new File( schemaFolder, "views" );
            if ( viewFolder.exists() )
            {
                File[] list = viewFolder.listFiles();
                if ( ( list != null ) && ( list.length > 0 ) )
                {
                    for ( int i = 0; i < list.length; ++i )
                    {
                        File f = list[i];
                        if ( !( f.isFile() ) )
                        {
                            continue;
                        }
                        Document document = builder.build( f );
                        Element root = document.getRootElement();
                        JSONObject treeNode = new JSONObject();
                        treeNode.put( "text", f.getName() + ":" + root.getAttributeValue( "remarks" ) );

                        treeNode.put( "cls", "folder" );
                        treeNode.put( "id", "view_" + f.getName() );
                        treeNode.put( "icon", "sqlexplorer_viewer/images/view.gif" );
                        treeNode.put( "allowDelete", true );
                        treeNode.put( "allowModify", true );
                        treeNode.put( "allowSet", true );
                        treeNode.put( "allowBrowse", true );
                        treeNode.put( "name", f.getName() );
                        treeNode.put( "remarks", root.getAttributeValue( "remarks" ) );
                        result.put( treeNode );
                    }
                }
            }
        }
        else if ( objectId.equals( "PROCEDURE" ) )
        {
            SAXBuilder builder = new SAXBuilder();
            File procedureFolder = new File( schemaFolder, "views" );
            if ( procedureFolder.exists() )
            {
                File[] list = procedureFolder.listFiles();
                if ( ( list != null ) && ( list.length > 0 ) )
                {
                    for ( int i = 0; i < list.length; ++i )
                    {
                        File f = list[i];
                        if ( !( f.isFile() ) )
                        {
                            continue;
                        }
                        Document document = builder.build( f );
                        Element root = document.getRootElement();
                        JSONObject treeNode = new JSONObject();
                        treeNode.put( "text", f.getName() + ":" + root.getAttributeValue( "remarks" ) );
                        treeNode.put( "cls", "folder" );
                        treeNode.put( "id", "procedure_" + f.getName() );
                        treeNode.put( "icon", "sqlexplorer_viewer/images/stored_procedure.gif" );
                        treeNode.put( "allowDelete", true );
                        treeNode.put( "allowModify", true );
                        treeNode.put( "allowSet", true );
                        treeNode.put( "allowBrowse", true );
                        treeNode.put( "name", f.getName() );
                        treeNode.put( "remarks", root.getAttributeValue( "remarks" ) );
                        result.put( treeNode );
                    }
                }
            }
        }
        else if ( objectId.startsWith( "table_" ) )
        {
            String[][] list = { { "columns", "字段" }, { "indices", "索引" } };
            for ( int i = 0; i < list.length; ++i )
            {
                String[] item = list[i];
                JSONObject treeNode = new JSONObject();
                treeNode.put( "text", item[1] );
                treeNode.put( "id", item[0] + "_" + parentId );
                treeNode.put( "icon", "sqlexplorer_viewer/images/closedFolder.gif" );
                treeNode.put( "allowAppend", true );
                result.put( treeNode );
            }
        }
        else if ( objectId.startsWith( "view_" ) )
        {
            String[][] list = { { "columns", "字段" } };
            for ( int i = 0; i < list.length; ++i )
            {
                String[] item = list[i];
                JSONObject treeNode = new JSONObject();
                treeNode.put( "text", item[1] );
                treeNode.put( "id", item[0] + "_" + parentId );
                treeNode.put( "icon", "sqlexplorer_viewer/images/closedFolder.gif" );
                treeNode.put( "allowAppend", true );
                result.put( treeNode );
            }
        }
        else if ( objectId.startsWith( "columns_" ) )
        {
            File file = new File( schemaFolder, "tables/" + parentId );
            String parent = "table";
            if ( !( file.exists() ) )
            {
                file = new File( schemaFolder, "views/" + parentId );
                parent = "view";
            }
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build( file );
            Element root = document.getRootElement();
            List list = XPath.selectNodes( root, "columns/column" );
            Iterator iter = list.iterator();
            while ( iter.hasNext() )
            {
                Element elem = (Element) iter.next();
                JSONObject treeNode = new JSONObject();
                String typeDscr = elem.getChildText( "type_name", elem.getNamespace() ).toLowerCase();
                if ( ( typeDscr.equals( "char" ) ) || ( typeDscr.equals( "varchar" ) ) )
                {
                    typeDscr = typeDscr + "(" + elem.getChildText( "column_size", elem.getNamespace() ) + ")";
                }
                treeNode.put( "text", elem.getChildText( "column_name", elem.getNamespace() ) + " <b>" + typeDscr + "</b>" );

                treeNode.put( "leaf", true );
                treeNode.put( "icon", "sqlexplorer_viewer/images/columns.gif" );
                treeNode.put( "meta", "COLUMN" );
                treeNode.put( parent, parentId );
                treeNode.put( "name", elem.getChildText( "column_name", elem.getNamespace() ) );

                result.put( treeNode );
            }
        }
        else if ( objectId.startsWith( "indices_" ) )
        {
            File file = new File( schemaFolder, "tables/" + parentId );
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build( file );
            Element root = document.getRootElement();
            List list = XPath.selectNodes( root, "indices/index" );
            Iterator iter = list.iterator();
            while ( iter.hasNext() )
            {
                Element elem = (Element) iter.next();
                JSONObject treeNode = new JSONObject();
                treeNode.put( "text", elem.getAttributeValue( "name" ) );
                treeNode.put( "leaf", true );
                treeNode.put( "icon", "sqlexplorer_viewer/images/index.gif" );
                treeNode.put( "meta", "INDEX" );
                treeNode.put( "table", parentId );
                treeNode.put( "name", elem.getAttributeValue( "name" ) );
                treeNode.put( "type", elem.getAttributeValue( "type" ) );
                treeNode.put( "allowDelete", true );
                treeNode.put( "allowModify", true );
                result.put( treeNode );
            }
        }
        else if ( !( objectId.startsWith( "constraints_" ) ) )
        {
            if ( !( objectId.startsWith( "triggers_" ) ) )
            {
                ;
            }
        }
        return result.toString();
    }

    private File getSchemaFolder()
    {
        return new File( getServletContext().getRealPath( "sqlex/schema" ) );
    }

    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        request.setCharacterEncoding( "UTF-8" );
        String action = request.getParameter( "action" );
        String data = request.getParameter( "data" );

        response.setContentType( "text/json; charset=utf-8" );
        PrintWriter out = response.getWriter();
        try
        {
            if ( action != null )
            {
                if ( action.equals( "getChildren" ) )
                {
                    String nodeId = request.getParameter( "node" );
                    out.write( getChildren( nodeId ) );
                }
                else if ( action.equals( "appendChild" ) )
                {
                    String nodeId = request.getParameter( "node" );
                    appendChild( nodeId, request );
                    out.print( true );
                }
                else if ( action.equals( "remove" ) )
                {
                    JSONObject node = new JSONObject( data );
                    boolean result = removeNode( node, request.getParameter( "drop" ) );

                    out.print( result );
                }
                else if ( action.equals( "update" ) )
                {
                    String nodeId = request.getParameter( "node" );
                    boolean result = update( nodeId, request );
                    out.print( result );
                }
                else if ( action.equals( "loadConnections" ) )
                {
                    JSONObject result = new JSONObject();
                    result.put( "items", loadConnections() );
                    result.put( "success", true );
                    out.print( result );
                }
                else if ( action.equals( "loadDataSources" ) )
                {
                    JSONObject result = new JSONObject();
                    result.put( "items", loadDataSources() );
                    result.put( "success", true );
                    out.print( result );
                }
                else if ( action.equals( "loadTable" ) )
                {
                    JSONObject result = new JSONObject();
                    result.put( "data", loadTable( request.getParameter( "table" ) ) );
                    result.put( "success", true );
                    out.print( result );
                }
                else if ( action.equals( "saveConnections" ) )
                {
                    JSONArray connObjs = new JSONArray( request.getParameter( "data" ) );
                    boolean result = saveConnections( connObjs );
                    out.print( result );
                }
                else if ( action.equals( "saveDataSources" ) )
                {
                    JSONArray datas = new JSONArray( request.getParameter( "data" ) );
                    String transactionManager = request.getParameter( "manager" );
                    int tsCount = 0;
                    if ( ( transactionManager.equalsIgnoreCase( "stm" ) ) && ( datas.length() > 0 ) )
                    {
                        for ( int i = 0; i < datas.length(); ++i )
                        {
                            JSONObject jso = datas.getJSONObject( i );
                            boolean isTS = jso.optBoolean( "transaction" );
                            if ( !( isTS ) )
                            {
                                continue;
                            }
                            ++tsCount;
                        }
                    }

                    if ( tsCount > 1 )
                    {
                        throw new Exception( "只允许有一个数据源使用STM事务管理器" );
                    }
                    Configuration.setTransactionManager( transactionManager );
                    boolean result = saveDataSources( datas );
                    out.print( result );
                }
                else if ( action.equals( "saveColumns" ) )
                {
                    String objectName = request.getParameter( "object" );
                    JSONTokener tokener = new JSONTokener( request.getParameter( "data" ) );

                    JSONArray columns = new JSONArray( tokener );
                    saveColumns( objectName, columns );
                    out.print( true );
                }
                else if ( action.equals( "syncronize" ) )
                {
                    String direction = request.getParameter( "direction" );
                    File folder = getSchemaFolder();
                    Connection conn = ConnDispenser.getConnection();
                    try
                    {
                        if ( ( direction.equals( "bidi" ) ) || ( direction.equals( "db" ) ) )
                        {
                            meta2db( conn, folder, request );
                        }
                        if ( ( direction.equals( "bidi" ) ) || ( direction.equals( "meta" ) ) )
                        {
                            db2meta( conn, folder, request );
                        }
                        out.print( true );
                    }
                    finally
                    {
                        conn.close();
                    }
                }
                else if ( action.equals( "alter" ) )
                {
                    File folder = getSchemaFolder();
                    Connection conn = ConnDispenser.getConnection();
                    try
                    {
                        String obj = request.getParameter( "object" );
                        if ( ( obj == null ) || ( obj.indexOf( "table_" ) != 0 ) )
                        {
                            LOG.info( "没有指定表名称" );
                            out.print( false );
                        }
                        else
                        {
                            meta2Table( new File( folder, obj.replaceFirst( "_", "s/" ) ), conn );

                            out.print( true );
                        }
                    }
                    finally
                    {
                        conn.close();
                    }

                }
                else if ( action.equals( "singleReverse" ) )
                {
                    File folder = getSchemaFolder();
                    Connection conn = ConnDispenser.getConnection();
                    try
                    {
                        String tableName = request.getParameter( "tableName" );
                        if ( ( null == tableName ) || ( tableName.length() < 1 ) )
                        {
                            LOG.info( "获取表结构时没有取到表名" );
                            out.print( false );
                        }
                        else
                        {
                            tableName = tableName.replace( "table_", "" );
                            boolean result = singleReverse( conn, folder, tableName );
                            out.print( result );
                        }
                    }
                    finally
                    {
                        conn.close();
                    }
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
    }

    private boolean singleReverse( Connection conn, File folder, String tableName )
        throws Exception
    {
        SAXBuilder builder = new SAXBuilder();
        DatabaseMetaData dbMeta = conn.getMetaData();

        File tables = new File( folder, "tables" );
        String[] types = { "TABLE" };
        ResultSet rs = dbMeta.getTables( null, null, tableName, types );
        while ( rs.next() )
        {
            File file = new File( tables, tableName );
            Element table = null;
            Document document = null;
            if ( file.exists() )
            {
                document = builder.build( file );
                table = document.getRootElement();
            }

            if ( table == null )
            {
                table = new Element( "table" );
                String remarks = rs.getString( "REMARKS" );
                table.setAttribute( "remarks", ( remarks == null ) ? "" : remarks );
                document = new Document( table );
            }
            reverseColumn( dbMeta, table, tableName );
            reverseIndices( dbMeta, table, tableName );
            writeSchemaFile( document, file );
        }
        rs.close();
        return true;
    }

    private boolean saveConnections( JSONArray connObjs )
        throws Exception
    {
        List conns = new ArrayList();
        int i = 0;
        for ( int len = connObjs.length(); i < len; ++i )
        {
            JSONObject conn = connObjs.getJSONObject( i );
            Element connEl = new Element( "connection" );
            connEl.setAttribute( "name", conn.getString( "name" ) );
            if ( "default".equals( conn.getString( "default" ) ) )
            {
                connEl.setAttribute( "default", conn.getString( "default" ) );
            }
            Iterator keys;
            if ( conn.get( "type" ).equals( "jndi" ) )
            {
                connEl.setAttribute( "datasource", conn.getString( "datasource" ) );
            }
            else
            {
                conn.remove( "name" );
                conn.remove( "default" );
                conn.remove( "type" );
                for ( keys = conn.keys(); keys.hasNext(); )
                {
                    String key = (String) keys.next();
                    if ( !( key.equals( "datasource" ) ) )
                    {
                        connEl.setAttribute( key, conn.getString( key ) );
                    }
                }
            }
            conns.add( connEl );
        }
        Configuration.setConnections( conns );
        return true;
    }

    private boolean saveDataSources( JSONArray datas )
        throws Exception
    {
        List dataSources = new ArrayList();
        int i = 0;
        for ( int len = datas.length(); i < len; ++i )
        {
            JSONObject data = datas.getJSONObject( i );
            Element datasource = new Element( "datasource" );

            Element jndiName = new Element( "jndi-name" );
            jndiName.setText( data.getString( "jndi-name" ) );

            Element tsmanager = new Element( "transaction" );
            tsmanager.setText( data.getString( "transaction" ) );

            Element appInfo = new Element( "app-info" );
            for ( Iterator keys = data.keys(); keys.hasNext(); )
            {
                String key = (String) keys.next();
                if ( ( !( key.equals( "jndi-name" ) ) ) && ( !( key.equals( "transaction" ) ) ) )
                {
                    Element child = new Element( key );
                    child.setText( data.getString( key ) );
                    appInfo.addContent( child );
                }
            }
            datasource.addContent( jndiName );
            datasource.addContent( tsmanager );
            datasource.addContent( appInfo );

            dataSources.add( datasource );
        }
        Configuration.setDataSources( dataSources );
        return true;
    }

    private JSONArray loadConnections()
        throws JDOMException, JSONException
    {
        JSONArray result = new JSONArray();
        List list = Configuration.getConnections();
        for ( Iterator iter = list.iterator(); iter.hasNext(); )
        {
            Element connEl = (Element) iter.next();
            JSONObject connObj = new JSONObject();
            List attrList = connEl.getAttributes();
            for ( Iterator iterator = attrList.iterator(); iterator.hasNext(); )
            {
                Attribute attr = (Attribute) iterator.next();
                connObj.put( attr.getName(), attr.getValue() );
            }
            if ( connObj.has( "datasource" ) )
            {
                connObj.put( "type", "jndi" );
            }
            else
            {
                connObj.put( "type", "jdbc" );
            }
            result.put( connObj );
        }
        return result;
    }

    private JSONArray loadDataSources()
        throws JDOMException, JSONException
    {
        JSONArray result = new JSONArray();
        List list = Configuration.getDataSources();

        for ( Iterator iter = list.iterator(); iter.hasNext(); )
        {
            JSONObject obj = new JSONObject();
            Element datasource = (Element) iter.next();
            List children = datasource.getChildren();
            for ( Iterator itr = children.iterator(); itr.hasNext(); )
            {
                Element el = (Element) itr.next();
                List detailList = el.getChildren();

                if ( detailList.size() == 0 )
                {
                    String name = el.getName();
                    String value = el.getTextTrim();
                    obj.put( name, value );
                }
                else
                {
                    List appList = el.getChildren();
                    for ( Iterator appItr = appList.iterator(); appItr.hasNext(); )
                    {
                        Element child = (Element) appItr.next();
                        String name = child.getName();
                        String value = "";
                        if ( child.getContent().size() > 0 )
                        {
                            value = child.getContent( 0 ).getValue();
                        }
                        obj.put( name, value );
                    }
                }
            }
            result.put( obj );
        }
        return result;
    }

    private void saveColumns( String objectName, JSONArray columnArray )
        throws Exception
    {
        File schemaFolder = getSchemaFolder();
        int idx = objectName.indexOf( 95 );
        String object = objectName.substring( 0, idx );
        String name = objectName.substring( idx + 1 );
        File file = new File( schemaFolder, object + "s/" + name );
        if ( !( file.exists() ) )
        {
            throw new Exception( "对象不存在 - " + name );
        }
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build( file );
        Element root = document.getRootElement();
        Element columns = root.getChild( "columns", root.getNamespace() );
        if ( columns == null )
        {
            root.addContent( columns = new Element( "columns" ) );
        }
        columns.removeContent();
        for ( int i = 0; i < columnArray.length(); ++i )
        {
            JSONObject col = columnArray.getJSONObject( i );
            Element column = new Element( "column" );
            columns.addContent( column );
            Iterator keys = col.keys();
            while ( keys.hasNext() )
            {
                String key = (String) keys.next();
                column.addContent( new Element( key ).setText( col.getString( key ) ) );
            }
        }
        writeSchemaFile( document, file );
    }

    private void throwTableNotFound()
        throws Exception
    {
        throw new MessageException( "表不存在，可能已被删除" );
    }

    public static JSONObject getMulTableMeta( ServletContext servletContext, String tableName )
        throws JDOMException, IOException, JSONException
    {
        File schemaFolder = new File( servletContext.getRealPath( "sqlex/schema" ) );
        String[] tables = tableName.split( "," );
        SAXBuilder builder = new SAXBuilder();
        JSONObject table = new JSONObject();
        JSONArray columns = new JSONArray();

        JSONArray validator = new JSONArray();

        for ( String tName : tables )
        {
            File file = new File( schemaFolder, "tables/" + tName );
            if ( !( file.exists() ) )
            {
                return null;
            }
            Document document = builder.build( file );
            Element tableEl = document.getRootElement();

            List list = XPath.selectNodes( tableEl, "columns/column" );
            Iterator iter = list.iterator();

            while ( iter.hasNext() )
            {
                Element elem = (Element) iter.next();
                JSONObject column = new JSONObject();
                String columnName = elem.getChildText( "column_name", elem.getNamespace() );

                String header = elem.getChildText( "remarks", elem.getNamespace() );
                if ( ( header == null ) || ( header.equals( "" ) ) )
                {
                    header = columnName;
                }
                String type = elem.getChildText( "type_name", elem.getNamespace() ).toLowerCase();
                if ( type.indexOf( "char" ) > -1 )
                {
                    type = "string";
                }
                else if ( type.indexOf( "int" ) > -1 )
                {
                    type = "int";
                }
                else if ( type.equals( "date1" ) )
                {
                    column.put( "dateFormat", "Ymd" );
                }
                else if ( type.equals( "date" ) )
                {
                    column.put( "dateFormat", "Y-m-d" );
                }
                else if ( ( type.equals( "dec" ) ) || ( type.equals( "decimal" ) ) || ( type.equals( "money" ) )
                    || ( type.equals( "numeric" ) ) )
                {
                    type = "float";
                }
                else if ( type.equals( "time" ) )
                {
                    type = "date";
                    column.put( "dateFormat", "H:i:s" );
                }
                else if ( ( type.equals( "datetime" ) ) || ( type.equals( "timestamp" ) ) )
                {
                    type = "date";
                    column.put( "dateFormat", "Y-m-d H:i:s" );
                }
                else if ( type.equals( "serial" ) )
                {
                    type = "int";
                    column.put( "autoInc", true );
                }
                else
                {
                    type = "string";
                }
                column.put( "header", header );
                column.put( "dataIndex", tName + "_" + columnName );
                column.put( "type", type );
                column.put( "size", elem.getChildText( "column_size", elem.getNamespace() ) );
                column.put( "allowBlank", "true".equals( elem.getChildText( "is_nullable", elem.getNamespace() ) ) );

                String restriction = elem.getChildText( "restriction", elem.getNamespace() );
                if ( ( restriction != null ) && ( restriction.length() > 0 ) )
                {
                    JSONTokener tokener = new JSONTokener( restriction );
                    column.put( "restriction", tokener.nextValue() );
                }
                columns.put( column );
            }

            Element restEl = tableEl.getChild( "restriction", tableEl.getNamespace() );
            if ( restEl != null )
            {
                List contents = restEl.getContent( new Filter()
                {
                    public static final long serialVersionUID = 0L;

                    @Override
                    public boolean matches( Object arg )
                    {
                        return arg instanceof CDATA;
                    }
                } );
                if ( contents.size() > 0 )
                {
                    CDATA eachValidator = (CDATA) contents.get( 0 );
                    validator.put( eachValidator.getValue() );
                }
            }
        }
        table.put( "columns", columns );
        table.put( "validator", validator );
        LOG.debug( " getMulTableMeta  table is:" + table.toString() );
        return table;
    }

    public static JSONObject getTableMeta( ServletContext servletContext, String tableName )
        throws JDOMException, IOException, JSONException
    {
        File schemaFolder = new File( servletContext.getRealPath( "sqlex/schema" ) );
        File file = new File( schemaFolder, "tables/" + tableName );
        if ( !( file.exists() ) )
        {
            return null;
        }
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build( file );
        Element tableEl = document.getRootElement();
        JSONObject table = new JSONObject();
        List list = XPath.selectNodes( tableEl, "columns/column" );
        Iterator iter = list.iterator();
        JSONArray columns = new JSONArray();
        while ( iter.hasNext() )
        {
            Element elem = (Element) iter.next();
            JSONObject column = new JSONObject();
            String columnName = elem.getChildText( "column_name", elem.getNamespace() );

            String header = elem.getChildText( "remarks", elem.getNamespace() );
            if ( ( header == null ) || ( header.equals( "" ) ) )
            {
                header = columnName;
            }
            String type = elem.getChildText( "type_name", elem.getNamespace() ).toLowerCase();
            if ( type.indexOf( "char" ) > -1 )
            {
                type = "string";
            }
            else if ( type.indexOf( "int" ) > -1 )
            {
                type = "int";
            }
            else if ( type.equals( "date1" ) )
            {
                column.put( "dateFormat", "Ymd" );
            }
            else if ( type.equals( "date" ) )
            {
                column.put( "dateFormat", "Y-m-d" );
            }
            else if ( ( type.equals( "dec" ) ) || ( type.equals( "decimal" ) ) || ( type.equals( "money" ) )
                || ( type.equals( "numeric" ) ) )
            {
                type = "float";
            }
            else if ( type.equals( "time" ) )
            {
                type = "date";
                column.put( "dateFormat", "H:i:s" );
            }
            else if ( ( type.equals( "datetime" ) ) || ( type.equals( "timestamp" ) ) )
            {
                type = "date";
                column.put( "dateFormat", "Y-m-d H:i:s" );
            }
            else if ( type.equals( "serial" ) )
            {
                type = "int";
                column.put( "autoInc", true );
            }
            else
            {
                type = "string";
            }
            column.put( "header", header );
            column.put( "dataIndex", columnName );
            column.put( "type", type );
            column.put( "size", elem.getChildText( "column_size", elem.getNamespace() ) );
            column.put( "allowBlank", "true".equals( elem.getChildText( "is_nullable", elem.getNamespace() ) ) );

            String def = elem.getChildText( "column_def", elem.getNamespace() );
            if ( def != null )
            {
                column.put( "def", def );
            }
            String restriction = elem.getChildText( "restriction", elem.getNamespace() );
            if ( ( restriction != null ) && ( restriction.length() > 0 ) )
            {
                JSONTokener tokener = new JSONTokener( restriction );
                column.put( "restriction", tokener.nextValue() );
            }
            columns.put( column );
        }

        table.put( "columns", columns );
        Element restEl = tableEl.getChild( "restriction", tableEl.getNamespace() );
        if ( restEl != null )
        {
            List contents = restEl.getContent( new Filter()
            {
                public static final long serialVersionUID = 0L;

                @Override
                public boolean matches( Object arg )
                {
                    return arg instanceof CDATA;
                }
            } );
            if ( contents.size() > 0 )
            {
                CDATA validator = (CDATA) contents.get( 0 );
                table.put( "validator", validator.getValue() );
            }
        }
        return table;
    }

    public static JSONObject getViewMeta( ServletContext servletContext, String viewName )
        throws JDOMException, IOException, JSONException
    {
        File schemaFolder = new File( servletContext.getRealPath( "sqlex/schema" ) );
        File file = new File( schemaFolder, "views/" + viewName );
        if ( !( file.exists() ) )
        {
            return null;
        }
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build( file );
        Element viewEl = document.getRootElement();
        JSONObject view = new JSONObject();
        List list = XPath.selectNodes( viewEl, "columns/column" );
        Iterator iter = list.iterator();
        JSONArray columns = new JSONArray();
        while ( iter.hasNext() )
        {
            Element elem = (Element) iter.next();
            JSONObject column = new JSONObject();
            String columnName = elem.getChildText( "column_name", elem.getNamespace() );

            String header = elem.getChildText( "remarks", elem.getNamespace() );
            if ( ( header == null ) || ( header.equals( "" ) ) )
            {
                header = columnName;
            }
            String type = elem.getChildText( "type_name", elem.getNamespace() ).toLowerCase();
            if ( type.indexOf( "char" ) > -1 )
            {
                type = "string";
            }
            else if ( type.equals( "date" ) )
            {
                column.put( "dateFormat", "Y-m-d" );
            }
            else if ( ( type.equals( "dec" ) ) || ( type.equals( "decimal" ) ) )
            {
                type = "float";
            }
            else if ( type.equals( "integer" ) )
            {
                type = "int";
            }
            else if ( type.equals( "time" ) )
            {
                type = "date";
                column.put( "dateFormat", "H:i:s" );
            }
            else if ( type.equals( "datetime" ) )
            {
                type = "date";
                column.put( "dateFormat", "Y-m-d H:i:s" );
            }
            else
            {
                type = "string";
            }
            column.put( "header", header );
            column.put( "dataIndex", columnName );
            column.put( "type", type );
            column.put( "size", elem.getChildText( "column_size", elem.getNamespace() ) );
            column.put( "allowBlank", "true".equals( elem.getChildText( "is_nullable", elem.getNamespace() ) ) );

            String restriction = elem.getChildText( "restriction", elem.getNamespace() );
            if ( ( restriction != null ) && ( restriction.length() > 0 ) )
            {
                JSONTokener tokener = new JSONTokener( restriction );
                column.put( "restriction", tokener.nextValue() );
            }
            columns.put( column );
        }

        view.put( "columns", columns );
        Element restEl = viewEl.getChild( "restriction", viewEl.getNamespace() );
        if ( restEl != null )
        {
            List contents = restEl.getContent( new Filter()
            {
                public static final long serialVersionUID = 0L;

                @Override
                public boolean matches( Object arg )
                {
                    return arg instanceof CDATA;
                }
            } );
            if ( contents.size() > 0 )
            {
                CDATA validator = (CDATA) contents.get( 0 );
                view.put( "validator", validator.getValue() );
            }
        }
        return view;
    }

    private void meta2Table( File file, Connection conn )
        throws Exception
    {
        SAXBuilder builder = new SAXBuilder();
        DatabaseMetaData dbMeta = conn.getMetaData();
        Document document = builder.build( file );
        Element table = document.getRootElement();
        Element columns = table.getChild( "columns", table.getNamespace() );
        if ( columns != null )
        {
            List colList = columns.getChildren();
            Iterator colIter = colList.iterator();

            ResultSet rs = dbMeta.getColumns( null, null, file.getName(), "%" );
            String sql;
            if ( rs.next() )
            {
                sql = "ALTER TABLE " + file.getName();
                ArrayList array = new ArrayList();
                do
                {
                    String name = rs.getString( "COLUMN_NAME" );

                    Element column = (Element) XPath.selectSingleNode( columns, "column[column_name='" + name + "']" );

                    if ( column != null )
                    {
                        column.setAttribute( "action", "MODIFY" );
                    }
                    else
                    {
                        array.add( "DROP " + name );
                    }
                }
                while ( rs.next() );

                while ( colIter.hasNext() )
                {
                    Element column = (Element) colIter.next();
                    String action = column.getAttributeValue( "action" );
                    if ( action == null )
                    {
                        array.add( "ADD " + getColumnDefine( column ) );
                    }
                    else
                    {
                        array.add( "MODIFY " + getColumnDefine( column ) );
                    }
                }
                sql = sql + " " + array.toString().replaceAll( "^\\[|\\]$", "" );
            }
            else
            {
                sql = "CREATE TABLE " + file.getName();
                ArrayList array = new ArrayList();
                while ( colIter.hasNext() )
                {
                    Element column = (Element) colIter.next();
                    array.add( getColumnDefine( column ) );
                }
                sql = sql + array.toString().replaceFirst( "^\\[", "(" ).replaceFirst( "\\]$", ")" );
            }

            ConnDispenser.executeDDL( conn, sql );
        }
        List indexList = XPath.selectNodes( table, "indices/index" );
        for ( Iterator index = indexList.iterator(); index.hasNext(); )
        {
            Element indexElem = (Element) index.next();
            List indexCols = indexElem.getChildren( "column" );
            if ( indexCols.size() > 0 )
            {
                String indexType = indexElem.getAttributeValue( "type" );
                String prefix = ( ( indexType != null ) && ( indexType.equalsIgnoreCase( "unique" ) ) ) ? "UNIQUE" : "";

                String sqlText =
                    "CREATE " + prefix + " INDEX " + indexElem.getAttributeValue( "name" ) + " ON "
                        + table.getAttributeValue( "name" );

                ArrayList colNames = new ArrayList();
                for ( Iterator colIter = indexCols.iterator(); colIter.hasNext(); )
                {
                    Element column = (Element) colIter.next();
                    String sort = column.getAttributeValue( "sort" );
                    colNames.add( column.getAttributeValue( "name" ) + ( ( sort != null ) ? " " + sort : "" ) );
                }

                sqlText = sqlText + colNames.toString().replaceFirst( "^\\[", "(" ).replaceFirst( "\\]$", ")" );

                sqlText =
                    sqlText
                        + ( ( ( indexType != null ) && ( indexType.equalsIgnoreCase( "primary" ) ) ) ? " WITH PRIMARY"
                            : "" );
                try
                {
                    ConnDispenser.executeDDL( conn, sqlText );
                }
                catch ( SQLException e )
                {
                    LOG.debug( sqlText + ":" + e.getMessage() );
                }
            }
        }
    }

    private boolean meta2db( Connection conn, File folder, HttpServletRequest request )
        throws Exception
    {
        File[] tables = new File( folder, "tables" ).listFiles();
        String tableFlag = request.getParameter( "table" );
        if ( ( tableFlag != null ) && ( tableFlag.equals( "on" ) ) && ( tables != null ) )
        {
            for ( int i = 0; i < tables.length; ++i )
            {
                File file = tables[i];
                if ( !( file.isFile() ) )
                {
                    continue;
                }
                meta2Table( file, conn );
            }
        }
        return true;
    }

    private String getColumnDefine( Element column )
    {
        String name = column.getChildText( "column_name", column.getNamespace() );
        String type = column.getChildText( "type_name", column.getNamespace() );
        String def = column.getChildText( "column_def", column.getNamespace() );
        def = " DEFAULT " + ( ( type.toLowerCase().indexOf( "char" ) >= 0 ) ? "'" + def + "'" : def );

        if ( type.toLowerCase().indexOf( "char" ) >= 0 )
        {
            type = type + " (" + column.getChildText( "column_size", column.getNamespace() ) + ")";
        }
        else if ( type.equals( "decimal" ) )
        {
            type =
                type + " (" + column.getChildText( "column_size", column.getNamespace() ) + "," + column.getChildText( "decimal_digits", column.getNamespace() )
                    + ")";
        }
        String nullable = column.getChildText( "is_nullable", column.getNamespace() );
        return name + " " + type + ( ( nullable.equals( "false" ) ) ? " NOT NULL" : "" ) + def;
    }

    private boolean db2meta( Connection conn, File folder, HttpServletRequest request )
        throws Exception
    {
        SAXBuilder builder = new SAXBuilder();
        DatabaseMetaData dbMeta = conn.getMetaData();

        String tableFlag = request.getParameter( "table" );
        if ( ( tableFlag != null ) && ( tableFlag.equals( "on" ) ) )
        {
            File tables = new File( folder, "tables" );
            String[] types = { "TABLE" };
            ResultSet rs = dbMeta.getTables( null, null, "%", types );
            while ( rs.next() )
            {
                String tableName = rs.getString( "TABLE_NAME" );
                File file = new File( tables, tableName );
                Element table = null;
                Document document = null;
                if ( file.exists() )
                {
                    document = builder.build( file );
                    table = document.getRootElement();
                }

                if ( table == null )
                {
                    table = new Element( "table" );
                    String remarks = rs.getString( "REMARKS" );
                    table.setAttribute( "remarks", ( remarks == null ) ? "" : remarks );

                    document = new Document( table );
                }
                reverseColumn( dbMeta, table, tableName );
                reverseIndices( dbMeta, table, tableName );
                writeSchemaFile( document, file );
            }
            rs.close();
        }

        String viewFlag = request.getParameter( "view" );
        if ( ( viewFlag != null ) && ( viewFlag.equals( "on" ) ) )
        {
            File views = new File( folder, "views" );
            String[] typev = { "VIEW" };
            ResultSet rs = dbMeta.getTables( null, null, "%", typev );
            while ( rs.next() )
            {
                String viewName = rs.getString( "TABLE_NAME" );
                File file = new File( views, viewName );
                Element view = null;
                Document document = null;
                if ( file.exists() )
                {
                    document = builder.build( file );
                    view = document.getRootElement();
                }

                if ( view == null )
                {
                    view = new Element( "view" );
                    String remarks = rs.getString( "REMARKS" );
                    view.setAttribute( "remarks", ( remarks == null ) ? "" : remarks );

                    document = new Document( view );
                }
                writeSchemaFile( document, file );
            }
            rs.close();
        }

        String procFlag = request.getParameter( "procedure" );
        if ( ( procFlag != null ) && ( procFlag.equals( "on" ) ) )
        {
            File procs = new File( folder, "procedures" );
            ResultSet rs = dbMeta.getProcedures( null, null, "%" );
            while ( rs.next() )
            {
                String procName = rs.getString( "PROCEDURE_NAME" );
                File file = new File( procs, procName );
                Element proc = null;
                Document document = null;
                if ( file.exists() )
                {
                    document = builder.build( file );
                    proc = document.getRootElement();
                }
                if ( proc == null )
                {
                    proc = new Element( "procedure" );
                    String remarks = rs.getString( "REMARKS" );
                    proc.setAttribute( "remarks", ( remarks == null ) ? "" : remarks );

                    document = new Document( proc );
                }
                writeSchemaFile( document, file );
            }
            rs.close();
        }
        return true;
    }

    private void writeSchemaFile( Document document, File file )
        throws IOException
    {
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat( Format.getPrettyFormat().setEncoding( "utf-8" ).setIndent( "  " ) );
        FileOutputStream outStream = new FileOutputStream( file );
        outputter.output( document, outStream );
        outStream.close();
    }

    private void reverseColumn( DatabaseMetaData meta, Element table, String name )
        throws SQLException, JDOMException
    {
        Element columns = table.getChild( "columns", table.getNamespace() );
        if ( columns == null )
        {
            table.addContent( columns = new Element( "columns" ) );
        }
        ResultSet rs = meta.getColumns( null, null, name, "%" );
        while ( rs.next() )
        {
            String columnName = rs.getString( "COLUMN_NAME" );
            Element column = (Element) XPath.selectSingleNode( columns, "column[column_name='" + columnName + "']" );

            if ( column == null )
            {
                column = new Element( "column" );
                columns.addContent( column );
                String def = rs.getString( "COLUMN_DEF" );
                String remark = rs.getString( "REMARKS" );
                String nullable = rs.getString( "IS_NULLABLE" );
                column.addContent( new Element( "column_name" ).setText( columnName ) );
                column.addContent( new Element( "type_name" ).setText( rs.getString( "TYPE_NAME" ).toLowerCase() ) );

                if ( rs.getString( "TYPE_NAME" ).toLowerCase().equals( "int" ) )
                {
                    column.addContent( new Element( "column_size" ).setText( "10" ) );
                    column.addContent( new Element( "decimal_digits" ).setText( "0" ) );
                }
                else
                {
                    column.addContent( new Element( "column_size" ).setText( rs.getString( "COLUMN_SIZE" ) ) );
                    column.addContent( new Element( "decimal_digits" ).setText( rs.getString( "DECIMAL_DIGITS" ) ) );
                }

                column.addContent( new Element( "is_nullable" ).setText( ( nullable.equals( "YES" ) ) ? "true"
                    : "false" ) );
                column.addContent( new Element( "column_def" ).setText( ( def == null ) ? "" : def ) );
                column.addContent( new Element( "restriction" ).setText( "" ) );
                column.addContent( new Element( "remarks" ).setText( ( remark == null ) ? "" : remark ) );
            }
        }
        rs.close();
    }

    private void reverseIndices( DatabaseMetaData meta, Element table, String name )
        throws SQLException
    {
        Element indices = table.getChild( "indices", table.getNamespace() );
        if ( indices == null )
        {
            table.addContent( indices = new Element( "indices" ) );
        }
        else
        {
            indices.removeContent();
        }
        ArrayList names = new ArrayList();
        String tableName = name;
        ResultSet rs = meta.getPrimaryKeys( null, null, tableName );
        String indexName = null;
        Element index = new Element( "index" );
        while ( rs.next() )
        {
            indexName = rs.getString( "PK_NAME" );
            names.add( indexName );
            Element indexCol = new Element( "column" );
            indexCol.setAttribute( "name", rs.getString( "COLUMN_NAME" ) );
            index.addContent( indexCol );
        }

        if ( index.getContentSize() > 0 )
        {
            index.setAttribute( "name", ( indexName == null ) ? tableName + "_PK" : indexName );

            index.setAttribute( "type", "primary" );
            indices.addContent( index );
        }
        rs.close();
        rs = meta.getIndexInfo( null, null, tableName, false, false );
        indexName = "";
        while ( rs.next() )
        {
            String idxName = rs.getString( "INDEX_NAME" );
            if ( idxName == null )
            {
                continue;
            }
            if ( names.indexOf( idxName ) != -1 )
            {
                continue;
            }
            if ( !( indexName.equals( idxName ) ) )
            {
                indexName = rs.getString( "INDEX_NAME" );
                index = new Element( "index" );
                indices.addContent( index );
                index.setAttribute( "name", indexName );
                boolean nonUnique = rs.getBoolean( "NON_UNIQUE" );
                index.setAttribute( "type", ( nonUnique ) ? "common" : "unique" );
            }
            Element indexCol = new Element( "column" );
            indexCol.setAttribute( "name", rs.getString( "COLUMN_NAME" ) );
            String sort = rs.getString( "ASC_OR_DESC" );
            if ( sort != null )
            {
                indexCol.setAttribute( "sort", ( sort.equals( "D" ) ) ? "DESC" : "ASC" );
            }
            index.addContent( indexCol );
        }
        rs.close();
    }

    private boolean update( String nodeId, HttpServletRequest request )
        throws Exception
    {
        File schemaFolder = getSchemaFolder();
        String id = nodeId.replaceFirst( "[a-z]+_", "" );
        SAXBuilder builder = new SAXBuilder();
        if ( nodeId.startsWith( "table_" ) )
        {
            File file = new File( schemaFolder, "tables/" + id );
            if ( !( file.exists() ) )
            {
                throwTableNotFound();
            }
            Document document = builder.build( file );
            validateTable( request, file, id );
            Element tableEl = document.getRootElement();
            tableEl.setAttribute( "remarks", request.getParameter( "remarks" ) );
            String validator = request.getParameter( "validator" );
            if ( ( validator != null ) && ( validator.length() > 0 ) )
            {
                Element validEl = tableEl.getChild( "restriction", tableEl.getNamespace() );
                if ( validEl == null )
                {
                    tableEl.addContent( validEl = new Element( "restriction" ) );
                }
                validEl.removeContent();
                validEl.addContent( new CDATA( validator ) );
            }
            else
            {
                tableEl.removeChild( "restriction" );
            }
            writeSchemaFile( document, file );
            if ( !( id.equals( request.getParameter( "name" ) ) ) )
            {
                file.renameTo( new File( schemaFolder, "tables/" + request.getParameter( "name" ) ) );
            }
            return true;
        }
        if ( nodeId.startsWith( "view_" ) )
        {
            File file = new File( schemaFolder, "views/" + id );
            if ( !( file.exists() ) )
            {
                throw new Exception( "视图不存在: " + id );
            }
            Document document = builder.build( file );
            validateTable( request, file, id );
            Element table = document.getRootElement();
            table.setAttribute( "remarks", request.getParameter( "remarks" ) );
            writeSchemaFile( document, file );
            if ( !( id.equals( request.getParameter( "name" ) ) ) )
            {
                file.renameTo( new File( schemaFolder, "views/" + request.getParameter( "name" ) ) );
            }

            return true;
        }
        if ( nodeId.startsWith( "index_" ) )
        {
            String table = request.getParameter( "table" );
            File file = new File( schemaFolder, "tables/" + table );
            if ( !( file.exists() ) )
            {
                throwTableNotFound();
            }
            Document document = builder.build( file );
            validateIndex( request, document, id );
            Element index = (Element) XPath.selectSingleNode( document, "/table/indices/index[@name='" + id + "']" );
            if ( index != null )
            {
                index.setAttribute( "name", request.getParameter( "name" ) );
                index.setAttribute( "type", request.getParameter( "type" ) );
                index.removeContent();
                JSONArray colArray = new JSONArray( request.getParameter( "data" ) );
                for ( int i = 0; i < colArray.length(); ++i )
                {
                    JSONObject col = colArray.getJSONObject( i );
                    Element column = new Element( "column" );
                    column.setAttribute( "name", col.getString( "column_name" ) );
                    column.setAttribute( "sort", col.getString( "sort" ) );
                    index.addContent( column );
                }
                writeSchemaFile( document, file );
            }
            else
            {
                throw new Exception( "索引未找到: " + id );
            }
            return true;
        }
        return false;
    }

    private void validateTable( HttpServletRequest request, File folder, String oldId )
        throws Exception
    {
        String tableName = request.getParameter( "name" );
        if ( ( oldId == null ) || ( !( tableName.equals( oldId ) ) ) )
        {
            File file = new File( folder, tableName );
            if ( file.exists() )
            {
                throw new MessageException( "表名称已存在 - " + tableName );
            }
        }
    }

    private void validateIndex( HttpServletRequest request, Document document, String oldId )
        throws Exception
    {
        String indexName = request.getParameter( "name" );
        if ( ( oldId == null ) || ( !( indexName.equals( oldId ) ) ) )
        {
            Element table =
                (Element) XPath.selectSingleNode( document, "/table/indices/index[@name='" + indexName + "']" );

            if ( table != null )
            {
                throw new MessageException( "指定的索引名称已存在 - " + indexName );
            }
        }
    }

    public static String getViewSQL( String viewName, ServletContext servletContext )
        throws JDOMException, IOException
    {
        File schemaFolder = new File( servletContext.getRealPath( "sqlex/schema" ) );
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build( new File( schemaFolder, "views/" + viewName ) );
        Element sqlEl = (Element) XPath.selectSingleNode( document, "/view/sql" );
        if ( sqlEl != null )
        {
            return sqlEl.getText();
        }
        return null;
    }
}