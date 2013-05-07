package com.sanxing.sesame.component;

import java.io.IOException;
import java.util.Iterator;

import javax.activation.DataHandler;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.SAXException;

import com.sanxing.sesame.jaxp.BytesSource;
import com.sanxing.sesame.jaxp.SourceTransformer;
import com.sanxing.sesame.jaxp.StringSource;

public class CopyTransformer
    implements MessageTransformer
{
    private static final CopyTransformer INSTANCE = new CopyTransformer();

    private final SourceTransformer sourceTransformer;

    private boolean copyProperties;

    private boolean copyAttachments;

    private boolean copySecuritySubject;

    public CopyTransformer()
    {
        sourceTransformer = new SourceTransformer();

        copyProperties = true;

        copyAttachments = true;

        copySecuritySubject = true;
    }

    public boolean isCopyAttachments()
    {
        return copyAttachments;
    }

    public void setCopyAttachments( boolean copyAttachments )
    {
        this.copyAttachments = copyAttachments;
    }

    public boolean isCopyProperties()
    {
        return copyProperties;
    }

    public void setCopyProperties( boolean copyProperties )
    {
        this.copyProperties = copyProperties;
    }

    public boolean isCopySecuritySubject()
    {
        return copySecuritySubject;
    }

    public void setCopySecuritySubject( boolean copySecuritySubject )
    {
        this.copySecuritySubject = copySecuritySubject;
    }

    public static CopyTransformer getInstance()
    {
        return INSTANCE;
    }

    @Override
    public boolean transform( MessageExchange exchange, NormalizedMessage from, NormalizedMessage to )
        throws MessagingException
    {
        if ( copyProperties )
        {
            copyProperties( from, to );
        }

        Source content = from.getContent();
        if ( ( ( ( content instanceof StreamSource ) || ( content instanceof SAXSource ) ) )
            && ( !( content instanceof StringSource ) ) && ( !( content instanceof BytesSource ) ) )
        {
            try
            {
                content = sourceTransformer.toDOMSource( from );
            }
            catch ( TransformerException e )
            {
                throw new MessagingException( e );
            }
            catch ( ParserConfigurationException e )
            {
                throw new MessagingException( e );
            }
            catch ( IOException e )
            {
                throw new MessagingException( e );
            }
            catch ( SAXException e )
            {
                throw new MessagingException( e );
            }
        }
        to.setContent( content );

        if ( copyAttachments )
        {
            copyAttachments( from, to );
        }

        if ( copySecuritySubject )
        {
            copySecuritySubject( from, to );
        }

        return true;
    }

    public static void copyProperties( NormalizedMessage from, NormalizedMessage to )
    {
        for ( Iterator iter = from.getPropertyNames().iterator(); iter.hasNext(); )
        {
            String name = (String) iter.next();

            if ( !( "com.sanxing.sesame.body".equals( name ) ) )
            {
                Object value = from.getProperty( name );
                to.setProperty( name, value );
            }
        }
    }

    public static void copyAttachments( NormalizedMessage from, NormalizedMessage to )
        throws MessagingException
    {
        Iterator iter = from.getAttachmentNames().iterator();
        while ( iter.hasNext() )
        {
            String name = (String) iter.next();
            DataHandler value = from.getAttachment( name );
            to.addAttachment( name, value );
        }
    }

    public static void copySecuritySubject( NormalizedMessage from, NormalizedMessage to )
        throws MessagingException
    {
        to.setSecuritySubject( from.getSecuritySubject() );
    }
}