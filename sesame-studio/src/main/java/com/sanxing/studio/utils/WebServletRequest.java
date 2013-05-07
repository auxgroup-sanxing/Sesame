package com.sanxing.studio.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

public class WebServletRequest
    implements HttpServletRequest
{
    private HttpServletRequest request = null;

    public WebServletRequest( HttpServletRequest request )
    {
        this.request = request;
    }

    @Override
    public Object getAttribute( String arg0 )
    {
        return request.getAttribute( arg0 );
    }

    @Override
    public Enumeration getAttributeNames()
    {
        return request.getAttributeNames();
    }

    @Override
    public String getAuthType()
    {
        return request.getAuthType();
    }

    @Override
    public String getCharacterEncoding()
    {
        return request.getCharacterEncoding();
    }

    @Override
    public int getContentLength()
    {
        return request.getContentLength();
    }

    @Override
    public String getContentType()
    {
        return request.getContentType();
    }

    @Override
    public String getContextPath()
    {
        return request.getContextPath();
    }

    @Override
    public Cookie[] getCookies()
    {
        return request.getCookies();
    }

    @Override
    public long getDateHeader( String arg0 )
    {
        return request.getDateHeader( arg0 );
    }

    @Override
    public String getHeader( String arg0 )
    {
        return request.getHeader( arg0 );
    }

    @Override
    public Enumeration getHeaderNames()
    {
        return request.getHeaderNames();
    }

    @Override
    public Enumeration getHeaders( String arg0 )
    {
        return request.getHeaders( arg0 );
    }

    @Override
    public ServletInputStream getInputStream()
        throws IOException
    {
        return request.getInputStream();
    }

    @Override
    public int getIntHeader( String arg0 )
    {
        return request.getIntHeader( arg0 );
    }

    @Override
    public String getLocalAddr()
    {
        return request.getLocalAddr();
    }

    @Override
    public Locale getLocale()
    {
        return request.getLocale();
    }

    @Override
    public Enumeration getLocales()
    {
        return request.getLocales();
    }

    @Override
    public String getLocalName()
    {
        return request.getLocalName();
    }

    @Override
    public int getLocalPort()
    {
        return request.getLocalPort();
    }

    @Override
    public String getMethod()
    {
        return request.getMethod();
    }

    @Override
    public String getParameter( String name )
    {
        String value = request.getParameter( name );
        String encoding = request.getCharacterEncoding();
        try
        {
            if ( ( value != null ) && ( request.getMethod().equals( "GET" ) ) )
            {
                value = new String( value.getBytes( "ISO8859-1" ), ( encoding != null ) ? encoding : "utf-8" );
            }
        }
        catch ( UnsupportedEncodingException e )
        {
            e.printStackTrace();
        }
        return value;
    }

    @Override
    public Map getParameterMap()
    {
        return request.getParameterMap();
    }

    @Override
    public Enumeration getParameterNames()
    {
        return request.getParameterNames();
    }

    @Override
    public String[] getParameterValues( String name )
    {
        String[] values = request.getParameterValues( name );
        String encoding = request.getCharacterEncoding();
        for ( int i = 0; i < values.length; ++i )
        {
            String value = values[i];
            try
            {
                if ( ( value != null ) && ( request.getMethod().equals( "GET" ) ) )
                {
                    values[i] = new String( value.getBytes( "ISO8859-1" ), ( encoding != null ) ? encoding : "utf-8" );
                }
            }
            catch ( UnsupportedEncodingException e )
            {
                e.printStackTrace();
            }
        }
        return values;
    }

    @Override
    public String getPathInfo()
    {
        return request.getPathInfo();
    }

    @Override
    public String getPathTranslated()
    {
        return request.getPathTranslated();
    }

    @Override
    public String getProtocol()
    {
        return request.getProtocol();
    }

    @Override
    public String getQueryString()
    {
        return request.getQueryString();
    }

    @Override
    public BufferedReader getReader()
        throws IOException
    {
        return request.getReader();
    }

    /** @deprecated */
    @Deprecated
    @Override
    public String getRealPath( String arg0 )
    {
        return request.getRealPath( arg0 );
    }

    @Override
    public String getRemoteAddr()
    {
        return request.getRemoteAddr();
    }

    @Override
    public String getRemoteHost()
    {
        return request.getRemoteHost();
    }

    @Override
    public int getRemotePort()
    {
        return request.getRemotePort();
    }

    @Override
    public String getRemoteUser()
    {
        return request.getRemoteUser();
    }

    @Override
    public RequestDispatcher getRequestDispatcher( String arg0 )
    {
        return request.getRequestDispatcher( arg0 );
    }

    @Override
    public String getRequestedSessionId()
    {
        return request.getRequestedSessionId();
    }

    @Override
    public String getRequestURI()
    {
        return request.getRequestURI();
    }

    @Override
    public StringBuffer getRequestURL()
    {
        return request.getRequestURL();
    }

    @Override
    public String getScheme()
    {
        return request.getScheme();
    }

    @Override
    public String getServerName()
    {
        return request.getServerName();
    }

    @Override
    public int getServerPort()
    {
        return request.getServerPort();
    }

    @Override
    public String getServletPath()
    {
        return request.getServletPath();
    }

    @Override
    public HttpSession getSession()
    {
        return request.getSession();
    }

    @Override
    public HttpSession getSession( boolean arg0 )
    {
        return request.getSession( arg0 );
    }

    @Override
    public Principal getUserPrincipal()
    {
        return request.getUserPrincipal();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie()
    {
        return request.isRequestedSessionIdFromCookie();
    }

    /** @deprecated */
    @Deprecated
    @Override
    public boolean isRequestedSessionIdFromUrl()
    {
        return request.isRequestedSessionIdFromUrl();
    }

    @Override
    public boolean isRequestedSessionIdFromURL()
    {
        return request.isRequestedSessionIdFromURL();
    }

    @Override
    public boolean isRequestedSessionIdValid()
    {
        return request.isRequestedSessionIdValid();
    }

    @Override
    public boolean isSecure()
    {
        return request.isSecure();
    }

    @Override
    public boolean isUserInRole( String arg0 )
    {
        return request.isUserInRole( arg0 );
    }

    @Override
    public void removeAttribute( String arg0 )
    {
        request.removeAttribute( arg0 );
    }

    @Override
    public void setAttribute( String arg0, Object arg1 )
    {
        request.setAttribute( arg0, arg1 );
    }

    @Override
    public void setCharacterEncoding( String arg0 )
        throws UnsupportedEncodingException
    {
        request.setCharacterEncoding( arg0 );
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.ServletRequest#getAsyncContext()
     */
    @Override
    public AsyncContext getAsyncContext()
    {
        return request.getAsyncContext();
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.ServletRequest#getDispatcherType()
     */
    @Override
    public DispatcherType getDispatcherType()
    {
        return request.getDispatcherType();
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.ServletRequest#getServletContext()
     */
    @Override
    public ServletContext getServletContext()
    {
        return request.getServletContext();
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.ServletRequest#isAsyncStarted()
     */
    @Override
    public boolean isAsyncStarted()
    {
        return request.isAsyncStarted();
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.ServletRequest#isAsyncSupported()
     */
    @Override
    public boolean isAsyncSupported()
    {
        return request.isAsyncSupported();
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.ServletRequest#startAsync()
     */
    @Override
    public AsyncContext startAsync()
        throws IllegalStateException
    {
        return request.startAsync();
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.ServletRequest#startAsync(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    @Override
    public AsyncContext startAsync( ServletRequest arg0, ServletResponse arg1 )
        throws IllegalStateException
    {
        return request.startAsync( arg0, arg1 );
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#authenticate(javax.servlet.http.HttpServletResponse)
     */
    @Override
    public boolean authenticate( HttpServletResponse arg0 )
        throws IOException, ServletException
    {
        return request.authenticate( arg0 );
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getPart(java.lang.String)
     */
    @Override
    public Part getPart( String arg0 )
        throws IOException, ServletException
    {
        return request.getPart( arg0 );
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getParts()
     */
    @Override
    public Collection<Part> getParts()
        throws IOException, ServletException
    {
        return request.getParts();
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#login(java.lang.String, java.lang.String)
     */
    @Override
    public void login( String arg0, String arg1 )
        throws ServletException
    {
        request.login( arg0, arg1 );
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#logout()
     */
    @Override
    public void logout()
        throws ServletException
    {
        request.logout();
    }
}