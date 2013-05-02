package com.sanxing.sesame.engine.context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DehydrateManager {
	public static String DEHYDRATE_CONTEXT_ID = "DEHYDRATE_CONTEXT_ID";

	private static Map<String, ExecutionContext> cache = new ConcurrentHashMap();

	private static Map<String, Long> instackTimestamps = new ConcurrentHashMap();

	private static Map<String, String> actionIds = new ConcurrentHashMap();

	private static long timeout = 30000L;

	public static void dehydrate(String uuid, String actionId,
			ExecutionContext ec) {
		actionIds.put(uuid, actionId);
		cache.put(ec.getUuid(), ec);
		instackTimestamps.put(ec.getUuid(),
				Long.valueOf(System.currentTimeMillis()));
	}

	public static ExecutionContext hydrate(String uuid) {
		instackTimestamps.remove(uuid);

		ExecutionContext ec = (ExecutionContext) cache.get(uuid);

		cache.remove(uuid);

		return ec;
	}

	public static ExecutionContext getDehydratedExecutionContext(String uuid) {
		return ((ExecutionContext) cache.get(uuid));
	}

	public static boolean isDehydrated(String uuid) {
		return ((cache.containsKey(uuid)) && (actionIds.containsKey(uuid)));
	}

	public static boolean isDehydratedAction(String uuid, String actionId) {
		return actionId.equals(actionIds.get(uuid));
	}

	public static List<ExecutionContext> getTimeoutExecutionContexts() {
		List result = new ArrayList();
		Iterator iter = instackTimestamps.keySet().iterator();
		long now = System.currentTimeMillis();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			long timestamp = ((Long) instackTimestamps.get(key)).longValue();
			if (now - timestamp > timeout) {
				result.add((ExecutionContext) cache.get(key));
			}
		}
		return result;
	}
}