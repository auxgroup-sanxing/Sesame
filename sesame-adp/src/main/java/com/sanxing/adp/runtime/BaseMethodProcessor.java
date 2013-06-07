package com.sanxing.adp.runtime;

import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.transform.JDOMSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.adp.ADPException;
import com.sanxing.adp.api.ResultHolder;
import com.sanxing.adp.parser.OperationInfo;
import com.sanxing.adp.parser.PartInfo;
import com.sanxing.adp.util.XJUtil;
import com.sanxing.sesame.exceptions.AppException;
import com.sanxing.sesame.util.JdomUtil;

public abstract class BaseMethodProcessor
{
    ADPServer server;

    private static Logger LOG = LoggerFactory.getLogger( BaseMethodProcessor.class );

    public abstract Document process( Document request, OperationInfo paramOperationInfo, Object paramObject )
        throws ADPException, AppException;

    public void setServer( ADPServer server )
    {
        this.server = server;
    }

    void fufillINParams( OperationInfo oper, Document request, Object[] paramObjets )
        throws ADPException
    {
        int i = 0;

        Element body = request.getRootElement();
        if ( body == null )
        {
            throw new ADPException( "00006", oper.getCapOperationName() );
        }
        
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( "prepared to fufill input parameters..............." );
            LOG.debug( "input document is ...........\n " + JdomUtil.print( new JDOMSource( request ) ) );
        }
        
        Element newBody = new Element("body", body.getNamespace());
        newBody.addContent((Element)body.clone());

        List<PartInfo> params = oper.getParams();
        for ( PartInfo parameterInfo : params )
        {
            try
            {
                QName elementName = parameterInfo.getElementName();
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( "parsing element [" + elementName + "]" );
                }
                String javaType = parameterInfo.getJavaType();
                Element part = null;
                if ( elementName != null )
                {
                    Namespace ns = Namespace.getNamespace( elementName.getPrefix(), elementName.getNamespaceURI() );
                    part = newBody.getChild( elementName.getLocalPart(), ns );
                    if ( part != null )
                    {
                        part.setNamespace( Namespace.getNamespace( "", elementName.getNamespaceURI() ) );
                    }
                    else
                        throw new ADPException( "00007", elementName.getLocalPart() );
                }
                else
                {
                    part = newBody.getChild( parameterInfo.getName() );
                }
                if ( XJUtil.isPrimitive( javaType ) )
                {
                    String param = part.getText();
                    Object paramObj =
                        XJUtil.xmlPrimitiv2Java( parameterInfo.getJavaType(), parameterInfo.getXsType(), param );
                    paramObjets[i] = paramObj;
                }
                else
                {
                    Unmarshaller unmarshaller =
                        JAXBHelper.getUnMarshallerByClazz( this.server.jarFileClassLoader.loadClass( javaType ) );

                    Object paramObject = unmarshaller.unmarshal( new JDOMSource( part ) );
                    paramObjets[i] = paramObject;
                }

                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( "parsed element [" + elementName + "]" );
                }

                ++i;
            }
            catch ( JAXBException e )
            {
                throw new ADPException( "00005", e );
            }
            catch ( Exception e )
            {
                throw new ADPException( "99999", e );
            }
        }
    }

    void fufillOUTParams( OperationInfo oper, Document request, Object[] paramObjets )
    {
        int i = oper.getMethodParamCount() - oper.getParams().size() + 1;
        for ( PartInfo parameterInfo : oper.getResults() )
        {
            ResultHolder holder = new ResultHolder();
            paramObjets[i] = holder;
            ++i;
        }
    }

    void formatResponse( OperationInfo oper, Document response )
    {
        Element body = response.getRootElement();
        if ( body == null )
        {
            throw new ADPException( "00008", oper.getCapOperationName() );
        }

        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( "output document is ...........\n " + JdomUtil.print( new JDOMSource( response ) ) );
        }
        Iterator it = ( (Element) body.clone() ).getAdditionalNamespaces().iterator();
        while ( it.hasNext() )
        {
            body.removeNamespaceDeclaration( (Namespace) it.next() );
        }
        List<PartInfo> results = oper.getResults();
        for ( PartInfo resultInfo : results )
        {
            try
            {
                QName elementName = resultInfo.getElementName();
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( "parsing element [" + elementName + "]" );
                }
                String javaType = resultInfo.getJavaType();
                if ( !XJUtil.isPrimitive( javaType ) )
                {
                    if ( elementName != null )
                    {
                        if ( elementName.getLocalPart().equals( body.getName() ) )
                        {
                            body.setNamespace( Namespace.NO_NAMESPACE );
                            JdomUtil.allAdditionNamespace( body, Namespace.NO_NAMESPACE );
                        }
                        else
                        {
                            throw new ADPException( "00009", elementName.getLocalPart() );
                        }
                    }
                }
            }
            catch ( Exception e )
            {
                throw new ADPException( "99999", e );
            }
        }
    }
}