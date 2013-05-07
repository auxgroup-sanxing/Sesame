package com.sanxing.sesame.codec.impl;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import javax.xml.transform.Source;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.transform.JDOMSource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sanxing.sesame.binding.codec.BinarySource;
import com.sanxing.sesame.binding.codec.Decoder;
import com.sanxing.sesame.binding.codec.FormatException;
import com.sanxing.sesame.binding.codec.XMLResult;

public class JSONDecoder
    implements Decoder
{
    @Override
    public void init( String workspaceRoot )
    {
    }

    @Override
    public void decode( BinarySource source, XMLResult result )
        throws FormatException
    {
        try
        {
            String data = new String( source.getBytes(), source.getEncoding() );
            JSONObject json = new JSONObject( data );
            Element rootEl = new Element( "stream" );

            iterate( json, rootEl );

            Source content = new JDOMSource( new Document( rootEl ) );

            result.setContent( content );
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new FormatException( e.getMessage(), e );
        }
        catch ( JSONException e )
        {
            throw new FormatException( e.getMessage(), e );
        }
    }

    private void iterate( JSONObject json, Element element )
        throws JSONException
    {
        for ( Iterator iter = json.keys(); iter.hasNext(); )
        {
            String key = (String) iter.next();
            Object value = json.get( key );
            if ( value instanceof JSONObject )
            {
                JSONObject object = (JSONObject) value;
                Element child = new Element( key );
                iterate( object, child );
                element.addContent( child );
            }
            else if ( value instanceof JSONArray )
            {
                JSONArray array = (JSONArray) value;
                iterate( array, element, key );
            }
            else if ( value != null )
            {
                Element child = new Element( key );
                child.setText( String.valueOf( value ) );
                element.addContent( child );
            }
        }
    }

    private void iterate( JSONArray array, Element parent, String itemName )
        throws JSONException
    {
        int i = 0;
        for ( int len = array.length(); i < len; ++i )
        {
            Object item = array.get( i );
            if ( item instanceof JSONObject )
            {
                JSONObject object = (JSONObject) item;
                Element child = new Element( itemName );
                iterate( object, child );
                parent.addContent( child );
            }
            else if ( item instanceof JSONArray )
            {
                JSONArray items = (JSONArray) item;
                Element child = new Element( itemName );
                iterate( items, child, "item" );
                parent.addContent( child );
            }
            else if ( item != null )
            {
                Element child = new Element( itemName );
                child.setText( String.valueOf( item ) );
                parent.addContent( child );
            }
        }
    }

    @Override
    public void destroy()
    {
    }
}