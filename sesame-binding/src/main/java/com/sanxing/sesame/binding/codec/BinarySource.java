package com.sanxing.sesame.binding.codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.stream.StreamSource;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaElement;

public class BinarySource
    extends StreamSource
{
    private String encoding = System.getProperty( "file.encoding" );

    private XmlSchema schema;

    private XmlSchemaElement rootElement;

    private String elementName;

    private byte[] byteArray;

    private final Map<String, Object> properties = new Hashtable();

    public BinarySource()
    {
    }

    public BinarySource( InputStream inputStream )
    {
        super( inputStream );
    }

    public BinarySource( InputStream inputStream, String systemId )
    {
        super( inputStream, systemId );
    }

    public void setRootElement( XmlSchemaElement rootElement )
    {
        this.rootElement = rootElement;
    }
    
    public String getElementName()
    {
        return elementName;
    }

    public void setElementName( String elementName )
    {
        this.elementName = elementName;
    }

    public XmlSchemaElement getRootElement()
    {
        if ( rootElement != null )
        {
            return rootElement;
        }
        if ( ( schema != null ) && ( elementName != null ) )
        {
            return schema.getElementByName( elementName );
        }

        return null;
    }

    @Override
    public InputStream getInputStream()
    {
        if ( byteArray != null )
        {
            return new ByteArrayInputStream( byteArray );
        }

        return super.getInputStream();
    }

    public void setEncoding( String encoding )
    {
        this.encoding = encoding;
    }

    public String getEncoding()
    {
        return encoding;
    }

    public void setXMLSchema( XmlSchema schema )
    {
        this.schema = schema;
    }

    public XmlSchema getXMLSchema()
    {
        return schema;
    }

    public void setBytes( byte[] bytes )
    {
        byteArray = bytes;
    }

    public void setBytes( byte[] bytes, int length )
    {
        if ( bytes.length == length )
        {
            byteArray = bytes;
        }
        else
        {
            byteArray = new byte[length];
            System.arraycopy( bytes, 0, byteArray, 0, length );
        }
    }

    public byte[] getBytes()
    {
        if ( byteArray != null )
        {
            return byteArray;
        }
        try
        {
            InputStream inputStream = getInputStream();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int val;
            while ( ( val = inputStream.read() ) != -1 )
            {
                outputStream.write( val );
            }
            byteArray = outputStream.toByteArray();
            return byteArray;
        }
        catch ( IOException e )
        {
        }
        return null;
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
}