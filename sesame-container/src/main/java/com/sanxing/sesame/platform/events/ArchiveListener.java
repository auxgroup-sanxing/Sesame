package com.sanxing.sesame.platform.events;

import com.sanxing.sesame.container.JBIContainer;
import com.sanxing.sesame.jmx.mbean.admin.ClusterEvent;
import com.sanxing.sesame.jmx.mbean.managed.FileClientMBean;
import com.sanxing.sesame.mbean.ArchiveEntry;
import com.sanxing.sesame.mbean.ArchiveManager;
import com.sanxing.sesame.core.BaseServer;
import com.sanxing.sesame.core.Env;
import com.sanxing.sesame.core.Platform;
import com.sanxing.sesame.core.api.MBeanHelper;
import com.sanxing.sesame.core.listener.ClusterListener;
import java.io.File;
import javax.jbi.management.DeploymentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArchiveListener implements ClusterListener {
	private Logger LOG = LoggerFactory.getLogger(ArchiveListener.class);
	JBIContainer container;

	public ArchiveListener(JBIContainer _container) {
		this.container = _container;
	}

	public void setServer(BaseServer server) {
	}

	public void listen(ClusterEvent event) {
		if ((!(event instanceof ArchiveEvent)) || (Platform.getEnv().isAdmin()))
			return;
		try {
			ArchiveEvent ae = (ArchiveEvent) event;
			this.LOG.info("receving ae" + ae);
			if (!(ae.getEventSource().equals(this.container.getServerName()))) {
				ArchiveEvent achivaEvent = ae;
				String location = achivaEvent.getEntry().getLocation();

				FileClientMBean client = (FileClientMBean) MBeanHelper
						.getManagedMBean(FileClientMBean.class,
								MBeanHelper.getPlatformMBeanName("file-client"));

				String fileName = client.fetchFile(location);
				File file = new File(fileName);
				ArchiveEntry entry = new ArchiveEntry();
				entry.setLocation(file.getName());

				entry.setLastModified(achivaEvent.getEntry().getLastModified());

				this.container.getArchiveManager().updateArchive(
						file.getAbsolutePath(), entry, true);
				this.LOG.info("update entery [" + file.getAbsolutePath() + "]");
			}
		} catch (DeploymentException e) {
			e.printStackTrace();
		}
	}
}