package com.sanxing.adp.runtime;

import java.lang.reflect.Field;
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

    public abstract Element process( Document paramDocument, OperationInfo paramOperationInfo, Object paramObject )
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
                            body.setNamespace( Namespace.getNamespace( elementName.getNamespaceURI() ) );
                            for ( Element ele : (List<Element>) body.getChildren() )
                            {
                                Field field = paramClass.getDeclaredField( ele.getName() );
                                XmlElement xmlEle = field.getAnnotation( XmlElement.class );
                                ele.setNamespace( Namespace.getNamespace( xmlEle.namespace() ) );
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

    void allAddtionNamespace( Element part, String NamespaceURI )
    {
        List childrens = part.getChildren();
        for ( int i = 0; i < childrens.size(); ++i )
        {
            Element addition = (Element) childrens.get( i );
            addition.setNamespace( Namespace.getNamespace( "", NamespaceURI ) );
            if ( addition.getChildren().size() > 0 )
            {
                allAddtionNamespace( addition, NamespaceURI );
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
}