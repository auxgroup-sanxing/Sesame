package com.sanxing.studio.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TransformWriter
{
    private int index = 1;

    private int parentId = -1;

    private final PreparedStatement stmtSource;

    private final PreparedStatement stmtTarget;

    private final Connection connection;

    public TransformWriter( int svcid, int subseq, Connection conn )
        throws SQLException
    {
        connection = conn;
        stmtTarget = conn.prepareStatement( "INSERT INTO subreqcfg VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" );

        stmtSource = conn.prepareStatement( "INSERT INTO subfldsrccfg VALUES(?, ?, ?, ?, ?, ?, ?, ?)" );

        stmtTarget.setInt( 1, svcid );
        stmtTarget.setInt( 2, subseq );
    }

    public void closeStmt()
    {
        try
        {
            stmtTarget.close();
            stmtSource.close();
        }
        catch ( SQLException e )
        {
            e.printStackTrace();
        }
    }

    public void saveMappings( JSONArray mappings, int level, int repeat, int repSubseq, int repMsgid, int repFldid,
                              int kind, int flag )
        throws SQLException, JSONException
    {
        for ( int i = 0; i < mappings.length(); ++i )
        {
            JSONObject field = mappings.getJSONObject( i );
            JSONArray innerFields = field.optJSONArray( "fields" );
            if ( innerFields != null )
            {
                int index = this.index;
                JSONObject countField = field.optJSONObject( "countField" );
                int lRepeat = ( countField == null ) ? 0 : 1;
                int lSubseq = ( countField == null ) ? 0 : countField.optInt( "subseq" );
                int lMsgid = ( countField == null ) ? 0 : countField.optInt( "msgid" );
                int lFldid = ( countField == null ) ? 0 : countField.optInt( "fldid" );
                int lLevel = level;
                if ( !( field.optBoolean( "isHeader", false ) ) )
                {
                    index = this.index++;
                    lLevel = level + 1;
                    stmtTarget.setInt( 3, parentId-- );
                    stmtTarget.setInt( 4, index );
                    stmtTarget.setInt( 5, lSubseq );
                    stmtTarget.setInt( 6, lMsgid );
                    stmtTarget.setInt( 7, lFldid );
                    stmtTarget.setInt( 8, 0 );
                    stmtTarget.setInt( 9, getNewMapId() );
                    stmtTarget.setInt( 10, flag );
                    stmtTarget.setInt( 11, kind );
                    stmtTarget.setInt( 12, lRepeat );
                    stmtTarget.setInt( 13, lLevel );
                    stmtTarget.executeUpdate();
                }
                saveMappings( innerFields, lLevel, lRepeat, lSubseq, lMsgid, lFldid, kind, flag );
            }
            else
            {
                int mapId = getNewMapId();
                stmtTarget.setInt( 3, field.getInt( "fldid" ) );
                stmtTarget.setInt( 4, index++ );
                stmtTarget.setInt( 5, repSubseq );
                stmtTarget.setInt( 6, repMsgid );
                stmtTarget.setInt( 7, repFldid );
                stmtTarget.setInt( 8, field.optInt( "funcId" ) );
                stmtTarget.setInt( 9, mapId );
                stmtTarget.setInt( 10, flag );
                stmtTarget.setInt( 11, kind );
                stmtTarget.setInt( 12, repeat );
                stmtTarget.setInt( 13, level );
                stmtTarget.executeUpdate();
                saveParams( field.getJSONArray( "params" ), mapId );
            }
        }
    }

    private void saveParams( JSONArray array, int mapId )
        throws JSONException, SQLException
    {
        int seq = 0;
        for ( int i = 0; i < array.length(); ++i )
        {
            JSONObject param = array.getJSONObject( i );
            stmtSource.setInt( 1, mapId );
            stmtSource.setInt( 2, seq++ );
            stmtSource.setInt( 3, param.optInt( "msgid" ) );
            stmtSource.setInt( 4, param.optInt( "subseq" ) );
            stmtSource.setInt( 5, param.optInt( "fldid" ) );
            stmtSource.setInt( 6, param.optInt( "selStart" ) );
            stmtSource.setInt( 7, param.optInt( "selEnd" ) );
            stmtSource.setString( 8, param.optString( "value" ) );
            stmtSource.executeUpdate();
        }
    }

    private int getNewMapId()
        throws SQLException
    {
        Statement stmt = connection.createStatement();
        try
        {
            ResultSet rs = stmt.executeQuery( "SELECT MAX(asmid) FROM subreqcfg" );
            rs.next();
            int i = rs.getInt( 1 ) + 1;

            return i;
        }
        finally
        {
            stmt.close();
        }
    }

    public void resetIndex()
    {
        index = 1;
        parentId = -1;
    }
}