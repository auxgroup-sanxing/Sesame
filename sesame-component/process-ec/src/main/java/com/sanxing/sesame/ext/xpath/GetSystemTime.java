package com.sanxing.sesame.ext.xpath;

import java.util.List;

import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;

import com.sanxing.sesame.ext.xslt.Environment;

public class GetSystemTime implements Function {

    /*(non-Javadoc)
     * @see org.jaxen.Function#call(org.jaxen.Context, java.util.List)
     */
    @Override
    public Object call( Context arg0, List arg1 )
        throws FunctionCallException
    {
        return Environment.getSystemTime();
    }

}
