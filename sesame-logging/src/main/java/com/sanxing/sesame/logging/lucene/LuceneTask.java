package com.sanxing.sesame.logging.lucene;

import java.util.TimerTask;

public class LuceneTask extends TimerTask {
	public void run() {
		LuceneSearcher.getInstance().flushIndex();
	}
}