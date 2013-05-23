package com.sanxing.sesame.engine;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.jdom.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.engine.action.ActionUtil;
import com.sanxing.sesame.engine.action.beanshell.BSCManager;
import com.sanxing.sesame.engine.action.flow.AbortException;
import com.sanxing.sesame.engine.context.ExecutionContext;

import static com.sanxing.sesame.engine.ExecutionEnv.*;

public class Engine
{
    static final Logger LOG = LoggerFactory.getLogger( Engine.class );

    public static final String FLOW_DEFINATION = "FLOW_DEFINATION";

    private static Engine _instance;

    private final Map<String, FlowInfo> flowInfoCache = new HashMap();

    private Engine()
    {
        start();
    }

    public static synchronized Engine getInstance()
    {
        if ( _instance == null )
        {
            _instance = new Engine();
        }
        return _instance;
    }

    private void start()
    {
    }

    public void regsiterFunction( String packagePath )
    {
        BSCManager.regStaticFuncPath( packagePath );
    }

    public void registerFlow( String name, Element flowConfig )
    {
        FlowInfo flow = new FlowInfo();
        LOG.info( "prepare to register flow [" + name + "]" );
        flow.setName( name );
        flow.setFlowDefination( flowConfig );
        List nsList = flowConfig.getAdditionalNamespaces();
        for ( Iterator localIterator = nsList.iterator(); localIterator.hasNext(); )
        {
            Object nsObj = localIterator.next();
            Namespace ns = (Namespace) nsObj;
            flow.addNSMapping( ns.getURI(), ns.getPrefix() );
        }
        flowInfoCache.put( name, flow );
        BSCManager.reset4Flow( name );
        LOG.info( "flow [" + name + "] registed" );
    }

    public void execute( ExecutionContext executionCtx, String flowName )
    {
        prepareBeanShell( executionCtx, flowName );

        FlowInfo info = flowInfoCache.get( flowName );
        if ( info == null )
        {
            throw new RuntimeException( "Engine does not support this flow [" + flowName + "]" );
        }
        Element flow = info.getFlowDefination();
        try
        {
            executionCtx.put( NAMESPACE_CONTEXT, info.getNamespaceContext() );

            ActionUtil.bachInvoke( executionCtx.getDataContext(), flow.getChildren().iterator() );

            executionCtx.setCurrentAction( "finish" );
            LOG.debug( "Flow [" + flowName + "] is finished" );
        }
        catch ( AbortException e )
        {
            LOG.debug( "Flow [" + flowName + "] is aborted" );
            return;
        }
        catch ( InterruptedException e )
        {
            return;
        }
    }

    private void prepareBeanShell( ExecutionContext executionCtx, String flowName )
    {
        if ( executionCtx.isDebugging() )
        {
            executionCtx.put( BEANSHELL_CONTEXT, BSCManager.getInstanceForDebug( flowName ) );
        }
        else
        {
            executionCtx.put( BEANSHELL_CONTEXT, BSCManager.getInstance( flowName ) );
        }
    }
}