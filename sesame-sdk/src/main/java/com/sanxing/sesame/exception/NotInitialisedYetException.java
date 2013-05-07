package com.sanxing.sesame.exception;

import javax.jbi.messaging.MessagingException;

public class NotInitialisedYetException
    extends MessagingException
{
    private static final long serialVersionUID = -5810854045831852360L;

    public NotInitialisedYetException()
    {
        super( "Cannot perform operations on this component until it has been initialised via init()" );
    }
}