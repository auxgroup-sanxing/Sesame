package com.sanxing.sesame.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.security.auth.Subject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.SAXException;

import com.sanxing.sesame.constants.ExchangeConst;
import com.sanxing.sesame.jaxp.SourceTransformer;
import com.sanxing.sesame.jaxp.StringSource;

public final class MessageUtil
{
    public static void transfer( NormalizedMessage source, NormalizedMessage dest )
        throws MessagingException
    {
        dest.setContent( source.getContent() );
        for ( Iterator it = source.getPropertyNames().iterator(); it.hasNext(); )
        {
            String name = (String) it.next();
            dest.setProperty( name, source.getProperty( name ) );
        }
        for ( Iterator it = source.getAttachmentNames().iterator(); it.hasNext(); )
        {
            String name = (String) it.next();
            dest.addAttachment( name, source.getAttachment( name ) );
        }
        dest.setSecuritySubject( source.getSecuritySubject() );
    }

    public static NormalizedMessage copy( NormalizedMessage source )
        throws MessagingException
    {
        if ( source instanceof Fault )
        {
            return new FaultImpl( (Fault) source );
        }
        return new NormalizedMessageImpl( source );
    }

    public static NormalizedMessage copyIn( MessageExchange exchange )
        throws MessagingException
    {
        return copy( exchange.getMessage( ExchangeConst.IN ) );
    }

    public static NormalizedMessage copyOut( MessageExchange exchange )
        throws MessagingException
    {
        return copy( exchange.getMessage( ExchangeConst.OUT ) );
    }

    public static Fault copyFault( MessageExchange exchange )
        throws MessagingException
    {
        return ( (Fault) copy( exchange.getMessage( ExchangeConst.FAULT ) ) );
    }

    public static void transferInToIn( MessageExchange source, MessageExchange dest )
        throws MessagingException
    {
        transferToIn( source.getMessage( ExchangeConst.IN ), dest );
    }

    public static void transferOutToIn( MessageExchange source, MessageExchange dest )
        throws MessagingException
    {
        transferToIn( source.getMessage( ExchangeConst.OUT ), dest );
    }

    public static void transferToIn( NormalizedMessage sourceMsg, MessageExchange dest )
        throws MessagingException
    {
        transferTo( sourceMsg, dest, ExchangeConst.IN );
    }

    public static void transferOutToOut( MessageExchange source, MessageExchange dest )
        throws MessagingException
    {
        transferToOut( source.getMessage( ExchangeConst.OUT ), dest );
    }

    public static void transferInToOut( MessageExchange source, MessageExchange dest )
        throws MessagingException
    {
        transferToOut( source.getMessage( ExchangeConst.IN ), dest );
    }

    public static void transferToOut( NormalizedMessage sourceMsg, MessageExchange dest )
        throws MessagingException
    {
        transferTo( sourceMsg, dest, ExchangeConst.OUT );
    }

    public static void transferFaultToFault( MessageExchange source, MessageExchange dest )
        throws MessagingException
    {
        transferToFault( source.getFault(), dest );
    }

    public static void transferToFault( Fault fault, MessageExchange dest )
        throws MessagingException
    {
        transferTo( fault, dest, ExchangeConst.FAULT );
    }

    public static void transferTo( NormalizedMessage sourceMsg, MessageExchange dest, String name )
        throws MessagingException
    {
        NormalizedMessage destMsg = ( sourceMsg instanceof Fault ) ? dest.createFault() : dest.createMessage();
        transfer( sourceMsg, destMsg );
        dest.setMessage( destMsg, name );
    }

    public static void transferTo( MessageExchange source, MessageExchange dest, String name )
        throws MessagingException
    {
        NormalizedMessage sourceMsg = source.getMessage( name );
        NormalizedMessage destMsg = ( sourceMsg instanceof Fault ) ? dest.createFault() : dest.createMessage();
        transfer( sourceMsg, destMsg );
        dest.setMessage( destMsg, name );
    }

    public static void enableContentRereadability( NormalizedMessage message )
        throws MessagingException
    {
        if ( !( message.getContent() instanceof StreamSource ) )
        {
            return;
        }
        try
        {
            String content = new SourceTransformer().contentToString( message );
            if ( content != null )
            {
                message.setContent( new StringSource( content ) );
            }
        }
        catch ( TransformerException e )
        {
            throw new MessagingException( "Unable to convert message content into StringSource", e );
        }
        catch ( ParserConfigurationException e )
        {
            throw new MessagingException( "Unable to convert message content into StringSource", e );
        }
        catch ( IOException e )
        {
            throw new MessagingException( "Unable to convert message content into StringSource", e );
        }
        catch ( SAXException e )
        {
            throw new MessagingException( "Unable to convert message content into StringSource", e );
        }
    }

    public static class FaultImpl
        extends MessageUtil.NormalizedMessageImpl
        implements Fault
    {
        private static final long serialVersionUID = -6076815664102825860L;

        public FaultImpl()
        {
        }

        public FaultImpl( Fault fault )
            throws MessagingException
        {
            super( fault );
        }
    }

    public static class NormalizedMessageImpl
        implements NormalizedMessage, Serializable
    {
        private static final long serialVersionUID = -5813947566001096708L;

        private Subject subject;

        private Source content;

        private final Map properties = new HashMap();

        private final Map attachments = new HashMap();

        public NormalizedMessageImpl()
        {
        }

        public NormalizedMessageImpl( NormalizedMessage message )
            throws MessagingException
        {
            try
            {
                String str = new SourceTransformer().contentToString( message );
                if ( str != null )
                {
                    content = new StringSource( str );
                }
                for ( Iterator it = message.getPropertyNames().iterator(); it.hasNext(); )
                {
                    String name = (String) it.next();
                    properties.put( name, message.getProperty( name ) );
                }
                for ( Iterator it = message.getAttachmentNames().iterator(); it.hasNext(); )
                {
                    String name = (String) it.next();
                    DataHandler dh = message.getAttachment( name );
                    DataSource ds = dh.getDataSource();
                    if ( !( ds instanceof ByteArrayDataSource ) )
                    {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        FileUtil.copyInputStream( ds.getInputStream(), baos );
                        ByteArrayDataSource bads = new ByteArrayDataSource( baos.toByteArray(), ds.getContentType() );
                        bads.setName( ds.getName() );
                        dh = new DataHandler( bads );
                    }
                    attachments.put( name, dh );
                }
                subject = message.getSecuritySubject();
            }
            catch ( MessagingException e )
            {
                throw e;
            }
            catch ( Exception e )
            {
                throw new MessagingException( e );
            }
        }

        @Override
        public void addAttachment( String id, DataHandler data )
            throws MessagingException
        {
            attachments.put( id, data );
        }

        @Override
        public Source getContent()
        {
            return content;
        }

        @Override
        public DataHandler getAttachment( String id )
        {
            return ( (DataHandler) attachments.get( id ) );
        }

        @Override
        public Set getAttachmentNames()
        {
            return attachments.keySet();
        }

        @Override
        public void removeAttachment( String id )
            throws MessagingException
        {
            attachments.remove( id );
        }

        @Override
        public void setContent( Source content )
            throws MessagingException
        {
            this.content = content;
        }

        @Override
        public void setProperty( String name, Object value )
        {
            properties.put( name, value );
        }

        @Override
        public void setSecuritySubject( Subject sub )
        {
            subject = sub;
        }

        @Override
        public Set getPropertyNames()
        {
            return properties.keySet();
        }

        @Override
        public Object getProperty( String name )
        {
            return properties.get( name );
        }

        @Override
        public Subject getSecuritySubject()
        {
            return subject;
        }
    }
}