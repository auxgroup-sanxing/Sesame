package com.sanxing.sesame.jaxp;

import javax.xml.transform.TransformerFactory;

public class XSLTUtil
{
    private static final TransformerFactory transformerFactory = TransformerFactory.newInstance();

    public static TransformerFactory getTransformerfactory()
    {
        return transformerFactory;
    }
}