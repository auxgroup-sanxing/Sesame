package com.sanxing.sesame.engine.component;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import javax.jbi.messaging.ExchangeStatus;
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
import org.jdom.Namespace;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.sanxing.sesame.engine.ExecutionEnv;
import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.ActionException;
import com.sanxing.sesame.engine.action.ActionUtil;
import com.sanxing.sesame.engine.action.callout.CalloutException;
import com.sanxing.sesame.engine.action.callout.Reverse;
import com.sanxing.sesame.engine.action.callout.Reverter;
import com.sanxing.sesame.engine.action.flow.exceptions.Catcher;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.Variable;
import com.sanxing.sesame.engine.exceptions.MockException;
import com.sanxing.sesame.engine.xslt.TransformerManager;
import com.sanxing.sesame.exception.FaultException;
import com.sanxing.sesame.logging.Log;
import com.sanxing.sesame.logging.LogFactory;
import com.sanxing.sesame.logging.PerfRecord;
import com.sanxing.sesame.util.JdomUtil;

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

    private String useVariable = "request";

    private String toVar = "response";

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
        Element addressEl = actionEl.getChild( "address" );
        String strServiceName = addressEl.getChildTextTrim( "service-name" );
        serviceName =
            ( ( ( strServiceName == null ) || ( strServiceName.length() == 0 ) ) ? null
                : QName.valueOf( strServiceName ) );
        String strInterfaceName = addressEl.getChildTextTrim( "interface-name" );
        interfaceName = ( ( strInterfaceName == null ) ? null : QName.valueOf( strInterfaceName ) );
        String strOperationName = addressEl.getChildTextTrim( "operation-name" );
        operationName = ( ( strOperationName == null ) ? null : QName.valueOf( strOperationName ) );
        endpointName = addressEl.getChildTextTrim( "endpoint-name" );
        mode = actionEl.getAttributeValue( "mode", "direct" );

        useVariable = actionEl.getAttributeValue( "use-var" );
        toVar = actionEl.getAttributeValue( "to-var" );

        Element timeoutEl = actionEl.getChild( "onTimeout" );
        if ( timeoutEl != null )
        {
            timeout = Long.parseLong( timeoutEl.getAttributeValue( "timeout", "30" ) );

            timeoutEl.setName( "onException" );
            timeoutEl.setAttribute( "exception-key", "call.504" );
            timeoutEl.setAttribute( "index", "0" );
        }

        catches = actionEl.getChildren( "onException" );

        xsltEl = actionEl.getChild( "xslt" );

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
                    int index = Integer.parseInt( catchEl.getAttributeValue( "index" ) );
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
        dataCtx.addVariable( "faultcode", statusVar );
        Variable descVar = new Variable( e.getMessage(), 7 );
        dataCtx.addVariable( "faultstring", descVar );

        dataCtx.getExecutionContext().put( "process.faultcode", e.getKey() );
        dataCtx.getExecutionContext().put( "process.faultstring", e.getMessage() );
    }

    private void docall( DataContext ctx )
    {
        Long serial = (Long) ctx.getExecutionContext().get( "process.serial" );
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
            ProcessEngine engine = (ProcessEngine) ctx.getExecutionContext().get( "ENGINE" );

            if ( xsltEl != null )
            {
                transform( ctx, useVariable );
            }

            me = engine.getExchangeFactory().createInOptionalOutExchange();

            me.setProperty( "sesame.exchange.platform.serial", serial );

            me.setProperty( "sesame.exchange.tx.action", MDC.get( "ACTION" ) );
            me.setProperty( "sesame.exchange.client.type", MDC.get( "CLIENT_TYPE" ) );
            me.setProperty( "sesame.exchange.client.serial", MDC.get( "CLIENT_SERIAL" ) );
            me.setProperty( "sesame.exchange.client.id", MDC.get( "CLIENT_ID" ) );

            me.setProperty( "sesame.exchange.consumer", engine.getContext().getComponentName() );
            me.setProperty( "sesame.exchange.timeout", Long.valueOf( timeout ) );

            me.setProperty( "com.sanxing.sesame.dispatch", "straight" );
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
            me.setMessage( normalizedIn, "in" );

            timeMillis = System.currentTimeMillis();

            if ( mode.equals( "async" ) )
            {
                me.setProperty( "sesame.exchange.thread.switch", Boolean.valueOf( true ) );
                me.setProperty( SEND_TIME, Long.valueOf( timeMillis ) );
                engine.send( me );
                return;
            }
            me.setProperty( "sesame.exchange.thread.switch", Boolean.valueOf( mode.equals( "wait" ) ) );
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
                    Element faultEl = new Element( "fault" );
                    faultEl.addContent( new Element( "faultcode" ).setText( error.getClass().getSimpleName() ) );
                    String faultstring = ( error.getMessage() != null ) ? error.getMessage() : "";
                    faultstring = ILLEGAL_REGEX.matcher( faultstring ).replaceAll( "" );
                    faultEl.addContent( new Element( "faultstring" ).setText( faultstring ) );
                    String faultactor = String.valueOf( me.getProperty( "sesame.exchange.provider" ) );
                    faultEl.addContent( new Element( "faultactor" ).setText( faultactor ) );
                    Variable varResponse = new Variable( faultEl, 0 );
                    ctx.addVariable( toVar, varResponse );
                }
            }
        }
        catch ( FaultException e )
        {
            String message = "未取到故障详细信息";
            LOG.debug( "Fault Exception", e );
            throw new CalloutException( "500", message, e );
        }
        catch ( MessagingException e )
        {
        }
        catch ( TimeoutException e )
        {
        }
        catch ( Exception e )
        {
            String key = getExceptionKey( e );
            String message = getExceptionMsg( e );
            if ( e instanceof MockException )
            {
                ;
            }
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
        ClassLoader cl = (ClassLoader) ctx.getExecutionContext().get( "process.classloader" );
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
            Element contextEl = new Element( "context" );
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