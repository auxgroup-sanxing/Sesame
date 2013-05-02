package com.sanxing.sesame.jmx.mbean;

import com.sanxing.sesame.jmx.mbean.admin.ClusterEvent;
import com.sanxing.sesame.jmx.mbean.admin.ServerInfo;
import com.sanxing.sesame.core.BaseServer;
import com.sanxing.sesame.core.Environment;
import com.sanxing.sesame.core.Platform;
import com.jezhumble.javasysmon.CpuTimes;
import com.jezhumble.javasysmon.JavaSysMon;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerManager implements ServerManagerMBean {
	private static Logger LOG = LoggerFactory.getLogger(ServerManager.class);

	private AtomicBoolean started = new AtomicBoolean(true);

	private BaseServer server = null;

	public ServerManager(BaseServer server) {
		this.server = server;
	}

	public void listen(ClusterEvent event) {
		this.server.listenClusterEvent(event);
	}

	public void setServer(BaseServer server) {
		this.server = server;
	}

	public void start() {
		if (!(this.started.compareAndSet(false, true)))
			return;
		try {
			this.server.start();
		} catch (Exception e) {
			LOG.error("start server " + this.server.getName() + " err", e);
			this.started.set(false);
			throw new RuntimeException("start server error", e);
		}
	}

	public void stop() {
		if (!(this.started.compareAndSet(true, false)))
			return;
		try {
			this.server.shutdown();
		} catch (Exception e) {
			this.started.set(true);
			LOG.error("stop server err " + this.server.getName(), e);
		}
	}

	public String getName() {
		return this.server.getName();
	}

	public String getHostAddress() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			return e.getMessage();
		}
	}

	public int getJmxPort() {
		return Platform.getEnv().getAdminPort();
	}

	public String getState() {
		String state = "unknown";
		switch (this.server.getConfig().getServerState()) {
		case 1:
			state = "starting";
			break;
		case 2:
			state = "running";
			break;
		case 3:
			state = "stopping";
			break;
		case 0:
			state = "shutdown";
		}

		return state;
	}

	public void receiveEvent(ClusterEvent event) {
		this.server.listenClusterEvent(event);
	}

	public String getDescription() {
		return "server manager";
	}

	public String getSystemCpu() {
		String result = "0";
		JavaSysMon monitor = new JavaSysMon();
		CpuTimes preCpuTimes = monitor.cpuTimes();
		try {
			Thread.sleep(500L);
		} catch (InterruptedException localInterruptedException) {
		}
		float times = monitor.cpuTimes().getCpuUsage(preCpuTimes);
		result = String.format("%.2f", new Object[] { Float.valueOf(times) });
		return result;
	}

	public String getJVMMemory() {
		String result = "0";
		Runtime runtime = Runtime.getRuntime();
		long total = runtime.totalMemory();
		long free = runtime.freeMemory();
		float percent = 1.0F - ((float) free / (float) total);
		result = String.format("%.2f", new Object[] { Float.valueOf(percent) });
		return result;
	}
}