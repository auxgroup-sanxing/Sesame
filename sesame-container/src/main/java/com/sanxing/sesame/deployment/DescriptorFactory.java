package com.sanxing.sesame.deployment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sanxing.sesame.util.FileUtil;
import com.sanxing.sesame.util.W3CUtil;

public final class DescriptorFactory
{
    public static final String DESCRIPTOR_FILE = "META-INF/jbi.xml";

    private static final String XSD_SCHEMA_LANGUAGE = "http://www.w3.org/2001/XMLSchema";

    private static final Logger LOG = LoggerFactory.getLogger( DescriptorFactory.class );

    public static Descriptor buildDescriptor( File descriptorFile )
    {
        if ( descriptorFile.isDirectory() )
        {
            descriptorFile = new File( descriptorFile, "jbi.xml" );
        }
        if ( descriptorFile.isFile() )
        {
            try
            {
                return buildDescriptor( descriptorFile.toURL() );
            }
            catch ( MalformedURLException e )
            {
                throw new RuntimeException( "There is a bug here...", e );
            }
        }
        return null;
    }

    public static Descriptor buildDescriptor( final URL url )
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileUtil.copyInputStream( url.openStream(), baos );

            SchemaFactory schemaFactory = SchemaFactory.newInstance( "http://www.w3.org/2001/XMLSchema" );
            Schema schema = schemaFactory.newSchema( DescriptorFactory.class.getResource( "/jbi-descriptor.xsd" ) );
            Validator validator = schema.newValidator();
            validator.setErrorHandler( new ErrorHandler()
            {
                @Override
                public void warning( SAXParseException exception )
                    throws SAXException
                {
                    DescriptorFactory.LOG.debug( "Validation warning on " + url + ": " + exception );
                }

                @Override
                public void error( SAXParseException exception )
                    throws SAXException
                {
                    DescriptorFactory.LOG.info( "Validation error on " + url + ": " + exception );
                }

                @Override
                public void fatalError( SAXParseException exception )
                    throws SAXException
                {
                    throw exception;
                }
            } );
            validator.validate( new StreamSource( new ByteArrayInputStream( baos.toByteArray() ) ) );

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware( true );
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            Document doc = docBuilder.parse( new ByteArrayInputStream( baos.toByteArray() ) );
            Element jbi = doc.getDocumentElement();
            Descriptor desc = new Descriptor();
            desc.setVersion( Double.parseDouble( getAttribute( jbi, "version" ) ) );
            Element child = W3CUtil.getFirstChildElement( jbi );
            if ( "component".equals( child.getLocalName() ) )
            {
                Component component = parseComponent( child );
                desc.setComponent( component );
            }
            else if ( "shared-library".equals( child.getLocalName() ) )
            {
                SharedLibrary sharedLibrary = parseSharedLibrary( child );
                desc.setSharedLibrary( sharedLibrary );
            }
            else if ( "service-assembly".equals( child.getLocalName() ) )
            {
                ServiceAssembly serviceAssembly = parseServiceAssembly( child );
                desc.setServiceAssembly( serviceAssembly );
            }
            else if ( "services".equals( child.getLocalName() ) )
            {
                Services services = parseServiceUnit( child );
                desc.setServices( services );
            }
            checkDescriptor( desc );
            return desc;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    private static Services parseServiceUnit( Element child )
    {
        Services services = new Services();
        services.setBindingComponent( Boolean.valueOf( getAttribute( child, "binding-component" ) ).booleanValue() );
        List provides = new ArrayList();
        List consumes = new ArrayList();
        for ( Element e = W3CUtil.getFirstChildElement( child ); e != null; e = W3CUtil.getNextSiblingElement( e ) )
        {
            if ( "provides".equals( e.getLocalName() ) )
            {
                Provides p = new Provides();
                p.setInterfaceName( readAttributeQName( e, "interface-name" ) );
                p.setServiceName( readAttributeQName( e, "service-name" ) );
                p.setEndpointName( getAttribute( e, "endpoint-name" ) );
                provides.add( p );
            }
            else if ( "consumes".equals( e.getLocalName() ) )
            {
                Consumes c = new Consumes();
                c.setInterfaceName( readAttributeQName( e, "interface-name" ) );
                c.setServiceName( readAttributeQName( e, "service-name" ) );
                c.setEndpointName( getAttribute( e, "endpoint-name" ) );
                c.setLinkType( getAttribute( e, "link-type" ) );
                consumes.add( c );
            }
        }
        services.setProvides( (Provides[]) provides.toArray( new Provides[provides.size()] ) );
        services.setConsumes( (Consumes[]) consumes.toArray( new Consumes[consumes.size()] ) );
        return services;
    }

