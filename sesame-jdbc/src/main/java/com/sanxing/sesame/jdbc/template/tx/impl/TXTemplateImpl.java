package com.sanxing.sesame.jdbc.template.tx.impl;

import com.sanxing.sesame.jdbc.DataAccessException;
import com.sanxing.sesame.jdbc.DataAccessUtil;
import com.sanxing.sesame.jdbc.TXHelper;
import com.sanxing.sesame.jdbc.template.tx.DataAccessProcessor;
import com.sanxing.sesame.jdbc.template.tx.TXTemplate;

public class TXTemplateImpl
    implements TXTemplate
{
    private int txType = 0;

    public TXTemplateImpl( int txType )
    {
        this.txType = txType;
    }

    @Override
    public Object handle( DataAccessProcessor processor )
    {
        if ( txType == 2 )
        {
            TXHelper.beginTX();
        }
        else if ( txType == 1 )
        {
            TXHelper.beginNewTX();
        }
        else
        {
            throw new DataAccessException( "tx type[" + String.valueOf( txType ) + "] is invalid" );
        }

        Object result = null;
        try
        {
            result = processor.process();
            TXHelper.commit();
        }
        catch ( Throwable t )
        {
            TXHelper.rollback();
            DataAccessUtil.handleFault( t );
        }
        finally
        {
            TXHelper.close();
        }

        return result;
    }
}