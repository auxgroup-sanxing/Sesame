package com.sanxing.sesame.engine;

import java.util.HashMap;
import java.util.Map;

import com.sanxing.sesame.constants.ExchangeConst;

public abstract class ExecutionEnv
{
    public static final String NAMING_CONTEXT = "NAMING_CONTEXT";

    public static final String NAMESPACE_CONTEXT = "process.namespaces";

    public static final String CLASSLOADER = "process.classloader";

    public static final String PROCESS_GROUP = "process.group";

    public static final String PROCESS_NAME = "process.name";

    public static final String SERIAL_NUMBER = "process.serial";

    public static final String ACTION = "process.ACTION";

    public static final String PROCESS_FAULTCODE = "process.faultcode";

    public static final String PROCESS_FAULTSTRING = "process.faultstring";

    public static final String BEANSHELL_CONTEXT = "beanshell.context";

    private static final Map<String, String> exports = new HashMap();

    static
    {
        exports.put( ExchangeConst.SERIAL, SERIAL_NUMBER );
        exports.put( ExchangeConst.FAULT_CODE, PROCESS_FAULTCODE );
        exports.put( ExchangeConst.FAULT_TEXT, PROCESS_FAULTSTRING );
    }

    public static final Map<String, String> export()
    {
        return exports;
    }
}