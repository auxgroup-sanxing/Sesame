package com.sanxing.sesame.transport.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.mail.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.binding.codec.BinaryResult;
import com.sanxing.sesame.binding.context.MessageContext;
import com.sanxing.sesame.transport.quartz.SesameScheduler;
import com.sanxing.sesame.transport.util.SesameMailClient;

public class MailAcceptor
    extends TaskTransport
{
    private static Logger LOG = LoggerFactory.getLogger( MailAcceptor.class );

    private SesameMailClient mailClient;

    @Override
    public String getCharacterEncoding()
    {
        return "utf-8";
    }

    public String getDescription()
    {
        return "mail transport";
    }

    @Override
    public void reply( MessageContext context )
        throws IOException
    {
        BinaryResult result = (BinaryResult) context.getResult();
        byte[] resp = result.getBytes();
        try
        {
            LOG.debug( "reply get message is:::::::::::::::::[" + new String( resp ) + "]" );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    @Override
    public void close()
        throws IOException
    {
        try
        {
            scheduler.shutdown();
            workExecutor.shutdown();
            mailClient = null;
            active = false;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            throw new IOException( e.getMessage() );
        }
    }

    @Override
    public void open()
        throws IOException
    {
        try
        {
            active = true;
            mailClient = new SesameMailClient();
            mailClient.setProperties( properties );

            mailClient.initPOPServer();

            scheduler = new SesameScheduler();
            scheduler.init();
            scheduler.start();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            throw new IOException( e.getMessage() );
        }
    }

    @Override
    public void executeTask( Map<?, ?> properties )
        throws Exception
    {
        Message[] msgs = mailClient.getAllMails();
        for ( Message msg : msgs )
        {
            byte[] contentBuf = mailClient.processOneMail( msg );

            if ( contentBuf == null )
            {
                return;
            }
            InputStream in = new ByteArrayInputStream( contentBuf );

            String operationName = msg.getSubject();
            accept( in, operationName, (String) properties.get( "jobName" ) );
        }
        mailClient.disconnecteStore();
    }
}