/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package com.sanxing.statenet.transport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ShangjieZhou
 */
public class SocketClient
{
    private static final Logger LOG = LoggerFactory.getLogger( SocketClient.class );

    public static String send( String host, int port, String encoding, String request )
    {
        int i = 0;
        while ( i++ < 3 )
        {
            StringBuffer responseMsg = new StringBuffer();
            Socket socket = null;
            InputStream input;
            OutputStream out;
            BufferedReader br;
            PrintStream ps;
            try
            {
                // 向服务器申请连接
                socket = new Socket( host, port );
                input = socket.getInputStream();
                out = socket.getOutputStream();

                // 建立数据流
                br = new BufferedReader( new InputStreamReader( input, encoding ) );
                ps = new PrintStream( out, false, encoding );

                ps.println( request ); // 将读取得字符串传给server

                String str = "";
                while ( ( str = br.readLine() ) != null )
                {
                    responseMsg.append( str );
                    if ( !br.ready() )
                    {
                        break;
                    }
                }

                // 关闭连接
                input.close();
                out.close();
                br.close();
                ps.close();
                
                return responseMsg.toString();
            }
            catch ( Exception e )
            {
                LOG.error( e.getMessage(), e );
                continue;
            }
            finally
            {
                if ( socket != null && !socket.isClosed() )
                {
                    try
                    {
                        socket.close();
                    }
                    catch ( IOException e )
                    {
                        // ignore
                    }
                }
            }
        }
        return null;
    }

    public static void main(String[] args) throws Exception
    {
        JSONObject request = new JSONObject();
        request.put( "operation", "findOperByCode" );
        request.put( "oprCode", "chenke" );
        String result = SocketClient.send( "10.2.231.97", 8901, "GBK", request.toString() );
        JSONObject response = new JSONObject( result );
        System.out.println( response.getBoolean( "success" ) );
    }
}
