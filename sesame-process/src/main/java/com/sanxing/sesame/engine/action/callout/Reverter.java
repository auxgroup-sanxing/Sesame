package com.sanxing.sesame.engine.action.callout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.jaxen.NamespaceContext;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.Action;
import com.sanxing.sesame.engine.action.ActionFactory;
import com.sanxing.sesame.engine.action.ActionUtil;
import com.sanxing.sesame.engine.action.flow.AbortException;
import com.sanxing.sesame.engine.action.var.VarNotFoundException;
import com.sanxing.sesame.engine.component.CalloutAction;
import com.sanxing.sesame.engine.component.ProcessEngine;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.ExecutionContext;
import com.sanxing.sesame.engine.context.Variable;
import com.sanxing.sesame.logging.Log;
import com.sanxing.sesame.logging.LogFactory;
import com.sanxing.sesame.util.JdomUtil;

public class Reverter
{
    private static Logger LOG = LoggerFactory.getLogger( Reverter.class );

    private final List<Reverse> reverses = new ArrayList();

    private final Log reverseLog = LogFactory.getLog( "sesame.reverse" );

    public void pushReverse( Reverse reverse )
    {
        reverses.add( reverse );
    }

    public void execute( String group, CalloutException e )
    {
        Collections.sort( reverses );

        String exceptionKey = e.getKey();
        String exceptionMsg = e.getMessage();
        for ( Reverse reverse : reverses )
        {
            LOG.debug( reverse.toString() );
            if ( !( group.equals( reverse.getGroup() ) ) )
            {
                continue;
            }

            Reverse.CatchClause clause = reverse.getClause( exceptionKey );
            if ( clause != null )
            {
                if ( clause.isInstantly() )
                {
                    try
                    {
                        DataContext dataCtx = reverse.getSnapshot();

                        Variable statusVar = new Variable( exceptionKey, 7 );
                        dataCtx.addVariable( "faultcode", statusVar );
                        Variable descVar = new Variable( exceptionMsg, 7 );
                        dataCtx.addVariable( "faultstring", descVar );

                        dataCtx.getExecutionContext().put( "process.faultcode", e.getKey() );
                        dataCtx.getExecutionContext().put( "process.faultstring", e.getMessage() );

                        ActionUtil.bachInvoke( dataCtx, clause.getActions().iterator() );
                    }
                    catch ( AbortException localAbortException )
                    {
                    }
                    catch ( Throwable t )
                    {
                        LOG.error( "Reverse failure, write log for reverse task", t );

                        writeLog( reverse.getSnapshot(), clause.getActions().iterator() );
                    }
                }
                else
                {
                    LOG.info( "Write log for reverse task" );
                    writeLog( reverse.getSnapshot(), clause.getActions().iterator() );
                }
            }
        }
    }

    public void writeLog( DataContext dataCtx, Iterator<?> actions )
    {
        LOG.debug( "Write Reverse log: " + dataCtx );

        ExecutionContext executeCtx = dataCtx.getExecutionContext();

        ProcessEngine engine = (ProcessEngine) executeCtx.get( "ENGINE" );
        try
        {
            NamespaceContext namespaceCtx = (NamespaceContext) dataCtx.getExecutionContext().get( "process.namespaces" );

            while ( actions.hasNext() )
            {
                Element actionEl = (Element) actions.next();
                Action action = ActionFactory.getInstance( actionEl );
                LOG.debug( "Action is: " + action );
                if ( actionEl.getName().equals( "callout" ) )
                {
                    String useVariable = actionEl.getAttributeValue( "use-var" );
                    Variable var;
                    try
                    {
                        var = dataCtx.getVariable( useVariable );
                    }
                    catch ( VarNotFoundException e )
                    {
                        CalloutAction callout = (CalloutAction) action;
                        if ( callout.hasXSLT() )
                        {
                            callout.transform( dataCtx, useVariable );
                        }
                        var = dataCtx.getVariable( useVariable );
                    }

                    Element addressEl = actionEl.getChild( "address" );
                    String strServiceName = addressEl.getChildTextTrim( "service-name", addressEl.getNamespace() );
                    QName serviceName =
                        ( ( strServiceName == null ) || ( strServiceName.length() == 0 ) ) ? null
                            : QName.valueOf( strServiceName );
                    String strInterfaceName = addressEl.getChildTextTrim( "interface-name", addressEl.getNamespace() );
                    QName interfaceName = ( strInterfaceName == null ) ? null : QName.valueOf( strInterfaceName );
                    String strOperationName = addressEl.getChildTextTrim( "operation-name", addressEl.getNamespace() );
                    QName operationName = ( strOperationName == null ) ? null : QName.valueOf( strOperationName );
                    String endpointName = addressEl.getChildTextTrim( "endpoint-name", addressEl.getNamespace() );

                    MessageExchange me = engine.getExchangeFactory().createInOptionalOutExchange();

                    Long serial = (Long) executeCtx.get( "process.serial" );
                    me.setProperty( "sesame.exchange.platform.serial", serial );
                    me.setProperty( "sesame.exchange.consumer", engine.getContext().getComponentName() );
                    me.setProperty( "com.sanxing.sesame.dispatch", "straight" );
                    me.setProperty( "original-service", serviceName );
                    me.setProperty( "original-interface", interfaceName );
                    me.setProperty( "original-operation", operationName );
                    me.setProperty( "original-endpoint", endpointName );

                    me.setService( new QName( "http://www.sanxing.com/sesame/revert", "RevertService" ) );
                    me.setOperation( new QName( "writeLog" ) );

                    NormalizedMessage normalizedIn = me.createMessage();
                    Element element = (Element) var.get();
                    Source source = JdomUtil.JDOMElement2DOMSource( element );
                    normalizedIn.setContent( source );
                    me.setMessage( normalizedIn, "in" );
                    long timeout = 30L;
                    boolean ret = engine.sendSync( me, timeout * 1000L );
                    if ( !( ret ) )
                    {
                        throw new TimeoutException( "Send exchange timeout" );
                    }
                    if ( me.getError() != null )
                    {
                        throw me.getError();
                    }

                    reverseLog.debug( "Write reverse log success" );
                }
                else
                {
                    if ( action instanceof AbstractAction )
                    {
                        AbstractAction abstractAction = (AbstractAction) action;

                        abstractAction.setNamespaceContext( namespaceCtx );
                    }
                    action.work( dataCtx );
                }
            }
        }
        catch ( RuntimeException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            reverseLog.error( "Write Reverse log failure" );

            LOG.error( e.getMessage(), e );
        }
    }
}