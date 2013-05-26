package com.sanxing.sesame.binding.soap;

import javax.xml.soap.SOAPFault;

public class SoapFaultException
    extends Exception
{
    private static final long serialVersionUID = 7059696544964558264L;

    private SOAPFault fault;

    public SoapFaultException( SOAPFault soapFault )
    {
        this.fault = soapFault;
    }

    public SOAPFault getFault()
    {
        return this.fault;
    }
}