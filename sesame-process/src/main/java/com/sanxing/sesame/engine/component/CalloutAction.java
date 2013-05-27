package com.sanxing.sesame.engine.component;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;
import org.jdom.xpath.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.sanxing.sesame.constants.ExchangeConst;
import com.sanxing.sesame.engine.ExecutionEnv;
import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.ActionException;
import com.sanxing.sesame.engine.action.ActionUtil;
import com.sanxing.sesame.engine.action.Constant;
import com.sanxing.sesame.engine.action.callout.CalloutException;
import com.sanxing.sesame.engine.action.callout.Reverse;
import com.sanxing.sesame.engine.action.callout.Reverter;
import com.sanxing.sesame.engine.action.flow.exceptions.Catcher;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.Variable;
import com.sanxing.sesame.engine.exceptions.MockException;
import com.sanxing.sesame.engine.xslt.TransformerManager;
import com.sanxing.sesame.exception.FaultException;
import com.sanxing.sesame.exceptions.AppException;
import com.sanxing.sesame.exceptions.SystemException;
import com.sanxing.sesame.logging.Log;
import com.sanxing.sesame.logging.LogFactory;
import com.sanxing.sesame.logging.PerfRecord;
import com.sanxing.sesame.util.JdomUtil;

import static com.sanxing.sesame.engine.ExecutionEnv.*;

