package com.sanxing.sesame.binding.codec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.activation.DataSource;
import javax.xml.transform.Result;

import org.apache.ws.commons.schema.XmlSchema;

public class BinaryResult
    implements Result
{
    private String elementName;

    private String systemId;

    private OutputStream outputStream;

    private String encoding = System.getProperty( "file.encoding" );

    private XmlSchema schema;

    private final Map<String, Object> properties = new Hashtable();

    private final Map<String, DataSource> attachments = new Hashtable();

    public BinaryResult()
    {
        setOutputStream( new ByteArrayOutputStream() );
    }

    public BinaryResult( OutputStream outputStream )
    {
        setOutputStream( outputStream );
    }

    public BinaryResult( String systemId )
    {
        this.systemId = systemId;
    }

    public void setOutputStream( OutputStream outputStream )
    {
        this.outputStream = outputStream;
    }

    public OutputStream getOutputStream()
    {
        return outputStream;
    }

    public byte[] getBytes()
    {
        if ( outputStream instanceof ByteArrayOutputStream )
        {
            return ( (ByteArrayOutputStream) outputStream ).toByteArray();
        }

        return null;
    }

    public void setEncoding( String encoding )
    {
        this.encoding = encoding;
    }

    public String getEncoding()
    {
        return encoding;
    }

    @Override
    public void setSystemId( String systemId )
    {
        this.systemId = systemId;
    }

    @Override
    public String getSystemId()
    {
        return systemId;
    }

    public void setXMLSchema( XmlSchema schema )
    {
        this.schema = schema;
    }

    public XmlSchema getXMLSchema()
    {
        return schema;
    }

    public void setElementName( String elementName )
    {
        this.elementName = elementName;
    }

    public String getElementName()
    {
        return elementName;
    }

    public Set<String> getPropertyNames()
    {
        return properties.keySet();
    }

    public Object getProperty( String name )
    {
        return properties.get( name );
    }

    public void setProperty( String name, Object value )
    {
        properties.put( name, value );
    }

    public void addAttachment( String id, DataSource source )
    {
        attachments.put( id, source );
    }

    public void removeAttachment( String id )
    {
        attachments.remove( id );
    }

    public DataSource getAttachment( String id )
    {
        return attachments.get( id );
    }

    public Set<String> getAttachmentNames()
    {
        return attachments.keySet();
    }

    public void write( byte[] b )
        throws IOException
    {
        outputStream.write( b );
    }

    public void write( byte[] b, int off, int len )
        throws IOException
    {
        outputStream.write( b, off, len );
    }
}