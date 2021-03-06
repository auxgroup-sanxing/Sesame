package com.sanxing.sesame.engine.action.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.jdom.Element;
import org.jdom.input.DOMBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.ActionException;
import com.sanxing.sesame.engine.action.Constant;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.ExecutionContext;
import com.sanxing.sesame.engine.context.Variable;

import static com.sanxing.sesame.engine.ExecutionEnv.*;

public class DBAction
    extends AbstractAction
{
    private static Logger LOG = LoggerFactory.getLogger( DBAction.class );

    private Element _config;

    @Override
    public void doinit( Element config )
    {
        _config = config;
    }

    @Override
    public void dowork( DataContext messageCtx )
    {
        ExecutionContext execContext = messageCtx.getExecutionContext();
        Connection conn = null;
        try
        {
            String dsn = _config.getAttributeValue( Constant.ATTR_DSN_NAME );
            String var = _config.getAttributeValue( Constant.ATTR_VAR_NAME );
            String toVar = _config.getAttributeValue( Constant.ATTR_TO_VAR_NAME );
            String sql = _config.getTextTrim();
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( "Original SQL: " + sql );
            }

            Element paramEl = (Element) messageCtx.getVariable( var ).get();

            TransactionManager xact = execContext.getCurrentTM();
            conn = getConnection( execContext, dsn );

            if ( ( sql == null ) || ( sql.length() == 0 ) )
            {
                throw new ActionException( "SQL is blank" );
            }
            if ( sql.regionMatches( true, 0, "SELECT", 0, 5 ) )
            {
                PreparedStatement stmt = prepareStmt( conn, sql, paramEl );
                ResultSet set = stmt.executeQuery();
                org.w3c.dom.Document doc = RS2DOM.ResultSet2DOM( set );
                org.jdom.Document jdomDoc = new DOMBuilder().build( doc );
                Element jdomEle = jdomDoc.getRootElement();
                Variable result = new Variable( jdomEle, 0 );
                messageCtx.addVariable( toVar, result );
            }
            else
            {
                PreparedStatement stmt = prepareStmt( conn, sql, paramEl );
                stmt.execute();
                int affected = stmt.getUpdateCount();
                LOG.info( "Affected: [" + affected + "]" );
                Element resultEl = new Element( "result" );
                resultEl.addContent( new Element( "affected" ).setText( String.valueOf( affected ) ) );
                Variable result = new Variable( resultEl, 0 );
                messageCtx.addVariable( toVar, result );
            }

        }
        catch ( SQLException e )
        {
            LOG.error( e.getMessage(), e );
            throw new ActionException( this, e );
        }
        catch ( NamingException e )
        {
            LOG.error( e.getMessage(), e );
            throw new ActionException( this, e );
        }
        finally
        {
            if ( conn != null )
            {
                LOG.debug( "Close connection" );
                try
                {
                    conn.close();
                }
                catch ( SQLException e )
                {
                    LOG.error( e.getMessage(), e );
                }
            }
        }
    }

    private Connection getConnection( ExecutionContext execContext, String dsn )
        throws NamingException, SQLException
    {
        Context context = (Context) execContext.get( NAMING_CONTEXT );
        DataSource datasource = (DataSource) context.lookup( dsn );
        return datasource.getConnection();
    }

    private static PreparedStatement prepareStmt( Connection conn, String sql, Element paramEl )
        throws SQLException
    {
        Pattern p = Pattern.compile( ":([A-Za-z_]\\w*)", 2 );
        Matcher m = p.matcher( sql );

        String preparedSQL = m.replaceAll( "?" );
        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( "SQL: " + preparedSQL );
        }

        PreparedStatement stmt = conn.prepareStatement( preparedSQL, 1004, 1007 );

        int i = 0;
        m.reset();
        while ( m.find() )
        {
            String param = m.group( 1 );
            stmt.setObject( ++i, paramEl.getChildText( param, paramEl.getNamespace() ) );
        }
        return stmt;
    }
}