    private static ServiceAssembly parseServiceAssembly( Element child )
    {
        ServiceAssembly serviceAssembly = new ServiceAssembly();
        List sus = new ArrayList();
        for ( Element e = W3CUtil.getFirstChildElement( child ); e != null; e = W3CUtil.getNextSiblingElement( e ) )
        {
            if ( "identification".equals( e.getLocalName() ) )
            {
                serviceAssembly.setIdentification( readIdentification( e ) );
            }
            else if ( "service-unit".equals( e.getLocalName() ) )
            {
                ServiceUnit su = new ServiceUnit();
                for ( Element e2 = W3CUtil.getFirstChildElement( e ); e2 != null; e2 =
                    W3CUtil.getNextSiblingElement( e2 ) )
                {
                    if ( "identification".equals( e2.getLocalName() ) )
                    {
                        su.setIdentification( readIdentification( e2 ) );
                    }
                    else if ( "target".equals( e2.getLocalName() ) )
                    {
                        Target target = new Target();
                        for ( Element e3 = W3CUtil.getFirstChildElement( e2 ); e3 != null; e3 =
                            W3CUtil.getNextSiblingElement( e3 ) )
                        {
                            if ( "artifacts-zip".equals( e3.getLocalName() ) )
                            {
                                target.setArtifactsZip( getText( e3 ) );
                            }
                            else if ( "component-name".equals( e3.getLocalName() ) )
                            {
                                target.setComponentName( getText( e3 ) );
                            }
                        }
                        su.setTarget( target );
                    }
                }
                sus.add( su );
            }
            else if ( "connections".equals( e.getLocalName() ) )
            {
                Connections connections = new Connections();
                List cns = new ArrayList();
                for ( Element e2 = W3CUtil.getFirstChildElement( e ); e2 != null; e2 =
                    W3CUtil.getNextSiblingElement( e2 ) )
                {
                    if ( "connection".equals( e2.getLocalName() ) )
                    {
                        Connection cn = new Connection();
                        for ( Element e3 = W3CUtil.getFirstChildElement( e2 ); e3 != null; e3 =
                            W3CUtil.getNextSiblingElement( e3 ) )
                        {
                            if ( "consumer".equals( e3.getLocalName() ) )
                            {
                                Consumer consumer = new Consumer();
                                consumer.setInterfaceName( readAttributeQName( e3, "interface-name" ) );
                                consumer.setServiceName( readAttributeQName( e3, "service-name" ) );
                                consumer.setEndpointName( getAttribute( e3, "endpoint-name" ) );
                                cn.setConsumer( consumer );
                            }
                            else if ( "provider".equals( e3.getLocalName() ) )
                            {
                                Provider provider = new Provider();
                                provider.setServiceName( readAttributeQName( e3, "service-name" ) );
                                provider.setEndpointName( getAttribute( e3, "endpoint-name" ) );
                                cn.setProvider( provider );
                            }
                        }
                        cns.add( cn );
                    }
                }
                connections.setConnections( (Connection[]) cns.toArray( new Connection[cns.size()] ) );
                serviceAssembly.setConnections( connections );
            }
        }
        serviceAssembly.setServiceUnits( (ServiceUnit[]) sus.toArray( new ServiceUnit[sus.size()] ) );
        return serviceAssembly;
    }

