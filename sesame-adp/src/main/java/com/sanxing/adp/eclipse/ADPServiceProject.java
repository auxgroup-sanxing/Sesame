package com.sanxing.adp.eclipse;

import java.io.File;
import java.io.FileOutputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.adp.parser.CodeGenerator;
import com.sanxing.adp.parser.WSDLParser;
import com.sanxing.sesame.util.FileUtil;

public class ADPServiceProject
{
    private final String projectName;

    private final String serviceUnitPath;

    private static Logger LOG = LoggerFactory.getLogger( ADPServiceProject.class );

    public ADPServiceProject( String projectName, String serviceUnitPath )
        throws Exception
    {
        this.projectName = projectName;
        this.serviceUnitPath = serviceUnitPath;

        File theFile = new File( serviceUnitPath + "/../../../../warehouse/components/adp-ec/ADP_TEMPLATE.zip" );
        File targetDir = new File( serviceUnitPath + "/" + projectName );
        FileUtil.unpackArchive( theFile, targetDir );

        String projectFileName = targetDir + "/.project";
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build( new File( projectFileName ) );
        Element root = doc.getRootElement();
        root.getChild( "name" ).setText( projectName );
        persistence( projectFileName, doc );

        projectFileName = targetDir + "/pom.xml";
        doc = builder.build( new File( projectFileName ) );
        root = doc.getRootElement();
        root.getChild( "artifactId", root.getNamespace() ).setText( projectName );
        persistence( projectFileName, doc );

        generateCode();
    }

    public void generateCode()
        throws Exception
    {
        String srcDir = serviceUnitPath + "/" + projectName + "/src/main/java";
        String wsdlFile = serviceUnitPath + "/unit.wsdl";
        String[] regexs = { ".*?Impl\\.java", "\\.svn" };

        File file = new File( wsdlFile );
        File srcFile = new File( srcDir );
        FileUtil.deleteFileIfNotMatch( srcFile, regexs );
        WSDLParser parser = new WSDLParser();
        CodeGenerator gen = new CodeGenerator();
        gen.setTargetFolder( srcDir );
        parser.setGenerator( gen );
        parser.parse4CodeGen( file.getAbsolutePath() );
    }

    private void persistence( String projectFileName, Document doc )
        throws Exception
    {
        FileOutputStream fout = null;
        try
        {
            XMLOutputter output = new XMLOutputter();
            fout = new FileOutputStream( new File( projectFileName ) );
            output.setFormat( Format.getPrettyFormat() );
            output.output( doc, fout );
            fout.flush();
        }
        catch ( Exception e )
        {
            LOG.debug( e.getMessage(), e );
            throw e;
        }
        finally
        {
            if ( fout != null )
            {
                try
                {
                    fout.close();
                }
                catch ( Exception e )
                {
                    LOG.debug( e.getMessage(), e );
                    throw e;
                }
            }
        }
    }
}