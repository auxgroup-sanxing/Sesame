package com.sanxing.adp.runtime;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
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
                if ( XJUtil.isPrimitive( javaType ) )
                {
                    String param = body.getText();
                    Object paramObj =
                        XJUtil.xmlPrimitiv2Java( parameterInfo.getJavaType(), parameterInfo.getXsType(), param );
                    paramObjets[i] = paramObj;
                }
                else
                {
                    Class paramClass = server.jarFileClassLoader.loadClass( javaType );
                    if ( elementName != null )
                    {
                        if ( elementName.getLocalPart().equals( body.getName() ) )
                        {
                        	Namespace ns = Namespace.getNamespace( elementName.getPrefix(), elementName.getNamespaceURI() );
                            body.setNamespace( ns );
                            for ( Element ele : (List<Element>) body.getChildren() )
                            {
                                Field field = paramClass.getDeclaredField( ele.getName() );
                                XmlElement xmlEle = field.getAnnotation( XmlElement.class );
                                if ( "##default".equals( xmlEle.namespace() ) )
                                {
                                	ele.setNamespace( ns );
                                	allAddtionNamespace( ele, ns);
                                }
                                else
                                {
                                	ele.setNamespace( Namespace.getNamespace( xmlEle.namespace() ) );
                                    allAddtionNamespace( ele, Namespace.getNamespace( xmlEle.namespace() ));
                                }                              	
                            }
                        }
                        else
                        {
                            throw new ADPException( "00007", elementName.getLocalPart() );
                        }
                    }
                    
                    Unmarshaller unmarshaller = JAXBHelper.getUnMarshallerByClazz( paramClass );

                    Object paramObject = unmarshaller.unmarshal( new JDOMSource( body ) );
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

    void allAddtionNamespace( Element part, Namespace namespace )
    {
        List childrens = part.getChildren();
        for ( int i = 0; i < childrens.size(); ++i )
        {
            Element addition = (Element) childrens.get( i );
            addition.setNamespace( namespace );
            if ( addition.getChildren().size() > 0 )
            {
                allAddtionNamespace( addition, namespace );
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
                if ( ! XJUtil.isPrimitive( javaType ) )
                {
                    if ( elementName != null )
                    {
                        if ( elementName.getLocalPart().equals( body.getName() ) )
                        {
                            body.setNamespace( Namespace.NO_NAMESPACE );
                            allAddtionNamespace( body, Namespace.NO_NAMESPACE);
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