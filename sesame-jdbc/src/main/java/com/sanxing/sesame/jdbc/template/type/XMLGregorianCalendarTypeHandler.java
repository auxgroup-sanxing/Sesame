package com.sanxing.sesame.jdbc.template.type;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class XMLGregorianCalendarTypeHandler
    implements TypeHandler
{
    private XMLGregorianCalendar fromDate( java.util.Date date )
    {

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime( date );
        XMLGregorianCalendar gc = null;
        try
        {
            gc = DatatypeFactory.newInstance().newXMLGregorianCalendar( cal );
        }
        catch ( DatatypeConfigurationException e )
        {
            throw new RuntimeException( "There are some error for convert java.util.Date to XMLGregorianCalendar", e );
        }
        return gc;
    }

    private java.util.Date toDate( XMLGregorianCalendar gc )
    {
        return gc.toGregorianCalendar().getTime();
    }

    @Override
    public Object getField( ResultSet rs, int index )
        throws SQLException
    {
        Date value = rs.getDate( index );
        if ( rs.wasNull() )
        {
            return null;
        }
        return fromDate( value );
    }

    @Override
    public void setParameter( PreparedStatement ps, int index, Object value )
        throws SQLException
    {
        XMLGregorianCalendar v = (XMLGregorianCalendar) value;
        ps.setDate( index, new Date( toDate( v ).getTime() ) );
    }
}