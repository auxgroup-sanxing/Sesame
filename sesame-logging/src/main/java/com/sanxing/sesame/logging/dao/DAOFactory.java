package com.sanxing.sesame.logging.dao;

public class DAOFactory
{
    private static DAOFactory daoFactory = null;

    public static DAOFactory getDaoFactoryInstance()
    {
        if ( daoFactory == null )
        {
            synchronized ( DAOFactory.class )
            {
                if ( daoFactory == null )
                {
                    daoFactory = new DAOFactory();
                }
            }
        }
        return daoFactory;
    }

    public SesameBaseDAO productDAO( BaseBean bean, boolean callout )
    {
        if ( bean instanceof LogQueryBean )
        {
            if ( callout )
            {
                return new LogCalloutQueryDAO();
            }
            return new LogQueryDAO();
        }
        if ( bean instanceof LogBean )
        {
            if ( callout )
            {
                return new SesameCalloutLogDAO();
            }
            return new SesameLogDAO();
        }

        throw new RuntimeException( "unsupported type" );
    }
}