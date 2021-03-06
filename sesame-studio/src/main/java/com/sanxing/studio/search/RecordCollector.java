package com.sanxing.studio.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecordCollector
{
    private final Map<String, List<Record>> recordMap = new HashMap();

    static Logger logger = LoggerFactory.getLogger( RecordCollector.class.getName() );

    private final String[] recordTypeNames = { "service", "interface", "operation", "message", "element" };

    public RecordCollector()
    {
        for ( String recordTypeName : recordTypeNames )
        {
            List list = new ArrayList();
            recordMap.put( recordTypeName, list );
        }
    }

    public void addRecord( Record record )
    {
        List list = getRecordListByType( record.getType() );
        list.add( record );
    }

    public void addRecord( Record record, Record base )
    {
        if ( base != null )
        {
            record.join( base, false );
        }
        addRecord( record );
    }

    private void debug( String prompt, Record record )
    {
        logger.debug( prompt + record.toString() );
    }

    private void debug( String prompt )
    {
        logger.debug( prompt );
        Iterator iterator = recordMap.entrySet().iterator();
        while ( iterator.hasNext() )
        {
            Map.Entry entry = (Map.Entry) iterator.next();
            List list = (List) entry.getValue();
            for ( int i = 0; i < list.size(); ++i )
            {
                Record rec = (Record) list.get( i );
                logger.debug( rec.toString() );
            }
        }
    }

    public void joinRecord( Record join, boolean indexAnalyzed )
    {
        Iterator iterator = recordMap.entrySet().iterator();
        while ( iterator.hasNext() )
        {
            Map.Entry entry = (Map.Entry) iterator.next();
            List list = (List) entry.getValue();
            for ( int i = 0; i < list.size(); ++i )
            {
                Record rec = (Record) list.get( i );
                rec.join( join, indexAnalyzed );
            }
        }
    }

    public List<Record> getRecordListByType( String name )
    {
        return recordMap.get( name );
    }

    public Map<String, List<Record>> getRecordMap()
    {
        return recordMap;
    }
}