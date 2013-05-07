package com.sanxing.sesame.core.jms;

import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.naming.NamingException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.broker.jmx.ManagementContext;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.core.BaseServer;
import com.sanxing.sesame.core.Platform;
import com.sanxing.sesame.core.api.MBeanHelper;
import com.sanxing.sesame.jmx.mbean.admin.ClusterAdminMBean;
import com.sanxing.sesame.jmx.mbean.admin.ServerInfo;
import com.sanxing.sesame.util.SystemProperties;

public class ActiveMQJMSProvider
    implements JMSProvider
{
    private static Logger LOG = LoggerFactory.getLogger( ActiveMQJMSProvider.class );

    private BrokerService broker;

    @Override
    public void prepare( BaseServer server, JMSServiceInfo serviceInfo )
    {
        try
        {
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( "prepare jms broker" );
            }

            broker = new BrokerService();
            ManagementContext mCtx = new ManagementContext();
            mCtx.setJmxDomainName( Platform.getEnv().getDomain() + ".ActiveMQ" );
            mCtx.setMBeanServer( server.getMBeanServer() );
            mCtx.setUseMBeanServer( true );
            mCtx.start();
            broker.setUseJmx( true );
            broker.setManagementContext( mCtx );
            broker.setBrokerName( server.getName() );
            broker.setPersistent( true );
            broker.setDataDirectoryFile( new File( server.getServerDir(), "data/activemq-data" ) );

            broker.getSystemUsage().getMemoryUsage().setLimit( 67108864L );

            PolicyEntry policy = new PolicyEntry();

            policy.setMemoryLimit( 4194304L );

            policy.setProducerFlowControl( false );
            PolicyMap pMap = new PolicyMap();

            pMap.setDefaultEntry( policy );
            broker.setDestinationPolicy( pMap );

            String port = serviceInfo.getAppInfo().getChildText( "activemq-broker-port" );

            TransportConnector tc = broker.addConnector( "tcp://" + server.getConfig().getIP() + ":" + port );
            tc.setName( server.getName() );

            if ( Platform.getEnv().isClustered() )
            {
                ClusterAdminMBean clusterAdmin = MBeanHelper.getAdminMBean( ClusterAdminMBean.class, "cluster-manager" );
                List<ServerInfo> servers = clusterAdmin.getAllServer();

                boolean multicast =
                    SystemProperties.get( "com.sanxing.sesame.activemq.cluster.mode", "multicast" ).equals( "multicast" );
                String strClusterURI;
                if ( !( multicast ) )
                {
                    strClusterURI = "static:(";
                    for ( ServerInfo s : servers )
                    {
                        if ( !( s.getServerName().equals( server.getName() ) ) )
                        {
                            strClusterURI =
                                strClusterURI + "tcp://" + s.getIP() + ":"
                                    + s.getJmsServiceInfo().getAppInfo().getChildText( "activemq-broker-port" ) + ",";
                        }
                    }
                    strClusterURI = strClusterURI.substring( 0, strClusterURI.length() - 1 ) + ")";
                }
                else
                {
                    strClusterURI = "multicast://default";
                }
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( "clusterURI is [" + strClusterURI + "]" );
                }
                URI clusterURI = URI.create( strClusterURI );
                broker.addNetworkConnector( clusterURI );
            }

            broker.start();
            ActiveMQConnectionFactory qcf = new ActiveMQConnectionFactory( "vm://localhost" );
            qcf.setCopyMessageOnSend( false );
            qcf.setUseAsyncSend( true );

            server.getNamingContext().bind( server.getName() + "-QC", qcf );
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( "context is ...." + server.getNamingContext() );
                LOG.debug( " BINDING NAME IS " + server.getName() + "-QC" );
            }
            prepareOtherResource( server, serviceInfo, qcf );
        }
        catch ( Exception e )
        {
            if ( LOG.isTraceEnabled() )
            {
                LOG.trace( e.getMessage(), e );
            }
            else
            {
                LOG.error( "", e );
            }
        }
    }

    private void prepareOtherResource( BaseServer server, JMSServiceInfo serviceInfo, ActiveMQConnectionFactory qcf )
        throws JMSException, NamingException
    {
        Iterator iter = serviceInfo.getEntries().iterator();

        createQueue( server, qcf, server.getName() + "-NMR-QUEUE" );
        while ( iter.hasNext() )
        {
            JMSResourceEntry entry = (JMSResourceEntry) iter.next();
            String jndiName = entry.getJndiName();
            if ( entry.getType().equals( "queue" ) )
            {
                createQueue( server, qcf, jndiName );
            }
        }
    }

    private void createQueue( BaseServer server, ActiveMQConnectionFactory qcf, String jndiName )
        throws JMSException, NamingException
    {
        QueueConnection qc = qcf.createQueueConnection();

        QueueSession qs = qc.createQueueSession( false, 1 );

        Queue q = qs.createQueue( jndiName );
        server.getNamingContext().bind( jndiName, q );
    }

    @Override
    public void release()
    {
        try
        {
            broker.stop();
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage(), e );
        }
    }
}