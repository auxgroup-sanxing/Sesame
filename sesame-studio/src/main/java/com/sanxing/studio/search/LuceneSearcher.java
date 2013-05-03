package com.sanxing.studio.search;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class LuceneSearcher implements StringSearcher {
	private String name;
	private String indexDirectory;
	private IndexWriter writer;
	private Analyzer analyzer = new SmartChineseAnalyzer(Version.LUCENE_CURRENT);

	private Set<String> fieldSet = new TreeSet();

	static Logger logger = LoggerFactory.getLogger(LuceneSearcher.class.getName());

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LuceneSearcher(String dir) {
		this.indexDirectory = dir;
		cleanup();
	}

	public void createIndex() {
		try {
			File indexDIR = new File(this.indexDirectory);
			this.writer = new IndexWriter(FSDirectory.open(indexDIR),
					getAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addIndex(Record record) {
		logger.debug("add index " + record.toString());
		List<Column> columns = record.getColumns();
		Document doc = new Document();
		for (Column column : columns) {
			String field = column.getName();
			if ((field != null) && (column.getValue() != null)) {
				if (column.isIndexAnalyzed()) {
					doc.add(new Field(field, column.getValue(),
							Field.Store.YES, Field.Index.ANALYZED));
				} else {
					doc.add(new Field(field, column.getValue(),
							Field.Store.YES, Field.Index.NO));
				}

				this.fieldSet.add(field);
			}
		}
		try {
			this.writer.addDocument(doc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void closeIndex() {
		try {
			this.writer.optimize();
			this.writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void cleanup() {
		DeleteFileUtil.delete(this.indexDirectory);
	}

	public Set<Record> search(String queryString, int maxHits) {
		try {
			IndexReader reader = IndexReader.open(
					FSDirectory.open(new File(this.indexDirectory)), true);

			Searcher searcher = new IndexSearcher(reader);

			MultiFieldQueryParser parser = new MultiFieldQueryParser(
					Version.LUCENE_CURRENT,
					(String[]) (String[]) this.fieldSet
							.toArray(new String[this.fieldSet.size()]),
					getAnalyzer());

			Query query = parser.parse(queryString);

			TopScoreDocCollector collector = TopScoreDocCollector.create(
					maxHits, true);

			searcher.search(query, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			int numTotalHits = collector.getTotalHits();
			logger.debug("Searcher " + getName() + ":" + numTotalHits
					+ " total matching documents");

			Set records = new TreeSet();
			for (int i = 0; (i < hits.length) && (i < maxHits); ++i) {
				Document doc = searcher.doc(hits[i].doc);
				Record rec = RecordFactory.createRecord(getName());
				List<Fieldable> fields = doc.getFields();
				for (Fieldable field : fields) {
					rec.addField(new Column(field.name(), field.stringValue(),
							false));
				}

				rec.setSearcherName(getName());
				logger.debug(i + ": " + rec.toString());
				records.add(rec);
			}
			reader.close();
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

	public static Set<Record> prepareData() {
		Set records = new TreeSet();

		Record rec = new ServiceRecord();
		Column col1 = new Column("id", "0001", true);
		Column col2 = new Column("name", "GangService", true);
		Column col3 = new Column("address", "service for gang", true);
		rec.addField(col1);
		rec.addField(col2);
		rec.addField(col3);
		records.add(rec);

		return records;
	}
}