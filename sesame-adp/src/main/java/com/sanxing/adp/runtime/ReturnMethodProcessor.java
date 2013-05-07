package com.sanxing.adp.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.transform.JDOMResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.adp.ADPException;
import com.sanxing.adp.parser.OperationInfo;
import com.sanxing.adp.util.XJUtil;
import com.sanxing.sesame.exceptions.AppException;
import com.sanxing.sesame.util.ReflectUtil;

public class ReturnMethodProcessor
    extends BaseMethodProcessor
{
    private static Logger LOG = LoggerFactory.getLogger( ReturnMethodProcessor.class );

    @Override
    public Element process( Document request, OperationInfo operation, Object tx )
        throws AppException
    {
        Element body = request.getRootElement();

        Element newBody = new Element( "body", body.getNamespace() );
        newBody.addContent( (Element) body.clone() );
        Object[] paramObjets = new Object[operation.getMethodParamCount()];
        fufillINParams( operation, newBody, paramObjets );

        LOG.debug( "After fufillINParams" );
        Element root = invokeSingleResultTx( operation, tx, paramObjets );
        return root;
    }

    private Element invokeSingleResultTx( OperationInfo oper, Object tx, Object[] paramObjets )
        throws AppException
    {
        Element root = null;

        LOG.debug( "Before getMethodByName" );
        Method method = ReflectUtil.getMethodByName( oper.getOperationName(), tx.getClass() );

        if ( method == null )
        {
            throw new ADPException( "00001", oper.getOperationName() );
        }

        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( "prepare to call adp function [" + method.getName() + "]" );
        }
        Object result;
        try
        {
            result = method.invoke( tx, paramObjets );
        }
        catch ( IllegalArgumentException e )
        {
            throw new ADPException( "00002", oper.getOperationName(), e );
        }
        catch ( IllegalAccessException e )
        {
            throw new ADPException( "00003", oper.getOperationName(), e );
        }
        catch ( InvocationTargetException e )
        {
            if ( e.getCause() instanceof AppException )
            {
                throw ( (AppException) e.getCause() );
            }
            if ( e.getCause() instanceof ADPException )
            {
                throw ( (ADPException) e.getCause() );
            }

            throw new ADPException( "99999", e.getCause() );
        }
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( "call adp function successed , prepare to fufill result" );
        }

        if ( XJUtil.isPrimitive( oper.getResult().getJavaType() ) )
        {
            String elementNS = oper.getResult().getElementName().getNamespaceURI();
            String elementName = oper.getResult().getElementName().getLocalPart();
            Element ele = new Element( elementName, elementNS );
            ele.setText( result.toString() );
            root = ele;
        }
        else
        {
            Marshaller m = JAXBHelper.getMarshallerByClazz( result.getClass() );
            JDOMResult jdomResult = new JDOMResult();
            try
            {
                m.marshal( result, jdomResult );
            }
            catch ( JAXBException e )
            {
                throw new ADPException( "00004", e );
            }
            Element partResult = jdomResult.getDocument().getRootElement();
            partResult.detach();
            root = partResult;
        }

        return root;
    }
}