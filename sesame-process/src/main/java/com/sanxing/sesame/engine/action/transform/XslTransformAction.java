package com.sanxing.sesame.engine.action.transform;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;

import com.sanxing.sesame.constants.ExchangeConst;
import com.sanxing.sesame.engine.ExecutionEnv;
import com.sanxing.sesame.engine.action.AbstractAction;
import com.sanxing.sesame.engine.action.Constant;
import com.sanxing.sesame.engine.context.DataContext;
import com.sanxing.sesame.engine.context.Variable;
import com.sanxing.sesame.engine.xslt.TransformerManager;

import static com.sanxing.sesame.engine.ExecutionEnv.*;

public class XslTransformAction
    extends AbstractAction
{
    private String xsltPath;

    private String toVarName;

    private Element actionEl;

    private List<Namespace> namespaces;

    @Override
    public void doinit( Element actionEl )
    {
        namespaces = actionEl.getDocument().getRootElement().getAdditionalNamespaces();
        this.actionEl = actionEl;
        xsltPath = getPath( actionEl );
        toVarName = actionEl.getAttributeValue( Constant.ATTR_TO_VAR_NAME );
    }

    private String getPath( Element configEl )
    {
        int index = 0;
        String path = "";
        for ( Element el = configEl; el != null; el = el.getParentElement() )
        {
            index = ( el.getParentElement() != null ) ? el.getParentElement().indexOf( el ) : 0;
            path = "/" + el.getName() + "[" + index + "]" + path;
        }
        return path;
    }

    @Override
    public void dowork( DataContext ctx )
    {
        ClassLoader loader = (ClassLoader) ctx.getExecutionContext().get( CLASSLOADER );
        ClassLoader savedCl = Thread.currentThread().getContextClassLoader();
        try
        {
            if ( loader != null )
            {
                Thread.currentThread().setContextClassLoader( loader );
            }

            Transformer transformer = TransformerManager.getTransformer( actionEl, namespaces );

            Map<String, String> env = ExecutionEnv.export();
            for ( String name : env.keySet() )
            {
                Object value = ctx.getExecutionContext().get( env.get( name ) );
                transformer.setParameter( name, ( value != null ) ? value : "" );
            }

            Document document = new Document();
            Element contextEl = new Element( ExchangeConst.CONTEXT );
            document.setRootElement( contextEl );
            Set<Map.Entry<String, Variable>> variables = ctx.getVariables().entrySet();
            for ( Map.Entry var : variables )
            {
                String varName = (String) var.getKey();
                Variable variable = (Variable) var.getValue();
                if ( variable.getVarType() == 0 )
                {
                    Element elem = (Element) variable.get();
                    Document src = elem.getDocument();
                    if ( src == null )
                    {
                        src = new Document( elem );
                    }
                    Element varEl = (Element) elem.clone();
                    varEl.setName( varName );
                    varEl.setNamespace( Namespace.NO_NAMESPACE );
                    contextEl.addContent( varEl );
                }
            }
            JDOMSource source = new JDOMSource( document );
            JDOMResult result = new JDOMResult();
            transformer.transform( source, result );
            Document resultDoc = result.getDocument();
            Element root = resultDoc.getRootElement();
            Variable resultVar = new Variable( root, 0 );
            ctx.addVariable( toVarName, resultVar );
        }
        catch ( TransformerException e )
        {
            throw new RuntimeException( e.getMessage(), ( e.getCause() != null ) ? e.getCause() : e );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( savedCl );
        }
    }
}