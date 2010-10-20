/*
 * Copyright (c) 2010 The Jackson Laboratory
 * 
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jax.pubarray.server;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.jax.pubarray.db.ConnectionManager;
import org.jax.pubarray.db.ExperimentMetadata;
import org.jax.pubarray.db.PersistenceManager;

/**
 * Creates a database connection and injects it into the servlet context
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public abstract class DatabaseServletContextListener implements ServletContextListener
{
    private static final Logger LOG = Logger.getLogger(
            DatabaseServletContextListener.class.getName());
    
    private static final String DATABASE_PATH_ATTRIBUTE =
        DatabaseServletContextListener.class.getName() + ".databasePath";
    
    private ConnectionManager connectionManager;
    
    /**
     * The servlet context attribute used for the connection
     */
    public static final String CONNECTION_ATTRIBUTE =
        DatabaseServletContextListener.class.getName() + ".connection";
    
    /**
     * The servlet context attribute used for the experiment metadata
     */
    public static final String EXPERIMENT_METADATA_ATTRIBUTE = "experimentMetadata";
    
    /**
     * {@inheritDoc}
     */
    public void contextInitialized(ServletContextEvent sce)
    {
        LOG.info("initializing");
        
        try
        {
            ServletContext servletContext = sce.getServletContext();
            String databasePath =
                servletContext.getInitParameter(DATABASE_PATH_ATTRIBUTE);
            
            this.connectionManager = this.createConnectionManager(databasePath);
            
            LOG.info("using database path: " + databasePath);
            
            Connection connection = this.connectionManager.createConnection();
            servletContext.setAttribute(
                    CONNECTION_ATTRIBUTE,
                    connection);
            
            PersistenceManager persistenceManager = new PersistenceManager();
            ExperimentMetadata experimentMetadata =
                persistenceManager.getExperimentMetadata(connection);
            servletContext.setAttribute(
                    EXPERIMENT_METADATA_ATTRIBUTE,
                    experimentMetadata);
            
            
            LOG.info("done initializing");
        }
        catch(Exception ex)
        {
            LOG.log(Level.SEVERE,
                    "Failed to initialize connection " +
                    "because of exception",
                    ex);
        }
    }
    
    /**
     * Creates a new connection manager with the given database path
     * @param databasePath
     *          the path
     * @return
     *          the connection manager
     */
    protected abstract ConnectionManager createConnectionManager(String databasePath);

    /**
     * {@inheritDoc}
     */
    public void contextDestroyed(ServletContextEvent sce)
    {
        LOG.info("cleaning up");
        
        final ServletContext servletContext = sce.getServletContext();
        this.connectionManager.shutdownDatabase(
                (Connection)servletContext.getAttribute(CONNECTION_ATTRIBUTE));
        
        LOG.info("done cleaning up");
    }
}
