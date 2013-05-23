package com.sanxing.adp.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.xml.bind.Marshaller;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.transform.JDOMResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.adp.ADPException;
import com.sanxing.adp.api.ResultHolder;
import com.sanxing.adp.parser.OperationInfo;
import com.sanxing.adp.parser.PartInfo;
import com.sanxing.adp.util.XJUtil;
import com.sanxing.sesame.util.ReflectUtil;

public class VoidMethodProcessor
    extends BaseMethodProcessor
{
    private static Logger LOG = LoggerFactory.getLogger( VoidMethodProcessor.class );

    @Override
    public Document process( Document request, OperationInfo operation, Object tx )
    {
        Object[] paramObjets = new Object[operation.getMethodParamCount()];
        fufillINParams( operation, request, paramObjets );
        fufillOUTParams( operation, request, paramObjets );

        Document response = invokeMultiResultOperation( operation, tx, paramObjets );
        formatResponse( operation, response );
        return response;
    }

    private Document invokeMultiResultOperation( OperationInfo oper, Object tx, Object[] paramObjets )
        throws ADPException
    {
        Element root = new Element( "response" );
        Method method = ReflectUtil.getMethodByName( oper.getOperationName(), tx.getClass() );
        if ( method == null )
        {
            throw new ADPException( "7000", "no such method [" + oper.getOperationName() + "]" );
        }

        if ( method.getName().equals( oper.getOperationName() ) )
        {
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( "prepare to call adp function [" + method.getName() + "]" );
            }
            try
            {
                method.invoke( tx, paramObjets );
            }
            catch ( IllegalArgumentException e )
            {
                throw new ADPException( "7001", "illegal argument is passed to adp class", e );
            }
            catch ( IllegalAccessException e )
            {
                throw new ADPException( "7002", "illegal access to adp class, make sure method ["
                    + oper.getOperationName() + "] is public", e );
            }
            catch ( InvocationTargetException e )
            {
                if ( e.getCause() instanceof ADPException )
                {
                    throw ( (ADPException) e.getCause() );
                }

                throw new ADPException( "7003", "error in adp business class, error msg[" + e.getCause().getMessage()
                    + "]", e.getCause() );
            }

            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( "call adp function successed , prepare to fufill result" );
            }

            int i = oper.getMethodParamCount() - oper.getParams().size() + 1;
            for ( PartInfo parameterInfo : oper.getResults() )
            {
                ResultHolder holder = (ResultHolder) paramObjets[i];

                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( "prepared to fufill result [" + parameterInfo.getName() + "]" );
                }

                if ( XJUtil.isPrimitive( parameterInfo.getJavaType() ) )
                {
                    String elementNS = parameterInfo.getElementName().getNamespaceURI();
                    String elementName = parameterInfo.getElementName().getLocalPart();
                    Element ele = new Element( elementName, elementNS );
                    ele.setText( holder.getValue().toString() );
                    if ( LOG.isDebugEnabled() )
                    {
                        LOG.debug( "fufilled result [" + parameterInfo.getName() + "]" );
                    }
                }
                else
                {
                    JDOMResult result = new JDOMResult();
                    try
                    {
                        Marshaller m =
                            JAXBHelper.getMarshallerByClazz( server.jarFileClassLoader.loadClass( parameterInfo.getJavaType() ) );
                        m.marshal( holder.getValue(), result );
                    }
                    catch ( Exception e )
                    {
                        throw new ADPException( "7004", "marshall result to xml err", e );
                    }
                    Element partResult = result.getDocument().getRootElement();
                    partResult.detach();
                    root.addContent( partResult );

                    if ( LOG.isDebugEnabled() )
                    {
                        LOG.debug( "fufilled result [" + parameterInfo.getName() + "]" );
                    }
                }

                ++i;
            }

        }

        return new Document( root );
    }
}