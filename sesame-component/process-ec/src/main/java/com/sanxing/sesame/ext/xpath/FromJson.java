package com.sanxing.sesame.ext.xpath;

import java.util.List;

import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;

import com.sanxing.sesame.ext.xslt.StringUtil;

public class FromJson implements Function {

    /*(non-Javadoc)
     * @see org.jaxen.Function#call(org.jaxen.Context, java.util.List)
     */
    @Override
    public Object call( Context context, List args )
        throws FunctionCallException
    {
        if ( args.size() < 2 )
            return "";
        return StringUtil.fromJson( args.get( 0 ).toString(), args.get( 1 ).toString() );
    }

}
