package com.sanxing.sesame.logging.lucene;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.LogDocMergePolicy;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.sesame.logging.constants.LogConfig;
import com.sanxing.sesame.logging.dao.LogBean;

public class LuceneSearcher
{
    private String name;

    private String indexDirectory;

    private FSDirectory directory;

    private IndexWriter writer;

    private RAMDirectory ramDir;

    private IndexWriter ramWriter;

    private IndexReader reader;

    private Searcher searcher;

    private MultiFieldQueryParser parser;

    private Query query;

    private TopScoreDocCollector collector;

    private final Analyzer analyzer = new SmartChineseAnalyzer( Version.LUCENE_CURRENT );

    private static final Logger LOG = LoggerFactory.getLogger( LuceneSearcher.class );

    private static LuceneSearcher luceneSearcher = null;

    private static AtomicLong total = new AtomicLong( 0L );

    private final byte[] lock = new byte[0];

    private LuceneSearcher()
    {
    }

    private LuceneSearcher( String dir )
    {
        indexDirectory = dir;
    }

    public static synchronized LuceneSearcher getInstance()
    {
        if ( luceneSearcher == null )
        {
            synchronized ( LuceneSearcher.class )
            {
                if ( luceneSearcher == null )
                {
                    String dir =
                        System.getProperty( "SESAME_HOME" ) + "/"
                            + System.getProperty( LogConfig.SESAME_MONITOR_LUCENEDIRECTORY_PROPERTY_NAME, LogConfig.SESAME_MONITOR_LUCENEDIRECTORY_DEFAULT );
                    luceneSearcher = new LuceneSearcher( dir );
                    luceneSearcher.createIndex();
                }
            }
        }
        return luceneSearcher;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public void createIndex()
    {
        try
        {
            File indexDIR = new File( indexDirectory );
            directory = FSDirectory.open( indexDIR );
            writer = new IndexWriter( directory, getAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED );
            LogDocMergePolicy mp = new LogDocMergePolicy( writer );
            mp.setMergeFactor( 1000 );
            mp.setMinMergeDocs( 1000 );
            writer.setMergePolicy( mp );

            ramDir = new RAMDirectory();
            ramWriter = new IndexWriter( ramDir, getAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    public synchronized void addIndex( LuceneRecord record )
    {
        List<LuceneColumn> columns = record.getColumns();
        Document doc = new Document();
        for ( LuceneColumn column : columns )
        {
            String field = column.getName();
            if ( ( field != null ) && ( column.getValue() != null ) )
            {
                if ( column.isIndexAnalyzed() )
                {
                    doc.add( new Field( field, column.getValue(), Field.Store.YES, Field.Index.ANALYZED ) );
                }
                else
                {
                    doc.add( new Field( field, column.getValue(), Field.Store.YES, Field.Index.NO ) );
                }
            }
        }
        try
        {
            ramWriter.addDocument( doc );
            if ( total.addAndGet( 1L ) >= 1024L )
            {
                ramWriter.close();
                writer.addIndexesNoOptimize( new Directory[] { ramDir } );
                writer.commit();
                ramWriter = new IndexWriter( ramDir, getAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED );
                total.set( 0L );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    public synchronized void flushIndex()
    {
        try
        {
            if ( ramWriter != null )
            {
                ramWriter.close();
                writer.addIndexesNoOptimize( new Directory[] { ramDir } );
                writer.commit();
                ramWriter = new IndexWriter( ramDir, getAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED );
                total.set( 0L );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    public void addIndex( LogBean bean )
    {
        addIndex( new LuceneLogBeanRecord( bean ) );
    }

    public void closeIndex()
    {
        try
        {
            writer.optimize();
            writer.close();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    public int parse( String queryString, String[] fields )
    {
        try
        {
            reader = IndexReader.open( FSDirectory.open( new File( indexDirectory ) ), true );

            searcher = new IndexSearcher( reader );

            parser = new MultiFieldQueryParser( Version.LUCENE_CURRENT, fields, getAnalyzer() );
            query = parser.parse( queryString );
            collector = TopScoreDocCollector.create( searcher.maxDoc(), true );
            searcher.search( query, collector );
            int numTotalHits = collector.getTotalHits();
            return numTotalHits;
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
        }
        return -1;
    }

    public Set<LuceneRecord> pageSearch( int pageNo, int pageSize )
    {
        try
        {
            collector = TopScoreDocCollector.create( searcher.maxDoc(), true );
            searcher.search( query, collector );
            ScoreDoc[] hits = collector.topDocs( ( pageNo - 1 ) * pageSize, pageSize ).scoreDocs;

            Set records = new TreeSet();
            for ( int i = 0; i < hits.length; ++i )
            {
                Document doc = searcher.doc( hits[i].doc );
                LuceneRecord rec = new LuceneLogBeanRecord();
                List<Fieldable> fields = doc.getFields();
                for ( Fieldable field : fields )
                {
                    rec.addField( new LuceneColumn( field.name(), field.stringValue(), false ) );
                }
                rec.setSearcherName( getName() );
                LOG.info( i + ": " + rec.toString() );
                records.add( rec );
            }
            return records;
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
        }
        return null;
    }

    private Analyzer getAnalyzer()
    {
        return analyzer;
    }

    public Set<LuceneRecord> prepareData()
    {
        Set<LuceneRecord> records = new TreeSet<LuceneRecord>();

        LuceneRecord rec = new LuceneLogBeanRecord();
        LuceneColumn col1 = new LuceneColumn( "id", "0001", true );
        LuceneColumn col2 = new LuceneColumn( "name", "GangService", true );
        LuceneColumn col3 = new LuceneColumn( "address", "service for gang", true );
        rec.addField( col1 );
        rec.addField( col2 );
        rec.addField( col3 );
        records.add( rec );

        LogBean bean = new LogBean();
        bean.setServiceName( "<gang>LogMonitorProxy</gang>" );
        addIndex( bean );

        return records;
    }
}