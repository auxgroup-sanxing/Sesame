/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package com.sanxing.statenet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.auth.AuthChallengeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.statenet.auth.LoginConfiguration;
import com.sanxing.statenet.auth.PassiveCallbackHandler;
import com.sanxing.statenet.matcher.StringFilter;
import com.sanxing.statenet.matcher.StringMatcher;
import com.sanxing.statenet.IllegalDataException;
import com.sanxing.statenet.IllegalNameException;

/**
 * @author ShangjieZhou
 */
public class Authentication
    extends HttpServlet
    implements Filter
{
    private static final long serialVersionUID = 6500890152896700425L;
    
    private static final Logger LOG = LoggerFactory.getLogger( Authentication.class );

    private static StringMatcher stringMatcher;

    private static List<String> allowPath = new ArrayList();

    private static ThreadLocal<Subject> currSubject = new ThreadLocal();

    private FilterConfig filterConfig;

    /*(non-Javadoc)
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init( FilterConfig filterConfig )
        throws ServletException
    {
        this.filterConfig = filterConfig;
        this.filterConfig.getServletContext();

        allowPath.add( "^/$" );
        allowPath.add( "^/[^/]*.jsp" );
        allowPath.add( "^/ext-Ajax/.*" );
        allowPath.add( "^/package/.*" );

        allowPath.add( "^/LoginAction" );
        allowPath.add( "^/ResourceTree" );
        allowPath.add( "^/images/.*" );
        allowPath.add( "^/.*.css" );
        allowPath.add( "^/.*.jpg" );
        allowPath.add( "^/.*.gif" );
        stringMatcher = new StringFilter( allowPath, false );
    }

    /*(non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter( ServletRequest request, ServletResponse response, FilterChain filterChain )
    {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        try
        {
            String path = req.getRequestURI().substring( req.getContextPath().length() );

            LoginContext loginCtx = (LoginContext) req.getSession( true ).getAttribute( "LOGIN_CONTEXT" );
            if ( loginCtx == null )
            {
                if ( stringMatcher.match( path ) )
                {
                    filterChain.doFilter( request, response );
                }
                else
                {
                    String nonce = req.getSession( true ).getId().toLowerCase();

                    String auth = req.getHeader( "Authorization" );
                    if ( auth != null )
                    {
                        Map params = AuthChallengeParser.extractParams( auth );
                        params.put( "method", req.getMethod() );
                        params.put( "nonce", nonce );
                        try
                        {
                            String username = (String) params.get( "username" );
                            PassiveCallbackHandler handler = new PassiveCallbackHandler( username, params );
                            loginCtx = new LoginContext( "Statenet", null, handler, new LoginConfiguration() );
                            loginCtx.login();

                            currSubject.set( loginCtx.getSubject() );

                            req.getSession( true ).setAttribute( "LOGIN_CONTEXT", loginCtx );
                            filterChain.doFilter( request, response );
                        }
                        catch ( LoginException e )
                        {
                            LOG.error( e.getMessage() );
                            res.setHeader( "WWW-Authenticate",
                                "Digest realm=\"Statenet\",qop=\"auth,auth-int\",nonce=\"" + nonce + "\"" );
                            res.sendError( 401 );
                        }
                    }
                    else
                    {
                        res.setHeader( "WWW-Authenticate",
                            "Digest realm=\"Statenet\",qop=\"auth,auth-int\",nonce=\"" + nonce + "\"" );
                        res.sendError( 401 );
                    }
                }
            }
            else
            {
                currSubject.set( loginCtx.getSubject() );
                filterChain.doFilter( request, response );
            }
        }
        catch ( IOException e )
        {
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( e.getMessage() );
            }
        }
        catch ( Throwable throwable )
        {
            String message = throwable.getMessage();
            try
            {
                Throwable cause = throwable.getCause();
                if ( cause instanceof ClassNotFoundException )
                {
                    message = " 找不到类: " + cause.getMessage();
                }
                else if ( !( cause instanceof IllegalDataException ) )
                {
                    if ( !( cause instanceof IllegalNameException ) )
                    {
                        LOG.error( throwable.getMessage(), cause );
                    }
                }
                if ( !( res.isCommitted() ) )
                {
                    res.sendError( 500, message );
                }
            }
            catch ( IllegalStateException e )
            {
                LOG.error( e.getMessage(), e );
            }
            catch ( IOException e )
            {
                LOG.error( e.getMessage(), e );
            }
        }
    }

}
