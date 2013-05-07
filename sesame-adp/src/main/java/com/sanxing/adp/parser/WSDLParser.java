package com.sanxing.adp.parser;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanxing.adp.ADPException;
import com.sanxing.adp.runtime.Registry;
import com.sanxing.adp.util.XJUtil;

public class WSDLParser
{
    WSDLFactory wsdlFactory = null;

    private final List<PortTypeInfo> portTypeInfos = new LinkedList();

    private SchemaHolder schemaHolder;

    static Logger LOG = LoggerFactory.getLogger( WSDLParser.class );

    private CodeGenerator generator;

    public void setGenerator( CodeGenerator generator )
    {
        this.generator = generator;
    }

    public void parse4CodeGen( String url )
    {
        try
        {
            wsdlFactory = WSDLFactory.newInstance();
            WSDLReader reader = wsdlFactory.newWSDLReader();
            reader.setFeature( "javax.wsdl.verbose", false );
            Definition def = reader.readWSDL( null, url );

            handleDefination( def );

            generator.addSchemaHolder( schemaHolder );
            if ( LOG.isTraceEnabled() )
            {
                LOG.trace( "parse service [" + def.getQName() + " ]ok......................." );
            }
            for ( PortTypeInfo typeInfo : portTypeInfos )
            {
                generator.addPortType( typeInfo );
            }
            generator.generate();
        }
        catch ( WSDLException e )
        {
            LOG.error( "parse wsdl file [" + url + "] err", e );
            throw new ADPException( "9999", "parse sercive definition err" );
        }
        catch ( Exception e )
        {
            LOG.error( "", e );
            throw new ADPException( "9999", e.getMessage() );
        }
    }

    public void parse4runTime( Definition def, ClassLoader loader )
    {
        try
        {
            handleDefination( def );
            if ( LOG.isTraceEnabled() )
            {
                LOG.trace( "parse service [" + def.getQName() + " ]ok......................." );
            }

            for ( PortTypeInfo pi : portTypeInfos )
            {
                for ( OperationInfo oi : pi.getOperations() )
                {
                    oi.init4runtime( pi, loader );
                }
            }
        }
        catch ( WSDLException e )
        {
            throw new ADPException( "9999", "parse sercive definition err" );
        }
        catch ( Exception e )
        {
            LOG.error( "", e );
            throw new ADPException( "9999", e.getMessage() );
        }
    }

