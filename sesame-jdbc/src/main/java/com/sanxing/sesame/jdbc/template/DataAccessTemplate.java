package com.sanxing.sesame.jdbc.template;

public abstract interface DataAccessTemplate
    extends IndexedQueryTemplate, IndexedUpdateTemplate, NamedQueryTemplate, NamedUpdateTemplate,
    CustomizedQueryTemplate, CustomizedUpdateTemplate
{
}