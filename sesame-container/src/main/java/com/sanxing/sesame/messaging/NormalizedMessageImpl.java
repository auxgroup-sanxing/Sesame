package com.sanxing.sesame.messaging;

import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
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
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import com.sanxing.sesame.exception.RuntimeJBIException;
import com.sanxing.sesame.jaxp.BytesSource;
import com.sanxing.sesame.jaxp.SourceTransformer;
import com.sanxing.sesame.jaxp.StringSource;
import com.sanxing.sesame.util.ByteArrayDataSource;
import com.sanxing.sesame.util.FileUtil;

public class NormalizedMessageImpl
    implements NormalizedMessage, Externalizable
{
    private static final long serialVersionUID = 9179194301410526549L;

    private static final SourceTransformer TRANSFORMER = new SourceTransformer();

    protected transient MessageExchangeImpl exchange;

    private transient Source content;

    private transient Object body;

    private Subject securitySubject;

    private Map properties;

    private Map attachments;

    public NormalizedMessageImpl()
    {
    }

    public NormalizedMessageImpl( MessageExchangeImpl exchange )
    {
        this.exchange = exchange;
    }

    @Override
    public Source getContent()
    {
        if ( ( content == null ) && ( body != null ) )
        {
            try
            {
                getMarshaler().marshal( exchange, this, body );
            }
            catch ( MessagingException e )
            {
                throw new RuntimeJBIException( e );
            }
        }
        return content;
    }

    @Override
    public void setContent( Source source )
    {
        content = source;
    }

    @Override
    public Subject getSecuritySubject()
    {
        return securitySubject;
    }

    @Override
    public void setSecuritySubject( Subject securitySubject )
    {
        this.securitySubject = securitySubject;
    }

    @Override
    public Object getProperty( String name )
    {
        if ( properties != null )
        {
            return properties.get( name );
        }
        return null;
    }

    @Override
    public Set getPropertyNames()
    {
        if ( properties != null )
        {
            return Collections.unmodifiableSet( properties.keySet() );
        }
        return Collections.EMPTY_SET;
    }

    @Override
    public void setProperty( String name, Object value )
    {
        if ( value == null )
        {
            if ( properties != null )
            {
                properties.remove( name );
            }
        }
        else
        {
            getProperties().put( name, value );
        }
    }

    @Override
    public void addAttachment( String id, DataHandler handler )
    {
        getAttachments().put( id, handler.getDataSource() );
    }

    @Override
    public DataHandler getAttachment( String id )
    {
        if ( ( attachments != null ) && ( attachments.get( id ) != null ) )
        {
            return new DataHandler( (DataSource) attachments.get( id ) );
        }
        return null;
    }

    public Iterator listAttachments()
    {
        if ( attachments != null )
        {
            return attachments.keySet().iterator();
        }
        return Collections.EMPTY_LIST.iterator();
    }

    @Override
    public void removeAttachment( String id )
    {
        if ( attachments != null )
        {
            attachments.remove( id );
        }
    }

    @Override
    public Set getAttachmentNames()
    {
        if ( attachments != null )
        {
            return Collections.unmodifiableSet( attachments.keySet() );
        }
        return Collections.EMPTY_SET;
    }

    @Override
    public String toString()
    {
        return super.toString() + "{properties: " + getProperties() + "}";
    }

    public Object getBody()
        throws MessagingException
    {
        if ( body == null )
        {
            body = getMarshaler().unmarshal( exchange, this );
        }
        return body;
    }

    public Object getBody( PojoMarshaler marshaler )
        throws MessagingException
    {
        return marshaler.unmarshal( exchange, this );
    }

    public void setBody( Object body )
        throws MessagingException
    {
        this.body = body;
    }

    public String getBodyText()
        throws TransformerException
    {
        return TRANSFORMER.toString( getContent() );
    }

    public void setBodyText( String xml )
    {
        setContent( new StringSource( xml ) );
    }

    public PojoMarshaler getMarshaler()
    {
        return exchange.getMarshaler();
    }

    public MessageExchange getExchange()
    {
        return exchange;
    }

    public Fault createFault()
        throws MessagingException
    {
        return getExchange().createFault();
    }

    protected Map getProperties()
    {
        if ( properties == null )
        {
            properties = createPropertiesMap();
        }
        return properties;
    }

    protected Map getAttachments()
    {
        if ( attachments == null )
        {
            attachments = createAttachmentsMap();
        }
        return attachments;
    }

    protected void setAttachments( Map attachments )
    {
        this.attachments = attachments;
    }

    protected void setProperties( Map properties )
    {
        this.properties = properties;
    }

    protected Map createPropertiesMap()
    {
        return new HashMap();
    }

    protected Map createAttachmentsMap()
    {
        return new HashMap();
    }

    @Override
    public void writeExternal( ObjectOutput out )
        throws IOException
    {
        try
        {
            convertAttachments();
            out.writeObject( attachments );
            out.writeObject( properties );
            String src = TRANSFORMER.toString( content );
            out.writeObject( src );

            if ( ( ( ( content instanceof StreamSource ) || ( content instanceof SAXSource ) ) )
                && ( !( content instanceof StringSource ) ) && ( !( content instanceof BytesSource ) ) )
            {
                content = new StringSource( src );
            }
        }
        catch ( TransformerException e )
        {
            throw ( (IOException) new IOException( "Could not transform content to string" ).initCause( e ) );
        }
    }

    private void convertAttachments()
        throws IOException
    {
        if ( attachments != null )
        {
            Map newAttachments = createAttachmentsMap();
            for ( Iterator it = attachments.keySet().iterator(); it.hasNext(); )
            {
                String name = (String) it.next();
                DataSource ds = (DataSource) attachments.get( name );
                if ( ds instanceof ByteArrayDataSource )
                {
                    newAttachments.put( name, ds );
                }
                else
                {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    FileUtil.copyInputStream( ds.getInputStream(), baos );
                    ByteArrayDataSource bads = new ByteArrayDataSource( baos.toByteArray(), ds.getContentType() );
                    bads.setName( ds.getName() );
                    newAttachments.put( name, bads );
                }
            }
            attachments = newAttachments;
        }
    }

    @Override
    public void readExternal( ObjectInput in )
        throws IOException, ClassNotFoundException
    {
        attachments = ( (Map) in.readObject() );
        properties = ( (Map) in.readObject() );
        String src = (String) in.readObject();
        if ( src != null )
        {
            content = new StringSource( src );
        }
    }
}