package com.sanxing.studio.search;

import java.util.Set;

public interface StringSearcher
{
    public abstract void addIndex( Record record );

    public abstract void closeIndex();

    public abstract Set<Record> search( String queryString, int maxHits );
}