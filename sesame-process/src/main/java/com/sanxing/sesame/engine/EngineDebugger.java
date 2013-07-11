package com.sanxing.sesame.engine;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jaxen.Function;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.sanxing.sesame.classloader.JarFileClassLoader;
import com.sanxing.sesame.component.params.AppParameters;
import com.sanxing.sesame.component.params.Parameter;
import com.sanxing.sesame.constants.ExchangeConst;
import com.sanxing.sesame.core.naming.JNDIUtil;
import com.sanxing.sesame.engine.action.Constant;
import com.sanxing.sesame.engine.component.ProcessEngine;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.ExecutionContext;
import com.sanxing.sesame.engine.context.Variable;
import com.sanxing.sesame.engine.xpath.XPathUtil;
import com.sanxing.sesame.engine.xslt.TransformerManager;

import static com.sanxing.sesame.engine.ExecutionEnv.*;

public class EngineDebugger
{
    private static Logger LOG = LoggerFactory.getLogger( EngineDebugger.class );

    private final Map<String, ClassLoader> loaders = new HashMap();

    private final ExecutionContext executionCtx;

    private Thread thread;

    public EngineDebugger( ExecutionContext executionCtx )
    {
        this.executionCtx = executionCtx;

        Object serial = executionCtx.get( SERIAL_NUMBER );

        this.executionCtx.getDataContext().addVariable( ExchangeConst.SERIAL,
            new Variable( ( serial != null ) ? serial : Integer.valueOf( 0 ), 8 ) );

        this.executionCtx.put( NAMING_CONTEXT, JNDIUtil.getInitialContext() );

        this.executionCtx.put( ExchangeConst.ENGINE, ProcessEngine.jbiInstance );

        appendBizParameters( executionCtx );
    }

    private void appendBizParameters( ExecutionContext executionCtx )
    {
        String suName = (String) this.executionCtx.get( ExchangeConst.SERVICE_NAME );
        String operationName = (String) this.executionCtx.get( ExchangeConst.OPERATION_NAME );

        LOG.debug( "append biz param : suName [" + suName + "]" );

        LOG.debug( "append biz param : operationName [" + operationName + "]" );

        List<String> paramNames = new LinkedList();
        paramNames.addAll( AppParameters.getInstance().getAppParamKeys() );
        paramNames.addAll( AppParameters.getInstance().getSuParamKeys( suName ) );
        paramNames.addAll( AppParameters.getInstance().getOperationParamKeys( suName, operationName ) );
        for ( String paramName : paramNames )
        {
            Parameter param = AppParameters.getInstance().getParamter( suName, operationName, paramName );
            LOG.debug( "APPEND BIZ PARAMEER [" + param + "]" );
            if ( param.getType().equals( Parameter.PARAMTYPE.PARAM_TYPE_BOOLEAN ) )
            {
                executionCtx.getDataContext().addVariable( param.getName(), new Variable( param.getTypedValue(), 6 ) );
            }
            else if ( ( param.getType().equals( Parameter.PARAMTYPE.PARAM_TYPE_INT ) )
                || ( param.getType().equals( Parameter.PARAMTYPE.PARAM_TYPE_DOUBLE ) ) )
            {
                executionCtx.getDataContext().addVariable( param.getName(), new Variable( param.getTypedValue(), 8 ) );
            }
            else if ( param.getType().equals( Parameter.PARAMTYPE.PARAM_TYPE_STRING ) )
            {
                executionCtx.getDataContext().addVariable( param.getName(), new Variable( param.getTypedValue(), 7 ) );
            }
        }
    }

