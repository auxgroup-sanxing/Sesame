package com.sanxing.sesame.wssecurity.commons;

import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.utils.resolver.ResourceResolverException;
import org.apache.xml.security.utils.resolver.implementations.ResolverFragment;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class WsuIdResolver
    extends ResolverFragment
{
    private Document doc;

    public WsuIdResolver( Document doc )
    {
        this.doc = doc;
    }

    public XMLSignatureInput engineResolve( Attr uri, String baseURI )
        throws ResourceResolverException
    {
        String uriNodeValue = uri.getNodeValue();
        Node selectedElem = null;
        if ( uriNodeValue.equals( "" ) )
        {
            selectedElem = this.doc;
        }
        else
        {
            String id = uriNodeValue.substring( 1 );
            selectedElem = Utils.lookupElementById( this.doc.getDocumentElement(), id );
            if ( selectedElem == null )
            {
                Object[] exArgs = { id };
                throw new ResourceResolverException( "signature.Verification.MissingID", exArgs, uri, baseURI );
            }
        }

        XMLSignatureInput result = new XMLSignatureInput( selectedElem );
        result.setExcludeComments( true );
        result.setMIMEType( "text/xml" );
        result.setSourceURI( uriNodeValue );
        return result;
    }
}
