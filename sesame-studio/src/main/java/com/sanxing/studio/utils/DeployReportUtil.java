package com.sanxing.studio.utils;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.Chapter;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Section;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sanxing.sesame.core.keymanager.KeyStoreInfo;
import com.sanxing.sesame.core.keymanager.ServiceKeyProvider;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.JDomDriver;

public class DeployReportUtil
{
    private static final Logger LOG = LoggerFactory.getLogger( DeployReportUtil.class );

    private PdfWriter writer = null;

    private final com.lowagie.text.Document document = new com.lowagie.text.Document();

    private final String outputPath;

    private Font sectionFont;

    private Font chapterFont;

    private Font tableFont;

    private Font textFont;

    public DeployReportUtil( String outputPath )
        throws DocumentException, IOException
    {
        this.outputPath = outputPath;
        writer = PdfWriter.getInstance( document, new FileOutputStream( outputPath ) );

        document.open();
        initFont();
    }

    public void buildReport()
    {
        try
        {
            Chapter chapter = newChapter( "环境参数配置" );
            addAddressBookSection( chapter );
            addKeyStoreSection( chapter );
            addKeyProviderSection( chapter );
            addDatabaseSection( chapter );
            addJMSSection( chapter );
            addChapter( chapter );
            close();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    private void initFont()
        throws DocumentException, IOException
    {
        BaseFont bfChinese = BaseFont.createFont( "STSongStd-Light", "UniGB-UCS2-H", false );

        sectionFont = new Font( bfChinese, 16.0F, 1, Color.black );
        chapterFont = new Font( bfChinese, 18.0F, 3, Color.BLUE );
        tableFont = new Font( bfChinese, 12.0F, 0, Color.black );
        textFont = new Font( bfChinese, 12.0F, 0, Color.black );
    }

    private Chapter newChapter( String title )
    {
        Paragraph pg = new Paragraph( title, chapterFont );
        Chapter chapter = new Chapter( pg, 1 );
        chapter.setNumberDepth( 0 );
        return chapter;
    }

    private void addChapter( Chapter chapter )
        throws DocumentException
    {
        document.add( chapter );
    }

    private void close()
    {
        document.close();
        writer.close();
    }

    private void addAddressBookSection( Chapter chapter )
        throws JDOMException, IOException
    {
        Paragraph title11 = new Paragraph( "地址簿", sectionFont );
        Section section1 = chapter.addSection( title11 );
        Paragraph someSectionText = new Paragraph( "以下是地址簿列表，请按照需要配置.", textFont );
        section1.add( someSectionText );
        someSectionText = new Paragraph( "  ", textFont );
        section1.add( someSectionText );

        File file = new File( System.getProperty( "SESAME_HOME" ), "conf/address-book.xml" );

        if ( !( file.exists() ) )
        {
            return;
        }
        LOG.debug( "Loading addresses from file: " + file );
        SAXBuilder builder = new SAXBuilder();
        org.jdom.Document doc = builder.build( file );
        Element root = doc.getRootElement();
        Iterator itr = root.getChildren( "location" ).iterator();

        PdfPTable table = new PdfPTable( 2 );
        PdfPCell cell = null;
        while ( itr.hasNext() )
        {
            Element locationEl = (Element) itr.next();
            cell = new PdfPCell( new Paragraph( "name", tableFont ) );
            cell.setBackgroundColor( new Color( 192, 192, 192 ) );
            table.addCell( cell );
            cell = new PdfPCell( new Paragraph( locationEl.getAttributeValue( "name" ), tableFont ) );

            cell.setBackgroundColor( new Color( 192, 192, 192 ) );
            table.addCell( cell );

            cell = new PdfPCell( new Paragraph( "url", tableFont ) );
            table.addCell( cell );
            cell = new PdfPCell( new Paragraph( locationEl.getAttributeValue( "url" ), tableFont ) );

            table.addCell( cell );

            cell = new PdfPCell( new Paragraph( "style", tableFont ) );
            table.addCell( cell );
            cell = new PdfPCell( new Paragraph( locationEl.getAttributeValue( "style" ), tableFont ) );

            table.addCell( cell );
        }

        section1.add( table );
    }

    private void addKeyStoreSection( Chapter chapter )
        throws FileNotFoundException
    {
        Paragraph title11 = new Paragraph( "keystore", sectionFont );
        Section section1 = chapter.addSection( title11 );
        Paragraph someSectionText = new Paragraph( "以下是keystore列表，请按照需要配置.", textFont );

        section1.add( someSectionText );
        someSectionText = new Paragraph( "  ", textFont );
        section1.add( someSectionText );

        Map stores = null;

        File storeFiles = new File( System.getProperty( "SESAME_HOME" ), "security/keystore.config" );

        if ( !( storeFiles.exists() ) )
        {
            return;
        }
        XStream xstream = new XStream( new JDomDriver() );
        stores = (Map) xstream.fromXML( new FileInputStream( storeFiles ) );

        Iterator itr = stores.entrySet().iterator();
        PdfPTable table = new PdfPTable( 2 );
        PdfPCell cell = null;
        while ( itr.hasNext() )
        {
            Map.Entry entry = (Map.Entry) itr.next();

            KeyStoreInfo info = (KeyStoreInfo) entry.getValue();

            cell = new PdfPCell( new Paragraph( "keystore名称", tableFont ) );
            cell.setBackgroundColor( new Color( 192, 192, 192 ) );
            table.addCell( cell );
            cell = new PdfPCell( new Paragraph( info.getName(), tableFont ) );
            cell.setBackgroundColor( new Color( 192, 192, 192 ) );
            table.addCell( cell );

            cell = new PdfPCell( new Paragraph( "keystore路径", tableFont ) );
            table.addCell( cell );
            cell = new PdfPCell( new Paragraph( info.getKeystorePath(), tableFont ) );

            table.addCell( cell );

            cell = new PdfPCell( new Paragraph( "描述", tableFont ) );
            table.addCell( cell );
            cell = new PdfPCell( new Paragraph( info.getDescription(), tableFont ) );
            table.addCell( cell );
        }

        section1.add( table );
    }

    private void addKeyProviderSection( Chapter chapter )
        throws FileNotFoundException
    {
        Paragraph title11 = new Paragraph( "keyprovier", sectionFont );
        Section section1 = chapter.addSection( title11 );
        Paragraph someSectionText = new Paragraph( "以下是keyprovier列表，请按照需要配置.", textFont );

        section1.add( someSectionText );
        someSectionText = new Paragraph( "  ", textFont );
        section1.add( someSectionText );

        Map stores = null;

        File storeFiles = new File( System.getProperty( "SESAME_HOME" ), "security/skps.config" );

        if ( !( storeFiles.exists() ) )
        {
            return;
        }
        XStream xstream = new XStream( new JDomDriver() );
        stores = (Map) xstream.fromXML( new FileInputStream( storeFiles ) );

        Iterator itr = stores.entrySet().iterator();
        PdfPTable table = new PdfPTable( 2 );
        PdfPCell cell = null;
        while ( itr.hasNext() )
        {
            Map.Entry entry = (Map.Entry) itr.next();

            ServiceKeyProvider info = (ServiceKeyProvider) entry.getValue();

            cell = new PdfPCell( new Paragraph( "keyprovider名称", tableFont ) );
            cell.setBackgroundColor( new Color( 192, 192, 192 ) );
            table.addCell( cell );
            cell = new PdfPCell( new Paragraph( info.getName(), tableFont ) );
            cell.setBackgroundColor( new Color( 192, 192, 192 ) );
            table.addCell( cell );

            cell = new PdfPCell( new Paragraph( "别名", tableFont ) );
            table.addCell( cell );
            cell = new PdfPCell( new Paragraph( info.getAlias(), tableFont ) );
            table.addCell( cell );

            cell = new PdfPCell( new Paragraph( "keystore名称 ", tableFont ) );
            table.addCell( cell );
            cell = new PdfPCell( new Paragraph( info.getKeystoreName(), tableFont ) );

            table.addCell( cell );

            cell = new PdfPCell( new Paragraph( "密钥是否成对  ", tableFont ) );
            table.addCell( cell );
            cell = new PdfPCell( new Paragraph( ( info.isPri() ) ? "是" : "否", tableFont ) );
            table.addCell( cell );
        }

        section1.add( table );
    }

    private void addJMSSection( Chapter chapter )
        throws JDOMException, IOException
    {
        Paragraph title11 = new Paragraph( "消息队列", sectionFont );
        Section section1 = chapter.addSection( title11 );
        Paragraph someSectionText = new Paragraph( "以下是消息队列列表，请按照需要配置.", textFont );

        section1.add( someSectionText );
        someSectionText = new Paragraph( "  ", textFont );
        section1.add( someSectionText );

        String serverName = System.getProperty( "server-name" );
        if ( serverName == null )
        {
            return;
        }
        File file = new File( System.getProperty( "SESAME_HOME" ), "conf/" + serverName + ".xml" );

        if ( !( file.exists() ) )
        {
            return;
        }
        LOG.debug( "Loading addresses from file: " + file );
        SAXBuilder builder = new SAXBuilder();
        org.jdom.Document doc = builder.build( file );
        Element root = doc.getRootElement();
        Iterator itr = root.getChildren( "jms" ).iterator();

        PdfPTable table = new PdfPTable( 2 );
        PdfPCell cell = null;
        while ( itr.hasNext() )
        {
            Element jms = (Element) itr.next();
            Element appinfo = jms.getChild( "app-info", jms.getNamespace() );
            if ( appinfo == null )
            {
                continue;
            }

            cell = new PdfPCell( new Paragraph( "JNDI名称", tableFont ) );
            cell.setBackgroundColor( new Color( 192, 192, 192 ) );
            table.addCell( cell );
            cell = new PdfPCell( new Paragraph( appinfo.getChildText( "jndi-name" ), tableFont ) );

            cell.setBackgroundColor( new Color( 192, 192, 192 ) );
            table.addCell( cell );

            cell = new PdfPCell( new Paragraph( "消息类型 ", tableFont ) );
            table.addCell( cell );
            cell = new PdfPCell( new Paragraph( appinfo.getChildText( "type" ), tableFont ) );

            table.addCell( cell );

            cell = new PdfPCell( new Paragraph( "消息路由端口", tableFont ) );
            table.addCell( cell );
            cell = new PdfPCell( new Paragraph( appinfo.getChildText( "activemq-broker-port" ), tableFont ) );

            table.addCell( cell );
        }

        section1.add( table );
    }

    private void addDatabaseSection( Chapter chapter )
        throws JDOMException, IOException
    {
        Paragraph title11 = new Paragraph( "数据源", sectionFont );
        Section section1 = chapter.addSection( title11 );
        Paragraph someSectionText = new Paragraph( "以下是数据源列表，请按照需要配置.", textFont );
        section1.add( someSectionText );
        someSectionText = new Paragraph( "  ", textFont );
        section1.add( someSectionText );

        String serverName = System.getProperty( "server-name" );
        if ( serverName == null )
        {
            return;
        }
        File file = new File( System.getProperty( "SESAME_HOME" ), "conf/" + serverName + ".xml" );

        if ( !( file.exists() ) )
        {
            return;
        }
        LOG.debug( "Loading database from file: " + file );
        SAXBuilder builder = new SAXBuilder();
        org.jdom.Document doc = builder.build( file );
        Element root = doc.getRootElement();
        Iterator itr = root.getChildren( "jdbc" ).iterator();

        PdfPTable table = new PdfPTable( 2 );
        PdfPCell cell = null;
        while ( itr.hasNext() )
        {
            Element jdbc = (Element) itr.next();
            if ( jdbc == null )
            {
                continue;
            }
            Iterator dbIter = jdbc.getChildren( "datasource" ).iterator();
            while ( dbIter.hasNext() )
            {
                Element database = (Element) dbIter.next();
                if ( database == null )
                {
                    continue;
                }
                Element jndi = database.getChild( "jndi-name", database.getNamespace() );
                Element appinfo = database.getChild( "app-info", database.getNamespace() );
                if ( jndi == null )
                {
                    continue;
                }
                if ( appinfo == null )
                {
                    continue;
                }

                cell = new PdfPCell( new Paragraph( "JNDI名称", tableFont ) );
                cell.setBackgroundColor( new Color( 192, 192, 192 ) );
                table.addCell( cell );
                cell = new PdfPCell( new Paragraph( jndi.getText(), tableFont ) );
                cell.setBackgroundColor( new Color( 192, 192, 192 ) );
                table.addCell( cell );

                cell = new PdfPCell( new Paragraph( "驱动类", tableFont ) );
                table.addCell( cell );
                cell = new PdfPCell( new Paragraph( appinfo.getChildText( "driver-class" ), tableFont ) );

                table.addCell( cell );

                cell = new PdfPCell( new Paragraph( "连接URL", tableFont ) );
                table.addCell( cell );
                cell = new PdfPCell( new Paragraph( appinfo.getChildText( "url" ), tableFont ) );

                table.addCell( cell );

                cell = new PdfPCell( new Paragraph( "用户名", tableFont ) );
                table.addCell( cell );
                cell = new PdfPCell( new Paragraph( appinfo.getChildText( "username" ), tableFont ) );

                table.addCell( cell );

                cell = new PdfPCell( new Paragraph( "密码", tableFont ) );
                table.addCell( cell );
                cell = new PdfPCell( new Paragraph( appinfo.getChildText( "password" ), tableFont ) );

                table.addCell( cell );

                cell = new PdfPCell( new Paragraph( "最大空闲连接", tableFont ) );
                table.addCell( cell );
                cell = new PdfPCell( new Paragraph( appinfo.getChildText( "max-idle" ), tableFont ) );

                table.addCell( cell );

                cell = new PdfPCell( new Paragraph( "最大等待连接时间", tableFont ) );
                table.addCell( cell );
                cell = new PdfPCell( new Paragraph( appinfo.getChildText( "max-wait" ), tableFont ) );

                table.addCell( cell );

                cell = new PdfPCell( new Paragraph( "最大活动连接数", tableFont ) );
                table.addCell( cell );
                cell = new PdfPCell( new Paragraph( appinfo.getChildText( "max-active" ), tableFont ) );

                table.addCell( cell );

                cell = new PdfPCell( new Paragraph( "初始化连接池数量", tableFont ) );
                table.addCell( cell );
                cell = new PdfPCell( new Paragraph( appinfo.getChildText( "initial-size" ), tableFont ) );

                table.addCell( cell );
            }

        }

        section1.add( table );
    }
}