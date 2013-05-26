package com.sanxing.sesame.wssecurity.commons;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Utils
{
    public static final Logger logger = LoggerFactory.getLogger( Utils.class );

    public static SOAPHeaderElement getHeaderByNameAndActor( SOAPEnvelope env, QName desiredQName, Set roleSet,
                                                             boolean isEndPoint )
        throws SOAPException
    {
        logger.debug( "Searching for a header named " + desiredQName.getLocalPart() + " in the "
            + desiredQName.getNamespaceURI() + " namespace " );
        SOAPHeader header = env.getHeader();
        if ( header == null )
        {
            return null;
        }
        Iterator i = header.examineAllHeaderElements();
        while ( i.hasNext() )
        {
            SOAPHeaderElement headerElement = (SOAPHeaderElement) i.next();

            String actorURI = headerElement.getActor();
            logger.debug( "Next header's actor: " + actorURI );
            if ( actorURI == null ? !isEndPoint : ( ( roleSet != null ) && ( roleSet.contains( actorURI ) ) )
                || ( "http://schemas.xmlsoap.org/soap/actor/next".equals( actorURI ) ) )
            {
                Name headerName = headerElement.getElementName();
                logger.debug( "matching header's local name: " + headerName.getLocalName() );

                if ( headerName.getLocalName().equals( desiredQName.getLocalPart() ) )
                {
                    logger.debug( "matching header's namespace: " + headerName.getURI() );
                    String desiredNamespaceURI = desiredQName.getNamespaceURI();
                    if ( desiredNamespaceURI == null ? headerName.getURI() == null
                        : desiredNamespaceURI.equals( headerName.getURI() ) )
                    {
                        logger.debug( "found a match" );
                        return headerElement;
                    }
                }
            }
        }
        logger.debug( "no match found" );
        return null;
    }

    public static SOAPElement locateChildSOAPElement( SOAPElement parent, String childNsUri, String childLocalName )
    {
        Iterator childIter = parent.getChildElements();
        while ( childIter.hasNext() )
        {
            Object child = childIter.next();
            if ( ( child instanceof SOAPElement ) )
            {
                SOAPElement childElement = (SOAPElement) child;
                Name childName = childElement.getElementName();
                if ( ( childNsUri.equals( childName.getURI() ) )
                    && ( childLocalName.equals( childName.getLocalName() ) ) )
                    return childElement;
            }
        }
        return null;
    }

    public static Element locateChildDOMElement( Element parent, String childNsUri, String childLocalName )
    {
        NodeList children = parent.getChildNodes();
        int numChildren = children.getLength();
        for ( int i = 0; i < numChildren; i++ )
        {
            Node child = children.item( i );
            if ( ( child instanceof Element ) )
            {
                Element childElement = (Element) child;
                if ( ( childNsUri.equals( child.getNamespaceURI() ) )
                    && ( childLocalName.equals( child.getLocalName() ) ) )
                    return childElement;
            }
        }
        return null;
    }

    public static byte[] charsToUTF8Bytes( char[] chars )
    {
        try
        {
            ByteArrayOutputStream bytesOutStream = new ByteArrayOutputStream( chars.length * 4 );
            OutputStreamWriter charToByte = new OutputStreamWriter( bytesOutStream, "UTF-8" );
            charToByte.write( chars, 0, chars.length );
            charToByte.close();
            return bytesOutStream.toByteArray();
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    public static void loadKeyStoreData( KeyStore keyStore, String keyStoreResourcePath, String keyStorePassword,
                                         Object caller )
    {
        try
        {
            FileInputStream keyStoreInputStream = new FileInputStream( keyStoreResourcePath );

            keyStore.load( keyStoreInputStream,
                ( keyStoreInputStream != null ) || ( keyStorePassword != null ) ? keyStorePassword.toCharArray() : null );
            keyStoreInputStream.close();
        }
        catch ( NoSuchAlgorithmException e )
        {
            logger.error( e.getMessage(), e );
            throw new RuntimeException( e );
        }
        catch ( CertificateException e )
        {
            logger.error( e.getMessage(), e );
            throw new RuntimeException( e );
        }
        catch ( IOException e )
        {
            logger.error( e.getMessage(), e );
            throw new RuntimeException( "Error reading keystore: ", e );
        }
    }

    public static Element lookupElementById( Element context, String id )
    {
        if ( id == null )
        {
            throw new IllegalArgumentException( "id cannot be null" );
        }

        NodeList children = context.getChildNodes();
        int numChildren = children.getLength();
        for ( int i = 0; i < numChildren; i++ )
        {
            Node child = children.item( i );
            if ( ( child instanceof Element ) )
            {
                Element childElement = (Element) child;
                if ( id.equals( childElement.getAttributeNS(
                    "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id" ) ) )
                    return childElement;
                if ( id.equals( childElement.getAttributeNS( null, "Id" ) ) )
                {
                    return childElement;
                }
                Element result = lookupElementById( childElement, id );
                if ( result != null )
                    return result;
            }
        }
        return null;
    }

    public static String getElementId( Element element )
    {
        String id =
            element.getAttributeNS(
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id" );
        if ( id == null )
        {
            id = element.getAttributeNS( null, "Id" );
        }
        return id;
    }

    public static void setElementId( Element element, String id )
    {
        element.setAttributeNS( "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
            "wsu:Id", id );
    }

    public static boolean isDescendantOf( Node node, Node other )
    {
        Node parent = node.getParentNode();
        if ( parent == null )
            return false;

        if ( other.equals( parent ) )
            return true;
        return isDescendantOf( parent, other );
    }

    public static String getTextFromDOMNode( Node node )
    {
        int type = node.getNodeType();
        if ( ( type == 8 ) || ( type == 7 ) )
        {
            return "";
        }

        StringBuffer text = new StringBuffer();

        String value = node.getNodeValue();
        if ( value != null )
            text.append( value );
        if ( node.hasChildNodes() )
        {
            NodeList children = node.getChildNodes();
            for ( int i = 0; i < children.getLength(); i++ )
            {
                Node child = children.item( i );
                text.append( getTextFromDOMNode( child ) );
            }
        }

        return text.toString();
    }

    public static PKIXCertPathValidatorResult validateCertificateChain( Certificate cert, KeyStore keyStore )
        throws CertificateException, NoSuchAlgorithmException, CertPathValidatorException, KeyStoreException,
        InvalidAlgorithmParameterException
    {
        List certList = new LinkedList();
        certList.add( cert );
        CertPath certPath = CertificateFactory.getInstance( "X.509" ).generateCertPath( certList );
        return validateCertificationChain( certPath, keyStore );
    }

    public static PKIXCertPathValidatorResult validateCertificationChain( CertPath certPath, KeyStore keyStore )
        throws NoSuchAlgorithmException, CertPathValidatorException, KeyStoreException,
        InvalidAlgorithmParameterException
    {
        CertPathValidator validator = CertPathValidator.getInstance( CertPathValidator.getDefaultType() );
        PKIXParameters validationParams = new PKIXParameters( keyStore );

        validationParams.setRevocationEnabled( false );
        return (PKIXCertPathValidatorResult) validator.validate( certPath, validationParams );
    }
}