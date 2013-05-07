package com.sanxing.sesame.logging.constants;

public abstract interface LogStage
{
    public static final String STAGE_ACCEPTOR_BEFORE_PARSE = "接入组件解码前";

    public static final String STAGE_ACCEPTOR_AFTER_PARSE = "接入组件解码后";

    public static final String STAGE_ACCEPTOR_BEFORE_ASSEMBLE = "接入组件编码前";

    public static final String STAGE_ACCEPTOR_AFTER_ASSEMBLE = "接入组件编码后";

    public static final String STAGE_CONNECTOR_BEFORE_ASSEMBLE = "callout编码前";

    public static final String STAGE_CONNECTOR_AFTER_ASSEMBLE = "callout编码后";

    public static final String STAGE_CONNECTOR_BEFORE_PARSE = "callout解码前";

    public static final String STAGE_CONNECTOR_AFTER_PARSE = "callout解码后";

    public static final String STAGE_END = "交易结束";
}