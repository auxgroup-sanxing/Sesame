package com.sanxing.sesame;

public abstract interface JbiConstants
{
    public static final String SEND_SYNC = "javax.jbi.messaging.sendSync";

    public static final String PROTOCOL_TYPE = "javax.jbi.messaging.protocol.type";

    public static final String PROTOCOL_HEADERS = "javax.jbi.messaging.protocol.headers";

    public static final String SECURITY_SUBJECT = "javax.jbi.security.subject";

    public static final String SOAP_HEADERS = "com.sanxing.sesame.soap.headers";

    public static final String PERSISTENT_PROPERTY_NAME = "com.sanxing.sesame.persistent";

    public static final String DATESTAMP_PROPERTY_NAME = "com.sanxing.sesame.datestamp";

    public static final String STATELESS_CONSUMER = "com.sanxing.sesame.consumer.stateless";

    public static final String STATELESS_PROVIDER = "com.sanxing.sesame.provider.stateless";

    public static final String SENDER_ENDPOINT = "com.sanxing.sesame.senderEndpoint";

    public static final String HTTP_DESTINATION_URI = "com.sanxing.sesame.http.destination.uri";

    public static final String MESSAGING_LOGGER = "com.sanxing.sesame.messaging.logger";

    public static final String CORRELATION_ID = "com.sanxing.sesame.correlationId";

    public static final String SESAME_SLOGAN = "Sesame is an ESB made in China";
}