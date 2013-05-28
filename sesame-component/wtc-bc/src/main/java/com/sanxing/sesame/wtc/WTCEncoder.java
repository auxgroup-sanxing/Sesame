package com.sanxing.sesame.wtc;

import com.sanxing.sesame.binding.codec.BinaryResult;
import com.sanxing.sesame.binding.codec.FormatException;
import com.sanxing.sesame.binding.codec.XMLSource;
import com.sanxing.sesame.codec.impl.EncodeFSV;
import com.sanxing.sesame.codec.impl.validate.ValidateVFormat;
import com.sanxing.sesame.codec.util.HexBinary;
import com.sanxing.sesame.codec.util.RidFillBlank;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaObjectTable;
import org.jdom.Document;
import org.jdom.Element;

public class WTCEncoder
    extends EncodeFSV
{
    private static final Logger LOG = LoggerFactory.getLogger( WTCEncoder.class );

    public static final String OFFSET = "offset";

    private static final int PASSWD_FLAG = 1;

    private static final int ERRCODE_LENGTH = 7;

    private Map<String, SOProperty> properties = new Hashtable();

    public void encode( XMLSource source, BinaryResult result )
        throws FormatException
    {
        String charset = result.getEncoding();
        XmlSchema schema = result.getXMLSchema();
        OutputStream output = result.getOutputStream();
        String elementName = result.getElementName();

        if ( charset == null )
            throw new FormatException( "charset not specified!" );
        if ( schema == null )
            throw new FormatException( " schema is null!" );
        try
        {
            Document message = source.getJDOMDocument();
            if ( message == null )
                throw new FormatException( "can not get document from parameter [XMLSource]!" );
            message.getRootElement().setName( elementName );

            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( "Encode by XML schema elements: " );
                Iterator iter = schema.getElements().getNames();
                while ( iter.hasNext() )
                {
                    LOG.debug( iter.next().toString() );
                }
            }
            encode( message.getRootElement(), schema, output, charset );

            String key = String.valueOf( output.hashCode() );
            if ( getProperty( key ) != null )
            {
                SOProperty prop = removeProperty( key );
                result.setProperty( OFFSET, prop.getOffset() );
                if ( prop.getChannelFrom() != null )
                {
                    result.setProperty( "channel_from", prop.getChannelFrom() );
                }
                if ( prop.getChannelTo() != null )
                {
                    result.setProperty( "channel_to", prop.getChannelTo() );
                }

                if ( prop.getPinFlag() != null )
                {
                    result.setProperty( "pin_flag", prop.getPinFlag() );
                }

                if ( prop.getPinSeed() != null )
                {
                    result.setProperty( "pin_seed", prop.getPinSeed() );
                }

                LOG.debug( "set property: " + prop );
            }
        }
        catch ( FormatException fe )
        {
            throw fe;
        }
        catch ( Exception e )
        {
            throw new FormatException( e.getMessage(), e );
        }
    }

    public void encodeVFiled( OutputStream output, ValidateVFormat vVFormat, String elementType, String encodeCharset,
                              String elementValue, String elementName )
        throws FormatException
    {
        int headLength = vVFormat.getHeadLength();

        char headBlank = vVFormat.getHeadBlank();

        String headAlign = vVFormat.getHeadAlign();

        int headRadix = vVFormat.getHeadRadix();

        VariableHead head = new VariableHead();
        head.setHeadLength( headLength );
        head.setHeadBlank( headBlank );
        head.setHeadAlign( headAlign );
        head.setHeadRadix( headRadix );
        head.setId( vVFormat.getId() );

        VariableElement element = new VariableElement();
        element.setElementName( elementName );
        element.setEncodeCharset( encodeCharset );
        element.setElementValue( elementValue );
        element.setElementType( elementType );
        element.setHead( head );

        encodeVFiled( output, element );
    }

    private void encodeVFiled( OutputStream output, VariableElement element )
        throws FormatException
    {
        try
        {
            int length = element.getByteNumber();
            if ( length <= 250 )
            {
                encodeSingleVFiled( output, element );
            }
            else
            {
                int start = 0;
                int number = 250;
                String front = element.subString( start, number );
                String backend = element.subString( number );
                element.setElementValue( front );
                encodeSingleVFiled( output, element );

                element.setElementValue( backend );
                encodeVFiled( output, element );
            }
        }
        catch ( Exception e )
        {
            throw new FormatException( e.getMessage(), e );
        }
    }

    private void encodePasswdField( String key, byte[] bytes )
    {
        SOProperty prop = getProperty( key );
        if ( prop != null )
        {
            byte[] offset = prop.getOffset();
            if ( offset != null )
                prop.setOffset( ByteUtil.cancat( offset, bytes ) );
            else
                prop.setOffset( bytes );
        }
        else
        {
            prop = new SOProperty();
            prop.setOffset( bytes );
            setProperty( key, prop );
        }
    }

    private void encodeSkipField( String key, String elementName, String elementValue )
    {
        SOProperty prop = getProperty( key );
        if ( prop == null )
        {
            prop = new SOProperty();
        }
        if ( elementName.equalsIgnoreCase( "channel_from" ) )
            prop.setChannelFrom( elementValue );
        else if ( elementName.equalsIgnoreCase( "channel_to" ) )
            prop.setChannelTo( elementValue );
        else if ( elementName.equalsIgnoreCase( "pin_flag" ) )
            prop.setPinFlag( elementValue );
        else if ( elementName.equalsIgnoreCase( "pin_seed" ) )
        {
            prop.setPinSeed( elementValue );
        }
        setProperty( key, prop );
    }

    private void encodeSingleVFiled( OutputStream output, VariableElement element )
        throws Exception
    {
        VariableHead head = element.getHead();
        int headLength = head.getHeadLength();
        char headBlank = head.getHeadBlank();
        String headAlign = head.getHeadAlign();
        int headRadix = head.getHeadRadix();
        byte[] headBytes = element.getHeadBytes();
        String key = String.valueOf( output.hashCode() );

        int id = head.getId();
        if ( id == PASSWD_FLAG )
        {
            short size = (short) ( ( (ByteArrayOutputStream) output ).size() + 107 );
            byte[] bytes = ByteUtil.shortToByte( size );
            encodePasswdField( key, bytes );

            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( "id: " + id + "; elementName: " + element.getElementName() );
                LOG.debug( "password offset: " + size );
            }

        }

        if ( skipElement( element.getElementName() ) )
        {
            encodeSkipField( key, element.getElementName(), element.getElementValue() );
            return;
        }

        encodeHead( output, headBytes, headLength, headBlank, headAlign, headRadix, element.getEncodeCharset(),
            element.getElementName() );

        if ( "string".equals( element.getElementType() ) )
            output.write( element.getElementValue().getBytes( element.getEncodeCharset() ) );
        else
            output.write( HexBinary.decode( element.getElementValue() ) );
    }

    public void encodeHead( OutputStream output, byte[] headBytes, int headLength, char headBlank, String headAlign,
                            int headRadix, String encodeCharset, String elementName )
        throws FormatException
    {
        byte[] bytes = fillBlankByte( headBytes, headLength, headBlank, headAlign, encodeCharset, elementName );
        try
        {
            output.write( bytes );
        }
        catch ( IOException e )
        {
            throw new FormatException( e.getMessage(), e );
        }
    }

    public static byte[] fillBlankByte( byte[] elementBytes, int length, char blank, String align,
                                        String encodeCharset, String elementName )
        throws FormatException
    {
        ByteBuffer bf = ByteBuffer.allocate( length );
        int len = elementBytes.length;

        if ( len > length )
            throw new FormatException( "element:[" + elementName
                + "],length of elementValue is more longer then xsd defind len:[" + length + "]" );
        if ( len == length )
        {
            return elementBytes;
        }

        if ( "L".equals( align ) )
        {
            bf.put( elementBytes );
            while ( bf.position() <= bf.limit() - 2 )
                bf.putChar( blank );
        }
        else
        {
            while ( bf.position() <= bf.limit() - 2 - len )
            {
                bf.putChar( blank );
            }
            bf.put( elementBytes );
        }

        return bf.array();
    }

    public void encodeNonNumber( OutputStream output, String elementValue, int length, char blank, String align,
                                 String encodeCharset, String elementName )
        throws FormatException
    {
        String chgValue = elementValue;
        try
        {
            int eleLen = elementValue.getBytes( encodeCharset ).length;
            if ( ( eleLen > length ) && ( elementName.equalsIgnoreCase( "errorCode" ) ) )
            {
                String filtered = elementValue.replaceAll( "\\.", "" );
                if ( filtered.length() > ERRCODE_LENGTH )
                    elementValue = filtered.substring( 0, ERRCODE_LENGTH );
                else
                {
                    elementValue = filtered;
                }
            }
            chgValue = RidFillBlank.fillBlank( elementValue, length, blank, align, encodeCharset, elementName );

            output.write( chgValue.getBytes( encodeCharset ) );
        }
        catch ( Exception e )
        {
            throw new FormatException( e.getMessage(), e );
        }
    }

    public SOProperty getProperty( String name )
    {
        return (SOProperty) this.properties.get( name );
    }

    public void setProperty( String name, SOProperty value )
    {
        this.properties.put( name, value );
    }

    public SOProperty removeProperty( String name )
    {
        return (SOProperty) this.properties.remove( name );
    }

    public boolean skipElement( String elementName )
    {
        if ( ( elementName.equalsIgnoreCase( "channel_from" ) ) || ( elementName.equalsIgnoreCase( "channel_to" ) )
            || ( elementName.equalsIgnoreCase( "pin_flag" ) ) || ( elementName.equalsIgnoreCase( "pin_seed" ) ) )
        {
            return true;
        }
        return false;
    }

    class SOProperty
    {
        private byte[] offset;

        private String channelTo;

        private String channelFrom;

        private String pinSeed;

        private String pinFlag;

        SOProperty()
        {
        }

        public byte[] getOffset()
        {
            return this.offset;
        }

        public void setOffset( byte[] offset )
        {
            this.offset = offset;
        }

        public String getChannelTo()
        {
            return this.channelTo;
        }

        public void setChannelTo( String channelTo )
        {
            this.channelTo = channelTo;
        }

        public String getChannelFrom()
        {
            return this.channelFrom;
        }

        public void setChannelFrom( String channelFrom )
        {
            this.channelFrom = channelFrom;
        }

        public String getPinSeed()
        {
            return this.pinSeed;
        }

        public void setPinSeed( String pinSeed )
        {
            this.pinSeed = pinSeed;
        }

        public String getPinFlag()
        {
            return this.pinFlag;
        }

        public void setPinFlag( String pinFlag )
        {
            this.pinFlag = pinFlag;
        }

        public String toString()
        {
            return "SOProperty [offset=" + Arrays.toString( this.offset ) + ", channelTo=" + this.channelTo
                + ", channelFrom=" + this.channelFrom + ", pinSeed=" + this.pinSeed + ", pinFlag=" + this.pinFlag + "]";
        }
    }
}
