package com.sanxing.studio.auth;

import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class LoginConfiguration
    extends Configuration
{
    private Document conf = null;

    public LoginConfiguration()
    {
        URL url = super.getClass().getClassLoader().getResource( "login-config.xml" );
        if ( url == null )
        {
            throw new RuntimeException( "Can't find <login-config.xml> in class-path" );
        }
        SAXReader reader = new SAXReader();
        try
        {
            conf = reader.read( url );
        }
        catch ( DocumentException e )
        {
            conf = DocumentHelper.createDocument();
            conf.add( DocumentHelper.createElement( "config" ) );
            throw new RuntimeException( e.getMessage(), e );
        }
    }

    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry( String name )
    {
        Element el = conf.getRootElement().element( name );
        if ( el != null )
        {
            List list = new ArrayList();
            List modules = el.elements( "module" );
            for ( Iterator iter = modules.iterator(); iter.hasNext(); )
            {
                Element moduleEl = (Element) iter.next();
                String moduleName = moduleEl.attributeValue( "class" );
                String sflag = moduleEl.attributeValue( "flag" );
                AppConfigurationEntry.LoginModuleControlFlag controlFlag = null;
                if ( sflag.equalsIgnoreCase( "REQUIRED" ) )
                {
                    controlFlag = AppConfigurationEntry.LoginModuleControlFlag.REQUIRED;
                }
                else if ( sflag.equalsIgnoreCase( "REQUISITE" ) )
                {
                    controlFlag = AppConfigurationEntry.LoginModuleControlFlag.REQUISITE;
                }
                else if ( sflag.equalsIgnoreCase( "SUFFICIENT" ) )
                {
                    controlFlag = AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT;
                }
                else if ( sflag.equalsIgnoreCase( "OPTIONAL" ) )
                {
                    controlFlag = AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL;
                }
                else
                {
                    MessageFormat form = new MessageFormat( "Configuration Error:\n\tInvalid control flag: {0}" );
                    Object[] source = { sflag };
                    throw new RuntimeException( form.format( source ) );
                }
                Map options = new HashMap();
                List<Attribute> attributes = moduleEl.attributes();
                for ( Attribute attr : attributes )
                {
                    options.put( attr.getName(), attr.getValue() );
                }
                AppConfigurationEntry entry = new AppConfigurationEntry( moduleName, controlFlag, options );
                list.add( entry );
            }
            return ( (AppConfigurationEntry[]) list.toArray( new AppConfigurationEntry[list.size()] ) );
        }

        return null;
    }

    @Override
    public void refresh()
    {
    }
}