public class CalloutAction
    extends AbstractAction
{
    private static final String SEND_TIME = null;

    private static Logger LOG = LoggerFactory.getLogger( CalloutAction.class );

    private static Pattern ILLEGAL_REGEX = Pattern.compile( "[<>&'\"\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]" );

    private static Pattern SPLIT_REGEX = Pattern.compile( "," );

    private Element actionEl;

    private QName serviceName;

    private QName interfaceName;

    private QName operationName;

    private String endpointName;

    private String useVariable = ExchangeConst.REQUEST;

    private String toVar = ExchangeConst.RESPONSE;

    private String mode = "direct";

    private Element xsltEl;

    private String nodePath;

    private List<Element> catches;

    private long timeout = 0L;

    private int redirect = 0;

    private String responseText;

    private String status;

    private String faultType;

    private List<Namespace> namespaces;

    @Override
    public void doinit( Element actionEl )
    {
        this.actionEl = actionEl;
        Element addressEl = actionEl.getChild( "address", actionEl.getNamespace() );
        String strServiceName = addressEl.getChildTextTrim( "service-name", actionEl.getNamespace() );
        serviceName =
            ( ( ( strServiceName == null ) || ( strServiceName.length() == 0 ) ) ? null
                : QName.valueOf( strServiceName ) );
        String strInterfaceName = addressEl.getChildTextTrim( "interface-name", actionEl.getNamespace() );
        interfaceName = ( ( strInterfaceName == null ) ? null : QName.valueOf( strInterfaceName ) );
        String strOperationName = addressEl.getChildTextTrim( "operation-name", actionEl.getNamespace() );
        operationName = ( ( strOperationName == null ) ? null : QName.valueOf( strOperationName ) );
        endpointName = addressEl.getChildTextTrim( "endpoint-name", actionEl.getNamespace() );
        mode = actionEl.getAttributeValue( "mode", "direct" );

        useVariable = actionEl.getAttributeValue( "use-var" );
        toVar = actionEl.getAttributeValue( Constant.ATTR_TO_VAR_NAME );

        Element timeoutEl = actionEl.getChild( "onTimeout", actionEl.getNamespace() );
        if ( timeoutEl != null )
        {
            timeout = Long.parseLong( timeoutEl.getAttributeValue( "timeout", "30" ) );

            timeoutEl.setName( "onException" );
            timeoutEl.setAttribute( "exception-key", "call.504" );
            timeoutEl.setAttribute( Constant.ATTR_INDEX, "0" );
        }

        catches = actionEl.getChildren( "onException" );

        xsltEl = actionEl.getChild( "xslt", actionEl.getNamespace() );

        String value = actionEl.getAttributeValue( "is-emulator", "0" );
        if ( value != null )
        {
            redirect = Integer.parseInt( value );
            switch ( redirect )
            {
                case 0:
                    break;
                case 1:
                    responseText = actionEl.getAttributeValue( "emulator-text" );
                    break;
                case 2:
                    responseText = actionEl.getAttributeValue( "emulator-fault-text" );
                    status = actionEl.getAttributeValue( "fault-code" );
                    faultType = actionEl.getAttributeValue( "fault-type" );
            }

        }

        nodePath = getPath( actionEl );
        namespaces = actionEl.getDocument().getRootElement().getAdditionalNamespaces();
    }

    @Override
    public void dowork( DataContext dataCtx )
    {
        String group = getGroup();

        if ( catches.size() > 0 )
        {
            try
            {
                docall( dataCtx );

                DataContext cloneContext = (DataContext) dataCtx.clone();

                Reverter reverter = dataCtx.getExecutionContext().getReverter();
                Reverse reverse = new Reverse();
                reverse.setGroup( group );
                reverse.setSnapshot( cloneContext );
                reverter.pushReverse( reverse );
                int i = 0;
                for ( int len = catches.size(); i < len; ++i )
                {
                    Element catchEl = catches.get( i );
                    String strKeys = catchEl.getAttributeValue( "exception-key", "" );
                    String[] exceptionKeys = ( strKeys.length() > 0 ) ? SPLIT_REGEX.split( strKeys ) : new String[0];
                    int index = Integer.parseInt( catchEl.getAttributeValue( Constant.ATTR_INDEX ) );
                    if ( index < reverse.getIndex() )
                    {
                        reverse.setIndex( index );
                    }
                    boolean instantly = catchEl.getAttributeValue( "instant", "true" ).equals( "true" );
                    reverse.put( exceptionKeys, catchEl.getChildren(), instantly );
                }
            }
            catch ( CalloutException e )
            {
                boolean catched = false;
                int i = 0;
                for ( int len = catches.size(); i < len; ++i )
                {
                    Element catchEl = catches.get( i );
                    String strKeys = catchEl.getAttributeValue( "exception-key", "" );
                    String[] exceptionKeys = ( strKeys.length() > 0 ) ? SPLIT_REGEX.split( strKeys ) : new String[0];

                    catched = revert( dataCtx, group, e, catched, catchEl, exceptionKeys );
                    if ( catched )
                    {
                        break;
                    }
                }

                if ( !( catched ) )
                {
                    LOG.debug( "Uncaught exception", e );
                    throw e;
                }
            }
            catch ( Exception e )
            {
                throw new CalloutException( "500", e.getMessage(), e );
            }
        }
        else
        {
            docall( dataCtx );
        }
    }

    private boolean revert( DataContext dataCtx, String group, CalloutException e, boolean catched, Element catchEl,
                            String[] exceptionKeys )
    {
        if ( Catcher.isCatchable( e.getKey(), exceptionKeys ) )
        {
            catched = true;
            boolean instantly = catchEl.getAttributeValue( "instant", "true" ).equals( "true" );
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( "Catched exception:", e );
            }

            setErrorVar( dataCtx, e );

            ActionException exception = null;
            Reverter reverter = dataCtx.getExecutionContext().getReverter();
            try
            {
                if ( instantly )
                {
                    ActionUtil.bachInvoke( dataCtx, catchEl.getChildren().iterator() );
                }
                else
                {
                    reverter.writeLog( dataCtx, catchEl.getChildren().iterator() );
                }
            }
            catch ( ActionException ex )
            {
                exception = ex;
                reverter.writeLog( dataCtx, catchEl.getChildren().iterator() );
            }

            reverter.execute( group, e );

            if ( exception != null )
            {
                throw exception;
            }
        }

        return catched;
    }

    private void setErrorVar( DataContext dataCtx, CalloutException e )
    {
        Variable statusVar = new Variable( e.getKey(), 7 );
        dataCtx.addVariable( ExchangeConst.FAULT_CODE, statusVar );
        Variable descVar = new Variable( e.getMessage(), 7 );
        dataCtx.addVariable( ExchangeConst.FAULT_TEXT, descVar );

        dataCtx.getExecutionContext().put( PROCESS_FAULTCODE, e.getKey() );
        dataCtx.getExecutionContext().put( PROCESS_FAULTSTRING, e.getMessage() );
    }

    private void docall( DataContext ctx )
    {
        Long serial = (Long) ctx.getExecutionContext().get( SERIAL_NUMBER );
        long timeMillis = System.currentTimeMillis();
        MessageExchange me;
        try
        {
            if ( redirect > 0 )
            {
                if ( ( toVar != null ) && ( responseText != null ) && ( responseText.length() > 0 ) )
                {
                    StreamSource source = new StreamSource( new StringReader( responseText ) );
                    Document responseDoc = JdomUtil.source2JDOMDocument( source );
                    Element responseEl = responseDoc.getRootElement();
                    Variable responseVar = new Variable( responseEl, 0 );
                    ctx.addVariable( toVar, responseVar );
                }
                if ( redirect == 2 )
                {
                    throw new MockException( status, "Fake callout exception" );
                }
                return;
            }
            ProcessEngine engine = (ProcessEngine) ctx.getExecutionContext().get( ExchangeConst.ENGINE );

            if ( xsltEl != null )
            {
                transform( ctx, useVariable );
            }

            me = engine.getExchangeFactory().createInOptionalOutExchange();

            me.setProperty( ExchangeConst.PLATFORM_SERIAL, serial );

            me.setProperty( ExchangeConst.TX_ACTION, MDC.get( "ACTION" ) );
            me.setProperty( ExchangeConst.CLIENT_TYPE, MDC.get( "CLIENT_TYPE" ) );
            me.setProperty( ExchangeConst.CLIENT_SERIAL, MDC.get( "CLIENT_SERIAL" ) );
            me.setProperty( ExchangeConst.CLIENT_ID, MDC.get( "CLIENT_ID" ) );

            me.setProperty( ExchangeConst.CONSUMER, engine.getContext().getComponentName() );
            me.setProperty( ExchangeConst.TIMEOUT, Long.valueOf( timeout ) );

            me.setProperty( ExchangeConst.DISPATCHER, ExchangeConst.STRAIGHT );
            me.setService( serviceName );
            me.setInterfaceName( interfaceName );
            me.setOperation( operationName );
            if ( ( serviceName != null ) && ( endpointName != null ) )
            {
                ServiceEndpoint endpoint = engine.getContext().getEndpoint( serviceName, endpointName );
                me.setEndpoint( endpoint );
            }
            NormalizedMessage normalizedIn = me.createMessage();
            Variable var = ctx.getVariable( useVariable );
            Element ele = (Element) var.get();
            Source source = JdomUtil.JDOMElement2DOMSource( ele );
            normalizedIn.setContent( source );
            me.setMessage( normalizedIn, ExchangeConst.IN );

            timeMillis = System.currentTimeMillis();

            if ( mode.equals( "async" ) )
            {
                me.setProperty( ExchangeConst.THREAD_SWITCH, Boolean.valueOf( true ) );
                me.setProperty( SEND_TIME, Long.valueOf( timeMillis ) );
                engine.send( me );
                return;
            }
            me.setProperty( ExchangeConst.THREAD_SWITCH, Boolean.valueOf( mode.equals( "wait" ) ) );
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( "Before SendSync (mode: " + mode + ", timeout: " + ( timeout * 1000L ) + ")" );
            }
            boolean ret = engine.sendSync( me, timeout * 1000L );
            if ( !( ret ) )
            {
                throw new TimeoutException( "Send exchange timeout" );
            }

            if ( me.getStatus() == ExchangeStatus.ERROR )
            {
                if ( me.getFault() != null )
                {
                    if ( toVar != null )
                    {
                        Document document = JdomUtil.source2JDOMDocument( me.getFault().getContent() );
                        Element responseEl = document.getRootElement();
                        Variable varResponse = new Variable( responseEl, 0 );
                        ctx.addVariable( toVar, varResponse );
                    }
                    throw FaultException.newInstance( me );
                }
                if ( ( toVar != null ) && ( me.getError() != null ) )
                {
                    Exception error = me.getError();
                    Element faultEl = new Element( ExchangeConst.FAULT );
                    faultEl.addContent( new Element( ExchangeConst.FAULT_CODE ).setText( error.getClass().getSimpleName() ) );
                    String faultstring = ( error.getMessage() != null ) ? error.getMessage() : "";
                    faultstring = ILLEGAL_REGEX.matcher( faultstring ).replaceAll( "" );
                    faultEl.addContent( new Element( ExchangeConst.FAULT_TEXT ).setText( faultstring ) );
                    String faultactor = String.valueOf( me.getProperty( ExchangeConst.PROVIDER ) );
                    faultEl.addContent( new Element( ExchangeConst.FAULT_ACTOR ).setText( faultactor ) );
                    Variable varResponse = new Variable( faultEl, 0 );
                    ctx.addVariable( toVar, varResponse );
                }
            }

            if (this.toVar != null) {
              NormalizedMessage response = me.getMessage(ExchangeConst.OUT);
              Document docResponse = JdomUtil.source2JDOMDocument(response.getContent());
              Element eleResponse = docResponse.getRootElement();
              Variable varResponse = new Variable(eleResponse, 0);
              ctx.addVariable(this.toVar, varResponse);
            }
       }
        catch ( FaultException e )
        {
            String key = "500";
            String message = "未取到故障详细信息";
            LOG.debug( "Fault Exception", e );
            Fault fault = e.getFault();
            Document faultDoc = JdomUtil.source2JDOMDocument( fault.getContent() );
            XPath xpath = (XPath) fault.getProperty( ExchangeConst.STATUS_XPATH );
            if ( xpath != null )
            {
                try
                {
                    key = xpath.valueOf( faultDoc );
                }
                catch ( JDOMException ex )
                {
                    message = "获取故障代码失败: " + ex.getMessage();
                }
            }
            XPath textPath = (XPath) fault.getProperty( ExchangeConst.STATUS_TEXT_XPATH );
            if ( textPath != null )
            {
                try
                {
                    message = textPath.valueOf( faultDoc );
                }
                catch ( JDOMException ex )
                {
                }
            }
            throw new CalloutException( key, message, e );
        }
        catch ( MessagingException e )
        {
            throw new CalloutException("503", e.getMessage(), e);
        }
        catch ( TimeoutException e )
        {
            throw new CalloutException("504", e.getMessage(), e);
        }
        catch ( Exception e )
        {
            String key = getExceptionKey( e );
            String message = getExceptionMsg( e );
            if ( ( e instanceof MockException ) )
            {
                key = ( (MockException) e ).getErrKey();
            }
            else if ( ( e instanceof SystemException ) )
            {
                key = ( (SystemException) e ).getGlobalErrCode();
            }
            else if ( ( e instanceof AppException ) )
            {
                key = ( (AppException) e ).getGlobalErrCode();
            }

            LOG.debug( "Callout failure, The exception key is: [" + key + "]" );

            throw new CalloutException( key, message, e );
        }
        finally
        {
            Log sensor = LogFactory.getLog( "sesame.system.sensor.callout" );
            if ( sensor.isInfoEnabled() )
            {
                PerfRecord perf = new PerfRecord();
                perf.setSerial( serial.longValue() );
                perf.setElapsedTime( System.currentTimeMillis() - timeMillis );
                perf.setServiceName( String.valueOf( serviceName ) );
                perf.setOperationName( operationName.getLocalPart() );
                sensor.info( "--------------------------------------------------------------------------------", perf );
            }
        }
    }

    private String getExceptionKey( Exception exception )
    {
        String message = exception.getMessage();
        if ( message == null )
        {
            return "500";
        }

        int p = message.indexOf( 124, 0 );
        return ( ( p > -1 ) ? message.substring( 0, p ) : "500" );
    }

    private String getExceptionMsg( Exception exception )
    {
        String message = exception.getMessage();
        if ( message == null )
        {
            return null;
        }

        int p = message.indexOf( 124, 0 );
        return ( ( p > -1 ) ? message.substring( p + 1 ) : message );
    }

    private String getGroup()
    {
        Element element = actionEl.getParentElement();
        for ( ; element != null; element = element.getParentElement() )
        {
            if ( element.getName().equals( "group" ) )
            {
                return element.getAttributeValue( "id", "" );
            }
        }

        return "";
    }

    private String getPath( Element config )
    {
        int index = 0;
        String path = "";
        for ( Element el = config; el != null; el = el.getParentElement() )
        {
            index = ( el.getParentElement() != null ) ? el.getParentElement().indexOf( el ) : 0;
            path = "/" + el.getName() + "[" + index + "]" + path;
        }
        return path;
    }

    public boolean hasXSLT()
    {
        return ( xsltEl != null );
    }

    public void transform( DataContext ctx, String toVar )
    {
        ClassLoader cl = (ClassLoader) ctx.getExecutionContext().get( CLASSLOADER );
        ClassLoader savedCl = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader( cl );
            Transformer transformer = TransformerManager.getTransformer( xsltEl, namespaces );
            Map env = ExecutionEnv.export();
            for ( String name : (Set<String>) env.keySet() )
            {
                Object value = ctx.getExecutionContext().get( (String) env.get( name ) );
                transformer.setParameter( name, ( value != null ) ? value : "" );
            }

            Document document = new Document();
            Element contextEl = new Element( ExchangeConst.CONTEXT );
            document.setRootElement( contextEl );
            Set<Map.Entry<String, Variable>> variables = ctx.getVariables().entrySet();
            for ( Map.Entry var : variables )
            {
                String varName = (String) var.getKey();
                Variable variable = (Variable) var.getValue();
                if ( variable.getVarType() == 0 )
                {
                    Element elem = (Element) variable.get();
                    Document src = elem.getDocument();
                    if ( src == null )
                    {
                        src = new Document( elem );
                    }
                    Element varEl = (Element) elem.clone();
                    varEl.setName( varName );
                    varEl.setNamespace( Namespace.NO_NAMESPACE );
                    JdomUtil.allAdditionNamespace( varEl, Namespace.NO_NAMESPACE );
                    contextEl.addContent( varEl );
                }
            }
            JDOMSource source = new JDOMSource( document );
            JDOMResult result = new JDOMResult();
            transformer.transform( source, result );
            Document resultDoc = result.getDocument();
            Element root = resultDoc.getRootElement();
            Variable resultVar = new Variable( root, 0 );
            ctx.addVariable( toVar, resultVar );
        }
        catch ( TransformerException e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( savedCl );
        }
    }
}