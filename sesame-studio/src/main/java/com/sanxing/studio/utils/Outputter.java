package com.sanxing.studio.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jdom.Element;
import org.jdom.xpath.XPath;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Outputter
{
    public static void datasetToExcel( Element tableMeta, ResultSet rs, File file )
        throws Exception
    {
        FileOutputStream fOut = new FileOutputStream( file );
        datasetToExcel( tableMeta, rs, fOut );
        fOut.flush();

        fOut.close();
    }

    public static void datasetToExcel( Element tableMeta, ResultSet rs, OutputStream out )
        throws Exception
    {
        HSSFWorkbook workbook = new HSSFWorkbook();

        HSSFSheet sheet = workbook.createSheet();

        JSONArray enumArray = new JSONArray();
        ResultSetMetaData meta = rs.getMetaData();

        HSSFRow headerRow = sheet.createRow( 0 );
        int i = 1;
        for ( int count = meta.getColumnCount(); i <= count; ++i )
        {
            HSSFCell cell = headerRow.createCell( (short) i );

            cell.setCellType( 1 );

            String label = meta.getColumnLabel( i );
            String colName = meta.getColumnName( i );
            Element column =
                ( tableMeta == null ) ? null : (Element) XPath.selectSingleNode( tableMeta,
                    "columns/column[lower-case(column_name)='" + colName.toLowerCase() + "']" );

            JSONObject enumObj = new JSONObject();
            enumArray.put( enumObj );
            if ( column != null )
            {
                label = column.getChildText( "remarks" );
                JSONTokener json = new JSONTokener( column.getChildText( "restriction", column.getNamespace() ) );
                Object token = ( json.more() ) ? json.nextValue() : null;
                if ( token instanceof JSONArray )
                {
                    JSONArray array = (JSONArray) token;
                    int j = 0;
                    for ( int len = array.length(); j < len; ++j )
                    {
                        JSONObject obj = array.getJSONObject( j );
                        enumObj.put( obj.getString( "value" ), obj.getString( "text" ) );
                    }
                }
            }
            if ( label == null )
            {
                label = colName;
            }
            cell.setCellValue( new HSSFRichTextString( label ) );
        }

        int rowIndex = 1;
        while ( rs.next() )
        {
            HSSFRow row = sheet.createRow( rowIndex++ );
            i = 1;
            for ( int count = meta.getColumnCount(); i <= count; ++i )
            {
                HSSFCell cell = row.createCell( (short) i );
                JSONObject enumObj = enumArray.getJSONObject( i - 1 );
                if ( enumObj.length() > 0 )
                {
                    String value = rs.getString( i );
                    if ( value != null )
                    {
                        value = value.trim();
                    }
                    if ( enumObj.has( value ) )
                    {
                        value = enumObj.getString( value );
                    }
                    cell.setCellType( 1 );
                    cell.setCellValue( new HSSFRichTextString( value ) );
                }
                else
                {
                    switch ( meta.getColumnType( i ) )
                    {
                        case -1:
                        case 1:
                        case 12:
                            cell.setCellType( 1 );
                            cell.setCellValue( new HSSFRichTextString( rs.getString( i ) ) );
                            break;
                        case -6:
                        case -5:
                        case 4:
                        case 5:
                            cell.setCellType( 0 );
                            cell.setCellValue( rs.getInt( i ) );
                            break;
                        case 91:
                        case 92:
                            cell.setCellType( 1 );
                            cell.setCellValue( new HSSFRichTextString( rs.getString( i ) ) );
                            break;
                        case 3:
                        case 6:
                        case 8:
                            cell.setCellValue( rs.getDouble( i ) );
                            cell.setCellType( 0 );
                    }
                }
            }
        }

        workbook.write( out );
    }
}