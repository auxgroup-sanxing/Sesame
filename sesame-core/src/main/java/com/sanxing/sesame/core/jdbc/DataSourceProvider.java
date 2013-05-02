package com.sanxing.sesame.core.jdbc;

import com.sanxing.sesame.core.BaseServer;

public abstract interface DataSourceProvider {
	public abstract void provide(BaseServer paramBaseServer,
			DataSourceInfo paramDataSourceInfo);

	public abstract void release();
}