package com.sanxing.sesame.jmx;

import java.io.Serializable;
import java.util.Arrays;

public class FilePackage
    implements Serializable
{
    private static final long serialVersionUID = -2442691602156220380L;

    private final String fileName;

    private long currentPackage;

    private long pageSize;

    private byte[] packageData;

    public boolean isEnd()
    {
        return ( ( packageData == null ) || ( packageData.length < pageSize ) );
    }

    @Override
    public String toString()
    {
        return "FilePackage [currentPackage=" + currentPackage + ", fileName=" + fileName + ", packageData="
            + Arrays.toString( packageData ) + "]";
    }

    public FilePackage( String fileName )
    {
        this.fileName = fileName;
    }

    public String getFileName()
    {
        return fileName;
    }

    public long getCurrentPackage()
    {
        return currentPackage;
    }

    public void setCurrentPackage( long currentPackage )
    {
        this.currentPackage = currentPackage;
    }

    public long getPageSize()
    {
        return pageSize;
    }

    public void setPageSize( long pageSize )
    {
        this.pageSize = pageSize;
    }

    public byte[] getPackageData()
    {
        return packageData;
    }

    public void setPackageData( byte[] packageData )
    {
        this.packageData = packageData;
    }
}