package com.sanxing.sesame.jdbc.template;

public interface DataAccessTemplate
    extends IndexedQueryTemplate, IndexedUpdateTemplate, NamedQueryTemplate, NamedUpdateTemplate,
    CustomizedQueryTemplate, CustomizedUpdateTemplate
{
}