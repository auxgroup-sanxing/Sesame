package com.sanxing.sesame.wtc.output;

public class SOPFormat
{
    private int columns;

    private String seprator;

    private String linePrompt;

    private String header;

    private String charset;

    public int getColumns()
    {
        return this.columns;
    }

    public void setColumns( int columns )
    {
        this.columns = columns;
    }

    public String getHeader()
    {
        return this.header;
    }

    public void setHeader( String header )
    {
        this.header = header;
    }

    public String getSeprator()
    {
        return this.seprator;
    }

    public void setSeprator( String seprator )
    {
        this.seprator = seprator;
    }

    public String getLinePrompt()
    {
        return this.linePrompt;
    }

    public void setLinePrompt( String linePrompt )
    {
        this.linePrompt = linePrompt;
    }

    public String getCharset()
    {
        return this.charset;
    }

    public void setCharset( String charset )
    {
        this.charset = charset;
    }

    public SOPFormat()
    {
        setColumns( 16 );
        setSeprator( " " );
        setLinePrompt( "%4s: " );
        setCharset( "GBK" );
    }
}
