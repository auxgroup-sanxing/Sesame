package com.sanxing.studio.search;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearcherUtil extends WSDLParser {
	private static SearcherUtil _instance;
	private Map<String, LuceneSearcher> indexMap = new HashMap();

	private final String[] indexNames = { "project", "service", "interface",
			"operation", "message", "element" };
	private String indexRootPath;
	static Logger logger = LoggerFactory.getLogger(SearcherUtil.class.getName());

	public static synchronized SearcherUtil getInstance(String indexRootPath) {
		if (_instance == null) {
			_instance = new SearcherUtil(indexRootPath);
		}
		return _instance;
	}

	public void buildIndexes(String projectRootPath) {
		File curPath = new File(projectRootPath);

		for (File file : curPath.listFiles()) {
			if (file.getName().equals("unit.wsdl") == true) {
				Record projectRecord = getProjectRecord(getProjJbiFile(file));

				Record stdRecord = getStdRecord(file);
				RecordCollector recCollector = getWSDLRecords(new File(
						file.getAbsolutePath()));

				recCollector.joinRecord(projectRecord, false);
				addRecord(recCollector, stdRecord);
			} else if (file.getName().equals("jbi.xml") == true) {
				Record projectRecord = getProjectRecord(file);
				if (projectRecord != null) {
					addRecord(projectRecord);
				}
			}
			if (file.isDirectory())
				buildIndexes(file.getAbsolutePath());
		}
	}

	public void closeIndexs() {
		for (String name : this.indexMap.keySet()) {
			LuceneSearcher searcher = (LuceneSearcher) this.indexMap.get(name);
			searcher.closeIndex();
		}
	}

	private Set<Record> search(String queryString, int maxHits) {
		Set records = new TreeSet();
		for (String name : this.indexMap.keySet()) {
			LuceneSearcher searcher = (LuceneSearcher) this.indexMap.get(name);
			Set recs = searcher.search(queryString, maxHits);
			if (recs != null) {
				records.addAll(recs);
			}
		}
		return records;
	}

	public Set<Record> search(String queryString, int maxHits, String[] filters) {
		Set records = new TreeSet();
		if ((filters != null) && (filters.length > 0)) {
			for (String type : filters) {
				LuceneSearcher searcher = (LuceneSearcher) this.indexMap
						.get(type);
				if (searcher != null) {
					Set recs = searcher.search(queryString, maxHits);
					if (recs != null)
						records.addAll(recs);
				}
			}
		} else {
			records = search(queryString, maxHits);
		}
		return records;
	}

	private SearcherUtil(String indexRootPath) {
		this.indexRootPath = indexRootPath;
	}

	public void init() {
		cleanup();
		for (String indexName : this.indexNames) {
			LuceneSearcher index = new LuceneSearcher(this.indexRootPath + "/"
					+ indexName);

			index.setName(indexName);
			index.createIndex();
			this.indexMap.put(indexName, index);
		}
	}

	public void cleanup() {
		Iterator it = this.indexMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry o = (Map.Entry) it.next();
			LuceneSearcher index = (LuceneSearcher) o.getValue();
			index.cleanup();
		}
		this.indexMap.clear();
	}

	private void addRecord(RecordCollector recCollector, Record stdRecord) {
		Map collectorMap = recCollector.getRecordMap();
		Iterator it = collectorMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry o = (Map.Entry) it.next();
			List<Record> obj = (List) o.getValue();
			for (Record record : obj) {
				record.join(stdRecord, true);
				addRecord(record);
			}
		}
	}

	private LuceneSearcher getSearcherByName(String name) {
		return ((LuceneSearcher) this.indexMap.get(name));
	}

	private void addRecord(Record record) {
		LuceneSearcher searcher = getSearcherByName(record.getSearcherName());
		searcher.addIndex(record);
	}
}