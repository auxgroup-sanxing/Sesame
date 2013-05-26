package com.sanxing.sesame.binding.soap;

import com.ibm.wsdl.extensions.soap.SOAPAddressImpl;
import com.sanxing.sesame.binding.soap.util.WSDLMerge;
import com.sanxing.sesame.util.WSDLLocatorImpl;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLLocator;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SAAJServlet
    extends HttpServlet
{
    private static final Logger log = LoggerFactory.getLogger( SAAJServlet.class );

    private static final long serialVersionUID = -759250743584297018L;

    private Map<String, SOAPEndPoint> mapping = new HashMap();

    protected SOAPConnection soapConnection = null;

    protected MessageFactory messageFactory = null;

    public void init( ServletConfig servletConfig )
        throws ServletException
    {
        super.init( servletConfig );
        try
        {
            this.messageFactory = MessageFactory.newInstance();
            SOAPConnectionFactory connFactory = SOAPConnectionFactory.newInstance();
            this.soapConnection = connFactory.createConnection();
        }
        catch ( SOAPException e )
        {
            log.error( e.getMessage(), e );
            throw new ServletException( "Unable to create message factory: " + e.getMessage() );
        }
    }

    public void destroy()
    {
        this.mapping.clear();
        super.destroy();
    }

    public void setMessageFactory( MessageFactory factory )
    {
        this.messageFactory = factory;
    }

    public MessageFactory getMessageFactory()
    {
        return this.messageFactory;
    }

    protected static MimeHeaders getHeaders( HttpServletRequest req )
    {
        Enumeration enumeration = req.getHeaderNames();
        MimeHeaders headers = new MimeHeaders();
        while (enumeration.hasMoreElements())
        {
            String headerName = (String) enumeration.nextElement();
            String headerValue = req.getHeader( headerName );

            StringTokenizer values = new StringTokenizer( headerValue, "," );
            while (values.hasMoreTokens())
                headers.addHeader( headerName, values.nextToken().trim() );
        }

        return headers;
    }

    protected static void putHeaders( MimeHeaders headers, HttpServletResponse response )
    {
        Iterator it = headers.getAllHeaders();

        while ( it.hasNext() )
        {
            MimeHeader header = (MimeHeader) it.next();
            String[] values = headers.getHeader( header.getName() );
            if ( values.length == 1 )
            {
                response.setHeader( header.getName(), header.getValue() );
            }
            else
            {
                StringBuffer concat = new StringBuffer();
                int i = 0;
                while ( i < values.length )
                {
                    if ( i != 0 )
                    {
                        concat.append( ',' );
                    }
                    concat.append( values[( i++ )] );
                }
                response.setHeader( header.getName(), concat.toString() );
            }
        }
    }

    public void doPost( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        SOAPEndPoint endpoint = (SOAPEndPoint) this.mapping.get( request.getLocalPort() + request.getRequestURI() );
        if ( endpoint == null )
        {
            response.sendError( 404 );
            return;
        }
        try
        {
            MimeHeaders headers = getHeaders( request );

            InputStream input = request.getInputStream();

            SOAPMessage messsage = this.messageFactory.createMessage( headers, input );

            SOAPMessage reply = endpoint.processRequest( messsage, request );

            if ( reply != null )
            {
                if ( reply.saveRequired() )
                {
                    reply.saveChanges();
                }

                response.setStatus( 200 );

                putHeaders( reply.getMimeHeaders(), response );

                OutputStream os = response.getOutputStream();

                reply.writeTo( os );

                os.flush();
            }
            else
            {
                response.setStatus( 204 );
            }
        }
        catch ( Exception ex )
        {
            log.error( ex.getMessage(), ex );
            throw new ServletException( "SAAJ POST failed", ex );
        }
    }

    public void doGet( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        SOAPEndPoint servlet = (SOAPEndPoint) this.mapping.get( request.getLocalPort() + request.getRequestURI() );
        if ( servlet == null )
        {
            response.sendError( 404 );
            return;
        }

        if ( request.getParameterMap().containsKey( "wsdl" ) )
            try
            {
                WSDLMerge merge = new WSDLMerge();
                WSDLFactory wsdlFactory = WSDLFactory.newInstance();
                WSDLReader reader = wsdlFactory.newWSDLReader();
                WSDLLocator locator = new WSDLLocatorImpl( servlet.getDefinition().getDocumentBaseURI() );
                Definition sourceDef = reader.readWSDL( locator );
                Definition definition = merge.merge( sourceDef );

                Collection portTypes = definition.getPortTypes().values();
                Map services = definition.getServices();
                for ( Iterator it = services.values().iterator(); it.hasNext(); )
                {
                    Service service = (Service) it.next();
                    Map ports = service.getPorts();
                    Iterator ite = ports.values().iterator();
                    while (ite.hasNext())
                    {
                        Port port = (Port) ite.next();
                        Binding binding = port.getBinding();
                        if ( binding != null )
                        {
                            PortType oldportType = binding.getPortType();
                            Iterator iter = portTypes.iterator();
                            while ( iter.hasNext() )
                            {
                                PortType portType = (PortType) iter.next();
                                if ( portType.getQName().getLocalPart().equalsIgnoreCase(
                                    oldportType.getQName().getLocalPart() ) )
                                {
                                    binding.setPortType( portType );
                                    break;
                                }
                            }

                        }

                        QName sesa_location = new QName( "http://www.sanxing.com/ns/sesame", "location" );
                        QName location = (QName) port.getExtensionAttribute( sesa_location );
                        if ( location != null )
                        {
                            SOAPAddress address = new SOAPAddressImpl();
                            address.setLocationURI( location.getLocalPart() );
                            port.addExtensibilityElement( address );
                            port.getExtensionAttributes().remove( sesa_location );
                        }
                    }
                }

                WSDLWriter wsdlWriter = wsdlFactory.newWSDLWriter();
                wsdlWriter.writeWSDL( definition, response.getOutputStream() );
                response.getOutputStream().flush();
            }
            catch ( WSDLException e )
            {
                throw new ServletException( e.getMessage(), e );
            }
            catch ( Throwable e )
            {
                throw new ServletException( e.getMessage(), e );
            }
    }

    public void addMapping( String endpointAddr, SOAPEndPoint endpoint )
    {
        this.mapping.put( endpointAddr, endpoint );
    }

    public void removeMapping( String endpointAddr )
    {
        this.mapping.remove( endpointAddr );
    }
}