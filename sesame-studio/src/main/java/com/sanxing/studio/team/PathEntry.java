package com.sanxing.studio.team;

import java.util.Date;

public class PathEntry
{
    public static final String DIR = "dir";

    public static final String FILE = "file";

    public static final String NONE = "none";

    String name;

    String author;

    String relativePath;

    String kind;

    long revision;

    long size;

    Date date;

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor( String author )
    {
        this.author = author;
    }

    public String getRelativePath()
    {
        return relativePath;
    }

    public void setRelativePath( String relativePath )
    {
        this.relativePath = relativePath;
    }

    public String getKind()
    {
        return kind;
    }

    public void setKind( String kind )
    {
        this.kind = kind;
    }

    public long getRevision()
    {
        return revision;
    }

    public void setRevision( long revision )
    {
        this.revision = revision;
    }

    public long getSize()
    {
        return size;
    }

    public void setSize( long size )
    {
        this.size = size;
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate( Date date )
    {
        this.date = date;
    }
}