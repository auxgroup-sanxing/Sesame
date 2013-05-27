package com.sanxing.sesame.engine.action.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sanxing.sesame.engine.action.Constant;

public class RS2DOM
{
    private static final String xsdns = "http://www.w3.org/2001/XMLSchema";

    public static Document ResultSet2XSDDOM( ResultSet rs )
        throws SQLException
    {
        Document mySchema = null;
        try
        {
            mySchema = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        }
        catch ( ParserConfigurationException pce )
        {
            pce.printStackTrace();
        }
        Element root = mySchema.createElementNS( "http://www.w3.org/2001/XMLSchema", "xsd:schema" );
        root.setAttribute( "elementFormDefault", "qualified" );
        root.setAttribute( "attributeFormDefault", "unqualified" );
        mySchema.appendChild( root );

        Element result = mySchema.createElementNS( "http://www.w3.org/2001/XMLSchema", "xsd:element" );
        result.setAttribute( Constant.ATTR_NAME, "result" );
        root.appendChild( result );

        Element ct = mySchema.createElementNS( "http://www.w3.org/2001/XMLSchema", "xsd:complexType" );
        result.appendChild( ct );

        Element seq = mySchema.createElementNS( "http://www.w3.org/2001/XMLSchema", "xsd:sequence" );
        ct.appendChild( seq );

        Element row = mySchema.createElementNS( "http://www.w3.org/2001/XMLSchema", "xsd:element" );
        row.setAttribute( Constant.ATTR_NAME, "row" );
        row.setAttribute( "maxOccurs", "unbounded" );
        seq.appendChild( row );

        ct = mySchema.createElementNS( "http://www.w3.org/2001/XMLSchema", "xsd:complexType" );
        row.appendChild( ct );

        seq = mySchema.createElementNS( "http://www.w3.org/2001/XMLSchema", "xsd:sequence" );
        ct.appendChild( seq );

        root = null;
        ct = null;

        ResultSetMetaData rsmd = rs.getMetaData();
        for ( int i = 1; i <= rsmd.getColumnCount(); ++i )
        {
            Element element = mySchema.createElementNS( "http://www.w3.org/2001/XMLSchema", "xsd:element" );
            if ( rsmd.isNullable( i ) == 1 )
            {
                element.setAttribute( "nillable", "true" );
            }
            element.setAttribute( Constant.ATTR_NAME, rsmd.getColumnLabel( i ) );
            switch ( rsmd.getColumnType( i ) )
            {
                case -5:
                    element.setAttribute( Constant.ATTR_TYPE, "xsd:integer" );
                    break;
                case -7:
                    element.setAttribute( Constant.ATTR_TYPE, "xsd:string" );
                    break;
                case 16:
                    element.setAttribute( Constant.ATTR_TYPE, "xsd:boolean" );
                    break;
                case 1:
                    element.setAttribute( Constant.ATTR_TYPE, "xsd:string" );
                    break;
                case 91:
                    element.setAttribute( Constant.ATTR_TYPE, "xsd:date" );
                    break;
                case 3:
                    element.setAttribute( Constant.ATTR_TYPE, "xsd:integer" );
                    break;
                case 8:
                    element.setAttribute( Constant.ATTR_TYPE, "xsd:double" );
                    break;
                case 6:
                    element.setAttribute( Constant.ATTR_TYPE, "xsd:float" );
                    break;
                case 4:
                    element.setAttribute( Constant.ATTR_TYPE, "xsd:integer" );
                    break;
                case -1:
                    element.setAttribute( Constant.ATTR_TYPE, "xsd:string" );
                    break;
                case 2:
                    element.setAttribute( Constant.ATTR_TYPE, "xsd:decimal" );
                    break;
                case 7:
                    element.setAttribute( Constant.ATTR_TYPE, "xsd:double" );
                    break;
                case 5:
                    element.setAttribute( Constant.ATTR_TYPE, "xsd:integer" );
                    break;
                case 92:
                    element.setAttribute( Constant.ATTR_TYPE, "xsd:time" );
                    break;
                case 93:
                    element.setAttribute( Constant.ATTR_TYPE, "xsd:dateTime" );
                    break;
                case -6:
                    element.setAttribute( Constant.ATTR_TYPE, "xsd:byte" );
                    break;
                case 12:
                    element.setAttribute( Constant.ATTR_TYPE, "xsd:string" );
                    break;
                default:
                    element.setAttribute( Constant.ATTR_TYPE, "xsd:string" );
            }

            seq.appendChild( element );
        }
        return mySchema;
    }

    public static Document ResultSet2DOM( ResultSet rs )
        throws SQLException
    {
        Document myDocument = null;
        try
        {
            myDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        }
        catch ( ParserConfigurationException pce )
        {
            pce.printStackTrace();
        }
        Element root = myDocument.createElement( "result" );

        myDocument.appendChild( root );

        ResultSetMetaData rsmd = rs.getMetaData();

        while ( ( !( rs.isLast() ) ) && ( rs.next() ) )
        {
            Element row = myDocument.createElement( "row" );
            root.appendChild( row );
            for ( int i = 1; i <= rsmd.getColumnCount(); ++i )
            {
                Element element = myDocument.createElement( rsmd.getColumnLabel( i ) );

                String value = rs.getString( i );
                if ( value == null )
                {
                    element.setAttribute( "xsi:nil", "true" );
                }
                else
                {
                    element.appendChild( myDocument.createTextNode( rs.getString( i ) ) );
                }
                row.appendChild( element );
            }
        }
        return myDocument;
    }
}