    private static SharedLibrary parseSharedLibrary( Element child )
    {
        SharedLibrary sharedLibrary = new SharedLibrary();
        sharedLibrary.setClassLoaderDelegation( getAttribute( child, "class-loader-delegation" ) );
        sharedLibrary.setVersion( getAttribute( child, "version" ) );
        for ( Element e = W3CUtil.getFirstChildElement( child ); e != null; e = W3CUtil.getNextSiblingElement( e ) )
        {
            if ( "identification".equals( e.getLocalName() ) )
            {
                sharedLibrary.setIdentification( readIdentification( e ) );
            }
            else if ( "shared-library-class-path".equals( e.getLocalName() ) )
            {
                ClassPath sharedLibraryClassPath = new ClassPath();
                List l = new ArrayList();
                for ( Element e2 = W3CUtil.getFirstChildElement( e ); e2 != null; e2 =
                    W3CUtil.getNextSiblingElement( e2 ) )
                {
                    if ( "path-element".equals( e2.getLocalName() ) )
                    {
                        l.add( getText( e2 ) );
                    }
                }
                sharedLibraryClassPath.setPathList( l );
                sharedLibrary.setSharedLibraryClassPath( sharedLibraryClassPath );
            }
            else if ( "callback-class".equals( e.getLocalName() ) )
            {
                String callbackClazz = W3CUtil.getElementText( e );
                sharedLibrary.setCallbackClazz( callbackClazz );
            }
        }
        return sharedLibrary;
    }

    private static Component parseComponent( Element child )
    {
        Component component = new Component();
        component.setType( child.getAttribute( "type" ) );
        component.setComponentClassLoaderDelegation( getAttribute( child, "component-class-loader-delegation" ) );
        component.setBootstrapClassLoaderDelegation( getAttribute( child, "bootstrap-class-loader-delegation" ) );
        List sls = new ArrayList();
        DocumentFragment ext = null;
        for ( Element e = W3CUtil.getFirstChildElement( child ); e != null; e = W3CUtil.getNextSiblingElement( e ) )
        {
            if ( "identification".equals( e.getLocalName() ) )
            {
                component.setIdentification( readIdentification( e ) );
            }
            else if ( "component-class-name".equals( e.getLocalName() ) )
            {
                component.setComponentClassName( getText( e ) );
                component.setDescription( getAttribute( e, "description" ) );
            }
            else if ( "component-class-path".equals( e.getLocalName() ) )
            {
                ClassPath componentClassPath = new ClassPath();
                List l = new ArrayList();
                for ( Element e2 = W3CUtil.getFirstChildElement( e ); e2 != null; e2 =
                    W3CUtil.getNextSiblingElement( e2 ) )
                {
                    if ( "path-element".equals( e2.getLocalName() ) )
                    {
                        l.add( getText( e2 ) );
                    }
                }
                componentClassPath.setPathList( l );
                component.setComponentClassPath( componentClassPath );
            }
            else if ( "bootstrap-class-name".equals( e.getLocalName() ) )
            {
                component.setBootstrapClassName( getText( e ) );
            }
            else if ( "bootstrap-class-path".equals( e.getLocalName() ) )
            {
                ClassPath bootstrapClassPath = new ClassPath();
                List l = new ArrayList();
                for ( Element e2 = W3CUtil.getFirstChildElement( e ); e2 != null; e2 =
                    W3CUtil.getNextSiblingElement( e2 ) )
                {
                    if ( "path-element".equals( e2.getLocalName() ) )
                    {
                        l.add( getText( e2 ) );
                    }
                }
                bootstrapClassPath.setPathList( l );
                component.setBootstrapClassPath( bootstrapClassPath );
            }
            else if ( "shared-library".equals( e.getLocalName() ) )
            {
                SharedLibraryList sl = new SharedLibraryList();
                sl.setName( getText( e ) );
                sl.setVersion( getAttribute( e, "version" ) );
                sls.add( sl );
            }
            else
            {
                if ( ext == null )
                {
                    ext = child.getOwnerDocument().createDocumentFragment();
                }
                ext.appendChild( e );
            }
        }
        component.setSharedLibraries( (SharedLibraryList[]) sls.toArray( new SharedLibraryList[sls.size()] ) );
        if ( ext != null )
        {
            InstallationDescriptorExtension descriptorExtension = new InstallationDescriptorExtension();
            descriptorExtension.setDescriptorExtension( ext );
            component.setDescriptorExtension( descriptorExtension );
        }
        return component;
    }

    private static String getAttribute( Element e, String name )
    {
        if ( e.hasAttribute( name ) )
        {
            return e.getAttribute( name );
        }
        return null;
    }

