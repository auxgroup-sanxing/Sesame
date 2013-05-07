package com.sanxing.sesame.transport.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sanxing.sesame.binding.Carrier;
import com.sanxing.sesame.binding.codec.BinarySource;
import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.binding.transport.Acceptor;
import com.sanxing.sesame.binding.transport.BaseTransport;
import com.sanxing.sesame.transport.quartz.SesameScheduler;
import com.sanxing.sesame.transport.quartz.TaskImpl;

public abstract class TaskTransport
    extends BaseTransport
    implements Acceptor
{
    private static Logger LOG = LoggerFactory.getLogger( TaskTransport.class );

    protected SesameScheduler scheduler;

    protected Map<String, Map<String, Object>> bindingPropsCache = new HashMap();

    protected ExecutorService workExecutor = new ThreadPoolExecutor( 2, 5, 60L, TimeUnit.MILLISECONDS,
        new ArrayBlockingQueue( 50 ) );

    protected URI uri;

    protected Map<?, ?> properties = new HashMap();

    protected boolean active = false;

    protected String operationName = "";

    protected String readLine = "false";

    protected String setContextAction = "true";

    protected String encoding = "GBK";

    protected int buffer_size = 1024;

    public void accept( InputStream in, String operationName, String contextPath )
        throws IOException
    {
        byte[] bytes = new byte[buffer_size];
        setOperationName( operationName );
        read( in, bytes, contextPath );
    }

    protected void read( InputStream input, byte[] bytes, String contextPath )
        throws IOException
    {
        if ( !( readLine.equals( "true" ) ) )
        {
            byte[] temp = new byte[1024];
            int len = 0;
            int position = 0;
            while ( ( len = input.read( temp ) ) != -1 )
            {
                System.arraycopy( temp, 0, bytes, position, len );
                position += len;
            }
            input.close();
            try
            {
                workExecutor.execute( new Command( bytes, position, contextPath ) );
            }
            catch ( Exception e )
            {
                throw new IOException( "ftp read error!" );
            }
        }
        else
        {
            InputStreamReader reader = new InputStreamReader( input );
            BufferedReader bufferReader = new BufferedReader( reader );
            String value = "";
            while ( ( value = bufferReader.readLine() ) != null )
            {
                byte[] getBuffer = value.getBytes( encoding );
                try
                {
                    if ( getBuffer.length > 0 )
                    {
                        workExecutor.execute( new Command( getBuffer, getBuffer.length, contextPath ) );
                    }
                }
                catch ( Exception e )
                {
                    throw new IOException( "ftp read error!" );
                }
            }
            bufferReader.close();
            reader.close();
            input.close();
        }
    }

    @Override
    public URI getURI()
    {
        return uri;
    }

    @Override
    public void removeCarrier( String contextPath, Carrier receiver )
    {
        try
        {
            deleteJob( contextPath, uri.getScheme() );

            super.removeCarrier( contextPath, receiver );
            bindingPropsCache.remove( contextPath );
        }
        catch ( SchedulerException e )
        {
            e.printStackTrace();
        }
    }

    private void registryJob( Map<String, Object> unitProps )
        throws Exception
    {
        scheduler.registryJob( unitProps );
    }

    private void deleteJob( String jobName, String groupName )
        throws SchedulerException
    {
        scheduler.deleteJob( jobName, groupName );
    }

    @Override
    public void addCarrier( String contextPath, Carrier receiver )
    {
        super.addCarrier( contextPath, receiver );
        Map unitProps = bindingPropsCache.get( contextPath );

        String cornExp = (String) unitProps.get( "cornExp" );
        if ( ( cornExp == null ) || ( cornExp.length() < 1 ) )
        {
            unitProps.put( "cornExp", properties.get( "cornExp" ) );
        }

        try
        {
            registryJob( unitProps );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    @Override
    public void setURI( URI uri )
    {
        this.uri = uri;
    }

    public String getProperties( String key )
    {
        Object value = properties.get( key );
        return ( ( value == null ) ? "" : (String) value );
    }

    @Override
    public void setProperties( Map<?, ?> properties )
    {
        this.properties = properties;
        String readLine_temp = getProperties( "readLine" );
        if ( readLine_temp.length() > 0 )
        {
            readLine = readLine_temp;
        }
        String setContextAction_temp = getProperties( "setContextAction" );
        if ( setContextAction_temp.length() > 0 )
        {
            setContextAction = setContextAction_temp;
        }
        String encoding_temp = getProperties( "encoding" );
        if ( encoding_temp.length() > 0 )
        {
            encoding = encoding_temp;
        }
        if ( getProperties( "buffer_size" ).length() > 0 )
        {
            buffer_size = Integer.parseInt( getProperties( "buffer_size" ) );
        }
    }

    public void setOperationName( String operationName )
    {
        this.operationName = operationName;
    }

    @Override
    public void setConfig( String contextPath, Element config )
        throws IllegalArgumentException
    {
        LOG.debug( "::::::::::::::::::::::::::::::::::::::path is:" + contextPath + ",config is:" + config );

        Map properties = new HashMap();

        if ( contextPath == null )
        {
            if ( config != null )
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
            LOG.debug( " .............SET root properties is:" + properties );
            setProperties( properties );
        }
        else
        {
            NodeList list = config.getChildNodes();
            for ( int i = 0; i < list.getLength(); ++i )
            {
                Node child = list.item( i );
                if ( child.getNodeType() == 1 )
                {
                    properties.put( child.getNodeName(), child.getTextContent().trim() );
                }
            }
            properties.put( "transport", this );
            properties.put( "taskClass", TaskImpl.class );
            properties.put( "jobName", contextPath );
            properties.put( "groupName", uri.getScheme() );
            bindingPropsCache.put( contextPath, properties );
        }
    }

    @Override
    public boolean isActive()
    {
        return active;
    }

    public abstract void executeTask( Map<?, ?> paramMap )
        throws Exception;

    class Command
        implements Runnable
    {
        private final byte[] inputBuffer;

        private final int realLen;

        private final String contextPath;

        Command( byte[] paramArrayOfByte, int paramInt, String paramString )
        {
            inputBuffer = paramArrayOfByte;
            realLen = paramInt;
            contextPath = paramString;
        }

        @Override
        public void run()
        {
            BinarySource input = new BinarySource();
            input.setBytes( inputBuffer, realLen );
            input.setEncoding( getCharacterEncoding() );
            MessageContext ctx = new MessageContext( TaskTransport.this, input );
            ctx.setPath( contextPath );
            if ( setContextAction.equals( "true" ) )
            {
                ctx.setAction( operationName );
            }

            try
            {
                postMessage( ctx );
                reply( ctx );
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
    }
}