package com.sanxing.sesame.component.params;

import org.jdom.Element;

public class Parameter
{
    public static int PARAM_TYPE_INT = 0;

    public static int PARAM_TYPE_BOOLEAN = 1;

    public static int PARAM_TYPE_STRING = 2;

    public static int PARAM_TYPE_DOUBLE = 3;

    private String name;

    private String value;

    private PARAMTYPE type = PARAMTYPE.PARAM_TYPE_STRING;

    private String comment;

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getValue()
    {
        return value;
    }

    public Object getTypedValue()
    {
        try
        {
            if ( type.equals( PARAMTYPE.PARAM_TYPE_INT ) )
            {
                return Integer.valueOf( Integer.parseInt( value ) );
            }
            if ( type.equals( PARAMTYPE.PARAM_TYPE_BOOLEAN ) )
            {
                return Boolean.valueOf( Boolean.parseBoolean( value ) );
            }
            if ( type.equals( PARAMTYPE.PARAM_TYPE_DOUBLE ) )
            {
                return Double.valueOf( Double.parseDouble( value ) );
            }
            return value;
        }
        catch ( NumberFormatException e )
        {
            e.printStackTrace();
            throw e;
        }
    }

    public void setValue( String value )
    {
        try
        {
            if ( type.equals( PARAMTYPE.PARAM_TYPE_INT ) )
            {
                Integer.parseInt( value );
            }
            if ( type.equals( PARAMTYPE.PARAM_TYPE_BOOLEAN ) )
            {
                Boolean.parseBoolean( value );
            }
            if ( type.equals( PARAMTYPE.PARAM_TYPE_DOUBLE ) )
            {
                Double.parseDouble( value );
            }
        }
        catch ( NumberFormatException e )
        {
            e.printStackTrace();
            throw e;
        }

        this.value = value;
    }

    public PARAMTYPE getType()
    {
        return type;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment( String comment )
    {
        this.comment = comment;
    }

    public static Parameter newIntParameter()
    {
        Parameter param = new Parameter();
        param.type = PARAMTYPE.PARAM_TYPE_INT;
        return param;
    }

    public static Parameter newBooleanParameter()
    {
        Parameter param = new Parameter();
        param.type = PARAMTYPE.PARAM_TYPE_BOOLEAN;
        return param;
    }

    public static Parameter newStringParameter()
    {
        Parameter param = new Parameter();
        param.type = PARAMTYPE.PARAM_TYPE_STRING;
        return param;
    }

    public static Parameter newDoubleParameter()
    {
        Parameter param = new Parameter();
        param.type = PARAMTYPE.PARAM_TYPE_DOUBLE;
        return param;
    }

    @Override
    public String toString()
    {
        return "Parameter [name=" + name + ", value=" + value + ", type=" + type + ", comment=" + comment + "]";
    }

    public Element toElement()
    {
        Element element = new Element( "param" );
        element.setAttribute( "name", name );
        element.setAttribute( "type", type.name() );
        element.setAttribute( "comment", comment );
        element.setText( value );
        return element;
    }

    public static Parameter newParameter( PARAMTYPE type )
    {
        Parameter param = null;
        if ( type.equals( PARAMTYPE.PARAM_TYPE_INT ) )
        {
            param = newIntParameter();
        }
        else if ( type.equals( PARAMTYPE.PARAM_TYPE_DOUBLE ) )
        {
            param = newDoubleParameter();
        }
        else if ( type.equals( PARAMTYPE.PARAM_TYPE_BOOLEAN ) )
        {
            param = newBooleanParameter();
        }
        else if ( type.equals( PARAMTYPE.PARAM_TYPE_STRING ) )
        {
            param = newStringParameter();
        }

        return param;
    }

    public static Parameter fromElement( Element element )
    {
        Parameter param = null;
        if ( element.getAttributeValue( "type" ).equals( PARAMTYPE.PARAM_TYPE_INT.name() ) )
        {
            param = newIntParameter();
        }
        else if ( element.getAttributeValue( "type" ).equals( PARAMTYPE.PARAM_TYPE_DOUBLE.name() ) )
        {
            param = newDoubleParameter();
        }
        else if ( element.getAttributeValue( "type" ).equals( PARAMTYPE.PARAM_TYPE_BOOLEAN.name() ) )
        {
            param = newBooleanParameter();
        }
        else if ( element.getAttributeValue( "type" ).equals( PARAMTYPE.PARAM_TYPE_STRING.name() ) )
        {
            param = newStringParameter();
        }
        else
        {
            throw new RuntimeException( "unkown param type" );
        }
        param.setComment( element.getAttributeValue( "comment" ) );
        param.setName( element.getAttributeValue( "name" ) );
        param.setValue( element.getText() );
        return param;
    }

    public static enum PARAMTYPE
    {
        PARAM_TYPE_INT, PARAM_TYPE_BOOLEAN, PARAM_TYPE_STRING, PARAM_TYPE_DOUBLE;
    }
}