    private static QName readAttributeQName( Element e, String name )
    {
        String attr = getAttribute( e, name );
        if ( attr != null )
        {
            return W3CUtil.createQName( e, attr );
        }
        return null;
    }

    private static String getText( Element e )
    {
        return W3CUtil.getElementText( e ).trim();
    }

    private static Identification readIdentification( Element e )
    {
        Identification ident = new Identification();
        for ( Element e2 = W3CUtil.getFirstChildElement( e ); e2 != null; e2 = W3CUtil.getNextSiblingElement( e2 ) )
        {
            if ( "name".equals( e2.getLocalName() ) )
            {
                ident.setName( W3CUtil.getElementText( e2 ) );
            }
            else if ( "description".equals( e2.getLocalName() ) )
            {
                ident.setDescription( W3CUtil.getElementText( e2 ) );
            }
        }
        return ident;
    }

    public static void checkDescriptor( Descriptor descriptor )
    {
        List violations = new ArrayList();

        if ( descriptor.getVersion() != 1.0D )
        {
            violations.add( "JBI descriptor version should be set to '1.0' but is " + descriptor.getVersion() );
        }

        if ( descriptor.getComponent() != null )
        {
            checkComponent( violations, descriptor.getComponent() );
        }
        else if ( descriptor.getServiceAssembly() != null )
        {
            checkServiceAssembly( violations, descriptor.getServiceAssembly() );
        }
        else if ( descriptor.getServices() != null )
        {
            checkServiceUnit( violations, descriptor.getServices() );
        }
        else if ( descriptor.getSharedLibrary() != null )
        {
            checkSharedLibrary( violations, descriptor.getSharedLibrary() );
        }
        else
        {
            violations.add( "The jbi descriptor does not contain any informations" );
        }

        if ( violations.size() > 0 )
        {
            throw new RuntimeException( "The JBI descriptor is not valid, please correct these violations "
                + violations.toString() );
        }
    }

    private static void checkComponent( List<String> violations, Component component )
    {
        if ( component.getIdentification() == null )
        {
            violations.add( "The component has not identification" );
        }
        else if ( isBlank( component.getIdentification().getName() ) )
        {
            violations.add( "The component name is not set" );
        }

        if ( component.getBootstrapClassName() == null )
        {
            violations.add( "The component has not defined a boot-strap class name" );
        }
        if ( ( component.getBootstrapClassPath() == null )
            || ( component.getBootstrapClassPath().getPathElements() == null ) )
        {
            violations.add( "The component has not defined any boot-strap class path elements" );
        }
    }

    private static void checkServiceAssembly( List<String> violations, ServiceAssembly serviceAssembly )
    {
        if ( serviceAssembly.getIdentification() == null )
        {
            violations.add( "The service assembly has not identification" );
        }
        else if ( isBlank( serviceAssembly.getIdentification().getName() ) )
        {
            violations.add( "The service assembly name is not set" );
        }
    }

    private static void checkServiceUnit( List<String> violations, Services services )
    {
    }

    private static void checkSharedLibrary( List<String> violations, SharedLibrary sharedLibrary )
    {
        if ( sharedLibrary.getIdentification() == null )
        {
            violations.add( "The shared library has not identification" );
        }
        else if ( isBlank( sharedLibrary.getIdentification().getName() ) )
        {
            violations.add( "The shared library name is not set" );
        }
    }

    public static String getDescriptorAsText( File descriptorFile )
    {
        if ( descriptorFile.isDirectory() )
        {
            descriptorFile = new File( descriptorFile, "META-INF/jbi.xml" );
        }
        if ( descriptorFile.isFile() )
        {
            try
            {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                InputStream is = new FileInputStream( descriptorFile );
                FileUtil.copyInputStream( is, os );
                return os.toString();
            }
            catch ( Exception e )
            {
                LOG.debug( "Error reading jbi descritor: " + descriptorFile, e );
            }
        }
        return null;
    }

    private static boolean isBlank( String str )
    {
        if ( str == null )
        {
            return true;
        }
        int strLen = str.length();
        if ( strLen == 0 )
        {
            return true;
        }
        for ( int i = 0; i < strLen; ++i )
        {
            if ( !( Character.isWhitespace( str.charAt( i ) ) ) )
            {
                return false;
            }
        }
        return true;
    }
}