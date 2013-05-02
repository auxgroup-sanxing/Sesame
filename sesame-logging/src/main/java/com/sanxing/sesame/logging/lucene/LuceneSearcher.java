package com.sanxing.sesame.logging.lucene;

import com.sanxing.sesame.logging.dao.LogBean;
import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.index.LogDocMergePolicy;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class LuceneSearcher {
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
	private Analyzer analyzer = new SmartChineseAnalyzer(Version.LUCENE_CURRENT);

	private static final Logger LOG = LoggerFactory.getLogger(LuceneSearcher.class);

	private static LuceneSearcher luceneSearcher = null;

	private static AtomicLong total = new AtomicLong(0L);

	private byte[] lock = new byte[0];

	private LuceneSearcher() {
	}

	private LuceneSearcher(String dir) {
		this.indexDirectory = dir;
	}

	public static synchronized LuceneSearcher getInstance() {
		if (luceneSearcher == null) {
			synchronized (LuceneSearcher.class) {
				if (luceneSearcher == null) {
					String dir = System.getProperty("STATENET_HOME")
							+ "/"
							+ System.getProperty(
									"sesame.logging.monitor.lucene.name",
									"logs/index");
					luceneSearcher = new LuceneSearcher(dir);
					luceneSearcher.createIndex();
				}
			}
		}
		return luceneSearcher;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void createIndex() {
		try {
			File indexDIR = new File(this.indexDirectory);
			this.directory = FSDirectory.open(indexDIR);
			this.writer = new IndexWriter(this.directory, getAnalyzer(), true,
					IndexWriter.MaxFieldLength.LIMITED);
			LogDocMergePolicy mp = new LogDocMergePolicy(this.writer);
			mp.setMergeFactor(1000);
			mp.setMinMergeDocs(1000);
			this.writer.setMergePolicy(mp);

			this.ramDir = new RAMDirectory();
			this.ramWriter = new IndexWriter(this.ramDir, getAnalyzer(), true,
					IndexWriter.MaxFieldLength.LIMITED);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void addIndex(LuceneRecord record) {
		List<LuceneColumn> columns = record.getColumns();
		Document doc = new Document();
		for (LuceneColumn column : columns) {
			String field = column.getName();
			if ((field != null) && (column.getValue() != null)) {
				if (column.isIndexAnalyzed())
					doc.add(new Field(field, column.getValue(),
							Field.Store.YES, Field.Index.ANALYZED));
				else
					doc.add(new Field(field, column.getValue(),
							Field.Store.YES, Field.Index.NO));
			}
		}
		try {
			this.ramWriter.addDocument(doc);
			if (total.addAndGet(1L) >= 1024L) {
				this.ramWriter.close();
				this.writer
						.addIndexesNoOptimize(new Directory[] { this.ramDir });
				this.writer.commit();
				this.ramWriter = new IndexWriter(this.ramDir, getAnalyzer(),
						true, IndexWriter.MaxFieldLength.LIMITED);
				total.set(0L);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void flushIndex() {
		try {
			if (this.ramWriter != null) {
				this.ramWriter.close();
				this.writer
						.addIndexesNoOptimize(new Directory[] { this.ramDir });
				this.writer.commit();
				this.ramWriter = new IndexWriter(this.ramDir, getAnalyzer(),
						true, IndexWriter.MaxFieldLength.LIMITED);
				total.set(0L);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addIndex(LogBean bean) {
		addIndex(new LuceneLogBeanRecord(bean));
	}

	public void closeIndex() {
		try {
			this.writer.optimize();
			this.writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int parse(String queryString, String[] fields) {
		try {
			this.reader = IndexReader.open(
					FSDirectory.open(new File(this.indexDirectory)), true);

			this.searcher = new IndexSearcher(this.reader);

			this.parser = new MultiFieldQueryParser(Version.LUCENE_CURRENT,
					fields, getAnalyzer());
			this.query = this.parser.parse(queryString);
			this.collector = TopScoreDocCollector.create(
					this.searcher.maxDoc(), true);
			this.searcher.search(this.query, this.collector);
			int numTotalHits = this.collector.getTotalHits();
			return numTotalHits;
		} catch (Exception e) {
			System.out.print(e);
			e.printStackTrace();
		}
		return -1;
	}

	public Set<LuceneRecord> pageSearch(int pageNo, int pageSize) {
		try {
			this.collector = TopScoreDocCollector.create(
					this.searcher.maxDoc(), true);
			this.searcher.search(this.query, this.collector);
			ScoreDoc[] hits = this.collector.topDocs((pageNo - 1) * pageSize,
					pageSize).scoreDocs;

			Set records = new TreeSet();
			for (int i = 0; i < hits.length; ++i) {
				Document doc = this.searcher.doc(hits[i].doc);
				LuceneRecord rec = new LuceneLogBeanRecord();
				List<Fieldable> fields = doc.getFields();
				for (Fieldable field : fields) {
					rec.addField(new LuceneColumn(field.name(), field
							.stringValue(), false));
				}
				rec.setSearcherName(getName());
				System.out.println(i + ": " + rec.toString());
				records.add(rec);
			}
			return records;
		} catch (Exception e) {
			System.out.print(e);
			e.printStackTrace();
		}
		return null;
	}

	private Analyzer getAnalyzer() {
		return this.analyzer;
	}

	public Set<LuceneRecord> prepareData() {
		Set<LuceneRecord> records = new TreeSet<LuceneRecord>();

		LuceneRecord rec = new LuceneLogBeanRecord();
		LuceneColumn col1 = new LuceneColumn("id", "0001", true);
		LuceneColumn col2 = new LuceneColumn("name", "GangService", true);
		LuceneColumn col3 = new LuceneColumn("address", "service for gang",
				true);
		rec.addField(col1);
		rec.addField(col2);
		rec.addField(col3);
		records.add(rec);

		LogBean bean = new LogBean();
		bean.setServiceName("<gang>LogMonitorProxy</gang>");
		addIndex(bean);

		return records;
	}
}