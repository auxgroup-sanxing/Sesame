package com.sanxing.sesame.logging.dao;

import java.sql.Timestamp;

public class LogBean extends BaseBean {
	private static final long serialVersionUID = 7993539740276309124L;
	private Long serialNumber;
	private Timestamp startTime;
	private Timestamp updateTime;
	private String state = "9";
	private String exceptionMessage;
	private String serviceName;
	private String operationName;
	private String transactionCode;
	private String channel;
	private String content;
	private String stage;
	private long expireTime;
	private Long count;
	private boolean callout = false;

	public String toString() {
		return "LogBean [channel=" + this.channel + ", content=" + this.content
				+ ", count=" + this.count + ", exceptionMessage="
				+ this.exceptionMessage + ", expireTime=" + this.expireTime
				+ ", operationName=" + this.operationName + ", serialNumber="
				+ this.serialNumber + ", serviceName=" + this.serviceName
				+ ", stage=" + this.stage + ", startTime=" + this.startTime
				+ ", state=" + this.state + ", transactionCode="
				+ this.transactionCode + ", updateTime=" + this.updateTime
				+ "]";
	}

	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isCallout() {
		return this.callout;
	}

	public void setCallout(boolean callout) {
		this.callout = callout;
	}

	public Long getCount() {
		return this.count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public Timestamp getStartTime() {
		return this.startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public Timestamp getUpdateTime() {
		return this.updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public String getServiceName() {
		return this.serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getOperationName() {
		return this.operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public String getTransactionCode() {
		return this.transactionCode;
	}

	public void setTransactionCode(String transactionCode) {
		this.transactionCode = transactionCode;
	}

	public String getChannel() {
		return this.channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public long getExpireTime() {
		return this.expireTime;
	}

	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}

	public String getStage() {
		return this.stage;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}

	public Long getSerialNumber() {
		return this.serialNumber;
	}

	public void setSerialNumber(Long serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getState() {
		return this.state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getExceptionMessage() {
		return this.exceptionMessage;
	}

	public void setExceptionMessage(String exceptionMessage) {
		this.exceptionMessage = exceptionMessage;
	}
}