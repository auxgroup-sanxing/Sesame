package com.sanxing.studio.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class WebServletResponse
    implements HttpServletResponse
{
    private HttpServletResponse response = null;

    public WebServletResponse( HttpServletResponse response )
    {
        this.response = response;
    }

    @Override
    public void addCookie( Cookie arg0 )
    {
        response.addCookie( arg0 );
    }

    @Override
    public void addDateHeader( String arg0, long arg1 )
    {
        response.addDateHeader( arg0, arg1 );
    }

    @Override
    public void addHeader( String arg0, String arg1 )
    {
        response.addHeader( arg0, arg1 );
    }

    @Override
    public void addIntHeader( String arg0, int arg1 )
    {
        response.addIntHeader( arg0, arg1 );
    }

    @Override
    public boolean containsHeader( String arg0 )
    {
        return response.containsHeader( arg0 );
    }

    /** @deprecated */
    @Deprecated
    @Override
    public String encodeRedirectUrl( String arg0 )
    {
        return response.encodeRedirectUrl( arg0 );
    }

    @Override
    public String encodeRedirectURL( String arg0 )
    {
        return response.encodeRedirectURL( arg0 );
    }

    /** @deprecated */
    @Deprecated
    @Override
    public String encodeUrl( String arg0 )
    {
        return response.encodeUrl( arg0 );
    }

    @Override
    public String encodeURL( String arg0 )
    {
        return response.encodeURL( arg0 );
    }

    @Override
    public void flushBuffer()
        throws IOException
    {
        response.flushBuffer();
    }

    @Override
    public int getBufferSize()
    {
        return response.getBufferSize();
    }

    @Override
    public String getCharacterEncoding()
    {
        return response.getCharacterEncoding();
    }

    @Override
    public String getContentType()
    {
        return response.getContentType();
    }

    @Override
    public Locale getLocale()
    {
        return response.getLocale();
    }

    @Override
    public ServletOutputStream getOutputStream()
        throws IOException
    {
        return response.getOutputStream();
    }

    @Override
    public PrintWriter getWriter()
        throws IOException
    {
        return response.getWriter();
    }

    @Override
    public boolean isCommitted()
    {
        return response.isCommitted();
    }

    @Override
    public void reset()
    {
        response.reset();
    }

    @Override
    public void resetBuffer()
    {
        response.resetBuffer();
    }

    @Override
    public void sendError( int code, String message )
        throws IOException
    {
        if ( message == null )
        {
            message = "未知错误，请查看服务器日志";
        }
        response.setCharacterEncoding( "utf-8" );
        response.sendError( code, message );
    }

    @Override
    public void sendError( int arg0 )
        throws IOException
    {
        response.sendError( arg0 );
    }

    public void sendError( String message )
        throws IOException
    {
        if ( message == null )
        {
            message = "未知错误，请查看服务器日志";
        }
        response.setCharacterEncoding( "utf-8" );
        response.sendError( 500, message );
    }

    @Override
    public void sendRedirect( String arg0 )
        throws IOException
    {
        response.sendRedirect( arg0 );
    }

    @Override
    public void setBufferSize( int arg0 )
    {
        response.setBufferSize( arg0 );
    }

    @Override
    public void setCharacterEncoding( String arg0 )
    {
        response.setCharacterEncoding( arg0 );
    }

    @Override
    public void setContentLength( int arg0 )
    {
        response.setContentLength( arg0 );
    }

    @Override
    public void setContentType( String arg0 )
    {
        response.setContentType( arg0 );
    }

    @Override
    public void setDateHeader( String arg0, long arg1 )
    {
        response.setDateHeader( arg0, arg1 );
    }

    @Override
    public void setHeader( String arg0, String arg1 )
    {
        response.setHeader( arg0, arg1 );
    }

    @Override
    public void setIntHeader( String arg0, int arg1 )
    {
        response.setIntHeader( arg0, arg1 );
    }

    @Override
    public void setLocale( Locale arg0 )
    {
        response.setLocale( arg0 );
    }

    /** @deprecated */
    @Deprecated
    @Override
    public void setStatus( int arg0, String arg1 )
    {
        response.setStatus( arg0, arg1 );
    }

    @Override
    public void setStatus( int arg0 )
    {
        response.setStatus( arg0 );
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#getHeader(java.lang.String)
     */
    @Override
    public String getHeader( String arg0 )
    {
        return response.getHeader( arg0 );
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#getHeaderNames()
     */
    @Override
    public Collection<String> getHeaderNames()
    {
        return response.getHeaderNames();
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#getHeaders(java.lang.String)
     */
    @Override
    public Collection<String> getHeaders( String arg0 )
    {
        return response.getHeaders( arg0 );
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServletResponse#getStatus()
     */
    @Override
    public int getStatus()
    {
        return response.getStatus();
    }
}