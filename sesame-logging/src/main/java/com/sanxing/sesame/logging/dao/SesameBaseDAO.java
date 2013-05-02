package com.sanxing.sesame.logging.dao;

import com.sanxing.sesame.jdbc.data.PageInfo;
import java.util.List;

public abstract interface SesameBaseDAO {
	public abstract boolean updateOnDuplicate(BaseBean paramBaseBean);

	public abstract boolean updateStateOnDuplicate(BaseBean paramBaseBean);

	public abstract int updateState(BaseBean paramBaseBean);

	public abstract void insert(BaseBean paramBaseBean);

	public abstract int update(BaseBean paramBaseBean);

	public abstract LogBean queryForRecord(BaseBean paramBaseBean);

	public abstract List<?> queryForRecordSet(BaseBean paramBaseBean,
			PageInfo paramPageInfo);

	public abstract long queryCount(BaseBean paramBaseBean);
}