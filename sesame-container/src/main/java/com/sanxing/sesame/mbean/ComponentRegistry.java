package com.sanxing.sesame.mbean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jbi.JBIException;
import javax.jbi.component.Component;

public class ComponentRegistry
{
    private final Map<ComponentNameSpace, ComponentMBeanImpl> idMap = new LinkedHashMap();

    private boolean runningStateInitialized;

    private final Registry registry;

    protected ComponentRegistry( Registry reg )
    {
        registry = reg;
    }

    public synchronized ComponentMBeanImpl registerComponent( ComponentNameSpace name, String description,
                                                              Component component, boolean binding, boolean service,
                                                              String[] sharedLibraries )
    {
        ComponentMBeanImpl result = null;
        if ( !( idMap.containsKey( name ) ) )
        {
            result =
                new ComponentMBeanImpl( registry.getContainer(), name, description, component, binding, service,
                    sharedLibraries );
            idMap.put( name, result );
        }
        return result;
    }

    public synchronized void start()
        throws JBIException
    {
        Iterator i;
        if ( !( setInitialRunningStateFromStart() ) )
        {
            for ( i = getComponents().iterator(); i.hasNext(); )
            {
                ComponentMBeanImpl lcc = (ComponentMBeanImpl) i.next();
                lcc.doStart();
            }
        }
    }

    public synchronized void stop()
        throws JBIException
    {
        for ( Object element : getReverseComponents() )
        {
            ComponentMBeanImpl lcc = (ComponentMBeanImpl) element;
            lcc.doStop();
        }
        runningStateInitialized = false;
    }

    public synchronized void shutDown()
        throws JBIException
    {
        for ( Object element : getReverseComponents() )
        {
            ComponentMBeanImpl lcc = (ComponentMBeanImpl) element;
            lcc.persistRunningState();
            lcc.doShutDown();
        }
    }

    private Collection<ComponentMBeanImpl> getReverseComponents()
    {
        synchronized ( idMap )
        {
            List l = new ArrayList( idMap.values() );
            Collections.reverse( l );
            return l;
        }
    }

    public synchronized void deregisterComponent( ComponentMBeanImpl component )
    {
        idMap.remove( component.getComponentNameSpace() );
    }

    public ComponentMBeanImpl getComponent( ComponentNameSpace id )
    {
        synchronized ( idMap )
        {
            return idMap.get( id );
        }
    }

    public Collection<ComponentMBeanImpl> getComponents()
    {
        synchronized ( idMap )
        {
            return new ArrayList( idMap.values() );
        }
    }

    private boolean setInitialRunningStateFromStart()
        throws JBIException
    {
        boolean result = !( runningStateInitialized );
        Iterator i;
        if ( !( runningStateInitialized ) )
        {
            runningStateInitialized = true;
            for ( i = getComponents().iterator(); i.hasNext(); )
            {
                ComponentMBeanImpl lcc = (ComponentMBeanImpl) i.next();
                if ( !( lcc.isPojo() ) )
                {
                    lcc.setInitialRunningState();
                }
                else
                {
                    lcc.doStart();
                }
            }
        }
        return result;
    }
}