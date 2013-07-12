package com.sanxing.sesame.codec.impl;

import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sanxing.sesame.binding.codec.BinaryResult;
import com.sanxing.sesame.binding.codec.Encoder;
import com.sanxing.sesame.binding.codec.FormatException;
import com.sanxing.sesame.binding.codec.XMLSource;

public class JSONEncoder
    implements Encoder
{
    @Override
    public void init( String workspaceRoot )
    {
    }

    @Override
    public void encode( XMLSource source, BinaryResult result )
        throws FormatException
    {
        try
        {
            Document document = source.getJDOMDocument();
            Element rootEl = document.getRootElement();
            
            if ( rootEl.getText() != null && rootEl.getText().trim().length() > 0)
            {
                result.write( rootEl.getText().getBytes( result.getEncoding() ) );
            }
            else
            {
                JSONObject object = new JSONObject();

                iterate( rootEl, object );

                result.write( object.toString().getBytes( result.getEncoding() ) );
            }
        }
        catch ( Exception e )
        {
            throw new FormatException( e.getMessage(), e );
        }
    }

    private void iterate( Element element, JSONObject object )
        throws JSONException
    {
        List<Attribute> attributes = element.getAttributes();
        for ( Attribute attribute : attributes )
        {
            object.put( attribute.getName(), convert( attribute.getValue() ) );
        }

        List<Element> children = element.getChildren();
        for ( Element child : children )
        {
            if ( ( child.getAttributes().isEmpty() ) && ( child.getChildren().isEmpty() ) )
            {
                if ( child.getText() != null && child.getText().startsWith( "[" ) && child.getText().endsWith( "]" ) )
                {
                    JSONArray array = new JSONArray( child.getText() );
                    object.put( child.getName(), array );
                }
                else if ( child.getText() != null && child.getText().startsWith( "{" ) && child.getText().endsWith( "}" ) )
                {
                    JSONObject json = new JSONObject( child.getText() );
                    object.put( child.getName(), json );
                }
                else
                {
                    object.put( child.getName(), convert ( child.getText() ) );
                }
            }
            else
            {
                JSONObject json = new JSONObject();
                object.append( child.getName(), json );
                iterate( child, json );
            }
        }
    }

    private Object convert( String value )
    {
        if ( value == null )
        {
            return null;
        }
        if ( value.length() == 0 )
        {
            return "";
        }
        if ( "true".equalsIgnoreCase( value ) || "false".equalsIgnoreCase( value ) )
        {
            return Boolean.parseBoolean( value );
        }
        if ( value.charAt( 0 ) != '0' && NumberUtils.isNumber( value ))
        {
            try
            {
                return Long.parseLong( value );
            }
            catch ( NumberFormatException e )
            {
                try
                {
                    return Double.parseDouble( value );
                }
                catch ( NumberFormatException ex )
                {
                }
            }
        }
        return value;
    }

    @Override
    public void destroy()
    {
    }
}