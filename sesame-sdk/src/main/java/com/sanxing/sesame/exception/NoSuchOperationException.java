package com.sanxing.sesame.exception;

import javax.jbi.messaging.MessagingException;
import javax.xml.namespace.QName;

public class NoSuchOperationException
    extends MessagingException
{
    private static final long serialVersionUID = 7522792718495917920L;

    private final QName operationName;

    public NoSuchOperationException( QName operationName )
    {
        super( "No such operation name: " + operationName );
        this.operationName = operationName;
    }

    public QName getOperationName()
    {
        return operationName;
    }
}