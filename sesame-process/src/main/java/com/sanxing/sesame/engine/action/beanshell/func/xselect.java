package com.sanxing.sesame.engine.action.beanshell.func;

import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.Element;
import org.jdom.Text;

import bsh.CallStack;
import bsh.EvalError;
import bsh.Interpreter;

import com.sanxing.sesame.engine.action.beanshell.BeanShellAction;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.Variable;

public class xselect
{
    public static Object invoke( Interpreter env, CallStack callstack, Element var, String strPath )
    {
        try
        {
            BeanShellAction action = (BeanShellAction) env.get( "this_action" );
            DataContext ctx = (DataContext) env.get( "ctx" );
            Variable result = action.select( var, strPath, ctx );
            if ( result.getVarType() == 1 )
            {
                return ( (Text) result.get() ).getTextTrim();
            }
            if ( result.getVarType() == 2 )
            {
                return ( (CDATA) result.get() ).getText();
            }
            if ( result.getVarType() == 8 )
            {
                return Integer.valueOf( ( (Number) result.get() ).intValue() );
            }
            if ( result.getVarType() == 3 )
            {
                return ( (Attribute) result.get() ).getValue();
            }
            return result.get();
        }
        catch ( EvalError e )
        {
            e.printStackTrace();
        }
        return null;
    }
}