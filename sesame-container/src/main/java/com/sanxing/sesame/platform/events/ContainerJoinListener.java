package com.sanxing.sesame.platform.events;

import com.sanxing.sesame.container.JBIContainer;
import com.sanxing.sesame.jmx.mbean.admin.ClusterAdminMBean;
import com.sanxing.sesame.jmx.mbean.admin.ClusterEvent;
import com.sanxing.sesame.mbean.ArchiveManager;
import com.sanxing.sesame.core.BaseServer;
import com.sanxing.sesame.core.Env;
import com.sanxing.sesame.core.Platform;
import com.sanxing.sesame.core.api.MBeanHelper;
import com.sanxing.sesame.core.event.ServerJoinEvent;
import com.sanxing.sesame.core.listener.ClusterListener;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContainerJoinListener implements ClusterListener {
	private Logger LOG = LoggerFactory.getLogger(ContainerJoinListener.class);
	JBIContainer container;

	public void listen(ClusterEvent event) {
		if (event instanceof ServerJoinEvent) {
			ClusterAdminMBean clusterAdmin = (ClusterAdminMBean) MBeanHelper
					.getAdminMBean(ClusterAdminMBean.class, "cluster-manager");

			if (Platform.getEnv().isAdmin()) {
				Set<ArchiveEvent> installedFiles = this.container
						.getArchiveManager().getPublishedEvents();
				this.LOG.debug("Notify new joined server install events("
						+ installedFiles.size() + ")");
				for (ArchiveEvent archivaEvent : installedFiles) {
					clusterAdmin
							.fireEvent(archivaEvent, event.getEventSource());
				}
			} else {
				ContainerEndpointsEvent myEvent = new ContainerEndpointsEvent(
						this.container);

				clusterAdmin.fireEvent(myEvent, event.getEventSource());
			}
		}
	}

	public void setServer(BaseServer _server) {
	}

	public ContainerJoinListener(JBIContainer _container) {
		this.container = _container;
	}
}