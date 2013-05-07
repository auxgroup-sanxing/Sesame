package com.sanxing.studio.emu;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.transform.stream.StreamSource;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.xml.sax.InputSource;

import com.sanxing.sesame.binding.codec.Decoder;
import com.sanxing.sesame.binding.codec.Encoder;

public abstract class BindingUnit
{
    public static final int STOPED = 0;

    public static final int STARTED = 1;

    private final File serviceUnitRoot;

    private Map<?, ?> properties;

    private int currentState = 0;

    private final XmlSchemaCollection schemas = new XmlSchemaCollection();

    private Definition definition;

    public BindingUnit( Map<?, ?> properties, File serviceUnitRoot )
        throws Exception
    {
        this.serviceUnitRoot = serviceUnitRoot;
        setProperties( properties );
    }

    protected void setProperties( Map<?, ?> properties )
        throws Exception
    {
        this.properties = new Hashtable();
    }

    public void init()
        throws Exception
    {
        currentState = 0;
    }

    public void start()
        throws Exception
    {
        currentState = 1;
    }

    public void stop()
        throws Exception
    {
        currentState = 0;
    }

    public abstract void shutDown()
        throws Exception;

    public int getCurrentState()
    {
        return currentState;
    }

    protected abstract Decoder getDecoder()
        throws Exception;

    protected abstract Encoder getEncoder()
        throws Exception;

    protected abstract int read( InputStream paramInputStream, byte[] paramArrayOfByte )
        throws IOException;

    protected abstract void write( OutputStream paramOutputStream, byte[] paramArrayOfByte, int paramInt )
        throws IOException;

    protected org.jdom.Element raw2xml( byte[] buffer, int length, XmlSchema schema, String elementName )
        throws Exception
    {
        String charset = getProperty( "encoding" );
        Decoder decoder = getDecoder();
        ByteArrayInputStream byteStream = new ByteArrayInputStream( buffer );
        InputSource input = new InputSource( byteStream );
        input.setEncoding( charset );

        return null;
    }

    public File getUnitRoot()
    {
        return serviceUnitRoot;
    }

    protected String getProperty( String key )
    {
        return String.valueOf( properties.get( key ) );
    }

    protected int getProperty( String key, int def )
    {
        try
        {
            return Integer.parseInt( String.valueOf( properties.get( key ) ) );
        }
        catch ( Exception e )
        {
        }
        return def;
    }

    protected void dispose()
    {
        properties.clear();
    }

    protected Definition getDefinition()
    {
        return definition;
    }

    protected XmlSchema getSchema( String systemId )
    {
        synchronized ( schemas )
        {
            XmlSchema[] xsa = schemas.getXmlSchema( systemId );
            if ( xsa.length > 0 )
            {
                return xsa[( xsa.length - 1 )];
            }
            return null;
        }
    }

    protected XmlSchema schemaForNamespace( String namespace )
    {
        return schemas.schemaForNamespace( namespace );
    }

    protected XmlSchema[] getSchemas()
    {
        return schemas.getXmlSchemas();
    }

    protected XmlSchema loadSchema( URL url )
        throws IOException
    {
        InputStream inputStream = url.openStream();
        try
        {
            StreamSource inputSource = new StreamSource();
            inputSource.setInputStream( inputStream );
            inputSource.setSystemId( url.toString() );
            XmlSchema result = schemas.read( inputSource, null );
            XmlSchema localXmlSchema1 = result;

            return localXmlSchema1;
        }
        finally
        {
            inputStream.close();
        }
    }

    private XmlSchema loadSchema( org.w3c.dom.Element element, String systemId )
        throws IOException
    {
        XmlSchema result = schemas.read( element, systemId );
        return result;
    }

    protected Definition loadDefition( File wsdl )
        throws WSDLException, IOException
    {
        WSDLFactory wsdlFactory = WSDLFactory.newInstance();
        WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
        wsdlReader.setFeature( "javax.wsdl.verbose", false );
        wsdlReader.setFeature( "javax.wsdl.importDocuments", true );
        definition = wsdlReader.readWSDL( wsdl.toURL().toString() );
        List list = definition.getExtensibilityElements();
        if ( !( list.isEmpty() ) )
        {
            ExtensibilityElement extEl = (ExtensibilityElement) list.get( 0 );
            org.w3c.dom.Element schemaEl =
                ( extEl instanceof Schema ) ? ( (Schema) extEl ).getElement()
                    : ( (UnknownExtensibilityElement) extEl ).getElement();
            loadSchema( schemaEl, schemaEl.getBaseURI() );
        }
        return definition;
    }

    protected int xml2raw( org.jdom.Element dataEl, XmlSchema schema, byte[] result )
        throws Exception
    {
        // String charset = getProperty("encoding");
        // Encoder encoder = getEncoder();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        // Document document = new Document(dataEl);

        byte[] bytes = output.toByteArray();
        System.arraycopy( bytes, 0, result, 0, bytes.length );
        return bytes.length;
    }

    public static byte[] string2Bcd( String str )
    {
        if ( str.length() % 2 != 0 )
        {
            str = "0" + str;
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        char[] carray = str.toCharArray();
        for ( int i = 0; i < carray.length; i += 2 )
        {
            int high = carray[i] - '0';
            int low = carray[( i + 1 )] - '0';
            output.write( high << 4 | low );
        }
        return output.toByteArray();
    }

    public static String bcd2String( byte[] bytes )
    {
        StringBuffer sbuf = new StringBuffer();
        for ( int i = 0; i < bytes.length; ++i )
        {
            int h = ( ( bytes[i] & 0xFF ) >> 4 ) + 48;
            sbuf.append( (char) h );
            int l = ( bytes[i] & 0xF ) + 48;
            sbuf.append( (char) l );
        }
        return sbuf.toString();
    }
}