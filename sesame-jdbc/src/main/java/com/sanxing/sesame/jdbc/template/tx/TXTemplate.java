package com.sanxing.sesame.jdbc.template.tx;

public interface TXTemplate
{
    public abstract Object handle( DataAccessProcessor processor );
}