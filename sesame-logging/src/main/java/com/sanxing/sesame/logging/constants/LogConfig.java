package com.sanxing.sesame.logging.constants;

public abstract interface LogConfig {
	public static final String STATENET_MONITOR_ON_PROPERTY_NAME = "sesame.logging.monitor";
	public static final String STATENET_MONITOR_DATASOURCE_PROPERTY_NAME = "sesame.logging.monitor.datasource.name";
	public static final String STATENET_MONITOR_DATASOURCE_DEFAULT = "SN_DATASOURCE";
	public static final String STATENET_MONITOR_JMS_PROPERTY_NAME = "sesame.logging.monitor.jms.name";
	public static final String STATENET_MONITOR_JMS_DEFAULT = "LOGTOPIC";
	public static final String STATENET_QUEUE_CONSUMERS_PROPERTY_NAME = "sesame.logging.monitor.consumers";
	public static final String STATENET_QUEUE_CONSUMERS_DEFAULT = "5";
	public static final String STATENET_MONITOR_LUCENEDIRECTORY_PROPERTY_NAME = "sesame.logging.monitor.lucene.name";
	public static final String STATENET_MONITOR_LUCENEDIRECTORY_DEFAULT = "logs/index";
	public static final int STATENET_MAXIMUM_MESSESGES = 10000;
	public static final int STATENET_VARCHAR_MAXIMUM_SIZE = 4000;
}