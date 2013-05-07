package com.sanxing.adp.codegen;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import com.sanxing.adp.parser.WSDLParser;

public class Main
{

    public static void main( String[] args )
    {
        try
        {
            if ( args.length == 0 )
            {
                pringUsage();
            }
            if ( args.length == 1 )
            {
                if ( args[0].equals( "-i" ) )
                {
                    System.out.println( "please input wsdl file absolute path (incluede file name)" );
                    BufferedReader reader = new BufferedReader( new InputStreamReader( System.in ) );
                    String wsdlFileLocation = reader.readLine();
                    File wsdlFile = new File( wsdlFileLocation );
                    if ( !( wsdlFile.exists() ) )
                    {
                        throw new RuntimeException( "file not exsit" );
                    }
                    System.out.println( "please input target dir ,default is current dir" );
                    String targetDir = reader.readLine();
                    if ( targetDir.equals( "" ) )
                    {
                        targetDir = ".";
                    }

                    File targetDirFile = new File( targetDir );
                    if ( !( targetDirFile.exists() ) )
                    {
                        targetDirFile.mkdirs();
                    }
                    System.out.println( "please input target packageName " );
                    String packageName = reader.readLine();

                    WSDLParser parser = new WSDLParser();
                    ClientProxyGenerator generator = new ClientProxyGenerator();
                    generator.setTargetFolder( targetDir );
                    generator.setTargetPackageName( packageName );
                    parser.setGenerator( generator );
                    parser.parse4CodeGen( new File( wsdlFileLocation ).toURL().toString() );
                }
                else
                {
                    pringUsage();
                }

            }

        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    private static void pringUsage()
    {
        StringBuffer usage = new StringBuffer();
        usage = usage.append( "ClientInterfaceGen -i #interactive" );
        System.out.println( usage );
    }
}