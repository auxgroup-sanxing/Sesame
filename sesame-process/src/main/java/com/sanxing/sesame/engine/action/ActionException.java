package com.sanxing.sesame.engine.action;

import com.sanxing.sesame.exceptions.SystemException;

public class ActionException
    extends SystemException
{
    private static final long serialVersionUID = 1168364675955353694L;

    private String message;

    static
    {
        registerErrMsgs( "process_err.msg" );
    }

    public ActionException( AbstractAction action )
    {
        setErrMsgArgs( new String[] { action.getName() + "-" + action.getActionId() } );
    }

    public ActionException( AbstractAction action, String errorCode )
    {
        setErrMsgArgs( new String[] { action.getName() + "-" + action.getActionId() } );
        setErrorCode( errorCode );
    }

    public ActionException( AbstractAction action, Throwable e )
    {
        super( e );
        setErrMsgArgs( new String[] { action.getName() + "-" + action.getActionId() } );
        setErrorCode( "99999" );
    }

    public ActionException( String errorCode )
    {
        setErrorCode( errorCode );
    }

    public ActionException( String errorCode, String message )
    {
        this( errorCode );
        this.message = message;
    }

    public ActionException( String errorCode, String message, Throwable cause )
    {
        this( cause, errorCode );
        this.message = message;
    }

    public ActionException()
    {
        setErrorCode( "99999" );
    }

    public ActionException( Throwable cause )
    {
        super( cause );
        setErrorCode( "99999" );
    }

    public ActionException( Throwable cause, String errorCode )
    {
        super( cause );
        setErrorCode( errorCode );
    }

    @Override
    public String getMessage()
    {
        if ( message != null )
        {
            return message;
        }

        return super.getMessage();
    }

    @Override
    public String getModuleName()
    {
        return "998";
    }
}