package com.sanxing.adp.parser;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.adp.util.XJUtil;
import com.sanxing.sesame.util.GetterUtil;
import com.sanxing.sesame.util.SystemProperties;

public class PortTypeInfo
{
    private QName definationName;

    private QName name;

    private final List<OperationInfo> operations = new LinkedList();

    private static Logger LOG = LoggerFactory.getLogger( PortTypeInfo.class );

    private String className;

    Map<Class, Object> beanCache = new ConcurrentHashMap();

    public void addOperation( OperationInfo operationInfo )
    {
        operations.add( operationInfo );
    }

    public List<OperationInfo> getOperations()
    {
        return operations;
    }

    public QName getName()
    {
        return name;
    }

    public void setName( QName name )
    {
        this.name = name;
    }

    public QName getDefinationName()
    {
        return definationName;
    }

    public void setDefinationName( QName definationName )
    {
        this.definationName = definationName;
    }

    public OperationInfo getOperation( String operQName )
    {
        for ( OperationInfo info : operations )
        {
            if ( info.getOperationName().equals( operQName ) )
            {
                return info;
            }
        }
        throw new RuntimeException( "unkown operation :[" + operQName + "]" );
    }

    public String getClassName()
    {
        if ( className == null )
        {
            className = XJUtil.ns2ClassName( getName() ) + "Impl";
        }
        return className;
    }

    public Object getTx()
    {
        try
        {
            Class clazz = PortTypeInfo.class.getClassLoader().loadClass( getClassName() );

            Object tx = clazz.newInstance();
            return tx;
        }
        catch ( InstantiationException e )
        {
            LOG.error( "initialize class [" + getClassName() + "] err", e );
        }
        catch ( IllegalAccessException e )
        {
            LOG.error( "initialize class [" + getClassName() + "] err", e );
        }
        catch ( ClassNotFoundException e )
        {
            LOG.error( "initialize class [" + getClassName() + "] err", e );
        }
        return null;
    }

    public Object getTx( ClassLoader load )
    {
        try
        {
            boolean statefulADPBean =
                GetterUtil.getBoolean( SystemProperties.get( "com.sanxing.sesame.adp.stateful" ), false );
            if ( statefulADPBean )
            {
                Class clazz = load.loadClass( getClassName() );
                Object tx = clazz.newInstance();
                return tx;
            }
            Class clazz = load.loadClass( getClassName() );
            if ( !( beanCache.containsKey( clazz ) ) )
            {
                Object tx = clazz.newInstance();
                beanCache.put( clazz, tx );
            }
            return beanCache.get( clazz );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "initialize class [" + getClassName() + "] err,please check class path ", e );
        }
    }

    @Override
    public String toString()
    {
        return "PortTypeInfo{definationName=" + definationName + ", name=" + name + ", operations=" + operations + '}';
    }
}