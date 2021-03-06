package com.sanxing.sesame.util;

import java.io.PrintWriter;

public class IndentPrinter
{
    private int indentLevel;

    private final String indent;

    private final PrintWriter out;

    public IndentPrinter()
    {
        this( new PrintWriter( System.out ), "  " );
    }

    public IndentPrinter( PrintWriter out )
    {
        this( out, "  " );
    }

    public IndentPrinter( PrintWriter out, String indent )
    {
        this.out = out;
        this.indent = indent;
    }

    public void println( Object value )
    {
        out.print( value.toString() );
        out.println();
    }

    public void println( String text )
    {
        out.print( text );
        out.println();
    }

    public void print( String text )
    {
        out.print( text );
    }

    public void printIndent()
    {
        for ( int i = 0; i < indentLevel; ++i )
        {
            out.print( indent );
        }
    }

    public void println()
    {
        out.println();
    }

    public void incrementIndent()
    {
        indentLevel += 1;
    }

    public void decrementIndent()
    {
        indentLevel -= 1;
    }

    public int getIndentLevel()
    {
        return indentLevel;
    }

    public void setIndentLevel( int indentLevel )
    {
        this.indentLevel = indentLevel;
    }

    public void flush()
    {
        out.flush();
    }
}