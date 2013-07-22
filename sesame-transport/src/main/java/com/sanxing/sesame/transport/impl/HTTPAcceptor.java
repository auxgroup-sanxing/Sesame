package com.sanxing.sesame.transport.impl;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sanxing.sesame.binding.annotation.Description;
import com.sanxing.sesame.binding.codec.BinaryResult;
import com.sanxing.sesame.binding.codec.BinarySource;
import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.binding.transport.Acceptor;
import com.sanxing.sesame.binding.transport.BaseTransport;
import com.sanxing.sesame.transport.util.HttpThreadPool;

@Description( "超文本传输协议" )
public class HTTPAcceptor
    extends BaseTransport
    implements Acceptor
{

    private static final Logger LOG = LoggerFactory.getLogger( HTTPAcceptor.class );

    private Server webServer;

    private boolean active;

    private URI uri;

    private Map<?, ?> properties = new HashMap();

    @Override
    public void reply( MessageContext context )
        throws IOException
    {
        context.getSerial();
        context.getPath();
    }

    @Override
    public void close()
        throws IOException
    {
        if ( ( active ) && ( webServer != null ) )
        {
            try
            {
                webServer.stop();
                webServer.destroy();
            }
            catch ( Exception e )
            {
                LOG.debug( e.getMessage(), e );
            }
            active = false;
        }
        LOG.info( "Transport closed: " + getURI() );
    }

    @Override
    public String getCharacterEncoding()
    {
        String encoding = getProperty( "encoding" );
        return ( ( encoding != null ) ? encoding : System.getProperty( "file.encoding" ) );
    }

    public String getProperty( String key )
    {
        return ( (String) properties.get( key ) );
    }

    public int getProperty( String key, int def )
    {
        try
        {
            String value = getProperty( key );
            return ( ( value == null ) ? def : Integer.parseInt( value ) );
        }
        catch ( NumberFormatException e )
        {
        }
        return def;
    }

    @Override
    public boolean isActive()
    {
        return active;
    }

    @Override
    public void open()
        throws IOException
    {
        HttpThreadPool pool = new HttpThreadPool();

        webServer = new Server();
        webServer.setThreadPool( pool );

        ServletContextHandler appContext = new ServletContextHandler( 1 );
        appContext.setContextPath( "/" );
        appContext.addServlet( new ServletHolder( new InnerServlet() ), "/*" );
        webServer.setHandler( appContext );

        int portNumber = ( uri.getPort() == -1 ) ? 80 : uri.getPort();
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setHost( uri.getHost() );
        connector.setPort( portNumber );
        connector.setMaxIdleTime( 30000 );
        webServer.setConnectors( new Connector[] { connector } );
        webServer.setSendServerVersion( true );

        LOG.debug( "Connectors: " + webServer.getConnectors().length );
        try
        {
            webServer.start();
            active = webServer.isStarted();
            LOG.info( this + " listening......" );
        }
        catch ( IOException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            LOG.debug( e.getMessage(), e );
            throw new IOException( e.getMessage() );
        }
    }

    @Override
    public void setURI( URI uri )
    {
        this.uri = uri;
    }

    @Override
    public URI getURI()
    {
        return uri;
    }

    @Override
    public void setConfig( String contextPath, Element config )
        throws IllegalArgumentException
    {
        Map properties = new HashMap();
        if ( ( config != null ) && ( contextPath == null ) )
        {
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            try
            {
                String expression = "*/*";
                NodeList nodes = (NodeList) xpath.evaluate( expression, config, XPathConstants.NODESET );
                int i = 0;
                for ( int len = nodes.getLength(); i < len; ++i )
                {
                    Element prop = (Element) nodes.item( i );
                    properties.put( prop.getNodeName(), prop.getTextContent().trim() );
                }

            }
            catch ( XPathExpressionException e )
            {
                throw new IllegalArgumentException( e.getMessage(), e );
            }
        }
    }

    @Override
    protected void setProperties( Map<?, ?> properties )
        throws IllegalArgumentException
    {
        this.properties = properties;
    }

    private void received( HttpServletRequest request, HttpServletResponse response )
        throws IOException
    {
        BinarySource source = new BinarySource();
        BinaryResult result = new BinaryResult();

        MessageContext ctx = new MessageContext( this, source );
        ctx.setPath( request.getContextPath().length() > 0 ? request.getContextPath() : "/" );
        String action = request.getHeader( "HTTPAction" );
        if ( action == null)
        {
            action = request.getRequestURI().substring( request.getContextPath().length() + 1 );
        }
        ctx.setAction( action );
        try
        {
            String encoding = request.getCharacterEncoding();
            encoding = ( encoding != null ) ? encoding : getCharacterEncoding();

            source.setInputStream( request.getInputStream() );
            source.setEncoding( encoding );
            
            result.setOutputStream( response.getOutputStream() );
            result.setEncoding( encoding );
            
            ctx.setResult( result );
            
            postMessage( ctx );
        }
        catch ( Throwable t )
        {
            LOG.debug( t.getMessage(), t );
            response.sendError( 500, t.getMessage() );
        }
    }

    @Override
    public String toString()
    {
        return "{ name:'" + super.getClass().getSimpleName() + "', url: '" + getURI() + "' }";
    }

    private class InnerServlet
        extends HttpServlet
    {
        private static final long serialVersionUID = 6294753513239090289L;

        @Override
        protected void doGet( HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException
        {
            LOG.debug( "doGet " + request.getRequestURI() );
            received( request, response );
        }

        @Override
        protected void doPost( HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException
        {
            LOG.debug( "doPost " + request.getRequestURI() );
            received( request, response );
        }
    }
}