    public void start( String componentRoot, final String flowName )
    {
        ClassLoader classloader = loaders.get( componentRoot );
        if ( classloader == null )
        {
            JarFileClassLoader loader =
                new JarFileClassLoader( new URL[0], Thread.currentThread().getContextClassLoader(), false,
                    new String[0], new String[] { "java.", "javax." } );
            File folder = new File( componentRoot );
            loader.addClassesDir( folder );
            loader.addJarDir( new File( folder, "lib" ) );
            loaders.put( componentRoot, loader );

            registerTransformExtension( componentRoot, loader );
            registerCustomXPathFunction( componentRoot, loader );

            classloader = loader;
        }

        executionCtx.put( CLASSLOADER, classloader );
        executionCtx.openDebugging();
        Runnable worker = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    TransformerManager.clearCache( flowName );
                    MDC.put( "ACTION", "debugger" );
                    if ( executionCtx.get( SERIAL_NUMBER ) != null )
                    {
                        MDC.put( "SERIAL", String.valueOf( executionCtx.get( SERIAL_NUMBER ) ) );
                    }
                    MDC.put( "CLIENT_TYPE", "debugger" );
                    MDC.put( "CLIENT_ID", "debugger" );
                    if ( executionCtx.getUuid() != null )
                    {
                        MDC.put( "CLIENT_SERIAL", executionCtx.getUuid() );
                    }
                    executionCtx.put( ACTION, flowName );
                    Engine.getInstance().execute( executionCtx, flowName );
                }
                catch ( Throwable t )
                {
                    EngineDebugger.LOG.error( t.getMessage(), t );
                    try
                    {
                        executionCtx.put( "exception", t );
                        executionCtx.setCurrentAction( "exception" );
                    }
                    catch ( InterruptedException localInterruptedException )
                    {
                    }
                }
                finally
                {
                    if ( ( executionCtx != null ) && ( !( executionCtx.isDehydrated() ) ) )
                    {
                        executionCtx.closeDebugging();
                    }
                }
            }
        };
        thread = new Thread( worker );
        thread.setContextClassLoader( classloader );
        thread.setDaemon( true );
        thread.start();
    }

    public void resume()
    {
        synchronized ( executionCtx )
        {
            executionCtx.closeDebugging();
            executionCtx.notify();
        }
    }

    public void terminate()
    {
        synchronized ( executionCtx )
        {
            executionCtx.closeDebugging();
            executionCtx.terminate();
            executionCtx.notify();
        }
    }

    public void nextStep()
        throws InterruptedException
    {
        synchronized ( executionCtx )
        {
            executionCtx.notify();
        }
    }

    public DataContext getCurrentContext()
    {
        return executionCtx.getDataContext();
    }

    private void registerTransformExtension( String componentRoot, ClassLoader classloader )
    {
        File extensionFile = new File( componentRoot, "transform.ext" );
        if ( extensionFile.exists() )
        {
            SAXBuilder builder = new SAXBuilder();
            try
            {
                Document doc = builder.build( extensionFile );
                List<Element> list = doc.getRootElement().getChildren( "class" );
                for ( Element classEl : list )
                {
                    String prefix = classEl.getAttributeValue( "prefix" );
                    String className = classEl.getAttributeValue( "class-name" );
                    classloader.loadClass( className );
                    LOG.debug( "register class [" + prefix + ":" + className + "]" );
                    TransformerManager.registerExtension( prefix, className );
                }
            }
            catch ( JDOMException e )
            {
                throw new RuntimeException( "please check transform.ext", e );
            }
            catch ( IOException e )
            {
                throw new RuntimeException( e.getMessage(), e );
            }
            catch ( ClassNotFoundException e )
            {
                throw new RuntimeException( e.getMessage(), e );
            }
        }
    }

    private void registerCustomXPathFunction( String componentRoot, ClassLoader classloader )
    {
        File functionFile = new File( componentRoot, "xpath.ext" );
        if ( !( functionFile.exists() ) )
        {
            return;
        }
        try
        {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build( functionFile );
            List functionEles = doc.getRootElement().getChildren();
            for ( int i = 0; i < functionEles.size(); ++i )
            {
                String name = null;
                String namespaceUri = null;
                try
                {
                    Element eleFunc = (Element) functionEles.get( i );
                    String prefix = eleFunc.getAttributeValue( "prefix" );
                    name = eleFunc.getAttributeValue( Constant.ATTR_NAME );
                    namespaceUri = "http://www.sanxing.net.cn/sesame/" + prefix;
                    String className = eleFunc.getAttributeValue( "class-name" );
                    String desc = eleFunc.getAttributeValue( "description" );
                    LOG.debug( "add function [" + prefix + ":" + namespaceUri + "]" + name );
                    Class functionClazz = classloader.loadClass( className );
                    Function function = (Function) functionClazz.newInstance();
                    XPathUtil.registerFunction( namespaceUri, prefix, name, function );
                }
                catch ( Exception e )
                {
                    LOG.error( "Register function [" + namespaceUri + ":" + name + "] err", e );
                }
            }
        }
        catch ( JDOMException e )
        {
            throw new RuntimeException( "please check xpath.ext", e );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }
    }
}