    public void handleDefination( Definition def )
        throws Exception
    {
        if ( LOG.isTraceEnabled() )
        {
            LOG.trace( " begin to analyze wsdl definition" );
        }

        schemaHolder = new SchemaHolder( def );
        Map portTypes = def.getAllPortTypes();
        Registry registry = Registry.getInstance();
        Iterator iterPortypeNames = portTypes.keySet().iterator();
        while ( iterPortypeNames.hasNext() )
        {
            PortType portType = (PortType) portTypes.get( iterPortypeNames.next() );
            if ( LOG.isTraceEnabled() )
            {
                LOG.trace( "found portType (interface):" + portType.getQName() );
            }
            PortTypeInfo portTypeInfo = new PortTypeInfo();
            portTypeInfos.add( portTypeInfo );
            portTypeInfo.setName( portType.getQName() );
            portTypeInfo.setDefinationName( def.getQName() );
            registry.registerInfterface( portTypeInfo );
            List operations = portType.getOperations();
            for ( int i = 0; i < operations.size(); ++i )
            {
                Operation operation = (Operation) operations.get( i );
                OperationInfo operationInfo = new OperationInfo();
                if ( LOG.isTraceEnabled() )
                {
                    LOG.debug( "found operation, name:[" + operation.getName() + "]" );
                }
                operationInfo.setOperationName( operation.getName() );
                portTypeInfo.addOperation( operationInfo );
                if ( operation.getDocumentationElement() != null )
                {
                    operationInfo.setDescription( operation.getDocumentationElement().getTextContent() );
                    if ( LOG.isTraceEnabled() )
                    {
                        LOG.trace( "found operation commment [" + operationInfo.getDescription() + "]" );
                    }
                }

                int count = 0;
                if ( ( operation.getInput() != null ) && ( operation.getInput().getMessage() != null ) )
                {
                    ++count;
                    Map inputParts = operation.getInput().getMessage().getParts();
                    Iterator inputPartsIter = inputParts.keySet().iterator();
                    while ( inputPartsIter.hasNext() )
                    {
                        Part part = (Part) inputParts.get( inputPartsIter.next() );
                        PartInfo parameterInfo = new PartInfo();
                        parameterInfo.setType( 0 );
                        fillParameterInfo( part, parameterInfo );
                        if ( LOG.isTraceEnabled() )
                        {
                            LOG.trace( "found parameters [" + count + "] content is [" + parameterInfo + "]" );
                        }
                        operationInfo.addParamter( parameterInfo );
                    }
                }

                count = 0;
                if ( ( operation.getOutput() != null ) && ( operation.getOutput().getMessage() != null ) )
                {
                    Map outputParts = operation.getOutput().getMessage().getParts();
                    Iterator outputPartsIter = outputParts.keySet().iterator();
                    while ( outputPartsIter.hasNext() )
                    {
                        Part part = (Part) outputParts.get( outputPartsIter.next() );
                        PartInfo parameterInfo = new PartInfo();

                        parameterInfo.setType( 1 );
                        fillParameterInfo( part, parameterInfo );
                        if ( LOG.isTraceEnabled() )
                        {
                            LOG.trace( "found result [" + count + "] content is [" + parameterInfo + "]" );
                        }
                        operationInfo.addResult( parameterInfo );
                    }
                }

                Map faults = operation.getFaults();
                Iterator faultsKeys = faults.keySet().iterator();
                while ( faultsKeys.hasNext() )
                {
                    Fault fault = (Fault) faults.get( faultsKeys.next() );
                    FaultInfo faultInfo = new FaultInfo();
                    faultInfo.setName( StringUtils.capitalize( fault.getName() ) );
                    operationInfo.addFault( faultInfo );
                    Map faultParts = fault.getMessage().getParts();
                    Iterator faultPartsIter = faultParts.keySet().iterator();
                    while ( faultPartsIter.hasNext() )
                    {
                        Part faultPart = (Part) faultParts.get( faultPartsIter.next() );
                        PartInfo partInfo = new PartInfo();
                        partInfo.setType( 2 );
                        fillParameterInfo( faultPart, partInfo );
                        if ( LOG.isTraceEnabled() )
                        {
                            LOG.trace( "found fault [" + count + "] content is [" + partInfo + "]" );
                        }
                        faultInfo.addPart( partInfo );
                    }
                }
            }
        }
    }

    private void fillParameterInfo( Part part, PartInfo parameterInfo )
    {
        try
        {
            parameterInfo.setElementName( part.getElementName() );
            parameterInfo.setName( part.getName() );
            if ( part.getElementName() == null )
            {
                QName typeName = part.getTypeName();
                String type = typeName.getLocalPart();
                if ( !( typeName.getNamespaceURI().equals( "http://www.w3.org/2001/XMLSchema" ) ) )
                {
                    return;
                }
                parameterInfo.setJavaType( XJUtil.xsType2Java( type ) );
                parameterInfo.setXsType( type );
                return;
            }

            String namespace = parameterInfo.getElementName().getNamespaceURI();
            String localPart = parameterInfo.getElementName().getLocalPart();
            XmlSchemaElement schemaElement = schemaHolder.getTypes().get( parameterInfo.getElementName() );
            if ( schemaElement == null )
            {
                throw new RuntimeException( "element [" + parameterInfo.getElementName() + "] not define in schema" );
            }
            if ( ( schemaElement.getSchemaTypeName() != null )
                && ( schemaElement.getSchemaTypeName().getNamespaceURI().equals( "http://schemas.xmlsoap.org/soap/envelope/" ) ) )
            {
                String type = schemaElement.getSchemaTypeName().getLocalPart();

                parameterInfo.setJavaType( XJUtil.xsType2Java( type ) );
                parameterInfo.setXsType( type );
                return;
            }
            parameterInfo.setJavaType( XJUtil.ns2package( namespace ) + "." + StringUtils.capitalize( localPart ) );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "fill in parameter info err", e );
        }
    }
}