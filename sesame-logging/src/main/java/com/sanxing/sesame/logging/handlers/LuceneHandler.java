package com.sanxing.sesame.logging.handlers;

import com.sanxing.sesame.logging.dao.LogBean;
import com.sanxing.sesame.logging.lucene.LuceneSearcher;

public class LuceneHandler
    implements LogHandler
{
    @Override
    public void handle( LogBean bean )
    {
        LuceneSearcher searcher = LuceneSearcher.getInstance();
        searcher.addIndex( bean );
    }
}