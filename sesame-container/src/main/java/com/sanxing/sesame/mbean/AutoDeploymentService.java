package com.sanxing.sesame.mbean;

import com.sanxing.sesame.container.EnvironmentContext;
import com.sanxing.sesame.container.JBIContainer;
import com.sanxing.sesame.management.AttributeInfoHelper;
import com.sanxing.sesame.util.FileUtil;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.jbi.JBIException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AutoDeploymentService extends BaseSystemService implements
		AutoDeploymentServiceMBean {
	private static final Log LOG = LogFactory
			.getLog(AutoDeploymentService.class);
	private EnvironmentContext environmentContext;
	private boolean monitorInstallationDirectory;
	private boolean monitorDeploymentDirectory;
	private int monitorInterval;
	private AtomicBoolean started;
	private Timer statsTimer;
	private TimerTask timerTask;
	private ArchiveManager archiveManager;

	public AutoDeploymentService() {
		this.monitorInstallationDirectory = true;
		this.monitorDeploymentDirectory = true;
		this.monitorInterval = 10;

		this.started = new AtomicBoolean(false);
	}

	public String getDescription() {
		return "automatically installs and deploys JBI Archives";
	}

	public boolean isMonitorInstallationDirectory() {
		return this.monitorInstallationDirectory;
	}

	public void setMonitorInstallationDirectory(
			boolean monitorInstallationDirectory) {
		this.monitorInstallationDirectory = monitorInstallationDirectory;
	}

	public boolean isMonitorDeploymentDirectory() {
		return this.monitorDeploymentDirectory;
	}

	public void setMonitorDeploymentDirectory(boolean monitorDeploymentDirectory) {
		this.monitorDeploymentDirectory = monitorDeploymentDirectory;
	}

	public int getMonitorInterval() {
		return this.monitorInterval;
	}

	public void setMonitorInterval(int monitorInterval) {
		this.monitorInterval = monitorInterval;
	}

	public void start() throws JBIException {
		super.start();
		if (this.started.compareAndSet(false, true))
			scheduleDirectoryTimer();
	}

	public void stop() throws JBIException {
		if (this.started.compareAndSet(true, false)) {
			super.stop();
			if (this.timerTask != null)
				this.timerTask.cancel();
		}
	}

	public void init(JBIContainer container) throws JBIException {
		super.init(container);
		this.environmentContext = container.getEnvironmentContext();
		this.archiveManager = container.getArchiveManager();

		if (this.environmentContext.getTmpDir() != null)
			FileUtil.deleteFile(this.environmentContext.getTmpDir());
	}

	protected Class<AutoDeploymentServiceMBean> getServiceMBean() {
		return AutoDeploymentServiceMBean.class;
	}

	public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
		AttributeInfoHelper helper = new AttributeInfoHelper();
		helper.addAttribute(getObjectToManage(),
				"monitorInstallationDirectory",
				"Periodically monitor the Installation directory");
		helper.addAttribute(getObjectToManage(), "monitorInterval",
				"Interval (secs) before monitoring");
		return AttributeInfoHelper.join(super.getAttributeInfos(),
				helper.getAttributeInfos());
	}

	private void scheduleDirectoryTimer() {
		if ((isMonitorInstallationDirectory())
				|| (isMonitorDeploymentDirectory())) {
			if (this.statsTimer == null) {
				this.statsTimer = new Timer(true);
			}
			if (this.timerTask != null) {
				this.timerTask.cancel();
			}
			this.timerTask = new TimerTask() {
				public void run() {
					if (!(AutoDeploymentService.this.isStarted()))
						return;
					try {
						if (AutoDeploymentService.this
								.isMonitorInstallationDirectory()) {
							AutoDeploymentService.this.archiveManager
									.scanInstallDir();
						}
						if (AutoDeploymentService.this
								.isMonitorDeploymentDirectory())
							AutoDeploymentService.this.archiveManager
									.scanDeployDir();
					} catch (Throwable t) {
						AutoDeploymentService.LOG.error(t.getMessage(), t);
					}
				}
			};
			long interval = this.monitorInterval * 1000;
			this.statsTimer.scheduleAtFixedRate(this.timerTask, 0L, interval);
		}
	}
}