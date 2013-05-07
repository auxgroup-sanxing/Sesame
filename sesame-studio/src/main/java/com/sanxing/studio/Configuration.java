package com.sanxing.studio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.studio.utils.CommonUtil;

public class Configuration
    implements ServletContextListener
{
    private static final Logger LOG = LoggerFactory.getLogger( Configuration.class );

    private static ServletContext servletContext = null;

    private static Document prefs = null;

    private static Document server = null;

    private static Document cluster = null;

    public static String getRealPath( String path )
    {
        return servletContext.getRealPath( path );
    }

    public static File getWorkfile( String path )
    {
        return new File( servletContext.getRealPath( "workarea/" + path ) );
    }

    public static File getWorkspaceRoot()
    {
        return Application.getWorkspaceRoot();
    }

    public static File getWarehouseRoot()
    {
        return Application.getWarehouseRoot();
    }

    public static File getWorkspaceFile( String path )
    {
        return new File( getWorkspaceRoot(), path );
    }

    public static File getWarehouseFile( String path )
    {
        return new File( getWarehouseRoot(), path );
    }

    @Override
    public void contextDestroyed( ServletContextEvent event )
    {
        servletContext = null;
        SQLDataSource.closeDataSource();
    }

    @Override
    public void contextInitialized( ServletContextEvent event )
    {
        servletContext = event.getServletContext();

        File file = getPrefsFile( servletContext );
        File serverFile = getServerFile( servletContext );
        File clusterFile = getClusterFile( servletContext );
        SAXBuilder builder = CommonUtil.newSAXBuilder();
        try
        {
            prefs = builder.build( file );
            server = builder.build( serverFile );
            cluster = builder.build( clusterFile );
        }
        catch ( JDOMException e )
        {
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    private static Element getDefaultConnection()
        throws JDOMException
    {
        Element connEl = (Element) XPath.selectSingleNode( prefs, "/prefs/database/connection[@default='default']" );
        if ( connEl == null )
        {
            throw new JDOMException( "没有找到缺省数据库连接配置" );
        }
        return connEl;
    }

    public static boolean useDataSource()
        throws JDOMException
    {
        Element connEl = getDefaultConnection();
        return ( connEl.getAttribute( "datasource" ) != null );
    }

    public static String getDataSource()
        throws JDOMException
    {
        Element connEl = getDefaultConnection();
        String jndi = connEl.getAttributeValue( "datasource" );
        return jndi;
    }

    public static String getConnetionUrl()
        throws JDOMException
    {
        Element connEl = getDefaultConnection();
        String url = connEl.getAttributeValue( "url" );
        return url;
    }

    public static String getDriverClass()
        throws JDOMException
    {
        Element connEl = getDefaultConnection();
        String driverClass = connEl.getAttributeValue( "driver" );
        return driverClass;
    }

    public static String getConnectionUser()
        throws JDOMException
    {
        Element connEl = getDefaultConnection();
        return connEl.getAttributeValue( "user" );
    }

    public static String getConnectionPasswd()
        throws JDOMException
    {
        Element connEl = getDefaultConnection();
        return connEl.getAttributeValue( "password" );
    }

    public static List getConnections()
        throws JDOMException
    {
        return XPath.selectNodes( prefs, "/prefs/database/connection" );
    }

    public static List getDataSources()
        throws JDOMException
    {
        return XPath.selectNodes( server, "/server/jdbc/datasource" );
    }

    public static void setConnections( List connections )
        throws JDOMException, IOException
    {
        Element databaseEl = (Element) XPath.selectSingleNode( prefs, "/prefs/database" );
        databaseEl.removeChildren( "connection" );
        databaseEl.addContent( connections );
        writePrefsFile();
        ConnDispenser.closeDataSource();
    }

    public static void setTransactionManager( String value )
        throws JDOMException, IOException
    {
        Element tsmEl = (Element) XPath.selectSingleNode( server, "/server/transaction-manager" );
        if ( tsmEl == null )
        {
            tsmEl = new Element( "transaction-manager" );
            server.getRootElement().addContent( tsmEl );
        }

        tsmEl.setText( value );
        writeServerFile();
    }

    public static void setDataSources( List dataSources )
        throws JDOMException, IOException
    {
        Element jdbcEl = (Element) XPath.selectSingleNode( server, "/server/jdbc" );
        if ( jdbcEl == null )
        {
            jdbcEl = new Element( "jdbc" );
            server.getRootElement().addContent( jdbcEl );
        }
        jdbcEl.removeChildren( "datasource" );
        jdbcEl.addContent( dataSources );
        writeServerFile();
    }

    public static Element getJMS()
        throws JDOMException
    {
        return ( (Element) XPath.selectSingleNode( server, "/server/jms/app-info" ) );
    }

    public static void setJMS( JSONArray jmsValue )
        throws Exception
    {
        Element info = new Element( "app-info" );
        int i = 0;
        for ( int len = jmsValue.length(); i < len; ++i )
        {
            JSONObject data = jmsValue.getJSONObject( i );

            String key = (String) data.keys().next();
            String value = data.getString( key );

            Element el = new Element( key );
            el.setText( value );
            info.addContent( el );
        }
        Element jmsEl = (Element) XPath.selectSingleNode( server, "/server/jms" );
        if ( jmsEl == null )
        {
            jmsEl = new Element( "jms" );
            server.getRootElement().addContent( jmsEl );
        }
        jmsEl.removeChildren( "app-info" );
        jmsEl.addContent( info );
        writeServerFile();
    }

    public static List getClusters()
        throws JDOMException
    {
        return XPath.selectNodes( cluster, "/managed-servers/server" );
    }

    public static void publishDataSource( JSONArray servers, List dataSources )
        throws JDOMException, IOException, JSONException
    {
        boolean publish;
        Element jdbcEl;
        Iterator itr;
        for ( int i = 0; i < servers.length(); ++i )
        {
            String serverName = servers.getJSONObject( i ).getString( "name" );
            publish = servers.getJSONObject( i ).optBoolean( "publish" );

            Element serverEl =
                (Element) XPath.selectSingleNode( cluster, "/managed-servers/server[server-name='" + serverName + "']" );

            if ( serverEl != null )
            {
                jdbcEl = (Element) XPath.selectSingleNode( serverEl, "jdbc" );
                if ( jdbcEl == null )
                {
                    jdbcEl = new Element( "jdbc" );
                    cluster.getRootElement().addContent( jdbcEl );
                }

                if ( serverEl != null )
                {
                    for ( itr = dataSources.iterator(); itr.hasNext(); )
                    {
                        Element inputDsEl = (Element) itr.next();
                        Element inputJndiEl = (Element) inputDsEl.getContent( 0 );
                        String inputJndiName = inputJndiEl.getTextTrim();

                        List dsList = XPath.selectNodes( jdbcEl, "datasource" );
                        Iterator iterator;
                        if ( ( dsList != null ) && ( !( dsList.isEmpty() ) ) )
                        {
                            for ( iterator = dsList.iterator(); iterator.hasNext(); )
                            {
                                Element datasourceEl = (Element) iterator.next();
                                if ( datasourceEl != null )
                                {
                                    String jndiName = datasourceEl.getChildText( "jndi-name" );
                                    if ( inputJndiName.equals( jndiName ) )
                                    {
                                        jdbcEl.removeContent( datasourceEl );
                                    }
                                }
                            }
                        }
                        if ( publish )
                        {
                            jdbcEl.addContent( (Element) inputDsEl.clone() );
                        }
                    }
                }
            }
        }
        writeClusterFile();
    }

    public static void abolishDataSource( List<String> dataSources )
        throws JDOMException, IOException
    {
        if ( ( dataSources != null ) && ( !( dataSources.isEmpty() ) ) )
        {
            for ( Object element : dataSources )
            {
                String dsName = (String) element;
                List dsList = XPath.selectNodes( cluster, "/managed-servers/server/jdbc/datasource" );

                if ( ( dsList != null ) && ( !( dsList.isEmpty() ) ) )
                {
                    for ( Iterator itr = dsList.iterator(); itr.hasNext(); )
                    {
                        Element dsEl = (Element) itr.next();
                        String jndiName = dsEl.getChildText( "jndi-name" );
                        if ( jndiName.equals( dsName ) )
                        {
                            dsEl.detach();
                        }
                    }
                }
            }
        }
        writeClusterFile();
    }

    public static void writePrefsFile()
        throws IOException
    {
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat( Format.getPrettyFormat().setEncoding( "GBK" ).setIndent( "  " ) );
        FileOutputStream outStream = new FileOutputStream( getPrefsFile( servletContext ) );
        outputter.output( prefs, outStream );
        outStream.close();
    }

    public static void writeServerFile()
        throws IOException
    {
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat( Format.getPrettyFormat().setEncoding( "GBK" ).setIndent( "  " ) );
        FileOutputStream outStream = new FileOutputStream( getServerFile( servletContext ) );
        outputter.output( server, outStream );
        outStream.close();
    }

    public static File getPrefsFile( ServletContext servletContext )
    {
        return new File( servletContext.getRealPath( "WEB-INF/prefs.xml" ) );
    }

    public static File getAddressBookFile()
    {
        String sesame_home = System.getProperty( "SESAME_HOME" );
        return new File( sesame_home, "conf/address-book.xml" );
    }

    public static File getServerFile( ServletContext servletContext )
    {
        String sesame_home = System.getProperty( "SESAME_HOME" );
        if ( sesame_home == null )
        {
            sesame_home = servletContext.getInitParameter( "ConfigPath" );
        }
        return new File( sesame_home + File.separator + "conf" + File.separator + "admin.xml" );
    }

    public static File getServerFile()
    {
        String sesame_home = System.getProperty( "SESAME_HOME" );
        if ( sesame_home == null )
        {
            sesame_home = servletContext.getInitParameter( "ConfigPath" );
        }
        return new File( sesame_home + File.separator + "conf" + File.separator + "admin.xml" );
    }

    public static Element getSCMPrefs()
        throws JDOMException
    {
        Element scmEl = (Element) XPath.selectSingleNode( prefs, "/prefs/scm" );
        return ( (Element) scmEl.clone() );
    }

    public static void setSCMPrefs( Element prefsEl )
        throws JDOMException
    {
        Element scmEl = (Element) XPath.selectSingleNode( prefs, "/prefs/scm" );
        if ( scmEl != null )
        {
            scmEl.detach();
        }
        prefsEl.setName( "scm" );
        prefsEl.setNamespace( Namespace.NO_NAMESPACE );
        prefs.getRootElement().addContent( prefsEl );
    }

    public static File getClusterFile( ServletContext servletContext )
    {
        String sesame_home = System.getProperty( "SESAME_HOME" );
        if ( sesame_home == null )
        {
            sesame_home = servletContext.getInitParameter( "ConfigPath" );
        }
        return new File( sesame_home + File.separator + "conf" + File.separator + "cluster.xml" );
    }

    public static File getClusterFile()
    {
        String sesame_home = System.getProperty( "SESAME_HOME" );
        if ( sesame_home == null )
        {
            sesame_home = servletContext.getInitParameter( "ConfigPath" );
        }
        return new File( sesame_home + File.separator + "conf" + File.separator + "cluster.xml" );
    }

    public static void writeClusterFile()
        throws IOException
    {
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat( Format.getPrettyFormat().setEncoding( "GBK" ).setIndent( "  " ) );
        FileOutputStream outStream = new FileOutputStream( getClusterFile( servletContext ) );
        outputter.output( cluster, outStream );
        outStream.close();
    }

    public static void writeClusterFile( Document cluster, File clusterFile )
        throws IOException
    {
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat( Format.getPrettyFormat().setEncoding( "GBK" ).setIndent( "  " ) );
        FileOutputStream outStream = new FileOutputStream( clusterFile );
        outputter.output( cluster, outStream );
        outStream.close();
    }
}