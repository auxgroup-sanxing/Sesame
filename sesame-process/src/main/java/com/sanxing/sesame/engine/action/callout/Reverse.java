package com.sanxing.sesame.engine.action.callout;

import java.util.ArrayList;
import java.util.List;

import com.sanxing.sesame.engine.action.flow.exceptions.Catcher;
import com.sanxing.sesame.engine.context.DataContext;

public class Reverse
    implements Comparable<Reverse>
{
    private String group;

    private int index;

    private final List<CatchClause> clauses = new ArrayList();

    private DataContext snapshot;

    @Override
    public int compareTo( Reverse o )
    {
        if ( index == o.index )
        {
            return 0;
        }
        if ( index < o.index )
        {
            return -1;
        }
        return 1;
    }

    public CatchClause getClause( String exceptionKey )
    {
        for ( Object element : clauses )
        {
            CatchClause clause = (CatchClause) element;
            if ( Catcher.isCatchable( exceptionKey, clause.getExceptionKeys() ) )
            {
                return clause;
            }
        }
        return null;
    }

    public void put( String[] catches, List<?> actions, boolean instantly )
    {
        CatchClause clause = new CatchClause();
        clause.catches = catches;
        clause.actions = actions;
        clause.instantly = instantly;
        clauses.add( clause );
    }

    public String getGroup()
    {
        return group;
    }

    public void setGroup( String group )
    {
        this.group = group;
    }

    public int getIndex()
    {
        return index;
    }

    public void setIndex( int index )
    {
        this.index = index;
    }

    public DataContext getSnapshot()
    {
        return snapshot;
    }

    public void setSnapshot( DataContext snapshot )
    {
        this.snapshot = snapshot;
    }

    @Override
    public String toString()
    {
        return "Reverse [index=" + index + ", group=" + group + ", catches=" + clauses + "]";
    }

    public static class CatchClause
    {
        private String[] catches;

        private List<?> actions;

        private boolean instantly;

        public String[] getExceptionKeys()
        {
            return catches;
        }

        public List<?> getActions()
        {
            return actions;
        }

        public boolean isInstantly()
        {
            return instantly;
        }
    }
}